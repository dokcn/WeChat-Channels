package fun.dokcn.util;

import java.util.concurrent.TimeUnit;

public class MiscUtil {

    public static void sleepInSeconds(int seconds) {
        try {
            TimeUnit.SECONDS.sleep(seconds);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
