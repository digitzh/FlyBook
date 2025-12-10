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
 * 客户端连接地址: ws://localhost:8081/ws/{userId}
 */
@ServerEndpoint(value = "/ws/{userId}", configurator = SpringWebSocketConfigurator.class)
@Component
@Slf4j
public class WebSocketServer {

    // 静态初始化块，用于确认类是否被加载
    static {
        System.out.println("========== WebSocketServer 类已加载 ==========");
    }

    // 记录当前在线连接数 线程安全
    // Key = userId, Value = Session
    private static final ConcurrentHashMap<Long, Session> ONLINE_SESSION_MAP = new ConcurrentHashMap<>();
    
    // 构造函数，用于确认实例是否被创建
    public WebSocketServer() {
        log.info("========== WebSocketServer 实例被创建 ==========");
    }

    /**
     * 连接建立成功
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("userId") Long userId) {
        try {
            log.info("========== WebSocket 连接请求到达 ==========");
            log.info("用户 ID: {}", userId);
            log.info("Session ID: {}", session.getId());
            log.info("请求 URI: {}", session.getRequestURI());
            log.info("查询字符串: {}", session.getQueryString());
            
            // 将连接存入 Map
            ONLINE_SESSION_MAP.put(userId, session);
            log.info("用户 connected: {}, 当前在线人数: {}", userId, ONLINE_SESSION_MAP.size());
            log.info("=========================================");
        } catch (Exception e) {
            log.error("处理 WebSocket 连接时发生异常", e);
            throw e;
        }
    }

    /**
     * 连接关闭
     */
    @OnClose
    public void onClose(@PathParam("userId") Long userId) {
        ONLINE_SESSION_MAP.remove(userId);
        log.info("用户 disconnected: {}, 当前在线人数: {}", userId, ONLINE_SESSION_MAP.size());
    }

    /**
     * 收到客户端消息后调用的方法
     * (在这个简单的 IM 架构中，我们主要用 HTTP 发消息，WebSocket 主要用于服务器推，
     * 但这里保留接收能力，用于心跳检测)
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        log.info("收到客户端消息: {}", message);
        // 在这里处理 ping-pong 心跳
        if ("ping".equals(message)) {
            try {
                session.getBasicRemote().sendText("pong");
            } catch (IOException e) {
                log.error("pong 发送失败", e);
            }
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error("========== WebSocket 发生错误 ==========");
        if (session != null) {
            log.error("Session ID: {}", session.getId());
            log.error("Session 是否打开: {}", session.isOpen());
            log.error("请求 URI: {}", session.getRequestURI());
        } else {
            log.error("Session 为 null");
        }
        log.error("错误类型: {}", error.getClass().getName());
        log.error("错误消息: {}", error.getMessage());
        log.error("完整错误堆栈:", error);
        log.error("=========================================");
    }

    /**
     * 发送消息给指定用户
     * 供 Service 层调用
     */
    public static void pushMessage(Long userId, String message) {
        Session session = ONLINE_SESSION_MAP.get(userId);
        if (session != null && session.isOpen()) {
            try {
                // 加锁防止并发写入异常
                synchronized (session) {
                    session.getBasicRemote().sendText(message);
                }
            } catch (IOException e) {
                log.error("推送消息失败: userId={}", userId, e);
            }
        } else {
            // 用户不在线，不需要处理，消息已经存库了，等他上线拉取即可
            log.info("用户不在线，跳过推送: {}", userId);
        }
    }
}

