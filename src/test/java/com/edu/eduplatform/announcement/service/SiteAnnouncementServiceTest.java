package com.edu.eduplatform.announcement.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.edu.eduplatform.announcement.domain.SiteAnnouncement;
import com.edu.eduplatform.announcement.repository.SiteAnnouncementRepository;
import java.lang.reflect.Field;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SiteAnnouncementServiceTest {

    @Mock
    private SiteAnnouncementRepository siteAnnouncementRepository;

    @InjectMocks
    private SiteAnnouncementService siteAnnouncementService;

    @Test
    void getCurrentMessage_있으면_메시지를_반환한다() throws Exception {
        SiteAnnouncement announcement = withId(SiteAnnouncement.builder().message("점검 안내").build(), 1L);
        when(siteAnnouncementRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(announcement));

        assertThat(siteAnnouncementService.getCurrentMessage()).contains("점검 안내");
    }

    @Test
    void getCurrentMessage_없으면_빈_값을_반환한다() {
        when(siteAnnouncementRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.empty());

        assertThat(siteAnnouncementService.getCurrentMessage()).isEmpty();
    }

    @Test
    void save_기존_공지가_없으면_새로_만든다() {
        when(siteAnnouncementRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.empty());

        siteAnnouncementService.save("새 공지");

        verify(siteAnnouncementRepository).save(any(SiteAnnouncement.class));
    }

    @Test
    void save_기존_공지가_있으면_내용을_갱신하고_새로_만들지_않는다() {
        SiteAnnouncement existing = SiteAnnouncement.builder().message("기존 공지").build();
        when(siteAnnouncementRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(existing));

        siteAnnouncementService.save("수정된 공지");

        assertThat(existing.getMessage()).isEqualTo("수정된 공지");
        verify(siteAnnouncementRepository, never()).save(any());
    }

    @Test
    void clear_존재하면_삭제한다() {
        SiteAnnouncement existing = SiteAnnouncement.builder().message("공지").build();
        when(siteAnnouncementRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(existing));

        siteAnnouncementService.clear();

        verify(siteAnnouncementRepository).delete(existing);
    }

    @Test
    void clear_존재하지_않으면_아무것도_하지_않는다() {
        when(siteAnnouncementRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.empty());

        siteAnnouncementService.clear();

        verify(siteAnnouncementRepository, never()).delete(any());
    }

    private static SiteAnnouncement withId(SiteAnnouncement announcement, Long id) throws Exception {
        Field field = SiteAnnouncement.class.getDeclaredField("id");
        field.setAccessible(true);
        field.set(announcement, id);
        return announcement;
    }
}
