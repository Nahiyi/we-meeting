package cn.clazs.easymeeting.interceptor;

import cn.clazs.easymeeting.entity.dto.UserTokenInfoDTO;
import cn.clazs.easymeeting.entity.vo.ResponseVO;
import cn.clazs.easymeeting.redis.RedisComponent;
import cn.clazs.easymeeting.util.StringUtil;
import com.alibaba.fastjson2.JSON;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@Slf4j
@RequiredArgsConstructor
public class TokenInterceptor implements HandlerInterceptor {

    private final RedisComponent redisComponent;

    // 存储当前请求的用户信息，供 Controller 使用
    public static final String CURRENT_USER = "currentUser";

    // Token 续期阈值：剩余 1 天时自动续期
    private static final long RENEW_THRESHOLD_SECONDS = 24 * 60 * 60;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 从 Header 获取 token
        String token = request.getHeader("token");

        // token 为空
        if (StringUtil.isEmpty(token)) {
            writeUnauthorized(response, "请先登录");
            return false;
        }

        // 验证 token 是否存在于 Redis
        UserTokenInfoDTO userInfo = redisComponent.getUserTokenInfo(token);
        if (userInfo == null) {
            // token 不存在，可能是被踢下线或已过期
            writeUnauthorized(response, "登录已失效，请重新登录");
            return false;
        }

        // Token 自动续期：剩余时间小于 1 天时自动延长
        boolean renewed = redisComponent.renewTokenIfNeeded(token, RENEW_THRESHOLD_SECONDS);
        if (renewed) {
            log.debug("Token 已自动续期，用户：{}", userInfo.getUserId());
        }

        // 将用户信息存入 request，供后续使用
        request.setAttribute(CURRENT_USER, userInfo);

        return true;
    }

    // 将未授权信息写入响应体
    private void writeUnauthorized(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json;charset=UTF-8");
        ResponseVO<Object> result = ResponseVO.fail(HttpStatus.UNAUTHORIZED.value(), message);
        response.getWriter().write(JSON.toJSONString(result));
    }
}
