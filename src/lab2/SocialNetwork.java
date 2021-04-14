/*
Social Network Simulation for Lab 2
Author: Vincent Yu
Date: 03/02/2021

Usage:
    After compiled with javac command, run java SocialNetwork. In the terminal, it will show a prompt asking you to
    input the number of accounts you want to simulate. The program will then go into an infinite loop to simulate social
    network. To stop the simulation, press enter at the terminal.

    * To change post specific attributes (such as content), tap into class Post.
    * To change the author name logic, tap into main() in class Accounts.

NOTE: Discussions are at the bottom.
*/

package lab2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.*;


/** Post class to construct a post object in social network simulation */
class Post {

    // Attributes
    private final String author;
    private final String content;
    private final String postTime;

    /** Constructor
     * @param author: String type. The author/creator of the post.
     */
    Post(String author) {

        // Assign author to the post
        this.author = author;

        // Make a fake post with author's name
        this.content = author + " TO THE MOON !!!!!!";

        // Get current date as post time
        // Reference: https://stackabuse.com/how-to-get-current-date-and-time-in-java/
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
        this.postTime = formatter.format(date);
    }

    /** Retrieve post attributes and print the formatted version of the post. */
    public void printPostDetail() {
        System.out.println("Author: " + author);
        System.out.println("Time Posted: " + postTime);
        System.out.println("Post Content: ");
        System.out.println(content);
        System.out.println();
    }
}


/** Concurrent accounts for social network simulations */
class Accounts {

    // Declare volatile list to ensure posts are added/retrieved concurrently with no issue
    private volatile List<Post> posts = Collections.synchronizedList(new ArrayList<Post>());

    /** Synchronized post function to add posts to the volatile list.
     *
     * @param author: String type. The author/creator of the post.
     */
    public synchronized void post(String author){

        // Create new post
        Post simulatedPost = new Post(author);

        // Add to list
        posts.add(simulatedPost);

        // Print out how many posts there are currently
        System.out.println("There are currently " + posts.size() + " posts. \n");
    }

    /** Synchronized view function to retrieve the latest 6 posts. */
    public synchronized void view() {

        // Get the last 6 posts and reorder them with the last post in front
        List<Post> latestSixPosts = new ArrayList<>(posts.subList(Math.max(posts.size() - 6, 0), posts.size()));
        Collections.reverse(latestSixPosts);

        // Print out each post's details using post's built-in function
        for (Post post: latestSixPosts){
            post.printPostDetail();
        }
    }

    /** Single account social network simulation.
     *
     * At the beginning of the simulation, the function creates a fake account/author.
     *  It will then goes into an infinite loop for simulation. In each iteration, the
     *  function will pick a random number of seconds for the thread to sleep to simulate
     *  someone not using social network. Then, the function will randomly pick either
     *  post or view action.
     *
     * @throws InterruptedException: Throws an exception is the thread is interrupted.
     */
    public void simulate(String author) throws InterruptedException {

        // Running a infinite loop until thread is interrupted
        while (true) {

            // Get a random number of seconds for the thread to sleep
            int stepAway = (int)(Math.random() * 20000);

            // Simulate a person setting social media aside for a while
            System.out.println(author + " tries to step away from social media for " + stepAway / 1000 + " seconds... \n");
            Thread.sleep(stepAway);

            // Randomly pick either post or view action
            if (Math.random() < 0.5) {
                System.out.println(author + " couldn't resist the temptation and posted something...");
                post(author);

            }
            else {
                System.out.println(author + " couldn't resist the temptation and checks the latest 6 posts, which are: ");
                view();
            }
        }
    }

    /** Main function for social network simulation with multiple accounts.
     *
     * With the given input number of accounts to simulate, the function will create the
     *  corresponding number of threads with runnable interface, where single account
     *  simulation is called. Once all threads are created, the function will start the
     *  threads and the simulation won't stop until it is interrupted by terminal input
     *  (enter / next line).
     *
     * @throws IOException: Added for when readLine() is called.
     */
    public void main() throws IOException {

        // Catch input numbers
        BufferedReader reader = new BufferedReader (new InputStreamReader(System.in));

        System.out.println("Enter the number of accounts you want for simulation: ");

        // Split the string into individual elements and get the first element as input
        String[] inputNums = reader.readLine().split(" ");
        int numAccounts = Integer.parseInt(inputNums[0]);

        // Create random number generator for account name creation
        // Reference: https://www.geeksforgeeks.org/java-util-random-nextint-java/
        Random random = new Random();

        // Create threads using Runnable
        ArrayList<Thread> accounts = new ArrayList<>();

        for (int i = 0; i < numAccounts; i++) {
            // Create the simulated account with a random author
            String author = "Bot No." + random.nextInt(numAccounts * 100000);

            Thread account = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        simulate(author);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });

            accounts.add(account);
        }

        // Start simulation
        for (Thread account: accounts){
            account.start();
        }

        // Give a prompt to stop the simulation
        System.out.println("PRESS ENTER TO STOP SIMULATION");
        reader.readLine();

        // Once enter is detected, stop all threads
        for (Thread account: accounts){
            account.interrupt();
        }

    }
}

/** Main class for social network simulation */
public class SocialNetwork {

    public static void main(String[] args) throws IOException {
        new Accounts().main();
    }
}


/* Discussions: Experiments with Synchronized & Pros and Cons of Concurrency Design

Experiments:
    * With at least one part (function or data structure) synchronized, the whole simulation will still work. However,
        the terminal output can be out of order if two actions (whether post or view) are executed at the same time.

    * The benefit of synchronizing everything is to have a formatted terminal output. In a more realistic setting,
        it is sufficient to only synchronize on the data structure or the functions.

Pros:
    * For social media, when multiple users are posting at the same time, the posts will be consistent. In other words,
        with the synchronized design, calling the post action to the same list will not cause conflict/information loss.

    * (Although not shown in the implemented codes above) When different function calls are acquiring different
        attributes (i.e., not sharing memory), concurrent design is more efficient. For example, posting and account
        registration are not sharing the same critical sections, therefore the concurrent design can efficiently use
        the resources, whereas sequential design will have to schedule the tasks in order. This means that if one user
        is creating a new account while another going to post something (but he/she is only a bit late by one second),
        the user can't post until the account registration is done with a sequential design.

Cons:

    * Common concurrency drawbacks: harder to conduct unit/integrated testing, harder to debug, harder to maintain CI/CD

    * With the current design, the synchronized functions are defined on the same (default) lock. If more volatile
        attributes are to be added, and if more functions not using the posts attribute are to be implemented, then
        the design will have to change from one unspecified lock to multiple designated locks.

Suggested Improvements:
    Instead of building the concurrent feature into the application itself, build the application as usual but handle
    the data concurrency problem with a separate data pipeline. E.g., Apache Kafka.
*/
