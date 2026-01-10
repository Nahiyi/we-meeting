package cn.clazs.easymeeting.controller;

import cn.clazs.easymeeting.entity.dto.LoginDto;
import cn.clazs.easymeeting.entity.dto.RegisterDto;
import cn.clazs.easymeeting.entity.vo.CheckCodeVO;
import cn.clazs.easymeeting.entity.vo.ResponseVO;
import cn.clazs.easymeeting.entity.vo.UserInfoVO;
import cn.clazs.easymeeting.exception.BusinessException;
import cn.clazs.easymeeting.redis.RedisComponent;
import cn.clazs.easymeeting.service.UserInfoService;
import com.wf.captcha.ArithmeticCaptcha;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

@RestController
@RequestMapping("/account")
@Validated
@Slf4j
public class AccountController extends ABaseController {

    @Resource
    private UserInfoService userInfoService;

    @Resource
    private RedisComponent redisComponent;

    /**
     * 生成一条验证码，并保存答案到redis的接口
     */
    @RequestMapping("/checkCode")
    public ResponseVO checkCode() {
        ArithmeticCaptcha captcha = new ArithmeticCaptcha(100, 42);
        String code = captcha.text();
        String checkCodeKey = redisComponent.saveCheckCode(code);

        // 验证码的问题的图片的base64
        String checkCodeBase64 = captcha.toBase64();
        CheckCodeVO checkCodeVO = new CheckCodeVO();
        checkCodeVO.setCheckCode(checkCodeBase64);
        checkCodeVO.setCheckCodeKey(checkCodeKey);

        return getSuccessResponseVO(checkCodeVO);
    }

    @RequestMapping("/register")
    public ResponseVO register(@Valid @RequestBody RegisterDto registerDto) throws BusinessException {
        try {
            // 与登录同理，均需校验验证码
            if (!registerDto.getCheckCode().equalsIgnoreCase(redisComponent.getCheckCode(registerDto.getCheckCodeKey()))) {
                throw new BusinessException("图片验证码不正确");
            }
            this.userInfoService.register(registerDto.getEmail(), registerDto.getNickName(), registerDto.getPassword());
            return getSuccessResponseVO(null);
        } finally {
            redisComponent.deleteCheckCode(registerDto.getCheckCodeKey());
        }
    }

    @RequestMapping("/login")
    public ResponseVO login(@Valid @RequestBody LoginDto loginDto) throws BusinessException {
        try {
            // 根据验证码id找到对应验证码并校验
            if (!loginDto.getCheckCode().equalsIgnoreCase(redisComponent.getCheckCode(loginDto.getCheckCodeKey()))) {
                throw new BusinessException("图片验证码不正确");
            }
            UserInfoVO userInfoVO = this.userInfoService.login(loginDto.getEmail(), loginDto.getPassword());
            return getSuccessResponseVO(userInfoVO);
        } finally {
            // 无论成功失败，删除redis中的验证码
            redisComponent.deleteCheckCode(loginDto.getCheckCodeKey());
        }
    }
}