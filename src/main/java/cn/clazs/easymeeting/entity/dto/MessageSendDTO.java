package cn.clazs.easymeeting.entity.dto;

import cn.clazs.easymeeting.entity.enums.MessageSendToType;
import cn.clazs.easymeeting.entity.enums.MessageTypeEnum;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class MessageSendDTO<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 发给人还是群组 */
    private MessageSendToType messageSendToType;
    /** 会议ID */
    private String meetingId;
    /** 消息类型 */
    private MessageTypeEnum messageType;
    /** 消息内容 */
    private T messageContent;
    /** 发送人ID */
    private String sendUserId;
    /** 发送人昵称 */
    private String sendUserNickName;
    /** 接收用户ID */
    private String receiveUserId;
    /** 发送时间戳 */
    private Long sendTime;
    /** 消息ID */
    private Long messageId;
    /** 状态 */
    private Integer status;
    /** 文件名 */
    private String fileName;
    /** 文件类型 */
    private Integer fileType;
    /** 文件大小 */
    private Long fileSize;

}
