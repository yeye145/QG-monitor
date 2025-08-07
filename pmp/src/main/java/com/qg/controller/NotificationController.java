package com.qg.controller;

import com.qg.domain.Result;
import com.qg.service.NotificationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "通知")
@RestController
@RequestMapping("/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    /**
     * 根据接收者id查询通知（未读）
     * @param receiverId
     * @return
     */
    @GetMapping("/selectByReceiverId/{receiverId}")
    public Result selectByReceiverId(@PathVariable Integer receiverId) {
        return notificationService.selectByReceiverId(receiverId);
    }
}
