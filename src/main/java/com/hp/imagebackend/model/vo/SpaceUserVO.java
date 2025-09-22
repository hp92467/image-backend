package com.hp.imagebackend.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

// /model/vo/SpaceUserVO.java
@Data
public class SpaceUserVO implements Serializable {
    private Long id;
    private Long spaceId;
    private Long userId;
    private String spaceRole;
    private Date createTime;
    private Date updateTime;
    private UserVO user; // 关联的用户信息
    private SpaceVO space; // 关联的空间信息
    private static final long serialVersionUID = 1L;
    
    // 静态转换方法
    public static SpaceUser voToObj(SpaceUserVO spaceUserVO) { /* ... */ }
    public static SpaceUserVO objToVo(SpaceUser spaceUser) { /* ... */ }
}