package com.bytedance.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bytedance.dto.RegisterRequest;
import com.bytedance.entity.User;

public interface IUserService extends IService<User> {
    void register(RegisterRequest request);
    String login(String username, String password);
}
