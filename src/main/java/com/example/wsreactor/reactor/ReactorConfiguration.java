package com.example.wsreactor.reactor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class ReactorConfiguration {

    public final static String SOCKET_ROOT = "/wsapi/topics";
    public final static String SOCKET_ADMIN_SERVICE = SOCKET_ROOT + "/admin";
    public final static String SOCKET_GAMER_SERVICE = SOCKET_ROOT + "/gamer";

    @Autowired
    private SinkSocketHandler sinkSocketHandler;

    @Autowired
    private SimpleSocketHandler simpleSocketHandler;

    @Bean
    public HandlerMapping socketsMapping() {
        Map<String, Object> map = new HashMap<>();
        map.put(SOCKET_ADMIN_SERVICE, sinkSocketHandler);
        map.put(SOCKET_GAMER_SERVICE, simpleSocketHandler);
        SimpleUrlHandlerMapping simpleUrlHandlerMapping = new SimpleUrlHandlerMapping();
        simpleUrlHandlerMapping.setUrlMap(map);

        simpleUrlHandlerMapping.setOrder(10);
        return simpleUrlHandlerMapping;
    }

    @Bean
    public WebSocketHandlerAdapter socketsHandlerAdapter() {
        return new WebSocketHandlerAdapter();
    }
}
