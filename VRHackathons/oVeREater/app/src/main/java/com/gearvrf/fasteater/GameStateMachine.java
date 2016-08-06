package com.gearvrf.fasteater;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by b1.miller on 7/29/2016.
 */
public class GameStateMachine {

    public enum GameStatus {
        STATE_BOOT_ANIMATION,
        STATE_GAME_LOAD,
        STATE_GAME_STARTED,
        STATE_GAME_IN_PROGRESS,
        STATE_GAME_END
    }

    private Player player;
    private List<FlyingItem> flyingItems;
    private Random random;
    private int currentLevel;
    private GameStatus status = GameStatus.STATE_GAME_END;

    public GameStateMachine() {

        random = new Random(System.currentTimeMillis());

        restartGame();
    }

    public void setCurrentLevel(int newLevel) {
        currentLevel = newLevel;
    }

    public int getCurrentLevel() {
        return currentLevel;
    }

    public int getScore() {
        return player.getCurrentScore();
    }

    public void setScore(int newScore) {
        player.setCurrentScore(newScore);
    }

    public void incrementScore(int valueIncrease) {
        player.incrementScore(valueIncrease);
    }

    public GameStatus getStatus() {
        return status;
    }

    public void setStatus(GameStatus status) {
        this.status = status;
    }

    /**
     * Restart a new game. Reset all status back to the beginning
     */
    public void restartGame() {

    }

    public void startGame() {

        setStatus(GameStatus.STATE_GAME_STARTED);
    }

    public void stopGame() {

        restartGame();
    }

    public List<FlyingItem> getCurrentFlyingItems() {
        List<FlyingItem> currentFlyingItems = new ArrayList<FlyingItem>();
        for (FlyingItem item : flyingItems) {
            if (item.isInMotion()) {
                currentFlyingItems.add(item);
            }
        }
        return currentFlyingItems;
    }

    // TODO: probably not run() / while, but callbacks to change state
    public void run() {

    }
}
