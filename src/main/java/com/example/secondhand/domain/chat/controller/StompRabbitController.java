package com.example.secondhand.domain.chat.controller;

import com.example.secondhand.domain.chat.dto.ChatDto;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class StompRabbitController {

	private final RabbitTemplate template;

	private final static String CHAT_EXCHANGE_NAME = "chat.exchange";
	private final static String CHAT_QUEUE_NAME = "chat.queue";

	@MessageMapping("chat.enter.{chatRoomId}")
	public void enter(ChatDto chatDto, @DestinationVariable String chatRoomId) {
		chatDto.setMessage("입장하셨습니다.");
		chatDto.setRegDate(LocalDateTime.now());

		// exchange 로 chatDto 내용을 전달
		template.convertAndSend(CHAT_EXCHANGE_NAME, "room." + chatRoomId, chatDto);
	}


	@MessageMapping("chat.message.{chatRoomId}")
	public void send(ChatDto chatDto, @DestinationVariable String chatRoomId) {
		chatDto.setRegDate(LocalDateTime.now());

		// exchange 로 chatDto 내용을 전달
		template.convertAndSend(CHAT_EXCHANGE_NAME, "room." + chatRoomId, chatDto);
	}

	// receiver()는 단순히 큐에 들어온 메세지를 소비만 함
	@RabbitListener(queues = CHAT_QUEUE_NAME)
	public void receive(ChatDto chatDto) {
		log.info("chatDto.getMessage = {}",chatDto.getMessage());

	}
}