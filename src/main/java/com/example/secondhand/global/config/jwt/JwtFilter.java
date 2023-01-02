package com.example.secondhand.global.config.jwt;

import com.example.secondhand.domain.user.model.Model;
import com.example.secondhand.global.config.redis.RedisDao;
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

@Slf4j
public class JwtFilter extends GenericFilterBean {

    private final RedisDao redisDao;
    private TokenProvider tokenProvider;

    public JwtFilter(TokenProvider tokenProvider, RedisDao redisDao) {
        this.tokenProvider = tokenProvider;
        this.redisDao = redisDao;
    }

    //토큰의 인증정보를 SecurityContext에 저장하는 역할 수행
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        String jwt = resolveToken(httpServletRequest);

        log.info("----------------------------------");
        log.info(jwt);
        log.info("----------------------------------");

        String requestURI = httpServletRequest.getRequestURI();

        if (jwt != null && tokenProvider.validateToken(jwt)) {
            if (redisDao.getValues(jwt) == null) {
                Authentication authentication = tokenProvider.getAuthentication(jwt);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.info("Security Context에 '{}' 인증 정보를 저장했습니다, uri: {}", authentication.getName(), requestURI);
            }
        } else {
            log.info("유효한 JWT 토큰이 없습니다, uri {}", requestURI);
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }

    //Request Header에서 토큰 정보를 꺼내옴.
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(Model.AUTHORIZATION_HEADER);

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
