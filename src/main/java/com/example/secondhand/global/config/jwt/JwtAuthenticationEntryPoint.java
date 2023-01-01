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

        // {"username":"loop-study", "age":20}
        String result = objectMapper.writeValueAsString(error);
        response.getWriter().write(result);
    }
}
