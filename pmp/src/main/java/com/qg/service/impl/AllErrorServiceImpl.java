package com.qg.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qg.domain.*;
import com.qg.domain.Module;
import com.qg.mapper.*;
import com.qg.service.AllErrorService;
import com.qg.vo.BackendResponsibilityVO;
import com.qg.vo.FrontendResponsibilityVO;
import com.qg.vo.MobileResponsibilityVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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

    @Autowired
    private ResponsibilityMapper responsibilityMapper;

    @Autowired
    private UsersMapper usersMapper;


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


        List<Responsibility> BackendResponsibilityList = responsibilityMapper.selectList(new LambdaQueryWrapper<Responsibility>()
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

            for (Responsibility responsibility : BackendResponsibilityList) {
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

        List<FrontendResponsibilityVO>frontendResponsibilityVOList = new ArrayList<>();

        List<Responsibility> FrontendResponsibilityList = responsibilityMapper.selectList(new LambdaQueryWrapper<Responsibility>()
                .eq(Responsibility::getProjectId, projectId)
                .eq(Responsibility::getPlatform, "frontend"));

        for (FrontendError frontendError : frontendErrors) {
            FrontendResponsibilityVO frontendResponsibilityVO = new FrontendResponsibilityVO();
            Long id = frontendError.getId();

            frontendResponsibilityVO.setProjectId(frontendError.getProjectId());
            frontendResponsibilityVO.setId(id);
            frontendResponsibilityVO.setBreadcrumbs(frontendError.getBreadcrumbs());
            frontendResponsibilityVO.setErrorType(frontendError.getErrorType());
            frontendResponsibilityVO.setCaptureType(frontendError.getCaptureType());
            frontendResponsibilityVO.setDuration(frontendError.getDuration());
            frontendResponsibilityVO.setElementInfo(frontendError.getElementInfo());
            frontendResponsibilityVO.setMessage(frontendError.getMessage());
            frontendResponsibilityVO.setStack(frontendError.getStack());
            frontendResponsibilityVO.setRequestInfo(frontendError.getRequestInfo());
            frontendResponsibilityVO.setResponseInfo(frontendError.getResponseInfo());
            frontendResponsibilityVO.setTags(frontendError.getTags());
            frontendResponsibilityVO.setSessionId(frontendError.getSessionId());
            frontendResponsibilityVO.setResourceInfo(frontendError.getResourceInfo());
            frontendResponsibilityVO.setTimestamp(frontendError.getTimestamp());
            frontendResponsibilityVO.setUserAgent(frontendError.getUserAgent());


            for (Responsibility responsibility : FrontendResponsibilityList) {
                if (responsibility.getErrorId().equals(id)) {
                    Long responsibleId = responsibility.getResponsibleId();
                    Long delegatorId = responsibility.getDelegatorId();

                    if (responsibleId != null && delegatorId != null) {
                        frontendResponsibilityVO.setDelegatorId(delegatorId);
                        Users responsibleUser = usersMapper.selectById(responsibleId);
                        if (responsibleUser != null) {
                            frontendResponsibilityVO.setName(responsibleUser.getUsername());
                            frontendResponsibilityVO.setAvatarUrl(responsibleUser.getAvatar());
                        }

                    }
                }
            }
            frontendResponsibilityVOList.add(frontendResponsibilityVO);
        }


        List<Responsibility> MobileResponsibilities = responsibilityMapper.selectList(new LambdaQueryWrapper<Responsibility>()
                .eq(Responsibility::getProjectId, projectId)
                .eq(Responsibility::getPlatform, "mobile"));

        List<MobileResponsibilityVO>mobileResponsibilityVOList = new ArrayList<>();

        for (MobileError error : mobileErrors) {
            Long id = error.getId();
            MobileResponsibilityVO mobileResponsibilityVO = new MobileResponsibilityVO();
            mobileResponsibilityVO.setId(id);
            mobileResponsibilityVO.setClassName(error.getClassName());
            mobileResponsibilityVO.setErrorType(error.getErrorType());
            mobileResponsibilityVO.setMessage(error.getMessage());
            mobileResponsibilityVO.setProjectId(error.getProjectId());
            mobileResponsibilityVO.setTimestamp(error.getTimestamp());
            mobileResponsibilityVO.setStack(error.getStack());

            for (Responsibility responsibility : MobileResponsibilities) {
                if (responsibility.getErrorId().equals(id)) {
                    Long responsibleId = responsibility.getResponsibleId();
                    Long delegatorId = responsibility.getDelegatorId();

                    if (responsibleId != null && delegatorId != null) {
                        mobileResponsibilityVO.setDelegatorId(delegatorId);
                        Users responsibleUser = usersMapper.selectById(responsibleId);
                        if (responsibleUser != null) {
                            mobileResponsibilityVO.setName(responsibleUser.getUsername());
                            mobileResponsibilityVO.setAvatarUrl(responsibleUser.getAvatar());
                        }

                    }
                }
            }
            mobileResponsibilityVOList.add(mobileResponsibilityVO);
        }

        return new Result(200,
                List.of(backendResponsibilityVOList, frontendResponsibilityVOList, mobileResponsibilityVOList),
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
