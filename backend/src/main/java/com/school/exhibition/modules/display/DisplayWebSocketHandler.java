package com.school.exhibition.modules.display;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class DisplayWebSocketHandler extends TextWebSocketHandler {

    private static final Map<String, WebSocketSession> SESSIONS = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        SESSIONS.put(session.getId(), session);
        log.info("[WS] 大屏接入: {} 当前在线: {}", session.getId(), SESSIONS.size());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        SESSIONS.remove(session.getId());
        log.info("[WS] 大屏断开: {}", session.getId());
    }

    /**
     * 广播消息到所有大屏端
     * 
     * @param type    消息类型：REFRESH_PLAYLIST / FORCE_PROFILE / NOTICE
     * @param payload 载荷
     */
    public void broadcast(String type, String payload) {
        Map<String, Object> msg = Map.of("type", type, "payload", payload);
        try {
            String json = objectMapper.writeValueAsString(msg);
            TextMessage tm = new TextMessage(json);
            SESSIONS.values().forEach(s -> {
                try {
                    if (s.isOpen())
                        s.sendMessage(tm);
                } catch (Exception e) {
                    log.warn("推送失败: {}", e.getMessage());
                }
            });
        } catch (Exception e) {
            log.error("广播异常", e);
        }
    }
}