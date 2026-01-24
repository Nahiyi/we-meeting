package cn.clazs.easymeeting.context;

import cn.clazs.easymeeting.entity.dto.UserTokenInfoDTO;
import cn.clazs.easymeeting.exception.BusinessException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 用户上下文工具类
 * 基于 RequestContextHolder 实现，在单次请求链路中获取当前登录用户信息
 */
public class UserContext {

    private static final String CURRENT_USER = "currentUser";

    /**
     * 获取当前请求的用户信息
     *
     * @return 当前登录用户的 Token 信息
     * @throws BusinessException 如果用户未登录或请求上下文不存在
     */
    public static UserTokenInfoDTO getCurrentUser() {
        HttpServletRequest request = getRequest();
        UserTokenInfoDTO userInfo = (UserTokenInfoDTO) request.getAttribute(CURRENT_USER);

        if (userInfo == null) {
            throw new BusinessException("用户未登录或登录已失效");
        }

        return userInfo;
    }

    /**
     * 获取当前用户 ID
     */
    public static String getCurrentUserId() {
        return getCurrentUser().getUserId();
    }

    /**
     * 获取当前用户昵称
     */
    public static String getCurrentUserNickName() {
        return getCurrentUser().getNickName();
    }

    /**
     * 获取当前用户的当前会议 ID
     */
    public static String getCurrentMeetingId() {
        return getCurrentUser().getCurrentMeetingId();
    }

    /**
     * 设置当前用户信息（通常在拦截器中调用）
     */
    public static void setCurrentUser(UserTokenInfoDTO userInfo) {
        getRequest().setAttribute(CURRENT_USER, userInfo);
    }

    /**
     * 获取当前请求的 HttpServletRequest
     */
    private static HttpServletRequest getRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes == null) {
            throw new BusinessException("当前线程不存在请求上下文，可能是在非 Web 环境中调用");
        }

        return attributes.getRequest();
    }

    /**
     * 清除当前用户信息（可选，通常无需手动调用）
     */
    public static void clearCurrentUser() {
        getRequest().removeAttribute(CURRENT_USER);
    }
}
