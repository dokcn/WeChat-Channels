package fun.dokcn.controller;

import cn.hutool.core.util.StrUtil;
import com.hellokaton.blade.annotation.request.Form;
import com.hellokaton.blade.annotation.route.POST;
import com.hellokaton.blade.ioc.annotation.Inject;
import com.hellokaton.blade.mvc.WebContext;
import fun.dokcn.service.TriggerService;

import java.time.LocalTime;
import java.util.Optional;

import static fun.dokcn.util.DateTimeUtil.TIME_FORMATTER;

// @Path("/trigger")
public class TriggerController {

    @Inject
    private TriggerService triggerService;

    @POST("/addOrModifyTrigger")
    public void addOrModifyTrigger(@Form(name = "triggerName") String triggerName,
                                   @Form(name = "time") String timeParam,
                                   @Form(name = "repeated", defaultValue = "false") boolean repeated,
                                   @Form(name = "changeTriggerTime") boolean changeTriggerTime) {
        LocalTime time = Optional.ofNullable(timeParam)
                .map(timeString -> LocalTime.parse(timeString, TIME_FORMATTER))
                .orElseThrow(() -> new IllegalArgumentException("timeToChange not passed"));
        if (changeTriggerTime && StrUtil.isBlank(triggerName)) {
            throw new IllegalArgumentException("triggerName not passed");
        }

        triggerService.addOrModifyTrigger(triggerName, time, repeated, changeTriggerTime);

        WebContext.response().redirect("/?doNotCheckIsStreaming");
    }

    @POST("/removeTrigger")
    public void removeTrigger(@Form(name = "triggerName") String triggerName) {
        if (StrUtil.isBlank(triggerName)) {
            throw new IllegalArgumentException("triggerName not passed");
        }

        triggerService.removeTrigger(triggerName);

        WebContext.response().redirect("/?doNotCheckIsStreaming");
    }

}
