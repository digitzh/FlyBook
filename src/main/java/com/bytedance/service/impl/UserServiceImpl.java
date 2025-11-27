package com.bytedance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bytedance.entity.User;
import com.bytedance.mapper.UserMapper;
import com.bytedance.service.IUserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Override
    public User login(String username) {
        // 简单模拟：如果用户存在就返回，不存在就注册
        User user = this.getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
//        User user = query().eq("username", username).one();
        if (user == null) {
            user = User.builder()
                    .username(username)
                    .avatarUrl("")
                    .build();
            save(user);
        }
        return user;
    }
}

