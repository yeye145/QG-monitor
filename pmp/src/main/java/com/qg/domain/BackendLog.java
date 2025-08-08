package com.qg.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description: 后端日志类  // 类说明
 * @ClassName: backendLog    // 类名
 * @Author: lrt          // 创建者
 * @Date: 2025/8/7 21:00   // 时间
 * @Version: 1.0     // 版本
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BackendLog {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long timestamp;
    private String level;
    private String context;
    private String module;
    private String source = "backend";
    private String projectId;
    private EnvironmentSnapshot environmentSnapshot;
    private String environment;
}
