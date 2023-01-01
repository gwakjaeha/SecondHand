package com.example.secondhand.domain.user.type;

public interface AccountStatusCode {
	//현재 이용중인 상태
	String ACCOUNT_STATUS_ING = "ING";
	//현재 정지된 상태
	String ACCOUNT_STATUS_STOP = "STOP";
	//현재 탈퇴된 회원
	String ACCOUNT_STATUS_WITHDRAW = "WITHDRAW";

}
