package com.qg.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * @Description: 后端错误Vo  // 类说明
 * @ClassName: BackendResponsibilityVO    // 类名
 * @Author: lrt          // 创建者
 * @Date: 2025/8/9 15:19   // 时间
 * @Version: 1.0     // 版本
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BackendResponsibilityVO {

    private LocalDateTime timestamp;
    private String module;
    private String projectId;
    private String environment;
    private String errorType;
    private String stack;
    private Map<String, Object> environmentSnapshot;

    private String Name;
    private Long delegatorId;
    private String avatarUrl;


}
