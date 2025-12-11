package com.bytedance.service;

import com.bytedance.entity.Message;
import com.bytedance.service.impl.MessageServiceImpl;
import com.bytedance.usecase.message.SendMessageUseCase;
import com.bytedance.usecase.message.SyncMessagesUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * MessageServiceImpl 单元测试
 */
@ExtendWith(MockitoExtension.class)
class MessageServiceImplTest {

    @Mock
    private SendMessageUseCase sendMessageUseCase;

    @Mock
    private SyncMessagesUseCase syncMessagesUseCase;

    @InjectMocks
    private MessageServiceImpl messageService;

    private Message testMessage;
    private List<Message> testMessages;

    @BeforeEach
    void setUp() {
        // 准备测试数据
        testMessage = Message.builder()
                .messageId(1L)
                .conversationId(100L)
                .senderId(1L)
                .seq(1L)
                .msgType(1)
                .content("{\"text\":\"Hello\"}")
                .createdTime(LocalDateTime.now())
                .build();

        Message message2 = Message.builder()
                .messageId(2L)
                .conversationId(100L)
                .senderId(2L)
                .seq(2L)
                .msgType(1)
                .content("{\"text\":\"World\"}")
                .createdTime(LocalDateTime.now())
                .build();

        testMessages = Arrays.asList(testMessage, message2);
    }

    @Test
    void testSendMessage_Success() {
        // 模拟发送消息用例返回成功结果
        when(sendMessageUseCase.execute(100L, 1L, 1, "{\"text\":\"Hello\"}"))
                .thenReturn(testMessage);

        // 执行发送消息
        Message result = messageService.sendMessage(100L, 1L, 1, "{\"text\":\"Hello\"}");

        // 验证结果
        assertNotNull(result);
        assertEquals(1L, result.getMessageId());
        assertEquals(100L, result.getConversationId());
        assertEquals(1L, result.getSenderId());
        assertEquals("{\"text\":\"Hello\"}", result.getContent());

        // 验证调用了发送消息用例
        verify(sendMessageUseCase, times(1))
                .execute(100L, 1L, 1, "{\"text\":\"Hello\"}");
    }

    @Test
    void testSendTextMsg_Success() {
        // 模拟发送消息用例返回成功结果
        when(sendMessageUseCase.execute(eq(100L), eq(1L), eq(1), anyString()))
                .thenReturn(testMessage);

        // 执行发送文本消息
        Message result = messageService.sendTextMsg(100L, 1L, "Hello");

        // 验证结果
        assertNotNull(result);
        assertEquals(testMessage, result);

        // 验证调用了发送消息用例，并检查content包含text字段
        verify(sendMessageUseCase, times(1))
                .execute(eq(100L), eq(1L), eq(1), argThat(content -> 
                    content.contains("\"text\"") && content.contains("Hello")
                ));
    }

    @Test
    void testSendMessage_NotMember() {
        // 模拟发送消息用例抛出非成员异常
        when(sendMessageUseCase.execute(100L, 1L, 1, "{\"text\":\"Hello\"}"))
                .thenThrow(new RuntimeException("您不是该会话成员，无法发送消息"));

        // 验证异常被抛出
        assertThrows(RuntimeException.class, () -> {
            messageService.sendMessage(100L, 1L, 1, "{\"text\":\"Hello\"}");
        });

        verify(sendMessageUseCase, times(1))
                .execute(100L, 1L, 1, "{\"text\":\"Hello\"}");
    }

    @Test
    void testSendMessage_ConversationNotFound() {
        // 模拟发送消息用例抛出会话不存在异常
        when(sendMessageUseCase.execute(999L, 1L, 1, "{\"text\":\"Hello\"}"))
                .thenThrow(new RuntimeException("会话不存在"));

        // 验证异常被抛出
        assertThrows(RuntimeException.class, () -> {
            messageService.sendMessage(999L, 1L, 1, "{\"text\":\"Hello\"}");
        });

        verify(sendMessageUseCase, times(1))
                .execute(999L, 1L, 1, "{\"text\":\"Hello\"}");
    }

    @Test
    void testSyncMessages_Success() {
        // 模拟同步消息用例返回消息列表
        when(syncMessagesUseCase.execute(100L, 0L))
                .thenReturn(testMessages);

        // 执行同步消息
        List<Message> result = messageService.syncMessages(100L, 0L);

        // 验证结果
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(testMessage, result.get(0));

        // 验证调用了同步消息用例
        verify(syncMessagesUseCase, times(1)).execute(100L, 0L);
    }

    @Test
    void testSyncMessages_EmptyList() {
        // 模拟同步消息用例返回空列表
        when(syncMessagesUseCase.execute(100L, 0L))
                .thenReturn(Arrays.asList());

        // 执行同步消息
        List<Message> result = messageService.syncMessages(100L, 0L);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.isEmpty());

        // 验证调用了同步消息用例
        verify(syncMessagesUseCase, times(1)).execute(100L, 0L);
    }

    @Test
    void testSyncMessages_WithAfterSeq() {
        // 模拟同步消息用例返回指定序列号之后的消息
        when(syncMessagesUseCase.execute(100L, 5L))
                .thenReturn(Arrays.asList(testMessages.get(1)));

        // 执行同步消息
        List<Message> result = messageService.syncMessages(100L, 5L);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(2L, result.get(0).getSeq());

        // 验证调用了同步消息用例
        verify(syncMessagesUseCase, times(1)).execute(100L, 5L);
    }
}

