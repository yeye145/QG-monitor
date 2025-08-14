package com.qg.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qg.domain.*;
import com.qg.mapper.FrontendErrorMapper;
import com.qg.mapper.ResponsibilityMapper;
import com.qg.mapper.UsersMapper;
import com.qg.service.FrontendResponsibilityService;
import com.qg.vo.FrontendResponsibilityVO;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description: // 类说明
 * @ClassName: FrontendResponsibilityServiceImpl    // 类名
 * @Author: lrt          // 创建者
 * @Date: 2025/8/9 15:51   // 时间
 * @Version: 1.0     // 版本
 */
@Service
@Slf4j
public class FrontendResponsibilityServiceImpl implements FrontendResponsibilityService {

    @Autowired
    private FrontendErrorMapper frontendErrorMapper;

    @Autowired
    private UsersMapper usersMapper;


    @Autowired
    private ResponsibilityMapper responsibilityMapper;



//    @Override
//    public Result selectByCondition(String projectId, String type) {
//        if (projectId == null) {
//            return new Result(400, "参数不能为空");
//        }
//        LambdaQueryWrapper<FrontendError> queryWrapper = new LambdaQueryWrapper<>();
//
//        queryWrapper.eq(FrontendError::getProjectId, projectId);
//
//        if (type != null && !type.isEmpty()) {
//            queryWrapper.eq(FrontendError::getErrorType, type);
//        }
//
//        List<FrontendError> frontendErrors = frontendErrorMapper.selectList(queryWrapper);
//
//        List<FrontendResponsibilityVO>frontendResponsibilityVOList = new ArrayList<>();
//
//        List<Responsibility> responsibilities = responsibilityMapper.selectList(new LambdaQueryWrapper<Responsibility>()
//                .eq(Responsibility::getProjectId, projectId)
//                .eq(Responsibility::getPlatform, "frontend"));
//
//        for (FrontendError frontendError : frontendErrors) {
//            FrontendResponsibilityVO frontendResponsibilityVO = new FrontendResponsibilityVO();
//            Long id = frontendError.getId();
//
//            frontendResponsibilityVO.setProjectId(frontendError.getProjectId());
//            frontendResponsibilityVO.setBreadcrumbs(frontendError.getBreadcrumbs());
//            frontendResponsibilityVO.setErrorType(frontendError.getErrorType());
//            frontendResponsibilityVO.setId(id);
//            frontendResponsibilityVO.setCaptureType(frontendError.getCaptureType());
//            frontendResponsibilityVO.setDuration(frontendError.getDuration());
//            frontendResponsibilityVO.setElementInfo(frontendError.getElementInfo());
//            frontendResponsibilityVO.setMessage(frontendError.getMessage());
//            frontendResponsibilityVO.setStack(frontendError.getStack());
//            frontendResponsibilityVO.setRequestInfo(frontendError.getRequestInfo());
//            frontendResponsibilityVO.setResponseInfo(frontendError.getResponseInfo());
//            frontendResponsibilityVO.setTags(frontendError.getTags());
//            frontendResponsibilityVO.setSessionId(frontendError.getSessionId());
//            frontendResponsibilityVO.setResourceInfo(frontendError.getResourceInfo());
//            frontendResponsibilityVO.setTimestamp(frontendError.getTimestamp());
//            frontendResponsibilityVO.setUserAgent(frontendError.getUserAgent());
//
//            Users user = usersMapper.selectOne(new LambdaQueryWrapper<Users>()
//                    .eq(Users::getId, id));
//
//            for (Responsibility responsibility : responsibilities) {
//                if (responsibility.getErrorId().equals(id)) {
//                    Long responsibleId = responsibility.getResponsibleId();
//                    Long delegatorId = responsibility.getDelegatorId();
//
//                    if (responsibleId != null && delegatorId != null) {
//                        frontendResponsibilityVO.setDelegatorId(delegatorId);
//                        Users responsibleUser = usersMapper.selectById(responsibleId);
//                        if (responsibleUser != null) {
//                            frontendResponsibilityVO.setName(responsibleUser.getUsername());
//                            frontendResponsibilityVO.setAvatarUrl(responsibleUser.getAvatar());
//                        }
//
//                    }
//                }
//            }
//            frontendResponsibilityVOList.add(frontendResponsibilityVO);
//
//
//        }
//
//        return new Result(200, List.of(new ArrayList<>(),frontendResponsibilityVOList,new ArrayList<>()), "查询成功");
//    }
@Override
public Result selectByCondition(String projectId, String type) {
    // 参数校验
    if (projectId == null || projectId.isEmpty()) {
        return new Result(Code.BAD_REQUEST, "项目ID不能为空");
    }

    try {
        // 构建查询条件
        LambdaQueryWrapper<FrontendError> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FrontendError::getProjectId, projectId)
                .orderByDesc(FrontendError::getTimestamp);

        // 添加错误类型条件
        if (type != null && !type.isEmpty()) {
            queryWrapper.eq(FrontendError::getErrorType, type);
        }

        // 执行查询
        List<FrontendError> frontendErrors = frontendErrorMapper.selectList(queryWrapper);

        // 查询责任人信息
        List<Responsibility> responsibilities = responsibilityMapper.selectList(
                new LambdaQueryWrapper<Responsibility>()
                        .eq(Responsibility::getProjectId, projectId)
                        .eq(Responsibility::getPlatform, "frontend")
        );

        // 构建责任人映射（按错误类型映射）
        final Map<String, Responsibility> responsibilityMap = responsibilities.stream()
                .collect(Collectors.toMap(Responsibility::getErrorType, r -> r, (r1, r2) -> r1));

        // 获取所有相关的用户信息（一次性查询）
        Set<Long> userIds = responsibilities.stream()
                .filter(r -> r.getResponsibleId() != null)
                .map(Responsibility::getResponsibleId)
                .collect(Collectors.toSet());

        final Map<Long, Users> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            List<Users> usersList = usersMapper.selectBatchIds(userIds);
            userMap.putAll(usersList.stream().collect(Collectors.toMap(Users::getId, u -> u)));
        }

        // 处理错误数据并关联责任人信息
        List<FrontendResponsibilityVO> frontendResponsibilityVOList = frontendErrors.stream()
                .map(frontendError -> {
                    FrontendResponsibilityVO vo = new FrontendResponsibilityVO();
                    BeanUtils.copyProperties(frontendError, vo);
                    vo.setId(frontendError.getId());

                    // 根据错误类型匹配责任人
                    Responsibility responsibility = responsibilityMap.get(frontendError.getErrorType());
                    if (responsibility != null &&
                            responsibility.getResponsibleId() != null &&
                            responsibility.getDelegatorId() != null) {

                        vo.setDelegatorId(responsibility.getDelegatorId());
                        vo.setResponsibleId(responsibility.getResponsibleId());
                        Users responsibleUser = userMap.get(responsibility.getResponsibleId());
                        if (responsibleUser != null) {
                            vo.setName(responsibleUser.getUsername());
                            vo.setAvatarUrl(responsibleUser.getAvatar());
                            vo.setResponsibleId(responsibility.getResponsibleId());
                        }
                    }
                    return vo;
                })
                .collect(Collectors.toList());

        return new Result(Code.SUCCESS,
                List.of(new ArrayList<>(), frontendResponsibilityVOList, new ArrayList<>()),
                "查询成功");

    } catch (Exception e) {
        log.error("查询前端责任人信息时发生异常: projectId={}, type={}", projectId, type, e);
        return new Result(Code.INTERNAL_ERROR, "查询失败: " + e.getMessage());
    }
}

}
