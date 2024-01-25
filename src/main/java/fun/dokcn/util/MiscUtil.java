package fun.dokcn.util;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class MiscUtil {

    public static void sleepInSeconds(int seconds) {
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static int randomIntegerInRange(int startInclusive, int endExclusive) {
        Random random = new Random();
        return random.nextInt(startInclusive, endExclusive);
    }

}
