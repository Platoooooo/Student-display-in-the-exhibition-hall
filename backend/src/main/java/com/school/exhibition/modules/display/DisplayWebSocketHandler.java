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
        try {
            session.sendMessage(new TextMessage(
                    objectMapper.writeValueAsString(Map.of("type", "WELCOME", "payload", session.getId()))));
        } catch (Exception ignored) {}
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        SESSIONS.remove(session.getId());
        log.info("[WS] 大屏断开: {} 剩余: {}", session.getId(), SESSIONS.size());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // 心跳：收到 ping 回 pong
        String payload = message.getPayload();
        if (payload == null) return;
        if (payload.contains("\"ping\"") || "ping".equalsIgnoreCase(payload.trim())) {
            session.sendMessage(new TextMessage(
                    objectMapper.writeValueAsString(Map.of("type", "PONG",
                            "payload", String.valueOf(System.currentTimeMillis())))));
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.warn("[WS] 传输错误: {}", exception.getMessage());
    }

    public int onlineCount() {
        return SESSIONS.size();
    }

    /**
     * 广播到所有大屏端
     *
     * @param type    REFRESH_PLAYLIST / FORCE_PROFILE / NOTICE / SWITCH_MODE
     * @param payload 载荷
     */
    public void broadcast(String type, String payload) {
        Map<String, Object> msg = Map.of("type", type, "payload", payload == null ? "" : payload);
        try {
            String json = objectMapper.writeValueAsString(msg);
            TextMessage tm = new TextMessage(json);
            SESSIONS.values().forEach(s -> {
                try {
                    if (s.isOpen()) s.sendMessage(tm);
                } catch (Exception e) {
                    log.warn("推送失败 {}: {}", s.getId(), e.getMessage());
                }
            });
        } catch (Exception e) {
            log.error("广播异常", e);
        }
    }
}
