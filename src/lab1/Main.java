package lab1;

// TODO DOCUMENTATION
class FibRecursive {

    public static int fib(int n) {
        if (n <= 1) {
            return n;
        }
        else {
            return fib(n - 1) + fib(n - 2);
        }
    }
}

// TODO DOCUMENTATION
class FibConcurrent extends Thread{
    private int n;
    public int result;

    public FibConcurrent(int n){
        this.n = n;
    }

    public void run() {
        if (n <= 1){
            result = n;
        }
        else {
            FibConcurrent fib1 = new FibConcurrent(n - 1);
            FibConcurrent fib2 = new FibConcurrent(n - 2);
            fib1.start();
            fib2.start();
            try {
                fib1.join();
                fib2.join();
                result = fib1.result + fib2.result;
            } catch (InterruptedException e) {
                // e.printStackTrace();
            }
        }
    }
}

public class Main {

    public static void comparison(int n){
        // Time the recursive method first
        long startRec = System.currentTimeMillis();

        int resultRec = FibRecursive.fib(n);

        long endRec = System.currentTimeMillis();

        System.out.println("Result: " + resultRec);
        System.out.println("Recursive method took: " + (endRec - startRec) + " ms");
        System.out.println();

        // Now, time the concurrent method
        long startConc = System.currentTimeMillis();

        FibConcurrent fib = new FibConcurrent(n);
        fib.start();
        try{
            fib.join();
        } catch (InterruptedException e) {
            // e.printStackTrace();
        }

        long endConc = System.currentTimeMillis();

        System.out.println("Result: " + fib.result);
        System.out.println("Concurrent method took: " + (endConc - startConc) + " ms");
    }

    public static void main(String[] args) {

        // Catch input number
        int n = Integer.parseInt(args[0]) - 1;

        // Run comparison
        comparison(n);

        /*
        Findings:
        1. Recursive much faster.
        2. Concurrent having out of memory error after n=18

        Reason why the concurrent one is much slower;
        1. Starting 2 new threads at each splitting stage --> out of memory problem.
        2. With more new threads, there are more join actions, and sync cost time.
        */
    }
}
