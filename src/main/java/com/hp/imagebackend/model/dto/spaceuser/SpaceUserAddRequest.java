package com.hp.imagebackend.model.dto.spaceuser;

import lombok.Data;

import java.io.Serializable;

@Data
public class SpaceUserAddRequest implements Serializable {
    private Long spaceId;
    private Long userId;
    private String spaceRole;
    private static final long serialVersionUID = 1L;
}