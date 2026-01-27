package cn.clazs.easymeeting.controller;

import cn.clazs.easymeeting.context.UserContext;
import cn.clazs.easymeeting.entity.dto.UserTokenInfoDTO;
import cn.clazs.easymeeting.entity.po.PrivateChatMessage;
import cn.clazs.easymeeting.entity.po.PrivateChatUnread;
import cn.clazs.easymeeting.entity.vo.PageResult;
import cn.clazs.easymeeting.entity.vo.ResponseVO;
import cn.clazs.easymeeting.service.PrivateChatService;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 私聊消息控制器
 * 处理好友之间的一对一私聊消息
 */
@RestController
@RequestMapping("/private-chat")
@Validated
@Slf4j
@RequiredArgsConstructor
public class PrivateChatController {

    private final PrivateChatService privateChatService;

    /**
     * 发送私聊消息
     *
     * @param contactId   接收者ID
     * @param messageType 消息类型（5-文本，6-媒体）
     * @param message     消息内容
     * @param fileName    文件名（媒体消息时使用）
     * @param fileSize    文件大小（媒体消息时使用）
     * @param fileType    文件类型（媒体消息时使用）
     * @return 发送的消息
     */
    @PostMapping("/send")
    public ResponseVO<PrivateChatMessage> sendMessage(
            @NotEmpty String contactId,
            @NotNull Integer messageType,
            String message,
            String fileName,
            Long fileSize,
            Integer fileType) {

        UserTokenInfoDTO userTokenInfoDTO = UserContext.getCurrentUser();


        PrivateChatMessage chatMessage = new PrivateChatMessage();
        chatMessage.setSendUserId(userTokenInfoDTO.getUserId());
        chatMessage.setSendUserNickName(userTokenInfoDTO.getNickName());
        chatMessage.setReceiveUserId(contactId);
        chatMessage.setMessageType(messageType);
        chatMessage.setMessageContent(message);
        chatMessage.setFileName(fileName);
        chatMessage.setFileSize(fileSize);
        chatMessage.setFileType(fileType);

        PrivateChatMessage result = privateChatService.sendMessage(chatMessage);
        return ResponseVO.success(result);
    }


    /**
     * 获取聊天历史
     *
     * @param contactId 联系人ID
     * @param pageNo    页码
     * @param pageSize  每页数量
     * @return 消息分页结果
     */
    @GetMapping("/history")
    public ResponseVO<PageResult<PrivateChatMessage>> getChatHistory(
            @NotEmpty String contactId,
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "20") Integer pageSize) {

        UserTokenInfoDTO userTokenInfoDTO = UserContext.getCurrentUser();
        String userId = userTokenInfoDTO.getUserId();

        PageResult<PrivateChatMessage> result = privateChatService.getChatHistory(userId, contactId, pageNo, pageSize);
        return ResponseVO.success(result);
    }

    /**
     * 加载更多历史消息
     *
     * @param contactId    联系人ID
     * @param maxMessageId 最大消息ID（查询比这个ID更早的消息）
     * @param pageSize     每页数量
     * @return 消息分页结果
     */
    @GetMapping("/history/before")
    public ResponseVO<PageResult<PrivateChatMessage>> getMessagesBeforeId(
            @NotEmpty String contactId,
            @NotNull Long maxMessageId,
            @RequestParam(defaultValue = "20") Integer pageSize) {

        UserTokenInfoDTO userTokenInfoDTO = UserContext.getCurrentUser();
        String userId = userTokenInfoDTO.getUserId();

        PageResult<PrivateChatMessage> result = privateChatService.getMessagesBeforeId(userId, contactId, maxMessageId, pageSize);
        return ResponseVO.success(result);
    }

    /**
     * 获取单个会话的未读消息数
     *
     * @param contactId 联系人ID
     * @return 未读消息数
     */
    @GetMapping("/unread")
    public ResponseVO<Integer> getUnreadCount(
            @NotEmpty String contactId) {

        UserTokenInfoDTO userTokenInfoDTO = UserContext.getCurrentUser();
        String userId = userTokenInfoDTO.getUserId();

        Integer count = privateChatService.getUnreadCount(userId, contactId);
        return ResponseVO.success(count);
    }

    /**
     * 获取所有会话的未读消息数
     *
     * @return Map<联系人ID, 未读数>
     */
    @GetMapping("/unread/all")
    public ResponseVO<Map<String, Integer>> getAllUnreadCounts() {
        UserTokenInfoDTO userTokenInfoDTO = UserContext.getCurrentUser();
        String userId = userTokenInfoDTO.getUserId();

        Map<String, Integer> result = privateChatService.getAllUnreadCounts(userId);
        return ResponseVO.success(result);
    }

    /**
     * 获取未读消息列表（包含最后消息预览）
     *
     * @return 未读消息记录列表
     */
    @GetMapping("/unread/list")
    public ResponseVO<List<PrivateChatUnread>> getUnreadList() {
        UserTokenInfoDTO userTokenInfoDTO = UserContext.getCurrentUser();
        String userId = userTokenInfoDTO.getUserId();

        List<PrivateChatUnread> result = privateChatService.getUnreadList(userId);
        return ResponseVO.success(result);
    }

    /**
     * 标记消息已读
     *
     * @param contactId 联系人ID
     * @return 成功响应
     */
    @PostMapping("/read")
    public ResponseVO<Void> markAsRead(
            @NotEmpty String contactId) {

        UserTokenInfoDTO userTokenInfoDTO = UserContext.getCurrentUser();
        String userId = userTokenInfoDTO.getUserId();

        privateChatService.markAsRead(userId, contactId);
        return ResponseVO.success(null);
    }
}
