package fun.dokcn.service;

import com.hellokaton.blade.ioc.annotation.Bean;
import com.hellokaton.blade.ioc.annotation.Inject;
import fun.dokcn.entity.TriggerInfo;
import fun.dokcn.mapper.TriggerMapper;
import fun.dokcn.util.DbUtil;
import org.apache.ibatis.session.SqlSessionFactory;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerKey;

import java.time.LocalTime;

import static fun.dokcn.util.SchedulingUtil.*;

@Bean
public class TriggerService {

    @Inject
    private SqlSessionFactory sqlSessionFactory;

    public void insert() {
        DbUtil.dbAction(sqlSessionFactory, TriggerMapper.class, mapper -> {
            TriggerInfo trigger = TriggerInfo.builder()
                    .name("testKey")
                    .time(LocalTime.now())
                    .build();
            System.out.println(trigger);
            mapper.insert(trigger);
            return null;
        });
    }

    public void addOrModifyTrigger(String triggerName, LocalTime triggerTime, boolean repeated, boolean changeTriggerTime) {
        try {
            TriggerKey triggerKey = null;
            if (changeTriggerTime && !scheduler.checkExists(triggerKey = TriggerKey.triggerKey(triggerName))) {
                throw new IllegalArgumentException("trigger not found: " + triggerName);
            }

            Trigger trigger = createTrigger(triggerName, CLOSE_STREAMING_JOB_KEY, triggerTime, repeated);

            if (changeTriggerTime) {
                scheduler.rescheduleJob(triggerKey, trigger);
            } else {
                // todo: avoid trigger of same time to be added
                scheduler.scheduleJob(trigger);
            }
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
    }

    public void removeTrigger(String triggerName) {
        try {
            TriggerKey triggerKey = TriggerKey.triggerKey(triggerName);
            if (!scheduler.checkExists(triggerKey)) {
                throw new IllegalArgumentException("trigger not found: " + triggerName);
            }

            scheduler.unscheduleJob(triggerKey);
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
    }

}
