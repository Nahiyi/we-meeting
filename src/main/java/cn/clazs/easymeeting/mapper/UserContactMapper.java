package cn.clazs.easymeeting.mapper;

import cn.clazs.easymeeting.entity.po.UserContact;
import cn.clazs.easymeeting.entity.vo.UserContactVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserContactMapper {

    int insert(UserContact userContact);

    /**
     * 插入或更新（利用唯一索引），已存在则更新状态
     */
    int insertOrUpdate(UserContact userContact);

    int updateByUserId(UserContact userContact);

    int deleteByUserId(@Param("userId") String userId);

    int deleteByContactId(@Param("contactId") String contactId);

    UserContact selectByUserId(@Param("userId") String userId);

    UserContact selectByContactId(@Param("contactId") String contactId);

    List<UserContact> selectByStatus(@Param("status") Integer status);

    List<UserContact> selectAll();

    /**
     * 根据用户ID和联系人ID查询
     */
    UserContact selectByUserIdAndContactId(@Param("userId") String userId, @Param("contactId") String contactId);

    /**
     * 查询用户的所有联系人
     */
    List<UserContact> selectContactsByUserId(@Param("userId") String userId);

    /**
     * 查询用户的正常好友列表（status=0）
     */
    List<UserContact> selectNormalContactsByUserId(@Param("userId") String userId);

    /**
     * 更新联系人状态
     */
    int updateStatus(@Param("userId") String userId, @Param("contactId") String contactId, @Param("status") Integer status);

    /**
     * 查询用户的正常好友列表（关联用户昵称）
     */
    List<UserContactVO> selectNormalContactsWithNickName(@Param("userId") String userId);
}
