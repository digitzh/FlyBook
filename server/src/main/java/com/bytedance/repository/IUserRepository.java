package com.bytedance.repository;

import com.bytedance.entity.User;

import java.util.List;

/**
 * 用户数据访问接口
 * 抽象数据访问层，便于未来替换数据源
 */
public interface IUserRepository {
    /**
     * 根据ID查询用户
     */
    User findById(Long userId);

    /**
     * 根据用户名查询用户
     */
    User findByUsername(String username);

    /**
     * 保存用户
     */
    void save(User user);

    /**
     * 检查用户名是否存在
     */
    boolean existsByUsername(String username);

    /**
     * 根据ID列表批量查询用户
     */
    List<User> findByIds(List<Long> userIds);
}
