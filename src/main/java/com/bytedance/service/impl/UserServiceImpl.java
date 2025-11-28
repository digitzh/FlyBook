package com.bytedance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bytedance.entity.User;
import com.bytedance.mapper.UserMapper;
import com.bytedance.service.IUserService;
import com.bytedance.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public String login(String username, String password) {
        // 1. 查用户
        User user = this.getOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username));

        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        // 2. 校验密码 (为了演示这里是明文对比，实际应该用 BCryptPasswordEncoder)
        if (!user.getPassword().equals(password)) {
            throw new RuntimeException("密码错误");
        }

        // 3. 生成 Token
        return jwtUtils.createToken(user.getUserId());
    }
}

