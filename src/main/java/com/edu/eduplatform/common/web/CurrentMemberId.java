package com.edu.eduplatform.common.web;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 컨트롤러 메서드 파라미터에 붙여 "현재 회원"의 id(Long, 없으면 null)를 주입받는다.
 * 지금은 세션 기반으로 조회하지만({@link CurrentMemberIdArgumentResolver}), 이 애너테이션 뒤로
 * 조회 방식을 숨겨뒀기 때문에 나중에 토큰/AOP 기반으로 바꿔도 컨트롤러 코드는 그대로 둘 수 있다.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface CurrentMemberId {
}
