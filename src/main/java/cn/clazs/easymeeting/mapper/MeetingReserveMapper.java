package cn.clazs.easymeeting.mapper;

import cn.clazs.easymeeting.entity.po.MeetingReserve;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MeetingReserveMapper {

    int insert(MeetingReserve meetingReserve);

    int updateById(MeetingReserve meetingReserve);

    MeetingReserve selectById(@Param("meetingId") String meetingId);

    List<MeetingReserve> selectByCreateUserId(@Param("createUserId") String createUserId);

    List<MeetingReserve> selectByStatus(@Param("status") Integer status);

    int deleteById(@Param("meetingId") String meetingId);

    /**
     * 查询用户创建的预约会议（分页）
     */
    List<MeetingReserve> selectCreatedReserves(@Param("userId") String userId,
                                               @Param("offset") int offset,
                                               @Param("pageSize") int pageSize);

    Long countCreatedReserves(@Param("userId") String userId);

    /**
     * 查询用户被邀请的预约会议（分页）
     */
    List<MeetingReserve> selectInvitedReserves(@Param("userId") String userId,
                                               @Param("offset") int offset,
                                               @Param("pageSize") int pageSize);

    Long countInvitedReserves(@Param("userId") String userId);

    /**
     * 查询用户创建的未开始的预约会议（按开始时间倒序）
     */
    List<MeetingReserve> selectCreatedReservesByStatus(@Param("userId") String userId,
                                                       @Param("status") Integer status);

    /**
     * 查询用户今天的预约会议（创建的 + 被邀请的）
     */
    List<MeetingReserve> selectTodayReserves(@Param("userId") String userId);

    /**
     * 根据实际会议ID查询预约会议
     */
    MeetingReserve selectByRealMeetingId(@Param("realMeetingId") String realMeetingId);

    /**
     * 查询需要自动开始的会议
     */
    List<MeetingReserve> selectReservesToStart(@Param("status") Integer status, @Param("now") String now);
}
