package com.edu.eduplatform.progress.controller;

import com.edu.eduplatform.common.web.CurrentMemberId;
import com.edu.eduplatform.lesson.exception.LessonNotFoundException;
import com.edu.eduplatform.member.exception.MemberNotFoundException;
import com.edu.eduplatform.progress.dto.ProgressCompleteRequest;
import com.edu.eduplatform.progress.service.ProgressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
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
    public void complete(@CurrentMemberId Long memberId, @Valid @RequestBody ProgressCompleteRequest request) {
        progressService.complete(memberId, request.lessonId());
    }

    @ExceptionHandler(MemberNotFoundException.class)
    public ResponseEntity<String> handleMemberNotFound(MemberNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }

    @ExceptionHandler(LessonNotFoundException.class)
    public ResponseEntity<String> handleLessonNotFound(LessonNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }
}
