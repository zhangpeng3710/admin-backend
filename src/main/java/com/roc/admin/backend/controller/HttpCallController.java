package com.roc.admin.backend.controller;

import com.roc.admin.backend.constant.ResponseData;
import com.roc.admin.backend.dao.entity.RbacUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;

@Slf4j
@RestController
@RequestMapping("/http")
public class HttpCallController {

    @Resource
    private RestTemplate restTemplate;

    @GetMapping(value = "/restTemplate")
    public ResponseData<Object> test1(@RequestBody RbacUser user) {
        String fooResourceUrl = "http://localhost:9001/tomin/test/user";
        ResponseData response = restTemplate.getForObject(fooResourceUrl, ResponseData.class);
        return response;

    }
}
