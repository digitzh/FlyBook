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
        // 这里演示使用 Hutool:
        String encryptedPwd = BCrypt.hashpw(request.getPassword());

        // 3. 构建新用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(encryptedPwd);



        // 设置默认头像 (飞书风格往往是随机颜色背景+名字首字母，这里我们先用一个固定图或随机图)
        // 建议你去网上找几个好看的头像 URL 填在这里
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
        // 2. 校验密码 (为了演示这里是明文对比，实际应该用 BCryptPasswordEncoder)
        if (!user.getPassword().equals(password)) {
            throw new RuntimeException("密码错误");
        }

        // 3. 生成 Token
        return jwtUtils.createToken(user.getUserId());
    }
}

