package com.qg.collections;

import java.util.List;

/**
 * @Description: // 类说明
 * @ClassName: EmbeddingService    // 类名
 * @Author: lrt          // 创建者
 * @Date: 2025/8/14 12:02   // 时间
 * @Version: 1.0     // 版本
 */
public interface EmbeddingService {
    List<Float> getEmbedding(String text);
}
