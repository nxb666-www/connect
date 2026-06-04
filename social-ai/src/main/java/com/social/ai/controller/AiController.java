package com.social.ai.controller;

import com.social.ai.dto.AiChatDTO;
import com.social.ai.service.AiService;
import com.social.ai.vo.AiChatVO;
import com.social.common.core.result.Result;
import com.social.common.security.context.LoginUserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "AI对话管理", description = "AI对话、会话管理等接口")
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    @Operation(summary = "发送AI对话消息")
    @PostMapping("/chat")
    public Result<AiChatVO> chat(@Valid @RequestBody AiChatDTO dto) {
        Long userId = LoginUserContext.getUserId();
        AiChatVO vo = aiService.chat(userId, dto);
        return Result.success(vo);
    }

    @Operation(summary = "获取对话历史")
    @GetMapping("/history/{sessionId}")
    public Result<List<AiChatVO>> getChatHistory(@PathVariable String sessionId) {
        Long userId = LoginUserContext.getUserId();
        return Result.success(aiService.getChatHistory(userId, sessionId));
    }

    @Operation(summary = "获取会话列表")
    @GetMapping("/sessions")
    public Result<List<String>> getChatSessions() {
        Long userId = LoginUserContext.getUserId();
        return Result.success(aiService.getChatSessions(userId));
    }

    @Operation(summary = "删除会话")
    @DeleteMapping("/session/{sessionId}")
    public Result<Void> deleteSession(@PathVariable String sessionId) {
        Long userId = LoginUserContext.getUserId();
        aiService.deleteSession(userId, sessionId);
        return Result.success();
    }
}
