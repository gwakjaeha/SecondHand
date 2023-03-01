package com.example.secondhand.global.status;

public interface UserStatusCode {
	//이메일 인증 요청중인 상태
	String USER_STATUS_REQ = "REQ";
	//현재 이용중인 상태
	String USER_STATUS_ING = "ING";
	//현재 정지된 상태
	String USER_STATUS_STOP = "STOP";
	//현재 탈퇴된 회원
	String USER_STATUS_WITHDRAW = "WITHDRAW";

}
