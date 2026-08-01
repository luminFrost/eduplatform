package com.edu.eduplatform.common.util;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * 오늘 날짜를 시드로 쓰는 결정론적 셔플 — DB에 "오늘 뭘 냈는지" 저장하지 않고도 같은 날엔 항상 같은
 * 결과, 자정이 지나면 자동으로 바뀌는 "매일" 콘텐츠(그림 퀴즈, 오늘의 단어 등)를 만드는 데 쓴다.
 */
public final class DailySeed {

    private DailySeed() {
    }

    public static long forToday() {
        return LocalDate.now().toEpochDay();
    }

    /** extraSeed로 같은 날 안에서도 서로 다른 셔플 결과를 여러 개 뽑을 수 있다(예: 단어장 vs 단어 퀴즈). */
    public static <T> List<T> shuffledForToday(List<T> items, long extraSeed) {
        List<T> copy = new ArrayList<>(items);
        Collections.shuffle(copy, new Random(forToday() + extraSeed));
        return copy;
    }
}
