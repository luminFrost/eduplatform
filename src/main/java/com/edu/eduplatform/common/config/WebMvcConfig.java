package com.edu.eduplatform.common.config;

import com.edu.eduplatform.common.web.CurrentMemberIdArgumentResolver;
import com.edu.eduplatform.common.web.CurrentMemberSession;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final CurrentMemberSession currentMemberSession;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new CurrentMemberIdArgumentResolver(currentMemberSession));
    }
}
