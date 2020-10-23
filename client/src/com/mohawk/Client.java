package com.mohawk;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

/**
 * Represents a client application which talks to a blackjack server.
 */
public class Client extends Thread {

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
        String typing = "firstmessage";
        try {
            Socket client = new Socket(serverName, port);
            System.out.println("Just connected to " + client.getRemoteSocketAddress());
            OutputStream outToServer = client.getOutputStream();
            DataOutputStream out = new DataOutputStream(outToServer);
            InputStream inFromServer = client.getInputStream();
            DataInputStream in = new DataInputStream(inFromServer);
            System.out.println("Server says " + in.readUTF());
            while (!typing.equals("exit")) {
                typing = keyboard.nextLine();
                out.writeUTF(typing);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    System.err.print(ie);
                }
                if (in.available() > 0) {
                    System.out.println("Server replies " + in.readUTF());
                }
            }
            out.writeUTF("exit");
            out.close();
            in.close();
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
