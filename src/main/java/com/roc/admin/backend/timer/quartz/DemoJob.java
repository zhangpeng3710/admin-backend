package com.roc.admin.backend.timer.quartz;

import lombok.extern.slf4j.Slf4j;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

/**
 * @Description
 * @Author: Zhang Peng
 * @Date: 2024/6/16
 */
@Slf4j
public class DemoJob extends QuartzJobBean {

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        JobDataMap jobDataMap = context.getMergedJobDataMap();
        String jobParam = jobDataMap.getString("jobParam");

        log.info("DemoJob started with param: {}", jobParam);

        try {
            // Simulate work
            Thread.sleep(2000);

            // job logic
            log.info("DemoJob completed successfully");

        } catch (InterruptedException e) {
            log.error("Job was interrupted", e);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("Error in DemoJob", e);
            // Consider implementing retry logic or error handling
            throw new JobExecutionException(e);
        }
    }
}

