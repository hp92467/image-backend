package com.hp.imagebackend.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

// /model/entity/Space.java
@TableName(value ="space")
@Data
public class Space implements Serializable {

    // 主键，使用雪花算法生成的ID
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String spaceName;
    private Integer spaceLevel;
    private Integer spaceType;
    private Long maxSize;
    private Long maxCount;
    private Long totalSize;
    private Long totalCount;
    private Long userId;
    private Date createTime;
    private Date editTime;
    private Date updateTime;

    // 逻辑删除字段
    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}