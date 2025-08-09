package com.qg.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description: 移动错误类  // 类说明
 * @ClassName: MobileError    // 类名
 * @Author: lrt          // 创建者
 * @Date: 2025/8/7 21:18   // 时间
 * @Version: 1.0     // 版本
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MobileError {
    @TableId(value = "id",type = IdType.AUTO)
    private Long id;
    private String projectId;
    private Long timestamp;
    private String errorType;
    private String message;
    private String stack;
    private String className;
}
