package cn.clazs.easymeeting.service.impl;

import cn.clazs.easymeeting.config.AppConfig;
import cn.clazs.easymeeting.entity.dto.LoginDTO;
import cn.clazs.easymeeting.entity.dto.RegisterDTO;
import cn.clazs.easymeeting.entity.dto.UserTokenInfoDTO;
import cn.clazs.easymeeting.entity.enums.UserStatus;
import cn.clazs.easymeeting.entity.po.UserInfo;
import cn.clazs.easymeeting.entity.vo.UserInfoVO;
import cn.clazs.easymeeting.exception.BusinessException;
import cn.clazs.easymeeting.mapper.UserMapper;
import cn.clazs.easymeeting.redis.RedisComponent;
import cn.clazs.easymeeting.service.UserService;
import cn.clazs.easymeeting.util.CopyUtil;
import cn.clazs.easymeeting.util.JwtUtil;
import cn.clazs.easymeeting.util.StringUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service("userInfoService")
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final AsyncUserServiceImpl asyncUserInfoService;
    private final AppConfig appConfig;
    private final RedisComponent redisComponent;

    @Override
    public void register(RegisterDTO dto) throws BusinessException {
        UserInfo userInfo = this.userMapper.selectByEmail(dto.getEmail());
        if (userInfo != null) {
            throw new BusinessException("邮箱账号已经存在");
        }
        // 获取长度12的字符串
        String userId = StringUtil.generateUserId();
        userInfo = new UserInfo();
        userInfo.setUserId(userId);
        userInfo.setNickName(dto.getNickName());
        userInfo.setEmail(dto.getEmail());
        userInfo.setPassword(StringUtil.encodeMd5(dto.getPassword()));
        userInfo.setCreateTime(LocalDateTime.now());
        userInfo.setMeetingNo(StringUtil.generateMeetingNo());
        userInfo.setStatus(UserStatus.ENABLED.getCode());
        this.userMapper.insert(userInfo);
    }

    /**
     * 登录方式为邮箱+密码登录
     */
    @Override
    public UserInfoVO login(LoginDTO dto) throws BusinessException {
        UserInfo userInfo = this.userMapper.selectByEmail(dto.getEmail());

        if (null == userInfo || !userInfo.getPassword().equals(dto.getPassword())) {
            throw new BusinessException("账号或者密码不正确");
        }

        if (UserStatus.DISABLED.getCode().equals(userInfo.getStatus())) {
            throw new BusinessException("账号已禁用");
        }

        Long lastOffTime = userInfo.getLastOffTime();
        Long lastLoginTime = userInfo.getLastLoginTime();
        if (lastOffTime != null && lastLoginTime != null && lastOffTime <= lastLoginTime) {
            throw new BusinessException("此账号已经在别处登录，请退出后再登录");
        }

        // 获取token
        String token = JwtUtil.generateToken(userInfo.getUserId());

        // 构建TokenUserInfoDTO并存入Redis
        UserTokenInfoDTO userTokenInfoDto = CopyUtil.copy(userInfo, UserTokenInfoDTO.class);
        userTokenInfoDto.setToken(token);
        userTokenInfoDto.setMeetingNo(userInfo.getMeetingNo());
        userTokenInfoDto.setAdmin(appConfig.getAdminEmails().contains(dto.getEmail()));
        redisComponent.saveUserTokenInfo(userTokenInfoDto);

        UserInfoVO userInfoVO = CopyUtil.copy(userInfo, UserInfoVO.class);
        userInfoVO.setToken(token);
        userInfoVO.setAdmin(userTokenInfoDto.getAdmin());

        // 异步更新登录时间
        asyncUserInfoService.updateLoginTime(userInfo);

        return userInfoVO;
    }

    @Override
    public void logout(String token) throws BusinessException {
        // 根据Token获取用户信息
        UserTokenInfoDTO userTokenInfoDto = redisComponent.getUserTokenInfo(token);
        if (userTokenInfoDto == null) {
            throw new BusinessException("Token无效或已过期");
        }

        // 从数据库获取用户完整信息
        UserInfo userInfo = this.userMapper.selectById(userTokenInfoDto.getUserId());
        if (userInfo == null) {
            throw new BusinessException("用户不存在");
        }

        // 异步更新用户最后退出时间
        asyncUserInfoService.updateLogoutTime(userInfo);

        // 删除Redis中的Token
        redisComponent.removeToken(token);
    }
}