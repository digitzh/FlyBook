# FlyBook server

FlyBook server是一个基于 Spring Boot 的即时通信（IM）服务端项目，提供用户管理、会话管理、消息发送与同步、实时推送等核心功能。

## 🛠 技术栈

- **框架**: Spring Boot
- **数据库**: MySQL
- **ORM**: MyBatis-Plus
- **实时通信**: WebSocket
- **身份认证**: JWT
- **工具库**: Hutool
- **Java 版本**: JDK 17

## 📁 项目结构

```
server/
├── src/
│   ├── main/
│   │   ├── java/com/bytedance/
│   │   │   ├── MainApplication.java          # 启动类
│   │   │   ├── config/                        # 配置类
│   │   │   │   ├── WebSocketConfig.java       # WebSocket 配置
│   │   │   │   ├── AuthConfig.java            # 认证配置
│   │   │   │   └── ...
│   │   │   ├── controller/                    # 控制器层
│   │   │   │   ├── UserController.java        # 用户相关接口
│   │   │   │   ├── ConversationController.java # 会话相关接口
│   │   │   │   ├── MessageController.java     # 消息相关接口
│   │   │   │   └── UploadController.java      # 文件上传接口
│   │   │   ├── service/                       # 服务层
│   │   │   │   ├── IUserService.java
│   │   │   │   ├── IConversationService.java
│   │   │   │   ├── IMessageService.java
│   │   │   │   └── impl/                      # 服务实现
│   │   │   ├── mapper/                        # MyBatis Mapper
│   │   │   ├── entity/                        # 实体类
│   │   │   ├── dto/                           # 数据传输对象
│   │   │   ├── vo/                            # 视图对象
│   │   │   ├── utils/                         # 工具类
│   │   │   ├── consumer/                      # WebSocket 服务
│   │   │   │   └── WebSocketServer.java
│   │   │   └── interceptor/                   # 拦截器
│   │   └── resources/
│   │       ├── application.yaml               # 应用配置
│   │       ├── db/                            # 数据库脚本
│   │       │   ├── create_tables.sql          # 建表脚本
│   │       │   └── init_test_users.sql        # 测试用户数据
│   │       └── mapper/                        # MyBatis XML 映射文件
│   └── test/                                  # 测试代码
└── pom.xml                                    # Maven 依赖配置
```

## 🗄️ 数据库结构

### 核心表说明

#### users (用户表)
- `user_id`: 用户ID（主键）
- `username`: 用户名
- `avatar_url`: 头像URL
- `password`: 密码（可为空）

#### conversations (会话表)
- `conversation_id`: 会话ID（主键）
- `type`: 会话类型（1=单聊，2=群聊）
- `name`: 会话名称（群聊名称）
- `avatar_url`: 会话头像
- `owner_id`: 群主ID
- `current_seq`: 当前最新序列号
- `last_msg_content`: 最新消息摘要
- `last_msg_time`: 最新消息时间

#### messages (消息表)
- `message_id`: 消息ID（主键）
- `conversation_id`: 所属会话ID
- `sender_id`: 发送者ID
- `seq`: 会话内序列号（唯一）
- `quote_id`: 引用的消息ID（回复消息）
- `msg_type`: 消息类型（1-6）
- `content`: 消息内容（JSON格式）
- `mentions`: 被@用户列表（JSON格式）
- `is_revoked`: 是否已撤回

#### conversation_members (会话成员表)
- `id`: 主键
- `conversation_id`: 会话ID
- `user_id`: 用户ID
- `last_ack_seq`: 已确认同步到的序列号
- `unread_count`: 未读消息数
- `role`: 角色（1=成员，2=管理员）
- `is_muted`: 是否免打扰
- `is_top`: 是否置顶

#### message_reactions (消息表情回应表)
- `id`: 主键
- `message_id`: 消息ID
- `user_id`: 用户ID
- `reaction_type`: 表情类型（如：thumbsup, heart）
