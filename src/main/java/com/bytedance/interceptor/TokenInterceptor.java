package com.bytedance.interceptor;


import com.bytedance.utils.UserContext;
import com.bytedance.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class TokenInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 如果是 OPTIONS 请求（跨域预检），直接放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 2. 获取 Header 中的 Token
        // 约定前端将 Token 放在 Header: "Authorization" 里面，格式通常是 "Bearer token字符串"
        String authHeader = request.getHeader("Authorization");

        if (StringUtils.hasLength(authHeader) && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7); // 去掉 "Bearer " 前缀

            // 3. 解析 Token
            Long userId = jwtUtils.getUserIdFromToken(token);

            if (userId != null) {
                // 4. 【核心】将 userId 放入当前线程上下文
                UserContext.setUserId(userId);
                return true; // 放行
            }
        }

        // 5. 校验失败，返回 401
        response.setStatus(401);
        response.setContentType("application/json;charset=utf-8");
        response.getWriter().write("{\"code\": 401, \"msg\": \"未登录或Token已过期\"}");
        return false; // 拦截
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 6. 【非常重要】请求结束后清理 ThreadLocal，防止内存泄漏
        UserContext.clear();
    }
}
