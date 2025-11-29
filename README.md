# FlyBook

参考链接：[类飞书的 Android 办公应用开发 - 飞书文档](https://bytedance.larkoffice.com/wiki/ZchQw86ELicBrgkm9RMcuggYnyh)

## 主页设计
主页主要分为聊天(即时通信)，代办事项，以及个人主页三个部分,有两种主题(浅色与深色)。

<img src="README_IMG\\homepage.png" width="240px" height="528px">
<img src="README_IMG\\homepage_black.png" width="240px" height="528px">

### 即时通信板块
包括主页中展示的聊天列表与每个好友的详细聊天界面。

<img src="README_IMG\\homepage.png" width="240px" height="528px">
<img src="README_IMG\\homepage_black.png" width="240px" height="528px">
<img src="README_IMG\\chatlist.png" width="240px" height="528px">
<img src="README_IMG\\chatlist_black.png" width="240px" height="528px">

已实现功能：
- 聊天界面中聊天列表展示
- 最新消息的动态展示
- 最新消息未读的右上角的红点提醒
- 聊天详情界面过往消息展示
- 我方新发送消息展示(仅是在前端实现，未与后端交互)

### 待办事项板块
可以分类展示待办事项(主要分为文件，通知，事务三类)，若完成事项则不再展示。

<img src="README_IMG\\todolist.png" width="240px" height="528px">
<img src="README_IMG\\todolist_black.png" width="240px" height="528px">

已实现功能
- 待办事项的分类展示

### 个人主页模块
主要提供用户的基本信息，以及个人账户的设置模块等。

<img src="README_IMG\\me.png" width="240px" height="528px">
<img src="README_IMG\\me_black.png" width="240px" height="528px">

已实现功能
- 个人主页的展示功能
