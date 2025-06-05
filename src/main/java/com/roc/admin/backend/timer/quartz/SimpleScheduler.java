package com.roc.admin.backend.timer.quartz;

import lombok.extern.slf4j.Slf4j;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

import java.util.Date;

/**
 * @Description
 * @Author: Zhang Peng
 * @Date: 2024/6/16
 */
@Slf4j
public class SimpleScheduler {


    public void run() throws SchedulerException, InterruptedException {
        JobDetail job = JobBuilder.newJob(DemoJob.class)
                .withIdentity("MyJob", "MyGroup")
                .build();

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("MyTrigger", "MyGroup")
                .startAt(new Date())
                .withSchedule(
                        SimpleScheduleBuilder
                                .simpleSchedule()
                                .withIntervalInSeconds(10)
                                .withRepeatCount(10)
                )
                .build();

        Scheduler scheduler = new StdSchedulerFactory().getScheduler();
        scheduler.scheduleJob(job, trigger);
        scheduler.start();

        log.info("sleeping for scheduler to run job.");
        Thread.sleep(60 * 1000L);

        log.info("shutdown the scheduler gracefully.");
        scheduler.shutdown(true);
    }

}