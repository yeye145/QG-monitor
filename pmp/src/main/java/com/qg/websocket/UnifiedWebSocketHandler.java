package com.qg.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
@Slf4j
public class UnifiedWebSocketHandler extends TextWebSocketHandler {

    private final CopyOnWriteArrayList<WebSocketSession> sessions = new CopyOnWriteArrayList<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
        log.info("WebSocket连接建立，SessionID: {}, 当前连接数: {}", session.getId(), sessions.size());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            // 解析消息类型
            String payload = message.getPayload();
            Map<String, Object> messageMap = objectMapper.readValue(payload, Map.class);

            String messageType = (String) messageMap.get("type");
            Object data = messageMap.get("data");

            // 根据消息类型路由处理
            switch (messageType) {
                case "error":
                    handleErrorMessage(session, data);
                    break;
                case "alert":
                    handleAlertMessage(session, data);
                    break;
                case "heartbeat":
                    handleHeartbeat(session);
                    break;
                default:
                    log.warn("未知消息类型: {}", messageType);
                    break;
            }
        } catch (Exception e) {
            log.error("处理WebSocket消息失败", e);
        }
    }

    private void handleErrorMessage(WebSocketSession session, Object data) {
        // 处理错误消息
        log.debug("处理错误消息: {}", data);
    }

    private void handleAlertMessage(WebSocketSession session, Object data) {
        // 处理告警消息
        log.debug("处理告警消息: {}", data);
    }

    private void handleHeartbeat(WebSocketSession session) {
        // 处理心跳消息
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("type", "heartbeat");
            response.put("timestamp", System.currentTimeMillis());
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
        } catch (Exception e) {
            log.error("发送心跳响应失败", e);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
        log.info("WebSocket连接关闭，SessionID: {}, 状态: {}, 剩余连接数: {}",
                session.getId(), status, sessions.size());
    }

    /**
     * 广播消息给所有客户端
     */
    public void broadcastMessage(Object messageData) {
        try {
            String message = objectMapper.writeValueAsString(messageData);
            TextMessage textMessage = new TextMessage(message);

            sessions.forEach(session -> {
                try {
                    if (session.isOpen()) {
                        session.sendMessage(textMessage);
                    }
                } catch (Exception e) {
                    log.error("发送消息到Session {}失败", session.getId(), e);
                }
            });
        } catch (Exception e) {
            log.error("广播消息失败", e);
        }
    }

    /**
     * 发送消息给特定类型订阅者
     */
    public void sendMessageByType(String messageType, Object data) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("type", messageType);
            message.put("data", data);
            message.put("timestamp", System.currentTimeMillis());

            broadcastMessage(message);
        } catch (Exception e) {
            log.error("发送{}类型消息失败", messageType, e);
        }
    }
}
