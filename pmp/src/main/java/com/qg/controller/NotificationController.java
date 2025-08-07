package com.qg.controller;

import com.qg.domain.Notification;
import com.qg.domain.Result;
import com.qg.service.NotificationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping
    public Result addNotification(@RequestBody Notification notification) {
        return notificationService.addNotification(notification);
    }
}
