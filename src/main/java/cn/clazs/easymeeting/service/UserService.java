package cn.clazs.easymeeting.service;

import cn.clazs.easymeeting.entity.dto.LoginDTO;
import cn.clazs.easymeeting.entity.dto.RegisterDTO;
import cn.clazs.easymeeting.entity.vo.UserInfoVO;
import cn.clazs.easymeeting.exception.BusinessException;

public interface UserService {

    void register(RegisterDTO dto) throws BusinessException;

    UserInfoVO login(LoginDTO dto) throws BusinessException;

    void logout(String token) throws BusinessException;

}