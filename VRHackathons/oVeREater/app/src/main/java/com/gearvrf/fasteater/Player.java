package com.gearvrf.fasteater;

/**
 * Created by b1.miller on 7/29/2016.
 */
public class Player {

    private static int NUM_STARTING_LIVES = 3;

    private int currentScore;
    private int numLivesRemaining;

    public Player() {
        currentScore = 0;
        numLivesRemaining = NUM_STARTING_LIVES;
    }

    public int getCurrentScore() {
        return currentScore;
    }

    public void setCurrentScore(int currentScore) {
        this.currentScore = currentScore;
    }

    public void incrementScore(int valueIncrease) {
        currentScore += valueIncrease;
    }

    public int getNumLivesRemaining() {
        return numLivesRemaining;
    }

    public void setNumLivesRemaining(int numLivesRemaining) {
        this.numLivesRemaining = numLivesRemaining;
    }

    public boolean hasLivesRemaining() {
        return (numLivesRemaining > 0);
    }

    public boolean isDead() {
        return (numLivesRemaining < 1);
    }

    public void loseALife() {
        numLivesRemaining--;
    }

    public void reset() {
        currentScore = 0;
        numLivesRemaining = NUM_STARTING_LIVES;
    }
}
