package com.bytedance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bytedance.entity.ConversationMember;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ConversationMemberMapper extends BaseMapper<ConversationMember> {

    // 原子更新未读数
    // 使用 MyBatis 的注解写原生 SQL，性能最高
    // 逻辑：给群里除了发送者(senderId)以外的所有人，unread_count + 1
    @Update("UPDATE conversation_members SET unread_count = unread_count + 1 " +
            "WHERE conversation_id = #{conversationId} AND user_id != #{senderId}")
    void incrementUnreadCount(@Param("conversationId") Long conversationId,
                              @Param("senderId") Long senderId);
}
