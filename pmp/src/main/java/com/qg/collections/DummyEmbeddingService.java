package com.qg.collections;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @Description: // 类说明
 * @ClassName: DummyEmbeddingService    // 类名
 * @Author: lrt          // 创建者
 * @Date: 2025/8/14 12:00   // 时间
 * @Version: 1.0     // 版本
 */


public class DummyEmbeddingService implements EmbeddingService {
    private static final int DIMENSION = 384;
    private final Random random = new Random();

    @Override
    public List<Float> getEmbedding(String text) {
        List<Float> vector = new ArrayList<>(DIMENSION);
        for (int i = 0; i < DIMENSION; i++) {
            vector.add(random.nextFloat());
        }
        return vector;
    }
}
