package fun.dokcn.entity;

import java.math.BigDecimal;

public record IncomeInfo(
        String streamingTitle,
        String streamingTime,
        String streamingDuration,
        int numberOfWatch,
        BigDecimal income
) {

    public static IncomeInfo emptyIncomeInfo() {
        return new IncomeInfo("未知", "未知", "未知", 0, BigDecimal.ZERO);
    }

}
