package cn.clazs.easymeeting.controller;

import cn.clazs.easymeeting.context.UserContext;
import cn.clazs.easymeeting.entity.dto.UserTokenInfoDTO;
import cn.clazs.easymeeting.entity.enums.UserContactStatus;
import cn.clazs.easymeeting.entity.po.UserContact;
import cn.clazs.easymeeting.entity.po.UserContactApply;
import cn.clazs.easymeeting.entity.vo.ResponseVO;
import cn.clazs.easymeeting.entity.vo.UserContactApplyVO;
import cn.clazs.easymeeting.entity.vo.UserContactVO;
import cn.clazs.easymeeting.entity.vo.UserInfoVoForSearch;
import cn.clazs.easymeeting.service.UserContactApplyService;
import cn.clazs.easymeeting.service.UserContactService;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/userContact")
@Validated
@Slf4j
@RequiredArgsConstructor
public class UserContactController {

    private final UserContactService userContactService;
    private final UserContactApplyService userContactApplyService;

    @GetMapping("/searchContact")
    public ResponseVO<UserInfoVoForSearch> searchContact(@NotEmpty String searchUserId) {
        UserTokenInfoDTO userTokenInfoDTO = UserContext.getCurrentUser();
        UserInfoVoForSearch userInfoVoForSearch = userContactService.searchContact(userTokenInfoDTO.getUserId(), searchUserId);
        return ResponseVO.success(userInfoVoForSearch);
    }

    @RequestMapping("/contactApply")
    public ResponseVO<?> contactApply(@NotEmpty String receiverUserId) {
        UserTokenInfoDTO userTokenInfoDTO = UserContext.getCurrentUser();
        UserContactApply userContactApply = new UserContactApply();
        userContactApply.setApplyUserId(userTokenInfoDTO.getUserId());
        userContactApply.setReceiveUserId(receiverUserId);
        Integer status = userContactApplyService.saveUserContactApply(userContactApply);
        return ResponseVO.success(status);

    }

    @RequestMapping("/dealWithApply")
    public ResponseVO<?> dealWithApply(@NotEmpty String applyUserId, @NotNull Integer status) {
        UserTokenInfoDTO userTokenInfoDTO = UserContext.getCurrentUser();
        userContactApplyService.dealWithApply(applyUserId, userTokenInfoDTO.getUserId(), userTokenInfoDTO.getNickName(), status);
        return ResponseVO.success(null);
    }

    /**
     * 获取联系人列表（包含昵称）
     */
    @GetMapping("/loadContactUser")
    public ResponseVO<?> loadContactUser() {
        UserTokenInfoDTO userTokenInfoDTO = UserContext.getCurrentUser();
        List<UserContactVO> contacts = userContactService.getNormalContactsWithNickName(userTokenInfoDTO.getUserId());
        return ResponseVO.success(contacts);
    }

    /**
     * 获取收到的好友申请列表（包含申请人昵称）
     */
    @GetMapping("/loadContactApply")
    public ResponseVO<?> loadContactApply() {
        UserTokenInfoDTO userTokenInfoDTO = UserContext.getCurrentUser();
        List<UserContactApplyVO> applies = userContactApplyService.getReceivedAppliesWithNickName(userTokenInfoDTO.getUserId());
        return ResponseVO.success(applies);
    }

    @RequestMapping("/operateContact")
    public ResponseVO<?> operateContact(@NotEmpty String contactId, @NotNull Integer status) {
        UserTokenInfoDTO userTokenInfoDTO = UserContext.getCurrentUser();
        String userId = userTokenInfoDTO.getUserId();
        // 1. 校验 status 是否为有效枚举值
        UserContactStatus statusEnum = UserContactStatus.getUserContactStatus(status);
        if (statusEnum == null) {
            return ResponseVO.error("无效的状态值");
        }
        // 2. 只允许删除(1)或拉黑(2)操作，不允许通过此接口恢复正常(0)
        if (UserContactStatus.NORMAL.equals(statusEnum)) {
            return ResponseVO.error("不支持的操作");
        }
        // 3. 校验联系人是否存在
        UserContact contact = userContactService.getByUserIdAndContactId(userId, contactId);
        if (contact == null) {
            return ResponseVO.error("联系人不存在");
        }
        // 4. 更新状态
        userContactService.updateStatus(userId, contactId, statusEnum.getStatus());
        return ResponseVO.success(null);
    }

}
