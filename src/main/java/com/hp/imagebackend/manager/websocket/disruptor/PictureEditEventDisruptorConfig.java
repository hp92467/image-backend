package com.hp.imagebackend.manager.websocket.disruptor;

import cn.hutool.core.thread.ThreadFactoryBuilder;
import com.lmax.disruptor.dsl.Disruptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

//配置类，定义一些disruptor参数

@Configuration
public class PictureEditEventDisruptorConfig {

    @Resource
    private PictureEditEventWorkHandler pictureEditEventWorkHandler;

    @Bean("pictureEditEventDisruptor")
    public Disruptor<PictureEditEvent> messageModelRingBuffer() {
        
        int bufferSize = 1024 * 256;//环形buffer大小
        //创建Disruptor
        Disruptor<PictureEditEvent> disruptor = new Disruptor<>(
                PictureEditEvent::new,
                bufferSize,
                ThreadFactoryBuilder.create().setNamePrefix("pictureEditEventDisruptor").build()
        );
        //设置消费者
        disruptor.handleEventsWithWorkerPool(pictureEditEventWorkHandler);
        
        disruptor.start();
        return disruptor;
    }
}
