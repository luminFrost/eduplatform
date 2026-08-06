package com.edu.eduplatform.announcement.repository;

import com.edu.eduplatform.announcement.domain.SiteAnnouncement;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SiteAnnouncementRepository extends JpaRepository<SiteAnnouncement, Long> {

    Optional<SiteAnnouncement> findFirstByOrderByIdAsc();
}
