package com.edu.eduplatform.progress.controller;

import com.edu.eduplatform.progress.dto.ProgressCompleteRequest;
import com.edu.eduplatform.progress.service.ProgressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/progress")
public class ProgressApiController {

    private final ProgressService progressService;

    @PostMapping("/complete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void complete(@Valid @RequestBody ProgressCompleteRequest request) {
        progressService.complete(request.memberId(), request.lessonId());
    }
}
