package cn.clazs.easymeeting.mapper;

import cn.clazs.easymeeting.entity.po.UserInfo;
import cn.clazs.easymeeting.entity.query.UserQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserMapper {

    int insert(UserInfo userInfo);

    int updateById(UserInfo userInfo);

    int updatePassword(@Param("userId") String userId, @Param("password") String password);

    int updateLastLoginTime(@Param("userId") String userId, @Param("lastLoginTime") Long lastLoginTime);

    int updateLastOffTime(@Param("userId") String userId, @Param("lastOffTime") Long lastOffTime);

    UserInfo selectById(@Param("userId") String userId);

    UserInfo selectByEmail(@Param("email") String email);

    List<UserInfo> selectList(UserQuery query);

    int selectCount(UserQuery query);

    int deleteById(@Param("userId") String userId);
}
