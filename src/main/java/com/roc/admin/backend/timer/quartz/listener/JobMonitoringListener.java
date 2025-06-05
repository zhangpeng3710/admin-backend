package com.roc.admin.backend.timer.quartz.listener;

import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.JobListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
public class JobMonitoringListener implements JobListener {
    private final ConcurrentHashMap<JobKey, JobStats> jobStatsMap = new ConcurrentHashMap<>();

    // Global statistics for all jobs
    private final AtomicInteger globalJobsStartedCount = new AtomicInteger(0);
    private final AtomicInteger globalJobsSucceededCount = new AtomicInteger(0);
    private final AtomicInteger globalJobsFailedCount = new AtomicInteger(0);
    private final AtomicLong globalTotalExecutionTime = new AtomicLong(0);

    @Override
    public String getName() {
        return "GlobalJobMonitor";
    }

    @Override
    public void jobToBeExecuted(JobExecutionContext context) {
        JobKey jobKey = context.getJobDetail().getKey();
        JobStats stats = jobStatsMap.computeIfAbsent(jobKey, k -> new JobStats());
        stats.jobStarted();
        globalJobsStartedCount.incrementAndGet();

        log.info("Job {}.{} is starting [This Job Run #{}]",
                jobKey.getGroup(), jobKey.getName(),
                stats.getRunCount());
    }

    @Override
    public void jobExecutionVetoed(JobExecutionContext context) {
        JobKey jobKey = context.getJobDetail().getKey();
        log.warn("Job {}.{} execution was vetoed. [This Job Run #{}]",
                jobKey.getGroup(), jobKey.getName(),
                jobStatsMap.getOrDefault(jobKey, new JobStats()).getRunCount());
    }

    @Override
    public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
        JobKey jobKey = context.getJobDetail().getKey();
        JobStats stats = jobStatsMap.computeIfAbsent(jobKey, k -> {
            log.warn("JobStats not found for job {}.{} in jobWasExecuted, creating new. This might indicate an issue if jobToBeExecuted was not called.", jobKey.getGroup(), jobKey.getName());
            return new JobStats();
        });
        long runtime = context.getJobRunTime();

        if (jobException != null) {
            stats.jobFailed();
            globalJobsFailedCount.incrementAndGet();
            log.error("Job {}.{} failed after {}ms [This Job Fails: {}]. Error: {}",
                    jobKey.getGroup(), jobKey.getName(),
                    runtime, stats.getFailedCount(),
                    jobException.getMessage(), jobException);
        } else {
            stats.jobCompleted(runtime);
            globalJobsSucceededCount.incrementAndGet();
            globalTotalExecutionTime.addAndGet(runtime);
            log.info("Job {}.{} completed successfully in {}ms [This Job Successes: {}, This Job Failures: {}, This Job AvgTime: {}ms]",
                    jobKey.getGroup(), jobKey.getName(),
                    runtime,
                    stats.getSuccessCount(),
                    stats.getFailedCount(),
                    stats.getAverageExecutionTime());
        }
    }

    public JobStats getJobStats(JobKey jobKey) {
        return jobStatsMap.get(jobKey);
    }

    public int getGlobalJobsStartedCount() {
        return globalJobsStartedCount.get();
    }

    public int getGlobalJobsSucceededCount() {
        return globalJobsSucceededCount.get();
    }

    public int getGlobalJobsFailedCount() {
        return globalJobsFailedCount.get();
    }

    public long getGlobalTotalExecutionTime() {
        return globalTotalExecutionTime.get();
    }

    public long getGlobalAverageExecutionTime() {
        int succeeded = globalJobsSucceededCount.get();
        return succeeded > 0 ? globalTotalExecutionTime.get() / succeeded : 0;
    }

    public static class JobStats {
        private final AtomicInteger runCount = new AtomicInteger(0);
        private final AtomicInteger successCount = new AtomicInteger(0);
        private final AtomicInteger failedCount = new AtomicInteger(0);
        private final AtomicLong totalExecutionTime = new AtomicLong(0);

        public void jobStarted() {
            runCount.incrementAndGet();
        }

        public void jobCompleted(long executionTime) {
            successCount.incrementAndGet();
            totalExecutionTime.addAndGet(executionTime);
        }

        public void jobFailed() {
            failedCount.incrementAndGet();
        }

        public int getRunCount() {
            return runCount.get();
        }

        public int getSuccessCount() {
            return successCount.get();
        }

        public int getFailedCount() {
            return failedCount.get();
        }

        public long getTotalExecutionTime() {
            return totalExecutionTime.get();
        }

        public long getAverageExecutionTime() {
            int completions = successCount.get();
            return completions > 0 ? totalExecutionTime.get() / completions : 0;
        }
    }
}