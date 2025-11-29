package com.bytedance.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bytedance.dto.RegisterRequest;
import com.bytedance.entity.User;
import com.bytedance.mapper.UserMapper;
import com.bytedance.service.IUserService;
import com.bytedance.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserMapper userMapper;

    @Override
    public void register(RegisterRequest request) {
        // 1. 校验用户名是否已存在
        User existUser = this.getOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.getUsername()));

        if (existUser != null) {
            throw new RuntimeException("用户名已存在");
        }

        // 2. 密码加密 (使用 BCrypt，这是目前最推荐的加密方式)
        // 需要在 pom.xml 引入 hutool-all，或者使用 Spring Security 的 BCryptPasswordEncoder
        // 这里演示使用 Hutool
        String encryptedPwd = BCrypt.hashpw(request.getPassword());

        // 3. 构建新用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(encryptedPwd);

        // 设置默认头像
        user.setAvatarUrl("https://lf-flow-web-cdn.doubao.com/obj/flow-doubao/doubao/chat/static/image/logo-icon-white-bg.72df0b1a.png");

        user.setCreatedTime(LocalDateTime.now());

        // 4. 保存
        this.save(user);
    }


    @Override
    public String login(String username, String password) {
        // 1. 查用户
        User user = this.getOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username));

        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 2. 校验密码
        // 如果用户没有设置密码（测试用户），允许空密码登录
        String userPassword = user.getPassword();
        if (StrUtil.isBlank(userPassword)) {
            // 测试用户没有密码，允许空密码或任意密码登录
            if (StrUtil.isNotBlank(password)) {
                // 如果前端传了密码但用户没有密码，需要密码为空才能登录
                // 这里为了测试方便，允许任意密码
            }
        } else {
            // 用户有密码，需要验证
            // 为了演示这里是明文对比，实际应该用 BCryptPasswordEncoder
            if (!userPassword.equals(password)) {
                throw new RuntimeException("密码错误");
            }
        }

        // 3. 生成 Token
        return jwtUtils.createToken(user.getUserId());
    }
}

