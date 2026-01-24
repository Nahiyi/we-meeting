package cn.clazs.easymeeting.controller;

import cn.clazs.easymeeting.context.UserContext;
import cn.clazs.easymeeting.entity.dto.JoinMeetingDTO;
import cn.clazs.easymeeting.entity.dto.UserTokenInfoDTO;
import cn.clazs.easymeeting.entity.enums.MeetingCreateDTO;
import cn.clazs.easymeeting.entity.enums.MeetingMemberStatus;
import cn.clazs.easymeeting.entity.enums.MeetingStatus;
import cn.clazs.easymeeting.entity.po.MeetingInfo;
import cn.clazs.easymeeting.entity.po.MeetingMember;
import cn.clazs.easymeeting.entity.vo.PageResult;
import cn.clazs.easymeeting.entity.vo.ResponseVO;
import cn.clazs.easymeeting.exception.BusinessException;
import cn.clazs.easymeeting.service.MeetingInfoService;
import cn.clazs.easymeeting.service.MeetingMemberService;
import cn.clazs.easymeeting.util.StringUtil;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/meeting")
@Validated
@Slf4j
@RequiredArgsConstructor
public class MeetingController {

    private final MeetingInfoService meetingInfoService;
    private final MeetingMemberService meetingMemberService;

    /**
     * 加载所有历史会议（我创建的 + 我参加的）
     */
    @GetMapping("/loadMeeting")
    public ResponseVO<PageResult<MeetingInfo>> loadMeeting(
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        // 基于 UserContext 获取当前用户 ID
        String userId = UserContext.getCurrentUserId();

        PageResult<MeetingInfo> result = meetingInfoService.loadAllMeetings(userId, pageNo, pageSize);
        return ResponseVO.success(result);
    }

    /**
     * 加载我创建的会议
     */
    @GetMapping("/loadMyCreatedMeeting")
    public ResponseVO<PageResult<MeetingInfo>> loadMyCreatedMeeting(
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        String userId = UserContext.getCurrentUserId();

        PageResult<MeetingInfo> result = meetingInfoService.loadCreatedMeetings(userId, pageNo, pageSize);
        return ResponseVO.success(result);
    }

    /**
     * 加载我参加的会议（不包括我创建的）
     */
    @GetMapping("/loadMyJoinedMeeting")
    public ResponseVO<PageResult<MeetingInfo>> loadMyJoinedMeeting(
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        String userId = UserContext.getCurrentUserId();

        PageResult<MeetingInfo> result = meetingInfoService.loadJoinedMeetings(userId, pageNo, pageSize);
        return ResponseVO.success(result);
    }

    /**
     * 快速会议 - 创建会议
     * 使用个人会议号还是系统生成，会议主题，是否需要会议密码
     */
    @PostMapping("/quickMeeting")
    public ResponseVO<String> quickMeeting(@RequestBody @Validated MeetingCreateDTO meetingCreateDTO) {
        UserTokenInfoDTO currentUser = UserContext.getCurrentUser();
        if (currentUser.getCurrentMeetingId() != null) {
            throw new BusinessException("你有未结束的会议，无法创建新的会议");
        }
        // 创建MeetingInfo实体类
        MeetingInfo meetingInfo = new MeetingInfo();
        meetingInfo.setMeetingName(meetingCreateDTO.getMeetingName());
        // 会议号，若有就用用户传入的，无则使用用户的会议号
        meetingInfo.setMeetingNo(meetingCreateDTO.getMeetingNoType() == 0 ? currentUser.getMeetingNo() : StringUtil.generateMeetingNo());
        meetingInfo.setJoinPassword(meetingCreateDTO.getJoinPassword());
        meetingInfo.setCreateUserId(currentUser.getUserId());

        meetingInfoService.quickMeeting(meetingInfo, currentUser.getNickName());

        return ResponseVO.success(meetingInfo.getMeetingId());
    }

    /**
     * 必须先通过preJoinMeeting接口验证，前端才能调用joinMeeting接口入会
     */
    @PostMapping("/preJoinMeeting")
    public ResponseVO<String> preJoinMeeting(@NotNull String meetingNo, @NotEmpty String nickName, String password) {
        meetingNo = meetingNo.replace(" ", "");
        // 当前用户持有密码和会议号，欲加入指定会议
        String meetingId = meetingInfoService.preJoinMeeting(meetingNo, nickName, password);
        return ResponseVO.success(meetingId);
    }

    /**
     * 加入会议
     */
    @PostMapping("/joinMeeting")
    public ResponseVO<Void> joinMeeting(@RequestBody JoinMeetingDTO joinMeetingDTO) {
        UserTokenInfoDTO currentUser = UserContext.getCurrentUser();
        // 验证用户是否已通过 preJoinMeeting
        // currentMeetingId 在 preJoinMeeting 中设置，如果为空说明没有通过预验证
        if (StringUtil.isEmpty(currentUser.getCurrentMeetingId())) {
            throw new BusinessException("请先通过会议号加入会议");
        }

        // 校验通过后，从 token 获取用户信息，不信任前端传入，这样即使前端伪造 userId 也无效
        joinMeetingDTO.setUserId(currentUser.getUserId());
        joinMeetingDTO.setNickName(currentUser.getNickName());
        joinMeetingDTO.setSex(currentUser.getSex());
        joinMeetingDTO.setMeetingId(currentUser.getCurrentMeetingId());

        meetingInfoService.joinMeeting(joinMeetingDTO);
        return ResponseVO.success();
    }

    @GetMapping("/exitMeeting")
    public ResponseVO<Void> exitMeeting() {
        meetingInfoService.exitMeetingRoom();
        return ResponseVO.success();
    }

    @GetMapping("/kickOutMeeting")
    public ResponseVO<Void> kickOutMeeting(@RequestParam String userId) {
        UserTokenInfoDTO userTokenInfoDTO = UserContext.getCurrentUser();
        meetingInfoService.forceExitMeetingRoom(userTokenInfoDTO, userId, MeetingMemberStatus.KICK_OUT);
        return ResponseVO.success();
    }

    @GetMapping("/blackMeeting")
    public ResponseVO<Void> blackMeeting(@RequestParam String userId) {
        UserTokenInfoDTO userTokenInfoDTO = UserContext.getCurrentUser();
        meetingInfoService.forceExitMeetingRoom(userTokenInfoDTO, userId, MeetingMemberStatus.BLACKLIST);
        return ResponseVO.success();
    }

    @GetMapping("/finishMeeting")
    public ResponseVO<Void> finishMeeting() {
        UserTokenInfoDTO userTokenInfoDTO = UserContext.getCurrentUser();
        meetingInfoService.finishMeeting(userTokenInfoDTO.getCurrentMeetingId(), userTokenInfoDTO.getUserId());
        return ResponseVO.success();
    }

    @GetMapping("/getCurrentMeeting")
    public ResponseVO<MeetingInfo> getCurrentMeeting() {
        String currentMeetingId = UserContext.getCurrentMeetingId();
        if (StringUtil.isEmpty(currentMeetingId)) {
            return ResponseVO.success(null);
        }
        MeetingInfo meetingInfo = meetingInfoService.getMeetingById(currentMeetingId);
        if (MeetingStatus.FINISHED.getStatus().equals(meetingInfo.getStatus())) {
            return ResponseVO.success(null);
        }
        return ResponseVO.success(meetingInfo);
    }

    /**
     * 删除历史会议的显示
     */
    @GetMapping("/delMeetingRecord")
    public ResponseVO<Void> delMeetingRecord(@NotEmpty String meetingId) {
        String userId = UserContext.getCurrentUserId();
        MeetingMember meetingMember = meetingMemberService.getMember(meetingId, userId);
        meetingMember.setStatus(MeetingMemberStatus.DEL_MEETING.getStatus());
        meetingMemberService.updateMember(meetingMember);
        return ResponseVO.success();
    }

    /**
     * 查看历史会议会议的成员信息
     */
    @GetMapping("/loadMeetingMembers")
    public ResponseVO<List<MeetingMember>> loadMeetingMembers(@NotEmpty String meetingId) {
        String currentUserId = UserContext.getCurrentUserId();
        List<MeetingMember> meetingMemberList = meetingMemberService.getMembersByMeetingId(meetingId);
        // 检查当前用户是否在会议成员列表中
        Optional<MeetingMember> currentUserMember = meetingMemberList.stream()
                .filter(member -> member.getUserId().equals(currentUserId))
                .findFirst();
        if (currentUserMember.isEmpty()) {
            throw new BusinessException("你不在会议中，无法查看成员信息");
        }
        return ResponseVO.success(meetingMemberList);
    }

    /**
     * 预约一场会议
     */
    @RequestMapping("/reserveJoinMeeting")
    public ResponseVO<Void> reserveJoinMeeting(@NotEmpty String meetingId, @NotEmpty String nickName, String password) {
        UserTokenInfoDTO userTokenInfoDTO = UserContext.getCurrentUser();
        userTokenInfoDTO.setNickName(nickName);
        meetingInfoService.reserveJoinMeeting(meetingId, userTokenInfoDTO, password);
        return ResponseVO.success(null);
    }

    /**
     * 邀请联系人加入会议
     * 邀请者必须在会议中，被邀请者必须是邀请者的好友
     * 邀请信息会保存到 Redis，有效期 3 分钟
     */
    @PostMapping("/inviteContactToMeeting")
    public ResponseVO<Void> inviteContactToMeeting(@RequestBody List<String> contactsId) {
        if (contactsId == null || contactsId.isEmpty()) {
            throw new BusinessException("邀请列表不能为空");
        }
        UserTokenInfoDTO userTokenInfoDTO = UserContext.getCurrentUser();
        meetingInfoService.inviteContact(userTokenInfoDTO, contactsId);
        return ResponseVO.success();
    }

    /**
     * 接受会议邀请
     * 被邀请用户接受邀请后，无需输入密码，直接设置 currentMeetingId
     * 然后前端跳转到会议页面，调用 joinMeeting 正式加入
     *
     * @param meetingId 会议ID
     * @return 会议ID（供前端跳转使用）
     */
    @PostMapping("/acceptInvite")
    public ResponseVO<String> acceptInvite(@RequestParam @NotEmpty String meetingId) {
        UserTokenInfoDTO userTokenInfoDTO = UserContext.getCurrentUser();
        meetingInfoService.acceptInvite(userTokenInfoDTO, meetingId);
        // 返回 meetingId，前端可以用来跳转到会议页面
        return ResponseVO.success(meetingId);
    }
}

