package com.example.secondhand.domain.user.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum StatusTrue {
    REGISTER_STATUS_TRUE("REGISTER_STATUS_TRUE", "회원가입 성공"),
    SOCIAL_REGISTER_STATUS_TRUE("SOCIAL_REGISTER_STATUS_TRUE", "소셜 회원가입 성공"),
    SOCIAL_ADD_INFO_STAUTS_TRUE("SOCIAL_ADD_INFO_STAUTS_TRUE","추가 정보 요청 성공"),
    LOGIN_STATUS_TRUE("LOGIN_STATUS_TRUE", "로그인 성공"),
    READ_STATUS_TRUE("READ_STATUS_TRUE", "정보 불러오기 성공"),
    LOGOUT_STATUS_TRUE("LOGOUT_STATUS_TRUE", "로그아웃 성공"),
    UPDATE_STATUS_TURE("UPDATE_STATUS_TURE", "회원정보 업데이트 성공"),
    PHONE_CHECK_STATUS_TRUE("PHONE_CHECK_STATUS_TRUE", "휴대폰 인증번호 보내기 성공"),
    PASSWORD_CHANGE_STATUS_TRUE("PASSWORD_CHANGE_STATUS_TRUE", "비밀번호 변경 성공"),
    USER_DELETE_STATUS_TRUE("USER_DELETE_STATUS_TRUE", "회원탈퇴 성공");

    private final String status;
    private final String statusMessage;
}
