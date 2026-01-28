package cn.clazs.easymeeting.task;

import cn.clazs.easymeeting.entity.enums.MeetingReserveStatus;
import cn.clazs.easymeeting.entity.enums.MeetingStatus;
import cn.clazs.easymeeting.entity.po.MeetingInfo;
import cn.clazs.easymeeting.entity.po.MeetingReserve;
import cn.clazs.easymeeting.mapper.MeetingInfoMapper;
import cn.clazs.easymeeting.mapper.MeetingReserveMapper;
import cn.clazs.easymeeting.util.StringUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class MeetingStatusTask {

    private final MeetingReserveMapper meetingReserveMapper;
    private final MeetingInfoMapper meetingInfoMapper;

    /**
     * 每分钟检查一次，自动开启已到达开始时间的预约会议
     */
    @Scheduled(cron = "0 0/1 * * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void checkMeetingStatus() {
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        List<MeetingReserve> reserves = meetingReserveMapper.selectReservesToStart(
                MeetingReserveStatus.NO_START.getStatus(), now);

        if (reserves.isEmpty()) {
            return;
        }

        log.info("扫描到 {} 个会议需要自动开始", reserves.size());

        for (MeetingReserve reserve : reserves) {
            try {
                startMeeting(reserve);
            } catch (Exception e) {
                log.error("自动开始会议失败: reserveId={}", reserve.getMeetingId(), e);
            }
        }
    }

    private void startMeeting(MeetingReserve reserve) {
        // 1. 更新预约表状态
        reserve.setStatus(MeetingReserveStatus.RUNNING.getStatus());
        meetingReserveMapper.updateById(reserve);

        // 2. 更新会议表状态
        if (StringUtil.isNotEmpty(reserve.getRealMeetingId())) {
            MeetingInfo meetingInfo = new MeetingInfo();
            meetingInfo.setMeetingId(reserve.getRealMeetingId());
            meetingInfo.setStatus(MeetingStatus.RUNNING.getStatus());
            meetingInfoMapper.updateById(meetingInfo);
            log.info("会议自动开始成功: reserveId={}, realMeetingId={}", reserve.getMeetingId(), reserve.getRealMeetingId());
        } else {
            log.warn("预约记录缺少 realMeetingId: {}", reserve.getMeetingId());
        }
    }
}
