package com.hp.imagebackend.manager.websocket.disruptor;

import com.hp.imagebackend.manager.websocket.model.PictureEditRequestMessage;
import com.hp.imagebackend.model.entity.User;
import lombok.Data;
import org.springframework.web.socket.WebSocketSession;

@Data
public class PictureEditEvent {

    
    private PictureEditRequestMessage pictureEditRequestMessage;

    
    private WebSocketSession session;
    
    
    private User user;

    
    private Long pictureId;

}