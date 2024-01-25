package fun.dokcn;

import com.hellokaton.blade.Blade;
import com.hellokaton.blade.mvc.http.Request;
import fun.dokcn.util.SchedulingUtil;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.chromium.ChromiumDriverLogLevel;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.events.EventFiringDecorator;

import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

import static fun.dokcn.Constants.*;
import static fun.dokcn.service.SeleniumService.*;
import static fun.dokcn.util.DateTimeUtil.DATE_TIME_FORMATTER;
import static fun.dokcn.util.DateTimeUtil.TIME_FORMATTER;
import static fun.dokcn.util.SchedulingUtil.getNextTriggerTime;
import static fun.dokcn.util.SchedulingUtil.scheduleCloseStreaming;

@Slf4j
public class Main {

    public static void main(String[] args) {

        log.info("starting program...");

        boolean headless = System.getProperty(HEADLESS_PROPERTY_NAME) != null;
        ChromiumDriverLogLevel webDriverLogLevel = ChromiumDriverLogLevel
                .fromString(System.getProperty(WEB_DRIVER_LOG_LEVEL_PROPERTY_NAME, "warning"));

        ChromeOptions options = createOptions(headless);
        ChromeDriverService driverService = createDriverService(webDriverLogLevel);

        RemoteWebDriver driver = new ChromeDriver(driverService, options);
        driver = new EventFiringDecorator<>(RemoteWebDriver.class, new SeleniumListener(driver))
                .decorate(driver);

        // Utils.recordConsoleLogs(driver);

        driver.get(HOMEPAGE_URL);
        driver.navigate().refresh();

        Runtime.getRuntime().addShutdownHook(new Thread(driver::quit));

        createBlade(driver).start(args);

    }

    static Blade createBlade(RemoteWebDriver driver) {
        log.info("creating blade...");
        return Blade.create()
                .get("/", ctx -> {
                    boolean loggedIn = isLoggedIn(driver);
                    if (loggedIn && !LOGIN_FINISHED) {
                        ctx.redirect("/finishLogin");
                        return;
                    }

                    String exception = ctx.query("exception");
                    ctx.attribute("exception", exception);

                    ctx.attribute("isLoggedIn", loggedIn);
                    ctx.attribute("isStreaming", isStreaming(driver,
                            ctx.query("doNotCheckIsStreaming") == null));

                    LocalDateTime nextTriggerTime = getNextTriggerTime();
                    if (nextTriggerTime != null) {
                        ctx.attribute("nextTriggerTime", DATE_TIME_FORMATTER.format(nextTriggerTime));
                        ctx.attribute("timePlaceholder", TIME_FORMATTER.format(nextTriggerTime));
                    }

                    ctx.render("home");
                })

                .get("/login", ctx -> {
                    String qrCodeDataUrl = getLoginQrCode(driver);
                    ctx.attribute("qrCodeImageSrc", qrCodeDataUrl);
                    ctx.render("login");
                })

                .get("/checkLoggedIn", ctx -> {
                    ctx.text(isLoggedIn(driver) ? "true" : "false");
                })

                .get("/finishLogin", ctx -> {
                    toHomepage(driver);
                    isLoggedIn(driver, true);

                    synchronized (Main.class) {
                        if (LOGIN_FINISHED) {
                            log.info("actions after login has finished");
                        } else {
                            startRefresherThread(driver);
                            scheduleCloseStreaming(driver);
                            LOGIN_FINISHED = true;
                        }
                    }
                    ctx.redirect("/?doNotCheckIsStreaming");
                })

                .get("/logout", ctx -> {
                    logout(driver);
                    ctx.redirect("/?doNotCheckIsStreaming");
                })

                .post("/closeStreaming", ctx -> {
                    closeStreaming(driver);
                    ctx.redirect("/");
                })

                .post("/changeCloseStreamingTime", ctx -> {
                    Request request = ctx.request();
                    LocalTime timeToChange = request.form("timeToChange")
                            .map(time -> LocalTime.parse(time, TIME_FORMATTER))
                            .orElseThrow(() -> new IllegalArgumentException("timeToChange not passed"));
                    boolean isPermanent = request.formBoolean("isPermanent", false);
                    SchedulingUtil.changeTriggerTime(timeToChange, isPermanent);

                    ctx.redirect("/?doNotCheckIsStreaming");
                });
    }

    static ChromeOptions createOptions(boolean headless) {
        ChromeOptions options = new ChromeOptions();

        Optional<String> browserBinaryLocation = Optional.ofNullable(System.getProperty(BROWSER_BINARY_LOCATION_PROPERTY_NAME));
        browserBinaryLocation.ifPresent(binary -> options.setBinary(new File(binary)));

        options.setImplicitWaitTimeout(Duration.ofSeconds(5));

        // Disable a few things considered not appropriate for automation
        options.addArguments("--enable-automation");
        // disable "Chrome is being controlled by automated test software" info bar
        // options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});

        // Starts the browser maximized, regardless of any previous settings
        options.addArguments("--start-maximized");

        // Disable all chrome extensions
        options.addArguments("--disable-extensions");

        if (headless) {
            // New, native Headless mode
            options.addArguments("--headless=new");
            options.addArguments("--user-agent=Mozilla/5.0 " +
                    "(X11; Ubuntu; Linux x86_64) " +
                    "AppleWebKit/537.36 (KHTML, like Gecko) " +
                    "Chrome/120.0.0.0 " +
                    "Safari/537.36 " +
                    "Edg/120.0.0.0");
        }

        // Disables GPU hardware acceleration. If software renderer is not in place, then the GPU process won't launch
        options.addArguments("--disable-gpu");

        // The /dev/shm partition is too small in certain VM environments, causing Chrome to fail or crash
        options.addArguments("--disable-dev-shm-usage");

        // Disables the sandbox for all process types that are normally sandboxed
        options.addArguments("--no-sandbox");

        return options;
    }

    static ChromeDriverService createDriverService(ChromiumDriverLogLevel logLevel) {
        ChromeDriverService.Builder driverServiceBuilder = new ChromeDriverService.Builder()
                .withLogOutput(System.out)
                .withLogLevel(logLevel);

        Optional<String> driverBinaryLocation = Optional.ofNullable(System.getProperty(DRIVER_BINARY_LOCATION_PROPERTY_NAME));
        driverBinaryLocation.ifPresent(binary -> driverServiceBuilder.usingDriverExecutable(new File(binary)));

        return driverServiceBuilder.build();
    }

}
