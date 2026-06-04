package com.social.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.social.ai.dto.AiChatDTO;
import com.social.ai.entity.AiChat;
import com.social.ai.mapper.AiChatMapper;
import com.social.ai.service.AiService;
import com.social.ai.vo.AiChatVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiServiceImpl implements AiService {

    private final AiChatMapper aiChatMapper;

    @Value("${doubao.api-key}")
    private String apiKey;

    @Value("${doubao.api-url}")
    private String apiUrl;

    @Value("${doubao.model}")
    private String model;

    private WebClient webClient;

    private WebClient getWebClient() {
        if (webClient == null) {
            webClient = WebClient.builder()
                    .baseUrl(apiUrl)
                    .defaultHeader("Authorization", "Bearer " + apiKey)
                    .defaultHeader("Content-Type", "application/json")
                    .build();
        }
        return webClient;
    }

    @Override
    @Transactional
    public AiChatVO chat(Long userId, AiChatDTO dto) {
        String sessionId = dto.getSessionId();
        if (sessionId == null || sessionId.isEmpty()) {
            sessionId = UUID.randomUUID().toString().replace("-", "");
        }

        AiChat userMessage = new AiChat();
        userMessage.setUserId(userId);
        userMessage.setSessionId(sessionId);
        userMessage.setRole("user");
        userMessage.setContent(dto.getMessage());
        aiChatMapper.insert(userMessage);

        // 构建对话历史
        LambdaQueryWrapper<AiChat> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiChat::getUserId, userId)
                .eq(AiChat::getSessionId, sessionId)
                .orderByAsc(AiChat::getCreateTime);
        List<AiChat> history = aiChatMapper.selectList(wrapper);
        List<Map<String, String>> messages = history.stream()
                .map(m -> Map.of("role", m.getRole(), "content", m.getContent()))
                .collect(Collectors.toList());

        String aiResponse = callDoubaoApi(messages);

        AiChat aiMessage = new AiChat();
        aiMessage.setUserId(userId);
        aiMessage.setSessionId(sessionId);
        aiMessage.setRole("assistant");
        aiMessage.setContent(aiResponse);
        aiChatMapper.insert(aiMessage);

        return convertToVO(aiMessage);
    }

    private String callDoubaoApi(List<Map<String, String>> messages) {
        Map<String, Object> body = Map.of(
                "model", model,
                "messages", messages
        );

        try {
            Map<String, Object> response = getWebClient()
                    .post()
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block(Duration.ofSeconds(30));

            if (response != null && response.containsKey("choices")) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    if (message != null) {
                        return (String) message.get("content");
                    }
                }
            }
            return "抱歉，我暂时无法回复，请稍后重试。";
        } catch (WebClientResponseException e) {
            log.error("Doubao API error: status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
            return "抱歉，AI服务暂时不可用，请稍后重试。";
        } catch (Exception e) {
            log.error("Doubao API call failed", e);
            return "抱歉，AI服务暂时不可用，请稍后重试。";
        }
    }

    @Override
    public List<AiChatVO> getChatHistory(Long userId, String sessionId) {
        LambdaQueryWrapper<AiChat> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiChat::getUserId, userId)
                .eq(AiChat::getSessionId, sessionId)
                .orderByAsc(AiChat::getCreateTime);
        return aiChatMapper.selectList(wrapper).stream()
                .map(this::convertToVO)
                .toList();
    }

    @Override
    public List<String> getChatSessions(Long userId) {
        LambdaQueryWrapper<AiChat> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiChat::getUserId, userId)
                .select(AiChat::getSessionId)
                .groupBy(AiChat::getSessionId);
        return aiChatMapper.selectList(wrapper).stream()
                .map(AiChat::getSessionId)
                .toList();
    }

    @Override
    @Transactional
    public void deleteSession(Long userId, String sessionId) {
        LambdaQueryWrapper<AiChat> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiChat::getUserId, userId)
                .eq(AiChat::getSessionId, sessionId);
        aiChatMapper.delete(wrapper);
    }

    private AiChatVO convertToVO(AiChat chat) {
        AiChatVO vo = new AiChatVO();
        vo.setId(chat.getId());
        vo.setSessionId(chat.getSessionId());
        vo.setRole(chat.getRole());
        vo.setContent(chat.getContent());
        vo.setCreateTime(chat.getCreateTime());
        return vo;
    }
}
