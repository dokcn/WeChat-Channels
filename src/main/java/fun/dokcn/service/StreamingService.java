package fun.dokcn.service;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Set;
import java.util.StringJoiner;
import java.util.StringTokenizer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static fun.dokcn.Constants.*;
import static fun.dokcn.util.MiscUtil.sleepInSeconds;
import static fun.dokcn.util.RandomUtil.randomIntegerInRange;
import static fun.dokcn.util.SchedulingUtil.clearScheduler;

@Slf4j
public class StreamingService {

    public static void saveCookies(WebDriver driver) throws Exception {
        driver.get(HOMEPAGE_URL);
        // TODO remove sleep
        TimeUnit.SECONDS.sleep(10);

        Set<Cookie> cookies = driver.manage().getCookies();
        Path path = Path.of(COOKIES_FILE_PATH);

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
        }
    }

    public static void loadCookies(WebDriver driver) throws Exception {
        driver.get(HOMEPAGE_URL);

        try (BufferedReader reader = new BufferedReader(new FileReader(COOKIES_FILE_PATH))) {
            String line;
            while (StrUtil.isNotBlank(line = reader.readLine())) {
                StringTokenizer tokenizer = new StringTokenizer(line, COOKIE_PARTS_DELIMITER);
                Cookie cookie = new Cookie(tokenizer.nextToken(), tokenizer.nextToken(),
                        tokenizer.nextToken(), tokenizer.nextToken(),
                        Date.from(LocalDateTime.now().plusMonths(2).toInstant(ZoneOffset.ofHours(8))),
                        Boolean.parseBoolean(tokenizer.nextToken()), Boolean.parseBoolean(tokenizer.nextToken()),
                        tokenizer.nextToken());
                System.out.println(cookie);
                driver.manage().addCookie(cookie);
            }
        }
        driver.navigate().to(HOMEPAGE_URL);
    }

    public static boolean isLoggedIn(WebDriver driver, boolean throwException) {
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

    public static boolean isStreaming(WebDriver driver) {
        if (!isLoggedIn(driver)) return false;

        if (toHomepage(driver))
            driver.navigate().refresh();

        By loadingXpath = By.xpath("//*[@id=\"container-wrap\"]/div[1]");
        By startStreamingButtonXpath = By.xpath("/html/body/div[1]/div/div[2]/div[2]/div/div/div/div[3]/div[1]/div[1]/div/div[3]/div[2]/div/button");
        By enterStreamingRoomButtonXpath = By.xpath("/html/body/div[1]/div/div[2]/div[2]/div/div/div/div[3]/div[1]/div/div[2]/div/button");

        try {
            Wait<WebDriver> wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait.until(ExpectedConditions.invisibilityOfElementLocated(loadingXpath));
        } catch (Exception e) {
            log.error("wait for loading layer wrong: ", e);
        }

        boolean isStreaming = CompletableFuture.anyOf(
                CompletableFuture.supplyAsync(() -> {
                    try {
                        Wait<WebDriver> waitForStartStreamingButton = new WebDriverWait(driver, Duration.ofSeconds(5));
                        waitForStartStreamingButton.until(ExpectedConditions.presenceOfElementLocated(startStreamingButtonXpath));
                        return false;
                    } catch (Exception e) {
                        return true;
                    }
                }, IS_STREAMING_DETECTOR_POOL),
                CompletableFuture.supplyAsync(() -> {
                    try {
                        Wait<WebDriver> waitForEnterStreamingRoomButton = new WebDriverWait(driver, Duration.ofSeconds(5));
                        waitForEnterStreamingRoomButton.until(ExpectedConditions.presenceOfElementLocated(enterStreamingRoomButtonXpath));
                        return true;
                    } catch (Exception e) {
                        return false;
                    }
                }, IS_STREAMING_DETECTOR_POOL)
        ).handle((result, error) -> error == null && (boolean) result).join();

        return isStreaming;
    }

    /**
     * @return true indicates need to refresh page
     */
    public static boolean toHomepage(WebDriver driver) {
        String currentUrl = driver.getCurrentUrl();
        if (!HOMEPAGE_URL.equals(currentUrl)) {
            driver.get(HOMEPAGE_URL);
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

    static volatile boolean inOperation = false;
    static volatile boolean inRefreshing = false;

    public static volatile boolean LOGIN_FINISHED = false;

    public static void startRefresherThread(WebDriver driver) {
        log.info("enter startRefresherThread method");
        Thread thread = new Thread(() -> {
            while (true) {
                try {
                    TimeUnit.MINUTES.sleep(randomIntegerInRange(10, 30));
                    // TimeUnit.SECONDS.sleep(15);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                if (!inOperation) {
                    inRefreshing = true;
                    driver.navigate().refresh();
                    log.info("page refreshed");
                    inRefreshing = false;
                } else {
                    log.info("in operation");
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
        log.info("refresher thread started");
    }

    public static synchronized void closeStreaming(WebDriver driver) {
        // todo: handle situation of not streaming
        isLoggedIn(driver, true);

        while (inRefreshing) {
            log.info("refreshing");
        }

        inOperation = true;
        try {
            if (toHomepage(driver)) {
                driver.navigate().refresh();
            }

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
        } finally {
            inOperation = false;
        }
    }

    public static void logout(WebDriver driver) {
        if (!isLoggedIn(driver)) {
            log.info("not logged in, abort logout");
            return;
        }

        toHomepage(driver);

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
