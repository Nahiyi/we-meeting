package cn.clazs.easymeeting.controller;

import cn.clazs.easymeeting.context.UserContext;
import cn.clazs.easymeeting.entity.dto.UserTokenInfoDTO;
import cn.clazs.easymeeting.entity.enums.MeetingReserveStatus;
import cn.clazs.easymeeting.entity.po.MeetingReserve;
import cn.clazs.easymeeting.entity.vo.ResponseVO;
import cn.clazs.easymeeting.service.MeetingReserveService;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/meetingReserve")
@Validated
@Slf4j
@RequiredArgsConstructor
public class MeetingReserveController {

    private final MeetingReserveService meetingReserveService;

    /**
     * 加载预约会议列表（未开始的，按开始时间倒序）
     */
    @RequestMapping("/loadMeetingReserve")
    public ResponseVO<List<MeetingReserve>> loadMeetingReserve() {
        UserTokenInfoDTO userTokenInfoDTO = UserContext.getCurrentUser();
        String userId = userTokenInfoDTO.getUserId();
        // 查询未开始的预约会议，按 start_time desc 排序
        List<MeetingReserve> list = meetingReserveService.loadCreatedReservesByStatus(
                userId, MeetingReserveStatus.NO_START.getStatus());
        return ResponseVO.success(list);
    }

    /**
     * 创建预约会议
     * TODO 后面添加联系人后要有一个联系人接口，补充下面的inviteUserId
     */
    @RequestMapping("/createMeetingReserve")
    public ResponseVO<Void> createMeetingReserve(MeetingReserve meetingReserve,
                                                 @RequestParam(required = false) List<String> inviteUserId) {
        UserTokenInfoDTO userTokenInfoDTO = UserContext.getCurrentUser();
        meetingReserve.setCreateUserId(userTokenInfoDTO.getUserId());
        meetingReserveService.createReserve(meetingReserve, inviteUserId);
        return ResponseVO.success();
    }

    /**
     * 创建人删除预约会议
     */
    @RequestMapping("/delMeetingReserve")
    public ResponseVO<Void> delMeetingReserve(@NotEmpty String meetingId) {
        UserTokenInfoDTO userTokenInfoDTO = UserContext.getCurrentUser();
        String userId = userTokenInfoDTO.getUserId();
        meetingReserveService.deleteReserve(meetingId, userId);
        return ResponseVO.success();
    }

    /**
     * 用户退出预约会议（不想参加了）
     */
    @RequestMapping("/delMeetingReserveByUser")
    public ResponseVO<Void> delMeetingReserveByUser(@NotEmpty String meetingId) {
        UserTokenInfoDTO userTokenInfoDTO = UserContext.getCurrentUser();
        String userId = userTokenInfoDTO.getUserId();
        meetingReserveService.removeInviteMember(meetingId, userId);
        return ResponseVO.success();
    }

    /**
     * 获取今天的会议（创建的 + 被邀请的）
     */
    @RequestMapping("/loadTodayMeeting")
    public ResponseVO<List<MeetingReserve>> loadTodayMeeting() {
        UserTokenInfoDTO userTokenInfoDTO = UserContext.getCurrentUser();
        String userId = userTokenInfoDTO.getUserId();
        List<MeetingReserve> list = meetingReserveService.loadTodayReserves(userId);
        return ResponseVO.success(list);
    }

    /**
     * 开始预约会议 - 创建人点击开始
     * @param reserveId 预约会议ID
     * @return 实际会议ID (MeetingInfo.meetingId)
     */
    @RequestMapping("/startReserveMeeting")
    public ResponseVO<String> startReserveMeeting(@NotEmpty String reserveId) {
        UserTokenInfoDTO userTokenInfoDTO = UserContext.getCurrentUser();
        String realMeetingId = meetingReserveService.startReserveMeeting(reserveId, userTokenInfoDTO);
        return ResponseVO.success(realMeetingId);
    }

    /**
     * 加入预约会议 - 任何已认证用户加入
     * @param reserveId 预约会议ID
     * @return 实际会议ID
     */
    @RequestMapping("/joinReserveMeeting")
    public ResponseVO<String> joinReserveMeeting(@NotEmpty String reserveId) {
        UserTokenInfoDTO userTokenInfoDTO = UserContext.getCurrentUser();
        String realMeetingId = meetingReserveService.joinReserveMeeting(reserveId, userTokenInfoDTO);
        return ResponseVO.success(realMeetingId);
    }
}
