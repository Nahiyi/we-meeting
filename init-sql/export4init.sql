/*
 Navicat Premium Dump SQL

 Source Server         : localhost-wsl
 Source Server Type    : MySQL
 Source Server Version : 80044 (8.0.44)
 Source Host           : localhost:3306
 Source Schema         : easymeeting

 Target Server Type    : MySQL
 Target Server Version : 80044 (8.0.44)
 File Encoding         : 65001

 Date: 28/01/2026 12:38:55
*/

SET NAMES utf8mb4;
SET
FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for meeting_info
-- ----------------------------
DROP TABLE IF EXISTS `meeting_info`;
CREATE TABLE `meeting_info`
(
    `meeting_id`     varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会议ID，10位字符串',
    `meeting_no`     varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会议号',
    `meeting_name`   varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '会议名称',
    `create_time`    datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_user_id` varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '创建用户ID',
    `join_type`      tinyint NULL DEFAULT 0 COMMENT '加入类型：0自由加入，1需要密码',
    `join_password`  varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '加入密码',
    `start_time`     datetime NULL DEFAULT NULL COMMENT '开始时间',
    `end_time`       datetime NULL DEFAULT NULL COMMENT '结束时间',
    `status`         tinyint NULL DEFAULT 0 COMMENT '状态：0进行中，1已结束',
    PRIMARY KEY (`meeting_id`) USING BTREE,
    INDEX            `idx_meeting_no`(`meeting_no` ASC) USING BTREE,
    INDEX            `idx_create_user_id`(`create_user_id` ASC) USING BTREE,
    INDEX            `idx_status`(`status` ASC) USING BTREE,
    INDEX            `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '会议信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for meeting_member
-- ----------------------------
DROP TABLE IF EXISTS `meeting_member`;
CREATE TABLE `meeting_member`
(
    `meeting_id`     varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会议ID',
    `user_id`        varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户ID',
    `nick_name`      varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '昵称',
    `last_join_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最后加入时间',
    `status`         tinyint NULL DEFAULT 1 COMMENT '状态：0离开，1在线',
    `member_type`    tinyint NULL DEFAULT 0 COMMENT '成员类型：0普通成员，1主持人',
    `meeting_status` tinyint NULL DEFAULT 0 COMMENT '会议状态',
    PRIMARY KEY (`meeting_id`, `user_id`) USING BTREE,
    INDEX            `idx_user_id`(`user_id` ASC) USING BTREE,
    INDEX            `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '会议成员表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for meeting_reserve
-- ----------------------------
DROP TABLE IF EXISTS `meeting_reserve`;
CREATE TABLE `meeting_reserve`
(
    `meeting_id`      varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NOT NULL COMMENT '会议预约记录ID，10位字符串',
    `meeting_name`    varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会议名称',
    `join_type`       tinyint NULL DEFAULT 0 COMMENT '加入类型：0自由加入，1需要密码',
    `join_password`   varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '加入密码',
    `duration`        int NULL DEFAULT 60 COMMENT '会议时长（分钟）',
    `start_time`      datetime                                                      NOT NULL COMMENT '开始时间',
    `create_time`     datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_user_id`  varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NOT NULL COMMENT '创建用户ID',
    `status`          tinyint NULL DEFAULT 0 COMMENT '状态：0待开始，1进行中，2已结束，3已取消',
    `real_meeting_id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '实际会议ID',
    PRIMARY KEY (`meeting_id`) USING BTREE,
    UNIQUE INDEX `idx_real_meeting_id`(`real_meeting_id` ASC) USING BTREE COMMENT '一条会议预约表的记录对应的唯一一个会议的会议ID上的索引',
    INDEX             `idx_create_user_id`(`create_user_id` ASC) USING BTREE,
    INDEX             `idx_start_time`(`start_time` ASC) USING BTREE,
    INDEX             `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '预约会议表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for meeting_reserve_member
-- ----------------------------
DROP TABLE IF EXISTS `meeting_reserve_member`;
CREATE TABLE `meeting_reserve_member`
(
    `meeting_id`     varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会议ID',
    `invite_user_id` varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '被邀请用户ID',
    PRIMARY KEY (`meeting_id`, `invite_user_id`) USING BTREE,
    INDEX            `idx_invite_user_id`(`invite_user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '预约会议成员表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for message_chat_message_01
-- ----------------------------
DROP TABLE IF EXISTS `message_chat_message_01`;
CREATE TABLE `message_chat_message_01`
(
    `message_id`          bigint                                                       NOT NULL COMMENT '消息ID，雪花算法生成',
    `meeting_id`          varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会议ID，分表键',
    `message_type`        tinyint                                                      NOT NULL COMMENT '消息类型：5文本，6媒体',
    `message_content`     varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '消息内容',
    `send_user_id`        varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '发送者用户ID',
    `send_user_nick_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '发送者昵称',
    `send_time`           bigint                                                       NOT NULL COMMENT '发送时间戳（毫秒）',
    `receive_type`        tinyint NULL DEFAULT 0 COMMENT '接收类型：0群发，1私聊',
    `receive_user_id`     varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '接收者用户ID',
    `file_size`           bigint NULL DEFAULT NULL COMMENT '文件大小（字节）',
    `file_name`           varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件名',
    `file_type`           tinyint NULL DEFAULT NULL COMMENT '文件类型',
    `file_suffix`         varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件后缀',
    `status`              tinyint NULL DEFAULT 1 COMMENT '状态：0发送中，1已发送',
    PRIMARY KEY (`message_id`) USING BTREE,
    INDEX                 `idx_meeting_id`(`meeting_id` ASC) USING BTREE,
    INDEX                 `idx_send_time`(`send_time` ASC) USING BTREE,
    INDEX                 `idx_send_user_id`(`send_user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '会议聊天消息分表01' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for message_chat_message_02
-- ----------------------------
DROP TABLE IF EXISTS `message_chat_message_02`;
CREATE TABLE `message_chat_message_02`
(
    `message_id`          bigint                                                       NOT NULL COMMENT '消息ID，雪花算法生成',
    `meeting_id`          varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会议ID，分表键',
    `message_type`        tinyint                                                      NOT NULL COMMENT '消息类型：5文本，6媒体',
    `message_content`     varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '消息内容',
    `send_user_id`        varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '发送者用户ID',
    `send_user_nick_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '发送者昵称',
    `send_time`           bigint                                                       NOT NULL COMMENT '发送时间戳（毫秒）',
    `receive_type`        tinyint NULL DEFAULT 0 COMMENT '接收类型：0群发，1私聊',
    `receive_user_id`     varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '接收者用户ID',
    `file_size`           bigint NULL DEFAULT NULL COMMENT '文件大小（字节）',
    `file_name`           varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件名',
    `file_type`           tinyint NULL DEFAULT NULL COMMENT '文件类型',
    `file_suffix`         varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件后缀',
    `status`              tinyint NULL DEFAULT 1 COMMENT '状态：0发送中，1已发送',
    PRIMARY KEY (`message_id`) USING BTREE,
    INDEX                 `idx_meeting_id`(`meeting_id` ASC) USING BTREE,
    INDEX                 `idx_send_time`(`send_time` ASC) USING BTREE,
    INDEX                 `idx_send_user_id`(`send_user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '会议聊天消息分表02' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for message_chat_message_03
-- ----------------------------
DROP TABLE IF EXISTS `message_chat_message_03`;
CREATE TABLE `message_chat_message_03`
(
    `message_id`          bigint                                                       NOT NULL COMMENT '消息ID，雪花算法生成',
    `meeting_id`          varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会议ID，分表键',
    `message_type`        tinyint                                                      NOT NULL COMMENT '消息类型：5文本，6媒体',
    `message_content`     varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '消息内容',
    `send_user_id`        varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '发送者用户ID',
    `send_user_nick_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '发送者昵称',
    `send_time`           bigint                                                       NOT NULL COMMENT '发送时间戳（毫秒）',
    `receive_type`        tinyint NULL DEFAULT 0 COMMENT '接收类型：0群发，1私聊',
    `receive_user_id`     varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '接收者用户ID',
    `file_size`           bigint NULL DEFAULT NULL COMMENT '文件大小（字节）',
    `file_name`           varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件名',
    `file_type`           tinyint NULL DEFAULT NULL COMMENT '文件类型',
    `file_suffix`         varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件后缀',
    `status`              tinyint NULL DEFAULT 1 COMMENT '状态：0发送中，1已发送',
    PRIMARY KEY (`message_id`) USING BTREE,
    INDEX                 `idx_meeting_id`(`meeting_id` ASC) USING BTREE,
    INDEX                 `idx_send_time`(`send_time` ASC) USING BTREE,
    INDEX                 `idx_send_user_id`(`send_user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '会议聊天消息分表03' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for message_chat_message_04
-- ----------------------------
DROP TABLE IF EXISTS `message_chat_message_04`;
CREATE TABLE `message_chat_message_04`
(
    `message_id`          bigint                                                       NOT NULL COMMENT '消息ID，雪花算法生成',
    `meeting_id`          varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会议ID，分表键',
    `message_type`        tinyint                                                      NOT NULL COMMENT '消息类型：5文本，6媒体',
    `message_content`     varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '消息内容',
    `send_user_id`        varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '发送者用户ID',
    `send_user_nick_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '发送者昵称',
    `send_time`           bigint                                                       NOT NULL COMMENT '发送时间戳（毫秒）',
    `receive_type`        tinyint NULL DEFAULT 0 COMMENT '接收类型：0群发，1私聊',
    `receive_user_id`     varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '接收者用户ID',
    `file_size`           bigint NULL DEFAULT NULL COMMENT '文件大小（字节）',
    `file_name`           varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件名',
    `file_type`           tinyint NULL DEFAULT NULL COMMENT '文件类型',
    `file_suffix`         varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件后缀',
    `status`              tinyint NULL DEFAULT 1 COMMENT '状态：0发送中，1已发送',
    PRIMARY KEY (`message_id`) USING BTREE,
    INDEX                 `idx_meeting_id`(`meeting_id` ASC) USING BTREE,
    INDEX                 `idx_send_time`(`send_time` ASC) USING BTREE,
    INDEX                 `idx_send_user_id`(`send_user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '会议聊天消息分表04' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for message_chat_message_05
-- ----------------------------
DROP TABLE IF EXISTS `message_chat_message_05`;
CREATE TABLE `message_chat_message_05`
(
    `message_id`          bigint                                                       NOT NULL COMMENT '消息ID，雪花算法生成',
    `meeting_id`          varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会议ID，分表键',
    `message_type`        tinyint                                                      NOT NULL COMMENT '消息类型：5文本，6媒体',
    `message_content`     varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '消息内容',
    `send_user_id`        varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '发送者用户ID',
    `send_user_nick_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '发送者昵称',
    `send_time`           bigint                                                       NOT NULL COMMENT '发送时间戳（毫秒）',
    `receive_type`        tinyint NULL DEFAULT 0 COMMENT '接收类型：0群发，1私聊',
    `receive_user_id`     varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '接收者用户ID',
    `file_size`           bigint NULL DEFAULT NULL COMMENT '文件大小（字节）',
    `file_name`           varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件名',
    `file_type`           tinyint NULL DEFAULT NULL COMMENT '文件类型',
    `file_suffix`         varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件后缀',
    `status`              tinyint NULL DEFAULT 1 COMMENT '状态：0发送中，1已发送',
    PRIMARY KEY (`message_id`) USING BTREE,
    INDEX                 `idx_meeting_id`(`meeting_id` ASC) USING BTREE,
    INDEX                 `idx_send_time`(`send_time` ASC) USING BTREE,
    INDEX                 `idx_send_user_id`(`send_user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '会议聊天消息分表05' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for message_chat_message_06
-- ----------------------------
DROP TABLE IF EXISTS `message_chat_message_06`;
CREATE TABLE `message_chat_message_06`
(
    `message_id`          bigint                                                       NOT NULL COMMENT '消息ID，雪花算法生成',
    `meeting_id`          varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会议ID，分表键',
    `message_type`        tinyint                                                      NOT NULL COMMENT '消息类型：5文本，6媒体',
    `message_content`     varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '消息内容',
    `send_user_id`        varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '发送者用户ID',
    `send_user_nick_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '发送者昵称',
    `send_time`           bigint                                                       NOT NULL COMMENT '发送时间戳（毫秒）',
    `receive_type`        tinyint NULL DEFAULT 0 COMMENT '接收类型：0群发，1私聊',
    `receive_user_id`     varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '接收者用户ID',
    `file_size`           bigint NULL DEFAULT NULL COMMENT '文件大小（字节）',
    `file_name`           varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件名',
    `file_type`           tinyint NULL DEFAULT NULL COMMENT '文件类型',
    `file_suffix`         varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件后缀',
    `status`              tinyint NULL DEFAULT 1 COMMENT '状态：0发送中，1已发送',
    PRIMARY KEY (`message_id`) USING BTREE,
    INDEX                 `idx_meeting_id`(`meeting_id` ASC) USING BTREE,
    INDEX                 `idx_send_time`(`send_time` ASC) USING BTREE,
    INDEX                 `idx_send_user_id`(`send_user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '会议聊天消息分表06' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for message_chat_message_07
-- ----------------------------
DROP TABLE IF EXISTS `message_chat_message_07`;
CREATE TABLE `message_chat_message_07`
(
    `message_id`          bigint                                                       NOT NULL COMMENT '消息ID，雪花算法生成',
    `meeting_id`          varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会议ID，分表键',
    `message_type`        tinyint                                                      NOT NULL COMMENT '消息类型：5文本，6媒体',
    `message_content`     varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '消息内容',
    `send_user_id`        varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '发送者用户ID',
    `send_user_nick_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '发送者昵称',
    `send_time`           bigint                                                       NOT NULL COMMENT '发送时间戳（毫秒）',
    `receive_type`        tinyint NULL DEFAULT 0 COMMENT '接收类型：0群发，1私聊',
    `receive_user_id`     varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '接收者用户ID',
    `file_size`           bigint NULL DEFAULT NULL COMMENT '文件大小（字节）',
    `file_name`           varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件名',
    `file_type`           tinyint NULL DEFAULT NULL COMMENT '文件类型',
    `file_suffix`         varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件后缀',
    `status`              tinyint NULL DEFAULT 1 COMMENT '状态：0发送中，1已发送',
    PRIMARY KEY (`message_id`) USING BTREE,
    INDEX                 `idx_meeting_id`(`meeting_id` ASC) USING BTREE,
    INDEX                 `idx_send_time`(`send_time` ASC) USING BTREE,
    INDEX                 `idx_send_user_id`(`send_user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '会议聊天消息分表07' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for message_chat_message_08
-- ----------------------------
DROP TABLE IF EXISTS `message_chat_message_08`;
CREATE TABLE `message_chat_message_08`
(
    `message_id`          bigint                                                       NOT NULL COMMENT '消息ID，雪花算法生成',
    `meeting_id`          varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会议ID，分表键',
    `message_type`        tinyint                                                      NOT NULL COMMENT '消息类型：5文本，6媒体',
    `message_content`     varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '消息内容',
    `send_user_id`        varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '发送者用户ID',
    `send_user_nick_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '发送者昵称',
    `send_time`           bigint                                                       NOT NULL COMMENT '发送时间戳（毫秒）',
    `receive_type`        tinyint NULL DEFAULT 0 COMMENT '接收类型：0群发，1私聊',
    `receive_user_id`     varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '接收者用户ID',
    `file_size`           bigint NULL DEFAULT NULL COMMENT '文件大小（字节）',
    `file_name`           varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件名',
    `file_type`           tinyint NULL DEFAULT NULL COMMENT '文件类型',
    `file_suffix`         varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件后缀',
    `status`              tinyint NULL DEFAULT 1 COMMENT '状态：0发送中，1已发送',
    PRIMARY KEY (`message_id`) USING BTREE,
    INDEX                 `idx_meeting_id`(`meeting_id` ASC) USING BTREE,
    INDEX                 `idx_send_time`(`send_time` ASC) USING BTREE,
    INDEX                 `idx_send_user_id`(`send_user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '会议聊天消息分表08' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for message_chat_message_09
-- ----------------------------
DROP TABLE IF EXISTS `message_chat_message_09`;
CREATE TABLE `message_chat_message_09`
(
    `message_id`          bigint                                                       NOT NULL COMMENT '消息ID，雪花算法生成',
    `meeting_id`          varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会议ID，分表键',
    `message_type`        tinyint                                                      NOT NULL COMMENT '消息类型：5文本，6媒体',
    `message_content`     varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '消息内容',
    `send_user_id`        varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '发送者用户ID',
    `send_user_nick_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '发送者昵称',
    `send_time`           bigint                                                       NOT NULL COMMENT '发送时间戳（毫秒）',
    `receive_type`        tinyint NULL DEFAULT 0 COMMENT '接收类型：0群发，1私聊',
    `receive_user_id`     varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '接收者用户ID',
    `file_size`           bigint NULL DEFAULT NULL COMMENT '文件大小（字节）',
    `file_name`           varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件名',
    `file_type`           tinyint NULL DEFAULT NULL COMMENT '文件类型',
    `file_suffix`         varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件后缀',
    `status`              tinyint NULL DEFAULT 1 COMMENT '状态：0发送中，1已发送',
    PRIMARY KEY (`message_id`) USING BTREE,
    INDEX                 `idx_meeting_id`(`meeting_id` ASC) USING BTREE,
    INDEX                 `idx_send_time`(`send_time` ASC) USING BTREE,
    INDEX                 `idx_send_user_id`(`send_user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '会议聊天消息分表09' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for message_chat_message_10
-- ----------------------------
DROP TABLE IF EXISTS `message_chat_message_10`;
CREATE TABLE `message_chat_message_10`
(
    `message_id`          bigint                                                       NOT NULL COMMENT '消息ID，雪花算法生成',
    `meeting_id`          varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会议ID，分表键',
    `message_type`        tinyint                                                      NOT NULL COMMENT '消息类型：5文本，6媒体',
    `message_content`     varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '消息内容',
    `send_user_id`        varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '发送者用户ID',
    `send_user_nick_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '发送者昵称',
    `send_time`           bigint                                                       NOT NULL COMMENT '发送时间戳（毫秒）',
    `receive_type`        tinyint NULL DEFAULT 0 COMMENT '接收类型：0群发，1私聊',
    `receive_user_id`     varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '接收者用户ID',
    `file_size`           bigint NULL DEFAULT NULL COMMENT '文件大小（字节）',
    `file_name`           varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件名',
    `file_type`           tinyint NULL DEFAULT NULL COMMENT '文件类型',
    `file_suffix`         varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件后缀',
    `status`              tinyint NULL DEFAULT 1 COMMENT '状态：0发送中，1已发送',
    PRIMARY KEY (`message_id`) USING BTREE,
    INDEX                 `idx_meeting_id`(`meeting_id` ASC) USING BTREE,
    INDEX                 `idx_send_time`(`send_time` ASC) USING BTREE,
    INDEX                 `idx_send_user_id`(`send_user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '会议聊天消息分表10' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for message_chat_message_11
-- ----------------------------
DROP TABLE IF EXISTS `message_chat_message_11`;
CREATE TABLE `message_chat_message_11`
(
    `message_id`          bigint                                                       NOT NULL COMMENT '消息ID，雪花算法生成',
    `meeting_id`          varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会议ID，分表键',
    `message_type`        tinyint                                                      NOT NULL COMMENT '消息类型：5文本，6媒体',
    `message_content`     varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '消息内容',
    `send_user_id`        varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '发送者用户ID',
    `send_user_nick_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '发送者昵称',
    `send_time`           bigint                                                       NOT NULL COMMENT '发送时间戳（毫秒）',
    `receive_type`        tinyint NULL DEFAULT 0 COMMENT '接收类型：0群发，1私聊',
    `receive_user_id`     varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '接收者用户ID',
    `file_size`           bigint NULL DEFAULT NULL COMMENT '文件大小（字节）',
    `file_name`           varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件名',
    `file_type`           tinyint NULL DEFAULT NULL COMMENT '文件类型',
    `file_suffix`         varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件后缀',
    `status`              tinyint NULL DEFAULT 1 COMMENT '状态：0发送中，1已发送',
    PRIMARY KEY (`message_id`) USING BTREE,
    INDEX                 `idx_meeting_id`(`meeting_id` ASC) USING BTREE,
    INDEX                 `idx_send_time`(`send_time` ASC) USING BTREE,
    INDEX                 `idx_send_user_id`(`send_user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '会议聊天消息分表11' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for message_chat_message_12
-- ----------------------------
DROP TABLE IF EXISTS `message_chat_message_12`;
CREATE TABLE `message_chat_message_12`
(
    `message_id`          bigint                                                       NOT NULL COMMENT '消息ID，雪花算法生成',
    `meeting_id`          varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会议ID，分表键',
    `message_type`        tinyint                                                      NOT NULL COMMENT '消息类型：5文本，6媒体',
    `message_content`     varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '消息内容',
    `send_user_id`        varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '发送者用户ID',
    `send_user_nick_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '发送者昵称',
    `send_time`           bigint                                                       NOT NULL COMMENT '发送时间戳（毫秒）',
    `receive_type`        tinyint NULL DEFAULT 0 COMMENT '接收类型：0群发，1私聊',
    `receive_user_id`     varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '接收者用户ID',
    `file_size`           bigint NULL DEFAULT NULL COMMENT '文件大小（字节）',
    `file_name`           varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件名',
    `file_type`           tinyint NULL DEFAULT NULL COMMENT '文件类型',
    `file_suffix`         varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件后缀',
    `status`              tinyint NULL DEFAULT 1 COMMENT '状态：0发送中，1已发送',
    PRIMARY KEY (`message_id`) USING BTREE,
    INDEX                 `idx_meeting_id`(`meeting_id` ASC) USING BTREE,
    INDEX                 `idx_send_time`(`send_time` ASC) USING BTREE,
    INDEX                 `idx_send_user_id`(`send_user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '会议聊天消息分表12' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for message_chat_message_13
-- ----------------------------
DROP TABLE IF EXISTS `message_chat_message_13`;
CREATE TABLE `message_chat_message_13`
(
    `message_id`          bigint                                                       NOT NULL COMMENT '消息ID，雪花算法生成',
    `meeting_id`          varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会议ID，分表键',
    `message_type`        tinyint                                                      NOT NULL COMMENT '消息类型：5文本，6媒体',
    `message_content`     varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '消息内容',
    `send_user_id`        varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '发送者用户ID',
    `send_user_nick_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '发送者昵称',
    `send_time`           bigint                                                       NOT NULL COMMENT '发送时间戳（毫秒）',
    `receive_type`        tinyint NULL DEFAULT 0 COMMENT '接收类型：0群发，1私聊',
    `receive_user_id`     varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '接收者用户ID',
    `file_size`           bigint NULL DEFAULT NULL COMMENT '文件大小（字节）',
    `file_name`           varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件名',
    `file_type`           tinyint NULL DEFAULT NULL COMMENT '文件类型',
    `file_suffix`         varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件后缀',
    `status`              tinyint NULL DEFAULT 1 COMMENT '状态：0发送中，1已发送',
    PRIMARY KEY (`message_id`) USING BTREE,
    INDEX                 `idx_meeting_id`(`meeting_id` ASC) USING BTREE,
    INDEX                 `idx_send_time`(`send_time` ASC) USING BTREE,
    INDEX                 `idx_send_user_id`(`send_user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '会议聊天消息分表13' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for message_chat_message_14
-- ----------------------------
DROP TABLE IF EXISTS `message_chat_message_14`;
CREATE TABLE `message_chat_message_14`
(
    `message_id`          bigint                                                       NOT NULL COMMENT '消息ID，雪花算法生成',
    `meeting_id`          varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会议ID，分表键',
    `message_type`        tinyint                                                      NOT NULL COMMENT '消息类型：5文本，6媒体',
    `message_content`     varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '消息内容',
    `send_user_id`        varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '发送者用户ID',
    `send_user_nick_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '发送者昵称',
    `send_time`           bigint                                                       NOT NULL COMMENT '发送时间戳（毫秒）',
    `receive_type`        tinyint NULL DEFAULT 0 COMMENT '接收类型：0群发，1私聊',
    `receive_user_id`     varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '接收者用户ID',
    `file_size`           bigint NULL DEFAULT NULL COMMENT '文件大小（字节）',
    `file_name`           varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件名',
    `file_type`           tinyint NULL DEFAULT NULL COMMENT '文件类型',
    `file_suffix`         varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件后缀',
    `status`              tinyint NULL DEFAULT 1 COMMENT '状态：0发送中，1已发送',
    PRIMARY KEY (`message_id`) USING BTREE,
    INDEX                 `idx_meeting_id`(`meeting_id` ASC) USING BTREE,
    INDEX                 `idx_send_time`(`send_time` ASC) USING BTREE,
    INDEX                 `idx_send_user_id`(`send_user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '会议聊天消息分表14' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for message_chat_message_15
-- ----------------------------
DROP TABLE IF EXISTS `message_chat_message_15`;
CREATE TABLE `message_chat_message_15`
(
    `message_id`          bigint                                                       NOT NULL COMMENT '消息ID，雪花算法生成',
    `meeting_id`          varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会议ID，分表键',
    `message_type`        tinyint                                                      NOT NULL COMMENT '消息类型：5文本，6媒体',
    `message_content`     varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '消息内容',
    `send_user_id`        varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '发送者用户ID',
    `send_user_nick_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '发送者昵称',
    `send_time`           bigint                                                       NOT NULL COMMENT '发送时间戳（毫秒）',
    `receive_type`        tinyint NULL DEFAULT 0 COMMENT '接收类型：0群发，1私聊',
    `receive_user_id`     varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '接收者用户ID',
    `file_size`           bigint NULL DEFAULT NULL COMMENT '文件大小（字节）',
    `file_name`           varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件名',
    `file_type`           tinyint NULL DEFAULT NULL COMMENT '文件类型',
    `file_suffix`         varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件后缀',
    `status`              tinyint NULL DEFAULT 1 COMMENT '状态：0发送中，1已发送',
    PRIMARY KEY (`message_id`) USING BTREE,
    INDEX                 `idx_meeting_id`(`meeting_id` ASC) USING BTREE,
    INDEX                 `idx_send_time`(`send_time` ASC) USING BTREE,
    INDEX                 `idx_send_user_id`(`send_user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '会议聊天消息分表15' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for message_chat_message_16
-- ----------------------------
DROP TABLE IF EXISTS `message_chat_message_16`;
CREATE TABLE `message_chat_message_16`
(
    `message_id`          bigint                                                       NOT NULL COMMENT '消息ID，雪花算法生成',
    `meeting_id`          varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会议ID，分表键',
    `message_type`        tinyint                                                      NOT NULL COMMENT '消息类型：5文本，6媒体',
    `message_content`     varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '消息内容',
    `send_user_id`        varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '发送者用户ID',
    `send_user_nick_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '发送者昵称',
    `send_time`           bigint                                                       NOT NULL COMMENT '发送时间戳（毫秒）',
    `receive_type`        tinyint NULL DEFAULT 0 COMMENT '接收类型：0群发，1私聊',
    `receive_user_id`     varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '接收者用户ID',
    `file_size`           bigint NULL DEFAULT NULL COMMENT '文件大小（字节）',
    `file_name`           varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件名',
    `file_type`           tinyint NULL DEFAULT NULL COMMENT '文件类型',
    `file_suffix`         varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件后缀',
    `status`              tinyint NULL DEFAULT 1 COMMENT '状态：0发送中，1已发送',
    PRIMARY KEY (`message_id`) USING BTREE,
    INDEX                 `idx_meeting_id`(`meeting_id` ASC) USING BTREE,
    INDEX                 `idx_send_time`(`send_time` ASC) USING BTREE,
    INDEX                 `idx_send_user_id`(`send_user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '会议聊天消息分表16' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for message_chat_message_17
-- ----------------------------
DROP TABLE IF EXISTS `message_chat_message_17`;
CREATE TABLE `message_chat_message_17`
(
    `message_id`          bigint                                                       NOT NULL COMMENT '消息ID，雪花算法生成',
    `meeting_id`          varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会议ID，分表键',
    `message_type`        tinyint                                                      NOT NULL COMMENT '消息类型：5文本，6媒体',
    `message_content`     varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '消息内容',
    `send_user_id`        varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '发送者用户ID',
    `send_user_nick_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '发送者昵称',
    `send_time`           bigint                                                       NOT NULL COMMENT '发送时间戳（毫秒）',
    `receive_type`        tinyint NULL DEFAULT 0 COMMENT '接收类型：0群发，1私聊',
    `receive_user_id`     varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '接收者用户ID',
    `file_size`           bigint NULL DEFAULT NULL COMMENT '文件大小（字节）',
    `file_name`           varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件名',
    `file_type`           tinyint NULL DEFAULT NULL COMMENT '文件类型',
    `file_suffix`         varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件后缀',
    `status`              tinyint NULL DEFAULT 1 COMMENT '状态：0发送中，1已发送',
    PRIMARY KEY (`message_id`) USING BTREE,
    INDEX                 `idx_meeting_id`(`meeting_id` ASC) USING BTREE,
    INDEX                 `idx_send_time`(`send_time` ASC) USING BTREE,
    INDEX                 `idx_send_user_id`(`send_user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '会议聊天消息分表17' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for message_chat_message_18
-- ----------------------------
DROP TABLE IF EXISTS `message_chat_message_18`;
CREATE TABLE `message_chat_message_18`
(
    `message_id`          bigint                                                       NOT NULL COMMENT '消息ID，雪花算法生成',
    `meeting_id`          varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会议ID，分表键',
    `message_type`        tinyint                                                      NOT NULL COMMENT '消息类型：5文本，6媒体',
    `message_content`     varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '消息内容',
    `send_user_id`        varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '发送者用户ID',
    `send_user_nick_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '发送者昵称',
    `send_time`           bigint                                                       NOT NULL COMMENT '发送时间戳（毫秒）',
    `receive_type`        tinyint NULL DEFAULT 0 COMMENT '接收类型：0群发，1私聊',
    `receive_user_id`     varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '接收者用户ID',
    `file_size`           bigint NULL DEFAULT NULL COMMENT '文件大小（字节）',
    `file_name`           varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件名',
    `file_type`           tinyint NULL DEFAULT NULL COMMENT '文件类型',
    `file_suffix`         varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件后缀',
    `status`              tinyint NULL DEFAULT 1 COMMENT '状态：0发送中，1已发送',
    PRIMARY KEY (`message_id`) USING BTREE,
    INDEX                 `idx_meeting_id`(`meeting_id` ASC) USING BTREE,
    INDEX                 `idx_send_time`(`send_time` ASC) USING BTREE,
    INDEX                 `idx_send_user_id`(`send_user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '会议聊天消息分表18' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for message_chat_message_19
-- ----------------------------
DROP TABLE IF EXISTS `message_chat_message_19`;
CREATE TABLE `message_chat_message_19`
(
    `message_id`          bigint                                                       NOT NULL COMMENT '消息ID，雪花算法生成',
    `meeting_id`          varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会议ID，分表键',
    `message_type`        tinyint                                                      NOT NULL COMMENT '消息类型：5文本，6媒体',
    `message_content`     varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '消息内容',
    `send_user_id`        varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '发送者用户ID',
    `send_user_nick_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '发送者昵称',
    `send_time`           bigint                                                       NOT NULL COMMENT '发送时间戳（毫秒）',
    `receive_type`        tinyint NULL DEFAULT 0 COMMENT '接收类型：0群发，1私聊',
    `receive_user_id`     varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '接收者用户ID',
    `file_size`           bigint NULL DEFAULT NULL COMMENT '文件大小（字节）',
    `file_name`           varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件名',
    `file_type`           tinyint NULL DEFAULT NULL COMMENT '文件类型',
    `file_suffix`         varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件后缀',
    `status`              tinyint NULL DEFAULT 1 COMMENT '状态：0发送中，1已发送',
    PRIMARY KEY (`message_id`) USING BTREE,
    INDEX                 `idx_meeting_id`(`meeting_id` ASC) USING BTREE,
    INDEX                 `idx_send_time`(`send_time` ASC) USING BTREE,
    INDEX                 `idx_send_user_id`(`send_user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '会议聊天消息分表19' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for message_chat_message_20
-- ----------------------------
DROP TABLE IF EXISTS `message_chat_message_20`;
CREATE TABLE `message_chat_message_20`
(
    `message_id`          bigint                                                       NOT NULL COMMENT '消息ID，雪花算法生成',
    `meeting_id`          varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会议ID，分表键',
    `message_type`        tinyint                                                      NOT NULL COMMENT '消息类型：5文本，6媒体',
    `message_content`     varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '消息内容',
    `send_user_id`        varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '发送者用户ID',
    `send_user_nick_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '发送者昵称',
    `send_time`           bigint                                                       NOT NULL COMMENT '发送时间戳（毫秒）',
    `receive_type`        tinyint NULL DEFAULT 0 COMMENT '接收类型：0群发，1私聊',
    `receive_user_id`     varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '接收者用户ID',
    `file_size`           bigint NULL DEFAULT NULL COMMENT '文件大小（字节）',
    `file_name`           varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件名',
    `file_type`           tinyint NULL DEFAULT NULL COMMENT '文件类型',
    `file_suffix`         varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件后缀',
    `status`              tinyint NULL DEFAULT 1 COMMENT '状态：0发送中，1已发送',
    PRIMARY KEY (`message_id`) USING BTREE,
    INDEX                 `idx_meeting_id`(`meeting_id` ASC) USING BTREE,
    INDEX                 `idx_send_time`(`send_time` ASC) USING BTREE,
    INDEX                 `idx_send_user_id`(`send_user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '会议聊天消息分表20' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for message_chat_message_21
-- ----------------------------
DROP TABLE IF EXISTS `message_chat_message_21`;
CREATE TABLE `message_chat_message_21`
(
    `message_id`          bigint                                                       NOT NULL COMMENT '消息ID，雪花算法生成',
    `meeting_id`          varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会议ID，分表键',
    `message_type`        tinyint                                                      NOT NULL COMMENT '消息类型：5文本，6媒体',
    `message_content`     varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '消息内容',
    `send_user_id`        varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '发送者用户ID',
    `send_user_nick_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '发送者昵称',
    `send_time`           bigint                                                       NOT NULL COMMENT '发送时间戳（毫秒）',
    `receive_type`        tinyint NULL DEFAULT 0 COMMENT '接收类型：0群发，1私聊',
    `receive_user_id`     varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '接收者用户ID',
    `file_size`           bigint NULL DEFAULT NULL COMMENT '文件大小（字节）',
    `file_name`           varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件名',
    `file_type`           tinyint NULL DEFAULT NULL COMMENT '文件类型',
    `file_suffix`         varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件后缀',
    `status`              tinyint NULL DEFAULT 1 COMMENT '状态：0发送中，1已发送',
    PRIMARY KEY (`message_id`) USING BTREE,
    INDEX                 `idx_meeting_id`(`meeting_id` ASC) USING BTREE,
    INDEX                 `idx_send_time`(`send_time` ASC) USING BTREE,
    INDEX                 `idx_send_user_id`(`send_user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '会议聊天消息分表21' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for message_chat_message_22
-- ----------------------------
DROP TABLE IF EXISTS `message_chat_message_22`;
CREATE TABLE `message_chat_message_22`
(
    `message_id`          bigint                                                       NOT NULL COMMENT '消息ID，雪花算法生成',
    `meeting_id`          varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会议ID，分表键',
    `message_type`        tinyint                                                      NOT NULL COMMENT '消息类型：5文本，6媒体',
    `message_content`     varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '消息内容',
    `send_user_id`        varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '发送者用户ID',
    `send_user_nick_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '发送者昵称',
    `send_time`           bigint                                                       NOT NULL COMMENT '发送时间戳（毫秒）',
    `receive_type`        tinyint NULL DEFAULT 0 COMMENT '接收类型：0群发，1私聊',
    `receive_user_id`     varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '接收者用户ID',
    `file_size`           bigint NULL DEFAULT NULL COMMENT '文件大小（字节）',
    `file_name`           varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件名',
    `file_type`           tinyint NULL DEFAULT NULL COMMENT '文件类型',
    `file_suffix`         varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件后缀',
    `status`              tinyint NULL DEFAULT 1 COMMENT '状态：0发送中，1已发送',
    PRIMARY KEY (`message_id`) USING BTREE,
    INDEX                 `idx_meeting_id`(`meeting_id` ASC) USING BTREE,
    INDEX                 `idx_send_time`(`send_time` ASC) USING BTREE,
    INDEX                 `idx_send_user_id`(`send_user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '会议聊天消息分表22' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for message_chat_message_23
-- ----------------------------
DROP TABLE IF EXISTS `message_chat_message_23`;
CREATE TABLE `message_chat_message_23`
(
    `message_id`          bigint                                                       NOT NULL COMMENT '消息ID，雪花算法生成',
    `meeting_id`          varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会议ID，分表键',
    `message_type`        tinyint                                                      NOT NULL COMMENT '消息类型：5文本，6媒体',
    `message_content`     varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '消息内容',
    `send_user_id`        varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '发送者用户ID',
    `send_user_nick_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '发送者昵称',
    `send_time`           bigint                                                       NOT NULL COMMENT '发送时间戳（毫秒）',
    `receive_type`        tinyint NULL DEFAULT 0 COMMENT '接收类型：0群发，1私聊',
    `receive_user_id`     varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '接收者用户ID',
    `file_size`           bigint NULL DEFAULT NULL COMMENT '文件大小（字节）',
    `file_name`           varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件名',
    `file_type`           tinyint NULL DEFAULT NULL COMMENT '文件类型',
    `file_suffix`         varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件后缀',
    `status`              tinyint NULL DEFAULT 1 COMMENT '状态：0发送中，1已发送',
    PRIMARY KEY (`message_id`) USING BTREE,
    INDEX                 `idx_meeting_id`(`meeting_id` ASC) USING BTREE,
    INDEX                 `idx_send_time`(`send_time` ASC) USING BTREE,
    INDEX                 `idx_send_user_id`(`send_user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '会议聊天消息分表23' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for message_chat_message_24
-- ----------------------------
DROP TABLE IF EXISTS `message_chat_message_24`;
CREATE TABLE `message_chat_message_24`
(
    `message_id`          bigint                                                       NOT NULL COMMENT '消息ID，雪花算法生成',
    `meeting_id`          varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会议ID，分表键',
    `message_type`        tinyint                                                      NOT NULL COMMENT '消息类型：5文本，6媒体',
    `message_content`     varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '消息内容',
    `send_user_id`        varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '发送者用户ID',
    `send_user_nick_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '发送者昵称',
    `send_time`           bigint                                                       NOT NULL COMMENT '发送时间戳（毫秒）',
    `receive_type`        tinyint NULL DEFAULT 0 COMMENT '接收类型：0群发，1私聊',
    `receive_user_id`     varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '接收者用户ID',
    `file_size`           bigint NULL DEFAULT NULL COMMENT '文件大小（字节）',
    `file_name`           varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件名',
    `file_type`           tinyint NULL DEFAULT NULL COMMENT '文件类型',
    `file_suffix`         varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件后缀',
    `status`              tinyint NULL DEFAULT 1 COMMENT '状态：0发送中，1已发送',
    PRIMARY KEY (`message_id`) USING BTREE,
    INDEX                 `idx_meeting_id`(`meeting_id` ASC) USING BTREE,
    INDEX                 `idx_send_time`(`send_time` ASC) USING BTREE,
    INDEX                 `idx_send_user_id`(`send_user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '会议聊天消息分表24' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for message_chat_message_25
-- ----------------------------
DROP TABLE IF EXISTS `message_chat_message_25`;
CREATE TABLE `message_chat_message_25`
(
    `message_id`          bigint                                                       NOT NULL COMMENT '消息ID，雪花算法生成',
    `meeting_id`          varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会议ID，分表键',
    `message_type`        tinyint                                                      NOT NULL COMMENT '消息类型：5文本，6媒体',
    `message_content`     varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '消息内容',
    `send_user_id`        varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '发送者用户ID',
    `send_user_nick_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '发送者昵称',
    `send_time`           bigint                                                       NOT NULL COMMENT '发送时间戳（毫秒）',
    `receive_type`        tinyint NULL DEFAULT 0 COMMENT '接收类型：0群发，1私聊',
    `receive_user_id`     varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '接收者用户ID',
    `file_size`           bigint NULL DEFAULT NULL COMMENT '文件大小（字节）',
    `file_name`           varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件名',
    `file_type`           tinyint NULL DEFAULT NULL COMMENT '文件类型',
    `file_suffix`         varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件后缀',
    `status`              tinyint NULL DEFAULT 1 COMMENT '状态：0发送中，1已发送',
    PRIMARY KEY (`message_id`) USING BTREE,
    INDEX                 `idx_meeting_id`(`meeting_id` ASC) USING BTREE,
    INDEX                 `idx_send_time`(`send_time` ASC) USING BTREE,
    INDEX                 `idx_send_user_id`(`send_user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '会议聊天消息分表25' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for message_chat_message_26
-- ----------------------------
DROP TABLE IF EXISTS `message_chat_message_26`;
CREATE TABLE `message_chat_message_26`
(
    `message_id`          bigint                                                       NOT NULL COMMENT '消息ID，雪花算法生成',
    `meeting_id`          varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会议ID，分表键',
    `message_type`        tinyint                                                      NOT NULL COMMENT '消息类型：5文本，6媒体',
    `message_content`     varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '消息内容',
    `send_user_id`        varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '发送者用户ID',
    `send_user_nick_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '发送者昵称',
    `send_time`           bigint                                                       NOT NULL COMMENT '发送时间戳（毫秒）',
    `receive_type`        tinyint NULL DEFAULT 0 COMMENT '接收类型：0群发，1私聊',
    `receive_user_id`     varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '接收者用户ID',
    `file_size`           bigint NULL DEFAULT NULL COMMENT '文件大小（字节）',
    `file_name`           varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件名',
    `file_type`           tinyint NULL DEFAULT NULL COMMENT '文件类型',
    `file_suffix`         varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件后缀',
    `status`              tinyint NULL DEFAULT 1 COMMENT '状态：0发送中，1已发送',
    PRIMARY KEY (`message_id`) USING BTREE,
    INDEX                 `idx_meeting_id`(`meeting_id` ASC) USING BTREE,
    INDEX                 `idx_send_time`(`send_time` ASC) USING BTREE,
    INDEX                 `idx_send_user_id`(`send_user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '会议聊天消息分表26' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for message_chat_message_27
-- ----------------------------
DROP TABLE IF EXISTS `message_chat_message_27`;
CREATE TABLE `message_chat_message_27`
(
    `message_id`          bigint                                                       NOT NULL COMMENT '消息ID，雪花算法生成',
    `meeting_id`          varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会议ID，分表键',
    `message_type`        tinyint                                                      NOT NULL COMMENT '消息类型：5文本，6媒体',
    `message_content`     varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '消息内容',
    `send_user_id`        varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '发送者用户ID',
    `send_user_nick_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '发送者昵称',
    `send_time`           bigint                                                       NOT NULL COMMENT '发送时间戳（毫秒）',
    `receive_type`        tinyint NULL DEFAULT 0 COMMENT '接收类型：0群发，1私聊',
    `receive_user_id`     varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '接收者用户ID',
    `file_size`           bigint NULL DEFAULT NULL COMMENT '文件大小（字节）',
    `file_name`           varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件名',
    `file_type`           tinyint NULL DEFAULT NULL COMMENT '文件类型',
    `file_suffix`         varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件后缀',
    `status`              tinyint NULL DEFAULT 1 COMMENT '状态：0发送中，1已发送',
    PRIMARY KEY (`message_id`) USING BTREE,
    INDEX                 `idx_meeting_id`(`meeting_id` ASC) USING BTREE,
    INDEX                 `idx_send_time`(`send_time` ASC) USING BTREE,
    INDEX                 `idx_send_user_id`(`send_user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '会议聊天消息分表27' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for message_chat_message_28
-- ----------------------------
DROP TABLE IF EXISTS `message_chat_message_28`;
CREATE TABLE `message_chat_message_28`
(
    `message_id`          bigint                                                       NOT NULL COMMENT '消息ID，雪花算法生成',
    `meeting_id`          varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会议ID，分表键',
    `message_type`        tinyint                                                      NOT NULL COMMENT '消息类型：5文本，6媒体',
    `message_content`     varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '消息内容',
    `send_user_id`        varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '发送者用户ID',
    `send_user_nick_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '发送者昵称',
    `send_time`           bigint                                                       NOT NULL COMMENT '发送时间戳（毫秒）',
    `receive_type`        tinyint NULL DEFAULT 0 COMMENT '接收类型：0群发，1私聊',
    `receive_user_id`     varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '接收者用户ID',
    `file_size`           bigint NULL DEFAULT NULL COMMENT '文件大小（字节）',
    `file_name`           varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件名',
    `file_type`           tinyint NULL DEFAULT NULL COMMENT '文件类型',
    `file_suffix`         varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件后缀',
    `status`              tinyint NULL DEFAULT 1 COMMENT '状态：0发送中，1已发送',
    PRIMARY KEY (`message_id`) USING BTREE,
    INDEX                 `idx_meeting_id`(`meeting_id` ASC) USING BTREE,
    INDEX                 `idx_send_time`(`send_time` ASC) USING BTREE,
    INDEX                 `idx_send_user_id`(`send_user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '会议聊天消息分表28' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for message_chat_message_29
-- ----------------------------
DROP TABLE IF EXISTS `message_chat_message_29`;
CREATE TABLE `message_chat_message_29`
(
    `message_id`          bigint                                                       NOT NULL COMMENT '消息ID，雪花算法生成',
    `meeting_id`          varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会议ID，分表键',
    `message_type`        tinyint                                                      NOT NULL COMMENT '消息类型：5文本，6媒体',
    `message_content`     varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '消息内容',
    `send_user_id`        varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '发送者用户ID',
    `send_user_nick_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '发送者昵称',
    `send_time`           bigint                                                       NOT NULL COMMENT '发送时间戳（毫秒）',
    `receive_type`        tinyint NULL DEFAULT 0 COMMENT '接收类型：0群发，1私聊',
    `receive_user_id`     varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '接收者用户ID',
    `file_size`           bigint NULL DEFAULT NULL COMMENT '文件大小（字节）',
    `file_name`           varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件名',
    `file_type`           tinyint NULL DEFAULT NULL COMMENT '文件类型',
    `file_suffix`         varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件后缀',
    `status`              tinyint NULL DEFAULT 1 COMMENT '状态：0发送中，1已发送',
    PRIMARY KEY (`message_id`) USING BTREE,
    INDEX                 `idx_meeting_id`(`meeting_id` ASC) USING BTREE,
    INDEX                 `idx_send_time`(`send_time` ASC) USING BTREE,
    INDEX                 `idx_send_user_id`(`send_user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '会议聊天消息分表29' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for message_chat_message_30
-- ----------------------------
DROP TABLE IF EXISTS `message_chat_message_30`;
CREATE TABLE `message_chat_message_30`
(
    `message_id`          bigint                                                       NOT NULL COMMENT '消息ID，雪花算法生成',
    `meeting_id`          varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会议ID，分表键',
    `message_type`        tinyint                                                      NOT NULL COMMENT '消息类型：5文本，6媒体',
    `message_content`     varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '消息内容',
    `send_user_id`        varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '发送者用户ID',
    `send_user_nick_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '发送者昵称',
    `send_time`           bigint                                                       NOT NULL COMMENT '发送时间戳（毫秒）',
    `receive_type`        tinyint NULL DEFAULT 0 COMMENT '接收类型：0群发，1私聊',
    `receive_user_id`     varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '接收者用户ID',
    `file_size`           bigint NULL DEFAULT NULL COMMENT '文件大小（字节）',
    `file_name`           varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件名',
    `file_type`           tinyint NULL DEFAULT NULL COMMENT '文件类型',
    `file_suffix`         varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件后缀',
    `status`              tinyint NULL DEFAULT 1 COMMENT '状态：0发送中，1已发送',
    PRIMARY KEY (`message_id`) USING BTREE,
    INDEX                 `idx_meeting_id`(`meeting_id` ASC) USING BTREE,
    INDEX                 `idx_send_time`(`send_time` ASC) USING BTREE,
    INDEX                 `idx_send_user_id`(`send_user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '会议聊天消息分表30' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for message_chat_message_31
-- ----------------------------
DROP TABLE IF EXISTS `message_chat_message_31`;
CREATE TABLE `message_chat_message_31`
(
    `message_id`          bigint                                                       NOT NULL COMMENT '消息ID，雪花算法生成',
    `meeting_id`          varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会议ID，分表键',
    `message_type`        tinyint                                                      NOT NULL COMMENT '消息类型：5文本，6媒体',
    `message_content`     varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '消息内容',
    `send_user_id`        varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '发送者用户ID',
    `send_user_nick_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '发送者昵称',
    `send_time`           bigint                                                       NOT NULL COMMENT '发送时间戳（毫秒）',
    `receive_type`        tinyint NULL DEFAULT 0 COMMENT '接收类型：0群发，1私聊',
    `receive_user_id`     varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '接收者用户ID',
    `file_size`           bigint NULL DEFAULT NULL COMMENT '文件大小（字节）',
    `file_name`           varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件名',
    `file_type`           tinyint NULL DEFAULT NULL COMMENT '文件类型',
    `file_suffix`         varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件后缀',
    `status`              tinyint NULL DEFAULT 1 COMMENT '状态：0发送中，1已发送',
    PRIMARY KEY (`message_id`) USING BTREE,
    INDEX                 `idx_meeting_id`(`meeting_id` ASC) USING BTREE,
    INDEX                 `idx_send_time`(`send_time` ASC) USING BTREE,
    INDEX                 `idx_send_user_id`(`send_user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '会议聊天消息分表31' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for message_chat_message_32
-- ----------------------------
DROP TABLE IF EXISTS `message_chat_message_32`;
CREATE TABLE `message_chat_message_32`
(
    `message_id`          bigint                                                       NOT NULL COMMENT '消息ID，雪花算法生成',
    `meeting_id`          varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会议ID，分表键',
    `message_type`        tinyint                                                      NOT NULL COMMENT '消息类型：5文本，6媒体',
    `message_content`     varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '消息内容',
    `send_user_id`        varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '发送者用户ID',
    `send_user_nick_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '发送者昵称',
    `send_time`           bigint                                                       NOT NULL COMMENT '发送时间戳（毫秒）',
    `receive_type`        tinyint NULL DEFAULT 0 COMMENT '接收类型：0群发，1私聊',
    `receive_user_id`     varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '接收者用户ID',
    `file_size`           bigint NULL DEFAULT NULL COMMENT '文件大小（字节）',
    `file_name`           varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件名',
    `file_type`           tinyint NULL DEFAULT NULL COMMENT '文件类型',
    `file_suffix`         varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件后缀',
    `status`              tinyint NULL DEFAULT 1 COMMENT '状态：0发送中，1已发送',
    PRIMARY KEY (`message_id`) USING BTREE,
    INDEX                 `idx_meeting_id`(`meeting_id` ASC) USING BTREE,
    INDEX                 `idx_send_time`(`send_time` ASC) USING BTREE,
    INDEX                 `idx_send_user_id`(`send_user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '会议聊天消息分表32' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for private_chat_message_01
-- ----------------------------
DROP TABLE IF EXISTS `private_chat_message_01`;
CREATE TABLE `private_chat_message_01`
(
    `message_id`          bigint                                                       NOT NULL COMMENT '消息ID，雪花算法生成',
    `session_id`          varchar(25) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会话ID，分表键',
    `message_type`        tinyint                                                      NOT NULL COMMENT '消息类型：5文本，6媒体',
    `message_content`     varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '消息内容',
    `send_user_id`        varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '发送者用户ID',
    `send_user_nick_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '发送者昵称',
    `receive_user_id`     varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '接收者用户ID',
    `send_time`           bigint                                                       NOT NULL COMMENT '发送时间戳（毫秒）',
    `file_size`           bigint NULL DEFAULT NULL COMMENT '文件大小（字节）',
    `file_name`           varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件名',
    `file_type`           tinyint NULL DEFAULT NULL COMMENT '文件类型',
    `file_suffix`         varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件后缀',
    `status`              tinyint NULL DEFAULT 1 COMMENT '状态：0发送中，1已发送',
    PRIMARY KEY (`message_id`) USING BTREE,
    INDEX                 `idx_session_id`(`session_id` ASC) USING BTREE,
    INDEX                 `idx_send_time`(`send_time` ASC) USING BTREE,
    INDEX                 `idx_send_user_id`(`send_user_id` ASC) USING BTREE,
    INDEX                 `idx_receive_user_id`(`receive_user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '私聊消息分表01' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for private_chat_message_02
-- ----------------------------
DROP TABLE IF EXISTS `private_chat_message_02`;
CREATE TABLE `private_chat_message_02`
(
    `message_id`          bigint                                                       NOT NULL COMMENT '消息ID，雪花算法生成',
    `session_id`          varchar(25) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会话ID，分表键',
    `message_type`        tinyint                                                      NOT NULL COMMENT '消息类型：5文本，6媒体',
    `message_content`     varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '消息内容',
    `send_user_id`        varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '发送者用户ID',
    `send_user_nick_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '发送者昵称',
    `receive_user_id`     varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '接收者用户ID',
    `send_time`           bigint                                                       NOT NULL COMMENT '发送时间戳（毫秒）',
    `file_size`           bigint NULL DEFAULT NULL COMMENT '文件大小（字节）',
    `file_name`           varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件名',
    `file_type`           tinyint NULL DEFAULT NULL COMMENT '文件类型',
    `file_suffix`         varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件后缀',
    `status`              tinyint NULL DEFAULT 1 COMMENT '状态：0发送中，1已发送',
    PRIMARY KEY (`message_id`) USING BTREE,
    INDEX                 `idx_session_id`(`session_id` ASC) USING BTREE,
    INDEX                 `idx_send_time`(`send_time` ASC) USING BTREE,
    INDEX                 `idx_send_user_id`(`send_user_id` ASC) USING BTREE,
    INDEX                 `idx_receive_user_id`(`receive_user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '私聊消息分表02' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for private_chat_message_03
-- ----------------------------
DROP TABLE IF EXISTS `private_chat_message_03`;
CREATE TABLE `private_chat_message_03`
(
    `message_id`          bigint                                                       NOT NULL COMMENT '消息ID，雪花算法生成',
    `session_id`          varchar(25) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会话ID，分表键',
    `message_type`        tinyint                                                      NOT NULL COMMENT '消息类型：5文本，6媒体',
    `message_content`     varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '消息内容',
    `send_user_id`        varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '发送者用户ID',
    `send_user_nick_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '发送者昵称',
    `receive_user_id`     varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '接收者用户ID',
    `send_time`           bigint                                                       NOT NULL COMMENT '发送时间戳（毫秒）',
    `file_size`           bigint NULL DEFAULT NULL COMMENT '文件大小（字节）',
    `file_name`           varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件名',
    `file_type`           tinyint NULL DEFAULT NULL COMMENT '文件类型',
    `file_suffix`         varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件后缀',
    `status`              tinyint NULL DEFAULT 1 COMMENT '状态：0发送中，1已发送',
    PRIMARY KEY (`message_id`) USING BTREE,
    INDEX                 `idx_session_id`(`session_id` ASC) USING BTREE,
    INDEX                 `idx_send_time`(`send_time` ASC) USING BTREE,
    INDEX                 `idx_send_user_id`(`send_user_id` ASC) USING BTREE,
    INDEX                 `idx_receive_user_id`(`receive_user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '私聊消息分表03' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for private_chat_message_04
-- ----------------------------
DROP TABLE IF EXISTS `private_chat_message_04`;
CREATE TABLE `private_chat_message_04`
(
    `message_id`          bigint                                                       NOT NULL COMMENT '消息ID，雪花算法生成',
    `session_id`          varchar(25) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会话ID，分表键',
    `message_type`        tinyint                                                      NOT NULL COMMENT '消息类型：5文本，6媒体',
    `message_content`     varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '消息内容',
    `send_user_id`        varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '发送者用户ID',
    `send_user_nick_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '发送者昵称',
    `receive_user_id`     varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '接收者用户ID',
    `send_time`           bigint                                                       NOT NULL COMMENT '发送时间戳（毫秒）',
    `file_size`           bigint NULL DEFAULT NULL COMMENT '文件大小（字节）',
    `file_name`           varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件名',
    `file_type`           tinyint NULL DEFAULT NULL COMMENT '文件类型',
    `file_suffix`         varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件后缀',
    `status`              tinyint NULL DEFAULT 1 COMMENT '状态：0发送中，1已发送',
    PRIMARY KEY (`message_id`) USING BTREE,
    INDEX                 `idx_session_id`(`session_id` ASC) USING BTREE,
    INDEX                 `idx_send_time`(`send_time` ASC) USING BTREE,
    INDEX                 `idx_send_user_id`(`send_user_id` ASC) USING BTREE,
    INDEX                 `idx_receive_user_id`(`receive_user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '私聊消息分表04' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for private_chat_message_05
-- ----------------------------
DROP TABLE IF EXISTS `private_chat_message_05`;
CREATE TABLE `private_chat_message_05`
(
    `message_id`          bigint                                                       NOT NULL COMMENT '消息ID，雪花算法生成',
    `session_id`          varchar(25) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会话ID，分表键',
    `message_type`        tinyint                                                      NOT NULL COMMENT '消息类型：5文本，6媒体',
    `message_content`     varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '消息内容',
    `send_user_id`        varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '发送者用户ID',
    `send_user_nick_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '发送者昵称',
    `receive_user_id`     varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '接收者用户ID',
    `send_time`           bigint                                                       NOT NULL COMMENT '发送时间戳（毫秒）',
    `file_size`           bigint NULL DEFAULT NULL COMMENT '文件大小（字节）',
    `file_name`           varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件名',
    `file_type`           tinyint NULL DEFAULT NULL COMMENT '文件类型',
    `file_suffix`         varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件后缀',
    `status`              tinyint NULL DEFAULT 1 COMMENT '状态：0发送中，1已发送',
    PRIMARY KEY (`message_id`) USING BTREE,
    INDEX                 `idx_session_id`(`session_id` ASC) USING BTREE,
    INDEX                 `idx_send_time`(`send_time` ASC) USING BTREE,
    INDEX                 `idx_send_user_id`(`send_user_id` ASC) USING BTREE,
    INDEX                 `idx_receive_user_id`(`receive_user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '私聊消息分表05' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for private_chat_message_06
-- ----------------------------
DROP TABLE IF EXISTS `private_chat_message_06`;
CREATE TABLE `private_chat_message_06`
(
    `message_id`          bigint                                                       NOT NULL COMMENT '消息ID，雪花算法生成',
    `session_id`          varchar(25) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会话ID，分表键',
    `message_type`        tinyint                                                      NOT NULL COMMENT '消息类型：5文本，6媒体',
    `message_content`     varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '消息内容',
    `send_user_id`        varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '发送者用户ID',
    `send_user_nick_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '发送者昵称',
    `receive_user_id`     varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '接收者用户ID',
    `send_time`           bigint                                                       NOT NULL COMMENT '发送时间戳（毫秒）',
    `file_size`           bigint NULL DEFAULT NULL COMMENT '文件大小（字节）',
    `file_name`           varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件名',
    `file_type`           tinyint NULL DEFAULT NULL COMMENT '文件类型',
    `file_suffix`         varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件后缀',
    `status`              tinyint NULL DEFAULT 1 COMMENT '状态：0发送中，1已发送',
    PRIMARY KEY (`message_id`) USING BTREE,
    INDEX                 `idx_session_id`(`session_id` ASC) USING BTREE,
    INDEX                 `idx_send_time`(`send_time` ASC) USING BTREE,
    INDEX                 `idx_send_user_id`(`send_user_id` ASC) USING BTREE,
    INDEX                 `idx_receive_user_id`(`receive_user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '私聊消息分表06' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for private_chat_message_07
-- ----------------------------
DROP TABLE IF EXISTS `private_chat_message_07`;
CREATE TABLE `private_chat_message_07`
(
    `message_id`          bigint                                                       NOT NULL COMMENT '消息ID，雪花算法生成',
    `session_id`          varchar(25) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会话ID，分表键',
    `message_type`        tinyint                                                      NOT NULL COMMENT '消息类型：5文本，6媒体',
    `message_content`     varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '消息内容',
    `send_user_id`        varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '发送者用户ID',
    `send_user_nick_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '发送者昵称',
    `receive_user_id`     varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '接收者用户ID',
    `send_time`           bigint                                                       NOT NULL COMMENT '发送时间戳（毫秒）',
    `file_size`           bigint NULL DEFAULT NULL COMMENT '文件大小（字节）',
    `file_name`           varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件名',
    `file_type`           tinyint NULL DEFAULT NULL COMMENT '文件类型',
    `file_suffix`         varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件后缀',
    `status`              tinyint NULL DEFAULT 1 COMMENT '状态：0发送中，1已发送',
    PRIMARY KEY (`message_id`) USING BTREE,
    INDEX                 `idx_session_id`(`session_id` ASC) USING BTREE,
    INDEX                 `idx_send_time`(`send_time` ASC) USING BTREE,
    INDEX                 `idx_send_user_id`(`send_user_id` ASC) USING BTREE,
    INDEX                 `idx_receive_user_id`(`receive_user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '私聊消息分表07' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for private_chat_message_08
-- ----------------------------
DROP TABLE IF EXISTS `private_chat_message_08`;
CREATE TABLE `private_chat_message_08`
(
    `message_id`          bigint                                                       NOT NULL COMMENT '消息ID，雪花算法生成',
    `session_id`          varchar(25) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会话ID，分表键',
    `message_type`        tinyint                                                      NOT NULL COMMENT '消息类型：5文本，6媒体',
    `message_content`     varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '消息内容',
    `send_user_id`        varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '发送者用户ID',
    `send_user_nick_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '发送者昵称',
    `receive_user_id`     varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '接收者用户ID',
    `send_time`           bigint                                                       NOT NULL COMMENT '发送时间戳（毫秒）',
    `file_size`           bigint NULL DEFAULT NULL COMMENT '文件大小（字节）',
    `file_name`           varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件名',
    `file_type`           tinyint NULL DEFAULT NULL COMMENT '文件类型',
    `file_suffix`         varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件后缀',
    `status`              tinyint NULL DEFAULT 1 COMMENT '状态：0发送中，1已发送',
    PRIMARY KEY (`message_id`) USING BTREE,
    INDEX                 `idx_session_id`(`session_id` ASC) USING BTREE,
    INDEX                 `idx_send_time`(`send_time` ASC) USING BTREE,
    INDEX                 `idx_send_user_id`(`send_user_id` ASC) USING BTREE,
    INDEX                 `idx_receive_user_id`(`receive_user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '私聊消息分表08' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for private_chat_message_09
-- ----------------------------
DROP TABLE IF EXISTS `private_chat_message_09`;
CREATE TABLE `private_chat_message_09`
(
    `message_id`          bigint                                                       NOT NULL COMMENT '消息ID，雪花算法生成',
    `session_id`          varchar(25) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会话ID，分表键',
    `message_type`        tinyint                                                      NOT NULL COMMENT '消息类型：5文本，6媒体',
    `message_content`     varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '消息内容',
    `send_user_id`        varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '发送者用户ID',
    `send_user_nick_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '发送者昵称',
    `receive_user_id`     varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '接收者用户ID',
    `send_time`           bigint                                                       NOT NULL COMMENT '发送时间戳（毫秒）',
    `file_size`           bigint NULL DEFAULT NULL COMMENT '文件大小（字节）',
    `file_name`           varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件名',
    `file_type`           tinyint NULL DEFAULT NULL COMMENT '文件类型',
    `file_suffix`         varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件后缀',
    `status`              tinyint NULL DEFAULT 1 COMMENT '状态：0发送中，1已发送',
    PRIMARY KEY (`message_id`) USING BTREE,
    INDEX                 `idx_session_id`(`session_id` ASC) USING BTREE,
    INDEX                 `idx_send_time`(`send_time` ASC) USING BTREE,
    INDEX                 `idx_send_user_id`(`send_user_id` ASC) USING BTREE,
    INDEX                 `idx_receive_user_id`(`receive_user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '私聊消息分表09' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for private_chat_message_10
-- ----------------------------
DROP TABLE IF EXISTS `private_chat_message_10`;
CREATE TABLE `private_chat_message_10`
(
    `message_id`          bigint                                                       NOT NULL COMMENT '消息ID，雪花算法生成',
    `session_id`          varchar(25) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会话ID，分表键',
    `message_type`        tinyint                                                      NOT NULL COMMENT '消息类型：5文本，6媒体',
    `message_content`     varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '消息内容',
    `send_user_id`        varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '发送者用户ID',
    `send_user_nick_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '发送者昵称',
    `receive_user_id`     varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '接收者用户ID',
    `send_time`           bigint                                                       NOT NULL COMMENT '发送时间戳（毫秒）',
    `file_size`           bigint NULL DEFAULT NULL COMMENT '文件大小（字节）',
    `file_name`           varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件名',
    `file_type`           tinyint NULL DEFAULT NULL COMMENT '文件类型',
    `file_suffix`         varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件后缀',
    `status`              tinyint NULL DEFAULT 1 COMMENT '状态：0发送中，1已发送',
    PRIMARY KEY (`message_id`) USING BTREE,
    INDEX                 `idx_session_id`(`session_id` ASC) USING BTREE,
    INDEX                 `idx_send_time`(`send_time` ASC) USING BTREE,
    INDEX                 `idx_send_user_id`(`send_user_id` ASC) USING BTREE,
    INDEX                 `idx_receive_user_id`(`receive_user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '私聊消息分表10' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for private_chat_message_11
-- ----------------------------
DROP TABLE IF EXISTS `private_chat_message_11`;
CREATE TABLE `private_chat_message_11`
(
    `message_id`          bigint                                                       NOT NULL COMMENT '消息ID，雪花算法生成',
    `session_id`          varchar(25) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会话ID，分表键',
    `message_type`        tinyint                                                      NOT NULL COMMENT '消息类型：5文本，6媒体',
    `message_content`     varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '消息内容',
    `send_user_id`        varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '发送者用户ID',
    `send_user_nick_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '发送者昵称',
    `receive_user_id`     varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '接收者用户ID',
    `send_time`           bigint                                                       NOT NULL COMMENT '发送时间戳（毫秒）',
    `file_size`           bigint NULL DEFAULT NULL COMMENT '文件大小（字节）',
    `file_name`           varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件名',
    `file_type`           tinyint NULL DEFAULT NULL COMMENT '文件类型',
    `file_suffix`         varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件后缀',
    `status`              tinyint NULL DEFAULT 1 COMMENT '状态：0发送中，1已发送',
    PRIMARY KEY (`message_id`) USING BTREE,
    INDEX                 `idx_session_id`(`session_id` ASC) USING BTREE,
    INDEX                 `idx_send_time`(`send_time` ASC) USING BTREE,
    INDEX                 `idx_send_user_id`(`send_user_id` ASC) USING BTREE,
    INDEX                 `idx_receive_user_id`(`receive_user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '私聊消息分表11' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for private_chat_message_12
-- ----------------------------
DROP TABLE IF EXISTS `private_chat_message_12`;
CREATE TABLE `private_chat_message_12`
(
    `message_id`          bigint                                                       NOT NULL COMMENT '消息ID，雪花算法生成',
    `session_id`          varchar(25) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会话ID，分表键',
    `message_type`        tinyint                                                      NOT NULL COMMENT '消息类型：5文本，6媒体',
    `message_content`     varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '消息内容',
    `send_user_id`        varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '发送者用户ID',
    `send_user_nick_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '发送者昵称',
    `receive_user_id`     varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '接收者用户ID',
    `send_time`           bigint                                                       NOT NULL COMMENT '发送时间戳（毫秒）',
    `file_size`           bigint NULL DEFAULT NULL COMMENT '文件大小（字节）',
    `file_name`           varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件名',
    `file_type`           tinyint NULL DEFAULT NULL COMMENT '文件类型',
    `file_suffix`         varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件后缀',
    `status`              tinyint NULL DEFAULT 1 COMMENT '状态：0发送中，1已发送',
    PRIMARY KEY (`message_id`) USING BTREE,
    INDEX                 `idx_session_id`(`session_id` ASC) USING BTREE,
    INDEX                 `idx_send_time`(`send_time` ASC) USING BTREE,
    INDEX                 `idx_send_user_id`(`send_user_id` ASC) USING BTREE,
    INDEX                 `idx_receive_user_id`(`receive_user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '私聊消息分表12' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for private_chat_message_13
-- ----------------------------
DROP TABLE IF EXISTS `private_chat_message_13`;
CREATE TABLE `private_chat_message_13`
(
    `message_id`          bigint                                                       NOT NULL COMMENT '消息ID，雪花算法生成',
    `session_id`          varchar(25) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会话ID，分表键',
    `message_type`        tinyint                                                      NOT NULL COMMENT '消息类型：5文本，6媒体',
    `message_content`     varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '消息内容',
    `send_user_id`        varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '发送者用户ID',
    `send_user_nick_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '发送者昵称',
    `receive_user_id`     varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '接收者用户ID',
    `send_time`           bigint                                                       NOT NULL COMMENT '发送时间戳（毫秒）',
    `file_size`           bigint NULL DEFAULT NULL COMMENT '文件大小（字节）',
    `file_name`           varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件名',
    `file_type`           tinyint NULL DEFAULT NULL COMMENT '文件类型',
    `file_suffix`         varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件后缀',
    `status`              tinyint NULL DEFAULT 1 COMMENT '状态：0发送中，1已发送',
    PRIMARY KEY (`message_id`) USING BTREE,
    INDEX                 `idx_session_id`(`session_id` ASC) USING BTREE,
    INDEX                 `idx_send_time`(`send_time` ASC) USING BTREE,
    INDEX                 `idx_send_user_id`(`send_user_id` ASC) USING BTREE,
    INDEX                 `idx_receive_user_id`(`receive_user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '私聊消息分表13' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for private_chat_message_14
-- ----------------------------
DROP TABLE IF EXISTS `private_chat_message_14`;
CREATE TABLE `private_chat_message_14`
(
    `message_id`          bigint                                                       NOT NULL COMMENT '消息ID，雪花算法生成',
    `session_id`          varchar(25) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会话ID，分表键',
    `message_type`        tinyint                                                      NOT NULL COMMENT '消息类型：5文本，6媒体',
    `message_content`     varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '消息内容',
    `send_user_id`        varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '发送者用户ID',
    `send_user_nick_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '发送者昵称',
    `receive_user_id`     varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '接收者用户ID',
    `send_time`           bigint                                                       NOT NULL COMMENT '发送时间戳（毫秒）',
    `file_size`           bigint NULL DEFAULT NULL COMMENT '文件大小（字节）',
    `file_name`           varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件名',
    `file_type`           tinyint NULL DEFAULT NULL COMMENT '文件类型',
    `file_suffix`         varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件后缀',
    `status`              tinyint NULL DEFAULT 1 COMMENT '状态：0发送中，1已发送',
    PRIMARY KEY (`message_id`) USING BTREE,
    INDEX                 `idx_session_id`(`session_id` ASC) USING BTREE,
    INDEX                 `idx_send_time`(`send_time` ASC) USING BTREE,
    INDEX                 `idx_send_user_id`(`send_user_id` ASC) USING BTREE,
    INDEX                 `idx_receive_user_id`(`receive_user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '私聊消息分表14' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for private_chat_message_15
-- ----------------------------
DROP TABLE IF EXISTS `private_chat_message_15`;
CREATE TABLE `private_chat_message_15`
(
    `message_id`          bigint                                                       NOT NULL COMMENT '消息ID，雪花算法生成',
    `session_id`          varchar(25) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会话ID，分表键',
    `message_type`        tinyint                                                      NOT NULL COMMENT '消息类型：5文本，6媒体',
    `message_content`     varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '消息内容',
    `send_user_id`        varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '发送者用户ID',
    `send_user_nick_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '发送者昵称',
    `receive_user_id`     varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '接收者用户ID',
    `send_time`           bigint                                                       NOT NULL COMMENT '发送时间戳（毫秒）',
    `file_size`           bigint NULL DEFAULT NULL COMMENT '文件大小（字节）',
    `file_name`           varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件名',
    `file_type`           tinyint NULL DEFAULT NULL COMMENT '文件类型',
    `file_suffix`         varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件后缀',
    `status`              tinyint NULL DEFAULT 1 COMMENT '状态：0发送中，1已发送',
    PRIMARY KEY (`message_id`) USING BTREE,
    INDEX                 `idx_session_id`(`session_id` ASC) USING BTREE,
    INDEX                 `idx_send_time`(`send_time` ASC) USING BTREE,
    INDEX                 `idx_send_user_id`(`send_user_id` ASC) USING BTREE,
    INDEX                 `idx_receive_user_id`(`receive_user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '私聊消息分表15' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for private_chat_message_16
-- ----------------------------
DROP TABLE IF EXISTS `private_chat_message_16`;
CREATE TABLE `private_chat_message_16`
(
    `message_id`          bigint                                                       NOT NULL COMMENT '消息ID，雪花算法生成',
    `session_id`          varchar(25) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会话ID，分表键',
    `message_type`        tinyint                                                      NOT NULL COMMENT '消息类型：5文本，6媒体',
    `message_content`     varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '消息内容',
    `send_user_id`        varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '发送者用户ID',
    `send_user_nick_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '发送者昵称',
    `receive_user_id`     varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '接收者用户ID',
    `send_time`           bigint                                                       NOT NULL COMMENT '发送时间戳（毫秒）',
    `file_size`           bigint NULL DEFAULT NULL COMMENT '文件大小（字节）',
    `file_name`           varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件名',
    `file_type`           tinyint NULL DEFAULT NULL COMMENT '文件类型',
    `file_suffix`         varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件后缀',
    `status`              tinyint NULL DEFAULT 1 COMMENT '状态：0发送中，1已发送',
    PRIMARY KEY (`message_id`) USING BTREE,
    INDEX                 `idx_session_id`(`session_id` ASC) USING BTREE,
    INDEX                 `idx_send_time`(`send_time` ASC) USING BTREE,
    INDEX                 `idx_send_user_id`(`send_user_id` ASC) USING BTREE,
    INDEX                 `idx_receive_user_id`(`receive_user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '私聊消息分表16' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for private_chat_message_17
-- ----------------------------
DROP TABLE IF EXISTS `private_chat_message_17`;
CREATE TABLE `private_chat_message_17`
(
    `message_id`          bigint                                                       NOT NULL COMMENT '消息ID，雪花算法生成',
    `session_id`          varchar(25) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会话ID，分表键',
    `message_type`        tinyint                                                      NOT NULL COMMENT '消息类型：5文本，6媒体',
    `message_content`     varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '消息内容',
    `send_user_id`        varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '发送者用户ID',
    `send_user_nick_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '发送者昵称',
    `receive_user_id`     varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '接收者用户ID',
    `send_time`           bigint                                                       NOT NULL COMMENT '发送时间戳（毫秒）',
    `file_size`           bigint NULL DEFAULT NULL COMMENT '文件大小（字节）',
    `file_name`           varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件名',
    `file_type`           tinyint NULL DEFAULT NULL COMMENT '文件类型',
    `file_suffix`         varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件后缀',
    `status`              tinyint NULL DEFAULT 1 COMMENT '状态：0发送中，1已发送',
    PRIMARY KEY (`message_id`) USING BTREE,
    INDEX                 `idx_session_id`(`session_id` ASC) USING BTREE,
    INDEX                 `idx_send_time`(`send_time` ASC) USING BTREE,
    INDEX                 `idx_send_user_id`(`send_user_id` ASC) USING BTREE,
    INDEX                 `idx_receive_user_id`(`receive_user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '私聊消息分表17' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for private_chat_message_18
-- ----------------------------
DROP TABLE IF EXISTS `private_chat_message_18`;
CREATE TABLE `private_chat_message_18`
(
    `message_id`          bigint                                                       NOT NULL COMMENT '消息ID，雪花算法生成',
    `session_id`          varchar(25) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会话ID，分表键',
    `message_type`        tinyint                                                      NOT NULL COMMENT '消息类型：5文本，6媒体',
    `message_content`     varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '消息内容',
    `send_user_id`        varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '发送者用户ID',
    `send_user_nick_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '发送者昵称',
    `receive_user_id`     varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '接收者用户ID',
    `send_time`           bigint                                                       NOT NULL COMMENT '发送时间戳（毫秒）',
    `file_size`           bigint NULL DEFAULT NULL COMMENT '文件大小（字节）',
    `file_name`           varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件名',
    `file_type`           tinyint NULL DEFAULT NULL COMMENT '文件类型',
    `file_suffix`         varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件后缀',
    `status`              tinyint NULL DEFAULT 1 COMMENT '状态：0发送中，1已发送',
    PRIMARY KEY (`message_id`) USING BTREE,
    INDEX                 `idx_session_id`(`session_id` ASC) USING BTREE,
    INDEX                 `idx_send_time`(`send_time` ASC) USING BTREE,
    INDEX                 `idx_send_user_id`(`send_user_id` ASC) USING BTREE,
    INDEX                 `idx_receive_user_id`(`receive_user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '私聊消息分表18' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for private_chat_message_19
-- ----------------------------
DROP TABLE IF EXISTS `private_chat_message_19`;
CREATE TABLE `private_chat_message_19`
(
    `message_id`          bigint                                                       NOT NULL COMMENT '消息ID，雪花算法生成',
    `session_id`          varchar(25) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会话ID，分表键',
    `message_type`        tinyint                                                      NOT NULL COMMENT '消息类型：5文本，6媒体',
    `message_content`     varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '消息内容',
    `send_user_id`        varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '发送者用户ID',
    `send_user_nick_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '发送者昵称',
    `receive_user_id`     varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '接收者用户ID',
    `send_time`           bigint                                                       NOT NULL COMMENT '发送时间戳（毫秒）',
    `file_size`           bigint NULL DEFAULT NULL COMMENT '文件大小（字节）',
    `file_name`           varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件名',
    `file_type`           tinyint NULL DEFAULT NULL COMMENT '文件类型',
    `file_suffix`         varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件后缀',
    `status`              tinyint NULL DEFAULT 1 COMMENT '状态：0发送中，1已发送',
    PRIMARY KEY (`message_id`) USING BTREE,
    INDEX                 `idx_session_id`(`session_id` ASC) USING BTREE,
    INDEX                 `idx_send_time`(`send_time` ASC) USING BTREE,
    INDEX                 `idx_send_user_id`(`send_user_id` ASC) USING BTREE,
    INDEX                 `idx_receive_user_id`(`receive_user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '私聊消息分表19' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for private_chat_message_20
-- ----------------------------
DROP TABLE IF EXISTS `private_chat_message_20`;
CREATE TABLE `private_chat_message_20`
(
    `message_id`          bigint                                                       NOT NULL COMMENT '消息ID，雪花算法生成',
    `session_id`          varchar(25) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会话ID，分表键',
    `message_type`        tinyint                                                      NOT NULL COMMENT '消息类型：5文本，6媒体',
    `message_content`     varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '消息内容',
    `send_user_id`        varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '发送者用户ID',
    `send_user_nick_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '发送者昵称',
    `receive_user_id`     varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '接收者用户ID',
    `send_time`           bigint                                                       NOT NULL COMMENT '发送时间戳（毫秒）',
    `file_size`           bigint NULL DEFAULT NULL COMMENT '文件大小（字节）',
    `file_name`           varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件名',
    `file_type`           tinyint NULL DEFAULT NULL COMMENT '文件类型',
    `file_suffix`         varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件后缀',
    `status`              tinyint NULL DEFAULT 1 COMMENT '状态：0发送中，1已发送',
    PRIMARY KEY (`message_id`) USING BTREE,
    INDEX                 `idx_session_id`(`session_id` ASC) USING BTREE,
    INDEX                 `idx_send_time`(`send_time` ASC) USING BTREE,
    INDEX                 `idx_send_user_id`(`send_user_id` ASC) USING BTREE,
    INDEX                 `idx_receive_user_id`(`receive_user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '私聊消息分表20' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for private_chat_message_21
-- ----------------------------
DROP TABLE IF EXISTS `private_chat_message_21`;
CREATE TABLE `private_chat_message_21`
(
    `message_id`          bigint                                                       NOT NULL COMMENT '消息ID，雪花算法生成',
    `session_id`          varchar(25) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会话ID，分表键',
    `message_type`        tinyint                                                      NOT NULL COMMENT '消息类型：5文本，6媒体',
    `message_content`     varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '消息内容',
    `send_user_id`        varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '发送者用户ID',
    `send_user_nick_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '发送者昵称',
    `receive_user_id`     varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '接收者用户ID',
    `send_time`           bigint                                                       NOT NULL COMMENT '发送时间戳（毫秒）',
    `file_size`           bigint NULL DEFAULT NULL COMMENT '文件大小（字节）',
    `file_name`           varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件名',
    `file_type`           tinyint NULL DEFAULT NULL COMMENT '文件类型',
    `file_suffix`         varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件后缀',
    `status`              tinyint NULL DEFAULT 1 COMMENT '状态：0发送中，1已发送',
    PRIMARY KEY (`message_id`) USING BTREE,
    INDEX                 `idx_session_id`(`session_id` ASC) USING BTREE,
    INDEX                 `idx_send_time`(`send_time` ASC) USING BTREE,
    INDEX                 `idx_send_user_id`(`send_user_id` ASC) USING BTREE,
    INDEX                 `idx_receive_user_id`(`receive_user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '私聊消息分表21' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for private_chat_message_22
-- ----------------------------
DROP TABLE IF EXISTS `private_chat_message_22`;
CREATE TABLE `private_chat_message_22`
(
    `message_id`          bigint                                                       NOT NULL COMMENT '消息ID，雪花算法生成',
    `session_id`          varchar(25) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会话ID，分表键',
    `message_type`        tinyint                                                      NOT NULL COMMENT '消息类型：5文本，6媒体',
    `message_content`     varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '消息内容',
    `send_user_id`        varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '发送者用户ID',
    `send_user_nick_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '发送者昵称',
    `receive_user_id`     varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '接收者用户ID',
    `send_time`           bigint                                                       NOT NULL COMMENT '发送时间戳（毫秒）',
    `file_size`           bigint NULL DEFAULT NULL COMMENT '文件大小（字节）',
    `file_name`           varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件名',
    `file_type`           tinyint NULL DEFAULT NULL COMMENT '文件类型',
    `file_suffix`         varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件后缀',
    `status`              tinyint NULL DEFAULT 1 COMMENT '状态：0发送中，1已发送',
    PRIMARY KEY (`message_id`) USING BTREE,
    INDEX                 `idx_session_id`(`session_id` ASC) USING BTREE,
    INDEX                 `idx_send_time`(`send_time` ASC) USING BTREE,
    INDEX                 `idx_send_user_id`(`send_user_id` ASC) USING BTREE,
    INDEX                 `idx_receive_user_id`(`receive_user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '私聊消息分表22' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for private_chat_message_23
-- ----------------------------
DROP TABLE IF EXISTS `private_chat_message_23`;
CREATE TABLE `private_chat_message_23`
(
    `message_id`          bigint                                                       NOT NULL COMMENT '消息ID，雪花算法生成',
    `session_id`          varchar(25) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会话ID，分表键',
    `message_type`        tinyint                                                      NOT NULL COMMENT '消息类型：5文本，6媒体',
    `message_content`     varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '消息内容',
    `send_user_id`        varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '发送者用户ID',
    `send_user_nick_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '发送者昵称',
    `receive_user_id`     varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '接收者用户ID',
    `send_time`           bigint                                                       NOT NULL COMMENT '发送时间戳（毫秒）',
    `file_size`           bigint NULL DEFAULT NULL COMMENT '文件大小（字节）',
    `file_name`           varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件名',
    `file_type`           tinyint NULL DEFAULT NULL COMMENT '文件类型',
    `file_suffix`         varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件后缀',
    `status`              tinyint NULL DEFAULT 1 COMMENT '状态：0发送中，1已发送',
    PRIMARY KEY (`message_id`) USING BTREE,
    INDEX                 `idx_session_id`(`session_id` ASC) USING BTREE,
    INDEX                 `idx_send_time`(`send_time` ASC) USING BTREE,
    INDEX                 `idx_send_user_id`(`send_user_id` ASC) USING BTREE,
    INDEX                 `idx_receive_user_id`(`receive_user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '私聊消息分表23' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for private_chat_message_24
-- ----------------------------
DROP TABLE IF EXISTS `private_chat_message_24`;
CREATE TABLE `private_chat_message_24`
(
    `message_id`          bigint                                                       NOT NULL COMMENT '消息ID，雪花算法生成',
    `session_id`          varchar(25) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会话ID，分表键',
    `message_type`        tinyint                                                      NOT NULL COMMENT '消息类型：5文本，6媒体',
    `message_content`     varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '消息内容',
    `send_user_id`        varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '发送者用户ID',
    `send_user_nick_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '发送者昵称',
    `receive_user_id`     varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '接收者用户ID',
    `send_time`           bigint                                                       NOT NULL COMMENT '发送时间戳（毫秒）',
    `file_size`           bigint NULL DEFAULT NULL COMMENT '文件大小（字节）',
    `file_name`           varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件名',
    `file_type`           tinyint NULL DEFAULT NULL COMMENT '文件类型',
    `file_suffix`         varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件后缀',
    `status`              tinyint NULL DEFAULT 1 COMMENT '状态：0发送中，1已发送',
    PRIMARY KEY (`message_id`) USING BTREE,
    INDEX                 `idx_session_id`(`session_id` ASC) USING BTREE,
    INDEX                 `idx_send_time`(`send_time` ASC) USING BTREE,
    INDEX                 `idx_send_user_id`(`send_user_id` ASC) USING BTREE,
    INDEX                 `idx_receive_user_id`(`receive_user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '私聊消息分表24' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for private_chat_message_25
-- ----------------------------
DROP TABLE IF EXISTS `private_chat_message_25`;
CREATE TABLE `private_chat_message_25`
(
    `message_id`          bigint                                                       NOT NULL COMMENT '消息ID，雪花算法生成',
    `session_id`          varchar(25) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会话ID，分表键',
    `message_type`        tinyint                                                      NOT NULL COMMENT '消息类型：5文本，6媒体',
    `message_content`     varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '消息内容',
    `send_user_id`        varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '发送者用户ID',
    `send_user_nick_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '发送者昵称',
    `receive_user_id`     varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '接收者用户ID',
    `send_time`           bigint                                                       NOT NULL COMMENT '发送时间戳（毫秒）',
    `file_size`           bigint NULL DEFAULT NULL COMMENT '文件大小（字节）',
    `file_name`           varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件名',
    `file_type`           tinyint NULL DEFAULT NULL COMMENT '文件类型',
    `file_suffix`         varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件后缀',
    `status`              tinyint NULL DEFAULT 1 COMMENT '状态：0发送中，1已发送',
    PRIMARY KEY (`message_id`) USING BTREE,
    INDEX                 `idx_session_id`(`session_id` ASC) USING BTREE,
    INDEX                 `idx_send_time`(`send_time` ASC) USING BTREE,
    INDEX                 `idx_send_user_id`(`send_user_id` ASC) USING BTREE,
    INDEX                 `idx_receive_user_id`(`receive_user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '私聊消息分表25' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for private_chat_message_26
-- ----------------------------
DROP TABLE IF EXISTS `private_chat_message_26`;
CREATE TABLE `private_chat_message_26`
(
    `message_id`          bigint                                                       NOT NULL COMMENT '消息ID，雪花算法生成',
    `session_id`          varchar(25) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会话ID，分表键',
    `message_type`        tinyint                                                      NOT NULL COMMENT '消息类型：5文本，6媒体',
    `message_content`     varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '消息内容',
    `send_user_id`        varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '发送者用户ID',
    `send_user_nick_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '发送者昵称',
    `receive_user_id`     varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '接收者用户ID',
    `send_time`           bigint                                                       NOT NULL COMMENT '发送时间戳（毫秒）',
    `file_size`           bigint NULL DEFAULT NULL COMMENT '文件大小（字节）',
    `file_name`           varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件名',
    `file_type`           tinyint NULL DEFAULT NULL COMMENT '文件类型',
    `file_suffix`         varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件后缀',
    `status`              tinyint NULL DEFAULT 1 COMMENT '状态：0发送中，1已发送',
    PRIMARY KEY (`message_id`) USING BTREE,
    INDEX                 `idx_session_id`(`session_id` ASC) USING BTREE,
    INDEX                 `idx_send_time`(`send_time` ASC) USING BTREE,
    INDEX                 `idx_send_user_id`(`send_user_id` ASC) USING BTREE,
    INDEX                 `idx_receive_user_id`(`receive_user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '私聊消息分表26' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for private_chat_message_27
-- ----------------------------
DROP TABLE IF EXISTS `private_chat_message_27`;
CREATE TABLE `private_chat_message_27`
(
    `message_id`          bigint                                                       NOT NULL COMMENT '消息ID，雪花算法生成',
    `session_id`          varchar(25) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会话ID，分表键',
    `message_type`        tinyint                                                      NOT NULL COMMENT '消息类型：5文本，6媒体',
    `message_content`     varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '消息内容',
    `send_user_id`        varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '发送者用户ID',
    `send_user_nick_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '发送者昵称',
    `receive_user_id`     varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '接收者用户ID',
    `send_time`           bigint                                                       NOT NULL COMMENT '发送时间戳（毫秒）',
    `file_size`           bigint NULL DEFAULT NULL COMMENT '文件大小（字节）',
    `file_name`           varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件名',
    `file_type`           tinyint NULL DEFAULT NULL COMMENT '文件类型',
    `file_suffix`         varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件后缀',
    `status`              tinyint NULL DEFAULT 1 COMMENT '状态：0发送中，1已发送',
    PRIMARY KEY (`message_id`) USING BTREE,
    INDEX                 `idx_session_id`(`session_id` ASC) USING BTREE,
    INDEX                 `idx_send_time`(`send_time` ASC) USING BTREE,
    INDEX                 `idx_send_user_id`(`send_user_id` ASC) USING BTREE,
    INDEX                 `idx_receive_user_id`(`receive_user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '私聊消息分表27' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for private_chat_message_28
-- ----------------------------
DROP TABLE IF EXISTS `private_chat_message_28`;
CREATE TABLE `private_chat_message_28`
(
    `message_id`          bigint                                                       NOT NULL COMMENT '消息ID，雪花算法生成',
    `session_id`          varchar(25) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会话ID，分表键',
    `message_type`        tinyint                                                      NOT NULL COMMENT '消息类型：5文本，6媒体',
    `message_content`     varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '消息内容',
    `send_user_id`        varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '发送者用户ID',
    `send_user_nick_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '发送者昵称',
    `receive_user_id`     varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '接收者用户ID',
    `send_time`           bigint                                                       NOT NULL COMMENT '发送时间戳（毫秒）',
    `file_size`           bigint NULL DEFAULT NULL COMMENT '文件大小（字节）',
    `file_name`           varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件名',
    `file_type`           tinyint NULL DEFAULT NULL COMMENT '文件类型',
    `file_suffix`         varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件后缀',
    `status`              tinyint NULL DEFAULT 1 COMMENT '状态：0发送中，1已发送',
    PRIMARY KEY (`message_id`) USING BTREE,
    INDEX                 `idx_session_id`(`session_id` ASC) USING BTREE,
    INDEX                 `idx_send_time`(`send_time` ASC) USING BTREE,
    INDEX                 `idx_send_user_id`(`send_user_id` ASC) USING BTREE,
    INDEX                 `idx_receive_user_id`(`receive_user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '私聊消息分表28' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for private_chat_message_29
-- ----------------------------
DROP TABLE IF EXISTS `private_chat_message_29`;
CREATE TABLE `private_chat_message_29`
(
    `message_id`          bigint                                                       NOT NULL COMMENT '消息ID，雪花算法生成',
    `session_id`          varchar(25) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会话ID，分表键',
    `message_type`        tinyint                                                      NOT NULL COMMENT '消息类型：5文本，6媒体',
    `message_content`     varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '消息内容',
    `send_user_id`        varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '发送者用户ID',
    `send_user_nick_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '发送者昵称',
    `receive_user_id`     varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '接收者用户ID',
    `send_time`           bigint                                                       NOT NULL COMMENT '发送时间戳（毫秒）',
    `file_size`           bigint NULL DEFAULT NULL COMMENT '文件大小（字节）',
    `file_name`           varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件名',
    `file_type`           tinyint NULL DEFAULT NULL COMMENT '文件类型',
    `file_suffix`         varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件后缀',
    `status`              tinyint NULL DEFAULT 1 COMMENT '状态：0发送中，1已发送',
    PRIMARY KEY (`message_id`) USING BTREE,
    INDEX                 `idx_session_id`(`session_id` ASC) USING BTREE,
    INDEX                 `idx_send_time`(`send_time` ASC) USING BTREE,
    INDEX                 `idx_send_user_id`(`send_user_id` ASC) USING BTREE,
    INDEX                 `idx_receive_user_id`(`receive_user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '私聊消息分表29' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for private_chat_message_30
-- ----------------------------
DROP TABLE IF EXISTS `private_chat_message_30`;
CREATE TABLE `private_chat_message_30`
(
    `message_id`          bigint                                                       NOT NULL COMMENT '消息ID，雪花算法生成',
    `session_id`          varchar(25) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会话ID，分表键',
    `message_type`        tinyint                                                      NOT NULL COMMENT '消息类型：5文本，6媒体',
    `message_content`     varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '消息内容',
    `send_user_id`        varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '发送者用户ID',
    `send_user_nick_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '发送者昵称',
    `receive_user_id`     varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '接收者用户ID',
    `send_time`           bigint                                                       NOT NULL COMMENT '发送时间戳（毫秒）',
    `file_size`           bigint NULL DEFAULT NULL COMMENT '文件大小（字节）',
    `file_name`           varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件名',
    `file_type`           tinyint NULL DEFAULT NULL COMMENT '文件类型',
    `file_suffix`         varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件后缀',
    `status`              tinyint NULL DEFAULT 1 COMMENT '状态：0发送中，1已发送',
    PRIMARY KEY (`message_id`) USING BTREE,
    INDEX                 `idx_session_id`(`session_id` ASC) USING BTREE,
    INDEX                 `idx_send_time`(`send_time` ASC) USING BTREE,
    INDEX                 `idx_send_user_id`(`send_user_id` ASC) USING BTREE,
    INDEX                 `idx_receive_user_id`(`receive_user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '私聊消息分表30' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for private_chat_message_31
-- ----------------------------
DROP TABLE IF EXISTS `private_chat_message_31`;
CREATE TABLE `private_chat_message_31`
(
    `message_id`          bigint                                                       NOT NULL COMMENT '消息ID，雪花算法生成',
    `session_id`          varchar(25) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会话ID，分表键',
    `message_type`        tinyint                                                      NOT NULL COMMENT '消息类型：5文本，6媒体',
    `message_content`     varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '消息内容',
    `send_user_id`        varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '发送者用户ID',
    `send_user_nick_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '发送者昵称',
    `receive_user_id`     varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '接收者用户ID',
    `send_time`           bigint                                                       NOT NULL COMMENT '发送时间戳（毫秒）',
    `file_size`           bigint NULL DEFAULT NULL COMMENT '文件大小（字节）',
    `file_name`           varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件名',
    `file_type`           tinyint NULL DEFAULT NULL COMMENT '文件类型',
    `file_suffix`         varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件后缀',
    `status`              tinyint NULL DEFAULT 1 COMMENT '状态：0发送中，1已发送',
    PRIMARY KEY (`message_id`) USING BTREE,
    INDEX                 `idx_session_id`(`session_id` ASC) USING BTREE,
    INDEX                 `idx_send_time`(`send_time` ASC) USING BTREE,
    INDEX                 `idx_send_user_id`(`send_user_id` ASC) USING BTREE,
    INDEX                 `idx_receive_user_id`(`receive_user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '私聊消息分表31' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for private_chat_message_32
-- ----------------------------
DROP TABLE IF EXISTS `private_chat_message_32`;
CREATE TABLE `private_chat_message_32`
(
    `message_id`          bigint                                                       NOT NULL COMMENT '消息ID，雪花算法生成',
    `session_id`          varchar(25) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会话ID，分表键',
    `message_type`        tinyint                                                      NOT NULL COMMENT '消息类型：5文本，6媒体',
    `message_content`     varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '消息内容',
    `send_user_id`        varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '发送者用户ID',
    `send_user_nick_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '发送者昵称',
    `receive_user_id`     varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '接收者用户ID',
    `send_time`           bigint                                                       NOT NULL COMMENT '发送时间戳（毫秒）',
    `file_size`           bigint NULL DEFAULT NULL COMMENT '文件大小（字节）',
    `file_name`           varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件名',
    `file_type`           tinyint NULL DEFAULT NULL COMMENT '文件类型',
    `file_suffix`         varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件后缀',
    `status`              tinyint NULL DEFAULT 1 COMMENT '状态：0发送中，1已发送',
    PRIMARY KEY (`message_id`) USING BTREE,
    INDEX                 `idx_session_id`(`session_id` ASC) USING BTREE,
    INDEX                 `idx_send_time`(`send_time` ASC) USING BTREE,
    INDEX                 `idx_send_user_id`(`send_user_id` ASC) USING BTREE,
    INDEX                 `idx_receive_user_id`(`receive_user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '私聊消息分表32' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for private_chat_unread
-- ----------------------------
DROP TABLE IF EXISTS `private_chat_unread`;
CREATE TABLE `private_chat_unread`
(
    `id`                   bigint                                                       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`              varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户ID（消息接收者）',
    `contact_id`           varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '联系人ID（消息发送者）',
    `unread_count`         int NULL DEFAULT 0 COMMENT '未读消息数',
    `last_message_time`    bigint NULL DEFAULT NULL COMMENT '最后消息时间戳',
    `last_message_content` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '最后消息内容预览',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uk_user_contact`(`user_id` ASC, `contact_id` ASC) USING BTREE,
    INDEX                  `idx_user_id`(`user_id` ASC) USING BTREE,
    INDEX                  `idx_last_message_time`(`last_message_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 34 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '私聊未读消息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for user_contact
-- ----------------------------
DROP TABLE IF EXISTS `user_contact`;
CREATE TABLE `user_contact`
(
    `user_id`          varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户ID',
    `contact_id`       varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '联系人ID',
    `status`           tinyint NULL DEFAULT 0 COMMENT '状态：0正常，1拉黑',
    `last_update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    PRIMARY KEY (`user_id`, `contact_id`) USING BTREE,
    INDEX              `idx_contact_id`(`contact_id` ASC) USING BTREE,
    INDEX              `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户联系人表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for user_contact_apply
-- ----------------------------
DROP TABLE IF EXISTS `user_contact_apply`;
CREATE TABLE `user_contact_apply`
(
    `apply_id`        int                                                          NOT NULL AUTO_INCREMENT COMMENT '申请ID',
    `apply_user_id`   varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '申请用户ID',
    `receive_user_id` varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '接收用户ID',
    `last_apply_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最后申请时间',
    `status`          tinyint NULL DEFAULT 0 COMMENT '状态：0待处理，1已同意，2已拒绝，3已拉黑',
    PRIMARY KEY (`apply_id`) USING BTREE,
    UNIQUE INDEX `uk_apply_receive`(`apply_user_id` ASC, `receive_user_id` ASC) USING BTREE,
    INDEX             `idx_receive_user_id`(`receive_user_id` ASC) USING BTREE,
    INDEX             `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '联系人申请表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for user_info
-- ----------------------------
DROP TABLE IF EXISTS `user_info`;
CREATE TABLE `user_info`
(
    `user_id`         varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户ID',
    `email`           varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '邮箱',
    `nick_name`       varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '昵称',
    `sex`             tinyint(1) NULL DEFAULT NULL COMMENT '性别 0:女 1:男 2:保密',
    `password`        varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '密码',
    `status`          tinyint(1) NULL DEFAULT NULL COMMENT '状态',
    `create_time`     datetime NULL DEFAULT NULL COMMENT '创建时间',
    `last_login_time` bigint NULL DEFAULT NULL COMMENT '最后登录时间戳',
    `last_off_time`   bigint NULL DEFAULT NULL COMMENT '最后离线时间戳',
    `meeting_no`      varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '个人会议号',
    PRIMARY KEY (`user_id`) USING BTREE,
    UNIQUE INDEX `idx_key_email`(`email` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户信息表' ROW_FORMAT = Dynamic;

SET
FOREIGN_KEY_CHECKS = 1;
