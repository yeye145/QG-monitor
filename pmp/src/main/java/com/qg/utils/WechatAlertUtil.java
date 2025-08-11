package com.qg.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.qg.repository.RepositoryConstants.*;

@Slf4j
@Component
public class WechatAlertUtil {
    private final RestTemplate restTemplate;

    public WechatAlertUtil(RestTemplateBuilder builder) {
        this.restTemplate = builder.build();
    }

    public void sendAlert(String webhookUrl, String message, List<String> mentionedMobileList) {
        try {
            // 设置请求头为JSON
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // 构建消息体
            Map<String, Object> requestBody = new HashMap<>();
            // 固定为text类型
            requestBody.put(MESSAGE_TYPE.getAsString(), TEXT.getAsString());

            // 设置消息内容
            Map<String, Object> textContent = new HashMap<>();
            // 消息正文
            textContent.put(CONTENT.getAsString(), message);

            // 添加告警接收人
            if (mentionedMobileList != null && !mentionedMobileList.isEmpty()) {
                textContent.put(MENTIONED_MOBILE_LIST.getAsString(), mentionedMobileList);
            }

            // 组合最终请求体
            requestBody.put(TEXT.getAsString(), textContent);

            // 发送请求
            restTemplate.postForEntity(webhookUrl, new HttpEntity<>(requestBody, headers), String.class);
        } catch (Exception e) {
            log.error("发送告警失败:{}", e.getMessage());
        }
    }
}