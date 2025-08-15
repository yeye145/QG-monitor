package com.qg.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qg.domain.Message;
import com.qg.domain.Result;
import com.qg.mapper.MessageMapper;
import com.qg.service.MessageService;
import io.swagger.v3.oas.annotations.servers.Server;
import org.apache.ibatis.annotations.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Description: // 类说明
 * @ClassName: MessageServiceImpl    // 类名
 * @Author: lrt          // 创建者
 * @Date: 2025/8/13 19:02   // 时间
 * @Version: 1.0     // 版本
 */
@Service
public class MessageServiceImpl implements MessageService {

    @Autowired
    private MessageMapper   messageMapper;


    @Override
    public Result sendMessage(List<Message> messages) {
        if (messages == null || messages.isEmpty()) {
            return new Result(400, "消息列表不能为空");
        }
        int totalMessages = messages.size();
        int count = 0;
        for (Message message : messages) {
            count += messageMapper.insert(message);
        }

        if (count == totalMessages) {
            return new Result(200, "消息发送成功");
        } else {
            return new Result(500, "部分消息发送失败");
        }
    }

    @Override
    public Result getAllMessages(Long userId) {
        if (userId == null) {
            return new Result(400, "用户ID不能为空");
        }
        LambdaQueryWrapper<Message> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Message::getSendId, userId)
                .or()
                .eq(Message::getReceiverId, userId)
                .orderByDesc(Message::getTimestamp); // 按时间降序排列

        List<Message> messages = messageMapper.selectList(queryWrapper);
        if (messages != null && !messages.isEmpty()) {
            return new Result(200, messages, "查询成功");
        } else {
            return new Result(201, null,"没有找到相关消息");
        }

    }


}
