package com.example.secondhand.domain.user.dto;

import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class ReadAccountDto {

	@NotNull
	private Long areaId;

	@NotNull
	private String email;

	@NotNull
	private String userName;

	@NotNull
	private String phone;


}
