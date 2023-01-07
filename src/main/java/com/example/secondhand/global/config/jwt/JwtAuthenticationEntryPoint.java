package com.example.secondhand.global.config.jwt;

import static com.example.secondhand.global.exception.CustomErrorCode.JWT_TIMEOUT;

import com.example.secondhand.global.exception.CustomErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

//유효한 자격증명을 제공하지 않고 접근하려 할때 401 Unauthorized 에러를 리턴함.
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        //Content-type : application/json;charset=utf-8
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        response.setStatus(HttpStatus.UNAUTHORIZED.value());

        CustomErrorResponse error = new CustomErrorResponse();
        error.setStatus(JWT_TIMEOUT);
        error.setStatusMessage("만료된 JWT 토큰입니다.");

        String result = objectMapper.writeValueAsString(error);
        response.getWriter().write(result);
    }
}
