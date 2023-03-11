package com.example.secondhand.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor // enum에 하나의 프로퍼티가 있는데 프로퍼티를 사용해서 enum을 하나 생성해줄때 메세지를 넣어서 생성해주는 생성자가 없어서 선언해줘야함
@Getter
public enum CustomErrorCode {

    //로그인 & 로그아웃 검증
    JWT_CREDENTIALS_FALSE("로그인이 유효하지 않습니다."),
    JWT_TIMEOUT( "만료된 JWT 토큰입니다."),
    LOGIN_FALSE_NOT_EXIST_EMAIL("아이디 또는 비밀번호를 잘못 입력하였습니다."),
    LOGIN_FALSE_NOT_CORRECT_PASSWORD("아이디 또는 비밀번호를 잘못 입력하였습니다."),
    REFRESH_TOKEN_IS_BAD_REQUEST("잘못된 RefreshToken 입니다."),
    REQ_EMAIL("비활성화된 계정입니다. 이메일 인증을 완료해주세요."),
    STOP_EMAIL("정지된 계정입니다."),
    WITHDRAW_EMAIL("탈퇴된 계정입니다."),

    //회원가입
    REGISTER_INFO_NULL("필수 항목을 입력하지 않았습니다."),
    PASSWORD_SIZE_ERROR("비밀번호가 6자리 이상이여야 합니다."),
    NOT_EMAIL_FORM("이메일 형식이 아닙니다."),
    DUPLICATE_ACCOUNT("해당 이메일의 가입자가 이미 존재합니다."),
    SEND_EMAIL_FAIL("이메일 전송에 실패했습니다."),
    NOT_EXIST_UUID("유효하지 않은 접근입니다. 이메일 인증을 다시해주세요."),

    //정보변경
    UPDATE_INFO_NULL("필수 항목을 입력하지 않았습니다."),
    PASSWORD_IS_NOT_CHANGE("현재 사용중인 비밀번호로는 변경이 불가합니다."),
    PASSWORD_CHANGE_FALSE( "현재 비밀번호가 일치하지 않습니다." ),
    NOT_FOUND_USER("해당 이메일의 유저가 존재하지 않습니다."),
    NOT_FOUND_AREA("해당 지역이 존재하지 않습니다."),
    NOT_FOUND_CATEGORY("해당 카테고리가 존재하지 않습니다."),
    USER_NOT_MATCH("작성자만 내용을 수정하거나 삭제할 수 있습니다."),

    //회원탈퇴
    DELETE_ACCOUNT_FALSE("비밀번호 불일치로 회원탈퇴를 할 수 없습니다."),

    //물품
    READ_PRODUCT_INFO_NULL("필수 항목을 입력하지 않았습니다."),
    NOT_EXIST_PRODUCT("해당하는 물품이 없습니다."),
    ADD_PRODUCT_INFO_NULL("필수 항목을 입력하지 않았습니다."),
    UPDATE_PRODUCT_INFO_NULL("필수 항목을 입력하지 않았습니다."),
    DELETE_PRODUCT_INFO_NULL("물품 정보가 확인되지 않습니다."),

    //카테고리
    NOT_EXIST_CATEGORY("해당하는 카테고리가 없습니다."),

    //이미지 파일 저장
    SAVE_IMAGE_FILE_FALSE("이미지 파일 저장에 실패하였습니다."),

    //알수 없는 오류의 처리
    INTERNAL_SERVER_ERROR("서버에 오류가 발생했습니다."),
    INVALID_REQUEST("잘못된 요청입니다.");

    private final String statusMessage;
}
