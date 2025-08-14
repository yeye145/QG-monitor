package com.qg.controller;

import com.qg.domain.Message;
import com.qg.domain.Result;
import com.qg.service.MessageService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Description: // 类说明
 * @ClassName: MessageController    // 类名
 * @Author: lrt          // 创建者
 * @Date: 2025/8/13 20:36   // 时间
 * @Version: 1.0     // 版本
 */
@Slf4j
@RequestMapping("/messages")
@RestController
public class MessageController {

    @Autowired
    private MessageService messageService;


    @PostMapping("/sendMessages")
    public Result sendMessage(@RequestBody List<Message> messages) {
        System.out.println("Sending messages: " + messages);
        log.info("Sending message: {}", messages);
        return messageService.sendMessage(messages);
    }

    @GetMapping("/getMessages")
    public Result getMessages(@RequestParam Long userId) {
        System.out.println("Fetching messages for user ID: " + userId);
        return  messageService.getAllMessages(userId);
    }

}
