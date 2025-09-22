package com.hp.imagebackend.model.dto.spaceuser;

import lombok.Data;

import java.io.Serializable;

@Data
public class SpaceUserEditRequest implements Serializable {
    private Long id; // space_user 表的主键 id
    private String spaceRole;
    private static final long serialVersionUID = 1L;
}