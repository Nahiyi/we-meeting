package cn.clazs.easymeeting.service;

import cn.clazs.easymeeting.entity.po.UserContactApply;
import cn.clazs.easymeeting.entity.vo.UserContactApplyVO;

import java.util.List;

public interface UserContactApplyService {

    /**
     * 创建申请（如果已存在则更新状态为待处理）
     */
    UserContactApply createApply(UserContactApply userContactApply);

    /**
     * 创建或更新申请（利用唯一索引）
     */
    UserContactApply createOrUpdateApply(UserContactApply userContactApply);

    /**
     * 更新申请
     */
    UserContactApply updateApply(UserContactApply userContactApply);

    /**
     * 删除申请
     */
    void deleteById(Integer applyId);

    /**
     * 根据ID查询
     */
    UserContactApply getById(Integer applyId);

    /**
     * 查询所有申请
     */
    List<UserContactApply> getAll();

    /**
     * 根据申请用户ID查询
     */
    List<UserContactApply> getByApplyUserId(String applyUserId);

    /**
     * 根据接收用户ID查询
     */
    List<UserContactApply> getByReceiveUserId(String receiveUserId);

    /**
     * 根据状态查询
     */
    List<UserContactApply> getByStatus(Integer status);

    /**
     * 根据申请用户ID和接收用户ID查询（唯一索引）
     */
    UserContactApply getByApplyUserIdAndReceiveUserId(String applyUserId, String receiveUserId);

    /**
     * 更新申请状态
     */
    void updateStatus(Integer applyId, Integer status);

    /**
     * 根据唯一索引更新状态
     */
    void updateStatusByApplyUserIdAndReceiveUserId(String applyUserId, String receiveUserId, Integer status);

    /**
     * 查询用户收到的待处理申请
     */
    List<UserContactApply> getPendingByReceiveUserId(String receiveUserId);

    /**
     * 查询用户发出的申请
     */
    List<UserContactApply> getSentByApplyUserId(String applyUserId);

    /**
     * 检查是否已存在待处理的申请
     */
    boolean existsPendingApply(String applyUserId, String receiveUserId);

    /**
     * 保存好友申请
     */
    Integer saveUserContactApply(UserContactApply userContactApply);

    /**
     * 处理好友申请
     */
    void dealWithApply(String applyUserId, String userId, String nickName, Integer status);

    /**
     * 查询用户收到的申请（包含申请人昵称）
     */
    List<UserContactApplyVO> getReceivedAppliesWithNickName(String receiveUserId);
}
