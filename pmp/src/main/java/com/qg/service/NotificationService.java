package com.qg.service;

import com.qg.domain.Notification;
import com.qg.domain.Result;

public interface NotificationService {
    Result selectByReceiverId(Integer receiverId);

    Result addNotification(Notification notification);
}
