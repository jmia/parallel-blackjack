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
    private static String finalResults = "";

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

    public synchronized String getInitialState(int id) {
        String initialState = "Cards have been dealt.\n";
        String dealerState = ">> Dealer:\n" + dealerHand.get(0).toString() + " and something else face down.\n";
        String playerStates = "";
        for (int i = 0; i < playerHands.size(); i++) {
            playerStates += ">> Player " + i + ": ";
            if (i == id) {
                playerStates += "  <---- THAT'S YOU";
            }
            playerStates += "\n";
            for (int j = 0; j < playerHands.get(i).size(); j++) {
                playerStates += playerHands.get(i).get(j).toString() + "\n";
            }
            playerStates += "For a total value of " + getHandValue(playerHands.get(i)) + ".\n";
        }
        return initialState + dealerState + playerStates + "Waiting for your turn.\n";
    }

    public synchronized String hit() {
        int id = getPlayerTakingTurn();
        String playerState = "";
        playerHands.get(id).add(deck.hit());

        // This is a comically long statement, and I wanted to store it in local variables, but I forget
        // if Java and C# handle references in lists differently so it's staying ugly
        playerState += "You were dealt " + playerHands.get(id).get(playerHands.get(id).size() - 1).toString() + "\n" +
                "and now you have:\n";

        int value = getHandValue(playerHands.get(id));

        for (int i = 0; i < playerHands.get(id).size(); i++) {
            playerState += playerHands.get(id).get(i).toString() + "\n";
        }

        // Busted
        if (value > 21) {
            playerState += "For a hand value of " + value + ". YOU BUST!\n";
            // Blackjack
        } else if (value == 21) {
            playerState += "For a hand value of " + value + ". BLACKJACK!\n";
            // Everything else
        } else {
            playerState += "For a hand value of " + value + ". What would you like to do?\n";
        }

        return playerState;
    }

    public synchronized void playDealer() {
        int dealerValue = getHandValue(dealerHand);

        while (dealerValue < 17) {
            dealerHand.add(deck.hit());
            dealerValue = getHandValue(dealerHand);
        }
    }

    public synchronized void tallyUp() {
        // Here we'll total the scores
        String initialState = "============\nFinal Results\n============";
        String dealerState = ">> Dealer:\n";
        for (int i = 0; i < dealerHand.size(); i++) {
            dealerState += dealerHand.get(i).toString() + "\n";
        }
        int dealerValue = getHandValue(dealerHand);
        boolean dealerBust = false;
        boolean dealerBlackjack = false;
        if (dealerValue > 21) {
            dealerState += "For a total value of " + dealerValue + ". BUST!\n";
            dealerBust = true;
        } else if (dealerValue == 21) {
            dealerState += "For a total value of " + dealerValue + ". BLACKJACK!\n";
            dealerBlackjack = true;
        } else {
            dealerState += "For a total value of " + dealerValue + ".\n";
        }

        String playerStates = "";

        for (int i = 0; i < playerHands.size(); i++) {
            playerStates += ">> Player " + i + ": ";
            playerStates += "\n";
            for (int j = 0; j < playerHands.get(i).size(); j++) {
                playerStates += playerHands.get(i).get(j).toString() + "\n";
            }
            playerStates += "For a total value of " + getHandValue(playerHands.get(i)) + ".\n";
        }

    }

    public boolean getReadyToPlay() {
        return readyToPlay;
    }

    public void incrementPlayer() {
        playerTakingTurn++;
    }

    public int getPlayerTakingTurn() {
        return playerTakingTurn;
    }

    public boolean getResultsAreReady() {
        return resultsAreReady;
    }

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

    public void setFinalResults(String value) {
        finalResults = value;
    }

    /**
     * Returns a value for the player's score in hand
     *
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
