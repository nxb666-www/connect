package com.social.ai.service;

import com.social.ai.dto.AiChatDTO;
import com.social.ai.vo.AiChatVO;

import java.util.List;

public interface AiService {

    AiChatVO chat(Long userId, AiChatDTO dto);

    List<AiChatVO> getChatHistory(Long userId, String sessionId);

    List<String> getChatSessions(Long userId);

    void deleteSession(Long userId, String sessionId);
}
