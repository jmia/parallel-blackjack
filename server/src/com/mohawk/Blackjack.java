package com.mohawk;

import java.util.ArrayList;

public class Blackjack {

    // Internal game state
    private static int playerTakingTurn = 0;
    private static int numPlayers = 0;
    private static boolean readyToPlay = false;
    private static boolean gameOver = false;
    private static boolean resultsAreReady = false;

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
     * (these are stacks, so the test files will be "backwards")
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

    public synchronized void play() throws InterruptedException {
        Thread.sleep(3000);
    }

    public synchronized String getInitialState() {
        String initialState = "Cards have been dealt.\n";
        String dealerState = "Dealer:\n" + dealerHand.get(0).toString() + " and something else face down.\n";
        String playerStates = "";
        for (int i = 0; i < playerHands.size(); i++) {
            playerStates += ">> Player " + i + ":\n";
            for (int j = 0; j < playerHands.get(i).size(); j++) {
                playerStates+= playerHands.get(i).get(j).toString() + "\n";
            }
            playerStates += "For a total value of " + getHandValue(playerHands.get(i)) + ".\n";
        }
        return initialState + dealerState + playerStates + "Waiting for your turn.\n";
    }

    public boolean getReadyToPlay() {
        return readyToPlay;
    }       // should this be sync?

    public void incrementPlayer() {
        playerTakingTurn++;
    }

    public int getPlayerTakingTurn() {
        return playerTakingTurn;
    }

    public boolean getResultsAreReady() { return resultsAreReady; }     // should this be sync?

    public synchronized void setResultsAreReady() {
        resultsAreReady = true;
        notifyAll();
    }

    public void checkGameOver() {
        if (getPlayerTakingTurn() == numPlayers) {
            setGameOver();
        }
    }

    public void setGameOver() {
        gameOver = true;
    }

    public synchronized boolean getGameOver() {
        return gameOver;
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
