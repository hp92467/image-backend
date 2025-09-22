package com.hp.imagebackend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

// /model/dto/picture/PictureEditByBatchRequest.java
@Data
public class PictureEditByBatchRequest implements Serializable {
    private List<Long> pictureIdList;
    private Long spaceId;
    private String category;
    private List<String> tags;


    /**
     * 重命名规则，例如 "风景_{序号}"
     */



    private String nameRule;
    private static final long serialVersionUID = 1L;
}