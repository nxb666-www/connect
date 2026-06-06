package com.social.message.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.social.common.core.result.Result;
import com.social.common.security.context.LoginUserContext;
import com.social.message.dto.MessageSendDTO;
import com.social.message.service.MessageService;
import com.social.message.service.OnlineStatusService;
import com.social.message.vo.MessageVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Tag(name = "消息管理", description = "私信发送、接收、会话管理等接口")
@RestController
@RequestMapping("/api/message")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;
    private final OnlineStatusService onlineStatusService;

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    @Value("${file.base-url:http://localhost:8084}")
    private String baseUrl;

    @Operation(summary = "发送消息")
    @PostMapping("/send")
    public Result<Long> sendMessage(@Valid @RequestBody MessageSendDTO dto) {
        Long senderId = LoginUserContext.getUserId();
        Long messageId = messageService.sendMessage(senderId, dto);
        return Result.success(messageId);
    }

    @Operation(summary = "上传文件/图片")
    @PostMapping("/upload")
    public Result<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("文件不能为空");
        }

        String originalName = file.getOriginalFilename();
        String ext = "";
        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf("."));
        }

        String fileName = UUID.randomUUID().toString().replace("-", "") + ext;
        String datePath = java.time.LocalDate.now().toString().replace("-", "/");
        File dir = new File(uploadDir + "/" + datePath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        try {
            file.transferTo(new File(dir, fileName));
        } catch (IOException e) {
            return Result.error("文件上传失败: " + e.getMessage());
        }

        String url = baseUrl + "/uploads/" + datePath + "/" + fileName;
        String type = "image";
        if (!ext.matches("\\.(?i)(jpg|jpeg|png|gif|bmp|webp|svg)")) {
            type = "file";
        }

        Map<String, String> result = new HashMap<>();
        result.put("url", url);
        result.put("name", originalName);
        result.put("type", type);
        return Result.success(result);
    }

    @Operation(summary = "获取与某用户的聊天记录")
    @GetMapping("/conversation/{targetUserId}")
    public Result<Page<MessageVO>> getConversation(
            @PathVariable Long targetUserId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        Long userId = LoginUserContext.getUserId();
        Page<MessageVO> page = messageService.getConversation(userId, targetUserId, pageNum, pageSize);
        return Result.success(page);
    }

    @Operation(summary = "获取群聊消息记录")
    @GetMapping("/conversation/group/{groupId}")
    public Result<Page<MessageVO>> getGroupConversation(
            @PathVariable Long groupId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        Long userId = LoginUserContext.getUserId();
        Page<MessageVO> page = messageService.getGroupConversation(userId, groupId, pageNum, pageSize);
        return Result.success(page);
    }

    @Operation(summary = "标记群聊消息已读")
    @PostMapping("/read/group/{groupId}")
    public Result<Void> markGroupAsRead(@PathVariable Long groupId) {
        Long userId = LoginUserContext.getUserId();
        messageService.markGroupAsRead(userId, groupId);
        return Result.success();
    }

    @Operation(summary = "获取最近会话列表")
    @GetMapping("/conversations")
    public Result<List<MessageVO>> getRecentConversations() {
        Long userId = LoginUserContext.getUserId();
        return Result.success(messageService.getRecentConversations(userId));
    }

    @Operation(summary = "获取未读消息数")
    @GetMapping("/unread/count")
    public Result<Long> getUnreadCount() {
        Long userId = LoginUserContext.getUserId();
        return Result.success(messageService.getUnreadCount(userId));
    }

    @Operation(summary = "标记消息已读")
    @PostMapping("/read/{senderId}")
    public Result<Void> markAsRead(@PathVariable Long senderId) {
        Long userId = LoginUserContext.getUserId();
        messageService.markAsRead(userId, senderId);
        return Result.success();
    }

    @Operation(summary = "更新用户在线状态")
    @PostMapping("/heartbeat")
    public Result<Void> heartbeat() {
        Long userId = LoginUserContext.getUserId();
        onlineStatusService.updateLastActive(userId);
        return Result.success();
    }

    @Operation(summary = "批量检查用户在线状态")
    @PostMapping("/online")
    public Result<Map<Long, Boolean>> checkOnline(@RequestBody List<Long> userIds) {
        return Result.success(onlineStatusService.batchCheckOnline(userIds));
    }
}
