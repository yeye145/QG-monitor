package com.qg.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qg.domain.FrontendError;
import com.qg.domain.FrontendPerformance;
import com.qg.domain.Result;
import com.qg.mapper.FrontendErrorMapper;
import com.qg.service.FrontendErrorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Description: 前端错误应用  // 类说明
 * @ClassName: FrontendErrorServiceImpl    // 类名
 * @Author: lrt          // 创建者
 * @Date: 2025/8/7 21:35   // 时间
 * @Version: 1.0     // 版本
 */
@Service
public class FrontendErrorServiceImpl implements FrontendErrorService {

    @Autowired
    private FrontendErrorMapper frontendErrorMapper;

    @Override
    public Result selectByCondition(String projectId, String type) {
        if (projectId == null || type == null) {
            return new Result(400, "参数不能为空");
        }
        LambdaQueryWrapper<FrontendError> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.eq(FrontendError::getProjectId, projectId)
                    .eq(FrontendError::getErrorType, type);

        List<FrontendError> frontendErrors = frontendErrorMapper.selectList(queryWrapper);

        return new Result(200, frontendErrors, "查询成功");
    }

    @Override
    public Integer saveFrontendError(List<FrontendError> frontendErrors) {
        if (frontendErrors == null || frontendErrors.isEmpty()) {
            return 0; // 返回0表示没有数据需要保存
        }
        int count = 0;

        for (FrontendError error : frontendErrors) {
            count += frontendErrorMapper.insert(error);
        }

        return frontendErrors.size() == count ? count : 0; // 返回保存的记录数
    }
}
