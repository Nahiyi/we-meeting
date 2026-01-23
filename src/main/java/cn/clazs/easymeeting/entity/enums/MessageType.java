package cn.clazs.easymeeting.entity.enums;

import com.alibaba.fastjson2.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum MessageType {
    INIT(0, "连接ws获取信息"),
    ADD_MEETING_ROOM(1, "加入房间"),
    PEER(2, "发送peer"),
    EXIT_MEETING_ROOM(3, "退出房间"),
    FINIS_MEETING(4, "结束会议"),
    CHAT_TEXT_MESSAGE(5, "文本消息"),
    CHAT_MEDIA_MESSAGE(6, "媒体消息"),
    CHAT_MEDIA_MESSAGE_UPDATE(7, "媒体消息更新"),
    USER_CONTACT_APPLY(8, "好友申请消息"),
    INVITE_MESSAGE_MEETING(9, "邀请入会"),
    FORCE_OFF_LINE(10, "强制下线"),
    MEETING_USER_VIDEO_CHANGE(11, "用户视频改变"),
    // WebRTC 信令类型
    WEBRTC_OFFER(12, "WebRTC Offer"),
    WEBRTC_ANSWER(13, "WebRTC Answer"),
    WEBRTC_ICE_CANDIDATE(14, "ICE Candidate");

    @JSONField
    @JsonValue
    private final Integer type;

    @Getter
    private final String desc;

    /**
     * 根据 type 值获取枚举
     */
    public static MessageType getByType(Integer type) {
        if (type == null) {
            return null;
        }
        for (MessageType e : values()) {
            if (e.type.equals(type)) {
                return e;
            }
        }
        return null;
    }

    public MessageType getMessageType() {
        for (MessageType e : MessageType.values()) {
            if (e.type.equals(this.type)) {
                return e;
            }
        }
        return null;
    }

    @JSONField
    public Integer getType() {
        return type;
    }
}
