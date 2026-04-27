package com.example.chatapp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");   // msgs go OUT
        config.setApplicationDestinationPrefixes("/app"); // msgs come IN
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {

        // 1️⃣ Raw websocket endpoint (works better in production)
        registry.addEndpoint("/chat")
                .setAllowedOriginPatterns("*");

        // 2️⃣ SockJS fallback ONLY for browsers that need it
        registry.addEndpoint("/chat")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
}
