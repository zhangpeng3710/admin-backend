package com.roc.admin.backend.timer.quartz.listener;

import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.Trigger;
import org.quartz.Trigger.CompletedExecutionInstruction;
import org.quartz.TriggerKey;
import org.quartz.TriggerListener;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
public class TriggerMonitoringListener implements TriggerListener {
    private final Map<String, TriggerStats> triggerStatsMap = new ConcurrentHashMap<>();
    private final Map<String, Date> activeTriggerStartTimes = new ConcurrentHashMap<>();

    private static class TriggerStats {
        private final TriggerKey triggerKey;
        private final AtomicInteger firedCount = new AtomicInteger(0);
        private final AtomicInteger misfiredCount = new AtomicInteger(0);
        private final AtomicInteger completedCount = new AtomicInteger(0);
        private final AtomicLong totalExecutionTime = new AtomicLong(0);

        public TriggerStats(TriggerKey triggerKey) {
            this.triggerKey = triggerKey;
        }

        public void incrementFiredCount() {
            firedCount.incrementAndGet();
        }

        public void incrementMisfiredCount() {
            misfiredCount.incrementAndGet();
        }

        public void incrementCompletedCount(long executionTime) {
            if (executionTime >= 0) {
                completedCount.incrementAndGet();
                totalExecutionTime.addAndGet(executionTime);
            }
        }

        public TriggerKey getTriggerKey() {
            return triggerKey;
        }

        public int getFiredCount() {
            return firedCount.get();
        }

        public int getMisfiredCount() {
            return misfiredCount.get();
        }

        public int getCompletedCount() {
            return completedCount.get();
        }

        public long getTotalExecutionTime() {
            return totalExecutionTime.get();
        }

        public long getAverageExecutionTime() {
            int completions = completedCount.get();
            return completions > 0 ? totalExecutionTime.get() / completions : 0;
        }
    }

    @Override
    public String getName() {
        return "GlobalTriggerMonitor";
    }

    @Override
    public void triggerFired(Trigger trigger, JobExecutionContext context) {
        String triggerId = getTriggerId(trigger);
        TriggerStats stats = triggerStatsMap.computeIfAbsent(triggerId, k -> new TriggerStats(trigger.getKey()));
        stats.incrementFiredCount();
        activeTriggerStartTimes.put(triggerId, new Date());

        log.info("Trigger {}.{} fired (Total Fired: {}). Job {}.{} scheduled. Fire time: {}",
                trigger.getKey().getGroup(), trigger.getKey().getName(), stats.getFiredCount(),
                trigger.getJobKey().getGroup(), trigger.getJobKey().getName(),
                context.getFireTime());
    }

    @Override
    public boolean vetoJobExecution(Trigger trigger, JobExecutionContext context) {
        return false;
    }

    @Override
    public void triggerMisfired(Trigger trigger) {
        String triggerId = getTriggerId(trigger);
        TriggerStats stats = triggerStatsMap.computeIfAbsent(triggerId, k -> new TriggerStats(trigger.getKey()));
        stats.incrementMisfiredCount();

        log.warn("Trigger {}.{} misfired (Total Misfired: {}). Scheduled fire time was: {}",
                trigger.getKey().getGroup(), trigger.getKey().getName(), stats.getMisfiredCount(),
                trigger.getNextFireTime());
    }

    @Override
    public void triggerComplete(Trigger trigger, JobExecutionContext context,
                                CompletedExecutionInstruction triggerInstructionCode) {
        String triggerId = getTriggerId(trigger);
        Date startTime = activeTriggerStartTimes.remove(triggerId);
        long executionTime = -1;
        if (startTime != null) {
            executionTime = System.currentTimeMillis() - startTime.getTime();
        }

        TriggerStats stats = triggerStatsMap.get(triggerId);
        if (stats != null) {
            stats.incrementCompletedCount(executionTime);
            log.info("Trigger {}.{} completed. Job {}.{} execution finished. Duration: {}ms. Instruction: {}. (Total Completed: {}, Avg Time: {}ms)",
                    trigger.getKey().getGroup(), trigger.getKey().getName(),
                    context.getJobDetail().getKey().getGroup(), context.getJobDetail().getKey().getName(),
                    executionTime, triggerInstructionCode,
                    stats.getCompletedCount(), stats.getAverageExecutionTime());
        } else {
            log.info("Trigger {}.{} completed. Job {}.{} execution finished. Duration: {}ms (Stats not found). Instruction: {}",
                    trigger.getKey().getGroup(), trigger.getKey().getName(),
                    context.getJobDetail().getKey().getGroup(), context.getJobDetail().getKey().getName(),
                    executionTime, triggerInstructionCode);
        }
    }

    private String getTriggerId(Trigger trigger) {
        return trigger.getKey().getGroup() + "_" + trigger.getKey().getName();
    }

    public TriggerStats getTriggerStats(TriggerKey triggerKey) {
        if (triggerKey == null) return null;
        return triggerStatsMap.get(triggerKey.getGroup() + "_" + triggerKey.getName());
    }
}