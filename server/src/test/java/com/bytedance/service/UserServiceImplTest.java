package com.bytedance.service;

import com.bytedance.dto.RegisterRequest;
import com.bytedance.entity.User;
import com.bytedance.service.impl.UserServiceImpl;
import com.bytedance.usecase.user.LoginUserUseCase;
import com.bytedance.usecase.user.RegisterUserUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * UserServiceImpl 单元测试
 */
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private RegisterUserUseCase registerUserUseCase;

    @Mock
    private LoginUserUseCase loginUserUseCase;

    @InjectMocks
    private UserServiceImpl userService;

    private RegisterRequest registerRequest;
    private LoginUserUseCase.LoginResult loginResult;
    private User testUser;

    @BeforeEach
    void setUp() {
        // 准备测试数据
        registerRequest = new RegisterRequest();
        registerRequest.setUsername("testuser");
        registerRequest.setPassword("password123");

        testUser = User.builder()
                .userId(1L)
                .username("testuser")
                .password("password123")
                .avatarUrl("https://example.com/avatar.jpg")
                .build();

        loginResult = new LoginUserUseCase.LoginResult("test-token", testUser);
    }

    @Test
    void testRegister_Success() {
        // 执行注册
        assertDoesNotThrow(() -> userService.register(registerRequest));

        // 验证调用了注册用例
        verify(registerUserUseCase, times(1)).execute(registerRequest);
    }

    @Test
    void testRegister_ThrowsException() {
        // 模拟注册用例抛出异常
        doThrow(new RuntimeException("用户名已存在"))
                .when(registerUserUseCase).execute(any(RegisterRequest.class));

        // 验证异常被抛出
        assertThrows(RuntimeException.class, () -> {
            userService.register(registerRequest);
        });

        verify(registerUserUseCase, times(1)).execute(registerRequest);
    }

    @Test
    void testLogin_Success() {
        // 模拟登录用例返回成功结果
        when(loginUserUseCase.execute("testuser", "password123"))
                .thenReturn(loginResult);

        // 执行登录
        LoginUserUseCase.LoginResult result = userService.login("testuser", "password123");

        // 验证结果
        assertNotNull(result);
        assertEquals("test-token", result.getToken());
        assertEquals(testUser, result.getUser());
        assertEquals("testuser", result.getUser().getUsername());

        // 验证调用了登录用例
        verify(loginUserUseCase, times(1)).execute("testuser", "password123");
    }

    @Test
    void testLogin_UserNotFound() {
        // 模拟登录用例抛出用户不存在异常
        when(loginUserUseCase.execute("nonexistent", "password123"))
                .thenThrow(new RuntimeException("用户不存在"));

        // 验证异常被抛出
        assertThrows(RuntimeException.class, () -> {
            userService.login("nonexistent", "password123");
        });

        verify(loginUserUseCase, times(1)).execute("nonexistent", "password123");
    }

    @Test
    void testLogin_WrongPassword() {
        // 模拟登录用例抛出密码错误异常
        when(loginUserUseCase.execute("testuser", "wrongpassword"))
                .thenThrow(new RuntimeException("密码错误"));

        // 验证异常被抛出
        assertThrows(RuntimeException.class, () -> {
            userService.login("testuser", "wrongpassword");
        });

        verify(loginUserUseCase, times(1)).execute("testuser", "wrongpassword");
    }
}

