package com.hp.imagebackend.model.vo;

import cn.hutool.json.JSONUtil;
import com.hp.imagebackend.model.entity.Picture;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class PictureVO implements Serializable {
  
      
    private Long id;  
  
      
    private String url;  
  
      
    private String name;  
  
      
    private String introduction;  
  
      
    private List<String> tags;
  
      
    private String category;  
  
      
    private Long picSize;  
  
      
    private Integer picWidth;  
  
      
    private Integer picHeight;  
  
      
    private Double picScale;  
  
      
    private String picFormat;  
  
      
    private Long userId;  
  
      
    private Date createTime;  
  
      
    private Date editTime;
  
      
    private Date updateTime;  
  
      
    private UserVO user;  
  
    private static final long serialVersionUID = 1L;

    private Long spaceId;

    private String picColor;
  
      
    public static Picture voToObj(PictureVO pictureVO) {  
        if (pictureVO == null) {  
            return null;  
        }  
        Picture picture = new Picture();
        BeanUtils.copyProperties(pictureVO, picture);
        
        picture.setTags(JSONUtil.toJsonStr(pictureVO.getTags()));
        return picture;  
    }  
  
      
    public static PictureVO objToVo(Picture picture) {  
        if (picture == null) {  
            return null;  
        }  
        PictureVO pictureVO = new PictureVO();  
        BeanUtils.copyProperties(picture, pictureVO);  
        
        pictureVO.setTags(JSONUtil.toList(picture.getTags(), String.class));  
        return pictureVO;  
    }  
}