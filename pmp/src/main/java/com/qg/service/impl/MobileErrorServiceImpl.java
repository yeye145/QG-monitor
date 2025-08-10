package com.qg.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qg.domain.MobileError;
import com.qg.domain.Result;
import com.qg.mapper.MobileErrorMapper;
import com.qg.repository.MobileErrorRepository;
import com.qg.service.MobileErrorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.qg.domain.Code.BAD_REQUEST;

/**
 * @Description: 移动错误应用  // 类说明
 * @ClassName: MobileErrorServiceImpl    // 类名
 * @Author: lrt          // 创建者
 * @Date: 2025/8/7 21:36   // 时间
 * @Version: 1.0     // 版本
 */

@Slf4j
@Service
public class MobileErrorServiceImpl implements MobileErrorService {

    @Autowired
    private MobileErrorMapper mobileErrorMapper;
    @Autowired
    private MobileErrorRepository mobileErrorRepository;

    @Override
    public Result selectByCondition(String projectId, String type) {
        if (projectId == null || projectId.isEmpty()) {
            return new Result(BAD_REQUEST, "参数错误");
        }
        LambdaQueryWrapper<MobileError> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MobileError::getProjectId, projectId);

        if (type != null && !type.isEmpty()) {
            queryWrapper.eq(MobileError::getErrorType, type);
        }

        List<MobileError> mobileErrors = mobileErrorMapper.selectList(queryWrapper);

        return new Result(200, mobileErrors, "查询成功");
    }

    @Override
    public void receiveErrorFromSDK(String mobileErrorJSON) {
        try {
            mobileErrorRepository.statistics(JSONUtil.toBean(mobileErrorJSON, MobileError.class));
            log.info("mobile-error存入缓存成功");
        } catch (Exception e) {
            log.warn("mobile-error存入缓存失败,发生异常:{}", e.getMessage());
        }
    }
}
