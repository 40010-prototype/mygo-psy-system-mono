package com.mygo.config;

import com.mygo.utils.Context;
import jakarta.websocket.HandshakeResponse;
import jakarta.websocket.server.HandshakeRequest;
import jakarta.websocket.server.ServerEndpointConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

import java.util.List;
import java.util.Map;

@Configuration
public class WebSocketConfig extends ServerEndpointConfig.Configurator {

    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }

    /**
     * 建立握手时，连接前的操作
     */
    @Override
    public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {
        // 这个userProperties 可以通过 session.getUserProperties()获取


        Context.saveId(Long.valueOf(request.getHeaders().get("Sec-WebSocket-Protocol").get(0)));
        //当Sec-WebSocket-Protocol请求头不为空时,需要返回给前端相同的响应
        response.getHeaders().put("Sec-WebSocket-Protocol", request.getHeaders().get("Sec-WebSocket-Protocol"));
        super.modifyHandshake(sec, request, response);

    }

    /**
     * 初始化端点对象,也就是被@ServerEndpoint所标注的对象
     */
    @Override
    public <T> T getEndpointInstance(Class<T> clazz) throws InstantiationException {
        return super.getEndpointInstance(clazz);
    }
}

