package fun.dokcn.util;

import fun.dokcn.CloseStreamingJob;
import fun.dokcn.entity.TriggerInfo;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static fun.dokcn.Constants.CLOSE_STREAMING_JOB_NAME;
import static fun.dokcn.Constants.STREAMING_GROUP;
import static fun.dokcn.util.DateTimeUtil.*;
import static fun.dokcn.util.RandomUtil.randomString;

@Slf4j
public class SchedulingUtil {

    public static Scheduler scheduler;

    public static final JobKey CLOSE_STREAMING_JOB_KEY = new JobKey(CLOSE_STREAMING_JOB_NAME, STREAMING_GROUP);

    static {
        try {
            scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
    }

    public static LocalDateTime getNextTriggerTime() {
        try {
            Date now = new Date();
            List<? extends Trigger> triggersOfJob = getTriggersOfJob(CLOSE_STREAMING_JOB_KEY);
            LocalDateTime nextTriggerTime = triggersOfJob.stream()
                    .map(trigger -> trigger.getFireTimeAfter(now))
                    .filter(Objects::nonNull)
                    .map(DateTimeUtil::dateToLocalDateTime)
                    .sorted()
                    .findFirst()
                    .orElse(null);
            log.info("next trigger time after now: {}", nextTriggerTime == null ? "none" : nextTriggerTime);
            return nextTriggerTime;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static List<? extends Trigger> getTriggersOfJob(JobKey jobKey) {
        try {
            return scheduler.getTriggersOfJob(CLOSE_STREAMING_JOB_KEY);
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<TriggerInfo> getTriggerInfosOfJob(JobKey jobKey) {
        Date now = new Date();
        return getTriggersOfJob(CLOSE_STREAMING_JOB_KEY)
                .stream()
                .filter(trigger -> trigger.getFireTimeAfter(now) != null)
                .map(trigger -> {
                    Date triggerTimeAfterNow = trigger.getFireTimeAfter(now);
                    boolean repeated = trigger.getFinalFireTime() == null;
                    return TriggerInfo.builder()
                            .name(trigger.getKey().getName())
                            .nextTriggerTime(dateToLocalDateTime(triggerTimeAfterNow))
                            .repeated(repeated)
                            .build();
                })
                .toList();
    }

    public static Trigger createTrigger(String triggerName, JobKey jobKey,
                                        LocalTime triggerTime, boolean repeated) {
        if (jobKey == null) {
            throw new IllegalArgumentException("jobKey cannot be null");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime triggerDateTime = now.with(triggerTime);
        if (now.isAfter(triggerDateTime)) {
            triggerDateTime = triggerDateTime.plusDays(1);
        }
        log.info("schedule start time: {}", triggerDateTime);

        SimpleScheduleBuilder scheduleBuilder = repeated ?
                SimpleScheduleBuilder.repeatHourlyForever(24) :
                SimpleScheduleBuilder.simpleSchedule();
        scheduleBuilder.withMisfireHandlingInstructionIgnoreMisfires();

        if (triggerName == null) {
            triggerName = "%s-%s-%b-%s".formatted(jobKey.toString(),
                    DATE_TIME_FORMATTER.format(triggerDateTime),
                    repeated,
                    randomString(6));
        }

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(triggerName)
                .forJob(jobKey)
                .startAt(localDateTimeToDate(triggerDateTime))
                .withSchedule(scheduleBuilder)
                .build();

        return trigger;
    }

    public static void scheduleCloseStreaming(WebDriver driver, String defaultTriggerTime) {
        try {
            JobDataMap jobDataMap = new JobDataMap();
            jobDataMap.put("webDriver", driver);
            JobDetail jobDetail = JobBuilder.newJob(CloseStreamingJob.class)
                    .withIdentity(CLOSE_STREAMING_JOB_KEY)
                    .setJobData(jobDataMap)
                    .storeDurably()
                    .build();

            Trigger trigger = createTrigger(null,
                    CLOSE_STREAMING_JOB_KEY,
                    LocalTime.parse(defaultTriggerTime, TIME_FORMATTER),
                    true);
            scheduler.scheduleJob(jobDetail, trigger);

            // scheduler.addJob(jobDetail, true);
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
