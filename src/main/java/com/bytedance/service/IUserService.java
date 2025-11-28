package com.bytedance.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bytedance.entity.User;

public interface IUserService extends IService<User> {
    String login(String username, String password);
}
