// Thread via extend/public class -- jd
// adapted from:
// https://www.w3schools.com/java/java_threads.asp

/*
   to execute in a terminal window:
   $ javac Main.java
   $ java Main
*/

/*
public class Main extends Thread {
    public static void main(String[] args) {
        Main thread = new Main();
        thread.start();
        System.out.println("This code is outside of the thread");
    }

    public void run() {
        System.out.println("Running thread");

        for(int i = 4; i > 0; i--) {
            System.out.println("Thread: " + i);
            // Let the thread sleep for a while.
            // Thread.sleep(50);
        }

        System.out.println("Thread exiting.");
    }
}
*/

package tutorial;

class RunnableDemo implements Runnable {
    private Thread t;
    private String threadName;

    RunnableDemo( String name) {
        threadName = name;
        System.out.println("Creating " +  threadName );
    }

    public void run() {
        System.out.println("Running " +  threadName );
        try {
            for(int i = 4; i > 0; i--) {
                System.out.println("Thread: " + threadName + ", " + i);
                // Let the thread sleep for a while.
                Thread.sleep(50);
            }
        } catch (InterruptedException e) {
            System.out.println("Thread " +  threadName + " interrupted.");
        }
        System.out.println("Thread " +  threadName + " exiting.");
    }

    public void start () {
        System.out.println("Starting " +  threadName );
        if (t == null) {
            t = new Thread (this, threadName);
            t.start ();
        }
    }
}

public class Main {

    public static void main(String args[]) {
        RunnableDemo R1 = new RunnableDemo( "Thread-1");
        R1.start();

        RunnableDemo R2 = new RunnableDemo( "Thread-2");
        R2.start();
    }
}