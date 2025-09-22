package com.hp.imagebackend.model.dto.picture;

import lombok.Data;

@Data
public class PictureUploadByBatchRequest {  
  
      
    private String searchText;  
  
      
    private Integer count = 10;

    private String nameRule;
}