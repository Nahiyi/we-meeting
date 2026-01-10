package cn.clazs.easymeeting.service.impl;


import cn.clazs.easymeeting.config.AppConfig;
import cn.clazs.easymeeting.constant.Constants;
import cn.clazs.easymeeting.entity.dto.TokenUserInfoDto;
import cn.clazs.easymeeting.entity.enums.PageSize;
import cn.clazs.easymeeting.entity.enums.UserStatusEnum;
import cn.clazs.easymeeting.entity.po.UserInfo;
import cn.clazs.easymeeting.entity.query.PageQuery;
import cn.clazs.easymeeting.entity.query.UserInfoQuery;
import cn.clazs.easymeeting.entity.vo.PaginationResultVO;
import cn.clazs.easymeeting.entity.vo.UserInfoVO;
import cn.clazs.easymeeting.exception.BusinessException;
import cn.clazs.easymeeting.mapper.UserInfoMapper;
import cn.clazs.easymeeting.redis.RedisComponent;
import cn.clazs.easymeeting.service.UserInfoService;
import cn.clazs.easymeeting.util.CopyUtils;
import cn.clazs.easymeeting.util.StringTools;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Service("userInfoService")
public class UserInfoServiceImpl implements UserInfoService {

    @Resource
    private UserInfoMapper<UserInfo, UserInfoQuery> userInfoMapper;

    @Resource
    private AppConfig appConfig;

    @Resource
    private RedisComponent redisComponent;

    /**
     * 根据条件查询列表
     */
    public List<UserInfo> findListByParam(UserInfoQuery query) {
        return this.userInfoMapper.selectList(query);
    }

    /**
     * 根据条件查询数量
     */
    public Integer findCountByParam(UserInfoQuery query) {
        return this.userInfoMapper.selectCount(query);
    }

    /**
     * 分页查询
     */
    public PaginationResultVO<UserInfo> findListByPage(UserInfoQuery query) {
        Integer count = this.findCountByParam(query);
        Integer pageSize = query.getPageSize() == null ? PageSize.SIZE15.getSize() : query.getPageSize();
        PageQuery page = new PageQuery(query.getPageNo(), count, pageSize);
        query.setPageQuery(page);
        List<UserInfo> list = this.findListByParam(query);
        PaginationResultVO<UserInfo> result = new PaginationResultVO<>(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
        return result;
    }

    /**
     * 新增
     */
    public Integer add(UserInfo bean) {
        return this.userInfoMapper.insert(bean);
    }

    /**
     * 批量新增
     */
    public Integer addBatch(List<UserInfo> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.userInfoMapper.insertBatch(listBean);
    }

    /**
     * 批量新增或者修改
     */
    public Integer addOrUpdateBatch(List<UserInfo> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.userInfoMapper.insertOrUpdateBatch(listBean);
    }

    /**
     * 根据UserId查询
     */
    public UserInfo getUserInfoByUserId(String userId) {
        return this.userInfoMapper.selectByUserId(userId);
    }

    /**
     * 根据UserId更新
     */
    public Integer updateUserInfoByUserId(UserInfo bean, String userId) {
        return this.userInfoMapper.updateByUserId(bean, userId);
    }

    /**
     * 根据UserId删除
     */
    public Integer deleteUserInfoByUserId(String userId) {
        return this.userInfoMapper.deleteByUserId(userId);
    }

    /**
     * 根据Email查询
     */
    public UserInfo getUserInfoByEmail(String email) {
        return this.userInfoMapper.selectByEmail(email);
    }

    /**
     * 根据Email更新
     */
    public Integer updateUserInfoByEmail(UserInfo bean, String email) {
        return this.userInfoMapper.updateByEmail(bean, email);
    }

    /**
     * 根据Email删除
     */
    public Integer deleteUserInfoByEmail(String email) {
        return this.userInfoMapper.deleteByEmail(email);
    }

    @Override
    public void register(String email, String nickName, String password) throws BusinessException {
        UserInfo userInfo = this.userInfoMapper.selectByEmail(email);
        if (userInfo != null) {
            throw new BusinessException("邮箱账号已经存在");
        }
        Date curDate = new Date();
        // 获取长度12的字符串
        String userId = StringTools.getRandomNumber(Constants.LENGTH_12);
        userInfo = new UserInfo();
        userInfo.setUserId(userId);
        userInfo.setNickName(nickName);
        userInfo.setEmail(email);
        userInfo.setPassword(StringTools.encodeMd5(password));
        userInfo.setCreateTime(curDate);
        // userInfo.setLastOffTime(curDate.getTime());
        userInfo.setMeetingNo(StringTools.getMeetingNoOrMeetingId());
        userInfo.setStatus(UserStatusEnum.ENABLED.getCode());
        this.userInfoMapper.insert(userInfo);
    }

    /**
     * 登录方式为邮箱+密码登录
     * TODO: 更新最后登录时间
     * 2.退出登录接口，更新最后off时间
     */
    @Override
    public UserInfoVO login(String email, String password) throws BusinessException {
        UserInfo userInfo = this.userInfoMapper.selectByEmail(email);

        if (null == userInfo || !userInfo.getPassword().equals(password)) {
            throw new BusinessException("账号或者密码不正确");
        }

        if (UserStatusEnum.DISABLED.getCode().equals(userInfo.getStatus())) {
            throw new BusinessException("账号已禁用");
        }

        Long lastOffTime = userInfo.getLastOffTime();
        Long lastLoginTime = userInfo.getLastLoginTime();
        if (lastOffTime != null && lastLoginTime != null && lastOffTime <= lastLoginTime) {
            throw new BusinessException("此账号已经在别处登录，请退出后再登录");
        }

        TokenUserInfoDto tokenUserInfoDto = CopyUtils.copy(userInfo, TokenUserInfoDto.class);

        // 获取token
        String token = StringTools.encodeMd5(tokenUserInfoDto.getUserId() + StringTools.getRandomString(Constants.LENGTH_20));

        tokenUserInfoDto.setToken(token);
        tokenUserInfoDto.setMyMeetingNo(userInfo.getMeetingNo());
        tokenUserInfoDto.setAdmin(appConfig.getAdminEmails().contains(email));

        redisComponent.saveTokenUserInfoDto(tokenUserInfoDto);

        UserInfoVO userInfoVO = CopyUtils.copy(userInfo, UserInfoVO.class);
        userInfoVO.setToken(token);
        userInfoVO.setAdmin(tokenUserInfoDto.getAdmin());

        return userInfoVO;
    }
}