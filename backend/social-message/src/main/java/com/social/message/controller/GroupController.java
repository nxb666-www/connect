package com.social.message.controller;

import com.social.common.core.result.Result;
import com.social.common.security.context.LoginUserContext;
import com.social.message.entity.ChatGroup;
import com.social.message.service.GroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Tag(name = "群聊管理", description = "创建群、群成员管理等接口")
@RestController
@RequestMapping("/api/group")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    @Operation(summary = "创建群聊")
    @PostMapping
    public Result<Long> createGroup(@RequestBody Map<String, Object> body) {
        Long userId = LoginUserContext.getUserId();
        String name = (String) body.get("name");
        List<Long> memberIds = null;
        Object raw = body.get("memberIds");
        if (raw instanceof List<?>) {
            memberIds = ((List<?>) raw).stream()
                    .map(item -> Long.parseLong(item.toString()))
                    .collect(Collectors.toList());
        }
        Long groupId = groupService.createGroup(userId, name, memberIds);
        return Result.success(groupId);
    }

    @Operation(summary = "获取群信息")
    @GetMapping("/{groupId}")
    public Result<ChatGroup> getGroupInfo(@PathVariable Long groupId) {
        return Result.success(groupService.getGroupInfo(groupId));
    }

    @Operation(summary = "获取我的群聊列表")
    @GetMapping("/my")
    public Result<List<Map<String, Object>>> getMyGroups() {
        Long userId = LoginUserContext.getUserId();
        return Result.success(groupService.getMyGroups(userId));
    }

    @Operation(summary = "添加群成员")
    @PostMapping("/{groupId}/members")
    public Result<Void> addMember(@PathVariable Long groupId, @RequestBody Map<String, Long> body) {
        Long userId = LoginUserContext.getUserId();
        Long targetUserId = body.get("userId");
        groupService.addMember(groupId, userId, targetUserId);
        return Result.success();
    }

    @Operation(summary = "移除群成员")
    @DeleteMapping("/{groupId}/members/{userId}")
    public Result<Void> removeMember(@PathVariable Long groupId, @PathVariable Long userId) {
        Long operatorId = LoginUserContext.getUserId();
        groupService.removeMember(groupId, operatorId, userId);
        return Result.success();
    }

    @Operation(summary = "获取群成员列表")
    @GetMapping("/{groupId}/members")
    public Result<List<Map<String, Object>>> getGroupMembers(@PathVariable Long groupId) {
        return Result.success(groupService.getGroupMembers(groupId));
    }

    @Operation(summary = "解散群聊")
    @DeleteMapping("/{groupId}")
    public Result<Void> deleteGroup(@PathVariable Long groupId) {
        Long userId = LoginUserContext.getUserId();
        groupService.deleteGroup(groupId, userId);
        return Result.success();
    }

    @Operation(summary = "退出群聊")
    @PostMapping("/{groupId}/quit")
    public Result<Void> quitGroup(@PathVariable Long groupId) {
        Long userId = LoginUserContext.getUserId();
        groupService.quitGroup(groupId, userId);
        return Result.success();
    }
}
