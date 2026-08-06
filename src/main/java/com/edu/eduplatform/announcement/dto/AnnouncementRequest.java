package com.edu.eduplatform.announcement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AnnouncementRequest(

        @NotBlank(message = "공지 내용을 입력해 주세요.")
        @Size(max = 500, message = "공지는 500자 이내로 입력해 주세요.")
        String message
) {
}
