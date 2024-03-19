package fun.dokcn.service;

import fun.dokcn.entity.IncomeInfo;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static fun.dokcn.Constants.INCOME_INFO_URL;
import static fun.dokcn.service.StreamingService.isLoggedIn;

@Slf4j
public class IncomeService {

    public static List<IncomeInfo> getIncomeInfoList(WebDriver driver, Integer rowLimit) {

        if (!Objects.equals(driver.getCurrentUrl(), INCOME_INFO_URL))
            driver.get(INCOME_INFO_URL);

        if (!isLoggedIn(driver)) {
            log.info("not logged in, abort getIncomeInfoList");
            return Collections.emptyList();
        }

        String incomeTableRowsXpathString = "/html/body/div[1]/div/div[2]/div[2]/div/div/div/div[2]/div[2]/div/div[1]/div/div/div/div/div[1]/div/table/tbody/tr";
        if (rowLimit != null && rowLimit > 1)
            incomeTableRowsXpathString += "[position()<%d]".formatted(rowLimit + 1);
        By incomeTableRowsXpath = By.xpath(incomeTableRowsXpathString);

        List<WebElement> rows = driver.findElements(incomeTableRowsXpath);

        List<IncomeInfo> incomeInfoList = rows.stream()
                .map(row -> {
                    try {
                        String streamingTitle = row.findElement(By.xpath("./td[1]/div/div/div/div[1]")).getAttribute("textContent");
                        if (streamingTitle.isBlank())
                            streamingTitle = "暂无";
                        String streamingTime = row.findElement(By.xpath("./td[1]/div/div/div/div[2]")).getAttribute("textContent");
                        String streamingDuration = row.findElement(By.xpath("./td[2]/div/div")).getAttribute("textContent");
                        int numberOfWatch = Integer.parseInt(row.findElement(By.xpath("./td[3]/div/div")).getAttribute("textContent"));
                        BigDecimal income = new BigDecimal(row.findElement(By.xpath("./td[6]/div/div")).getAttribute("textContent").substring(1));
                        return new IncomeInfo(streamingTitle, streamingTime, streamingDuration, numberOfWatch, income);
                    } catch (Exception e) {
                        log.error("retrieve incomeInfo wrong: ", e);
                        return IncomeInfo.emptyIncomeInfo();
                    }
                })
                .toList();

        return incomeInfoList;

    }

}
