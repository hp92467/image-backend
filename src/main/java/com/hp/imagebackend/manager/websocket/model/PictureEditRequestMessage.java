package com.hp.imagebackend.manager.websocket.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
//定义图片编辑请求消息，也就是前端要发送给后端的参数
public class PictureEditRequestMessage {

    
    private String type;//消息类型

    
    private String editAction;//动作
}