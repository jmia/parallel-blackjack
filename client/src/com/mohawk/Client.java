package com.mohawk;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

/**
 * Represents a client application which talks to a blackjack server.
 */
public class Client extends Thread {
    private static boolean gameOver = false;
    private DataInputStream in;

    public Client(DataInputStream in) {
        setInputStream(in);
    }

    /**
     * Listens for output from the server and prints to the console
     */
    public void run() {
        try {
            while (!getGameOver()) {
                if (this.in.available() > 0) {
                    System.out.println(in.readUTF());
                }
            }
            System.out.println("We out!");
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Establishes a connection to a socket to play a
     * rousing game of blackjack.
     *
     * @param args an array of things pirates say
     */
    public static void main(String[] args) {

        // This is the original starter code

//        String serverName = args[0];
//        int port = Integer.parseInt(args[1]);
//        Scanner keyboard = new Scanner(System.in);
//        String typing = "firstmessage";
//        try
//        {
//            Socket client = new Socket(serverName, port);
//            System.out.println("Just connected to " + client.getRemoteSocketAddress());
//            OutputStream outToServer = client.getOutputStream();
//            DataOutputStream out = new DataOutputStream(outToServer);
//            InputStream inFromServer = client.getInputStream();
//            DataInputStream in = new DataInputStream(inFromServer);
//            System.out.println("Server says " + in.readUTF());
//            while(!typing.equals("exit")){
//                typing = keyboard.nextLine();
//                out.writeUTF(typing);
//                try{
//                    Thread.sleep(1000);
//                }catch(InterruptedException ie){
//                    System.err.print(ie);
//                }
//                if(in.available() > 0){
//                    System.out.println("Server replies " + in.readUTF());
//                }
//            }
//            out.writeUTF("exit");
//            out.close();
//            in.close();
//            client.close();
//        }catch(IOException e)
//        {
//            e.printStackTrace();
//        }




        // This was my threaded client idea. It prints out anything that comes in and doesn't prevent the user from
        // sending messages. It'll fill up the queue on the other side but that's a problem for Future Jen.

        String serverName = args[0];
        int port = Integer.parseInt(args[1]);
        Scanner keyboard = new Scanner(System.in);
        String typing = "there's no place like 192.168.0.1";
        try {
            Socket client = new Socket(serverName, port);
            System.out.println("Successfully connected to " + client.getRemoteSocketAddress());
            OutputStream outToServer = client.getOutputStream();
            DataOutputStream out = new DataOutputStream(outToServer);
            out.writeUTF("Greetings from Planet " + client.getLocalSocketAddress() + "!");

            InputStream inFromServer = client.getInputStream();
            DataInputStream in = new DataInputStream(inFromServer);

            // Creates a thread to listen for server output
            Client listener = new Client(in);
            listener.start();

            while (!typing.equals("exit")) {
                typing = keyboard.nextLine();
                out.writeUTF(typing);
            }

            // End the game:
            // shut down the thread,
            // tell the server we're done,
            // close connections
            listener.setGameOver(true);
            out.writeUTF("exit");
            out.close();
            client.close();

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets the state of play (game over or game on, wayne)
     *
     * @return if the game has been ended by main
     */
    public synchronized boolean getGameOver() {
        return this.gameOver;
    }

    /**
     * Sets the state of play when the game is over
     *
     * @param gameOver The state of play (true for game over)
     */
    public synchronized void setGameOver(boolean gameOver) {
        System.out.println("setting game over to " + gameOver);             // TODO: Remove when done testing
        this.gameOver = gameOver;
    }

    /**
     * Sets the data input stream from the server.
     *
     * @param in
     */
    public synchronized void setInputStream(DataInputStream in) {
        this.in = in;
    }
}
