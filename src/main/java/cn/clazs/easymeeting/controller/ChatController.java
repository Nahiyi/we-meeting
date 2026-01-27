package cn.clazs.easymeeting.controller;

import cn.clazs.easymeeting.constant.Constants;
import cn.clazs.easymeeting.context.UserContext;
import cn.clazs.easymeeting.entity.dto.UserTokenInfoDTO;
import cn.clazs.easymeeting.entity.enums.ReceiveType;
import cn.clazs.easymeeting.entity.po.ChatMessage;
import cn.clazs.easymeeting.entity.vo.PageResult;
import cn.clazs.easymeeting.entity.vo.ResponseVO;
import cn.clazs.easymeeting.exception.BusinessException;
import cn.clazs.easymeeting.service.impl.ChatMessageServiceImpl;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/chat")
@Validated
@Slf4j
@RequiredArgsConstructor
public class ChatController {
    private final ChatMessageServiceImpl chatMessageService;

    /**
     * 加载会议聊天消息
     *
     * 支持两种模式：
     * 1. 首次加载：不传 maxMessageId，返回最新的消息
     * 2. 加载更多：传入 maxMessageId，返回比该ID更早的消息
     *
     * @param maxMessageId 最大消息ID（可选，用于加载更多历史消息）
     * @param pageNo       页码（首次加载时使用）
     * @param pageSize     每页数量
     * @return 消息分页结果
     */
    @GetMapping("/loadMessage")
    public ResponseVO<PageResult<ChatMessage>> loadMessage(
            Long maxMessageId,
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "20") Integer pageSize) {

        UserTokenInfoDTO userTokenInfoDTO = UserContext.getCurrentUser();
        String meetingId = userTokenInfoDTO.getCurrentMeetingId();

        log.info("loadMessage 请求: userId={}, meetingId={}, pageNo={}, pageSize={}, maxMessageId={}",
                userTokenInfoDTO.getUserId(), meetingId, pageNo, pageSize, maxMessageId);

        // 校验用户是否在会议中
        if (meetingId == null || meetingId.isEmpty()) {
            throw new BusinessException("你当前不在会议中");
        }

        // 根据是否有 maxMessageId 决定查询方式
        PageResult<ChatMessage> result;
        if (maxMessageId != null) {
            // 加载更多：查询比 maxMessageId 更早的消息
            result = chatMessageService.getMessagesBeforeId(meetingId, maxMessageId, pageSize);
        } else {
            // 首次加载：分页查询最新消息
            result = chatMessageService.getMessagesByMeetingId(meetingId, pageNo, pageSize);
        }

        log.info("loadMessage 返回: meetingId={}, total={}, listSize={}",
                meetingId, result.getTotal(), result.getList() != null ? result.getList().size() : 0);

        return ResponseVO.success(result);
    }

    @GetMapping("/sendMessage")
    public ResponseVO<ChatMessage> sendMessage(
            String message,
            @NotNull Integer messageType,
            @NotEmpty String receiveId,
            String fileName,
            Long fileSize,
            Integer filType) {
        UserTokenInfoDTO userTokenInfoDTO = UserContext.getCurrentUser();
        ChatMessage chatMessage = new ChatMessage();

        chatMessage.setMessageType(messageType);
        chatMessage.setMessageContent(message);
        chatMessage.setFileName(fileName);
        chatMessage.setFileSize(fileSize);
        chatMessage.setFileType(filType);
        chatMessage.setSendUserId(userTokenInfoDTO.getUserId());
        chatMessage.setSendUserNickName(userTokenInfoDTO.getNickName());
        chatMessage.setMeetingId(userTokenInfoDTO.getCurrentMeetingId());
        if (Constants.ZERO_STR.equals(receiveId)) {
            chatMessage.setReceiveType(ReceiveType.ALL.getType());
        } else {
            chatMessage.setReceiveType(ReceiveType.USER.getType());
        }
        chatMessage.setReceiveUserId(receiveId);
        chatMessageService.saveMessage(chatMessage);
        return ResponseVO.success(chatMessage);
    }
}
