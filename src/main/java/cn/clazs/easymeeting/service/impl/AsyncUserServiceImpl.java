package cn.clazs.easymeeting.service.impl;

import cn.clazs.easymeeting.entity.po.UserInfo;
import cn.clazs.easymeeting.mapper.UserMapper;
import cn.clazs.easymeeting.util.StringUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 用户信息异步更新服务
 * 用于异步执行非关键业务，如：更新登录时间、退出时间等
 * 使用自定义线程池：userInfoAsyncExecutor
 */
@Service("asyncUserInfoService")
@RequiredArgsConstructor
@Slf4j
public class AsyncUserServiceImpl {

    private final UserMapper userInfoMapper;

    /**
     * 异步更新用户最后登录时间
     *
     * @param userInfo 用户信息
     */
    @Async("asyncTaskExecutor")
    public void updateLoginTime(UserInfo userInfo) {
        try {
            if (StringUtil.isEmpty(userInfo.getUserId())) {
                log.error("异步更新登录时间失败：用户ID为空");
                return;
            }
            userInfo.setLastLoginTime(System.currentTimeMillis());
            userInfoMapper.updateById(userInfo);
            log.info("用户 [{}] 登录时间更新成功", userInfo.getUserId());
        } catch (Exception e) {
            // 异步方法中的异常无法被调用方感知，须使用日志记录
            log.error("异步更新用户 [{}] 登录时间失败", userInfo.getUserId(), e);
        }
    }

    /**
     * 异步更新用户最后退出时间
     *
     * @param userInfo 用户信息
     */
    @Async("asyncTaskExecutor")
    public void updateLogoutTime(UserInfo userInfo) {
        try {
            if (StringUtil.isEmpty(userInfo.getUserId())) {
                log.error("异步更新退出时间失败：用户ID为空");
                return;
            }
            userInfo.setLastOffTime(System.currentTimeMillis());
            userInfoMapper.updateById(userInfo);
            log.info("用户 [{}] 退出时间更新成功", userInfo.getUserId());
        } catch (Exception e) {
            log.error("异步更新用户 [{}] 退出时间失败", userInfo.getUserId(), e);
        }
    }
}
