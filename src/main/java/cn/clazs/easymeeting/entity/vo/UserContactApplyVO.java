package cn.clazs.easymeeting.entity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 联系人申请VO（包含申请人昵称）
 */
@Data
public class UserContactApplyVO {

    /**
     * 申请ID
     */
    private Integer applyId;

    /**
     * 申请用户ID
     */
    private String applyUserId;

    /**
     * 申请用户昵称
     */
    private String applyUserNickName;

    /**
     * 接收用户ID
     */
    private String receiveUserId;

    /**
     * 接收用户昵称
     */
    private String receiveUserNickName;

    /**
     * 最后申请时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastApplyTime;

    /**
     * 状态：0-待处理，1-已同意，2-已拒绝，3-已拉黑
     */
    private Integer status;
}
