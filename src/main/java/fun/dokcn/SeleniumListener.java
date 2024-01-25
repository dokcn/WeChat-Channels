package fun.dokcn;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.events.WebDriverListener;

@Slf4j
@RequiredArgsConstructor
public class SeleniumListener implements WebDriverListener {

    private final WebDriver driver;

    @Override
    public void afterGet(WebDriver driver, String url) {
        log.info("afterGet: url: {}, current url: {}", url, driver.getCurrentUrl());
    }

    @Override
    public void afterRefresh(WebDriver.Navigation navigation) {
        log.info("afterRefresh: url: {}", driver.getCurrentUrl());
    }

}
