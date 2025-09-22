package com.hp.imagebackend.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@TableName(value = "picture")
@Data
public class Picture implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;


    private String url;

    private String thumbnailUrl;


    private String name;


    private String introduction;


    private String category;


    private String tags;


    private Long picSize;


    private Integer picWidth;


    private Integer picHeight;


    private Double picScale;


    private String picFormat;


    private Long userId;


    private Date createTime;


    private Date editTime;


    private Date updateTime;


    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    //新增可见性控制
    private Integer reviewStatus;


    private String reviewMessage;


    private Long reviewerId;


    private Date reviewTime;

    private Long spaceId;

    private String picColor;
}