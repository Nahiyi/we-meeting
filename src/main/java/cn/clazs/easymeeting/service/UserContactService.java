package cn.clazs.easymeeting.service;

import cn.clazs.easymeeting.entity.po.UserContact;
import cn.clazs.easymeeting.entity.vo.UserContactVO;
import cn.clazs.easymeeting.entity.vo.UserInfoVoForSearch;

import java.util.List;

public interface UserContactService {

    /**
     * 添加联系人
     */
    UserContact addContact(UserContact userContact);

    /**
     * 更新联系人
     */
    UserContact updateContact(UserContact userContact);

    /**
     * 删除联系人（根据用户ID）
     */
    void deleteByUserId(String userId);

    /**
     * 删除联系人（根据联系人ID）
     */
    void deleteByContactId(String contactId);

    /**
     * 根据用户ID查询
     */
    UserContact getByUserId(String userId);

    /**
     * 根据联系人ID查询
     */
    UserContact getByContactId(String contactId);

    /**
     * 根据用户ID和联系人ID查询
     */
    UserContact getByUserIdAndContactId(String userId, String contactId);

    /**
     * 查询用户的所有联系人
     */
    List<UserContact> getContactsByUserId(String userId);

    /**
     * 查询用户的正常好友列表（status=0）
     */
    List<UserContact> getNormalContactsByUserId(String userId);

    /**
     * 根据状态查询
     */
    List<UserContact> getByStatus(Integer status);

    /**
     * 查询所有联系人
     */
    List<UserContact> getAll();

    /**
     * 更新联系人状态
     */
    void updateStatus(String userId, String contactId, Integer status);

    /**
     * 检查是否已是联系人
     */
    boolean isContact(String userId, String contactId);

    /**
     * 搜索联系人
     */
    UserInfoVoForSearch searchContact(String userId, String contactId);

    /**
     * 查询用户的正常好友列表（包含昵称）
     */
    List<UserContactVO> getNormalContactsWithNickName(String userId);
}
