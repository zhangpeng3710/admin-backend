package com.roc.admin.backend.controller;

import com.roc.admin.backend.constant.ResponseData;
import com.roc.admin.backend.timer.quartz.ClusterScheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description
 * @Author: Zhang Peng
 * @Date: 2024/6/12
 */
@RestController
@RequestMapping("/timer/test")
public class TimerController {

    @Autowired
    private ClusterScheduler scheduler;

    @PostMapping("/quartz/cluster")
    public ResponseData<Object> quartzCluster() throws SchedulerException, InterruptedException {
        scheduler.run();
        return ResponseData.success();
    }

}

