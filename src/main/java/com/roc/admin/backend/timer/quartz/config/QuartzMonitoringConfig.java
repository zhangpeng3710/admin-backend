package com.roc.admin.backend.timer.quartz.config;

import com.roc.admin.backend.timer.quartz.listener.JobMonitoringListener;
import com.roc.admin.backend.timer.quartz.listener.TriggerMonitoringListener;
import lombok.RequiredArgsConstructor;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
@RequiredArgsConstructor
public class QuartzMonitoringConfig {

    private final Scheduler scheduler;
    private final JobMonitoringListener jobMonitoringListener;
    private final TriggerMonitoringListener triggerMonitoringListener;

    @PostConstruct
    public void init() throws SchedulerException {
        // Register global listeners
        scheduler.getListenerManager().addJobListener(jobMonitoringListener);
        scheduler.getListenerManager().addTriggerListener(triggerMonitoringListener);
    }
}