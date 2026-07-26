package com.edu.eduplatform.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA Auditing(생성/수정 시각 자동 기록)을 활성화한다.
 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {
}
