package com.hp.imagebackend.utils;

import java.awt.*;

// /utils/ColorSimilarUtils.java
public class ColorSimilarUtils {
    /**
     * 计算两种颜色之间的相似度（基于欧几里得距离）
     * @param color1 颜色1
     * @param color2 颜色2
     * @return 相似度 (0-1, 1表示完全相同)
     */
    public static double calculateSimilarity(Color color1, Color color2) {
        int r1 = color1.getRed(), g1 = color1.getGreen(), b1 = color1.getBlue();
        int r2 = color2.getRed(), g2 = color2.getGreen(), b2 = color2.getBlue();
        // 计算RGB三维空间中的欧几里得距离
        double distance = Math.sqrt(Math.pow(r1 - r2, 2) + Math.pow(g1 - g2, 2) + Math.pow(b1 - b2, 2));
        // 将距离归一化到0-1范围，并反转，得到相似度
        return 1 - distance / Math.sqrt(3 * Math.pow(255, 2));
    }
    // ... 重载方法支持十六进制字符串 ...
}