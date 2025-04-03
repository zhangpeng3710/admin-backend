package com.roc.admin.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.roc.admin.backend.constant.ResponseData;
import com.roc.admin.backend.dao.entity.RbacUser;
import com.roc.admin.backend.dao.mapper.RbacUserMapper;
import com.roc.admin.backend.dao.service.IRbacUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @Description
 * @Author: Zhang Peng
 * @Date: 2025/4/3
 */
@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private RbacUserMapper userMapper;

    @Resource
    private IRbacUserService userService;

    @GetMapping(value = "/user")
    public String getUserList() {
        return "user";
    }

    @PostMapping(value = "/addUser")
    public ResponseData<String> addUser(@RequestBody RbacUser user) {
        user.setCreateTime(new Date());
        user.setUpdateTime(new Date());
        userMapper.insert(user);
        return ResponseData.success();
    }

    @GetMapping(value = "/queryUser")
    public ResponseData<RbacUser> queryUser(@RequestBody RbacUser user) {
        LambdaQueryWrapper<RbacUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RbacUser::getUserEmail, user.getUserEmail());
        RbacUser userFromDb = userService.getOne(queryWrapper, true);
        return ResponseData.success(userFromDb);
    }
}
