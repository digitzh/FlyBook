create table conversation_members
(
    id              bigint auto_increment
        primary key,
    conversation_id bigint                              not null,
    user_id         bigint                              not null,
    last_ack_seq    bigint    default 0                 null comment '已确认同步到的序列号',
    unread_count    int       default 0                 null comment '未读消息数',
    role            tinyint   default 1                 null comment '角色: 1=成员, 2=管理员',
    is_muted        tinyint   default 0                 null comment '免打扰',
    is_top          tinyint   default 0                 null comment '是否置顶',
    joined_time     timestamp default CURRENT_TIMESTAMP null,
    constraint uk_conv_user
        unique (conversation_id, user_id)
)
    comment '会话成员表';

create table conversations
(
    conversation_id  bigint auto_increment
        primary key,
    type             tinyint      default 1                 not null comment '类型: 1=单聊(P2P), 2=群聊(Group)',
    name             varchar(128)                           null comment '群名称',
    avatar_url       varchar(255)                           null comment '群头像',
    owner_id         bigint       default 0                 null comment '群主ID',
    current_seq      bigint       default 0                 null comment '当前会话最新序列号',
    last_msg_content varchar(512) default ''                null comment '最新消息摘要',
    last_msg_time    timestamp    default CURRENT_TIMESTAMP null comment '最新消息时间',
    created_time     timestamp    default CURRENT_TIMESTAMP null
)
    comment '会话表';

create table message_reactions
(
    id            bigint auto_increment
        primary key,
    message_id    bigint                              not null comment '关联消息ID',
    user_id       bigint                              not null comment '操作用户ID',
    reaction_type varchar(32)                         not null comment '表情代码, 如: thumbsup, heart',
    created_time  timestamp default CURRENT_TIMESTAMP null,
    constraint uk_msg_user_react
        unique (message_id, user_id, reaction_type)
)
    comment '消息表情回应表';

create table messages
(
    message_id      bigint auto_increment comment '全局唯一消息ID'
        primary key,
    conversation_id bigint                              not null,
    sender_id       bigint                              not null,
    seq             bigint                              not null comment '会话内序列号',
    quote_id        bigint    default 0                 null comment '引用的消息ID (若不为0，则是回复消息)',
    msg_type        tinyint                             not null comment '1=Text, 2=Image, 3=Video, 4=File, 5=TodoCard, 6=RichPost',
    content         json                                not null comment '消息内容载体',
    mentions        json                                null comment '被@用户ID列表',
    is_revoked      tinyint   default 0                 null comment '是否已撤回',
    created_time    timestamp default CURRENT_TIMESTAMP null,
    constraint uk_conv_seq
        unique (conversation_id, seq)
)
    comment '消息存储表';

create index idx_conv_time
    on messages (conversation_id, created_time);

create table users
(
    user_id      bigint auto_increment comment '用户唯一ID'
        primary key,
    username     varchar(64)                            not null comment '用户名/姓名',
    avatar_url   varchar(255) default ''                null comment '头像链接',
    password     varchar(255)                           null comment '密码（可为空，用于测试）',
    created_time timestamp    default CURRENT_TIMESTAMP null
)
    comment '用户信息表';
