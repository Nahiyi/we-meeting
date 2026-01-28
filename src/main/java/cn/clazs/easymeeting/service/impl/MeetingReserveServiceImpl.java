package cn.clazs.easymeeting.service.impl;

import cn.clazs.easymeeting.entity.dto.UserTokenInfoDTO;
import cn.clazs.easymeeting.entity.enums.MeetingReserveStatus;
import cn.clazs.easymeeting.entity.enums.MeetingStatus;
import cn.clazs.easymeeting.entity.po.MeetingInfo;
import cn.clazs.easymeeting.entity.po.MeetingReserve;
import cn.clazs.easymeeting.entity.po.MeetingReserveMember;
import cn.clazs.easymeeting.entity.vo.PageResult;
import cn.clazs.easymeeting.exception.BusinessException;
import cn.clazs.easymeeting.mapper.MeetingReserveMapper;
import cn.clazs.easymeeting.mapper.MeetingReserveMemberMapper;
import cn.clazs.easymeeting.redis.RedisComponent;
import cn.clazs.easymeeting.service.MeetingInfoService;
import cn.clazs.easymeeting.service.MeetingReserveService;
import cn.clazs.easymeeting.util.StringUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class MeetingReserveServiceImpl implements MeetingReserveService {

    private final MeetingReserveMapper meetingReserveMapper;
    private final MeetingReserveMemberMapper meetingReserveMemberMapper;
    private final MeetingInfoService meetingInfoService;
    private final RedisComponent redisComponent;

    /**
     * 创建一个会议预约
     * @param inviteUserIds 该会议的邀请成员
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public MeetingReserve createReserve(MeetingReserve meetingReserve, List<String> inviteUserIds) {
        // 1. 预先创建实际会议（MeetingInfo），其状态为 SCHEDULED
        MeetingInfo meetingInfo = new MeetingInfo();
        meetingInfo.setMeetingName(meetingReserve.getMeetingName());
        meetingInfo.setJoinType(meetingReserve.getJoinType());
        meetingInfo.setJoinPassword(meetingReserve.getJoinPassword());
        meetingInfo.setCreateUserId(meetingReserve.getCreateUserId());
        meetingInfo.setStartTime(meetingReserve.getStartTime());
        if (meetingReserve.getDuration() != null) {
            meetingInfo.setEndTime(meetingReserve.getStartTime().plusMinutes(meetingReserve.getDuration()));
        }
        meetingInfoService.createScheduledMeeting(meetingInfo);

        // 2. 生成预约会议记录
        meetingReserve.setMeetingId(StringUtil.generateMeetingNo());
        meetingReserve.setCreateTime(LocalDateTime.now());
        meetingReserve.setStatus(MeetingReserveStatus.NO_START.getStatus()); // 待开始-对应SCHEDULED
        // 立刻关联 realMeetingId，使得可以查到一个真实可加入的会议号
        meetingReserve.setRealMeetingId(meetingInfo.getMeetingId());

        meetingReserveMapper.insert(meetingReserve);

        // 添加邀请成员
        if (inviteUserIds != null && !inviteUserIds.isEmpty()) {
            List<MeetingReserveMember> members = new ArrayList<>();
            for (String userId : inviteUserIds) {
                MeetingReserveMember member = new MeetingReserveMember();
                member.setMeetingId(meetingReserve.getMeetingId());
                member.setInviteUserId(userId);
                members.add(member);
            }
            meetingReserveMemberMapper.batchInsert(members);
        }

        log.info("创建预约会议成功: meetingId={}, meetingName={}",
                meetingReserve.getMeetingId(), meetingReserve.getMeetingName());
        return meetingReserve;
    }

    @Override
    public MeetingReserve updateReserve(MeetingReserve meetingReserve) {
        MeetingReserve existing = meetingReserveMapper.selectById(meetingReserve.getMeetingId());
        if (existing == null) {
            throw new BusinessException("预约会议不存在");
        }
        meetingReserveMapper.updateById(meetingReserve);
        return meetingReserveMapper.selectById(meetingReserve.getMeetingId());
    }

    @Override
    public MeetingReserve getReserveById(String meetingId) {
        return meetingReserveMapper.selectById(meetingId);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteReserve(String meetingId, String userId) {
        MeetingReserve existing = meetingReserveMapper.selectById(meetingId);
        if (existing == null) {
            throw new BusinessException("预约会议不存在");
        }
        if (!existing.getCreateUserId().equals(userId)) {
            throw new BusinessException("只有创建者才能删除预约会议");
        }
        // 如果关联了实际会议，且会议未开始（或根据业务需要），一并删除实际会议
        String realMeetingId = existing.getRealMeetingId();
        if (StringUtil.isNotEmpty(realMeetingId)) {
            // 尝试删除实际会议，忽略可能的异常（如会议已删除）
            try {
                meetingInfoService.deleteMeeting(realMeetingId);
                log.info("级联删除实际会议成功: realMeetingId={}", realMeetingId);
            } catch (Exception e) {
                log.warn("级联删除实际会议失败或会议不存在: realMeetingId={}", realMeetingId);
            }
        }

        // 先删除邀请成员
        meetingReserveMemberMapper.deleteByMeetingId(meetingId);
        // 再删除预约会议
        meetingReserveMapper.deleteById(meetingId);
        log.info("删除预约会议成功: meetingId={}", meetingId);
    }

    @Override
    public void cancelReserve(String meetingId, String userId) {
        MeetingReserve existing = meetingReserveMapper.selectById(meetingId);
        if (existing == null) {
            throw new BusinessException("预约会议不存在");
        }
        if (!existing.getCreateUserId().equals(userId)) {
            throw new BusinessException("只有创建者才能取消预约会议");
        }
        
        // 如果关联了实际会议，且会议未开始，一并删除实际会议
        String realMeetingId = existing.getRealMeetingId();
        if (StringUtil.isNotEmpty(realMeetingId)) {
            try {
                // 直接物理删除未开始的会议，或者也可以选择将其状态更新为取消（如果MeetingInfo有取消状态的话）
                // 目前MeetingInfo没有CANCELLED状态，且SCHEDULED状态的会议未产生实质数据，物理删除是合适的
                meetingInfoService.deleteMeeting(realMeetingId);
                log.info("级联删除实际会议成功: realMeetingId={}", realMeetingId);
            } catch (Exception e) {
                log.warn("级联删除实际会议失败或会议不存在: realMeetingId={}", realMeetingId);
            }
        }

        existing.setStatus(MeetingReserveStatus.CANCELLED.getStatus()); // 已取消
        meetingReserveMapper.updateById(existing);
        log.info("取消预约会议成功: meetingId={}", meetingId);
    }

    @Override
    public PageResult<MeetingReserve> loadCreatedReserves(String userId, Integer pageNo, Integer pageSize) {
        int offset = (pageNo - 1) * pageSize;
        List<MeetingReserve> list = meetingReserveMapper.selectCreatedReserves(userId, offset, pageSize);
        Long total = meetingReserveMapper.countCreatedReserves(userId);
        return new PageResult<>(pageNo, pageSize, total, list);
    }

    @Override
    public PageResult<MeetingReserve> loadInvitedReserves(String userId, Integer pageNo, Integer pageSize) {
        int offset = (pageNo - 1) * pageSize;
        List<MeetingReserve> list = meetingReserveMapper.selectInvitedReserves(userId, offset, pageSize);
        Long total = meetingReserveMapper.countInvitedReserves(userId);
        return new PageResult<>(pageNo, pageSize, total, list);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addInviteMembers(String meetingId, List<String> inviteUserIds) {
        MeetingReserve existing = meetingReserveMapper.selectById(meetingId);
        if (existing == null) {
            throw new BusinessException("预约会议不存在");
        }
        if (inviteUserIds != null && !inviteUserIds.isEmpty()) {
            List<MeetingReserveMember> members = new ArrayList<>();
            for (String userId : inviteUserIds) {
                // 检查是否已存在
                MeetingReserveMember existingMember = meetingReserveMemberMapper
                        .selectByMeetingIdAndUserId(meetingId, userId);
                if (existingMember == null) {
                    MeetingReserveMember member = new MeetingReserveMember();
                    member.setMeetingId(meetingId);
                    member.setInviteUserId(userId);
                    members.add(member);
                }
            }
            if (!members.isEmpty()) {
                meetingReserveMemberMapper.batchInsert(members);
            }
        }
    }

    @Override
    public void removeInviteMember(String meetingId, String inviteUserId) {
        meetingReserveMemberMapper.deleteByMeetingIdAndUserId(meetingId, inviteUserId);
    }

    @Override
    public List<MeetingReserveMember> getInviteMembers(String meetingId) {
        return meetingReserveMemberMapper.selectByMeetingId(meetingId);
    }

    @Override
    public List<MeetingReserve> loadCreatedReservesByStatus(String userId, Integer status) {
        return meetingReserveMapper.selectCreatedReservesByStatus(userId, status);
    }

    @Override
    public List<MeetingReserve> loadTodayReserves(String userId) {
        List<MeetingReserve> reserves = meetingReserveMapper.selectTodayReserves(userId);
        // 获取主键集合，批量查询
        Set<String> realMeetingIds = reserves.stream()
                .map(MeetingReserve::getRealMeetingId)
                .collect(Collectors.toSet());
        // 真实会议ID和入会使用的会议号的映射
        Map<String, String> idAndNoMap = meetingInfoService.getMeetingIdAndNoMapByIds(realMeetingIds);
        reserves.forEach(meetingReserve -> {
            // ps.这里不必再定义一个新的VO，直接让“会议号”覆盖“真实会议ID”字段位置即可
            String realMeetingId = meetingReserve.getRealMeetingId();
            meetingReserve.setRealMeetingId(idAndNoMap.get(realMeetingId));
        });
        return reserves;
    }

    /**
     * 开始一个会议，返回real会议ID：会议表记录ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String startReserveMeeting(String reserveId, UserTokenInfoDTO currentUser) {
        // 1. 查询预约会议
        MeetingReserve reserve = meetingReserveMapper.selectById(reserveId);
        if (reserve == null) {
            throw new BusinessException("预约会议不存在");
        }

        // 2. 验证是否为创建人
        if (!reserve.getCreateUserId().equals(currentUser.getUserId())) {
            throw new BusinessException("只有创建人才能开始会议");
        }

        String realMeetingId = reserve.getRealMeetingId();

        // 3. 处理开始逻辑（状态的一并设置：变为已开始/进行中）
        if (StringUtil.isNotEmpty(realMeetingId)) {
            // 已有 realMeetingId
            if (!MeetingReserveStatus.RUNNING.getStatus().equals(reserve.getStatus())) {
                // 如果未开始，则更新状态为进行中
                // Update MeetingInfo
                MeetingInfo meetingInfo = new MeetingInfo();
                meetingInfo.setMeetingId(realMeetingId);
                meetingInfo.setStatus(MeetingStatus.RUNNING.getStatus());
                meetingInfoService.updateMeeting(meetingInfo);

                // Update MeetingReserve
                reserve.setStatus(MeetingReserveStatus.RUNNING.getStatus());
                meetingReserveMapper.updateById(reserve);
            }
        } else {
            // @Deprecated：无 realMeetingId，创建实际会议（复用 quickMeeting 逻辑）
            MeetingInfo meetingInfo = new MeetingInfo();
            meetingInfo.setMeetingName(reserve.getMeetingName());
            meetingInfo.setJoinType(reserve.getJoinType());
            meetingInfo.setJoinPassword(reserve.getJoinPassword());
            meetingInfo.setCreateUserId(currentUser.getUserId());

            meetingInfoService.quickMeeting(meetingInfo, currentUser.getNickName());
            realMeetingId = meetingInfo.getMeetingId();

            // 更新预约会议的 realMeetingId 和 status
            reserve.setRealMeetingId(realMeetingId);
            reserve.setStatus(MeetingReserveStatus.RUNNING.getStatus()); // 进行中
            meetingReserveMapper.updateById(reserve);
        }

        // 4. 设置 currentMeetingId 到 token
        currentUser.setCurrentMeetingId(realMeetingId);
        currentUser.setCurrentNickName(currentUser.getNickName());
        redisComponent.updateUserTokenInfo(currentUser);

        log.info("开始预约会议成功: reserveId={}, realMeetingId={}", reserveId, realMeetingId);
        return realMeetingId;
    }

    @Override
    public String joinReserveMeeting(String reserveId, UserTokenInfoDTO currentUser) {
        // 1. 查询预约会议
        MeetingReserve reserve = meetingReserveMapper.selectById(reserveId);
        if (reserve == null) {
            throw new BusinessException("预约会议不存在");
        }

        // 2. 验证会议是否已开始
        // 必须有 realMeetingId 且 状态为进行中
        if (StringUtil.isEmpty(reserve.getRealMeetingId())
                || MeetingReserveStatus.NO_START.getStatus().equals(reserve.getStatus())) {
            throw new BusinessException("会议尚未开始");
        }

        // 3. 设置 currentMeetingId 到 token
        currentUser.setCurrentMeetingId(reserve.getRealMeetingId());
        currentUser.setCurrentNickName(currentUser.getNickName());
        redisComponent.updateUserTokenInfo(currentUser);

        // 4. 返回实际会议ID
        return reserve.getRealMeetingId();
    }
}
