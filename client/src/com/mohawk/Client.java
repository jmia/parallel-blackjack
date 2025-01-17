package com.mohawk;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

/**
 * Represents a client application which talks to a blackjack server.
 */
public class Client {

    // Client messaging/waiting state
    private static boolean wasGreeted = false;
    private static boolean receivedInitialGameState = false;
    private static boolean isTakingTurn = false;
    private static boolean isBust = false;
    private static boolean completedRound = false;
    private static boolean shouldEndConnection = false;

    /**
     * Establishes a connection to a socket to play a
     * rousing game of blackjack.
     *
     * @param args an array of things pirates say
     */
    public static void main(String[] args) {
        String serverName = args[0];
        int port = Integer.parseInt(args[1]);
        Scanner keyboard = new Scanner(System.in);
        String typing;
        try {
            Socket client = new Socket(serverName, port);
            System.out.println("Just connected to " + client.getRemoteSocketAddress());
            OutputStream outToServer = client.getOutputStream();
            DataOutputStream out = new DataOutputStream(outToServer);
            InputStream inFromServer = client.getInputStream();
            DataInputStream in = new DataInputStream(inFromServer);

            // Waiting for a greeting on connection
            while (!wasGreeted) {
                // Arbitrary sleep to keep the app from constantly pinging for responses
                Thread.sleep(500);
                if (in.available() > 0) {
                    // The server should send a message like, "Welcome to blackjack! Stand by for all players."
                    System.out.println(in.readUTF());
                    wasGreeted = true;
                }
            }

            // Waiting for the hand to be dealt
            while (!receivedInitialGameState) {
                // Arbitrary sleep to keep the app from constantly pinging for responses
                Thread.sleep(500);
                if (in.available() > 0) {
                    // The server should send a message like, "Here are all the cards. Waiting for your turn."
                    System.out.println(in.readUTF());
                    receivedInitialGameState = true;
                }
            }

            // Waiting for the server to tell us it's our turn
            while (!isTakingTurn) {
                // Arbitrary sleep to keep the app from constantly pinging for responses
                Thread.sleep(500);
                if (in.available() > 0) {
                    // The server should send a message like, "It's your turn now."
                    System.out.println(in.readUTF());
                    isTakingTurn = true;
                }
            }

            // The player will do some I/O here
            while (!completedRound && !isBust) {

                // Arbitrary sleep to keep the app from constantly pinging for responses
                Thread.sleep(300);

                // If there is a response
                if (in.available() > 0) {
                    String response = in.readUTF();
                    System.out.println(response);
                    // If the response is BLACKJACK, the round is over
                    if (response.contains("BLACKJACK!")) {
                        System.out.println("You hit blackjack! Time to wait for the round to end.");
                        completedRound = true;
                        // Tell the server to end the user's turn
                        out.writeUTF("blackjack");
                        continue;
                    }
                    // If the response is BUST, the round is over
                    if (response.contains("BUST!")) {
                        System.out.println("You busted out. Time to wait for the round to end.");
                        isBust = true;
                        completedRound = true;
                        // Tell the server to end the user's turn
                        out.writeUTF("bust");
                        continue;
                    }
                // If there is no response
                } else {
                    continue;
                }

                // The player can type something now
                System.out.println("Any key to hit, type s to stay, type exit to quit.");
                typing = keyboard.nextLine();
                switch(typing.toLowerCase()) {
                    case "s":
                        System.out.println("You chose to stay. Time to wait for the round to end.");
                        completedRound = true;
                        break;
                    case "exit":
                        System.out.println("You chose to exit. Thanks for playing.");
                        completedRound = true;
                        shouldEndConnection = true;
                        break;
                    default:
                        System.out.println("You chose to hit. Waiting for more cards.");
                        break;
                }
                out.writeUTF(typing);
            }

            while (!shouldEndConnection) {
                // Arbitrary sleep to keep the app from constantly pinging for responses
                Thread.sleep(500);
                if (in.available() > 0) {
                    // The server should send a message like, "Here are all the results. Thanks for playing."
                    System.out.println(in.readUTF());
                    shouldEndConnection = true;
                }
            }

            // Close it all out
            out.close();
            in.close();
            client.close();

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
