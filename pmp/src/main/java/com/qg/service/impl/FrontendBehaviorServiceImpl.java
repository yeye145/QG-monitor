package com.qg.service.impl;

import com.qg.domain.FrontendBehavior;
import com.qg.mapper.FrontendBehaviorMapper;
import com.qg.service.FrontendBehaviorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Description: 前端行为应用  // 类说明
 * @ClassName: FrontendBehaviorServiceImpl    // 类名
 * @Author: lrt          // 创建者
 * @Date: 2025/8/7 21:34   // 时间
 * @Version: 1.0     // 版本
 */
@Service
public class FrontendBehaviorServiceImpl implements FrontendBehaviorService {
    @Autowired
    private FrontendBehaviorMapper frontendBehaviorMapper;

    @Override
    public Integer saveFrontendBehavior(List<FrontendBehavior> behaviorList) {
        if (behaviorList == null || behaviorList.isEmpty()) {
            return 0; // 返回0表示没有数据需要保存
        }
        int count = 0;
        for (FrontendBehavior behavior : behaviorList) {
            // 假设有一个方法来保存单个行为数据条目
            count += frontendBehaviorMapper.insert(behavior);
        }
        return behaviorList.size() == count ? count : 0; // 返回保存的记录
    }
}
