package com.example.myhomepage.share

/* 待办模块调用；由 IM 模块实现
当用户在点击「分享」时，待办模块会调用该接口，并传入已构造好的 TodoShareCard
IM 模块负责：

打开会话选择界面；

用户选择会话后，将此 TodoShareCard 作为一条“待办卡片消息”发送到指定会话；

导航到该会话的聊天界面。
 */

interface ShareTodoToChat {
    /**
     * 从待办模块发起：分享一个待办到即时通信模块
     *
     * @param card 由待办模块构造的待办卡片数据，已经包含展示所需的全部信息
     */
    fun share(card: TodoShareCard)
}