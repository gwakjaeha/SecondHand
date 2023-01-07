package com.example.secondhand.domain.user.dto;

import com.example.secondhand.domain.user.domain.Account;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TokenInfoResponseDto {
    private Long userId;
    private Long areaId;
    private String email;
    private String password;
    private String userName;
    private String phone;
    private String status;
    private String emailAuthKey;
    private boolean admin;
    private LocalDateTime createDt;
    private LocalDateTime updateDt;
    private LocalDateTime deleteDt;

    public static TokenInfoResponseDto Response(Account account) {
        return TokenInfoResponseDto.builder()
            .userId(account.getUserId())
            .areaId(account.getAreaId())
            .email(account.getEmail())
            .password(account.getPassword())
            .userName(account.getUserName())
            .phone(account.getPhone())
            .status(account.getStatus())
            .emailAuthKey(account.getEmailAuthKey())
            .admin(account.isAdmin())
            .createDt(account.getCreateDt())
            .updateDt(account.getUpdateDt())
            .deleteDt(account.getDeleteDt())
            .build();
    }
}
