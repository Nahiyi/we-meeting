package cn.clazs.easymeeting.entity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 联系人VO（包含联系人昵称和在线状态）
 */
@Data
public class UserContactVO {

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 联系人ID
     */
    private String contactId;

    /**
     * 联系人昵称
     */
    private String contactNickName;

    /**
     * 联系人性别：0女，1男，2保密
     */
    private Integer sex;

    /**
     * 状态：0-正常，1-删除，2-拉黑
     */
    private Integer status;

    /**
     * 最后更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastUpdateTime;

    /**
     * 最后登录时间（时间戳）
     */
    private Long lastLoginTime;

    /**
     * 最后离开时间（时间戳）
     * 注意：此字段已废弃，在线状态通过 WebSocket 连接判断
     */
    @Deprecated
    private Long lastOffTime;

    /**
     * 是否在线（通过 WebSocket 连接状态判断，由 Service 层设置）
     */
    private Boolean online;

    /**
     * 未读消息数
     */
    private Integer unreadCount;

    /**
     * 最后消息时间
     */
    private Long lastMessageTime;

    /**
     * 最后消息内容预览
     */
    private String lastMessageContent;
}
