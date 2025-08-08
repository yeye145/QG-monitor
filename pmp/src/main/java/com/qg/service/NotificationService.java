package com.qg.service;

import com.qg.domain.Notification;
import com.qg.domain.Result;

import java.util.List;

public interface NotificationService {
    Result selectByReceiverId(Long receiverId, Integer isSenderExist);

    Result updateIsRead(Long receiverId);

    Result add(List<Notification> notificationList);

    Result updateIsReadById(Long id);

    Result deleteById(Long id);

    Result deleteByReceiverId(Long receiverId, Integer isSenderExist);
}
