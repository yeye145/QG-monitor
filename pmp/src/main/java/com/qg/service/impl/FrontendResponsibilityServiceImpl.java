package com.qg.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qg.domain.FrontendError;
import com.qg.domain.Responsibility;
import com.qg.domain.Result;
import com.qg.domain.Users;
import com.qg.mapper.FrontendErrorMapper;
import com.qg.mapper.ResponsibilityMapper;
import com.qg.mapper.UsersMapper;
import com.qg.service.FrontendResponsibilityService;
import com.qg.vo.FrontendResponsibilityVO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description: // 类说明
 * @ClassName: FrontendResponsibilityServiceImpl    // 类名
 * @Author: lrt          // 创建者
 * @Date: 2025/8/9 15:51   // 时间
 * @Version: 1.0     // 版本
 */
@Service
public class FrontendResponsibilityServiceImpl implements FrontendResponsibilityService {

    @Autowired
    private FrontendErrorMapper frontendErrorMapper;

    @Autowired
    private UsersMapper usersMapper;


    @Autowired
    private ResponsibilityMapper responsibilityMapper;



    @Override
    public Result selectByCondition(String projectId, String type) {
        if (projectId == null) {
            return new Result(400, "参数不能为空");
        }
        LambdaQueryWrapper<FrontendError> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.eq(FrontendError::getProjectId, projectId);

        if (type != null && !type.isEmpty()) {
            queryWrapper.eq(FrontendError::getErrorType, type);
        }

        List<FrontendError> frontendErrors = frontendErrorMapper.selectList(queryWrapper);

        List<FrontendResponsibilityVO>frontendResponsibilityVOList = new ArrayList<>();

        List<Responsibility> responsibilities = responsibilityMapper.selectList(new LambdaQueryWrapper<Responsibility>()
                .eq(Responsibility::getProjectId, projectId)
                .eq(Responsibility::getPlatform, "frontend"));

        for (FrontendError frontendError : frontendErrors) {
            FrontendResponsibilityVO frontendResponsibilityVO = new FrontendResponsibilityVO();
            Long id = frontendError.getId();

            frontendResponsibilityVO.setProjectId(frontendError.getProjectId());
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

            Users user = usersMapper.selectOne(new LambdaQueryWrapper<Users>()
                    .eq(Users::getId, id));

            for (Responsibility responsibility : responsibilities) {
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

        return new Result(200, frontendResponsibilityVOList, "查询成功");
    }
}
