package com.example.secondhand.domain.user.domain;

import com.example.secondhand.domain.user.status.AccountStatusCode;
import com.example.secondhand.global.domain.BaseEntity;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
public class Account extends BaseEntity implements AccountStatusCode {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long userId;
	private Long areaId;

	@Column(unique = true)
	private String email;
	private String password;
	private String userName;
	@Column(unique = true)
	private String phone;
	private String status;
	@Column(unique = true)
	private String emailAuthKey;
	private boolean admin;

	private LocalDateTime deleteDt;
}
