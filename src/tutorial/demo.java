package tutorial;

class Runner extends Thread{

    public void run(){
        double seconds = Math.random();
        try {
            sleep((long) seconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Done in " + seconds);
    }
}

public class demo {
    public static void main(String args[]){
        Runner x = new Runner();
        // Runner.start();
    }
}
