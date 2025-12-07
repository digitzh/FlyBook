package com.bytedance.controller;


import com.bytedance.common.Result;
import com.bytedance.dto.LoginRequest;
import com.bytedance.service.IUserService;
import com.bytedance.usecase.user.LoginUserUseCase;
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
        LoginUserUseCase.LoginResult loginResult = userService.login(request.getUsername(), request.getPassword());

        Map<String, Object> data = new HashMap<>();
        data.put("token", loginResult.getToken());
        data.put("userInfo", loginResult.getUser());

        return Result.success(data);
    }
}

