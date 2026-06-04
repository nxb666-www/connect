package com.social.post.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.social.common.core.result.Result;
import com.social.common.security.context.LoginUserContext;
import com.social.post.dto.CommentCreateDTO;
import com.social.post.dto.PostCreateDTO;
import com.social.post.service.CommentService;
import com.social.post.service.PostService;
import com.social.post.vo.CommentVO;
import com.social.post.vo.PostVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "动态管理", description = "动态发布、评论、点赞、收藏等接口")
@RestController
@RequestMapping("/api/post")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final CommentService commentService;

    @Operation(summary = "发布动态")
    @PostMapping
    public Result<Long> createPost(@Valid @RequestBody PostCreateDTO dto) {
        Long userId = LoginUserContext.getUserId();
        Long postId = postService.createPost(userId, dto);
        return Result.success(postId);
    }

    @Operation(summary = "获取动态详情")
    @GetMapping("/{postId}")
    public Result<PostVO> getPostDetail(@PathVariable Long postId) {
        Long userId = LoginUserContext.getUserId();
        PostVO vo = postService.getPostDetail(postId, userId);
        return Result.success(vo);
    }

    @Operation(summary = "获取动态列表（广场）")
    @GetMapping("/feed")
    public Result<Page<PostVO>> getPostFeed(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        Long userId = LoginUserContext.getUserId();
        Page<PostVO> page = postService.getPostFeed(userId, pageNum, pageSize);
        return Result.success(page);
    }

    @Operation(summary = "获取用户动态列表")
    @GetMapping("/user/{userId}")
    public Result<Page<PostVO>> getUserPosts(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        Long currentUserId = LoginUserContext.getUserId();
        Page<PostVO> page = postService.getUserPosts(userId, pageNum, pageSize);
        return Result.success(page);
    }

    @Operation(summary = "删除动态")
    @DeleteMapping("/{postId}")
    public Result<Void> deletePost(@PathVariable Long postId) {
        Long userId = LoginUserContext.getUserId();
        postService.deletePost(postId, userId);
        return Result.success();
    }

    @Operation(summary = "点赞动态")
    @PostMapping("/{postId}/like")
    public Result<Void> likePost(@PathVariable Long postId) {
        Long userId = LoginUserContext.getUserId();
        postService.likePost(postId, userId);
        return Result.success();
    }

    @Operation(summary = "取消点赞")
    @DeleteMapping("/{postId}/like")
    public Result<Void> unlikePost(@PathVariable Long postId) {
        Long userId = LoginUserContext.getUserId();
        postService.unlikePost(postId, userId);
        return Result.success();
    }

    @Operation(summary = "收藏动态")
    @PostMapping("/{postId}/collect")
    public Result<Void> collectPost(@PathVariable Long postId) {
        Long userId = LoginUserContext.getUserId();
        postService.collectPost(postId, userId);
        return Result.success();
    }

    @Operation(summary = "取消收藏")
    @DeleteMapping("/{postId}/collect")
    public Result<Void> uncollectPost(@PathVariable Long postId) {
        Long userId = LoginUserContext.getUserId();
        postService.uncollectPost(postId, userId);
        return Result.success();
    }

    @Operation(summary = "发表评论")
    @PostMapping("/comment")
    public Result<Long> createComment(@Valid @RequestBody CommentCreateDTO dto) {
        Long userId = LoginUserContext.getUserId();
        Long commentId = commentService.createComment(userId, dto);
        return Result.success(commentId);
    }

    @Operation(summary = "获取动态评论列表")
    @GetMapping("/{postId}/comments")
    public Result<List<CommentVO>> getPostComments(@PathVariable Long postId) {
        List<CommentVO> comments = commentService.getPostComments(postId);
        return Result.success(comments);
    }

    @Operation(summary = "删除评论")
    @DeleteMapping("/comment/{commentId}")
    public Result<Void> deleteComment(@PathVariable Long commentId) {
        Long userId = LoginUserContext.getUserId();
        commentService.deleteComment(commentId, userId);
        return Result.success();
    }

    @Operation(summary = "获取用户收藏的动态")
    @GetMapping("/collected/{userId}")
    public Result<Page<PostVO>> getUserCollectedPosts(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        Long currentUserId = LoginUserContext.getUserId();
        Page<PostVO> page = postService.getUserCollectedPosts(userId, currentUserId, pageNum, pageSize);
        return Result.success(page);
    }

    @Operation(summary = "获取用户点赞的动态")
    @GetMapping("/liked/{userId}")
    public Result<Page<PostVO>> getUserLikedPosts(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        Long currentUserId = LoginUserContext.getUserId();
        Page<PostVO> page = postService.getUserLikedPosts(userId, currentUserId, pageNum, pageSize);
        return Result.success(page);
    }

    @Operation(summary = "搜索动态")
    @GetMapping("/search")
    public Result<Map<String, Object>> searchPosts(@RequestParam String keyword) {
        Long userId = LoginUserContext.getUserId();
        List<PostVO> posts = postService.searchPosts(keyword, userId);
        List<Map<String, Object>> topics = postService.searchTopics(keyword);
        return Result.success(Map.of("posts", posts, "topics", topics));
    }

    @Operation(summary = "内部服务调用-获取统计数据")
    @GetMapping("/internal/stats")
    public Result<Map<String, Object>> getPostStats(@RequestParam String date) {
        return Result.success(postService.getPostStats(date));
    }

    @Operation(summary = "内部服务调用-搜索动态")
    @GetMapping("/internal/search")
    public Result<Map<String, Object>> internalSearchPosts(@RequestParam String keyword) {
        Long userId = LoginUserContext.getUserId();
        if (userId == null) userId = 0L;
        List<PostVO> posts = postService.searchPosts(keyword, userId);
        List<Map<String, Object>> topics = postService.searchTopics(keyword);
        return Result.success(Map.of("posts", posts, "topics", topics));
    }
}
