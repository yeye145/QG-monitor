package com.qg.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qg.domain.BackendError;
import com.qg.domain.Module;
import com.qg.domain.Result;
import com.qg.mapper.BackendErrorMapper;
import com.qg.mapper.ModuleMapper;
import com.qg.service.BackendErrorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Description: 后端错误应用  // 类说明
 * @ClassName: BackendErrorServiceImpl    // 类名
 * @Author: lrt          // 创建者
 * @Date: 2025/8/7 21:32   // 时间
 * @Version: 1.0     // 版本
 */
@Service
public class BackendErrorServiceImpl implements BackendErrorService {

    @Autowired
    private BackendErrorMapper backendErrorMapper;

    @Autowired
    private ModuleMapper moduleMapper;

    @Override
    public Result selectByCondition(String projectId, Long moduleId, String type) {
        if (projectId == null || moduleId == null || type == null) {
            return new Result(400, "参数不能为空");
        }

        LambdaQueryWrapper<BackendError> queryWrapper = new LambdaQueryWrapper<>();
        if (moduleMapper.selectById(moduleId) != null) {
            String moduleName = moduleMapper.selectById(moduleId).getModuleName();
            queryWrapper.eq(BackendError::getModule, moduleName)
                        .eq(BackendError::getProjectId, projectId)
                        .eq(BackendError::getType, type);
        } else {
            return new Result(400, "模块不存在");
        }

        List<BackendError> backendErrors = backendErrorMapper.selectList(queryWrapper);

        return new Result(200, backendErrors,"查询成功");

    }
}
