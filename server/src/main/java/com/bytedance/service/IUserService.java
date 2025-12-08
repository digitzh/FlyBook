package com.bytedance.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bytedance.dto.RegisterRequest;
import com.bytedance.entity.User;
import com.bytedance.usecase.user.LoginUserUseCase;

public interface IUserService extends IService<User> {
    void register(RegisterRequest request);
    LoginUserUseCase.LoginResult login(String username, String password);
}
