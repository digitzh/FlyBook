package com.bytedance.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bytedance.common.Result;
import com.bytedance.dto.LoginRequest;
import com.bytedance.entity.User;
import com.bytedance.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private IUserService userService;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginRequest request) {
        String token = userService.login(request.getUsername(), request.getPassword());

        // 返回 Token 和 用户信息
        User user = userService.getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername()));

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("userInfo", user);

        return Result.success(data);
    }
}

