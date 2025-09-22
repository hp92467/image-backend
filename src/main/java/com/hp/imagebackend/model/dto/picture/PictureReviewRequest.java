package com.hp.imagebackend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;
//图片审核请求
@Data
public class PictureReviewRequest implements Serializable {
  
      
    private Long id;  
  
      
    private Integer reviewStatus;  
  
      
    private String reviewMessage;  
  
  
    private static final long serialVersionUID = 1L;  
}
