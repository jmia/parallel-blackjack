package com.mohawk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Stack;
import java.util.stream.Collectors;

/**
 * Represents a standard deck of playing cards.
 */
public class Deck {
    private Stack<Card> cards;

    /**
     * Creates a deck with a collection of cards.
     * @param deckContents A string of comma-separated values, formatted
     *                     as initials (e.g. "QH,AS" for Queen of Hearts, Ace of Spades)
     */
    public Deck(String deckContents) {
        this.cards = buildDeck(deckContents);
    }

    public Stack<Card> getCards() {
        return cards;
    }

    public void setCards(Stack<Card> cards) {
        this.cards = cards;
    }

    /**
     * Creates a collection of playing cards from a
     * string of comma separated values.
     * @param deckContents The string of card values.
     * @return A complete 52-card 'deck'.
     */
    public Stack<Card> buildDeck(String deckContents) {
        Stack<Card> cards = new Stack<>();
        String[] deck = deckContents.split(",");

        for(String c : deck) {
            cards.push(new Card(c));
        }

        return cards;
    }

    public Card hit() {
        return cards.pop();
    }

    /**
     * Shuffles a deck of cards.
     */
    public void shuffle() {
        Collections.shuffle(this.cards);
        setCards(this.cards);
    }

    /**
     * A string representation of a deck of cards. Applies
     * some cool new hip Java methods, man.
     * @return An array string of card initial labels (e.g. [4D, KH])
     */
    public String toString() {
        return String.join(",",cards.stream()
                .map(c -> c.getLabel())
                .collect(Collectors.toList()).toString());
    }
}
