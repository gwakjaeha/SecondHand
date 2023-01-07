package com.example.secondhand.domain.user.status;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum StatusTrue {
    SEND_EMAIL_TRUE("SEND_EMAIL_TRUE", "이메일 전송 성공"),
    CERTIFICATION_EMAIL_TRUE("CERTIFICATION_EMAIL_TRUE", "이메일 인증 성공"),
    REGISTER_TRUE("REGISTER_TRUE", "회원가입 성공"),
    LOGIN_TRUE("LOGIN_TRUE", "로그인 성공"),
    READ_INFO_TRUE("READ_INFO_TRUE", "내 정보 불러오기 성공"),
    LOGOUT_TRUE("LOGOUT_TRUE", "로그아웃 성공"),
    CHANGE_INFO_TRUE("CHANGE_INFO_TRUE", "회원정보 변경 성공"),
    PASSWORD_CHANGE_TRUE("PASSWORD_CHANGE_TRUE", "비밀번호 변경 성공"),
    TOKEN_REISSUE_TRUE("TOKEN_REISSUE_TRUE", "토큰 재발급 성공"),
    DELETE_ACCOUNT_TRUE("DELETE_ACCOUNT_TRUE", "회원탈퇴 성공");

    private final String status;
    private final String statusMessage;
}
