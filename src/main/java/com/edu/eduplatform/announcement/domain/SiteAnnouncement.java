package com.edu.eduplatform.announcement.domain;

import com.edu.eduplatform.common.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사이트 전체 헤더에 노출되는 단일 공지. 항상 최대 한 행만 존재하도록
 * {@link com.edu.eduplatform.announcement.service.SiteAnnouncementService}가 관리한다.
 */
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SiteAnnouncement extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String message;

    @Builder
    public SiteAnnouncement(String message) {
        this.message = message;
    }

    public void updateMessage(String message) {
        this.message = message;
    }
}
