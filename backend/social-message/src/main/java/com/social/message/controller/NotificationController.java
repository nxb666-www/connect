package com.social.message.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.social.common.core.result.Result;
import com.social.common.security.context.LoginUserContext;
import com.social.message.dto.NotificationSendDTO;
import com.social.message.service.NotificationService;
import com.social.message.vo.NotificationVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "通知管理", description = "系统通知相关接口")
@RestController
@RequestMapping("/api/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "获取通知列表")
    @GetMapping("/list")
    public Result<Page<NotificationVO>> getNotifications(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        Long userId = LoginUserContext.getUserId();
        return Result.success(notificationService.getNotifications(userId, pageNum, pageSize));
    }

    @Operation(summary = "获取未读通知数")
    @GetMapping("/unread/count")
    public Result<Long> getUnreadCount() {
        Long userId = LoginUserContext.getUserId();
        return Result.success(notificationService.getUnreadCount(userId));
    }

    @Operation(summary = "标记单条通知已读")
    @PostMapping("/read/{id}")
    public Result<Void> markAsRead(@PathVariable Long id) {
        Long userId = LoginUserContext.getUserId();
        notificationService.markAsRead(id, userId);
        return Result.success();
    }

    @Operation(summary = "全部标记已读")
    @PostMapping("/read-all")
    public Result<Void> markAllAsRead() {
        Long userId = LoginUserContext.getUserId();
        notificationService.markAllAsRead(userId);
        return Result.success();
    }

    @Operation(summary = "内部接口-发送通知")
    @PostMapping("/internal/send")
    public Result<Void> sendNotification(@RequestBody NotificationSendDTO dto) {
        notificationService.sendNotification(dto);
        return Result.success();
    }
}
