package com.hp.imagebackend.model.vo;

import com.hp.imagebackend.model.entity.Space;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;

// /model/vo/SpaceVO.java
@Data
public class SpaceVO implements Serializable {
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
    
    // 关联的创建者用户信息
    private UserVO user;

    private static final long serialVersionUID = 1L;
    
    // VO 转实体类 的静态工具方法
    public static Space voToObj(SpaceVO spaceVO) {
        if (spaceVO == null) {
            return null;
        }
        Space space = new Space();
        BeanUtils.copyProperties(spaceVO, space);
        return space;
    }

    // 实体类 转 VO 的静态工具方法
    public static SpaceVO objToVo(Space space) {
        if (space == null) {
            return null;
        }
        SpaceVO spaceVO = new SpaceVO();
        BeanUtils.copyProperties(space, spaceVO);
        return spaceVO;
    }
}