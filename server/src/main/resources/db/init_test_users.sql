-- 清理旧数据（防止 ID 冲突或数据干扰）
DELETE FROM messages;
DELETE FROM conversation_members;
DELETE FROM conversations;
DELETE FROM users;

-- 创建三个测试用户
-- 用户 A: ZhangSan (ID: 1001) -> 扮演群主/发送者
-- 用户 B: LiSi (ID: 1002) -> 扮演接收者/被邀请者
-- 用户 C: WangWu (ID: 1003) -> 扮演路人

-- 注意：password 字段设置为 NULL，表示测试用户没有密码，登录时传空字符串即可
INSERT INTO users (user_id, username, avatar_url, password) VALUES 
(1001, 'ZhangSan', 'https://api.dicebear.com/7.x/avataaars/svg?seed=Zhang', NULL),
(1002, 'LiSi', 'https://api.dicebear.com/7.x/avataaars/svg?seed=Li', NULL),
(1003, 'WangWu', 'https://api.dicebear.com/7.x/avataaars/svg?seed=Wang', NULL);
