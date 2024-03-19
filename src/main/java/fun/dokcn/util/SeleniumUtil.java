package fun.dokcn.util;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.devtools.events.CdpEventTypes;
import org.openqa.selenium.logging.HasLogEvents;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
public class SeleniumUtil {

    public static void takeScreenshot(WebDriver driver, String pathString) {
        if (driver instanceof TakesScreenshot takesScreenshot) {
            Path screenshotPath = takesScreenshot.getScreenshotAs(OutputType.FILE).toPath();
            String screenshotFilename = screenshotPath.getFileName().toString();
            try {
                Files.copy(screenshotPath, Path.of(pathString, screenshotFilename));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            StackTraceElement stackTraceElement = new Throwable().getStackTrace()[0];
            log.info("screenshot took at {}:{}", stackTraceElement.getClassName(),
                    stackTraceElement.getMethodName());
        } else {
            log.error("this web driver does not support take screenshot functionality");
        }
    }

    public static void recordConsoleLogs(WebDriver driver) {
        if (driver instanceof HasLogEvents logEvents) {
            logEvents.onLogEvent(CdpEventTypes.consoleEvent(
                    consoleEvent -> log.info("console log: [{}]{}", consoleEvent.getType(), consoleEvent.getMessages()))
            );
        } else {
            log.error("this web driver does not support capture console log functionality");
        }
    }

    // todo fix zoom
    public static void scalePage(WebDriver driver, boolean scaleUp) throws AWTException {
        // WebElement htmlElement = driver.findElement(By.tagName("html"));
        // htmlElement.sendKeys(Keys.chord(Keys.CONTROL, scaleUp ? Keys.ADD : Keys.SUBTRACT));

        Robot robot = new Robot();
        int key = scaleUp ? KeyEvent.VK_ADD : KeyEvent.VK_SUBTRACT;
        robot.keyPress(KeyEvent.VK_CONTROL);
        robot.keyPress(key);
        robot.keyRelease(KeyEvent.VK_CONTROL);
        robot.keyRelease(key);
    }

}
