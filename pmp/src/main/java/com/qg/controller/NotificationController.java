package com.qg.controller;

import com.qg.domain.Notification;
import com.qg.domain.Result;
import com.qg.service.NotificationService;
import io.swagger.v3.oas.annotations.tags.Tag;
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
     * 根据接收者id查询通知
     * @param receiverId
     * @return
     */
    @GetMapping("/selectByReceiverId")
    public Result selectByReceiverId(@RequestParam Long receiverId, @RequestParam Integer isSenderExist) {
        return notificationService.selectByReceiverId(receiverId, isSenderExist);
    }

    /**
     * 根据接收者id更新通知为已读
     * @param receiverId
     * @return
     */
    @PutMapping("/updateIsRead/{receiverId}")
    public Result updateIsRead(@PathVariable Long receiverId) {
        return notificationService.updateIsRead(receiverId);
    }

    /**
     * 根据通知id更新通知为已读
     * @param id
     * @return
     */
    @PutMapping("/updateIsReadById/{id}")
    public Result updateIsReadById(@PathVariable Long id) {
        return notificationService.updateIsReadById(id);
    }


    /**
     * 添加通知
     * @param notificationList
     * @return
     */
    @PostMapping("/add")
    public Result addNotification(@RequestBody List<Notification> notificationList) {
        return notificationService.add(notificationList);
    }

    /**
     * 根据 id 删除 通知
     * @param id
     * @return
     */
    @DeleteMapping("/deleteById/{id}")
    public Result deleteById(@PathVariable Long id) {
        return notificationService.deleteById(id);
    }

    /**
     * 根据 接收者 id 删除 通知
     * @param receiverId
     * @return
     */
    @DeleteMapping("/deleteByReceiverId/{receiverId}")
    public Result deleteByReceiverId(@PathVariable Long receiverId) {
        return notificationService.deleteByReceiverId(receiverId);
    }
}
