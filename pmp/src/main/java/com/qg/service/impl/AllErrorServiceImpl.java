package com.qg.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qg.domain.BackendError;
import com.qg.domain.FrontendError;
import com.qg.domain.MobileError;
import com.qg.domain.Module;
import com.qg.domain.Result;
import com.qg.mapper.BackendErrorMapper;
import com.qg.mapper.FrontendErrorMapper;
import com.qg.mapper.MobileErrorMapper;
import com.qg.mapper.ModuleMapper;
import com.qg.service.AllErrorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Description: 所有错误应用类  // 类说明
 * @ClassName: AllErrorServiceimpl    // 类名
 * @Author: lrt          // 创建者
 * @Date: 2025/8/8 11:00   // 时间
 * @Version: 1.0     // 版本
 */
@Service
public class AllErrorServiceImpl implements AllErrorService {
    @Autowired
    private BackendErrorMapper backendErrorMapper;

    @Autowired
    private FrontendErrorMapper frontendErrorMapper;

    @Autowired
    private MobileErrorMapper mobileErrorMapper;

    @Autowired
    private ModuleMapper moduleMapper;


    @Override
    public Result selectByCondition(String projectId, Long moduleId, String type) {
        if (projectId == null || projectId.isEmpty()) {
            return new Result(400, "参数错误");
        }
        LambdaQueryWrapper<BackendError> backendQueryWrapper = new LambdaQueryWrapper<>();
        LambdaQueryWrapper<FrontendError> frontendQueryWrapper = new LambdaQueryWrapper<>();
        LambdaQueryWrapper<MobileError> mobileQueryWrapper = new LambdaQueryWrapper<>();


        Module module = moduleMapper.selectById(moduleId);
        if (module != null) {
            backendQueryWrapper
                    .eq(BackendError::getModule, module.getModuleName());
        }

        backendQueryWrapper.eq(BackendError::getProjectId, projectId);

        frontendQueryWrapper.eq(FrontendError::getProjectId, projectId);

        mobileQueryWrapper.eq(MobileError::getProjectId, projectId);


        if (type != null && !type.isEmpty()) {
            backendQueryWrapper.eq(BackendError::getErrorType, type);
            frontendQueryWrapper.eq(FrontendError::getErrorType, type);
            mobileQueryWrapper.eq(MobileError::getErrorType, type);

        }

        List<BackendError> backendErrors = backendErrorMapper.selectList(backendQueryWrapper);
        List<FrontendError> frontendErrors = frontendErrorMapper.selectList(frontendQueryWrapper);
        List<MobileError> mobileErrors = mobileErrorMapper.selectList(mobileQueryWrapper);

        return new Result(200,
                List.of(backendErrors, frontendErrors, mobileErrors),
                "查询成功");
    }

    @Override
    public Result selectById(Long id) {
        LambdaQueryWrapper<BackendError> backendQueryWrapper = new LambdaQueryWrapper<>();
        LambdaQueryWrapper<FrontendError> frontendQueryWrapper = new LambdaQueryWrapper<>();
        LambdaQueryWrapper<MobileError> mobileQueryWrapper = new LambdaQueryWrapper<>();

        if (id == null) {
            return new Result(400, "参数错误");
        }

        backendQueryWrapper.eq(BackendError::getId, id);
        frontendQueryWrapper.eq(FrontendError::getId, id);
        mobileQueryWrapper.eq(MobileError::getId, id);

        BackendError backendError = backendErrorMapper.selectOne(backendQueryWrapper);
        FrontendError frontendError = frontendErrorMapper.selectOne(frontendQueryWrapper);
        MobileError mobileError = mobileErrorMapper.selectOne(mobileQueryWrapper);

        return new Result(200,
                List.of(backendError, frontendError, mobileError),
                "查询成功");
    }
}
