package com.qg.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.qg.handler.JsonbTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * @Description: 移动性能类  // 类说明
 * @ClassName: MobilePerformance    // 类名
 * @Author: lrt          // 创建者
 * @Date: 2025/8/7 21:20   // 时间
 * @Version: 1.0     // 版本
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MobilePerformance {
    @TableId(value = "id",type = IdType.AUTO)
    private Long id;
    private String projectId;
    private Long timestamp;
    private String deviceModel;
    private String osVersion;
    private String batteryLevel;
    @TableField(typeHandler = JsonbTypeHandler.class)
    private Map<String, Object> memoryUsage;
    private String operationFps;
}
