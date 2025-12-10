# FlyBook

参考链接：[类飞书的 Android 办公应用开发 - 飞书文档](https://bytedance.larkoffice.com/wiki/ZchQw86ELicBrgkm9RMcuggYnyh)

一个类似飞书的即时通信和待办任务管理应用，包含 Android 客户端（Kotlin）和服务端（Java）。

## 📱 项目简介

FlyBook 是一个企业级即时通信应用，主要包含以下两个核心模块：

- **即时通信（IM）**：支持单聊、群聊，实时消息推送，消息历史记录
- **待办任务（Todo）**：支持创建、查看、管理待办事项，支持多种任务类型

## 🏗️ 项目结构

```
FlyBook/
├── app/                                # Android 客户端（Kotlin）
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/myhomepage/
│   │   │   │   ├── data/               # 数据模型
│   │   │   │   ├── database/           # Room 数据库
│   │   │   │   ├── network/            # 网络请求和 WebSocket
│   │   │   │   ├── todolist/           # 待办任务模块
│   │   │   │   ├── ui/                 # Compose UI 界面
│   │   │   │   ├── MainActivity.kt     # 主活动
│   │   │   │   └── WeViewModel.kt      # ViewModel
│   │   │   ├── res/                    # 资源文件（drawable、layout、values 等）
│   │   │   └── AndroidManifest.xml     # 清单文件
│   │   ├── androidTest/                # Android 测试
│   │   └── test/                       # 单元测试
│   ├── build.gradle.kts                # Gradle 配置
│   └── proguard-rules.pro              # ProGuard 混淆配置
│
├── server/                             # 服务端（Java Spring Boot）
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/bytedance/
│   │   │   │   ├── common/             # 公共类（常量、异常等）
│   │   │   │   ├── config/             # 配置类（WebSocket、缓存等）
│   │   │   │   ├── consumer/           # WebSocket 消费者
│   │   │   │   ├── controller/         # REST API 控制器
│   │   │   │   ├── dto/                # 数据传输对象
│   │   │   │   ├── entity/             # 数据库实体类
│   │   │   │   ├── handler/            # 事件处理器
│   │   │   │   ├── interceptor/        # 拦截器
│   │   │   │   ├── mapper/             # MyBatis Mapper 接口
│   │   │   │   ├── repository/         # 数据库访问层
│   │   │   │   ├── service/            # 业务逻辑层
│   │   │   │   ├── usecase/            # 用例层
│   │   │   │   ├── utils/              # 工具类
│   │   │   │   ├── vo/                 # 视图对象
│   │   │   │   └── MainApplication.java # 应用主类
│   │   │   └── resources/              # 配置文件（application.yaml 等）
│   │   └── test/                       # 测试代码
│   ├── pom.xml                         # Maven 配置
│   └── target/                         # 构建输出目录
│
├── gradle/                             # Gradle 配置文件
│   ├── libs.versions.toml              # 依赖版本管理
│   └── wrapper/
├── doc/                                # 项目文档
├── img/                                # 项目图片资源
│   ├── avatar/                         # 头像资源
│   └── README/                         # README 中使用的截图
├── build.gradle.kts                    # 根项目 Gradle 配置
├── settings.gradle.kts                 # Gradle 多项目配置
├── gradle.properties                   # Gradle 属性配置
├── gradlew / gradlew.bat               # Gradle 包装器脚本
├── README.md                           # 项目说明文档
└── local.properties                    # 本地开发配置（本地 SDK 路径等）
```

## 🛠️ 技术栈

### 客户端（Android）

- **语言**: Kotlin
- **UI 框架**: Jetpack Compose
- **架构组件**:
  - ViewModel + LiveData/StateFlow
  - Navigation Compose
- **数据库**: Room（本地数据存储）
- **网络**:
  - OkHttp（HTTP 请求）
  - WebSocket（实时消息推送）
  - Kotlinx Serialization（JSON 序列化）
- **图片加载**: Coil
- **UI 组件**: Material3

### 服务端（Java）

- **框架**: Spring Boot
- **数据库**: MySQL
- **ORM**: MyBatis Plus
- **缓存**: Redis（Spring Data Redis）
- **实时通信**: WebSocket（Spring WebSocket）
- **认证**: JWT（可选）
- **工具库**: Hutool
- **连接池**: Apache Commons Pool2

## 📋 功能特性

### 登录模块（可选）

<img src="img\\README\\login_nocode.png" width="240px" height="528px">
<img src="img\\README\\login.png" width="240px" height="528px">

### 即时通信模块

包括主页中展示的聊天列表与每个好友的详细聊天界面。

<img src="img\\README\\add_chat.png" width="240px" height="528px">
<img src="img\\README\\homepage.png" width="240px" height="528px">
<img src="img\\README\\homepage_black.png" width="240px" height="528px">
<img src="img\\README\\chatlist.png" width="240px" height="528px">
<img src="img\\README\\chatlist_complete.png" width="240px" height="528px">
<!-- <img src="img\\README\\chatlist_black.png" width="240px" height="528px"> -->
<img src="img\\README\\emoji.png" width="240px" height="528px">
<img src="img\\README\\emoji_boom.png" width="240px" height="528px">


#### ✅已完成

- 聊天列表展示
- 创建单聊和群聊会话
- 发送文本/图片消息
- 未读消息提醒
- 会话界面历史消息展示
- 发送消息展示
- 部分Emoji表情的添加与动画效果

### 待办任务模块

分类展示待办事项(主要分为文件、通知、事务、其他，不同事务按照颜色区分)，点击可进入详情页，详情页中的按钮"标记为已完成"，点击可修改状态。

<img src="img\\README\\todolist.png" width="240px" height="528px">
<!-- <img src="img\\README\\todolist_black.png" width="240px" height="528px"> -->
<img src="img\\README\\tododetails.png" width="240px" height="528px">
<!-- <img src="img\\README\\tododetails_black.png" width="240px" height="528px"> -->
<img src="img\\README\\tododetails_done.png" width="240px" height="528px">
<img src="img\\README\\add_todo.png" width="240px" height="528px">
<img src="img\\README\\date.png" width="240px" height="528px">
<img src="img\\README\\todolist_com+del.png" width="240px" height="528px">

#### ✅已完成

- 创建待办任务
- 任务类型分类（文件、会议、其他等）
- 任务详情查看
- 任务状态管理
- 任务列表展示（网格布局）
- 任务与聊天消息关联

### 个人主页模块

主要提供用户的基本信息，以及个人账户的设置模块等。

<img src="img\\README\\me.png" width="240px" height="528px">
<!-- <img src="img\\README\\me_black.png" width="240px" height="528px"> -->

#### ✅已完成

- 个人主页展示

## 🚀 快速开始

### 开发环境

**客户端**:
- Android Studio Otter | 2025.2.1 
- JDK 17
- Android SDK API 24（最低支持 Android 7.0）
- Gradle 8.13.0

**服务端**:
- JDK 17
- Maven 4.0.0
- MySQL

### 服务端部署

1. **配置数据库**

    创建数据库 `flybook`、导入 `db/flybook.sql`。

2. **修改配置文件**

   编辑 `server/src/main/resources/application.yaml`：
   ```yaml
   spring:
     datasource:
       url: jdbc:mysql://127.0.0.1:3306/flybook?useSSL=false&serverTimezone=UTC
       username: your_username
       password: your_password
   ```

3. **启动服务端**

   ```bash
   cd server
   mvn spring-boot:run
   ```

   服务端将在 `http://localhost:8081` 启动。

### 客户端部署

1. **配置服务端地址**

   编辑 `app/src/main/java/com/example/myhomepage/network/ApiService.kt` 和 `WebSocketManager.kt`：
   - **Android 模拟器**: 使用 `10.0.2.2:8081`（已配置）
   - **真机调试**: 将 `10.0.2.2` 替换为你的 PC 实际 IP 地址（如 `192.168.x.x:8081`）

2. **导入项目**

   使用 Android Studio 打开项目根目录。

3. **同步依赖**

   Android Studio 会自动同步 Gradle 依赖，或手动执行：
   ```bash
   ./gradlew build
   ```

4. **运行应用**

   连接 Android 设备或启动模拟器，点击运行按钮。

## 📡 API 接口

服务端接口参考：[FlyBook即时通信服务端接口文档](https://dcnihtjj5x0k.feishu.cn/wiki/K8mcwHGkai3Uxrkghvzc56GUnXg)

### 用户相关

- `POST /api/users/register` - 用户注册
- `POST /api/users/login` - 用户登录
- `GET /api/users/list` - 获取用户列表

### 会话相关

- `POST /api/conversations/create` - 创建会话
- `GET /api/conversations/list` - 获取会话列表
- `POST /api/conversations/members/add` - 添加会话成员

### 消息相关

- `POST /api/messages/send` - 发送消息
- `GET /api/messages/sync` - 同步消息历史
- `POST /api/conversations/unread/clear` - 清空单会话未读消息
- `POST /api/conversations/unread/clear-all` - 清空所有会话未读消息

### WebSocket

- `ws://host:port/ws/{userId}` - WebSocket 连接端点

### 项目构建

**客户端**:
```bash
./gradlew assembleDebug    # 构建 Debug APK
./gradlew assembleRelease  # 构建 Release APK
```

**服务端**:
```bash
cd server
mvn clean package          # 打包
mvn spring-boot:run        # 运行
```
