/*
Multiple implementations of Fibonacci calculation for lab 1.
Author: Vincent Yu
Date: 02/23/2021

NOTE: Discussions are at the bottom.
 */


package lab1;   // Delete this if not running under package named lab1


// Java native imports
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;


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

        // Base case: 0th fibonacci number should be 0.
        if (n == 0) {
            return 0;
        }
        // Base case 2: 1st and 2nd fibonacci number should be 1
        else if (n <= 2) {
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

        // Base case: 0th fibonacci number should be 0.
        if (n == 0) {
            result = 0;
        }
        // Base case 2: 1st and 2nd fibonacci number should be 1
        else if (n <= 2) {
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

        System.out.println("Looking for Fib(" + n + ")... \n");

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
        System.out.println("Time Elapsed: " + (endConc - startConc) + " ms \n");
    }

    public static void main(String[] args) throws IOException {

        // Catch input numbers
        BufferedReader reader = new BufferedReader (new InputStreamReader(System.in));

        System.out.println("Enter all the nth fibonacci number you are looking for: ");

        // Split the string into individual numbers
        String[] inputNums = reader.readLine().split(" ");

        // Convert the list of strings into integer inputs
        ArrayList<Integer> numbers = new ArrayList<>();

        for(String input: inputNums){
            numbers.add(Integer.parseInt(input));
        }

        // Run comparison
        for (int num: numbers){
            comparison(num);
        }

    }
}


/*
Discussion:

1. Runtime (ms) comparison result (n = 0 to 19):
          n	    0	1	2	3	4	5	6	7	8	9	10	11	12	13	14	15	16	17	18	19
  Method
Recursive	    1	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0
Concurrent	    1	1	0	0	1	0	1	1	2	2	4	14	11	16	31	48	81	156	302	500

2. Findings:

    * As n increases, the concurrent method now requires more threads, which results in longer runtime. Meanwhile,
        recursive method is steadily running at 0 millisecond. Both methods return the same correct result.

    * (On my machine) recursive method can still be used for n larger than 19. However, the concurrent method is unable
        to create the correct number of threads due to outOfMemoryError. As a result, when the recursive method is able
        to find the nth fibonacci number in less than 2 ms, the concurrent method is giving out the wrong answer and
        while running for much longer.

3. Explanation:

    * Even though recursive fibonacci method has an exponential time complexity, it can still handle situations when n
        is relatively small. The space complexity is linear, so memory is not a big issue.

    * For concurrent method, the number of threads created grows exponentially. At each stage, fib(n) is split into 2
        new threads, fib(n-1) and fib(n-2). Each of the two threads will then split again, all the way until they reach
        the base case. This is why one may run into outOfMemoryError since there are more threads than what JVM can
        handle.

    * Moreover, the grab the results from sub-threads, the function need to join the two threads, wait for their
        executions to finish, and then grab the result. Each time the join call requires a "significant" amount of time
        to carry out other actions, such as waiting and synchronization. Therefore, the runtime is much higher.

4. Improvement:

    * Don't use concurrent method to calculate fibonacci number. Use a hashmap (dictionary) method instead.

    * If concurrency is absolutely required, then create to thread to calculate fib(n-1) and fib(n-2), but the
        individual calculation should either be recursive (which is still not the best), or, as mentioned above,
        implemented using a hashmap.
*/
