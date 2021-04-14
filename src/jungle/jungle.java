/*
"Apes and Ladder" implementation for lab 5.
Collaborators: Professor Dave Wonnacott, Professor John Dougherty, Vincent Yu
Date: 04/14/2021
 */

package jungle;

// Java Imports
import java.util.Arrays;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


/**
 * @author davew + Vincent Yu
 *
 * The Ladder class is NOT a kind of thread,
 *  since it doesn't actually do anything except get used by Apes.
 * The ladder just keeps track of how many apes are on each rung.
 *
 * The general idea is as follows: whichever ape grabs the ladder first decides the direction to go until the ladder
 *  is clear. The apes on the opposite site will wait until the ladder is signaled cleared. When apes are grabbing
 *  the ladder, the later ape checks if the rung in front has another ape on it, and wait if it's not available.
 */
class Ladder {

    // Attributes
    private final int[] rungCapacity;
    private final boolean debug;

    private final Object numCheck = new Object();                   // Lock for functions related to apes number checks

    private final Lock lock = new ReentrantLock();                  // Re-entrant lock for multi-conditional signaling
    private final Condition oppoDirection = lock.newCondition();    // condition to signal if the ladder is clear
    private final Condition nextRung = lock.newCondition();         // condition to signal if the next rung is clear

    private volatile boolean eastBound = true;                      // Ladder direction indicator
    private volatile int numApes;                                   // Number of apes on the ladder

    /**
     * Constructor for object initialization.
     * @param _nRungs: Integer type. The capacity of the ladder (i.e., the number of rungs).
     */
    public Ladder(int _nRungs, boolean debug) {
        this.debug = debug;
        rungCapacity = new int[_nRungs];
        // capacity 1 available on each rung
        for (int i=0; i<_nRungs; i++)
            rungCapacity[i] = 1;
    }

    /**
     * Retrieves the ladder capacity (i.e., the number of rungs).
     * @return an integer representing the ladder capacity.
     */
    public int nRungs() {
        return rungCapacity.length;
    }

    // Functions related to add/subtract/retrieve the number of apes on ladder

    /**
     * Add one ape to ladder.
     */
    public void incApe() {
        synchronized (numCheck){
            numApes++;
        }
    }

    /**
     * Remove one ape from ladder.
     */
    public void decApe() {
        synchronized (numCheck){
            numApes--;
        }
    }

    /**
     * Retrieve the number of apes on the ladder now.
     * @return numApes: Integer type. Represents the number of apes on the ladder now.
     *                      Should be non-negative.
     */
    public int getNumApe() {
        synchronized (numCheck){
            return numApes;
        }
    }

    /**
     * Concurrent grab rung function with multi-conditional wait.
     * @param name: string type. Name of the ape.
     * @param which: integer type. Index of the rung to grab.
     * @param goingEast: boolean type. True if the ape is going east, and false if the ape is going west.
     * @return true to indicate that the ape has grabbed the rung.
     * @throws InterruptedException
     */
    public boolean grabRung(String name, int which, boolean goingEast) throws InterruptedException {

        lock.lock();

        try {
            // If the ladder has another ape coming from the opposite direction
            while (getNumApe() != 0 && goingEast != eastBound) {
                // Make the ape wait until the ladder is clear
                if (debug) {
                    System.out.println("Ape " + name + " is waiting on the opposite side... \n");
                }
                oppoDirection.await();
            }
            // If the rung in front has an ape grabbing it
            while (rungCapacity[which] < 1) {
                // Make the ape wait until the rung is available again
                if (debug) {
                    System.out.println("Ape " + name + " is waiting for the ape in front to leave... \n");
                }
                nextRung.await();
            }

            // If this is the first Ape on ladder, change ladder direction to wherever this ape is going
            if (getNumApe() == 0) {
                eastBound = goingEast;
                if (debug) {
                    System.out.print("\nApe " + name + " got to the ladder first! ");
                    System.out.println("The ladder is now " + (goingEast? "east": "west") + " bound only. \n");
                }
            }

            // Check if the ape is grabbing the first rung in front of it
            boolean newApeOnLadder = ((goingEast && which == 0) || (!goingEast && which == nRungs() - 1));

            // Increase the number of apes on the ladder by 1 if this is a new ape
            if (newApeOnLadder) {
                incApe();
            }

            // Grab rung once it's safe to do so
            rungCapacity[which]--;

            // Visualize the move
            if (debug) {
                System.out.println("Ape " + name + " got rung " + which);
                System.out.println("The ladder now looks like: " + Arrays.toString(rungCapacity));
            }

            return true;

        } finally {
            lock.unlock();
        }
    }

    /**
     * Concurrent release rung function with multi-conditional notify.
     * @param which: integer type. Index of the rung to release.
     */
    public void releaseRung(String name, int which) {
        lock.lock();

        try {
            // First, release the rung
            rungCapacity[which]++;

            if (debug) {
                System.out.println("... and released rung " + which);
                System.out.println("The ladder now looks like: " + Arrays.toString(rungCapacity) + "\n");
            }

            // Decrease ape by 1 if ape reaches the other side
            boolean reachesTheOtherSide = ((eastBound && (which + 1) == nRungs()) || (!eastBound && which == 0));
            if (reachesTheOtherSide) {
                decApe();
                if (debug) {
                    System.out.println("Ape " + name + " finished going " + (eastBound?"east.":"west."));
                    System.out.println(getNumApe() + " apes left on the ladder. \n");
                }
            }

            // Once the ladder is clear, allow apes from the other side to cross
            if (getNumApe() == 0){
                if (debug) {
                    System.out.println("Ladder is now clear. \n");
                }
                oppoDirection.signalAll();
            } else {
                // If this is just a regular scenario, notify the next ape waiting to grab the released rung
                nextRung.signalAll();
            }

        } finally {
            lock.unlock();
        }
    }
}


/**
 * @author davew & Vincent Yu
 *
 * The Ape class is a kind of thread,
 *  since all Apes can go about their activities concurrently
 * Note that each Ape has his or her own name and direction,
 *  but in this system, many Apes will share one Ladder.
 */
class Ape extends Thread {
    static private final boolean debug = true;  // "static" is shared by all Apes
    static private final double rungDelayMin = 0.8;
    static private final double rungDelayVar = 1.0;
    private String _name;
    private Ladder _ladderToCross;
    private boolean _goingEast; // if false, going west

    public Ape(String name, Ladder toCross, boolean goingEast) {
        _name = name;
        _ladderToCross = toCross;
        _goingEast = goingEast;
    }

    public void run() {
        int startRung, move, endRung;
        System.out.println("Ape " + _name + " starting to go " + (_goingEast?"East.":"West."));
        if (_goingEast) {
            startRung = 0;
            endRung = _ladderToCross.nRungs()-1;
            move = 1;
        } else {
            startRung = _ladderToCross.nRungs()-1;
            endRung = 0;
            move = -1;
        }

        if (debug)
            System.out.println("Ape " + _name + " wants rung " + startRung);
        try {
            if (!_ladderToCross.grabRung(_name, startRung, _goingEast)) {
                System.out.println("  Ape " + _name + " has been eaten by the crocodiles!");
                return;  // died
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for (int i = startRung+move; i!=endRung+move; i+=move) {
            Jungle.tryToSleep(rungDelayMin, rungDelayVar);
            if (debug)
                System.out.println("Ape " + _name + " wants rung " + i);
            try {
                if (!_ladderToCross.grabRung(_name, i, _goingEast)) {
                    System.out.println("Ape " + _name + ": AAaaaaaah!  falling off the ladder :-(");
                    System.out.println("  Ape " + _name + " has been eaten by the crocodiles!");
                    _ladderToCross.releaseRung(_name, i-move); /// so far, we have no way to wait, so release the old lock as we die :-(
                    return;  //  died
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            _ladderToCross.releaseRung(_name,i-move);
        }
        if (debug)
            System.out.println("Ape " + _name + " releasing " + endRung);
        _ladderToCross.releaseRung(_name, endRung);

        return;  // survived!
    }
}


/**
 * @author davew
 *
 * This class just exists to create the objects and threads we need:
 *  One ladder and many apes.
 * You should not need to change anything here unless you want to
 *  use it to add other objects that aren't associated with some
 *  existing object (an ape or ladder).
 */
class Jungle {

    private static java.util.Random dice = new java.util.Random(); // random number generator, for delays mostly
    public static void tryToSleep(double secMin, double secVar) {
        try {
            java.lang.Thread.sleep(Math.round(secMin*1000) + Math.round(dice.nextDouble()*(secVar)*1000));
        } catch (InterruptedException e) {
            System.out.println("Not Handling interruptions yet ... just going on with the program without as much sleep as needed ... how appropriate!");
        }
    }

    public static void main(String[] args) {
        //
        //  A solution for Lab 3 should work (have no deadlock, livelock, or starvation)
        //    regardless of the settings of the configuration variables below,
        //    i.e., even if there are infinite apes going both ways.
        //  It should also work regardless of timing, so any values for the
        //    timing configuration should work, and there should be no way to
        //    add spurious "tryToSleep"'s *anywhere* to mess it up.
        //
        int eastBound = 10;     // how many apes going East? use -1 for inifinity
        int westBound = 10;     // how many apes going West? use -1 for inifinity
        double apeMin = 4.0;    // how long to wait between consecutive apes going one way
        double apeVar = 1.0;    // 4 seconds is usually enough, but vary a bit to see what happens
        double sideMin = 5.0;   // how long to wait before coming back across
        double sideVar = 0.0;   // 5.0 seconds is usually enough

        // create a Ladder
        Ladder l = new Ladder(4, true);

        // create some Eastbound apes who want that ladder
        int nRemaining = eastBound;
        int apeCounter = 1;
        while (nRemaining != 0) {
            Ape a = new Ape("E-"+apeCounter, l,true);
            a.start();
            apeCounter++;
            // tryToSleep(apeMin, apeVar);
            if (nRemaining > 0)
                nRemaining--;
        }

        // put this in to create a pause that will avoid the problem BUT OF COURSE THIS IS NOT A SOLUTION TO THE LAB!
        // tryToSleep(sideMin, sideVar);

        // and create some Westbound apes who want the SAME ladder
        nRemaining = westBound;
        apeCounter = 1;
        while (nRemaining != 0) {
            Ape a = new Ape("W-"+apeCounter, l,false);
            a.start();
            apeCounter++;
            // tryToSleep(apeMin, apeVar);
            if (nRemaining > 0)
                nRemaining--;
        }
    }
}