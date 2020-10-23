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

    /**
     * Progresses a 'client' thread through the states of playing
     * a group game of blackjack.
     */
    public void run() {
        String line = "start";

        try {
            System.out.println("Just connected to " + server.getRemoteSocketAddress());

            // Method-level variables
            in = new DataInputStream(server.getInputStream());
            out = new DataOutputStream(server.getOutputStream());
            int id = Integer.parseInt(currentThread().getName());

            // Here we'll give them a welcome message
            out.writeUTF("Welcome to blackjack! Your ID is " + id + ". Please wait for all players to connect.");
            System.out.println("We greeted " + id + ". Now we go to sleep until the cards are dealt.");

            // We're in the pre-game state
            synchronized (blackjack) {
                while (!blackjack.getReadyToPlay()) {
                    // The cards haven't been dealt at this point
                    blackjack.wait();
                }
            }

            // Cards have been dealt
            // Print the state of play to each player
            out.writeUTF(blackjack.getInitialState(id));

            // Play a round for each thread
            synchronized (blackjack) {
                // Round state
                boolean playerCompletedRound = false;
                while (id != blackjack.getPlayerTakingTurn()) {
                    blackjack.wait();
                }

                out.writeUTF("It's your turn now.");
                System.out.println("We told player " + id + " it was their turn.");

                // Where the magic happens for each player
                out.writeUTF("What would you like to do?");

                while (!playerCompletedRound) {
                    // Arbitrary sleep to keep the thread from constantly pinging for responses
                    Thread.sleep(300);
                    // When user input comes in
                    if (in.available() > 0) {
                        String clientInput = in.readUTF();
                        String response = "";
                        // Determine how to respond to the user
                        switch (clientInput) {
                            // If they busted, hit blackjack, or chose to stay, the turn is over
                            case "blackjack":
                            case "bust":
                            case "s":
                                System.out.println("Player " + id + " completed their turn.");
                                playerCompletedRound = true;
                                break;
                            // If they chose to exit
                            case "exit":
                                System.out.println("Player " + id + " bailed.");
                                playerCompletedRound = true;
                                break;
                            // If they typed any other response
                            default:
                                System.out.println("Player " + id + " hit.");
                                response = blackjack.hit();
                                out.writeUTF(response);
                                break;
                        }
                    }
                }

                // Let the next player go
                blackjack.incrementPlayerTakingTurn();
                // If it's the last player, set the game over
                blackjack.checkAndSetGameOver();
                // Notify another thread
                blackjack.notifyAll();
            }

            // Round is over, we're not exiting until results are tallied
            System.out.println("Round is over for " + id);
            synchronized (blackjack) {
                while (!blackjack.getResultsAreReady()) {
                    blackjack.wait();
                }
            }

            System.out.println("Printing final results for " + id);
            out.writeUTF(blackjack.getFinalResults());

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
        // This is a hard-coded port to make it easy to run concurrent clients manually
        int port = 61013;
        int connectedUsers = 0;
        int expectedConnections = Integer.parseInt(args[0]);

        // Set up the deck
        // Please note that these decks are stacks and so cards will be dealt
        // starting from the end of the list
        String contents = readFile("deck.txt", Charset.defaultCharset());
        Deck deck = new Deck(contents);

        // If you'd like to reverse the deck so the cards are dealt
        // from the start of the list, uncomment this line
        // deck.reverse();

        // If you'd like to play some real cards man, uncomment this line
        // deck.shuffle();
        blackjack = new Blackjack(deck, expectedConnections);


        ServerSocket mySocket = new ServerSocket(port);

        // Wait for users to connect to the hardcoded port
        while (connectedUsers < expectedConnections) {
            System.out.println("Waiting for client on port "
                    + mySocket.getLocalPort() + "...");
            Socket server = mySocket.accept(); // blocking
            try {
                Thread t = new Server(server, connectedUsers);
                threads.add(t);
                t.start();
                connectedUsers++;

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // We're in the pre-game state, threads are waiting

        // Deal the cards, round-robin
        // This will call notify all when it completes
        blackjack.deal();

        // Threads are playing, check periodically for game over but not too often
        while (!blackjack.getGameOver()) {
            System.out.println("Main tried to find out if game was over.");
            Thread.sleep(2000);
        }

        // Players have played their rounds, time for the dealer to go
        blackjack.playDealer();

        // Tally up scores
        blackjack.tallyUp();

        // Notify threads that results are ready
        blackjack.setResultsAreReadyAndNotify();

        // We finished! Whew.
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
