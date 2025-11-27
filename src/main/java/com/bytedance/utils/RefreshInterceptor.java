package com.bytedance.utils;

import org.springframework.web.servlet.HandlerInterceptor;

public class RefreshInterceptor implements HandlerInterceptor {

//    private StringRedisTemplate stringRedisTemplate;
//
//    public RefreshInterceptor(StringRedisTemplate stringRedisTemplate) {
//        this.stringRedisTemplate = stringRedisTemplate;
//    }
//
//    @Override
//    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
//        String token = request.getHeader("authorization");
//        if (StrUtil.isBlank(token)) {       //如果没有token不保存到treadlocal 直接放行到后面 后面再拦截
//            return true;
//        }
//
//        String key = LOGIN_USER_KEY + token;
//        Map<Object, Object> userMap = stringRedisTemplate.opsForHash().entries(key);
//        if (userMap.isEmpty()) {   //一样，先放行后拦截
//            return true;
//        }
//
////        Object user = session.getAttribute("user");
////        UserHolder.saveUser(BeanUtil.copyProperties(user, UserDTO.class));
//
//        UserDTO userDTO = BeanUtil.fillBeanWithMap(userMap, new UserDTO(), false);  //有用户 直接存到threadlocal，后面放行
//        UserHolder.saveUser(userDTO);
//
//        stringRedisTemplate.expire(key, LOGIN_USER_TTL, TimeUnit.MINUTES);  //核心作用 刷新token
//        return true;
//    }
//
//    @Override
//    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
//        UserHolder.removeUser();
//    }
}
