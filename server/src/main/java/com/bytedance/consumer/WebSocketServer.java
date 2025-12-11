package com.bytedance.consumer;

import com.bytedance.config.SpringWebSocketConfigurator;
import com.bytedance.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.net.InetAddress;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket 服务端
 * 使用Redis管理在线状态，支持多实例部署
 */
@ServerEndpoint(value = "/ws/{userId}", configurator = SpringWebSocketConfigurator.class)
@Component
@Slf4j
public class WebSocketServer {

    // 本地Session映射（用于当前实例的WebSocket连接）
    private static final ConcurrentHashMap<Long, Session> ONLINE_SESSION_MAP = new ConcurrentHashMap<>();

    // Redis工具类（通过静态方式获取，因为@ServerEndpoint不能直接注入）
    private static RedisUtils redisUtils;

    // 当前实例ID（用于标识不同的服务实例）
    private static String instanceId;

    // 定义心跳超时时间 (毫秒)，比如 60秒
    // 如果 60秒内没有收到任何数据(包括 ping)，连接自动断开
    private static final long MAX_IDLE_TIMEOUT = 60 * 1000L;

    // Redis key前缀
    private static final String REDIS_KEY_ONLINE = "ws:online:"; // ws:online:{userId}
    private static final String REDIS_KEY_INSTANCE = "ws:instance:"; // ws:instance:{instanceId}
    private static final long ONLINE_TIMEOUT = 300; // 在线状态过期时间（秒），5分钟

    @Autowired
    public void setRedisUtils(RedisUtils redisUtils) {
        WebSocketServer.redisUtils = redisUtils;
    }

    @PostConstruct
    public void init() {
        // 生成实例ID：IP:PORT:UUID
        try {
            String host = InetAddress.getLocalHost().getHostAddress();
            instanceId = host + ":" + System.getProperty("server.port", "8081") + ":" + UUID.randomUUID().toString().substring(0, 8);
            log.info("WebSocket Server 实例ID: {}", instanceId);
        } catch (Exception e) {
            instanceId = UUID.randomUUID().toString();
            log.warn("无法获取主机IP，使用UUID作为实例ID: {}", instanceId);
        }
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("userId") Long userId) {
        try {
            // 设置最大空闲时间
            session.setMaxIdleTimeout(MAX_IDLE_TIMEOUT);

            // 存储到本地Session映射
            ONLINE_SESSION_MAP.put(userId, session);

            // 【新增】存储到Redis，标记用户在线
            if (redisUtils != null) {
                // 存储用户在线状态：ws:online:{userId} -> instanceId
                redisUtils.set(REDIS_KEY_ONLINE + userId, instanceId, ONLINE_TIMEOUT);
                // 存储实例下的用户：ws:instance:{instanceId} -> Set<userId>
                redisUtils.sAdd(REDIS_KEY_INSTANCE + instanceId, userId.toString());

                // 【新增】拉取并推送离线消息
                pushOfflineMessages(userId, session);
            }

            log.info("用户 connected: {}, 当前实例在线: {}, 实例ID: {}", userId, ONLINE_SESSION_MAP.size(), instanceId);
        } catch (Exception e) {
            log.error("连接异常", e);
        }
    }

    /**
     * 推送离线消息
     */
    private void pushOfflineMessages(Long userId, Session session) {
        if (redisUtils == null) {
            return;
        }

        String offlineKey = "ws:offline:" + userId;
        Long offlineCount = redisUtils.lSize(offlineKey);

        if (offlineCount != null && offlineCount > 0) {
            log.info("用户 {} 有 {} 条离线消息，开始推送", userId, offlineCount);
            
            // 批量拉取离线消息（每次最多100条）
            List<String> messages = redisUtils.lRange(offlineKey, 0, Math.min(99, offlineCount - 1));
            
            if (messages != null && !messages.isEmpty()) {
                int successCount = 0;
                for (String msg : messages) {
                    try {
                        synchronized (session) {
                            if (session.isOpen()) {
                                session.getBasicRemote().sendText(msg);
                                successCount++;
                            } else {
                                break;
                            }
                        }
                    } catch (IOException e) {
                        log.error("推送离线消息失败: userId={}", userId, e);
                        break;
                    }
                }

                // 删除已推送的消息
                for (int i = 0; i < successCount; i++) {
                    redisUtils.lPop(offlineKey);
                }

                log.info("用户 {} 离线消息推送完成: {}/{}", userId, successCount, messages.size());
            }
        }
    }

    @OnClose
    public void onClose(@PathParam("userId") Long userId, CloseReason reason) {
        // 打印关闭原因，方便排查是 超时关闭 还是 正常关闭
        // CloseCode: 1000=正常, 1001=离开, 1006=异常断开
        log.info("用户 disconnected: {}, 原因: {}", userId, reason.getCloseCode());

        // 移除本地Session
        ONLINE_SESSION_MAP.remove(userId);

        // 【新增】从Redis移除在线状态
        if (redisUtils != null) {
            redisUtils.delete(REDIS_KEY_ONLINE + userId);
            redisUtils.sRemove(REDIS_KEY_INSTANCE + instanceId, userId.toString());
        }
    }

    @OnMessage
    public void onMessage(String message, Session session, @PathParam("userId") Long userId) {
        // 只要收到消息（无论是业务消息还是心跳），IdleTimeout 计时器都会自动重置
        if ("ping".equals(message)) {
            try {
                // 收到 ping，回复 pong
                session.getBasicRemote().sendText("pong");
                
                // 【新增】更新Redis中的在线状态过期时间（心跳保活）
                if (redisUtils != null && userId != null) {
                    redisUtils.set(REDIS_KEY_ONLINE + userId, instanceId, ONLINE_TIMEOUT);
                }
                
                log.debug("收到心跳: ping -> pong"); // debug级别，防止日志刷屏
            } catch (IOException e) {
                log.error("心跳回复失败", e);
            }
        } else {
            log.info("收到业务消息: {}", message);
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        // 这里的逻辑通常是记录日志
        // 注意：onError 触发后，通常容器紧接着会调用 onClose，
        // 所以具体的移除操作建议统一放在 onClose 里，避免重复操作 Map
        log.error("WebSocket 发生错误, Session ID: {}", (session != null ? session.getId() : "null"));
        log.error("错误详情:", error);
    }

    /**
     * 推送消息（对外接口）
     * 支持多实例部署：先检查Redis中的在线状态，如果用户在其他实例，记录为离线消息
     */
    public static void pushMessage(Long userId, String message) {
        // 1. 先检查本地Session（当前实例）
        Session session = ONLINE_SESSION_MAP.get(userId);
        if (session != null && session.isOpen()) {
            synchronized (session) {
                try {
                    session.getBasicRemote().sendText(message);
                    log.debug("消息推送成功: userId={}, instance={}", userId, instanceId);
                    return;
                } catch (IOException e) {
                    log.error("消息推送失败: userId={}", userId, e);
                    // 发送失败通常意味着连接已断，尝试手动关闭
                    try {
                        session.close();
                    } catch (IOException ex) {
                        // ignore
                    }
                    // 继续执行，可能用户在其他实例
                }
            }
        }

        // 2. 检查Redis中的在线状态（可能在其他实例）
        if (redisUtils != null) {
            String onlineInstanceId = redisUtils.get(REDIS_KEY_ONLINE + userId);
            if (onlineInstanceId != null && !onlineInstanceId.equals(instanceId)) {
                // 用户在其他实例在线，但当前实例无法直接推送
                // 这里可以：
                // - 使用Redis Pub/Sub通知其他实例推送（需要其他实例订阅）
                // - 或者存储为离线消息，等待用户下次连接时拉取
                log.debug("用户在其他实例在线: userId={}, instance={}", userId, onlineInstanceId);
                // TODO: 可以实现Redis Pub/Sub或离线消息存储
                return;
            }
        }

        // 3. 用户不在线，存储为离线消息
        if (redisUtils != null) {
            String offlineKey = "ws:offline:" + userId;
            redisUtils.lPush(offlineKey, message);
            // 设置过期时间（7天）
            redisUtils.expire(offlineKey, 7 * 24 * 3600);
            log.debug("用户离线，消息已存储: userId={}", userId);
        } else {
            log.warn("用户不在线或连接已关闭: userId={}", userId);
        }
    }

    /**
     * 获取用户在线状态
     * @return 用户所在的实例ID，如果不在线返回null
     */
    public static String getUserOnlineInstance(Long userId) {
        if (redisUtils == null) {
            return ONLINE_SESSION_MAP.containsKey(userId) ? instanceId : null;
        }
        return redisUtils.get(REDIS_KEY_ONLINE + userId);
    }

    /**
     * 检查用户是否在线
     */
    public static boolean isUserOnline(Long userId) {
        if (redisUtils == null) {
            return ONLINE_SESSION_MAP.containsKey(userId);
        }
        return redisUtils.hasKey(REDIS_KEY_ONLINE + userId);
    }

    /**
     * 获取当前实例的在线用户数
     */
    public static int getLocalOnlineCount() {
        return ONLINE_SESSION_MAP.size();
    }
}
