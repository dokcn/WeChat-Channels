package fun.dokcn.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Date;

public class DateTimeUtil {

    public static final DateTimeFormatter TIME_FORMATTER = new DateTimeFormatterBuilder()
            .parseLenient()
            .appendPattern("HH:mm")
            .toFormatter();

    // todo: format date of today to 今天, tomorrow to 明天
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy 年 MM 月 dd 日 HH 时 mm 分");

    public static Date localDateTimeToDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.toInstant(ZoneOffset.ofHours(8)));
    }

    public static LocalDateTime dateToLocalDateTime(Date date) {
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.of("Asia/Shanghai"));
    }

    public static String toGMTString(LocalDateTime source, ZoneId sourceTimeZone) {
        return source.atZone(sourceTimeZone).withZoneSameInstant(ZoneId.of("GMT")).format(DateTimeFormatter.RFC_1123_DATE_TIME);
    }

    public static String toGMTString(LocalDateTime source) {
        return toGMTString(source, ZoneId.systemDefault());
    }

}
