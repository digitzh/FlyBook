package com.bytedance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bytedance.dto.RegisterRequest;
import com.bytedance.entity.User;
import com.bytedance.mapper.UserMapper;
import com.bytedance.service.IUserService;
import com.bytedance.usecase.user.LoginUserUseCase;
import com.bytedance.usecase.user.RegisterUserUseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 用户服务实现
 * 作为门面层，协调 UseCase
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    private final RegisterUserUseCase registerUserUseCase;
    private final LoginUserUseCase loginUserUseCase;

    @Autowired
    public UserServiceImpl(RegisterUserUseCase registerUserUseCase,
                          LoginUserUseCase loginUserUseCase) {
        this.registerUserUseCase = registerUserUseCase;
        this.loginUserUseCase = loginUserUseCase;
    }

    @Override
    public void register(RegisterRequest request) {
        registerUserUseCase.execute(request);
    }

    @Override
    public LoginUserUseCase.LoginResult login(String username, String password) {
        return loginUserUseCase.execute(username, password);
    }
}

