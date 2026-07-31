package com.edu.eduplatform.lesson.service;

import jakarta.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

/**
 * 레슨 콘텐츠에 쓰인 이모지를 큰 OpenMoji SVG 아이콘 경로로 바꿔주는 카탈로그.
 * static/images/openmoji/ 아래 실제로 내려받아 둔 파일과 openmoji-map.tsv 매핑만 신뢰한다
 * (파일명은 유니코드 코드포인트로 런타임 계산이 가능하지만, OpenMoji가 변형 선택자를
 * 포함/생략하는 규칙이 문자마다 달라 실제로 받아둔 매핑을 그대로 쓰는 편이 안전하다).
 */
@Component
public class IconCatalog {

    private final Map<String, String> iconToImagePath = new HashMap<>();

    @PostConstruct
    void load() throws IOException {
        ClassPathResource resource = new ClassPathResource("openmoji-map.tsv");
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                String[] parts = line.split("\t", 2);
                iconToImagePath.put(parts[0], "/images/openmoji/" + parts[1] + ".svg");
            }
        }
    }

    public String resolveImagePath(String icon) {
        if (icon == null) {
            return null;
        }
        return iconToImagePath.get(icon);
    }
}
