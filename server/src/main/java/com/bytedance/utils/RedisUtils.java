package com.bytedance.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis工具类
 * 提供常用的Redis操作方法
 */
@Component
public class RedisUtils {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    // ==================== String操作 ====================

    /**
     * 写入缓存，并设置过期时间
     * @param key 键
     * @param value 值 (通常是 JSON 字符串)
     * @param timeout 过期时间 (秒)
     */
    public void set(String key, String value, long timeout) {
        stringRedisTemplate.opsForValue().set(key, value, timeout, TimeUnit.SECONDS);
    }

    /**
     * 写入缓存（不过期）
     */
    public void set(String key, String value) {
        stringRedisTemplate.opsForValue().set(key, value);
    }

    /**
     * 读取缓存
     */
    public String get(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    /**
     * 删除缓存
     */
    public void delete(String key) {
        stringRedisTemplate.delete(key);
    }

    /**
     * 批量删除
     */
    public void delete(Set<String> keys) {
        stringRedisTemplate.delete(keys);
    }

    /**
     * 判断 key 是否存在
     */
    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(key));
    }

    /**
     * 设置过期时间
     */
    public boolean expire(String key, long timeout) {
        return Boolean.TRUE.equals(stringRedisTemplate.expire(key, timeout, TimeUnit.SECONDS));
    }

    /**
     * 获取过期时间（秒）
     */
    public Long getExpire(String key) {
        return stringRedisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    /**
     * 递增
     */
    public Long increment(String key) {
        return stringRedisTemplate.opsForValue().increment(key);
    }

    /**
     * 递增指定值
     */
    public Long increment(String key, long delta) {
        return stringRedisTemplate.opsForValue().increment(key, delta);
    }

    /**
     * 递减
     */
    public Long decrement(String key) {
        return stringRedisTemplate.opsForValue().decrement(key);
    }

    // ==================== Hash操作 ====================

    /**
     * Hash设置
     */
    public void hSet(String key, String hashKey, String value) {
        stringRedisTemplate.opsForHash().put(key, hashKey, value);
    }

    /**
     * Hash获取
     */
    public Object hGet(String key, String hashKey) {
        return stringRedisTemplate.opsForHash().get(key, hashKey);
    }

    /**
     * Hash删除
     */
    public void hDelete(String key, String... hashKeys) {
        stringRedisTemplate.opsForHash().delete(key, (Object[]) hashKeys);
    }

    /**
     * Hash是否存在
     */
    public boolean hHasKey(String key, String hashKey) {
        return Boolean.TRUE.equals(stringRedisTemplate.opsForHash().hasKey(key, hashKey));
    }

    /**
     * Hash大小
     */
    public Long hSize(String key) {
        return stringRedisTemplate.opsForHash().size(key);
    }

    // ==================== List操作 ====================

    /**
     * List左推入
     */
    public Long lPush(String key, String value) {
        return stringRedisTemplate.opsForList().leftPush(key, value);
    }

    /**
     * List右推入
     */
    public Long rPush(String key, String value) {
        return stringRedisTemplate.opsForList().rightPush(key, value);
    }

    /**
     * List左弹出
     */
    public String lPop(String key) {
        return stringRedisTemplate.opsForList().leftPop(key);
    }

    /**
     * List右弹出
     */
    public String rPop(String key) {
        return stringRedisTemplate.opsForList().rightPop(key);
    }

    /**
     * List范围获取
     */
    public List<String> lRange(String key, long start, long end) {
        return stringRedisTemplate.opsForList().range(key, start, end);
    }

    /**
     * List长度
     */
    public Long lSize(String key) {
        return stringRedisTemplate.opsForList().size(key);
    }

    // ==================== Set操作 ====================

    /**
     * Set添加
     */
    public Long sAdd(String key, String... values) {
        return stringRedisTemplate.opsForSet().add(key, values);
    }

    /**
     * Set移除
     */
    public Long sRemove(String key, String... values) {
        return stringRedisTemplate.opsForSet().remove(key, (Object[]) values);
    }

    /**
     * Set是否包含
     */
    public boolean sIsMember(String key, String value) {
        return Boolean.TRUE.equals(stringRedisTemplate.opsForSet().isMember(key, value));
    }

    /**
     * Set所有成员
     */
    public Set<String> sMembers(String key) {
        return stringRedisTemplate.opsForSet().members(key);
    }

    /**
     * Set大小
     */
    public Long sSize(String key) {
        return stringRedisTemplate.opsForSet().size(key);
    }

    // ==================== 分布式锁 ====================

    /**
     * 尝试获取分布式锁
     * @param key 锁的key
     * @param value 锁的值（通常使用UUID，用于释放锁时验证）
     * @param timeout 锁的过期时间（秒）
     * @return 是否获取成功
     */
    public boolean tryLock(String key, String value, long timeout) {
        Boolean result = stringRedisTemplate.opsForValue().setIfAbsent(key, value, timeout, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(result);
    }

    /**
     * 释放分布式锁
     * @param key 锁的key
     * @param value 锁的值（必须与获取锁时的值一致）
     * @return 是否释放成功
     */
    public boolean releaseLock(String key, String value) {
        // 使用Lua脚本确保原子性：只有value匹配时才删除
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        org.springframework.data.redis.core.script.DefaultRedisScript<Long> redisScript = 
            new org.springframework.data.redis.core.script.DefaultRedisScript<>();
        redisScript.setScriptText(script);
        redisScript.setResultType(Long.class);
        
        Long result = stringRedisTemplate.execute(
            redisScript,
            java.util.Collections.singletonList(key),
            value
        );
        return result != null && result > 0;
    }
}
