package cn.clazs.easymeeting.entity.po;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户联系人申请实体
 */
@Data
public class UserContactApply {

    /**
     * 申请ID
     */
    private Integer applyId;

    /**
     * 申请用户ID
     */
    private String applyUserId;

    /**
     * 接收用户ID
     */
    private String receiveUserId;

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
