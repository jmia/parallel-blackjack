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
            out.writeUTF("Welcome to blackjack! Your ID is " + id + ". Please wait for all players to connect.");
            System.out.println("We greeted " + id + ". Now we go to sleep until the cards are dealt.");

            // We're in the pre-game state
            synchronized (blackjack) {
                while (!blackjack.getReadyToPlay()) {       // does this method need to be sync if the block is sync?
                    // The cards haven't been dealt at this point
                    blackjack.wait();
                    // the deal method in main called notify all
                }
            }

            // I'm 100% sure of my state of play by this line.
            System.out.println("Printing the state of play for " + id + ".");
            out.writeUTF(blackjack.getInitialState());

            synchronized (blackjack) {
                boolean playerCompletedRound = false;       // i don't know if this will set locally or not
                boolean playerBust = false;
                while (id != blackjack.getPlayerTakingTurn()) {
                    blackjack.wait();
                }

                out.writeUTF("It's your turn now.");
                System.out.println("We told player " + id + " it was their turn.");

                // Where the magic happens for each player
                out.writeUTF("What would you like to do?");

                while (!playerCompletedRound) {
                    Thread.sleep(300);
                    // If user input comes in
                    if (in.available() > 0) {
                        String clientInput = in.readUTF();
                        switch (clientInput) {
                            case "n":
                                System.out.println("Player " + id + " stayed.");
                                playerCompletedRound = true;
                                break;
                            case "exit":
                                System.out.println("Player " + id + " bailed.");
                                playerCompletedRound = true;
                                break;
                            default:
                                System.out.println("Player " + id + " hit.");
                                // TODO: Hit me! (just testing with bust for now)
                                out.writeUTF("BUST!");
                                playerCompletedRound = true;
                                break;
                        }
                    }
                }

                blackjack.incrementPlayer();

                blackjack.checkGameOver();

                blackjack.notifyAll();
            }

            System.out.println("Round is over for " + id);

            // Round is over, we're not exiting until it's safe
            synchronized (blackjack) {
                while (!blackjack.getResultsAreReady()) {
                    blackjack.wait();
                }
            }

            System.out.println("We'll print the final results for " + id);
            out.writeUTF("THE GAME IS OVER! There will be some results here.");

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

        System.out.println("Main is happy and going to bed now.");
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
