package com.hp.imagebackend.model.dto.space;

import lombok.Data;

import java.io.Serializable;

// 用户编辑自己空间时使用的请求体（只能修改名称）
@Data
public class SpaceEditRequest implements Serializable {
    private Long id;
    private String spaceName;
    private static final long serialVersionUID = 1L;
}