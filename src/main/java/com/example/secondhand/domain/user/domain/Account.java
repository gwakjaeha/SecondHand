package com.example.secondhand.domain.user.domain;

import com.example.secondhand.domain.user.type.AccountStatusCode;
import java.time.LocalDateTime;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
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
@Entity
public class Account implements AccountStatusCode {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	Long userId;

	Long areaId;
	String email;
	String password;
	String userName;
	String phone;
	String status;
	String emailAuthKey;
	boolean adminYn;

	LocalDateTime createDt;
	LocalDateTime updateDt;
	LocalDateTime deleteDt;
}
