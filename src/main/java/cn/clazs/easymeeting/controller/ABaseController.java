package cn.clazs.easymeeting.controller;

import cn.clazs.easymeeting.entity.dto.TokenUserInfoDto;
import cn.clazs.easymeeting.entity.enums.ResponseCodeEnum;
import cn.clazs.easymeeting.entity.vo.ResponseVO;
import cn.clazs.easymeeting.redis.RedisComponent;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

public class ABaseController {

    @Resource
    private RedisComponent redisComponent;

    protected static final String STATUS_SUCCESS = "success";

    protected static final String STATUS_ERROR = "error";

    protected <T> ResponseVO getSuccessResponseVO(T t) {
        ResponseVO<T> responseVO = new ResponseVO<>();
        responseVO.setStatus(STATUS_SUCCESS);
        responseVO.setCode(ResponseCodeEnum.CODE_200.getCode());
        responseVO.setInfo(ResponseCodeEnum.CODE_200.getMsg());
        responseVO.setData(t);
        return responseVO;
    }

    /**
     * 从当前HTTP请求中获取Token并查询用户信息
     *
     * @return TokenUserInfoDto 包含用户信息的DTO对象
     */
    protected TokenUserInfoDto getTokenUserInfo() {
        // 获取当前HTTP请求对象
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        // 从请求头中获取token
        String token = request.getHeader("token");
        // 通过Redis组件根据token查询用户信息
        TokenUserInfoDto tokenUserInfoDto = redisComponent.getTokenUserInfoDto(token);
        return tokenUserInfoDto;
    }

    protected void resetTokenUserInfo(TokenUserInfoDto tokenUserInfoDto) {
        redisComponent.saveTokenUserInfoDto(tokenUserInfoDto);
    }
}
