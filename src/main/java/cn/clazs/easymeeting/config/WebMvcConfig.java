package cn.clazs.easymeeting.config;

import cn.clazs.easymeeting.interceptor.TokenInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final TokenInterceptor tokenInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Token 拦截器 - 校验登录状态
        registry.addInterceptor(tokenInterceptor)
                .addPathPatterns("/**")  // 拦截所有请求
                .excludePathPatterns(
                        "/account/checkCode",   // 获取验证码
                        "/account/login",       // 登录
                        "/account/register",    // 注册
                        "/error"                // 错误页面
                )
                .order(1);
    }
}
