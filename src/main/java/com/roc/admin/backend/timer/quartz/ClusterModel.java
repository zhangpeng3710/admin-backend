package com.roc.admin.backend.timer.quartz;

import com.roc.admin.backend.timer.quartz.listener.JobMonitoringListener;
import lombok.extern.slf4j.Slf4j;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Set;

/**
 * @Description Quartz cluster scheduler for distributed task scheduling
 * @Author: Zhang Peng
 * @Date: 2024/6/16
 */
@Slf4j
@Component
public class ClusterModel {

    // Add this field
    private final JobMonitoringListener jobMonitoringListener;

    @Autowired
    private Scheduler scheduler;

    public ClusterModel(JobMonitoringListener jobMonitoringListener) {
        this.jobMonitoringListener = jobMonitoringListener;
    }

    /**
     * Initializes the scheduler. This method is called after the bean has been constructed.
     * It ensures the scheduler is started if it's not already running.
     */
    @PostConstruct
    public void init() {
        try {
            // Check if the scheduler is already started by Spring Boot's auto-configuration
            if (scheduler != null && !scheduler.isStarted()) {
                log.info("Starting Quartz Scheduler as it was not started automatically...");
                scheduler.start();
                log.info("Quartz Scheduler started successfully.");
            } else if (scheduler == null) {
                log.error("Scheduler is null. Cannot start Quartz Scheduler.");
            } else {
                log.info("Quartz Scheduler is already running.");
            }
            // Optionally, schedule a default job here if needed
            // scheduleClusterJob(); 
        } catch (SchedulerException e) {
            log.error("Failed to start or initialize Quartz Scheduler", e);
            // Depending on the application's needs, you might want to re-throw or handle this
        }
    }

    /**
     * Shuts down the scheduler gracefully when the application is stopping.
     * This method is called before the bean is destroyed.
     */
    @PreDestroy
    public void destroy() {
        try {
            if (scheduler != null && scheduler.isStarted()) {
                log.info("Shutting down Quartz Scheduler...");
                // Pass true to wait for currently executing jobs to complete
                scheduler.shutdown(true);
                log.info("Quartz Scheduler shutdown complete.");
            }
        } catch (SchedulerException e) {
            log.error("Error while shutting down Quartz Scheduler", e);
        }
    }


    // Add this method
    public JobMonitoringListener.JobStats getJobStats(String jobName, String jobGroup) {
        return jobMonitoringListener.getJobStats(new JobKey(jobName, jobGroup));
    }

    /**
     * Schedules a distributed job that can be picked up by any node in the cluster.
     * The job identity is defined by its name and group.
     * If a job with the same identity already exists, it will be replaced (or deleted and re-added).
     *
     * @param jobClass       The class of the job to be executed.
     * @param jobName        The name of the job.
     * @param jobGroup       The group of the job.
     * @param triggerName    The name of the trigger.
     * @param triggerGroup   The group of the trigger.
     * @param cronExpression The cron expression for scheduling the job.
     * @param jobDataMap     Optional data to pass to the job.
     * @throws SchedulerException if there is an error scheduling the job.
     */
    public void scheduleJob(Class<? extends Job> jobClass, String jobName, String jobGroup,
                            String triggerName, String triggerGroup, String cronExpression,
                            JobDataMap jobDataMap) throws SchedulerException {
        try {
            JobKey jobKey = new JobKey(jobName, jobGroup);
            TriggerKey triggerKey = new TriggerKey(triggerName, triggerGroup);

            // Define the job and tie it to our DemoJob class
            JobBuilder jobBuilder = JobBuilder.newJob(jobClass)
                    .withIdentity(jobKey)
                    .requestRecovery(); // Request recovery if the job was executing during a server crash

            if (jobDataMap != null && !jobDataMap.isEmpty()) {
                jobBuilder.usingJobData(jobDataMap);
            }
            JobDetail jobDetail = jobBuilder.build();

            // Define the trigger for the job
            CronTrigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(triggerKey)
                    .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression)
                            .withMisfireHandlingInstructionDoNothing()) // Example misfire instruction
                    .build();

            // Check if the job already exists
            if (scheduler.checkExists(jobKey)) {
                log.info("Job {}.{} already exists. It will be replaced.", jobGroup, jobName);
                // Delete and then schedule (ensures fresh trigger association)
                scheduler.deleteJob(jobKey);
                scheduler.scheduleJob(jobDetail, trigger);
            } else {
                scheduler.scheduleJob(jobDetail, trigger);
            }

            log.info("Scheduled job {}.{} with trigger {}.{}. Next fire time: {}",
                    jobGroup, jobName, triggerGroup, triggerName, trigger.getNextFireTime());

        } catch (SchedulerException e) {
            log.error("Error scheduling job {}.{}: {}", jobGroup, jobName, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Example method to schedule a predefined DemoJob in cluster mode.
     */
    public void scheduleDefaultDemoJob() throws SchedulerException {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("jobParam", "Scheduled by ClusterModel");
        scheduleJob(DemoJob.class, "DemoJob", "ClusterJobs",
                "DemoTrigger", "ClusterTriggers",
                "0/20 * * * * ?", // Every 20 seconds
                jobDataMap);
    }

    /**
     * Lists all jobs and their triggers currently managed by the scheduler.
     * This is useful for diagnostics and understanding the current state of scheduled tasks.
     */
    public void listAllJobs() {
        log.info("Listing all scheduled jobs and their triggers:");
        try {
            for (String groupName : scheduler.getJobGroupNames()) {
                for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {
                    String jobName = jobKey.getName();
                    String jobGroup = jobKey.getGroup();
                    log.info("Job: {}.{} - Group: {}", jobName, jobGroup, jobKey.getGroup());

                    @SuppressWarnings("unchecked") // Quartz API uses raw types for triggers
                    Set<Trigger> triggers = (Set<Trigger>) scheduler.getTriggersOfJob(jobKey);
                    for (Trigger trigger : triggers) {
                        Trigger.TriggerState triggerState = scheduler.getTriggerState(trigger.getKey());
                        log.info("  Trigger: {} - Group: {} - State: {} - Next Fire Time: {} - Cron: {}",
                                trigger.getKey().getName(), trigger.getKey().getGroup(),
                                triggerState, trigger.getNextFireTime(),
                                (trigger instanceof CronTrigger) ? ((CronTrigger) trigger).getCronExpression() : "N/A");
                    }
                }
            }
        } catch (SchedulerException e) {
            log.error("Error listing jobs: {}", e.getMessage(), e);
        }
    }

    /**
     * Deletes a job and its associated triggers from the scheduler.
     *
     * @param jobName  The name of the job to delete.
     * @param jobGroup The group of the job to delete.
     * @return true if the job was found and deleted, false otherwise.
     * @throws SchedulerException if there is an error deleting the job.
     */
    public boolean deleteJob(String jobName, String jobGroup) throws SchedulerException {
        JobKey jobKey = new JobKey(jobName, jobGroup);
        try {
            if (scheduler.checkExists(jobKey)) {
                boolean deleted = scheduler.deleteJob(jobKey);
                if (deleted) {
                    log.info("Successfully deleted job: {}.{}", jobGroup, jobName);
                }
                return deleted;
            } else {
                log.warn("Job {}.{} not found for deletion.", jobGroup, jobName);
                return false;
            }
        } catch (SchedulerException e) {
            log.error("Error deleting job {}.{}: {}", jobGroup, jobName, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Pauses a specific job.
     *
     * @param jobName  The name of the job to pause.
     * @param jobGroup The group of the job to pause.
     * @throws SchedulerException if there is an error pausing the job.
     */
    public void pauseJob(String jobName, String jobGroup) throws SchedulerException {
        JobKey jobKey = new JobKey(jobName, jobGroup);
        try {
            if (scheduler.checkExists(jobKey)) {
                scheduler.pauseJob(jobKey);
                log.info("Paused job: {}.{}", jobGroup, jobName);
            } else {
                log.warn("Job {}.{} not found for pausing.", jobGroup, jobName);
            }
        } catch (SchedulerException e) {
            log.error("Error pausing job {}.{}: {}", jobGroup, jobName, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Resumes a paused job.
     *
     * @param jobName  The name of the job to resume.
     * @param jobGroup The group of the job to resume.
     * @throws SchedulerException if there is an error resuming the job.
     */
    public void resumeJob(String jobName, String jobGroup) throws SchedulerException {
        JobKey jobKey = new JobKey(jobName, jobGroup);
        try {
            if (scheduler.checkExists(jobKey)) {
                scheduler.resumeJob(jobKey);
                log.info("Resumed job: {}.{}", jobGroup, jobName);
            } else {
                log.warn("Job {}.{} not found for resuming.", jobGroup, jobName);
            }
        } catch (SchedulerException e) {
            log.error("Error resuming job {}.{}: {}", jobGroup, jobName, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Pauses all triggers in the scheduler, effectively pausing all job executions.
     *
     * @throws SchedulerException if there is an error pausing all triggers.
     */
    public void pauseAll() throws SchedulerException {
        try {
            scheduler.pauseAll();
            log.info("Paused all triggers in the scheduler.");
        } catch (SchedulerException e) {
            log.error("Error pausing all triggers: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Resumes all triggers in the scheduler, allowing all jobs to continue their schedules.
     *
     * @throws SchedulerException if there is an error resuming all triggers.
     */
    public void resumeAll() throws SchedulerException {
        try {
            scheduler.resumeAll();
            log.info("Resumed all triggers in the scheduler.");
        } catch (SchedulerException e) {
            log.error("Error resuming all triggers: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Checks the status of a specific trigger.
     *
     * @param triggerName  The name of the trigger.
     * @param triggerGroup The group of the trigger.
     * @return The state of the trigger.
     * @throws SchedulerException if there is an error getting the trigger state.
     */
    public Trigger.TriggerState getTriggerState(String triggerName, String triggerGroup) throws SchedulerException {
        TriggerKey triggerKey = new TriggerKey(triggerName, triggerGroup);
        try {
            return scheduler.getTriggerState(triggerKey);
        } catch (SchedulerException e) {
            log.error("Error getting state for trigger {}.{}: {}", triggerGroup, triggerName, e.getMessage(), e);
            throw e;
        }
    }
}
