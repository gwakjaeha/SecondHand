package com.example.secondhand.domain.user.dto;

import com.example.secondhand.domain.user.domain.User;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

    public static TokenInfoResponseDto Response(User user) {
        return TokenInfoResponseDto.builder()
            .userId(user.getId())
            .areaId(user.getArea().getId())
            .email(user.getEmail())
            .password(user.getPassword())
            .userName(user.getUserName())
            .phone(user.getPhone())
            .status(user.getStatus())
            .emailAuthKey(user.getEmailAuthKey())
            .admin(user.isAdmin())
            .createDt(user.getCreatedAt())
            .updateDt(user.getUpdatedAt())
            .deleteDt(user.getDeleteAt())
            .build();
    }
}
