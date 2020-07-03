package com.sanri.app.kafka;

import com.alibaba.fastjson.JSON;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.sanri.app.classloader.ClassLoaderManager;
import com.sanri.app.serializer.CustomObjectInputStream;
import com.sanri.app.serializer.KryoSerializer;
import com.sanri.app.servlet.ZkServlet;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.Session;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Date;

public class NotifyConsumerRecordClient {
    private ClientMessage clientMessage;
    private Session session;
    ClassLoader extendClassloader;
    private Logger logger = LoggerFactory.getLogger(getClass());

    public NotifyConsumerRecordClient(ClientMessage clientMessage, Session session) {
        this.clientMessage = clientMessage;
        this.session = session;

        ClassLoaderManager classLoaderManager =  ClassLoaderManager.getInstance();
        String classloader = clientMessage.getClassloader();
        extendClassloader = ClassLoader.getSystemClassLoader();
        if(StringUtils.isNotBlank(classloader)){
            extendClassloader = classLoaderManager.get(classloader);
        }
    }

    /**
     * 给客户端发送消息
     * @param consumerRecord
     */
    public void sendMessage(ConsumerRecord<byte[],byte[]> consumerRecord){
        byte[] value = consumerRecord.value();
        String serializable = clientMessage.getSerializable();
        try {
            Object object = deSerializable(serializable, value, extendClassloader);
            Date date = new Date(consumerRecord.timestamp());
            String format = DateFormatUtils.ISO_DATETIME_FORMAT.format(date);
            ExtendConsumerRecord extendConsumerRecord = new ExtendConsumerRecord(object, consumerRecord.partition(), consumerRecord.offset(), format);
            session.getAsyncRemote().sendText(JSON.toJSONString(extendConsumerRecord));
        } catch (IOException  | ClassNotFoundException e) {
            logger.error("给客户端发送消息时出错 [{}]",e.getMessage(),e);
        }

    }

    public String getTopic() {
        return clientMessage.getTopic();
    }

    public ClientMessage getClientMessage() {
        return clientMessage;
    }

    public Session getSession() {
        return session;
    }

    /**
     * 将一个字节数组反序列化为对象
     * @param serializable
     * @param valueBytes
     * @param extendClassloader
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private Object deSerializable(String serializable, byte[] valueBytes, ClassLoader extendClassloader) throws IOException, ClassNotFoundException {
        Object object;
        switch (serializable) {
            case "jdk":
                CustomObjectInputStream objectInputStream = new CustomObjectInputStream(new ByteArrayInputStream(valueBytes), extendClassloader);
                object = objectInputStream.readObject();

                break;
            case "kryo":
                Kryo kryo = KryoSerializer.kryos.get();

                Input input = new Input(valueBytes);
                kryo.setClassLoader(extendClassloader);
                object = kryo.readClassAndObject(input);
                break;
            case "fastJson":
            case "string":
            case "hex":
                object = ZkServlet.zkSerializerMap.get(serializable).deserialize(valueBytes);
                break;
            default:
                object = null;
        }
        return object;
    }

    public String action(){
        return clientMessage.getAction();
    }
}
