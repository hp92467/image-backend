package com.hp.imagebackend.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

// /model/enums/SpaceLevelEnum.java
@Getter
public enum SpaceLevelEnum {
    // 定义不同级别的空间：名称、枚举值、最大数量、最大容量
    COMMON("普通版", 0, 100, 100L * 1024 * 1024),
    PROFESSIONAL("专业版", 1, 1000, 1000L * 1024 * 1024),
    FLAGSHIP("旗舰版", 2, 10000, 10000L * 1024 * 1024);

    private final String text;
    private final int value;
    private final long maxCount;
    private final long maxSize;
    
    SpaceLevelEnum(String text, int value, long maxCount, long maxSize) {
        this.text = text;
        this.value = value;
        this.maxCount = maxCount;
        this.maxSize = maxSize;
    }

    // 根据整型值获取对应的枚举实例
    public static SpaceLevelEnum getEnumByValue(Integer value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (SpaceLevelEnum spaceLevelEnum : SpaceLevelEnum.values()) {
            if (spaceLevelEnum.value == value) {
                return spaceLevelEnum;
            }
        }
        return null;
    }
}