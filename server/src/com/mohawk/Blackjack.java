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

    /**
     * Builds a nicely formatted string to send to each player
     * that shows the state of play after the cards are first dealt
     * @param id the id of the thread requesting the state
     * @return the state of the table
     */
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

    /**
     * Pops a card off the deck for an individual player
     * and scores their turn
     * @return a string representation of their turn
     */
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

    /**
     * Simulates the dealer taking their turn
     */
    public synchronized void playDealer() {
        int dealerValue = getHandValue(dealerHand);

        // In standard blackjack rules,
        // the dealer must hit on <16, stay on 17+
        while (dealerValue < 17) {
            dealerHand.add(deck.hit());
            dealerValue = getHandValue(dealerHand);
        }
    }

    public synchronized void tallyUp() {
        // Here we'll total the scores
        String initialState = "============\nFinal Results\n============\n";
        String dealerState = ">> Dealer:\n";
        for (int i = 0; i < dealerHand.size(); i++) {
            dealerState += dealerHand.get(i).toString() + "\n";
        }
        int dealerValue = getHandValue(dealerHand);

        boolean dealerBust = false;
        if (dealerValue > 21) {
            dealerState += "For a total value of " + dealerValue + ". BUST!\n";
            dealerBust = true;
        } else if (dealerValue == 21) {
            dealerState += "For a total value of " + dealerValue + ". BLACKJACK!\n";
        } else {
            dealerState += "For a total value of " + dealerValue + ".\n";
        }

        String playerStates = "";

        for (int i = 0; i < playerHands.size(); i++) {
            playerStates += ">> Player " + i + ":\n";
            for (int j = 0; j < playerHands.get(i).size(); j++) {
                playerStates += playerHands.get(i).get(j).toString() + "\n";
            }
            int playerValue = getHandValue(playerHands.get(i));

            boolean playerBust = false;
            if (playerValue > 21) {
                playerStates += "For a total value of " + playerValue + ". BUST!\n";
                playerBust = true;
            } else if (playerValue == 21) {
                playerStates += "For a total value of " + playerValue + ". BLACKJACK!\n";
            } else {
                playerStates += "For a total value of " + playerValue + ".\n";
            }

            // Win conditions

            // Player bust out and dealer didn't
            if (playerBust && !dealerBust) {
                playerStates += "Dealer won against Player " + i + " this round.\n";
            }
            // Dealer bust out and player didn't
            else if (!playerBust && dealerBust) {
                playerStates += "Player " + i + " beat the dealer!\n";
            }
            // Nobody busted out, compare actual scores
            else if (!playerBust && !dealerBust) {
                if (playerValue > dealerValue) {
                    playerStates += "Player " + i + " beat the dealer!\n";
                }
                else if (playerValue == dealerValue) {
                    playerStates += "Round against Player " + i + " was a tie.\n";
                }
                else {
                    playerStates += "Dealer won against Player " + i + " this round.\n";
                }
            }
            // Everybody busted
            else if (playerBust && dealerBust) {
                playerStates += "Nobody won the round for Player " + i + ".\n";
            }
        }

        String results = initialState + dealerState + playerStates;

        results += "GAME OVER!";

        setFinalResults(results);

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

    public synchronized String getFinalResults() {
        return finalResults;
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
