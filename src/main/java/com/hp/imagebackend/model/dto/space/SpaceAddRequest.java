package com.hp.imagebackend.model.dto.space;

import lombok.Data;

import java.io.Serializable;

// 用户创建空间时使用的请求体
@Data
public class SpaceAddRequest implements Serializable {
    private String spaceName;
    private Integer spaceLevel;
    private Integer spaceType;
    private static final long serialVersionUID = 1L;
}