package com.qg.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * @Description: 数学工具类  // 类说明
 * @ClassName: MathUtil    // 类名
 * @Author: lrt          // 创建者
 * @Date: 2025/8/12 11:45   // 时间
 * @Version: 1.0     // 版本
 */
public class MathUtil {


    /**
     * 截断 double 到指定小数位数（不四舍五入）
     * @param value 要截断的值
     * @param decimalPlaces 保留的小数位数
     * @return 截断后的值
     */
    public static double truncate(double value, int decimalPlaces) {
        if (decimalPlaces < 0) {
            throw new IllegalArgumentException("小数位数不能为负数");
        }

        double multiplier = Math.pow(10, decimalPlaces);
        return Math.floor(value * multiplier) / multiplier;
    }

    /**
     * 使用 BigDecimal 精确截断
     * @param value 要截断的值
     * @param decimalPlaces 保留的小数位数
     * @return 截断后的值
     */
    public static double truncateWithBigDecimal(double value, int decimalPlaces) {
        if (decimalPlaces < 0) {
            throw new IllegalArgumentException("小数位数不能为负数");
        }

        return new BigDecimal(String.valueOf(value))
                .setScale(decimalPlaces, RoundingMode.DOWN)
                .doubleValue();
    }
}
