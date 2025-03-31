package com.roc.admin.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.roc.admin.backend.constant.ResponseData;
import com.roc.admin.backend.dao.entity.RbacUser;
import com.roc.admin.backend.dao.service.IRbacUserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/test")
public class TestController {
    @Resource
    private ObjectMapper mapper;

    @Resource
    private IRbacUserService userService;

    @GetMapping("/t1")
    public String test(@RequestBody String hh) throws JsonProcessingException {
        String path = System.getProperty("Dtest");
        System.out.println(path);
        String path2 = System.getProperty("Dtest2");
        System.out.println(path2);
        Map<String, String> map = new HashMap<>(2);
        map.put("code", "0");
        map.put("msg", "success");
        return mapper.writeValueAsString(map);

    }

    @GetMapping("/t2")
    public String test(
            @RequestParam String h1,
            @RequestBody String h2
    ) throws JsonProcessingException {

        Map<String, String> map = new HashMap<>(2);
        map.put("code", "0");
        map.put("msg", "success");
        return mapper.writeValueAsString(map);

    }

    @GetMapping(value = "/user")
    public ResponseData<RbacUser> getUserList() {

        LambdaQueryWrapper<RbacUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RbacUser::getUserEmail, "app1_admin@126.com");
        RbacUser userFromDb = userService.getOne(queryWrapper, true);

        return ResponseData.success(userFromDb);
    }
}
