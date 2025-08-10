package com.qg.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qg.domain.*;
import com.qg.domain.Module;
import com.qg.mapper.BackendErrorMapper;
import com.qg.mapper.ModuleMapper;
import com.qg.mapper.ResponsibilityMapper;
import com.qg.mapper.UsersMapper;
import com.qg.service.BackendPerformanceService;
import com.qg.service.BackendResponsibilityService;
import com.qg.service.ResponsibilityService;
import com.qg.vo.BackendResponsibilityVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description: // 类说明
 * @ClassName: BackendResponsibilityServiceImpl    // 类名
 * @Author: lrt          // 创建者
 * @Date: 2025/8/9 15:50   // 时间
 * @Version: 1.0     // 版本
 */
@Service
public class BackendResponsibilityServiceImpl implements BackendResponsibilityService {


    @Autowired
    private BackendErrorMapper backendErrorMapper;

    @Autowired
    private ModuleMapper moduleMapper;

    @Autowired
    private ResponsibilityMapper responsibilityMapper;

    @Autowired
    private UsersMapper usersMapper;


    @Override
    public Result selectByCondition(String projectId, Long moduleId, String type) {
        if (projectId == null) {
            return new Result(400, "参数不能为空");
        }

        LambdaQueryWrapper<BackendError> queryWrapper = new LambdaQueryWrapper<>();
        Module module = moduleMapper.selectById(moduleId);
        if (module != null) {
            String moduleName = module.getModuleName();
            queryWrapper.eq(BackendError::getModule, moduleName);
        } else {
            return new Result(400, "模块不存在");
        }
        if (type != null && !type.isEmpty()) {
            queryWrapper.eq(BackendError::getErrorType, type);
        }
        queryWrapper.eq(BackendError::getProjectId, projectId);

        List<BackendError> backendErrors = backendErrorMapper.selectList(queryWrapper);

        List<Responsibility> responsibilityList = responsibilityMapper.selectList(new LambdaQueryWrapper<Responsibility>()
                .eq(Responsibility::getProjectId, projectId)
                .eq(Responsibility::getPlatform, "backend"));

        List<BackendResponsibilityVO>backendResponsibilityVOList = new ArrayList<>();

        for (BackendError error : backendErrors) {
            Long id = error.getId();
            BackendResponsibilityVO backendResponsibilityVO = new BackendResponsibilityVO();
            backendResponsibilityVO.setEnvironment(error.getEnvironment());
            backendResponsibilityVO.setId(id);
            backendResponsibilityVO.setEnvironmentSnapshot(error.getEnvironmentSnapshot());
            backendResponsibilityVO.setProjectId(error.getProjectId());
            backendResponsibilityVO.setTimestamp(error.getTimestamp());
            backendResponsibilityVO.setErrorType(error.getErrorType());
            backendResponsibilityVO.setStack(error.getStack());
            backendResponsibilityVO.setModule(error.getModule());

            for (Responsibility responsibility : responsibilityList) {
                if (responsibility.getErrorId().equals(id)) {
                    Long responsibleId = responsibility.getResponsibleId();
                    Long delegatorId = responsibility.getDelegatorId();

                    if (responsibleId != null && delegatorId != null) {
                        backendResponsibilityVO.setDelegatorId(delegatorId);
                        Users responsibleUser = usersMapper.selectById(responsibleId);
                        if (responsibleUser != null) {
                            backendResponsibilityVO.setName(responsibleUser.getUsername());
                            backendResponsibilityVO.setAvatarUrl(responsibleUser.getAvatar());
                        }

                    }
                }
            }
            backendResponsibilityVOList.add(backendResponsibilityVO);
        }

        return new Result(200, List.of(backendResponsibilityVOList,new ArrayList<>(),new ArrayList<>()),"查询成功");

    }
}
