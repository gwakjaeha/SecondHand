package com.example.secondhand.domain.user.dto;

import com.example.secondhand.domain.user.domain.Account;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountDto {

	private Long userId;
	private Long areaId;
	private String email;
	private String password;
	private String userName;
	private String phone;

	private LocalDateTime createDt;
	private LocalDateTime updateDt;
	private LocalDateTime deleteDt;

	public static AccountDto fromEntity(Account account){
		return AccountDto.builder()
				.userId(account.getUserId())
				.areaId(account.getAreaId())
				.email(account.getEmail())
				.password(account.getPassword())
				.userName(account.getUserName())
				.phone(account.getPhone())
				.createDt(LocalDateTime.now())
				.build();
	}

}
