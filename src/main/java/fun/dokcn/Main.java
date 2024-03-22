package fun.dokcn;

import cn.hutool.core.util.StrUtil;
import com.hellokaton.blade.Blade;
import com.hellokaton.blade.mvc.WebContext;
import fun.dokcn.entity.IncomeInfo;
import fun.dokcn.service.TriggerService;
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
import java.util.List;
import java.util.Optional;

import static fun.dokcn.Constants.*;
import static fun.dokcn.service.IncomeService.getIncomeInfoList;
import static fun.dokcn.service.StreamingService.*;
import static fun.dokcn.util.DateTimeUtil.DATE_TIME_FORMATTER;
import static fun.dokcn.util.DateTimeUtil.TIME_FORMATTER;
import static fun.dokcn.util.SchedulingUtil.*;

@Slf4j
public class Main {

    public static void main(String[] args) {

        log.info("starting program...");

        RemoteWebDriver driver = configSelenium();

        createBlade(driver).start(args);

    }

    static Blade createBlade(RemoteWebDriver driver) {
        log.info("creating blade...");
        Blade blade = Blade.create();
        blade.staticOptions().addStatic("\\static");

        blade.get("/", ctx -> {
                    boolean loggedIn = isLoggedIn(driver);
                    ctx.attribute("isLoggedIn", loggedIn);
                    if (loggedIn && !LOGIN_FINISHED) {
                        ctx.redirect("/finishLogin");
                        return;
                    }

                    String exception = ctx.query("exception");
                    ctx.attribute("exception", exception);

                    if (loggedIn) {
                        // boolean doNotCheckIsStreaming = ctx.query("doNotCheckIsStreaming") != null;
                        // ctx.attribute("isStreaming", !doNotCheckIsStreaming && isStreaming(driver));

                        LocalDateTime nextTriggerTime = getNextTriggerTime();
                        if (nextTriggerTime != null) {
                            ctx.attribute("nextTriggerTime", DATE_TIME_FORMATTER.format(nextTriggerTime));
                        }

                        ctx.attribute("triggers", getTriggerInfosOfJob(CLOSE_STREAMING_JOB_KEY));
                    }

                    ctx.render("home");
                })

                .get("isStreaming", ctx -> {
                    String result = """
                            {
                              "isStreaming": %b
                            }
                            """.formatted(isStreaming(driver));
                    // ctx.json(result);
                    ctx.badRequest();
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
                    toMainPage(driver);
                    isLoggedIn(driver, true);

                    synchronized (Main.class) {
                        if (LOGIN_FINISHED) {
                            log.info("actions after login has finished");
                        } else {
                            startRefresherThread(driver);
                            String defaultTriggerTime = WebContext.blade()
                                    .getEnv("trigger.default.closeStreamingTriggerTime", "22:40");
                            scheduleCloseStreaming(driver, defaultTriggerTime);
                            LOGIN_FINISHED = true;
                        }
                    }
                    ctx.redirect("/");
                })

                .get("/logout", ctx -> {
                    logout(driver);
                    ctx.redirect("/");
                })

                .post("/closeStreaming", ctx -> {
                    closeStreaming(driver);
                    ctx.redirect("/");
                });

        blade.post("/trigger/addOrModifyTrigger", ctx -> {
                    String triggerName = ctx.request().form("triggerName").orElse(null);

                    LocalTime time = ctx.request().form("time")
                            .map(timeString -> LocalTime.parse(timeString, TIME_FORMATTER))
                            .orElseThrow(() -> new IllegalArgumentException("time not passed"));

                    boolean repeated = ctx.request().formBoolean("repeated", false);

                    boolean changeTriggerTime = ctx.request().formBoolean("changeTriggerTime")
                            .orElseThrow(() -> new IllegalArgumentException("changeTriggerTime not passed"));

                    if (changeTriggerTime && StrUtil.isBlank(triggerName)) {
                        throw new IllegalArgumentException("triggerName not passed");
                    }

                    blade.getBean(TriggerService.class).addOrModifyTrigger(triggerName, time, repeated, changeTriggerTime);

                    ctx.redirect("/?doNotCheckIsStreaming");
                })

                .post("/trigger/removeTrigger", ctx -> {
                    String triggerName = ctx.request().form("triggerName").orElse(null);
                    if (StrUtil.isBlank(triggerName)) {
                        throw new IllegalArgumentException("triggerName not passed");
                    }

                    blade.getBean(TriggerService.class).removeTrigger(triggerName);

                    ctx.redirect("/?doNotCheckIsStreaming");
                });

        blade.get("/income", ctx -> {
            boolean loggedIn = isLoggedIn(driver);
            if (!loggedIn) {
                ctx.redirect("/login");
                return;
            }

            List<IncomeInfo> incomeInfoList = getIncomeInfoList(driver, 0);
            ctx.attribute("incomeInfoList", incomeInfoList);

            ctx.render("income");
        });

        blade.get("/test", ctx -> {

        });

        return blade;
    }

    static RemoteWebDriver configSelenium() {
        boolean headless = System.getProperty(HEADLESS_PROPERTY_NAME) != null;
        ChromiumDriverLogLevel webDriverLogLevel = ChromiumDriverLogLevel
                .fromString(System.getProperty(WEB_DRIVER_LOG_LEVEL_PROPERTY_NAME, "warning"));

        ChromeDriverService driverService = createDriverService(webDriverLogLevel);
        ChromeOptions options = createOptions(headless);

        RemoteWebDriver driver = new ChromeDriver(driverService, options);
        driver = new EventFiringDecorator<>(RemoteWebDriver.class, new SeleniumListener(driver))
                .decorate(driver);

        // Utils.recordConsoleLogs(driver);

        driver.get(MAIN_URL);
        driver.navigate().refresh();

        Runtime.getRuntime().addShutdownHook(new Thread(driver::quit));
        return driver;
    }

    static ChromeDriverService createDriverService(ChromiumDriverLogLevel logLevel) {
        ChromeDriverService.Builder driverServiceBuilder = new ChromeDriverService.Builder()
                .withLogOutput(System.out)
                .withLogLevel(logLevel);

        Optional<String> driverBinaryLocation = Optional.ofNullable(System.getProperty(DRIVER_BINARY_LOCATION_PROPERTY_NAME));
        driverBinaryLocation.ifPresent(binary -> {
            log.info("using chromedriver binary location: {}", binary);
            driverServiceBuilder.usingDriverExecutable(new File(binary));
        });

        return driverServiceBuilder.build();
    }

    static ChromeOptions createOptions(boolean headless) {
        ChromeOptions options = new ChromeOptions();

        Optional<String> browserBinaryLocation = Optional.ofNullable(System.getProperty(BROWSER_BINARY_LOCATION_PROPERTY_NAME));
        browserBinaryLocation.ifPresent(binary -> {
            log.info("using chrome binary location: {}", binary);
            options.setBinary(new File(binary));
        });

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

}
