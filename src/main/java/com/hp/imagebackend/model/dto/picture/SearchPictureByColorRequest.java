package com.hp.imagebackend.model.dto.picture;

import lombok.Data;

@Data
public class SearchPictureByColorRequest {
    private String picColor;
    private Long spaceId;
    private static final long serialVersionUID = 1L;
}