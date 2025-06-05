package com.roc.admin.backend.timer.quartz;

import lombok.extern.slf4j.Slf4j;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerBuilder;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashSet;

/**
 * @Description
 * @Author: Zhang Peng
 * @Date: 2024/6/16
 */
@Slf4j
@Component
public class ClusterScheduler {

    @Resource
    private Scheduler scheduler;

    /**
     * 创建任务调度或者集群执行任务调度
     */
    public void run() throws SchedulerException, InterruptedException {
        cleanScheduler();
        addOneJobOneTrigger();
//        addOneJobTwoTrigger();

    }

    public void cleanScheduler() throws SchedulerException {
        log.info("clear any existing job scheduler before creating new one.");
        scheduler.clear();
    }

    public void addOneJobOneTrigger() throws SchedulerException {
        log.info("add one job triggered by one trigger");
        JobDetail job = JobBuilder.newJob(DemoJob.class)
                .withIdentity("MyJob", "OneJob-OneTrigger")
                .requestRecovery()
                .build();

        CronTrigger trigger1 = TriggerBuilder.newTrigger()
                .withIdentity("MyTrigger", "OneJob-OneTrigger")
                .startNow()
                .withSchedule(CronScheduleBuilder.cronSchedule("0,10,20,30,40,50 * * * * ? "))
                .build();

        scheduler.scheduleJob(job, trigger1);
    }


    public void addOneJobTwoTrigger() throws SchedulerException {
        log.info("add one job triggered by two trigger");
        JobDetail job = JobBuilder.newJob(DemoJob.class)
                .withIdentity("MyJob", "OneJob-TwoTrigger")
                .requestRecovery()
                .build();

        CronTrigger trigger1 = TriggerBuilder.newTrigger()
                .withIdentity("MyTrigger1", "OneJob-TwoTrigger")
                .startNow()
                .withSchedule(CronScheduleBuilder.cronSchedule("0,10,20,30,40,50 * * * * ? "))
                .build();
        CronTrigger trigger2 = TriggerBuilder.newTrigger()
                .withIdentity("MyTrigger2", "OneJob-TwoTrigger")
                .startNow()
                .withSchedule(CronScheduleBuilder.cronSchedule("0,20,40 * * * * ? "))
                .build();

        HashSet<CronTrigger> triggerHash = new HashSet<>();
        triggerHash.add(trigger1);
        triggerHash.add(trigger2);
        scheduler.scheduleJob(job, triggerHash, false);
    }
}
