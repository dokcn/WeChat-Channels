package fun.dokcn.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomUtil {

    public static int randomIntegerInRange(int startInclusive, int endExclusive) {
        Random random = new Random();
        return random.nextInt(startInclusive, endExclusive);
    }

    public static boolean randomBoolean() {
        return new Random().nextBoolean();
    }

    private static String randomString(int count,
                                       boolean includeUpperCaseLetters,
                                       boolean includeLowerCaseLetters,
                                       boolean includeNumbers) {
        StringBuilder sb = new StringBuilder(count);
        for (int i = 0; i < count; i++) {
            List<Character> forChoose = new ArrayList<>(3);

            if (includeUpperCaseLetters)
                forChoose.add(Character.toUpperCase((char) randomIntegerInRange('a', 'z' + 1)));

            if (includeLowerCaseLetters)
                forChoose.add((char) randomIntegerInRange('a', 'z' + 1));

            if (includeNumbers)
                forChoose.add((char) randomIntegerInRange('0', '9' + 1));

            sb.append(forChoose.get(randomIntegerInRange(0, forChoose.size())));
        }
        return sb.toString();
    }

    public static String randomString(int count) {
        return randomString(count, true, true, true);
    }

    public static String randomLowerCaseLettersString(int count) {
        return randomString(count, false, true, false);
    }

    public static String randomUpperCaseLettersString(int count) {
        return randomString(count, true, false, false);
    }

    public static String randomNumbersString(int count) {
        return randomString(count, false, false, true);
    }

    public static String randomLowerCaseLettersWithNumbersString(int count) {
        return randomString(count, false, true, true);
    }

    public static String randomUpperCaseLettersWithNumbersString(int count) {
        return randomString(count, true, false, true);
    }

    public static String randomLettersString(int count) {
        return randomString(count, true, true, false);
    }

}
