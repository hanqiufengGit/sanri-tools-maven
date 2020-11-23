package com.sanri.tools.modules.quartz.dtos;

import lombok.Data;
import org.quartz.JobKey;
import org.quartz.TriggerKey;

@Data
public class TriggerTask {
    private TriggerKey triggerKey;
    private JobKey jobKey;
    private Long startTime;
    private Long prevFireTime;
    private Long nextFireTime;

    public TriggerTask() {
    }

    public TriggerTask(TriggerKey triggerKey, JobKey jobKey, Long startTime, Long prevFireTime, Long nextFireTime) {
        this.triggerKey = triggerKey;
        this.jobKey = jobKey;
        this.startTime = startTime;
        this.prevFireTime = prevFireTime;
        this.nextFireTime = nextFireTime;
    }
}
