package com.hp.imagebackend.model.dto.space;

import lombok.Data;

import java.io.Serializable;

// 管理员更新空间时使用的请求体（可修改更多字段）
@Data
public class SpaceUpdateRequest implements Serializable {
    private Long id;
    private String spaceName;
    private Integer spaceLevel;
    private Long maxSize;
    private Long maxCount;
    private static final long serialVersionUID = 1L;
}