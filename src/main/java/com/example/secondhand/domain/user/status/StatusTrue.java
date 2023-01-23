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
    READ_ACCOUNT_INFO_TRUE("READ_ACCOUNT_INFO_TRUE", "내 계정 정보 불러오기 성공"),
    LOGOUT_TRUE("LOGOUT_TRUE", "로그아웃 성공"),
    CHANGE_ACCOUNT_INFO_TRUE("CHANGE_ACCOUNT_INFO_TRUE", "내 회원정보 변경 성공"),
    PASSWORD_CHANGE_TRUE("PASSWORD_CHANGE_TRUE", "비밀번호 변경 성공"),
    TOKEN_REISSUE_TRUE("TOKEN_REISSUE_TRUE", "토큰 재발급 성공"),
    DELETE_ACCOUNT_TRUE("DELETE_ACCOUNT_TRUE", "회원탈퇴 성공"),
    READ_PRODUCT_INFO_TRUE("READ_PRODUCT_INFO_TRUE", "상품 정보 읽기 성공"),
    READ_MY_SELLING_PRODUCT_INFO_TRUE("READ_MY_SELLING_PRODUCT_INFO_TRUE","내가 올린 상품 정보 읽기 성공"),
    ADD_PRODUCT_INFO_TRUE("ADD_PRODUCT_INFO_TRUE", "상품 정보 추가 성공"),
    ADD_INTEREST_PRODUCT_INFO_TRUE("ADD_INTEREST_PRODUCT_INFO_TRUE", "관심 상품 추가 성공"),
    READ_INTEREST_PRODUCT_INFO_TRUE("READ_INTEREST_PRODUCT_INFO_TRUE", "관심 상품 읽기 성공"),
    DELETE_INTEREST_PRODUCT_INFO_TRUE("DELETE_INTEREST_PRODUCT_INFO_TRUE", "관심 상품 삭제 성공"),
    UPDATE_PRODUCT_INFO_TRUE("UPDATE_PRODUCT_INFO_TRUE", "상품 정보 수정 성공"),
    DELETE_PRODUCT_INFO_TRUE("DELETE_PRODUCT_INFO_TRUE", "상품 정보 삭제 성공"),
    SAVE_PRODUCT_DOCUMENT_TRUE("SAVE_PRODUCT_DOCUMENT_TRUE", "Elasticsearch 에 데이터 저장 성공"),
    READ_POPULAR_PRODUCT_INFO_TRUE("READ_POPULAR_PRODUCT_INFO_TRUE", "인기 상품 읽기 성공");

    private final String status;
    private final String statusMessage;
}
