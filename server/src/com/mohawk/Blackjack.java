package com.mohawk;

import java.util.ArrayList;

public class Blackjack {

    // Internal game state
    private static int playerTakingTurn = 0;
    private static int numPlayers = 0;
    private static boolean readyToPlay = false;
    private static boolean gameOver = false;

    // Internal game data
    private static Deck deck;
    private static ArrayList<Card> dealerHand = new ArrayList<>();
    private static ArrayList<ArrayList<Card>> playerHands;

    public Blackjack(Deck deck, int numPlayers) {
        Blackjack.deck = deck;
        Blackjack.numPlayers = numPlayers;
        playerHands = new ArrayList<>();
    }

    /**
     * Deals a card to the dealer and each player
     */
    public synchronized void deal() {
        dealerHand.add(deck.hit());

        // Deal 1 to each player
        for (int i = 0; i < numPlayers; i++) {
            playerHands.add(new ArrayList<>());
            playerHands.get(i).add(deck.hit());
        }

        // Dealer gets another card
        dealerHand.add(deck.hit());

        // Each player gets another card
        for (int i = 0; i < numPlayers; i++) {
            playerHands.get(i).add(deck.hit());
        }

        readyToPlay = true;
        notifyAll();
    }

    public synchronized boolean getReadyToPlay() {
        return readyToPlay;
    }

    public synchronized void incrementPlayer() {
        playerTakingTurn++;
    }

    public synchronized int getPlayerTakingTurn() {
        return playerTakingTurn;
    }

    public synchronized void setGameOver() {
        gameOver = true;
    }

    public synchronized boolean isGameOver() {
        return gameOver;
    }

    public int getNumPlayers() {    // is only ever written to once
        return numPlayers;
    }

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
}
