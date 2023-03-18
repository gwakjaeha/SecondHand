package com.example.secondhand.domain.chat.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class StompConfig implements WebSocketMessageBrokerConfigurer {

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry.addEndpoint("/stomp/chat") // STOMP 프로토콜을 연결할 엔드포인트를 지정
			.setAllowedOriginPatterns("http://*.*.*.*:8080", "http://*:8080")
			.withSockJS();
	}

	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
		registry.setPathMatcher(new AntPathMatcher("."));  // url을 chat/room/1 -> chat.room.1으로 참조하기 위한 설정
		registry.setApplicationDestinationPrefixes("/pub");

		registry.enableStompBrokerRelay("/queue", "/topic", "/exchange", "/amq/queue");
	}
}
