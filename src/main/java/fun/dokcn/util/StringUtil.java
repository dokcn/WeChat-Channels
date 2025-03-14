package fun.dokcn.util;

import java.util.Optional;
import java.util.function.Function;

public class StringUtil {

    public static String ifBlank(String str, String defaultStr, Function<String, String> nonBlankConverter) {
        String converted = Optional.ofNullable(nonBlankConverter).map(it -> it.apply(str)).orElse(str);
        return (str == null || str.isBlank()) ? defaultStr : converted;
    }

    public static String ifBlank(String str) {
        return ifBlank(str, "", null);
    }

    public static String ifBlank(String str, Function<String, String> nonBlankConverter) {
        return ifBlank(str, "", nonBlankConverter);
    }

}
