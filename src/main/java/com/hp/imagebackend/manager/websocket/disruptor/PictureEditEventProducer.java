package com.hp.imagebackend.manager.websocket.disruptor;

import com.hp.imagebackend.manager.websocket.model.PictureEditRequestMessage;
import com.hp.imagebackend.model.entity.User;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;

//定义事件生产者
@Component
@Slf4j
public class PictureEditEventProducer {

    @Resource
    Disruptor<PictureEditEvent> pictureEditEventDisruptor;

    public void publishEvent(PictureEditRequestMessage pictureEditRequestMessage, WebSocketSession session, User user, Long pictureId) {
        RingBuffer<PictureEditEvent> ringBuffer = pictureEditEventDisruptor.getRingBuffer();
        //ringBuffer环形缓冲区
        long next = ringBuffer.next();
        PictureEditEvent pictureEditEvent = ringBuffer.get(next);
        pictureEditEvent.setSession(session);
        pictureEditEvent.setPictureEditRequestMessage(pictureEditRequestMessage);
        pictureEditEvent.setUser(user);
        pictureEditEvent.setPictureId(pictureId);
        //发布事件
        ringBuffer.publish(next);
    }

    //优雅停机,任务执行完才关机
    @PreDestroy
    public void close() {
        pictureEditEventDisruptor.shutdown();
    }
}
