package com.hp.imagebackend.manager.websocket;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.hp.imagebackend.manager.websocket.disruptor.PictureEditEventProducer;
import com.hp.imagebackend.manager.websocket.model.PictureEditActionEnum;
import com.hp.imagebackend.manager.websocket.model.PictureEditMessageTypeEnum;
import com.hp.imagebackend.manager.websocket.model.PictureEditRequestMessage;
import com.hp.imagebackend.manager.websocket.model.PictureEditResponseMessage;
import com.hp.imagebackend.model.entity.User;
import com.hp.imagebackend.service.UserService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PictureEditHandler extends TextWebSocketHandler {

    //每张图片的状态，key：图片id，value：正在编辑的用户id
    private final Map<Long, Long> pictureEditingUsers = new ConcurrentHashMap<>();

    //所有连接会话，key：图片id，value：会话集合
    private final Map<Long, Set<WebSocketSession>> pictureSessions = new ConcurrentHashMap<>();

    @Resource
    private UserService userService;


    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        User user = (User) session.getAttributes().get("user");
        Long pictureId = (Long) session.getAttributes().get("pictureId");
        pictureSessions.putIfAbsent(pictureId, ConcurrentHashMap.newKeySet());
        pictureSessions.get(pictureId).add(session);


        PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
        pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.INFO.getValue());
        String message = String.format("%s加入编辑", user.getUserName());
        pictureEditResponseMessage.setMessage(message);
        pictureEditResponseMessage.setUser(userService.getUserVO(user));

        broadcastToPicture(pictureId, pictureEditResponseMessage);
    }

    @Resource
    private PictureEditEventProducer pictureEditEventProducer;

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {

        PictureEditRequestMessage pictureEditRequestMessage = JSONUtil.toBean(message.getPayload(), PictureEditRequestMessage.class);

        Map<String, Object> attributes = session.getAttributes();
        User user = (User) attributes.get("user");
        Long pictureId = (Long) attributes.get("pictureId");

        pictureEditEventProducer.publishEvent(pictureEditRequestMessage, session, user, pictureId);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {


    }


    //广播消息 的方法。该方法会根据 pictureId，将响应消息发送给编辑该图片的所有会话广播消息 的方法。该方法会根据 pictureId，将响应消息发送给编辑该图片的所有会话
    private void broadcastToPicture(Long pictureId, PictureEditResponseMessage pictureEditResponseMessage, WebSocketSession excludeSession) throws Exception {
        Set<WebSocketSession> sessionSet = pictureSessions.get(pictureId);
        if (CollUtil.isNotEmpty(sessionSet)) {

            com.fasterxml.jackson.databind.ObjectMapper objectMapper = new ObjectMapper();

            //配置序列化解决精度问题,long转为string
            SimpleModule module = new SimpleModule();
            module.addSerializer(Long.class, ToStringSerializer.instance);
            module.addSerializer(Long.TYPE, ToStringSerializer.instance);
            objectMapper.registerModule(module);

            //序列化为JSON字符串
            String message = objectMapper.writeValueAsString(pictureEditResponseMessage);
            TextMessage textMessage = new TextMessage(message);
            for (WebSocketSession session : sessionSet) {

                //排除某些session不发送，比如自己session
                if (excludeSession != null && excludeSession.equals(session)) {
                    continue;
                }
                if (session.isOpen()) {
                    session.sendMessage(textMessage);
                }
            }
        }
    }

    private void broadcastToPicture(Long pictureId, PictureEditResponseMessage pictureEditResponseMessage) throws Exception {
        broadcastToPicture(pictureId, pictureEditResponseMessage, null);
    }

    //用户进入编辑状态，要设置当前用户为编辑用户，并且向其他客户端发送消息
    public void handleEnterEditMessage(PictureEditRequestMessage pictureEditRequestMessage, WebSocketSession session, User user, Long pictureId) throws Exception {

        if (!pictureEditingUsers.containsKey(pictureId)) {

            pictureEditingUsers.put(pictureId, user.getId());
            PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
            pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.ENTER_EDIT.getValue());
            String message = String.format("%s开始编辑图片", user.getUserName());
            pictureEditResponseMessage.setMessage(message);
            pictureEditResponseMessage.setUser(userService.getUserVO(user));
            broadcastToPicture(pictureId, pictureEditResponseMessage);
        }
    }

    //用户执行编辑操作时，将该操作同步给 除了当前用户之外 的其他客户端
    public void handleEditActionMessage(PictureEditRequestMessage pictureEditRequestMessage, WebSocketSession session, User user, Long pictureId) throws Exception {
        Long editingUserId = pictureEditingUsers.get(pictureId);
        String editAction = pictureEditRequestMessage.getEditAction();
        PictureEditActionEnum actionEnum = PictureEditActionEnum.getEnumByValue(editAction);
        if (actionEnum == null) {
            return;
        }

        if (editingUserId != null && editingUserId.equals(user.getId())) {
            PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
            pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.EDIT_ACTION.getValue());
            String message = String.format("%s执行%s", user.getUserName(), actionEnum.getText());
            pictureEditResponseMessage.setMessage(message);
            pictureEditResponseMessage.setEditAction(editAction);
            pictureEditResponseMessage.setUser(userService.getUserVO(user));

            broadcastToPicture(pictureId, pictureEditResponseMessage, session);
        }
    }

    //用户退出编辑操作时，移除当前用户的编辑状态，并且向其他客户端发送消息
    public void handleExitEditMessage(PictureEditRequestMessage pictureEditRequestMessage, WebSocketSession session, User user, Long pictureId) throws Exception {
        Long editingUserId = pictureEditingUsers.get(pictureId);
        if (editingUserId != null && editingUserId.equals(user.getId())) {

            pictureEditingUsers.remove(pictureId);

            PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
            pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.EXIT_EDIT.getValue());
            String message = String.format("%s退出编辑图片", user.getUserName());
            pictureEditResponseMessage.setMessage(message);
            pictureEditResponseMessage.setUser(userService.getUserVO(user));
            broadcastToPicture(pictureId, pictureEditResponseMessage);
        }
    }


}