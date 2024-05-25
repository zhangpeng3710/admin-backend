package com.roc.admin.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.roc.admin.backend.constant.ResponseCode;
import com.roc.admin.backend.constant.ResponseData;
import com.roc.admin.backend.dao.entity.RbacUser;
import com.roc.admin.backend.dao.service.IRbacUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

/**
 * @Description
 * @Author: Zhang Peng
 * @Date: 2024/5/12
 */
@Slf4j
@RestController
@RequestMapping("/login")
public class LoginController {

    @Autowired
    private IRbacUserService userService;

    @PostMapping(value = "/sessionDemo")
    public ResponseData<Object> login(HttpServletRequest request, @RequestBody RbacUser user) {
        LambdaQueryWrapper<RbacUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RbacUser::getUserEmail, user.getUserEmail());
        RbacUser userFromDb = userService.getOne(queryWrapper, true);

        if (userFromDb != null && userFromDb.getUserPasswd().equals(user.getUserPasswd())) {
            request.getSession().setAttribute("user", user);
            return ResponseData.success();
        } else {
            return ResponseData.fail(ResponseCode.FAIL);
        }
    }


    @PostMapping(value = "/test")
    public String test() {
        return "test";
    }
    
}