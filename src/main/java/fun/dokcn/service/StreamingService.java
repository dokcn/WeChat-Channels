package fun.dokcn.service;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static fun.dokcn.Constants.*;
import static fun.dokcn.util.DateTimeUtil.toGMTString;
import static fun.dokcn.util.MiscUtil.sleepInSeconds;
import static fun.dokcn.util.RandomUtil.randomIntegerInRange;
import static fun.dokcn.util.SchedulingUtil.clearScheduler;
import static fun.dokcn.util.StringUtil.ifBlank;

@Slf4j
public class StreamingService {

    // TODO: save cookies when container stop and load it while container up, avoid login state invalid
    public static void saveCookies(WebDriver driver) throws Exception {
        // driver.get(MAIN_URL);
        // TODO remove sleep
        // TimeUnit.SECONDS.sleep(10);

        Set<Cookie> cookies = driver.manage().getCookies();
        Path path = Path.of(COOKIES_FILE_PATH);
        try (FileChannel f = FileChannel.open(path, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
        }

        try (PrintWriter writer = new PrintWriter(path.toFile())) {
            for (Cookie cookie : cookies) {
                StringJoiner joiner = new StringJoiner(COOKIE_PARTS_DELIMITER);
                joiner.add(cookie.getName())
                        .add(cookie.getValue())
                        .add(cookie.getDomain())
                        .add(cookie.getPath())
                        .add(Boolean.toString(cookie.isSecure()))
                        .add(Boolean.toString(cookie.isHttpOnly()))
                        .add(cookie.getSameSite());
                System.out.println(joiner);
                writer.println(joiner);
            }
            writer.flush();
        }
    }

    public static void loadCookies(WebDriver driver) throws Exception {
        // driver.get(MAIN_URL);

        try (BufferedReader reader = new BufferedReader(new FileReader(COOKIES_FILE_PATH))) {
            List<String> lines = reader.lines().toList();
            if (lines.isEmpty()) {
                System.out.println("no cookies data");
                return;
            }

            driver.manage().deleteAllCookies();
            lines.forEach(line -> {
                if (StrUtil.isNotBlank(line)) {
                    String script = generateAddCookieScript(line);
                    ((JavascriptExecutor) driver).executeScript(script);
                }
            });
        }
        driver.get(MAIN_URL);
    }

    public static String generateAddCookieScript(String line) {
        StringTokenizer tokenizer = new StringTokenizer(line, COOKIE_PARTS_DELIMITER);

        String name = tokenizer.nextToken();
        String value = tokenizer.nextToken();
        String domain = ifBlank(tokenizer.nextToken(), ";domain=%s"::formatted);
        String path = ifBlank(tokenizer.nextToken(), ";path=%s"::formatted);
        String expiry = ifBlank(toGMTString(LocalDateTime.now().plusMonths(2)), ";expires=%s"::formatted);
        String isSecure = ifBlank(tokenizer.nextToken(), it -> Boolean.parseBoolean(it) ? ";Secure" : "");
        String isHttpOnly = ifBlank(tokenizer.nextToken(), it -> Boolean.parseBoolean(it) ? ";HttpOnly" : "");
        String sameSite = ifBlank(tokenizer.nextToken(), ";SameSite=%s"::formatted);
        // if (driver.manage().getCookieNamed(name) != null) {
        //     driver.manage().deleteCookieNamed(name);
        // }
        String script = """
                document.cookie = "%s=%s %s %s %s %s %s %s"
                """.formatted(name, value, domain, path, expiry, isSecure, isHttpOnly, sameSite);
        System.out.println(script);
        return script;
    }

    public static boolean isLoggedIn(WebDriver driver, boolean throwException) {
        // driver.get(MAIN_URL);
        /*new WebDriverWait(driver, Duration.ofSeconds(5)).until(ExpectedConditions.jsReturnsValue("""
                "return document.readyState === 'complete'
                """));*/
        String currentUrl = driver.getCurrentUrl();
        // log.info("isLoggedIn currentUrl: {}", currentUrl);
        boolean notLoggedIn = LOGIN_URL.equals(currentUrl);
        if (notLoggedIn && throwException)
            throw new IllegalStateException("not logged in");
        return !notLoggedIn;
    }

    public static boolean isLoggedIn(WebDriver driver) {
        return isLoggedIn(driver, false);
    }

    /*public static boolean isLoggedIn = false;

    public static void waitForLoggedIn(WebDriver driver) {
        Wait<WebDriver> wait = new WebDriverWait(driver,
                ChronoUnit.FOREVER.getDuration(),
                Duration.ofSeconds(1));
        wait.until(ExpectedConditions.urlToBe(HOMEPAGE_URL));
        isLoggedIn = true;
    }*/

    private static final ExecutorService IS_STREAMING_DETECTOR_POOL = Executors.newCachedThreadPool();

    public record UrlNotToBeCondition(String testUrl) implements ExpectedCondition<Boolean> {
        @Override
        public Boolean apply(WebDriver driver) {
            String currentUrl = driver.getCurrentUrl();
            return !testUrl.equals(currentUrl);
        }
    }

    public static synchronized boolean isStreaming(WebDriver driver) {
        if (!isLoggedIn(driver)) return false;

        if (toPage(driver, STREAMING_CONTROL_URL)) {
            driver.navigate().refresh();
        }

        try {
            return new WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(new UrlNotToBeCondition(STREAMING_CONTROL_URL));
        } catch (TimeoutException e) {
            return true;
        }

        /*if (toMainPage(driver))
            driver.navigate().refresh();

        By loadingXpath = By.xpath("//*[@id=\"container-wrap\"]/div[1]");
        try {
            Wait<WebDriver> wait = new WebDriverWait(driver, Duration.ofSeconds(5));
            wait.until(ExpectedConditions.invisibilityOfElementLocated(loadingXpath));
        } catch (Exception e) {
            System.out.println("wait for loading layer wrong: " + e);
        }

        try {
            By isLivingTextBy = By.cssSelector("#container-wrap > div.container-center > div > div > div > div.main-body > div.live-entrance > div > div.introduction > div");
            Wait<WebDriver> waitForEnterStreamingRoomButton = new WebDriverWait(driver, Duration.ofSeconds(3));
            waitForEnterStreamingRoomButton.until(new FindElementInShadowDomCondition(By.xpath("//*[@id=\"container-wrap\"]/div[2]/div/wujie-app"), isLivingTextBy));
            System.out.println("waitForEnterStreamingRoomButton success");
            return true;
        } catch (Throwable e) {
            System.out.println("waitForEnterStreamingRoomButton failed: " + e);
            return false;
        }*/
    }

    private record FindElementInShadowDomCondition(By shadowHostBy,
                                                   By targetBy) implements ExpectedCondition<WebElement> {
        @Override
        public WebElement apply(WebDriver driver) {
            WebElement shadowHost = new WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(ExpectedConditions.presenceOfElementLocated(shadowHostBy));
            SearchContext shadowRoot = shadowHost.getShadowRoot();
            /*try {
                return shadowRoot.findElement(targetBy);
            } catch (StaleElementReferenceException e) {
                return null;
            }*/
            return fineElementInShadowDom(driver, shadowHostBy, targetBy);
        }
    }

    private static WebElement fineElementInShadowDom(WebDriver driver, By shadowHostBy, By targetBy) {
        JavascriptExecutor javascriptExecutor = (JavascriptExecutor) driver;
        // WebElement shadowHost = driver.findElement(shadowHostBy);
        // SearchContext shadowRoot = (SearchContext) javascriptExecutor.executeScript("return arguments[0].shadowRoot", shadowHost);
        // return shadowRoot.findElement(targetBy);
        WebElement e = (WebElement) javascriptExecutor.executeScript("return document.querySelector(\"#container-wrap > div.container-center > div > wujie-app\").shadowRoot.querySelector(\"#container-wrap > div.container-center > div > div > div > div.main-body > div.live-entrance > div > div.introduction > div\")");
        return e;
    }

    /**
     * @return true indicates need to refresh page
     */
    public static boolean toMainPage(WebDriver driver) {
        return toPage(driver, MAIN_URL);
    }

    /**
     * @return true indicates need to refresh page
     */
    public static boolean toPage(WebDriver driver, String page) {
        String currentUrl = driver.getCurrentUrl();
        if (!page.equals(currentUrl)) {
            driver.get(page);
            return false;
        }
        return true;
    }

    public static String getLoginQrCode(WebDriver driver) {
        driver.get(LOGIN_URL);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.urlToBe(LOGIN_URL));
        wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(0));

        WebElement qrCodeElement = driver.findElement(By.className("qrcode"));
        String qrCodeDataUrl = qrCodeElement.getAttribute("src");
        /*byte[] decodedQrCode = Base64.getMimeDecoder()
                .decode(qrCodeDataUrl.substring(qrCodeDataUrl.indexOf(',')));
        Files.write(Path.of(QR_CODE_FILE_PATH), decodedQrCode);*/
        return qrCodeDataUrl;
    }

    public static volatile boolean LOGIN_FINISHED = false;

    public static void startRefresherThread(WebDriver driver) {
        log.info("enter startRefresherThread method");
        Thread thread = new Thread(() -> {
            while (true) {
                try {
                    TimeUnit.MINUTES.sleep(randomIntegerInRange(2, 5));
                    // TimeUnit.SECONDS.sleep(3);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                synchronized (StreamingService.class) {
                    try {
                        if (!LOGIN_FINISHED) {
                            log.info("already logged out, quit refresher");
                            break;
                        }

                        log.info("starting refresh");
                        // driver.navigate().refresh();
                        By homepageButtonXpath = By.xpath("/html/body/div[1]/div/div[1]/div/div/ul/li[1]/a");
                        driver.findElement(homepageButtonXpath).click();
                        sleepInSeconds(2);
                        driver.get(MAIN_URL);

                        log.info("refresh finish");
                    } catch (Exception e) {
                        log.error("refresh failed: ", e);
                    }
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
        log.info("refresher thread started");
    }

    public static synchronized void closeStreaming(WebDriver driver) {
        try {
            if (toMainPage(driver)) {
                driver.navigate().refresh();
            }

            isLoggedIn(driver, true);

            By enterStreamingRoomButtonXpath = By.xpath("/html/body/div[1]/div/div[2]/div[2]/div/div/div/div[3]/div[1]/div/div[2]/div/button");

            By closeStreamingButtonXpath = By.xpath("/html/body/div[1]/div/div[2]/div[2]/div/div/div/div[1]/div[2]/div[2]/div/button");
            By closeStreamingButtonAlternateXpath = By.xpath("/html/body/div[1]/div/div[2]/div[2]/div/div/div/div[1]/div[2]/div[3]/div/button");

            By closeStreamingConfirmButtonXpath = By.xpath("/html/body/div[3]/div/div/div/div[2]/div/div/div/div/div[2]/div[2]/button");
            By closeStreamingConfirmButtonAlternateXpath = By.xpath("/html/body/div[4]/div/div/div/div[2]/div/div/div/div/div[2]/div[2]/button");

            Wait<WebDriver> wait = new WebDriverWait(driver, Duration.ofSeconds(10));

            WebElement enterStreamingRoomButtonElement = wait.until(ExpectedConditions.elementToBeClickable(enterStreamingRoomButtonXpath));
            enterStreamingRoomButtonElement.click();

            try {
                WebElement closeStreamingButtonElement = wait.until(ExpectedConditions.elementToBeClickable(closeStreamingButtonXpath));
                closeStreamingButtonElement.click();
            } catch (Exception e) {
                WebElement closeStreamingButtonElement = wait.until(ExpectedConditions.elementToBeClickable(closeStreamingButtonAlternateXpath));
                closeStreamingButtonElement.click();
            }

            try {
                WebElement closeStreamingConfirmButtonElement = wait.until(ExpectedConditions.elementToBeClickable(closeStreamingConfirmButtonXpath));
                closeStreamingConfirmButtonElement.click();
            } catch (Exception e) {
                WebElement closeStreamingConfirmButtonElement = wait.until(ExpectedConditions.elementToBeClickable(closeStreamingConfirmButtonAlternateXpath));
                closeStreamingConfirmButtonElement.click();
            }
        } catch (Exception e) {
            log.error("close streaming fail: ", e);
        }
    }

    public static synchronized void closeStreaming2(WebDriver driver) {
        try {
            isLoggedIn(driver, true);

            if (!toPage(driver, STREAMING_CONTROL_URL)) {
                sleepInSeconds(5);
            }

            String scriptForCloseStreaming = """
                    document.querySelector('#container-wrap > div.container-center > div > wujie-app')
                        .shadowRoot
                        .querySelector('#container-wrap > div.container-center > div > div > div > div.live-realtime-title > div.live-realtime-title-right > div.content > div > button')
                        .click()
                    """;
            String scriptForConfirmCloseStreaming = """
                    document.querySelector('#container-wrap > div.container-center > div > wujie-app')
                    .shadowRoot
                    .querySelector('body > div:nth-child(4) > div > div > div > div.ant-popover-inner > div > div > div > div > div.dialog-ft > div.weui-desktop-btn_wrp.ok > button')
                    .click()
                    """;

            JavascriptExecutor javascriptExecutor = (JavascriptExecutor) driver;
            javascriptExecutor.executeScript(scriptForCloseStreaming);
            sleepInSeconds(2);
            javascriptExecutor.executeScript(scriptForConfirmCloseStreaming);
        } catch (Exception e) {
            log.error("close streaming fail: ", e);
        }
    }

    public static synchronized void logout(WebDriver driver) {
        if (!isLoggedIn(driver)) {
            log.info("not logged in, abort logout");
            return;
        }

        toMainPage(driver);

        // todo: add wait
        By usernameBarXpath = By.xpath("/html/body/div[1]/div/div[1]/div/div/div[2]/div/div[2]");
        WebElement usernameBarElement = driver.findElement(usernameBarXpath);
        usernameBarElement.click();

        By logoutButtonXpath = By.xpath("/html/body/div[1]/div/div[1]/div/div/div[2]/div/div[2]/div[1]/div[2]");
        WebElement logoutButtonElement = driver.findElement(logoutButtonXpath);
        logoutButtonElement.click();

        clearScheduler();
        sleepInSeconds(2);
        LOGIN_FINISHED = false;
    }

}
