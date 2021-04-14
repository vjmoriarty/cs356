package jungle;
import java.util.Arrays;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author davew
 *
 * The Ladder class is NOT a kind of thread,
 *  since it doesn't actually do anything except get used by Apes.
 * The ladder just keeps track of how many apes are on each rung.
 */

class Ladder {
    private volatile int rungCapacity[];

    private final Lock lock = new ReentrantLock();
    private final Condition oppoDirection = lock.newCondition();   // condition to signal if the ladder is clear
    private final Condition nextRung = lock.newCondition();        // condition to signal if the next rung is clear

    private volatile boolean eastBound = true;
    private volatile int numApes;

    public Ladder(int _nRungs) {
        rungCapacity = new int[_nRungs];
        // capacity 1 available on each rung
        for (int i=0; i<_nRungs; i++)
            rungCapacity[i] = 1;
    }

    public int nRungs() {
        return rungCapacity.length;
    }

    public synchronized void incApe() {
        numApes++;
    }

    public synchronized void decApe() {
        numApes--;
    }

    public synchronized int getNumApe() {
        return numApes;
    }

    // return True if you succeed in grabbing the rung
    public boolean grabRung(int which, boolean goingEast) throws InterruptedException {

        lock.lock();

        try {
            while (getNumApe() != 0 && goingEast != eastBound) {
                oppoDirection.await();
                System.out.println("Waiting on the opposite side...");
            }
            while (rungCapacity[which] < 1) {
                nextRung.await();
                System.out.println("Waiting for the ape in front to leave...");
            }

            // Grab rung once it's safe to do so
            rungCapacity[which]--;

            System.out.println("The ladder now looks like: " + Arrays.toString(rungCapacity));

            // If this is the first Ape on ladder, change ladder direction
            if (getNumApe() == 0) {
                eastBound = goingEast;
                System.out.println("The ladder is now " + (goingEast ? "east " : "west ") + "bound only.");
            }

            // Since some ape is grabbing the rung, the ladder is no longer empty
            incApe();

            return true;

        } finally {
            lock.unlock();
        }
    }

    public void releaseRung(int which) {
        lock.lock();

        try {
            rungCapacity[which]++;

            // Decrease ape by 1 if ape reaches the other side
            if ((which + 1) == nRungs()) {
                decApe();
            }

            if (getNumApe() == 0){
                oppoDirection.signalAll();
            } else {
                nextRung.signal();
            }

        } finally {
            lock.unlock();
        }
    }
}

/*
 * @author davew
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
            if (!_ladderToCross.grabRung(startRung, _goingEast)) {
                System.out.println("  Ape " + _name + " has been eaten by the crocodiles!");
                return;  // died
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (debug)
            System.out.println("Ape " + _name + "  got  rung " + startRung);
        for (int i = startRung+move; i!=endRung+move; i+=move) {
            Jungle.tryToSleep(rungDelayMin, rungDelayVar);
            if (debug)
                System.out.println("Ape " + _name + " wants rung " + i);
            try {
                if (!_ladderToCross.grabRung(i, _goingEast)) {
                    System.out.println("Ape " + _name + ": AAaaaaaah!  falling off the ladder :-(");
                    System.out.println("  Ape " + _name + " has been eaten by the crocodiles!");
                    _ladderToCross.releaseRung(i-move); /// so far, we have no way to wait, so release the old lock as we die :-(
                    return;  //  died
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (debug)
                System.out.println("Ape " + _name + "  got  " + i + " releasing " + (i-move));
            _ladderToCross.releaseRung(i-move);
        }
        if (debug)
            System.out.println("Ape " + _name + " releasing " + endRung);
        _ladderToCross.releaseRung(endRung);

        System.out.println("Ape " + _name + " finished going " + (_goingEast?"East.":"West."));
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
    public static void main(String[] args) throws InterruptedException {
        //
        //  A solution for Lab 3 should work (have no deadlock, livelock, or starvation)
        //    regardless of the settings of the configuration variables below,
        //    i.e., even if there are infinite apes going both ways.
        //  It should also work regardless of timing, so any values for the
        //    timing configuration should work, and there should be no way to
        //    add spurious "tryToSleep"'s *anywhere* to mess it up.
        //
        int    eastBound = 4; // how many apes going East? use -1 for inifinity
        int    westBound = 2; // how many apes going West? use -1 for inifinity
        double apeMin = 4.0;  // how long to wait between consecutive apes going one way
        double apeVar = 1.0;  // 4 seconds is usually enough, but vary a bit to see what happens
        double sideMin = 5.0; // how long to wait before coming back across
        double sideVar = 0.0; // 5.0 seconds is usually enough

        // create a Ladder
        Ladder l = new Ladder(4);

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
        tryToSleep(sideMin, sideVar);
        //tryToSleep(100000, 1000000);

        // and create some Westbound apes who want the SAME ladder
        nRemaining = westBound;
        apeCounter = 1;
        while (nRemaining != 0) {
            Ape a = new Ape("W-"+apeCounter, l,false);
            a.start();
            apeCounter++;
            tryToSleep(apeMin, apeVar);
            if (nRemaining > 0)
                nRemaining--;
        }
    }

    private static java.util.Random dice = new java.util.Random(); // random number generator, for delays mostly
    public static void tryToSleep(double secMin, double secVar) {
        try {
            java.lang.Thread.sleep(Math.round(secMin*1000) + Math.round(dice.nextDouble()*(secVar)*1000));
        } catch (InterruptedException e) {
            System.out.println("Not Handling interruptions yet ... just going on with the program without as much sleep as needed ... how appropriate!");
        }
    }
}

