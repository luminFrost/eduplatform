package com.edu.eduplatform.announcement.service;

import com.edu.eduplatform.announcement.domain.SiteAnnouncement;
import com.edu.eduplatform.announcement.repository.SiteAnnouncementRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SiteAnnouncementService {

    private final SiteAnnouncementRepository siteAnnouncementRepository;

    public Optional<String> getCurrentMessage() {
        return siteAnnouncementRepository.findFirstByOrderByIdAsc().map(SiteAnnouncement::getMessage);
    }

    /** 항상 한 행만 유지한다 — 있으면 갱신, 없으면 새로 만든다(즐겨찾기 리뷰 upsert와 같은 패턴). */
    @Transactional
    public void save(String message) {
        siteAnnouncementRepository.findFirstByOrderByIdAsc()
                .ifPresentOrElse(
                        existing -> existing.updateMessage(message),
                        () -> siteAnnouncementRepository.save(SiteAnnouncement.builder().message(message).build()));
    }

    @Transactional
    public void clear() {
        siteAnnouncementRepository.findFirstByOrderByIdAsc().ifPresent(siteAnnouncementRepository::delete);
    }
}
