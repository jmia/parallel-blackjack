package com.mohawk;

import java.io.*;
import java.net.Socket;
import java.sql.SQLOutput;
import java.util.Scanner;

/**
 * Represents a client application which talks to a blackjack server.
 */
public class Client {

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
                Thread.sleep(500);
                if (in.available() > 0) {
                    // The server should send a message like, "Welcome to blackjack! Stand by for all players."
                    System.out.println(in.readUTF());
                    wasGreeted = true;
                }
            }

            System.out.println("wasgreeted came back true");

            // Waiting for the hand to be dealt
            while (!receivedInitialGameState) {
                Thread.sleep(500);
                if (in.available() > 0) {
                    // The server should send a message like, "Here are all the cards. Waiting for your turn."
                    System.out.println(in.readUTF());
                    receivedInitialGameState = true;
                }
            }

            System.out.println("receivedInitialGameState came back true");

            // Waiting for the server to tell us it's our turn
            while (!isTakingTurn) {
                Thread.sleep(500);
                if (in.available() > 0) {
                    // The server should send a message like, "It's your turn now."
                    System.out.println(in.readUTF());
                    isTakingTurn = true;
                }
            }

            System.out.println("isTakingTurn came back true");

            // The player will do some I/O here.
            while (!completedRound && !isBust) {

                // Give the server a few ms to send something
                Thread.sleep(300);

                // check for a message from the server, might be the start of a round like
                // "What would you like to do?"
                // or a mid-round response like "BUST!" or "Your score is 17. What would you like to do?"


                // If there is a response
                if (in.available() > 0) {
                    String response = in.readUTF();
                    System.out.println(response);
                    // If the response is BUST, the round is over
                    if (response.equals("BUST!")) {
                        System.out.println("You busted out. Time to wait for the round to end.");
                        isBust = true;
                        completedRound = true;
                        continue;
                    }
                // If there is no response
                } else {
                    continue;
                }
                System.out.println("Any key to hit, n to stay, exit to quit.");
                typing = keyboard.nextLine();
                switch(typing.toLowerCase()) {
                    case "n":
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
