package com.qg.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qg.domain.*;
import com.qg.domain.MobileError;
import com.qg.mapper.MobileErrorMapper;
import com.qg.mapper.ResponsibilityMapper;
import com.qg.mapper.UsersMapper;
import com.qg.service.MobileResponsibilityService;
import com.qg.vo.MobileResponsibilityVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description: // 类说明
 * @ClassName: MobileResponsibilityServiceImpl    // 类名
 * @Author: lrt          // 创建者
 * @Date: 2025/8/9 15:52   // 时间
 * @Version: 1.0     // 版本
 */
@Service
@Slf4j
public class MobileResponsibilityServiceImpl implements MobileResponsibilityService {

    @Autowired
    private MobileErrorMapper mobileErrorMapper;

    @Autowired
    private ResponsibilityMapper responsibilityMapper;

    @Autowired
    private UsersMapper usersMapper;

//    @Override
//    public Result selectByCondition(String projectId, String type) {
//        if (projectId == null || projectId.isEmpty()) {
//            return  new Result(BAD_REQUEST, "参数错误");
//        }
//        LambdaQueryWrapper<MobileError> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.eq(MobileError::getProjectId, projectId);
//
//        if (type != null && !type.isEmpty()) {
//            queryWrapper.eq(MobileError::getErrorType, type);
//        }
//
//        List<MobileError> mobileErrors = mobileErrorMapper.selectList(queryWrapper);
//
//        List<Responsibility> responsibilities = responsibilityMapper.selectList(new LambdaQueryWrapper<Responsibility>()
//                .eq(Responsibility::getProjectId, projectId)
//                .eq(Responsibility::getPlatform, "mobile"));
//
//        List<MobileResponsibilityVO>mobileResponsibilityVOList = new ArrayList<>();
//
//        for (MobileError error : mobileErrors) {
//            Long id = error.getId();
//            MobileResponsibilityVO mobileResponsibilityVO = new MobileResponsibilityVO();
//            mobileResponsibilityVO.setId(id);
//            mobileResponsibilityVO.setClassName(error.getClassName());
//            mobileResponsibilityVO.setErrorType(error.getErrorType());
//            mobileResponsibilityVO.setMessage(error.getMessage());
//            mobileResponsibilityVO.setProjectId(error.getProjectId());
//            mobileResponsibilityVO.setTimestamp(error.getTimestamp());
//            mobileResponsibilityVO.setStack(error.getStack());
//
//            for (Responsibility responsibility : responsibilities) {
//                if (responsibility.getErrorId().equals(id)) {
//                    Long responsibleId = responsibility.getResponsibleId();
//                    Long delegatorId = responsibility.getDelegatorId();
//
//                    if (responsibleId != null && delegatorId != null) {
//                        mobileResponsibilityVO.setDelegatorId(delegatorId);
//                        Users responsibleUser = usersMapper.selectById(responsibleId);
//                        if (responsibleUser != null) {
//                            mobileResponsibilityVO.setName(responsibleUser.getUsername());
//                            mobileResponsibilityVO.setAvatarUrl(responsibleUser.getAvatar());
//                        }
//
//                    }
//                }
//            }
//            mobileResponsibilityVOList.add(mobileResponsibilityVO);
//        }
//
//
//        return new Result(200, List.of(new ArrayList<>(),new ArrayList<>(),mobileResponsibilityVOList), "查询成功");
//    }

    @Override
    public Result selectByCondition(String projectId, String type) {
        // 参数校验
        if (projectId == null || projectId.isEmpty()) {
            return new Result(Code.BAD_REQUEST, "项目ID不能为空");
        }

        try {
            // 构建查询条件
            LambdaQueryWrapper<MobileError> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(MobileError::getProjectId, projectId)
                    .orderByDesc(MobileError::getTimestamp);

            // 添加错误类型条件
            if (type != null && !type.isEmpty()) {
                queryWrapper.like(MobileError::getErrorType, type);
            }

            // 执行查询
            List<MobileError> mobileErrors = mobileErrorMapper.selectList(queryWrapper);

            // 查询责任人信息
            List<Responsibility> responsibilities = responsibilityMapper.selectList(
                    new LambdaQueryWrapper<Responsibility>()
                            .eq(Responsibility::getProjectId, projectId)
                            .eq(Responsibility::getPlatform, "mobile")
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
            List<MobileResponsibilityVO> mobileResponsibilityVOList = mobileErrors.stream()
                    .map(error -> {
                        MobileResponsibilityVO vo = new MobileResponsibilityVO();
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




            return new Result(Code.SUCCESS,
                    List.of(new ArrayList<>(), new ArrayList<>(), mobileResponsibilityVOList),
                    "查询成功");

        } catch (Exception e) {
            log.error("查询移动端责任人信息时发生异常: projectId={}, type={}", projectId, type, e);
            return new Result(Code.INTERNAL_ERROR, "查询失败: " + e.getMessage());
        }
    }

}
