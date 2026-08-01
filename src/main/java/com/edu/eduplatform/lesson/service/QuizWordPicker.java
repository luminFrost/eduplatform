package com.edu.eduplatform.lesson.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * "English — 한글" 문장에서 핵심 단어를 뽑아 빈칸 퀴즈를 만드는 공용 로직.
 * 레슨 이해도 퀴즈({@link LessonService})와 그림/매일 단어 퀴즈가 같은 방식을 공유한다.
 */
public final class QuizWordPicker {

    /** 퀴즈 빈칸으로 고르지 않을 문법 기능어(핵심 의미를 담지 않아 이해도 확인에 부적합). */
    private static final Set<String> STOPWORDS = Set.of(
            "a", "an", "the", "i", "you", "he", "she", "it", "we", "they", "me", "him", "her", "us", "them",
            "is", "am", "are", "was", "were", "be", "been", "being",
            "do", "does", "did", "have", "has", "had",
            "to", "of", "in", "on", "at", "for", "and", "but", "or", "so", "as", "with", "not",
            "my", "your", "his", "its", "our", "their", "this", "that", "these", "those",
            "will", "would", "can", "could", "may", "might", "shall", "should", "must",
            "please", "let's", "just", "very", "really"
    );

    private QuizWordPicker() {
    }

    /** 불용어를 제외하고 가장 긴 단어를 고른다(동률이면 문장에서 먼저 나오는 단어). */
    public static Optional<String> extractKeyWord(String sentence) {
        String best = null;
        for (String token : sentence.split("\\s+")) {
            String cleaned = token.replaceAll("[^A-Za-z']", "");
            if (cleaned.isEmpty() || STOPWORDS.contains(cleaned.toLowerCase())) {
                continue;
            }
            if (best == null || cleaned.length() > best.length()) {
                best = cleaned;
            }
        }
        return Optional.ofNullable(best);
    }

    /** 문장에서 단어(대소문자 무시, 단어 경계 기준) 첫 등장을 "___"로 치환한다. */
    public static String blankOut(String sentence, String word) {
        return sentence.replaceFirst("(?i)\\b" + Pattern.quote(word) + "\\b", "___");
    }

    /** pool에서 exclude(대소문자 무시)를 뺀 나머지를 섞어 최대 count개 반환한다. */
    public static List<String> pickDistractors(Collection<String> pool, String exclude, int count, Random random) {
        List<String> candidates = new ArrayList<>();
        for (String word : pool) {
            if (!word.equalsIgnoreCase(exclude)) {
                candidates.add(word);
            }
        }
        Collections.shuffle(candidates, random);
        return candidates.subList(0, Math.min(count, candidates.size()));
    }
}
