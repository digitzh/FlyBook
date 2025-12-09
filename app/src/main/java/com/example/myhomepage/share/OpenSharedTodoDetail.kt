package com.example.myhomepage.share

/* IM 模块调用；由 待办模块实现
当用户在聊天界面点击一条“待办卡片消息”时，IM 模块调用该接口，并传入当时的 TodoShareCard，
由待办任务块负责展示一个“只读详情页”，内容完全来自这份 Card，而不是从本地待办数据库读取
 */

interface OpenSharedTodoDetail {
    /**
     * 打开一个只读的待办详情页
     *
     * @param card 当时分享时的待办卡片内容
     */
    fun open(card: TodoShareCard)
}