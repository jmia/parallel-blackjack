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

            // Block-level variables
            in = new DataInputStream(server.getInputStream());
            out = new DataOutputStream(server.getOutputStream());
            int id = Integer.parseInt(currentThread().getName());

            // Here we'll give them a welcome message maybe
            out.writeUTF("Welcome to blackjack! Your ID is " + id + ". Please wait for the game to begin.");
            System.out.println("We told " + id + " welcome to blackjack! Now we go to sleep until the cards are dealt.");

            // We're in the pre-game state
            synchronized (blackjack) {
                while (!blackjack.getReadyToPlay()) {       // does this method need to be sync if the block is sync?
                    // The cards haven't been dealt at this point
                    blackjack.wait();
                    // the deal method in main called notify all
                }
            }

            // I'm 100% sure of my state of play by this line.
            System.out.println("Here's where we should print the state of play for " + id + " (I expect to see this three times)");

            if (id != blackjack.getPlayerTakingTurn()) {
                System.out.println("It's not " + id + "'s turn.");
            }

            blackjack.play(id); // this will need to be broken up to support i/o

            System.out.println("Round is over for " + id);

            // Round is over, we're not exiting until it's safe
            synchronized (blackjack) {
                while (!blackjack.getResultsAreReady()) {
                    blackjack.wait();
                }
            }

            System.out.println("We'll print the final results here for " + id);

            System.out.println("Goodbye!");

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

        while (!blackjack.getGameOver()) {
            System.out.println("Main tried to find out if game was over.");
            Thread.sleep(2000);
        }

        System.out.println("We made it to the end of the game on MAIN. Tally her up.");

        Thread.sleep(1000);
        blackjack.setResultsAreReady();

        System.out.println("We're done, amazing.");
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
