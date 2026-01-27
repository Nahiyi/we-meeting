package cn.clazs.easymeeting.mapper;

import cn.clazs.easymeeting.entity.po.PrivateChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 私聊消息 Mapper 接口
 * 支持动态表名，用于分表查询
 */
@Mapper
public interface PrivateChatMessageMapper {

    /**
     * 插入消息
     *
     * @param tableName 分表名
     * @param message   消息实体
     * @return 影响行数
     */
    int insert(@Param("tableName") String tableName,
               @Param("message") PrivateChatMessage message);

    /**
     * 根据消息ID查询
     *
     * @param tableName 分表名
     * @param messageId 消息ID
     * @return 消息实体
     */
    PrivateChatMessage selectById(@Param("tableName") String tableName,
                                  @Param("messageId") Long messageId);

    /**
     * 根据会话ID查询消息列表（分页，按发送时间倒序）
     *
     * @param tableName 分表名
     * @param sessionId 会话ID
     * @param offset    偏移量
     * @param limit     每页数量
     * @return 消息列表
     */
    List<PrivateChatMessage> selectBySessionId(@Param("tableName") String tableName,
                                               @Param("sessionId") String sessionId,
                                               @Param("offset") Integer offset,
                                               @Param("limit") Integer limit);

    /**
     * 统计会话消息数量
     *
     * @param tableName 分表名
     * @param sessionId 会话ID
     * @return 消息数量
     */
    Long countBySessionId(@Param("tableName") String tableName,
                          @Param("sessionId") String sessionId);

    /**
     * 查询指定消息ID之前的消息（用于加载更多历史消息）
     *
     * @param tableName    分表名
     * @param sessionId    会话ID
     * @param maxMessageId 最大消息ID（查询比这个ID更早的消息）
     * @param limit        数量限制
     * @return 消息列表
     */
    List<PrivateChatMessage> selectBeforeMessageId(@Param("tableName") String tableName,
                                                   @Param("sessionId") String sessionId,
                                                   @Param("maxMessageId") Long maxMessageId,
                                                   @Param("limit") Integer limit);

    /**
     * 更新消息状态
     *
     * @param tableName 分表名
     * @param messageId 消息ID
     * @param status    新状态
     * @return 影响行数
     */
    int updateStatus(@Param("tableName") String tableName,
                     @Param("messageId") Long messageId,
                     @Param("status") Integer status);
}
