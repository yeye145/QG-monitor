package com.qg.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qg.domain.BackendError;
import com.qg.domain.MobileError;
import com.qg.domain.Result;
import com.qg.mapper.MobileErrorMapper;
import com.qg.repository.MobileErrorRepository;
import com.qg.service.MobileErrorService;
import com.qg.utils.MathUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        System.out.println("mobileErrors: " + mobileErrors);

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

    @Override
    public Object[] getMobileErrorStats(String projectId) {
        LambdaQueryWrapper<MobileError> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MobileError::getProjectId, projectId);

        List<MobileError> mobileErrors = mobileErrorMapper.selectList(queryWrapper);

        LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);
        queryWrapper.ge(MobileError::getTimestamp, oneWeekAgo);

        Map<String ,Double> transformDataVOList = new HashMap<>();
        Map<String, Double>uvBillDataVOList = new HashMap<>();

        Integer count = 0;

        for (MobileError mobileError : mobileErrors) {
            if (mobileError.getEvent() > 0 && mobileError.getErrorType() != null) {
                addToMap(mobileError, transformDataVOList);
                addToMap(mobileError, uvBillDataVOList);
                count += mobileError.getEvent();
            }

        }

        if (count == 0) {
            return new Object[0]; // 如果没有数据，直接返回空数组
        }

        Integer finalCount = count;

        uvBillDataVOList.entrySet().removeIf(entry -> entry.getValue() == 0);
        System.out.println("uvBillDataVOList: " + uvBillDataVOList);
        System.out.println("finalCount: " + finalCount);

        uvBillDataVOList.replaceAll((k, v) -> MathUtil.truncate(v / finalCount, 3));

        System.out.println("uvBillDataVOList after normalization: " + uvBillDataVOList);


        return new Object[]{transformDataVOList, uvBillDataVOList};
    }

    private static void addToMap(MobileError mobileError, Map<String, Double> transformDataVOList) {
        if (mobileError.getErrorType() == null || mobileError.getEvent() == 0) {
            return; // 如果错误类型或事件为空，则不处理
        }
        if (transformDataVOList.containsKey(mobileError.getErrorType()) ) {
            transformDataVOList.put(mobileError.getErrorType(), transformDataVOList.get(mobileError.getErrorType()) + mobileError.getEvent());
            System.out.println("更新错误类型: " + mobileError.getErrorType() + ", 新的事件数: " + transformDataVOList.get(mobileError.getErrorType()));
        } else {
            transformDataVOList.put(mobileError.getErrorType(), Double.valueOf(mobileError.getEvent()));
            System.out.println("添加新的错误类型: " + mobileError.getErrorType() + ", 事件数: " + mobileError.getEvent());
        }
    }

}
