package com.bytedance.consumer;

import com.bytedance.config.SpringWebSocketConfigurator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket 服务端
 */
@ServerEndpoint(value = "/ws/{userId}", configurator = SpringWebSocketConfigurator.class)
@Component
@Slf4j
public class WebSocketServer {

    private static final ConcurrentHashMap<Long, Session> ONLINE_SESSION_MAP = new ConcurrentHashMap<>();

    // 定义心跳超时时间 (毫秒)，比如 60秒
    // 如果 60秒内没有收到任何数据(包括 ping)，连接自动断开
    private static final long MAX_IDLE_TIMEOUT = 60 * 1000L;

    @OnOpen
    public void onOpen(Session session, @PathParam("userId") Long userId) {
        try {
            // 【关键修改 1】设置最大空闲时间
            // 如果客户端 60秒 不发 ping，Tomcat 会自动调用 onClose 关闭连接
            session.setMaxIdleTimeout(MAX_IDLE_TIMEOUT);

            ONLINE_SESSION_MAP.put(userId, session);
            log.info("用户 connected: {}, 当前在线: {}", userId, ONLINE_SESSION_MAP.size());
        } catch (Exception e) {
            log.error("连接异常", e);
        }
    }

    @OnClose
    public void onClose(@PathParam("userId") Long userId, CloseReason reason) {
        // 【关键修改 2】打印关闭原因，方便排查是 超时关闭 还是 正常关闭
        // CloseCode: 1000=正常, 1001=离开, 1006=异常断开
        log.info("用户 disconnected: {}, 原因: {}", userId, reason.getCloseCode());

        // 移除用户，释放资源
        ONLINE_SESSION_MAP.remove(userId);
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        // 只要收到消息（无论是业务消息还是心跳），IdleTimeout 计时器都会自动重置
        if ("ping".equals(message)) {
            try {
                // 收到 ping，回复 pong
                session.getBasicRemote().sendText("pong");
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
     */
    public static void pushMessage(Long userId, String message) {
        Session session = ONLINE_SESSION_MAP.get(userId);
        if (session != null && session.isOpen()) {
            synchronized (session) {
                try {
                    session.getBasicRemote().sendText(message);
                } catch (IOException e) {
                    log.error("消息推送失败: userId={}", userId, e);
                    // 发送失败通常意味着连接已断，可以尝试手动关闭
                    try {
                        session.close();
                    } catch (IOException ex) {
                        // ignore
                    }
                }
            }
        } else {
            // 这里可以做“离线消息”的处理逻辑
            log.warn("用户不在线或连接已关闭: {}", userId);
        }
    }
}
