package com.mohawk;

/**
 * Represents a playing card in a standard deck.
 */
public class Card {
    private String label;
    private char suit;
    private String suitName;
    private String name;
    private int value;

    /**
     * Creates a card from its initial label in a text file.
     * @param label A 2 or 3 char string (e.g. QH for Queen of Hearts)
     */
    public Card(String label) {
        this.label = label;
        char[] chars = label.toCharArray();

        // If we get a 10
        if (chars.length == 3) {
            setSuit(chars[2]);
            setName("Ten");
            setValue(10);
        // Anything else is processed normally
        } else {
            setSuit(chars[1]);
            setName(chars[0]);
            setValue(chars[0]);
        }
    }

    public String getLabel() {
        return label;
    }

    public String getName() {
        return name;
    }

    private void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the written name of the card's face value.
     * @param name A char initial for the face value (e.g. Q for Queen)
     */
    private void setName(char name) {
        String[] names = new String[]
                { "Faceless One", "One" , "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine" };

        switch(name) {
            case 'A':
                setName("Ace");
                break;
            case 'J':
                setName("Jack");
                break;
            case 'Q':
                setName("Queen");
                break;
            case 'K':
                setName("King");
                break;
            default:
                setName(names[Integer.parseInt(String.valueOf(name))]);
                break;
        }
    }

    public char getSuit() {
        return suit;
    }

    /**
     * Sets the suit and its full name.
     * @param suit A char initial for the suit (e.g. D for Diamonds)
     */
    private void setSuit(char suit) {
        this.suit = suit;

        switch (suit) {
            case 'C':
                setSuitName("Clubs");
                break;
            case 'D':
                setSuitName("Diamonds");
                break;
            case 'H':
                setSuitName("Hearts");
                break;
            case 'S':
                setSuitName("Spades");
                break;
            default:
                setSuitName("Mysteries");
                break;
        }
    }

    public String getSuitName() {
        return suitName;
    }

    private void setSuitName(String suitName) {
        this.suitName = suitName;
    }

    public int getValue() {
        return value;
    }

    /**
     * Sets the card's Blackjack point value from its face value.
     * @param value A char initial for the card's face value (e.g. J for Jack, worth 10)
     */
    private void setValue(char value) {
        switch (value) {
            case 'A':
                this.value = 1;
                break;
            case 'J':
            case 'Q':
            case 'K':
                this.value = 10;
                break;
            default:
                this.value = Integer.parseInt(String.valueOf(value));
                break;
        }
    }

    private void setValue(int value) {
        this.value = value;
    }

    /**
     * A string representation of a card.
     * Includes point value, full name, and initial label.
     * (e.g. a 'QH' card would print: [10] Queen of Hearts (QH)
     * @return
     */
    public String toString() {
        return String.format("[%02d] %s of %s (%s)", getValue(), getName(), getSuitName(), getLabel());
    }

}
