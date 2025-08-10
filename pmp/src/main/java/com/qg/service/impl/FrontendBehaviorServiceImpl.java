package com.qg.service.impl;

import cn.hutool.json.JSONUtil;
import com.qg.aggregator.FrontendErrorAggregator;
import com.qg.domain.BackendError;
import com.qg.domain.FrontendBehavior;
import com.qg.domain.FrontendError;
import com.qg.domain.Result;
import com.qg.mapper.FrontendBehaviorMapper;
import com.qg.service.FrontendBehaviorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static com.qg.domain.Code.*;

/**
 * @Description: 前端行为应用  // 类说明
 * @ClassName: FrontendBehaviorServiceImpl    // 类名
 * @Author: lrt          // 创建者
 * @Date: 2025/8/7 21:34   // 时间
 * @Version: 1.0     // 版本
 */
@Service
@Slf4j
public class FrontendBehaviorServiceImpl implements FrontendBehaviorService {
    @Autowired
    private FrontendBehaviorMapper frontendBehaviorMapper;

    @Override
    public Result saveFrontendBehavior(String data) {
        // 参数校验
        if (data == null || data.trim().isEmpty()) {
            log.warn("前端用户行为数据为空");
            return new Result(BAD_REQUEST, "前端用户行为数据为空");
        }

        try {
            List<FrontendBehavior> behaviorList = JSONUtil.toList(data, FrontendBehavior.class);

            if (behaviorList == null || behaviorList.isEmpty()) {
                log.warn("解析后的前端用户行为数据为空");
                return new Result(BAD_REQUEST, "解析前端用户行为数据为空");
            }

            // 批量插入数据
            int successCount = 0;
            for (FrontendBehavior behavior : behaviorList) {
                if (behavior != null) { // 额外的空值检查
                    int result = frontendBehaviorMapper.insert(behavior);
                    successCount += result;
                }
            }

            log.info("保存前端用户行为数据完成，总共{}条，成功{}条", behaviorList.size(), successCount);
            return new Result(SUCCESS, "保存前端用户行为数据成功，共处理" + behaviorList.size() + "条数据");

        } catch (cn.hutool.json.JSONException e) {
            log.error("前端用户行为数据JSON解析失败: ", e);
            return new Result(BAD_REQUEST, "数据格式错误: " + e.getMessage());
        } catch (Exception e) {
            log.error("保存前端用户行为数据时发生异常: ", e);
            return new Result(INTERNAL_ERROR, "保存前端用户行为数据失败: " + e.getMessage());
        }
    }



}
