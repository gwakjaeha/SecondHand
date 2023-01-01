package com.example.secondhand.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor // enum에 하나의 프로퍼티가 있는데 프로퍼티를 사용해서 enum을 하나 생성해줄때 메세지를 넣어서 생성해주는 생성자가 없어서 선언해줘야함
@Getter
public enum CustomErrorCode {

    //로그인 & 로그아웃 검증
    JWT_CREDENTIALS_STATUS_FALSE("로그인이 유효하지 않습니다."),
    JWT_TIMEOUT( "만료된 JWT 토큰입니다."),
    LOGIN_FALSE("아이디 또는 비밀번호를 잘못 입력하였습니다."),
    REFRESH_TOKEN_IS_BAD_REQUEST("잘못된 RefreshToken 입니다"),
    STOP_EMAIL("정지된 계정입니다."),
    WITHDRAW_EMAIL("탈퇴된 계정입니다."),

    //회원가입
    REGISTER_INFO_NULL("필수 항목을 입력하지 않았습니다."),
    PASSWORD_SIZE_ERROR("비밀번호가 6자리 이상이여야 합니다."),
    NOT_EMAIL_FORM("이메일 형식이 아닙니다."),
    NOT_CONTAINS_EXCLAMATIONMARK("비밀번호에 특수문자가 포함되어있지 않습니다."),
    DUPLICATE_USER("해당 이메일의 가입자가 이미 존재합니다."),

    // 업데이트
    UPDATE_INFO_NULL("필수 항목을 입력하지 않았습니다."),

    // 비밀번호 변경
    PASSWORD_IS_NOT_CHANGE("현재 사용하고있는 비밀번호로는 변경이 불가합니다."),
    PASSWORD_CHANGE_STATUS_FALSE( "현재 비밀번호가 일치하지 않습니다." ),
    NOT_FOUND_USER("해당 이메일의 유저가 존재하지 않습니다"),

    // 알수 없는 오류의 처리
    INTERNAL_SERVER_ERROR("서버에 오류가 발생했습니다."),
    INVALID_REQUEST("잘못된 요청입니다.");

    private final String statusMessage;
}
