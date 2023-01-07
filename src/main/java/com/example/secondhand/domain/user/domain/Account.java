package com.example.secondhand.domain.user.domain;

import com.example.secondhand.domain.user.status.AccountStatusCode;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Account implements AccountStatusCode {
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

	@CreatedDate
	private LocalDateTime createDt;
	@LastModifiedDate
	private LocalDateTime updateDt;
	private LocalDateTime deleteDt;
}
