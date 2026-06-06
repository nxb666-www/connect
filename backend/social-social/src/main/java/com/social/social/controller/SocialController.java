package com.social.social.controller;

import com.social.common.core.result.Result;
import com.social.common.security.context.LoginUserContext;
import com.social.social.entity.Friend;
import com.social.social.service.BlacklistService;
import com.social.social.service.FollowService;
import com.social.social.service.FriendService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "社交关系管理", description = "关注、好友、黑名单等接口")
@RestController
@RequestMapping("/api/social")
@RequiredArgsConstructor
public class SocialController {

    private final FollowService followService;
    private final FriendService friendService;
    private final BlacklistService blacklistService;

    @Operation(summary = "关注用户")
    @PostMapping("/follow/{followId}")
    public Result<Void> follow(@PathVariable Long followId) {
        Long userId = LoginUserContext.getUserId();
        followService.follow(userId, followId);
        return Result.success();
    }

    @Operation(summary = "取消关注")
    @DeleteMapping("/follow/{followId}")
    public Result<Void> unfollow(@PathVariable Long followId) {
        Long userId = LoginUserContext.getUserId();
        followService.unfollow(userId, followId);
        return Result.success();
    }

    @Operation(summary = "获取关注列表")
    @GetMapping("/following/{userId}")
    public Result<List<Long>> getFollowingList(@PathVariable Long userId) {
        return Result.success(followService.getFollowingList(userId));
    }

    @Operation(summary = "获取粉丝列表")
    @GetMapping("/followers/{userId}")
    public Result<List<Long>> getFollowerList(@PathVariable Long userId) {
        return Result.success(followService.getFollowerList(userId));
    }

    @Operation(summary = "获取关注数")
    @GetMapping("/following/{userId}/count")
    public Result<Long> getFollowingCount(@PathVariable Long userId) {
        return Result.success(followService.getFollowingCount(userId));
    }

    @Operation(summary = "获取粉丝数")
    @GetMapping("/followers/{userId}/count")
    public Result<Long> getFollowerCount(@PathVariable Long userId) {
        return Result.success(followService.getFollowerCount(userId));
    }

    @Operation(summary = "是否关注")
    @GetMapping("/follow/check/{targetUserId}")
    public Result<Boolean> isFollowing(@PathVariable Long targetUserId) {
        Long userId = LoginUserContext.getUserId();
        return Result.success(followService.isFollowing(userId, targetUserId));
    }

    @Operation(summary = "发送好友申请")
    @PostMapping("/friend/{friendId}")
    public Result<Void> sendFriendRequest(@PathVariable Long friendId) {
        Long userId = LoginUserContext.getUserId();
        friendService.sendFriendRequest(userId, friendId);
        return Result.success();
    }

    @Operation(summary = "接受好友申请")
    @PostMapping("/friend/{friendId}/accept")
    public Result<Void> acceptFriendRequest(@PathVariable Long friendId) {
        Long userId = LoginUserContext.getUserId();
        friendService.acceptFriendRequest(userId, friendId);
        return Result.success();
    }

    @Operation(summary = "拒绝好友申请")
    @PostMapping("/friend/{friendId}/reject")
    public Result<Void> rejectFriendRequest(@PathVariable Long friendId) {
        Long userId = LoginUserContext.getUserId();
        friendService.rejectFriendRequest(userId, friendId);
        return Result.success();
    }

    @Operation(summary = "删除好友")
    @DeleteMapping("/friend/{friendId}")
    public Result<Void> deleteFriend(@PathVariable Long friendId) {
        Long userId = LoginUserContext.getUserId();
        friendService.deleteFriend(userId, friendId);
        return Result.success();
    }

    @Operation(summary = "获取好友列表")
    @GetMapping("/friends")
    public Result<List<Friend>> getFriendList() {
        Long userId = LoginUserContext.getUserId();
        return Result.success(friendService.getFriendList(userId));
    }

    @Operation(summary = "获取待处理的好友申请")
    @GetMapping("/friends/pending")
    public Result<List<Friend>> getPendingRequests() {
        Long userId = LoginUserContext.getUserId();
        return Result.success(friendService.getPendingRequests(userId));
    }

    @Operation(summary = "拉黑用户")
    @PostMapping("/blacklist/{blackId}")
    public Result<Void> addToBlacklist(@PathVariable Long blackId) {
        Long userId = LoginUserContext.getUserId();
        blacklistService.addToBlacklist(userId, blackId);
        return Result.success();
    }

    @Operation(summary = "取消拉黑")
    @DeleteMapping("/blacklist/{blackId}")
    public Result<Void> removeFromBlacklist(@PathVariable Long blackId) {
        Long userId = LoginUserContext.getUserId();
        blacklistService.removeFromBlacklist(userId, blackId);
        return Result.success();
    }

    @Operation(summary = "获取黑名单列表")
    @GetMapping("/blacklist")
    public Result<List<Long>> getBlacklist() {
        Long userId = LoginUserContext.getUserId();
        return Result.success(blacklistService.getBlacklist(userId));
    }

    @Operation(summary = "是否被拉黑")
    @GetMapping("/blacklist/check/{targetUserId}")
    public Result<Boolean> isBlocked(@PathVariable Long targetUserId) {
        Long userId = LoginUserContext.getUserId();
        return Result.success(blacklistService.isBlocked(userId, targetUserId));
    }

    @Operation(summary = "内部接口-检查是否为好友")
    @GetMapping("/internal/friend/check/{userId}/{targetUserId}")
    public Result<Boolean> isFriend(@PathVariable Long userId, @PathVariable Long targetUserId) {
        return Result.success(friendService.isFriend(userId, targetUserId));
    }

    @Operation(summary = "内部接口-检查是否被拉黑")
    @GetMapping("/internal/blacklist/check/{userId}/{targetUserId}")
    public Result<Boolean> isBlockedInternal(@PathVariable Long userId, @PathVariable Long targetUserId) {
        return Result.success(blacklistService.isBlocked(userId, targetUserId));
    }
}
