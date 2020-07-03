package com.sanri.app.kafka;

import com.alibaba.fastjson.JSON;

import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;

public class ClientMessageDecode implements Decoder.Text<ClientMessage> {
    @Override
    public ClientMessage decode(String s) throws DecodeException {
        return JSON.parseObject(s,ClientMessage.class);
    }

    @Override
    public boolean willDecode(String s) {
        return false;
    }

    @Override
    public void init(EndpointConfig endpointConfig) {

    }

    @Override
    public void destroy() {

    }
}
