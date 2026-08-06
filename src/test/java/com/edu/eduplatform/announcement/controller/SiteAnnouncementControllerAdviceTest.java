package com.edu.eduplatform.announcement.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.edu.eduplatform.announcement.domain.SiteAnnouncement;
import com.edu.eduplatform.announcement.repository.SiteAnnouncementRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class SiteAnnouncementControllerAdviceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SiteAnnouncementRepository siteAnnouncementRepository;

    @Test
    void 공지가_있으면_아무_페이지에나_배너가_보인다() throws Exception {
        SiteAnnouncement announcement = siteAnnouncementRepository.save(
                SiteAnnouncement.builder().message("전역배너테스트공지").build());
        try {
            mockMvc.perform(get("/"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("전역배너테스트공지")));
        } finally {
            // 이 테이블은 사이트 전체에서 항상 최대 한 행만 있어야 하는 싱글턴이라, 여기서 만든 행이
            // 다른 테스트(특히 "없으면" 케이스)에 새어나가지 않도록 반드시 정리한다.
            siteAnnouncementRepository.delete(announcement);
        }
    }

    @Test
    void 공지가_없으면_배너가_안_보인다() throws Exception {
        // 싱글턴 테이블이라 이전 테스트가 남긴 행이 있을 수 있어, 전제 조건을 직접 보장한다.
        siteAnnouncementRepository.deleteAll();

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("site-announcement"))));
    }
}
