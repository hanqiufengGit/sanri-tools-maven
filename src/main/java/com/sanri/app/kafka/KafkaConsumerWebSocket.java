package com.sanri.app.kafka;

import com.alibaba.fastjson.JSONObject;
import com.sanri.app.chat.ChatMessage;
import com.sanri.app.chat.ChatUserInfo;
import com.sanri.app.servlet.KafkaServlet;
import com.sanri.frame.DispatchServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.Date;

/**
 * 用于 kafka 客户端的主题消费数据推送
 */
@ServerEndpoint(value = "/consumer/{clientName}",decoders = ClientMessageDecode.class)
public class KafkaConsumerWebSocket {
    private KafkaServlet kafkaServlet = DispatchServlet.getServlet(KafkaServlet.class);
    private Logger log = LoggerFactory.getLogger(getClass());

    @OnOpen
    public void onOpen(Session session,@PathParam("clientName") String clientName) {
        log.info(clientName+" 连接建立成功");
        session.getAsyncRemote().sendText( " 连接已经建立,可以监听消息了");
    }

    @OnClose
    public void onClose(Session session){
        kafkaServlet.kafkaService.removeClient(session);
    }

    @OnError
    public void onError(Session session, Throwable error) {
        kafkaServlet.kafkaService.removeClient(session);
        error.printStackTrace();
    }

    @OnMessage
    public void onMessage(ClientMessage clientMessage, Session session) {
        NotifyConsumerRecordClient notifyConsumerRecordClient = new NotifyConsumerRecordClient(clientMessage, session);
        String action = notifyConsumerRecordClient.action();
        if("play".equals(action)) {
            kafkaServlet.kafkaService.startListener(notifyConsumerRecordClient);
        }else if("pause".equals(action)){
            kafkaServlet.kafkaService.removeClient(notifyConsumerRecordClient.getSession());
        }
    }
}
