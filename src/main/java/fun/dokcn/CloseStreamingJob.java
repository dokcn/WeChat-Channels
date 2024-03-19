package fun.dokcn;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.quartz.Job;
import org.quartz.JobExecutionContext;

import java.util.Date;

import static fun.dokcn.service.StreamingService.closeStreaming;

@Slf4j
public class CloseStreamingJob implements Job {

    @Override
    public void execute(JobExecutionContext context) {
        // todo: add retry logic
        log.info("firing close streaming");
        WebDriver driver = (WebDriver) context.getMergedJobDataMap().get("webDriver");
        closeStreaming(driver);
        Date nextTriggerTime = context.getNextFireTime();
        log.info("next trigger time: {}", nextTriggerTime == null ? "none" : nextTriggerTime);
    }

}
