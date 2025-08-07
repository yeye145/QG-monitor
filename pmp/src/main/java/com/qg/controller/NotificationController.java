package com.qg.controller;

import com.qg.domain.Notification;
import com.qg.domain.Result;
import com.qg.service.NotificationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    @GetMapping("/selectByReceiverId")
    public Result selectByReceiverId(@RequestParam Long receiverId, @RequestParam Integer isSenderExist) {
        return notificationService.selectByReceiverId(receiverId, isSenderExist);
    }

    /**
     * 更新通知为已读
     * @param receiverId
     * @return
     */
    @PutMapping("/updateIsRead/{receiverId}")
    public Result updateIsRead(@PathVariable Long receiverId) {
        return notificationService.updateIsRead(receiverId);
    }


    @PostMapping("/add")
    public Result addNotification(@RequestBody List<Notification> notificationList) {
        return notificationService.add(notificationList);
    }
}
