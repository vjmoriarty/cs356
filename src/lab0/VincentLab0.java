/*
Multiple implementations of the thread class for lab 0.
Author: Vincent Yu
Date: 02/17/2021

NOTE: Discussions are at the bottom.
 */

package lab0;   // Delete this if you are not running in a package

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/*
 * Slightly-modified Simple Thread Example from
 * http://journals.ecs.soton.ac.uk/java/tutorial/java/threads/simple.html
 */
class SimpleThread extends Thread {

    /** Initialization of the class.
     *
     * @param name: string type, name of the thread object.
     *            Can be obtained by getName() method.
     */
    public SimpleThread(String name) {
        super(name);
    }

    /** Demo function to test out what happens if the thread is running a function. */
    public void demoFunction() {
        ArrayList<Double> lst = new ArrayList<>();

        for (int i = 0; i < 10000; i++){
            lst.add(Math.random());
        }
    }

    /** Override original thread run function with custom operation. */
    @Override
    public void run() {
        for (int i = 0; i < 10; i++) {

            System.out.println(i + " " + this.getName());

            // To test running a function, replace the try/catch block with demoFunction()
            try {
                // Can be replaced with sleep(1000)
                sleep(1000);
                // sleep((int)(Math.random() * 1000));
            } catch (InterruptedException e) {
                e.printStackTrace();    // Added print traceback
            }

            // demoFunction();
        }

        // Finishes all 10 iterations
        System.out.println("DONE! " + this.getName());
    }
}


/**
 * Runnable Interface Implementation for Threads
 */
class Runner implements Runnable {

    String name;

    /** Initialization of the class.
     *
     * @param name: string type, name of the custom runnable interface.
     */
    public Runner(String name) {
        this.name = name;
    }

    /** Demo function to test out what happens if the thread is running a function. */
    public void demoFunction() {
        ArrayList<Double> lst = new ArrayList<>();

        for (int i = 0; i < 100; i++){
            lst.add(Math.random());
        }
    }

    /** Override original run function with custom operation. */
    @Override
    public void run() {
        for (int i = 0; i < 10; i++) {

            System.out.println(i + " " + name);

            try {
                // Can be replaced with Thread.sleep(1000) or demoFunction();
                Thread.sleep(1000);
                // Thread.sleep((int)(Math.random() * 1000));
            } catch (InterruptedException e) {
                e.printStackTrace();    // Added print traceback
            }
        }

        // Finishes all 10 iterations
        System.out.println("DONE! " + name);
    }
}


/**
 * Main Class to Run Both Simple and Runnable Threads
 */
public class VincentLab0 {

    public static void main (String[] args) throws InterruptedException {

        ArrayList<String> names = new ArrayList<>();

        // Add names here to create "thread pool"
        names.add("Jamaica");
        names.add("Fiji");
        names.add("USA");

        // Original simpleThread
        System.out.println("Running SimpleThreads... \n");

        ArrayList<SimpleThread> simpleThreads = new ArrayList<>();

        // Create simpleThread for each name
        for (String name: names){
            simpleThreads.add(
                    new SimpleThread(name)
            );
        }

        // Start each thread created
        for (SimpleThread thread: simpleThreads) {
            thread.start();
        }

        // Join to allow the following commands to wait
        for (SimpleThread thread: simpleThreads){
            thread.join();
        }

        // Split the process and switch to concurrent examples
        System.out.println("\n" + "-- Switching to Runnable Threads... --\n");
        TimeUnit.SECONDS.sleep(5);
        System.out.println("Running Runnable Threads \n");

        // With runnable interface
        ArrayList<Thread> concurThreads = new ArrayList<>();

        // Create and start threads
        for (String name: names){
            concurThreads.add(
                    new Thread(new Runner(name))
            );
        }

        for (Thread t: concurThreads){
            t.start();
        }

    }
}

/*
Discussion:

1. What happens when we replace random with a constant? A function?

    With constant: each iteration sleep for the same number of seconds for all threads. In theory, this should result in
        parallelism. But the order of names in the terminal output with change from time to time. For example, Jamaica
        may appear after Fiji even though the thread technically started before Fiji.

    With function: depends on the implementation of the function. With the above implementation, since both threads are
        adding elements to their own ArrayList, it works the same as sleep function, which, without randomness in the
        number of iterations, results in parallelism. However, other implementations may result in situations where one
        thread needs to wait for another to release the lock, or even results in deadlocks.

2. What happens when we add more threads? A single thread?

    With more threads or a single thread: the total amount of time used to run these thread does not vary drastically.
        However, the resources used by these processed may vary. More threads will require more processing power.

3. Runnable interface:

    Instead of extending another class, Thread can take in an abstract interface Runnable with
    defined run function. However, the implementation is slightly different, as the sleep function now requires a thread
    call instead.

    Runnable implementation is sometimes more useful, as one java class can only call extend from one class, but it can
    extend from a class and implement from an interface at the same time.

Usage:
1. Toggle each class to define the desired run operations for each thread.
2. Add/delete names in the Example class to create/delete more threads.
3. In command line, assuming not using package, run javac VincentLab0.java, then java VincentLab0.
    If class not found, add the package name or declare the proper classpath in the command line.
4. If using Eclipse or IntelliJ, just click run.
 */
