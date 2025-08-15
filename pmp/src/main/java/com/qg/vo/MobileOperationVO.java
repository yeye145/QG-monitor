package com.qg.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * @Description: // 类说明
 * @ClassName: MobileOperationVO    // 类名
 * @Author: lrt          // 创建者
 * @Date: 2025/8/13 14:42   // 时间
 * @Version: 1.0     // 版本
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MobileOperationVO {
    private LocalDateTime timestamp;
    private String operationId;
    private Long operationFps;
    private Map<String, Object> memoryUsage;
}
