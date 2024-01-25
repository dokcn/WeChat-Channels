package fun.dokcn;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.quartz.Job;
import org.quartz.JobExecutionContext;

import java.util.Date;

import static fun.dokcn.service.SeleniumService.closeStreaming;

@Slf4j
public class CloseStreamingJob implements Job {

    @Override
    public void execute(JobExecutionContext context) {
        log.info("firing close streaming");
        WebDriver driver = (WebDriver) context.getMergedJobDataMap().get("webDriver");
        closeStreaming(driver);
        Date nextFireTime = context.getNextFireTime();
        log.info("next fire time: {}", nextFireTime == null ? "none" : nextFireTime);
    }

}
