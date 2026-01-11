package cn.clazs.easymeeting.controller;

import cn.clazs.easymeeting.entity.dto.LoginDTO;
import cn.clazs.easymeeting.entity.dto.RegisterDTO;
import cn.clazs.easymeeting.entity.vo.CheckCodeVO;
import cn.clazs.easymeeting.entity.vo.ResponseVO;
import cn.clazs.easymeeting.entity.vo.UserInfoVO;
import cn.clazs.easymeeting.exception.BusinessException;
import cn.clazs.easymeeting.redis.RedisComponent;
import cn.clazs.easymeeting.service.UserService;
import cn.clazs.easymeeting.util.StringUtil;
import com.wf.captcha.ArithmeticCaptcha;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/account")
@Validated
@Slf4j
@RequiredArgsConstructor
public class AccountController {

    private final UserService userService;

    private final RedisComponent redisComponent;

    /**
     * 生成一条验证码，并保存答案到redis的接口
     */
    @RequestMapping("/checkCode")
    public ResponseVO<CheckCodeVO> checkCode() {
        ArithmeticCaptcha captcha = new ArithmeticCaptcha(100, 42);
        String code = captcha.text();
        String checkCodeKey = redisComponent.saveCheckCode(code);

        // 验证码的问题的图片的base64
        String checkCodeBase64 = captcha.toBase64();
        CheckCodeVO checkCodeVO = new CheckCodeVO();
        checkCodeVO.setCheckCode(checkCodeBase64);
        checkCodeVO.setCheckCodeKey(checkCodeKey);

        return ResponseVO.success(checkCodeVO);
    }

    @RequestMapping("/register")
    public ResponseVO<Void> register(@RequestBody @Valid RegisterDTO dto) throws BusinessException {
        try {
            // 与登录同理，均需校验验证码
            if (!dto.getCheckCode().equalsIgnoreCase(redisComponent.getCheckCode(dto.getCheckCodeKey()))) {
                throw new BusinessException("图片验证码不正确");
            }
            this.userService.register(dto);
            return ResponseVO.success();
        } finally {
            redisComponent.clearCheckCode(dto.getCheckCodeKey());
        }
    }

    @RequestMapping("/login")
    public ResponseVO<UserInfoVO> login(@RequestBody @Valid LoginDTO dto) throws BusinessException {
        try {
            // 根据验证码id找到对应验证码并校验
            if (!dto.getCheckCode().equalsIgnoreCase(redisComponent.getCheckCode(dto.getCheckCodeKey()))) {
                throw new BusinessException("图片验证码不正确");
            }
            UserInfoVO userInfoVO = this.userService.login(dto);
            return ResponseVO.success(userInfoVO);
        } finally {
            // 无论成功失败，删除redis中的验证码
            redisComponent.clearCheckCode(dto.getCheckCodeKey());
        }
    }

    /**
     * 用户退出登录
     */
    @RequestMapping("/logout")
    public ResponseVO<Void> logout(@RequestHeader String token) throws BusinessException {
        // 优先使用Header中的token，如果没有则使用URL参数中的token
        if (StringUtil.isEmpty(token)) {
            throw new BusinessException("Token不能为空");
        }
        this.userService.logout(token);
        return ResponseVO.success();
    }
}
