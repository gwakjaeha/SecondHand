package com.example.secondhand.global.config.jpa;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing
//스프링이 jpaAuditing 이 켜진 상태에서 시작하게 됨.
//테이블에 createdDate, LastModifiedDate 부분이 실행될때 자동으로 값을 저장하게 함.
public class JpaAuditingConfiguration {
}
