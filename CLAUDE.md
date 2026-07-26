# eduplatform

> 이 파일은 Claude(Claude Code / Cowork)가 **매 세션 시작 시 자동으로 읽는 프로젝트 메모리**다.
> 세션이 끊기거나 재접속해도 여기에 적힌 내용으로 맥락을 이어간다.
> 작업 상태가 바뀌면 아래 "작업 상태 / 다음 단계"를 갱신하고 커밋한다.

## 프로젝트 개요

초등학생과 성인을 대상으로 한 **영어 학습 웹 플랫폼**. 영어 입문 단계부터 시작해
단계별(BEGINNER → ELEMENTARY → INTERMEDIATE → ADVANCED)로 학습을 제공한다.
현재는 하나의 모놀리스 프로젝트로 개발하고, 추후 필요 시 분리 여부를 판단한다.

## 기술 스택

| 항목 | 버전 |
|------|------|
| Java | 21 (LTS) |
| Spring Boot | 4.1.0 |
| Spring Framework | 7.x |
| 빌드 도구 | Gradle 9.5.1 (Groovy DSL) |
| ORM | Spring Data JPA / Hibernate 7.4 |
| View | Thymeleaf (서버 렌더링) + REST(@RestController) 병행 |
| DB | H2 (인메모리, 개발용) |
| 기타 | Lombok |

## 아키텍처 / 패키지 구조

기능별(package-by-feature) 구조. 각 기능 패키지 안에 domain/repository/service/controller를 둔다.

```
com.edu.eduplatform
├── common/            공통
│   ├── config/        JpaConfig (JPA Auditing)
│   ├── entity/        BaseTimeEntity (생성/수정 시각 공통)
│   └── web/           HomeController (랜딩 페이지)
├── member/            회원 (초등/성인 구분, 학습 레벨) — 가장 완성도 높은 예시
│   ├── domain/        Member, MemberType(CHILD/ADULT), EnglishLevel
│   ├── repository/    MemberRepository
│   ├── service/       MemberService
│   └── controller/    MemberApiController (/api/members)
├── course/            코스 (대상/난이도)
│   ├── domain/        Course
│   └── repository/    CourseRepository
├── lesson/            레슨 (코스 하위 학습 단위)
│   ├── domain/        Lesson (courseId 참조)
│   └── repository/    LessonRepository
└── progress/          학습 진행 상황
    ├── domain/        LearningProgress (memberId, lessonId 참조)
    └── repository/    LearningProgressRepository
```

## 코딩 컨벤션

- 엔티티는 `BaseTimeEntity`를 상속해 생성/수정 시각을 자동 관리한다.
- 엔티티 기본 생성자는 `@NoArgsConstructor(access = PROTECTED)`, 생성은 `@Builder`로.
- 세터는 만들지 않는다. 상태 변경은 의미 있는 메서드로(예: `LearningProgress.complete()`).
- 서비스는 `@Transactional(readOnly = true)` 기본, 쓰기 메서드에만 `@Transactional`.
- 연관관계는 현재 **id 참조(Long)**로 느슨하게 둔다. 필요 시 `@ManyToOne`으로 전환.
- 페이지 렌더링은 `@Controller`(templates/), API는 `@RestController`(/api/...).

## 실행 방법

```bash
# 개발 서버 실행 (포그라운드)
./gradlew bootRun

# 백그라운드 실행 (세션과 무관하게 계속 돌림)
./scripts/run-dev.sh      # 시작, 로그: build/bootRun.log
./scripts/stop-dev.sh     # 중지

# 빌드/테스트
./gradlew build
./gradlew test
```

- 앱: http://localhost:8080
- H2 콘솔: http://localhost:8080/h2-console (JDBC URL: `jdbc:h2:mem:eduplatform`, user `sa`)

## 환경 주의사항

- `gradle.properties`의 `org.gradle.java.installations.paths`는 **이 Mac의 Homebrew JDK 21 경로**다.
  다른 환경/팀원과 공유 시 각자 경로로 바꾸거나, 이 줄을 지우고 toolchain 자동 다운로드로 전환한다.
- JDK 21은 Homebrew `openjdk@21`로 설치됨. IntelliJ 프로젝트 SDK도 21로 지정.

## Git

- 원격: `git@github.com:luminFrost/eduplatform.git` (SSH)
- 이 프로젝트는 상위 폴더(연습용 git repo)와 분리된 **독립 저장소**다.

### 브랜치 전략 (main + dev + feature/*)

| 브랜치 | 용도 | 규칙 |
|--------|------|------|
| `main` | 배포/안정 | 항상 동작하는 상태 유지. dev에서 검증된 것만 병합. 직접 커밋 금지. |
| `dev` | 개발 통합 | 기능들이 모이는 기본 개발 브랜치. feature 브랜치를 여기로 병합. |
| `feature/*` | 기능 개발 | dev에서 분기, 기능 완성 후 dev로 병합하고 삭제. 예: `feature/member-signup` |

**작업 흐름**
```bash
# 새 기능 시작
git switch dev && git pull
git switch -c feature/member-signup

# 개발 후 dev로 병합
git switch dev && git merge --no-ff feature/member-signup
git push origin dev
git branch -d feature/member-signup

# 안정화되면 main으로 배포
git switch main && git merge --no-ff dev && git push origin main
```
평소 개발은 `dev` 또는 `feature/*`에서 한다. `main`은 배포 시점에만 갱신.

## 작업 상태 / 다음 단계

> 세션을 이어받는 Claude는 이 섹션을 먼저 확인하고, 작업 후 갱신한다.
> 제품 기획("무엇을 만드나")은 **[docs/PRODUCT.md](docs/PRODUCT.md)**,
> 기술 설계(도메인 모델·화면·API·로드맵)는 **[docs/DESIGN.md](docs/DESIGN.md)** 참고.

**확정된 제품 방향**: 종합형 학습(듣기·말하기·읽기·쓰기+어휘) + 체계적 레벨/로드맵.
MVP = 회원가입/로그인 + 코스·레슨 학습(텍스트 활동 우선) + 진도 표시.

**완료됨**
- 프로젝트 초기 세팅 (Spring Boot 4.1.0 / Java 21), GitHub 연동
- 기능별 패키지 스켈레톤 생성 (member 예시 + course/lesson/progress 도메인)
- H2 + JPA 설정, 랜딩 페이지

**다음 단계 (예시, 우선순위 순)**
1. 회원 가입/조회 기능 구현 (MemberService + MemberApiController + DTO)
2. 코스·레슨 CRUD 및 목록/상세 페이지(Thymeleaf)
3. 학습 진행 처리(레슨 완료 시 LearningProgress 갱신)
4. 초등/성인 및 레벨별 코스 필터링
5. 테스트 코드 작성 (@DataJpaTest, @SpringBootTest)
