package com.bytedance.service;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.bytedance.entity.Conversation;
import com.bytedance.entity.ConversationMember;
import com.bytedance.mapper.ConversationMemberMapper;
import com.bytedance.repository.IConversationMemberRepository;
import com.bytedance.repository.IConversationRepository;
import com.bytedance.service.impl.ConversationServiceImpl;
import com.bytedance.usecase.conversation.AddMembersUseCase;
import com.bytedance.usecase.conversation.CreateConversationUseCase;
import com.bytedance.usecase.conversation.GetConversationListUseCase;
import com.bytedance.vo.ConversationVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ConversationServiceImpl 单元测试
 */
@ExtendWith(MockitoExtension.class)
class ConversationServiceImplTest {

    @Mock
    private CreateConversationUseCase createConversationUseCase;

    @Mock
    private GetConversationListUseCase getConversationListUseCase;

    @Mock
    private AddMembersUseCase addMembersUseCase;

    @Mock
    private IConversationRepository conversationRepository;

    @Mock
    private IConversationMemberRepository conversationMemberRepository;

    @Mock
    private ConversationMemberMapper conversationMemberMapper;

    @InjectMocks
    private ConversationServiceImpl conversationService;

    private Conversation testConversation;
    private ConversationVO testConversationVO;
    private ConversationMember testMember;

    @BeforeEach
    void setUp() {
        // 准备测试数据
        testConversation = Conversation.builder()
                .conversationId(100L)
                .type(2)
                .name("测试群聊")
                .ownerId(1L)
                .currentSeq(0L)
                .createdTime(LocalDateTime.now())
                .build();

        testConversationVO = ConversationVO.builder()
                .conversationId(100L)
                .type(2)
                .name("测试群聊")
                .unreadCount(0)
                .isTop(false)
                .build();

        testMember = ConversationMember.builder()
                .id(1L)
                .conversationId(100L)
                .userId(1L)
                .role(1)
                .unreadCount(0)
                .joinedTime(LocalDateTime.now())
                .build();
    }

    @Test
    void testCreateConversation_Success() {
        // 模拟创建会话用例返回会话ID
        when(createConversationUseCase.execute("测试群聊", 2, 1L))
                .thenReturn(100L);

        // 执行创建会话
        long result = conversationService.createConversation("测试群聊", 2, 1L);

        // 验证结果
        assertEquals(100L, result);

        // 验证调用了创建会话用例
        verify(createConversationUseCase, times(1))
                .execute("测试群聊", 2, 1L);
    }

    @Test
    void testGetConversationList_Success() {
        // 模拟获取会话列表用例返回列表
        List<ConversationVO> expectedList = Arrays.asList(testConversationVO);
        when(getConversationListUseCase.execute(1L))
                .thenReturn(expectedList);

        // 执行获取会话列表
        List<ConversationVO> result = conversationService.getConversationList(1L);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(100L, result.get(0).getConversationId());

        // 验证调用了获取会话列表用例
        verify(getConversationListUseCase, times(1)).execute(1L);
    }

    @Test
    void testGetConversationList_Empty() {
        // 模拟获取会话列表用例返回空列表
        when(getConversationListUseCase.execute(1L))
                .thenReturn(Collections.emptyList());

        // 执行获取会话列表
        List<ConversationVO> result = conversationService.getConversationList(1L);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.isEmpty());

        // 验证调用了获取会话列表用例
        verify(getConversationListUseCase, times(1)).execute(1L);
    }

    @Test
    void testAddMembers_Success() {
        // 模拟添加成员用例成功执行
        doNothing().when(addMembersUseCase)
                .execute(100L, Arrays.asList(2L, 3L), 1L);

        // 执行添加成员
        assertDoesNotThrow(() -> {
            conversationService.addMembers(100L, Arrays.asList(2L, 3L), 1L);
        });

        // 验证调用了添加成员用例
        verify(addMembersUseCase, times(1))
                .execute(100L, Arrays.asList(2L, 3L), 1L);
    }

    @Test
    void testAddMembers_ConversationNotFound() {
        // 模拟添加成员用例抛出会话不存在异常
        doThrow(new RuntimeException("会话不存在"))
                .when(addMembersUseCase)
                .execute(999L, Arrays.asList(2L, 3L), 1L);

        // 验证异常被抛出
        assertThrows(RuntimeException.class, () -> {
            conversationService.addMembers(999L, Arrays.asList(2L, 3L), 1L);
        });

        verify(addMembersUseCase, times(1))
                .execute(999L, Arrays.asList(2L, 3L), 1L);
    }

    @Test
    void testFindExistingConversation_Success() {
        // 准备测试数据
        List<Conversation> conversations = Arrays.asList(testConversation);
        List<ConversationMember> members = Arrays.asList(
                ConversationMember.builder().userId(1L).build(),
                ConversationMember.builder().userId(2L).build()
        );

        // 模拟仓库方法
        when(conversationRepository.findByNameAndType("测试群聊", 2))
                .thenReturn(conversations);
        when(conversationMemberRepository.findByConversationId(100L))
                .thenReturn(members);

        // 执行查找已存在会话
        Long result = conversationService.findExistingConversation(
                "测试群聊", 2, Arrays.asList(1L, 2L));

        // 验证结果
        assertNotNull(result);
        assertEquals(100L, result);

        // 验证调用了仓库方法
        verify(conversationRepository, times(1))
                .findByNameAndType("测试群聊", 2);
        verify(conversationMemberRepository, times(1))
                .findByConversationId(100L);
    }

    @Test
    void testFindExistingConversation_NotFound() {
        // 模拟仓库方法返回空列表
        when(conversationRepository.findByNameAndType("不存在的群", 2))
                .thenReturn(Collections.emptyList());

        // 执行查找已存在会话
        Long result = conversationService.findExistingConversation(
                "不存在的群", 2, Arrays.asList(1L, 2L));

        // 验证结果
        assertNull(result);

        // 验证调用了仓库方法
        verify(conversationRepository, times(1))
                .findByNameAndType("不存在的群", 2);
        verify(conversationMemberRepository, never())
                .findByConversationId(anyLong());
    }

    @Test
    void testFindExistingConversation_MembersNotMatch() {
        // 准备测试数据 - 成员不匹配
        List<Conversation> conversations = Arrays.asList(testConversation);
        List<ConversationMember> members = Arrays.asList(
                ConversationMember.builder().userId(1L).build(),
                ConversationMember.builder().userId(3L).build() // 不同的成员
        );

        // 模拟仓库方法
        when(conversationRepository.findByNameAndType("测试群聊", 2))
                .thenReturn(conversations);
        when(conversationMemberRepository.findByConversationId(100L))
                .thenReturn(members);

        // 执行查找已存在会话
        Long result = conversationService.findExistingConversation(
                "测试群聊", 2, Arrays.asList(1L, 2L)); // 查找成员1和2

        // 验证结果 - 成员不匹配，返回null
        assertNull(result);
    }

    @Test
    void testFindExistingConversation_NullName() {
        // 执行查找已存在会话 - 名称为null
        Long result = conversationService.findExistingConversation(
                null, 2, Arrays.asList(1L, 2L));

        // 验证结果
        assertNull(result);

        // 验证没有调用仓库方法
        verify(conversationRepository, never())
                .findByNameAndType(anyString(), anyInt());
    }

    @Test
    void testFindExistingConversation_EmptyName() {
        // 执行查找已存在会话 - 名称为空
        Long result = conversationService.findExistingConversation(
                "", 2, Arrays.asList(1L, 2L));

        // 验证结果
        assertNull(result);

        // 验证没有调用仓库方法
        verify(conversationRepository, never())
                .findByNameAndType(anyString(), anyInt());
    }

    @Test
    void testFindExistingConversation_EmptyMemberIds() {
        // 执行查找已存在会话 - 成员列表为空
        Long result = conversationService.findExistingConversation(
                "测试群聊", 2, Collections.emptyList());

        // 验证结果
        assertNull(result);

        // 验证没有调用仓库方法
        verify(conversationRepository, never())
                .findByNameAndType(anyString(), anyInt());
    }

    @Test
    void testClearUnreadCount_Success() {
        // 执行清除未读数
        assertDoesNotThrow(() -> {
            conversationService.clearUnreadCount(100L, 1L);
        });

        // 验证调用了Mapper方法
        verify(conversationMemberMapper, times(1))
                .clearUnreadCount(100L, 1L);
    }

    @Test
    void testClearAllUnreadCount_Success() {
        // 执行清除所有未读数
        assertDoesNotThrow(() -> {
            conversationService.clearAllUnreadCount(1L);
        });

        // 验证调用了Mapper方法
        verify(conversationMemberMapper, times(1))
                .clearAllUnreadCount(1L);
    }
}

