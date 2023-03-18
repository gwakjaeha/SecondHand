package com.example.secondhand.domain.chat.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ChatDto {
	private Long id;
	private Long chatRoomId;
	private String nickname;
	private String message;
	private String region;

	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	private LocalDateTime regDate;
}
