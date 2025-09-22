package com.hp.imagebackend.manager.websocket.model;

import com.hp.imagebackend.model.vo.UserVO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
//定义图片编辑响应消息，也就是后端要发送给前端的信息
public class PictureEditResponseMessage {

    
    private String type;

    
    private String message;

    
    private String editAction;

    
    private UserVO user;
}