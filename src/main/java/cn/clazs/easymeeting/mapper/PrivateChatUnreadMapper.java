package cn.clazs.easymeeting.mapper;

import cn.clazs.easymeeting.entity.po.PrivateChatUnread;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 私聊未读消息计数 Mapper 接口
 */
@Mapper
public interface PrivateChatUnreadMapper {

    /**
     * 增加未读消息数
     * 如果记录不存在则插入，存在则更新
     *
     * @param userId             用户ID（消息接收者）
     * @param contactId          联系人ID（消息发送者）
     * @param lastMessageTime    最后消息时间
     * @param lastMessageContent 最后消息内容预览
     * @return 影响行数
     */
    int incrementUnreadCount(@Param("userId") String userId,
                             @Param("contactId") String contactId,
                             @Param("lastMessageTime") Long lastMessageTime,
                             @Param("lastMessageContent") String lastMessageContent);

    /**
     * 清零未读消息数
     *
     * @param userId    用户ID
     * @param contactId 联系人ID
     * @return 影响行数
     */
    int clearUnreadCount(@Param("userId") String userId,
                         @Param("contactId") String contactId);

    /**
     * 查询单个会话的未读消息数
     *
     * @param userId    用户ID
     * @param contactId 联系人ID
     * @return 未读消息数，不存在返回 null
     */
    Integer getUnreadCount(@Param("userId") String userId,
                           @Param("contactId") String contactId);

    /**
     * 查询用户所有会话的未读消息记录
     *
     * @param userId 用户ID
     * @return 未读消息记录列表
     */
    List<PrivateChatUnread> selectByUserId(@Param("userId") String userId);

    /**
     * 查询用户的总未读消息数
     *
     * @param userId 用户ID
     * @return 总未读消息数
     */
    Integer getTotalUnreadCount(@Param("userId") String userId);
}
