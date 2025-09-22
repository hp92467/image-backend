package com.hp.imagebackend.model.dto.space;

import com.hp.imagebackend.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

// 查询空间时使用的请求体，继承了分页参数
@EqualsAndHashCode(callSuper = true)
@Data
public class SpaceQueryRequest extends PageRequest implements Serializable {
    private Long id;
    private Long userId;
    private String spaceName;
    private Integer spaceLevel;
    private Integer spaceType;
    private static final long serialVersionUID = 1L;
}