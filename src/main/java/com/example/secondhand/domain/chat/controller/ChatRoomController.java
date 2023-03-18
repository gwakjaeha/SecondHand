package com.example.secondhand.domain.chat.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/chat")
public class ChatRoomController {

	@GetMapping("/room")
	public String getRoom(Long chatRoomId, String nickname, Model model) {
		model.addAttribute("chatRoomId", chatRoomId);
		model.addAttribute("nickname", nickname);
		// 직접 채팅 기능을 실습해보기 위해 html 호출
		return "chat/room";
	}
}