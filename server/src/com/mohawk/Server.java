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
import java.util.Scanner;

public class Server extends Thread {

    private Socket server;
    private DataInputStream in;
    private DataOutputStream out;
    private static final Object lock = new Object();

    // Card state
    private static ArrayList<Card> dealerHand = new ArrayList<>();
    private static ArrayList<ArrayList<Card>> playerHands;

    // Game state
    static int totalPlayers;
    private static boolean gameOver = false;
    static int playerTakingTurn = 0;

    public Server(Socket theSocket, int playerId) throws IOException {
        super(Integer.toString(playerId));
        server = theSocket;
    }


    public void run() {
        String line = "start";

        try {
            // Print out some metadata about the player
            System.out.println("Just connected to " + server.getRemoteSocketAddress());
            System.out.println("Player is ID #" + currentThread().getName());

            // Set up the streams
            in = new DataInputStream(server.getInputStream());
            out = new DataOutputStream(server.getOutputStream());

            // Set up the local player state
            boolean userIsNotified = false;
            boolean roundOver = false;

            synchronized (lock) {
                out.writeUTF("Welcome to blackjack! Let's deal some cards.");
                lock.wait(); // go to sleep until the game kicks off
                out.writeUTF("this is where the outcome of the dealing will go");

                // Run a loop each time the notifyAll awakens all threads

                // If the game isn't over and the round isn't over
                while (!isGameOver() && !roundOver) {

                    // Keep going back to sleep if it's not your turn
                    while (getCurrentPlayer() != Integer.parseInt(currentThread().getName())) {
                        // Only send this message once
                        if (!userIsNotified) {
                            out.writeUTF("It is not your turn.");
                            userIsNotified = true;
                        }

                        // Will this allow other threads to continue automatically?
                        lock.wait();
                    }

                    out.writeUTF("Now it's your turn!");

                    // Play some stuff
                    out.writeUTF("You'd play a round here");

                    // Set up the next thread to run
                    incrementCurrentPlayer();

                    out.writeUTF("the next player will be" + getCurrentPlayer());

                    // If we've finished up everything, signal the end of the game
                    if (getCurrentPlayer() == totalPlayers) {
                        setGameOver();
                    }

                    // Turn is over, tell everyone we're ready to move on
                    lock.notifyAll();
                }
                // And now we wait for everyone else to go
                while (!isGameOver()) {
                    lock.wait();
                }

                // Game over, print everything and go home
                out.writeUTF("Game is over, somebody won. This is where the outcome goes.");
            }

            // Close up shop
            out.writeUTF("Closing up! Thanks for playing.");
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

    public static void main(String[] args) throws IOException, InterruptedException {

        int port = 61013;
        int connectedUsers = 0;
        int expectedConnections = Integer.parseInt(args[0]);

        ServerSocket mySocket = new ServerSocket(port);
        // Need a way to close the server without just killing it.
        while (!gameOver) {
            System.out.println("Waiting for clients on port "
                    + mySocket.getLocalPort() + "...");

            while(connectedUsers < expectedConnections) {
                Socket server = mySocket.accept();
                try {
                    Thread t = new Server(server, connectedUsers);
                    t.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                connectedUsers++;
            }

            System.out.println("we're ready to start");
            totalPlayers = connectedUsers;

            // Set up the deck
            String contents = readFile("deck.txt", Charset.defaultCharset());
            Deck deck = new Deck(contents);
            // deck.shuffle();

            // Dealer gets a card
            dealerHand.add(deck.hit());

            // Each player gets a card
            playerHands = new ArrayList<>(totalPlayers);

            for (int i = 0; i < totalPlayers; i++) {
                playerHands.add(new ArrayList<>());
                playerHands.get(i).add(deck.hit());
            }

            // Dealer gets another card
            dealerHand.add(deck.hit());

            // Each player gets another card
            for (int i = 0; i < connectedUsers; i++) {
                playerHands.get(i).add(deck.hit());
            }

            // Round begins

            // Now I need to tell everyone the cards have been dealt and what everyone has
            // But how do I call all the threads from within main? They're all named 't' and scoped to that while loop up there
            // Exception in thread "main" java.lang.IllegalMonitorStateException
            lock.notifyAll();
            // startGame();         // also has the same problem

            // Check every couple seconds to see if the game is over
            while (!isGameOver()) {
                Thread.sleep(2000);
            }

            // Tally up the game results
            System.out.println("This is where we score the stuff");

            // Send messages to users
            lock.notifyAll();

            System.out.println("We made it to the end of the game. By some miracle.");

            break;
        }
    }


    // This is all my blackjack console game logic
    // It's inelegant, but it works consistently every time for 1 player


//        try {
//            String contents = readFile("deck.txt", Charset.defaultCharset());
//            Deck deck = new Deck(contents);
////            System.out.println(deck);
//            deck.shuffle();
//
////            for (Card c : deck.getCards()) {
////                System.out.println(c.toString());
////            }
//
//            Scanner input = new Scanner(System.in);
//
//            ArrayList<Card> dealerHand = new ArrayList<>();
//            ArrayList<Card> playerHand = new ArrayList<>();
//
//            System.out.println("Welcome to Blackjack! X to exit. Any key to begin.");
//            String line = input.nextLine();
//
//            while (!line.toLowerCase().equals("x")) {
//                System.out.println("--- ROUND STARTED ---");
//
//                //
//                // Dealing
//                //
//                boolean dealerIsBust = false;
//                boolean playerIsBust = false;
//
//                // Player 1
//                playerHand.add(deck.hit());
//                System.out.println("you were dealt " + playerHand.get(0).toString());
//
//                // Dealer
//                dealerHand.add(deck.hit());
//                System.out.println("dealer was dealt " + dealerHand.get(0).toString());
//
//                // Player 1
//                playerHand.add(deck.hit());
//                System.out.println("you were dealt " + playerHand.get(1).toString());
//
//                // Dealer
//                dealerHand.add(deck.hit());
//                System.out.println("dealer was dealt a card face down.");
//                System.out.println("you can see the dealer has " + dealerHand.get(0).toString() + " and something else.");
//
//                System.out.println("");
//                System.out.println("");
//
//                int playerValue = getHandValue(playerHand);
//                System.out.println("your hand is");
//                for (Card c : playerHand) {
//                    System.out.println(c.toString());
//                }
//                System.out.println("for a total value of " + playerValue);
//
//                if (playerValue == 21) {
//                    // TODO: Natural blackjack condition
//                    System.out.println("you're a natural!");
//                }
//                else if (playerValue > 21) {
//                    playerIsBust = true;
//                    System.out.println("BUSTED!");
//                }
//
//                //
//                // Playing
//                //
//
//                // Player's turn
//                System.out.println("");
//                if (!playerIsBust && playerValue != 21) {
//                    System.out.println("you have " + playerValue + ". what do you want to do?");
//                    System.out.println("press any key to hit");
//                    System.out.println("press S to stand (end your turn)");
//                    line = input.nextLine();
//
//                    while (!line.toLowerCase().equals("s")) {
//                        playerHand.add(deck.hit());
//                        for (Card c : playerHand) {
//                            System.out.println(c.toString());
//                        }
//                        playerValue = getHandValue(playerHand);
//
//                        if (playerValue == 21) {
//                            System.out.println("you have " + playerValue);
//                            System.out.println("BLACKJACK!");
//                            break;
//                        }
//                        if (playerValue > 21) {
//                            playerIsBust = true;
//                            System.out.println("you have " + playerValue);
//                            System.out.println("BUSTED!");
//                            break;
//                        }
//                        else {
//                            System.out.println("you have " + playerValue + ". what do you want to do?");
//                            System.out.println("press any key to hit");
//                            System.out.println("press s to stand (end your turn)");
//                            line = input.nextLine();
//                        }
//                    }
//                }
//
//                // Dealer's turn
//                System.out.println("");
//                System.out.println("dealer turns over their card.");
//                int dealerValue = getHandValue(dealerHand);
//                System.out.println("dealer's hand is");
//                for (Card c : dealerHand) {
//                    System.out.println(c.toString());
//                }
//                System.out.println("for a total value of " + dealerValue);
//
//                while (dealerValue < 17) {
//                    System.out.println("dealer hits");
//                    dealerHand.add(deck.hit());
//                    for (Card c : dealerHand) {
//                        System.out.println(c.toString());
//                    }
//                    dealerValue = getHandValue(dealerHand);
//                    System.out.println("dealer has " + dealerValue);
//                }
//
//                if (dealerValue > 21) {
//                    System.out.println("DEALER BUSTED!");
//                    dealerIsBust = true;
//                }
//                else if (dealerValue == 21) {
//                    System.out.println("DEALER BLACKJACK!");
//                }
//                else {
//                    System.out.println("dealer stays.");
//                    System.out.println("dealer has " + dealerValue);
//                }
//
//                System.out.println("--- ROUND OVER ---");
//                System.out.println("tallying up...");
//                System.out.println("");
//
//                if (dealerIsBust && !playerIsBust) {
//                    System.out.println("Player wins!");
//                }
//                else if (dealerIsBust && playerIsBust) {
//                    System.out.println("Everyone busted. Ouchies.");
//                }
//                else if (playerIsBust && !dealerIsBust) {
//                    System.out.println("Dealer wins!");
//                }
//                else {
//                    if (playerValue > dealerValue) {
//                        System.out.println("Player wins!");
//                    }
//                    else if (playerValue == dealerValue) {
//                        System.out.println("It's a tie!");
//                    }
//                    else {
//                        System.out.println("Dealer wins!");
//                    }
//                }
//
//                System.out.println("");
//                System.out.println("Game over. X to exit. Any key to play again.");
//
//                line = input.nextLine();
//
//                if (!line.toLowerCase().equals("x")) {
//                    deck = new Deck(contents); // avoiding the "deep copy" trap
//                    deck.shuffle();
//                    dealerHand = new ArrayList<>();
//                    playerHand = new ArrayList<>();
//                }
//            }
//
//        }
//        catch (Exception e) {
//            System.out.println(e);
//        }

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

    // TODO: Be cool and make this an extension method?
    /**
     * Returns a value for the player's score in hand
     * @param hand The hand of cards to traverse
     * @return the int value score
     */
    static int getHandValue(ArrayList<Card> hand) {
        int sum = 0;
        for (int i = 0; i < hand.size(); i++) {
            sum += hand.get(i).getValue();
        }
        return sum;
    }

    public static synchronized void startGame() {
        synchronized (lock) {
            // notifyAll();         // doesn't work
        }
    }

    public static synchronized void setGameOver() {
        gameOver = true;
    }

    public static synchronized boolean isGameOver() {
        return gameOver;
    }

    public static synchronized int getCurrentPlayer() {
        return playerTakingTurn;
    }

    public static synchronized void incrementCurrentPlayer() {
        playerTakingTurn++;
    }
}