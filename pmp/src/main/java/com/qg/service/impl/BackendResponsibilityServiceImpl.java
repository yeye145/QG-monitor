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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description: // 类说明
 * @ClassName: BackendResponsibilityServiceImpl    // 类名
 * @Author: lrt          // 创建者
 * @Date: 2025/8/9 15:50   // 时间
 * @Version: 1.0     // 版本
 */
@Service
@Slf4j
public class BackendResponsibilityServiceImpl implements BackendResponsibilityService {


    @Autowired
    private BackendErrorMapper backendErrorMapper;

    @Autowired
    private ModuleMapper moduleMapper;

    @Autowired
    private ResponsibilityMapper responsibilityMapper;

    @Autowired
    private UsersMapper usersMapper;


//    @Override
//    public Result selectByCondition(String projectId, Long moduleId, String type) {
//        if (projectId == null) {
//            return new Result(400, "参数不能为空");
//        }
//
//        LambdaQueryWrapper<BackendError> queryWrapper = new LambdaQueryWrapper<>();
//        Module module = moduleMapper.selectById(moduleId);
//        if (module != null) {
//            String moduleName = module.getModuleName();
//            queryWrapper.eq(BackendError::getModule, moduleName);
//        }
//        if (type != null && !type.isEmpty()) {
//            queryWrapper.eq(BackendError::getErrorType, type);
//        }
//        queryWrapper.eq(BackendError::getProjectId, projectId);
//
//        List<BackendError> backendErrors = backendErrorMapper.selectList(queryWrapper);
//
//        List<Responsibility> responsibilityList = responsibilityMapper.selectList(new LambdaQueryWrapper<Responsibility>()
//                .eq(Responsibility::getProjectId, projectId)
//                .eq(Responsibility::getPlatform, "backend"));
//
//        List<BackendResponsibilityVO>backendResponsibilityVOList = new ArrayList<>();
//
//        for (BackendError error : backendErrors) {
//            Long id = error.getId();
//            BackendResponsibilityVO backendResponsibilityVO = new BackendResponsibilityVO();
//            backendResponsibilityVO.setEnvironment(error.getEnvironment());
//            backendResponsibilityVO.setId(id);
//            backendResponsibilityVO.setEnvironmentSnapshot(error.getEnvironmentSnapshot());
//            backendResponsibilityVO.setProjectId(error.getProjectId());
//            backendResponsibilityVO.setTimestamp(error.getTimestamp());
//            backendResponsibilityVO.setErrorType(error.getErrorType());
//            backendResponsibilityVO.setStack(error.getStack());
//            backendResponsibilityVO.setModule(error.getModule());
//
//            for (Responsibility responsibility : responsibilityList) {
//                if (responsibility.getErrorId().equals(id)) {
//                    Long responsibleId = responsibility.getResponsibleId();
//                    Long delegatorId = responsibility.getDelegatorId();
//
//                    if (responsibleId != null && delegatorId != null) {
//                        backendResponsibilityVO.setDelegatorId(delegatorId);
//                        Users responsibleUser = usersMapper.selectById(responsibleId);
//                        if (responsibleUser != null) {
//                            backendResponsibilityVO.setName(responsibleUser.getUsername());
//                            backendResponsibilityVO.setAvatarUrl(responsibleUser.getAvatar());
//                        }
//
//                    }
//                }
//            }
//            backendResponsibilityVOList.add(backendResponsibilityVO);
//        }
//
//        return new Result(200, List.of(backendResponsibilityVOList,new ArrayList<>(),new ArrayList<>()),"查询成功");
//
//    }

    @Override
    public Result selectByCondition(String projectId, String type) {
        // 参数校验
        if (projectId == null || projectId.isEmpty()) {
            return new Result(Code.BAD_REQUEST, "项目ID不能为空");
        }

        try {
            // 构建查询条件
            LambdaQueryWrapper<BackendError> queryWrapper = new LambdaQueryWrapper<>();


            // 添加错误类型条件
            if (type != null && !type.isEmpty()) {
                queryWrapper.like(BackendError::getErrorType, type);
            }

            // 添加项目ID条件
            queryWrapper.eq(BackendError::getProjectId, projectId)
                    .orderByDesc(BackendError::getTimestamp);

            // 执行查询
            List<BackendError> backendErrors = backendErrorMapper.selectList(queryWrapper);


            for (BackendError backendError : backendErrors) {
                System.out.println(backendError.getEnvironmentSnapshot());
            }

            // 查询责任人信息
            List<Responsibility> responsibilityList = responsibilityMapper.selectList(
                    new LambdaQueryWrapper<Responsibility>()
                            .eq(Responsibility::getProjectId, projectId)
                            .eq(Responsibility::getPlatform, "backend")
            );

            // 修复点：声明为 final
            final Map<String, Responsibility> responsibilityMap = responsibilityList.stream()
                    .collect(Collectors.toMap(Responsibility::getErrorType, r -> r, (r1, r2) -> r1));

            Set<Long> userIds = responsibilityList.stream()
                    .filter(r -> r.getResponsibleId() != null)
                    .map(Responsibility::getResponsibleId)
                    .collect(Collectors.toSet());

            final Map<Long, Users> userMap = new HashMap<>();
            if (!userIds.isEmpty()) {
                List<Users> usersList = usersMapper.selectBatchIds(userIds);
                userMap.putAll(usersList.stream().collect(Collectors.toMap(Users::getId, u -> u)));
            }

            // 处理错误数据并关联责任人信息
            List<BackendResponsibilityVO> backendResponsibilityVOList = backendErrors.stream()
                    .map(error -> {
                        BackendResponsibilityVO vo = new BackendResponsibilityVO();
                        BeanUtils.copyProperties(error, vo);
                        vo.setId(error.getId());

                        // 根据错误类型匹配责任人
                        Responsibility responsibility = responsibilityMap.get(error.getErrorType());
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

            for (BackendResponsibilityVO backendResponsibilityVO : backendResponsibilityVOList) {
                System.out.println(backendResponsibilityVO.getEnvironmentSnapshot());
            }

            return new Result(Code.SUCCESS,
                    List.of(backendResponsibilityVOList, new ArrayList<>(), new ArrayList<>()),
                    "查询成功");

        } catch (Exception e) {
            log.error("查询后端责任人信息时发生异常: projectId={},  type={}", projectId, type, e);
            return new Result(Code.INTERNAL_ERROR, "查询失败: " + e.getMessage());
        }
    }

}
