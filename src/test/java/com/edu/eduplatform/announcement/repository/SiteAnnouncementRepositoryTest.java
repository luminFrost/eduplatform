package com.edu.eduplatform.announcement.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.edu.eduplatform.announcement.domain.SiteAnnouncement;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

@DataJpaTest
class SiteAnnouncementRepositoryTest {

    @Autowired
    private SiteAnnouncementRepository siteAnnouncementRepository;

    @Test
    void findFirstByOrderByIdAsc_저장한_공지를_반환한다() {
        siteAnnouncementRepository.save(SiteAnnouncement.builder().message("점검 안내").build());

        assertThat(siteAnnouncementRepository.findFirstByOrderByIdAsc())
                .isPresent()
                .get()
                .extracting(SiteAnnouncement::getMessage)
                .isEqualTo("점검 안내");
    }

    @Test
    void findFirstByOrderByIdAsc_없으면_빈_값을_반환한다() {
        assertThat(siteAnnouncementRepository.findFirstByOrderByIdAsc()).isEmpty();
    }
}
