-- =====================================================
-- EasyMeeting 数据库初始化脚本
-- 适用于 MySQL 8.0+
-- =====================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `easymeeting`
DEFAULT CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE `easymeeting`;

-- =====================================================
-- 1. 用户信息表
-- =====================================================
CREATE TABLE IF NOT EXISTS `user_info` (
                                           `user_id` VARCHAR(12) NOT NULL COMMENT '用户ID，12位字符串',
    `email` VARCHAR(100) NOT NULL COMMENT '邮箱，唯一',
    `nick_name` VARCHAR(50) NOT NULL COMMENT '昵称',
    `sex` TINYINT DEFAULT 2 COMMENT '性别：0女，1男，2保密',
    `password` VARCHAR(64) NOT NULL COMMENT '密码（MD5加密）',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0禁用，1启用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `last_login_time` BIGINT DEFAULT NULL COMMENT '最后登录时间戳',
    `last_off_time` BIGINT DEFAULT NULL COMMENT '最后离开时间戳',
    `meeting_no` VARCHAR(10) DEFAULT NULL COMMENT '个人会议号，10位字符串',
    PRIMARY KEY (`user_id`),
    UNIQUE KEY `uk_email` (`email`),
    UNIQUE KEY `uk_meeting_no` (`meeting_no`),
    KEY `idx_status` (`status`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户信息表';

-- =====================================================
-- 2. 会议信息表
-- =====================================================
CREATE TABLE IF NOT EXISTS `meeting_info` (
                                              `meeting_id` VARCHAR(10) NOT NULL COMMENT '会议ID，10位字符串',
    `meeting_no` VARCHAR(10) NOT NULL COMMENT '会议号',
    `meeting_name` VARCHAR(100) DEFAULT NULL COMMENT '会议名称',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_user_id` VARCHAR(12) NOT NULL COMMENT '创建用户ID',
    `join_type` TINYINT DEFAULT 0 COMMENT '加入类型：0自由加入，1需要密码',
    `join_password` VARCHAR(20) DEFAULT NULL COMMENT '加入密码',
    `start_time` DATETIME DEFAULT NULL COMMENT '开始时间',
    `end_time` DATETIME DEFAULT NULL COMMENT '结束时间',
    `status` TINYINT DEFAULT 0 COMMENT '状态：0进行中，1已结束',
    PRIMARY KEY (`meeting_id`),
    KEY `idx_meeting_no` (`meeting_no`),
    KEY `idx_create_user_id` (`create_user_id`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会议信息表';

-- =====================================================
-- 3. 会议成员表
-- =====================================================
CREATE TABLE IF NOT EXISTS `meeting_member` (
                                                `meeting_id` VARCHAR(10) NOT NULL COMMENT '会议ID',
    `user_id` VARCHAR(12) NOT NULL COMMENT '用户ID',
    `nick_name` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    `last_join_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '最后加入时间',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0离开，1在线',
    `member_type` TINYINT DEFAULT 0 COMMENT '成员类型：0普通成员，1主持人',
    `meeting_status` TINYINT DEFAULT 0 COMMENT '会议状态',
    PRIMARY KEY (`meeting_id`, `user_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会议成员表';

-- =====================================================
-- 4. 预约会议表
-- =====================================================
CREATE TABLE IF NOT EXISTS `meeting_reserve` (
                                                 `meeting_id` VARCHAR(10) NOT NULL COMMENT '会议ID，10位字符串',
    `meeting_name` VARCHAR(100) NOT NULL COMMENT '会议名称',
    `join_type` TINYINT DEFAULT 0 COMMENT '加入类型：0自由加入，1需要密码',
    `join_password` VARCHAR(20) DEFAULT NULL COMMENT '加入密码',
    `duration` INT DEFAULT 60 COMMENT '会议时长（分钟）',
    `start_time` DATETIME NOT NULL COMMENT '开始时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_user_id` VARCHAR(12) NOT NULL COMMENT '创建用户ID',
    `status` TINYINT DEFAULT 0 COMMENT '状态：0待开始，1进行中，2已结束，3已取消',
    `real_meeting_id` VARCHAR(10) DEFAULT NULL COMMENT '实际会议ID',
    PRIMARY KEY (`meeting_id`),
    KEY `idx_create_user_id` (`create_user_id`),
    KEY `idx_start_time` (`start_time`),
    KEY `idx_status` (`status`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='预约会议表';

-- =====================================================
-- 5. 预约会议成员表
-- =====================================================
CREATE TABLE IF NOT EXISTS `meeting_reserve_member` (
                                                        `meeting_id` VARCHAR(10) NOT NULL COMMENT '会议ID',
    `invite_user_id` VARCHAR(12) NOT NULL COMMENT '被邀请用户ID',
    PRIMARY KEY (`meeting_id`, `invite_user_id`),
    KEY `idx_invite_user_id` (`invite_user_id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='预约会议成员表';

-- =====================================================
-- 6. 用户联系人表
-- =====================================================
CREATE TABLE IF NOT EXISTS `user_contact` (
                                              `user_id` VARCHAR(12) NOT NULL COMMENT '用户ID',
    `contact_id` VARCHAR(12) NOT NULL COMMENT '联系人ID',
    `status` TINYINT DEFAULT 0 COMMENT '状态：0正常，1拉黑',
    `last_update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    PRIMARY KEY (`user_id`, `contact_id`),
    KEY `idx_contact_id` (`contact_id`),
    KEY `idx_status` (`status`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户联系人表';

-- =====================================================
-- 7. 联系人申请表
-- =====================================================
CREATE TABLE IF NOT EXISTS `user_contact_apply` (
                                                    `apply_id` INT NOT NULL AUTO_INCREMENT COMMENT '申请ID',
                                                    `apply_user_id` VARCHAR(12) NOT NULL COMMENT '申请用户ID',
    `receive_user_id` VARCHAR(12) NOT NULL COMMENT '接收用户ID',
    `last_apply_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '最后申请时间',
    `status` TINYINT DEFAULT 0 COMMENT '状态：0待处理，1已同意，2已拒绝，3已拉黑',
    PRIMARY KEY (`apply_id`),
    UNIQUE KEY `uk_apply_receive` (`apply_user_id`, `receive_user_id`),
    KEY `idx_receive_user_id` (`receive_user_id`),
    KEY `idx_status` (`status`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='联系人申请表';

-- =====================================================
-- 8. 私聊未读消息表
-- =====================================================
CREATE TABLE IF NOT EXISTS `private_chat_unread` (
                                                     `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                                     `user_id` VARCHAR(12) NOT NULL COMMENT '用户ID（消息接收者）',
    `contact_id` VARCHAR(12) NOT NULL COMMENT '联系人ID（消息发送者）',
    `unread_count` INT DEFAULT 0 COMMENT '未读消息数',
    `last_message_time` BIGINT DEFAULT NULL COMMENT '最后消息时间戳',
    `last_message_content` VARCHAR(200) DEFAULT NULL COMMENT '最后消息内容预览',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_contact` (`user_id`, `contact_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_last_message_time` (`last_message_time`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='私聊未读消息表';


-- =====================================================
-- 9. 会议聊天消息分表 (01-32)
-- 使用 meeting_id 作为分表键
-- =====================================================

-- 创建分表的存储过程
DELIMITER //
CREATE PROCEDURE IF NOT EXISTS create_chat_message_tables()
BEGIN
    DECLARE i INT DEFAULT 1;
    DECLARE table_suffix VARCHAR(2);
    DECLARE create_sql TEXT;

    WHILE i <= 32 DO
        SET table_suffix = LPAD(i, 2, '0');
        SET create_sql = CONCAT('
            CREATE TABLE IF NOT EXISTS `message_chat_message_', table_suffix, '` (
                `message_id` BIGINT NOT NULL COMMENT ''消息ID，雪花算法生成'',
                `meeting_id` VARCHAR(10) NOT NULL COMMENT ''会议ID，分表键'',
                `message_type` TINYINT NOT NULL COMMENT ''消息类型：5文本，6媒体'',
                `message_content` VARCHAR(500) DEFAULT NULL COMMENT ''消息内容'',
                `send_user_id` VARCHAR(12) NOT NULL COMMENT ''发送者用户ID'',
                `send_user_nick_name` VARCHAR(50) DEFAULT NULL COMMENT ''发送者昵称'',
                `send_time` BIGINT NOT NULL COMMENT ''发送时间戳（毫秒）'',
                `receive_type` TINYINT DEFAULT 0 COMMENT ''接收类型：0群发，1私聊'',
                `receive_user_id` VARCHAR(12) DEFAULT NULL COMMENT ''接收者用户ID'',
                `file_size` BIGINT DEFAULT NULL COMMENT ''文件大小（字节）'',
                `file_name` VARCHAR(200) DEFAULT NULL COMMENT ''文件名'',
                `file_type` TINYINT DEFAULT NULL COMMENT ''文件类型'',
                `file_suffix` VARCHAR(20) DEFAULT NULL COMMENT ''文件后缀'',
                `status` TINYINT DEFAULT 1 COMMENT ''状态：0发送中，1已发送'',
                PRIMARY KEY (`message_id`),
                KEY `idx_meeting_id` (`meeting_id`),
                KEY `idx_send_time` (`send_time`),
                KEY `idx_send_user_id` (`send_user_id`)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT=''会议聊天消息分表', table_suffix, ''';
        ');

        SET @sql = create_sql;
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET i = i + 1;
END WHILE;
END //
DELIMITER ;

-- 执行创建分表
CALL create_chat_message_tables();

-- =====================================================
-- 10. 私聊消息分表 (01-32)
-- 使用 session_id 作为分表键
-- =====================================================

DELIMITER //
CREATE PROCEDURE IF NOT EXISTS create_private_chat_message_tables()
BEGIN
    DECLARE i INT DEFAULT 1;
    DECLARE table_suffix VARCHAR(2);
    DECLARE create_sql TEXT;

    WHILE i <= 32 DO
        SET table_suffix = LPAD(i, 2, '0');
        SET create_sql = CONCAT('
            CREATE TABLE IF NOT EXISTS `private_chat_message_', table_suffix, '` (
                `message_id` BIGINT NOT NULL COMMENT ''消息ID，雪花算法生成'',
                `session_id` VARCHAR(25) NOT NULL COMMENT ''会话ID，分表键'',
                `message_type` TINYINT NOT NULL COMMENT ''消息类型：5文本，6媒体'',
                `message_content` VARCHAR(500) DEFAULT NULL COMMENT ''消息内容'',
                `send_user_id` VARCHAR(12) NOT NULL COMMENT ''发送者用户ID'',
                `send_user_nick_name` VARCHAR(50) DEFAULT NULL COMMENT ''发送者昵称'',
                `receive_user_id` VARCHAR(12) NOT NULL COMMENT ''接收者用户ID'',
                `send_time` BIGINT NOT NULL COMMENT ''发送时间戳（毫秒）'',
                `file_size` BIGINT DEFAULT NULL COMMENT ''文件大小（字节）'',
                `file_name` VARCHAR(200) DEFAULT NULL COMMENT ''文件名'',
                `file_type` TINYINT DEFAULT NULL COMMENT ''文件类型'',
                `file_suffix` VARCHAR(20) DEFAULT NULL COMMENT ''文件后缀'',
                `status` TINYINT DEFAULT 1 COMMENT ''状态：0发送中，1已发送'',
                PRIMARY KEY (`message_id`),
                KEY `idx_session_id` (`session_id`),
                KEY `idx_send_time` (`send_time`),
                KEY `idx_send_user_id` (`send_user_id`),
                KEY `idx_receive_user_id` (`receive_user_id`)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT=''私聊消息分表', table_suffix, ''';
        ');

        SET @sql = create_sql;
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET i = i + 1;
END WHILE;
END //
DELIMITER ;

-- 执行创建分表
CALL create_private_chat_message_tables();

-- =====================================================
-- 清理存储过程
-- =====================================================
DROP PROCEDURE IF EXISTS create_chat_message_tables;
DROP PROCEDURE IF EXISTS create_private_chat_message_tables;

-- =====================================================
-- 插入测试管理员账号（可选）
-- 密码: 123456 (MD5: e10adc3949ba59abbe56e057f20f883e)
-- =====================================================
-- INSERT INTO `user_info` (`user_id`, `email`, `nick_name`, `sex`, `password`, `status`, `meeting_no`)
-- VALUES ('100000000001', 'admin@easymeeting.com', '管理员', 2, 'e10adc3949ba59abbe56e057f20f883e', 1, '1000000001');

-- =====================================================
-- 初始化完成
-- =====================================================
SELECT 'EasyMeeting 数据库初始化完成！' AS message;