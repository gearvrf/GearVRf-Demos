package com.gearvrf.fasteater;

import java.util.Random;

/**
 * Created by siva.penke on 7/30/2016.
 */
public class Helper {
    static Random random = new Random();
    // (0,10) returns inclusive 0 and 10; (-10, 10) returns 1 - 10 + 1 = -8
    public static int randomInRange(int min, int max) {
        return random.nextInt(Math.abs(max - min) + 1) + min;
    }

    public static float randomInRangeFloat(int min, int max) {
        return random.nextFloat() * (max - min) + min;
    }

    public static int randomNextInt(int rand) {
        return random.nextInt(rand);
    }

}
