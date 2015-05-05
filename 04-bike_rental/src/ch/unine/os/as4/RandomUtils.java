package ch.unine.os.as4;

import java.util.Random;

/**
 * Set of convenience methods to generate random numbers
 *
 * @author Sébastien Vaucher
 */
public class RandomUtils {
    private static final Random random;

    static {
        // Created on first usage of the class
        random = new Random();
    }

    /**
     * Return a random number that is min &gt;= number &gt;= max.
     * @param min Lower bound (inclusive)
     * @param max Upper bound (inclusive)
     * @return A random integer
     */
    public static int randomRange(int min, int max) {
        return random.nextInt(max - min + 1) + min;
    }
}
