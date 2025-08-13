package com.qg.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description: // 类说明
 * @ClassName: ButtonVO    // 类名
 * @Author: lrt          // 创建者
 * @Date: 2025/8/12 20:11   // 时间
 * @Version: 1.0     // 版本
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ButtonVO {
    private String buttonId;
    private Integer eventCount;
}