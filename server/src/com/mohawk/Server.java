package com.mohawk;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 * Represents a server that connects clients to play Blackjack.
 */
public class Server extends Thread {

    private Socket server;
    private DataInputStream in;
    private DataOutputStream out;

    // Tracks game state
    private static Blackjack blackjack;

    public Server(Socket theSocket, int id) throws IOException {
        super(Integer.toString(id));
        server = theSocket;
    }

    public void run() {
        String line = "start";

        try {
            System.out.println("Just connected to " + server.getRemoteSocketAddress());

            // block-level variables
            in = new DataInputStream(server.getInputStream());
            out = new DataOutputStream(server.getOutputStream());
            int id = Integer.parseInt(currentThread().getName());

            // Here we'll give them a welcome message maybe
            System.out.println("We told " + id + " welcome to blackjack! Now we go to sleep until the cards are dealt.");
            Thread.sleep(1000);

            // We're in the pre-game state
            synchronized (blackjack) {
                while (!blackjack.getReadyToPlay()) {       // does this method need to be sync if the block is sync?
                    // The cards haven't been dealt at this point
                    blackjack.wait();
                    // notifyAll is called on blackjack's monitor
                    System.out.println("Ready to play should have been set by Blackjack. We woke up on " + id);
                }
                // Wake up the next thread to get started!
                blackjack.notifyAll();
            }

            // Here we can safely broadcast the current state of play to all sockets
            // outside the sync block because we're done manipulating state
            System.out.println("Here's where we should print the state of play for " + id + " (I expect to see this twice)");
            Thread.sleep(1000);

            // Then it's time to determine whose turn it is
            synchronized (blackjack) {
                boolean userNotified = false;
                while (id != blackjack.getPlayerTakingTurn()) {
                    System.out.println("We're in the while loop because it's not" + id + "'s turn.");
                    if (!userNotified) {
                        // Print out that it's not their turn "waiting for player turns"
                        System.out.println("Client " + id + " has been told to wait.");
                        userNotified = true;
                    }
                    blackjack.wait();
                    System.out.println("Somebody called notify all and we're checking to see if it's " + id + "'s turn.");
                    // when notifyAll is called, we'll wake up and make this check again
                }

                System.out.println("We determined player " + id + " should take their turn.");
                blackjack.notifyAll();  // idk if this is necessary or not...
            }

            // Now we're going to take the player's turn!
            synchronized (blackjack) {
                System.out.println("This is where player " + id + " would take their turn inside A NEW sync block.");
                Thread.sleep(2000);
                System.out.println("We're going to increment players.");
                blackjack.incrementPlayer();
                System.out.println("We incremented the player to " + blackjack.getPlayerTakingTurn());
                System.out.println("Now we need to see if this was the last round...");

                // these are 0 index ids so if we've incremented to a number
                // higher than the highest index, we've hit the max players
                if (blackjack.getPlayerTakingTurn() == blackjack.getNumPlayers()) {
                    blackjack.setGameOver();
                }
                System.out.println("We're about to notify all threads.");
                blackjack.notifyAll();
            }

            System.out.println("We're outside the sync block which means the player has completed their turn.");

            synchronized (blackjack) {
                System.out.println("We're inside a sync block again for " + id + " and this is where we'd wait for the end of the game.");
                while(!blackjack.isGameOver()) {
                    blackjack.wait();
                    System.out.println(id + " woke up from the wait and will check to see if it's game over.");
                }
                System.out.println("It's game over! WHEW. Let's print the score.");
                blackjack.notifyAll();
            }

            // We're in the post-game state
            System.out.println("This is where we'd print the score for everyone. " + id + " successfully got here.");

            out.close();
            in.close();
            server.close();

        } catch (SocketTimeoutException s) {
            System.out.println("Socket timed out!");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            //out.writeUTF("Thank you for connecting to " + server.getLocalSocketAddress() + "\nGoodbye!");
        }

    }

    /**
     * This is where the game state and threads
     * are created and managed
     * @param args blargs
     * @throws IOException
     */
    public static void main(String[] args) throws IOException, InterruptedException {

        ArrayList<Thread> threads = new ArrayList<>();
        int port = 61013;
        int connectedUsers = 0;
        int expectedConnections = Integer.parseInt(args[0]);

        // Set up the deck
        String contents = readFile("deck.txt", Charset.defaultCharset());
        Deck deck = new Deck(contents);
        // deck.shuffle();
        blackjack = new Blackjack(deck, expectedConnections);


        ServerSocket mySocket = new ServerSocket(port);
        // Need a way to close the server without just killing it.
        while (connectedUsers < expectedConnections) {
            System.out.println("Waiting for client on port "
                    + mySocket.getLocalPort() + "...");
            Socket server = mySocket.accept(); //blocking
            try {
                Thread t = new Server(server, connectedUsers);
                threads.add(t);
                t.start();
                connectedUsers++;       // do I need to manage access to this?

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        System.out.println("All users are here, let's GO.");
        Thread.sleep(1000);

        // We're in the pre-game state, threads are waiting

        // Deal the cards, round-robin
        // This will call notify all when it completes
        blackjack.deal();
    }

    /**
     * Reads in a file and returns its string content.
     * Borrowed from Stack Overflow:
     * https://stackoverflow.com/a/326440
     * @param path The path of the file.
     * @param encoding The charset encoding of the file.
     * @return A string representation of the file.
     * @throws IOException
     */
    static String readFile(String path, Charset encoding)
            throws IOException
    {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }
}
