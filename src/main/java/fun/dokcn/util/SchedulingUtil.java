package fun.dokcn.util;

import fun.dokcn.CloseStreamingJob;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.Optional;

import static fun.dokcn.Constants.*;
import static fun.dokcn.util.DateTimeUtil.dateToLocalDateTime;
import static fun.dokcn.util.DateTimeUtil.localDateTimeToDate;

@Slf4j
public class SchedulingUtil {

    static Scheduler scheduler;

    static {
        try {
            scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
    }

    public static LocalDateTime getNextTriggerTime() {
        LocalDateTime nextTriggerTime;
        try {
            Date now = new Date();
            LocalDateTime triggerNextTime = Optional.ofNullable(scheduler.getTrigger(TriggerKey.triggerKey(CLOSE_STREAMING_TRIGGER_KEY)))
                    .map(trigger -> dateToLocalDateTime(trigger.getFireTimeAfter(now)))
                    .orElse(null);
            LocalDateTime triggerOnceNextTime = Optional.ofNullable(scheduler.getTrigger(TriggerKey.triggerKey(CLOSE_STREAMING_ONCE_TRIGGER_KEY)))
                    .map(trigger -> dateToLocalDateTime(trigger.getFireTimeAfter(now)))
                    .orElse(null);

            if (triggerNextTime != null && triggerOnceNextTime != null) {
                nextTriggerTime = triggerNextTime.isBefore(triggerOnceNextTime) ?
                        triggerNextTime :
                        triggerOnceNextTime;
            } else {
                nextTriggerTime = triggerNextTime != null ?
                        triggerNextTime :
                        triggerOnceNextTime;
            }
            log.info("next trigger time after now: {}", nextTriggerTime);
            return nextTriggerTime;
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
    }

    static Trigger createTrigger(String triggerKey, LocalTime triggerTime, String jobKeyString) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime triggerDateTime = now.with(triggerTime);
        if (now.isAfter(triggerDateTime)) {
            triggerDateTime = triggerDateTime.plusDays(1);
        }
        log.info("schedule start time: {}", triggerDateTime);

        SimpleScheduleBuilder scheduleBuilder = CLOSE_STREAMING_ONCE_TRIGGER_KEY.equals(triggerKey) ?
                SimpleScheduleBuilder.simpleSchedule() :
                SimpleScheduleBuilder.repeatHourlyForever(24);
        scheduleBuilder.withMisfireHandlingInstructionIgnoreMisfires();

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(triggerKey)
                .forJob(Optional.ofNullable(jobKeyString).map(JobKey::jobKey).orElse(null))
                .startAt(localDateTimeToDate(triggerDateTime))
                .withSchedule(scheduleBuilder)
                .build();

        return trigger;
    }

    public static void scheduleCloseStreaming(WebDriver driver) {
        try {
            // if (scheduler.checkExists(JobKey.jobKey(CLOSE_STREAMING_JOB_KEY)))
            //     return;

            JobDataMap jobDataMap = new JobDataMap();
            jobDataMap.put("webDriver", driver);
            JobDetail jobDetail = JobBuilder.newJob(CloseStreamingJob.class)
                    .withIdentity(CLOSE_STREAMING_JOB_KEY)
                    .setJobData(jobDataMap)
                    .build();

            Trigger trigger = createTrigger(CLOSE_STREAMING_TRIGGER_KEY, CLOSE_STREAMING_TRIGGER_TIME, null);

            scheduler.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
    }

    public static void changeTriggerTime(LocalTime triggerTime, boolean isPermanent) {
        try {
            Trigger trigger = createTrigger(isPermanent ? CLOSE_STREAMING_TRIGGER_KEY : CLOSE_STREAMING_ONCE_TRIGGER_KEY,
                    triggerTime,
                    CLOSE_STREAMING_JOB_KEY);

            TriggerKey triggerKey = trigger.getKey();
            if (scheduler.getTrigger(triggerKey) != null) {
                scheduler.rescheduleJob(triggerKey, trigger);
            } else {
                scheduler.scheduleJob(trigger);
            }
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
    }

    public static void clearScheduler() {
        try {
            scheduler.clear();
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
    }

}
