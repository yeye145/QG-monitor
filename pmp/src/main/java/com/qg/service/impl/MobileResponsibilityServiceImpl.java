package com.qg.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qg.domain.MobileError;
import com.qg.domain.Responsibility;
import com.qg.domain.Result;
import com.qg.domain.Users;
import com.qg.mapper.MobileErrorMapper;
import com.qg.mapper.ResponsibilityMapper;
import com.qg.mapper.UsersMapper;
import com.qg.service.MobileResponsibilityService;
import com.qg.vo.MobileResponsibilityVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.qg.domain.Code.BAD_REQUEST;

/**
 * @Description: // 类说明
 * @ClassName: MobileResponsibilityServiceImpl    // 类名
 * @Author: lrt          // 创建者
 * @Date: 2025/8/9 15:52   // 时间
 * @Version: 1.0     // 版本
 */
@Service
public class MobileResponsibilityServiceImpl implements MobileResponsibilityService {

    @Autowired
    private MobileErrorMapper mobileErrorMapper;

    @Autowired
    private ResponsibilityMapper responsibilityMapper;

    @Autowired
    private UsersMapper usersMapper;

    @Override
    public Result selectByCondition(String projectId, String type) {
        if (projectId == null || projectId.isEmpty()) {
            return  new Result(BAD_REQUEST, "参数错误");
        }
        LambdaQueryWrapper<MobileError> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MobileError::getProjectId, projectId);

        if (type != null && !type.isEmpty()) {
            queryWrapper.eq(MobileError::getErrorType, type);
        }

        List<MobileError> mobileErrors = mobileErrorMapper.selectList(queryWrapper);

        List<Responsibility> responsibilities = responsibilityMapper.selectList(new LambdaQueryWrapper<Responsibility>()
                .eq(Responsibility::getProjectId, projectId)
                .eq(Responsibility::getPlatform, "mobile"));

        List<MobileResponsibilityVO>mobileResponsibilityVOList = new ArrayList<>();

        for (MobileError error : mobileErrors) {
            Long id = error.getId();
            MobileResponsibilityVO mobileResponsibilityVO = new MobileResponsibilityVO();
            mobileResponsibilityVO.setClassName(error.getClassName());
            mobileResponsibilityVO.setErrorType(error.getErrorType());
            mobileResponsibilityVO.setMessage(error.getMessage());
            mobileResponsibilityVO.setProjectId(error.getProjectId());
            mobileResponsibilityVO.setTimestamp(error.getTimestamp());
            mobileResponsibilityVO.setStack(error.getStack());

            for (Responsibility responsibility : responsibilities) {
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


        return new Result(200, mobileResponsibilityVOList, "查询成功");
    }
}
