package cn.clazs.easymeeting.mapper;

import cn.clazs.easymeeting.entity.po.MeetingInfo;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

public interface MeetingInfoMapper {

    int insert(MeetingInfo meetingInfo);

    int updateById(MeetingInfo meetingInfo);

    MeetingInfo selectById(@Param("meetingId") String meetingId);

    /**
     * 批量查询会议信息
     * @param meetingIds 会议ID集合
     * @return 会议信息列表
     */
    List<MeetingInfo> selectByIds(@Param("meetingIds") Collection<String> meetingIds);

    MeetingInfo selectByMeetingNo(@Param("meetingNo") String meetingNo);

    /**
     * 根据会议号查询正在进行的会议（按创建时间倒序，取最新的一条）
     */
    MeetingInfo selectRunningMeetingByMeetingNo(@Param("meetingNo") String meetingNo);

    List<MeetingInfo> selectByCreateUserId(@Param("createUserId") String createUserId);

    List<MeetingInfo> selectList(@Param("status") Integer status);

    int deleteById(@Param("meetingId") String meetingId);

    // ==================== 分页查询历史会议 ====================

    /**
     * 查询所有历史会议（我创建的 + 我参加的）
     */
    List<MeetingInfo> selectAllMeetings(@Param("userId") String userId, @Param("offset") int offset, @Param("pageSize") int pageSize);

    Long countAllMeetings(@Param("userId") String userId);

    /**
     * 查询我创建的会议
     */
    List<MeetingInfo> selectCreatedMeetings(@Param("userId") String userId, @Param("offset") int offset, @Param("pageSize") int pageSize);

    Long countCreatedMeetings(@Param("userId") String userId);

    /**
     * 查询我参加的会议（不包括我创建的）
     */
    List<MeetingInfo> selectJoinedMeetings(@Param("userId") String userId, @Param("offset") int offset, @Param("pageSize") int pageSize);

    Long countJoinedMeetings(@Param("userId") String userId);
}
