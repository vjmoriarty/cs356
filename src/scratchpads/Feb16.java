package scratchpads;

// Thread via extend/public class -- jd
// adapted from:
// https://www.w3schools.com/java/java_threads.asp

/* 
   to execute in a terminal window:
   $ javac Feb16.java
   $ java Feb16
*/

class T1 extends Thread {

    public T1(String name){
        super(name);
    }

    public void run() {
        for (int i = 0; i < 10; i++){
            System.out.println("This code is running in a thread");
        }
    }
}

class T2 extends Thread {

    public T2(String name){
        super(name);
    }

    public void run() {
        for (int i = 0; i < 10; i++){
            System.out.println("This code is running in a second thread");
        }
    }
}

public class Feb16 {

    public static void main(String[] args){
        T1 t1 = new T1("1");
        T2 t2 = new T2("2");

        t1.start();
        t2.start();
    }
}

/**
 * thread 1 --> x = 1, y = y + x;
 * thread 2 --> y = 2, x = x + y;
 * deadlock
 */
