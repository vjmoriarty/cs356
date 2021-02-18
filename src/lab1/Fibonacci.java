/*
Multiple implementations of Fibonacci calculation for lab 1.
Author: Vincent Yu
Date: 02/17/2021

NOTE: Discussions are at the bottom.
 */


package lab1;   // Delete this if not running under package named lab1


/** Recursive and sequential fibonacci calculation */
class FibRecursive {

    /** Recursive method to find the nth fibonacci number.
     *
     * @param n: Integer type, the nth fibonacci number.
     *         n should be non-negative.
     *
     * @return sum of the (n-1)th and (n-2)th fibonacci number of the current number.
     */
    public static int fib(int n) {

        // Base case: return 1 if n is 0 or 1
        if (n <= 1) {
            return 1;
        }
        else {
            // Fib(n) = Fib(n-1) + Fib(n-2)
            return fib(n - 1) + fib(n - 2);
        }
    }
}


/** Fibonacci calculation implemented in thread form */
class FibConcurrent extends Thread{

    // Attributes
    private int n;
    public int result;

    /** Initialization of the class.
     *
     * @param n: Integer type, the nth fibonacci number.
     *         n should be non-negative.
     */
    public FibConcurrent(int n){
        this.n = n;
    }

    /** Thread run function */
    public void run() {
        // Base case: 0th or 1st fibonacci number should be 1.
        if (n <= 1){
            result = 1;
        }
        else {
            // Create two new threads to calculate the (n-1)th and (n-2)th fibonacci number
            FibConcurrent fib1 = new FibConcurrent(n - 1);
            FibConcurrent fib2 = new FibConcurrent(n - 2);

            fib1.start();
            fib2.start();

            // Use join to wait for the results from both sub-threads
            try {
                fib1.join();
                fib2.join();

                result = fib1.result + fib2.result;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}


/** Main Class of Fibonacci */
public class Fibonacci {

    /** Aggregated comparison method to test out sequential and concurrent methods side by side.
     *
     * @param n: Integer type, the nth fibonacci number.
     */
    public static void comparison(int n){

        // Time the recursive method first
        long startRec = System.currentTimeMillis();

        int resultRec = FibRecursive.fib(n);

        long endRec = System.currentTimeMillis();

        // Now, time the concurrent method
        long startConc = System.currentTimeMillis();

        FibConcurrent fib = new FibConcurrent(n);

        fib.start();

        try{
            fib.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        long endConc = System.currentTimeMillis();

        // Output the results and the time both methods took to terminal
        System.out.println("Recursive Method:");
        System.out.println("Result: " + resultRec);
        System.out.println("Time Elapsed: " + (endRec - startRec) + " ms");

        System.out.println();

        System.out.println("Concurrent Method:");
        System.out.println("Result: " + fib.result);
        System.out.println("Time Elapsed: " + (endConc - startConc) + " ms");
    }

    public static void main(String[] args) {

        // Catch input number
        int n = Integer.parseInt(args[0]) - 1;  // Get rid of -1 if you count from 0

        // Run comparison
        comparison(n);
    }
}


/*
Findings:
Recursive Method:
Result: 17711
Time Elapsed: 2 ms

Concurrent Method:
Result: 17711
Time Elapsed: 13950 ms

Reason why the concurrent one is much slower;
1. Starting 2 new threads at each splitting stage, more and more thread. However, only limited number of cores.
2. With more new threads, there are more join actions, and sync cost time.
*/
