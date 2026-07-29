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
불특정 다수 대상 오픈 서비스가 아니라 **개개인 약점 영역에 맞춘 학습**을 지향 — 공식 코스(레벨 기준) +
개인 코스(약점 영역 기준, `Course.ownerId`) 구조. 개인 코스 비중 판단은 자가선택→이력기반→진단테스트
순으로 구현 (Phase 5, 진단테스트가 최후순위). 자세한 내용은 PRODUCT.md 3-2, DESIGN.md 도메인 모델 참고.

**완료됨**
- 프로젝트 초기 세팅 (Spring Boot 4.1.0 / Java 21), GitHub 연동
- 기능별 패키지 스켈레톤 생성 (member 예시 + course/lesson/progress 도메인)
- H2 + JPA 설정, 랜딩 페이지
- 회원 가입/조회 구현 (dev 병합됨)
  - API: `POST /api/members`, `GET /api/members/{id}` (`MemberService`, `MemberApiController`, DTO, 중복 이메일/미존재 예외 처리)
  - 화면: `GET /members/new`(가입 폼), `POST /members`(가입 처리), `GET /members/{id}`(상세) — `MemberViewController`
  - 테스트: `MemberRepositoryTest`(@DataJpaTest), `MemberApiControllerTest`(@SpringBootTest + MockMvc)
- 코스·레슨 학습(Phase 2) 구현 (dev 병합됨)
  - API: `GET/POST /api/courses`(대상·레벨 필터), `GET /api/courses/{id}/lessons` — `CourseService`, `LessonService`
  - 화면: `GET /courses`(필터+목록), `GET /courses/{id}`(코스 상세+레슨 목록), `GET /lessons/{id}`(레슨 학습, 이전/다음 이동)
    - "학습 완료" 버튼은 자리만 배치 — Phase 3(LearningProgress 연동)에서 실제 동작 연결 예정.
  - 샘플 데이터: `SampleDataInitializer`(CommandLineRunner)가 대상×레벨 8개 조합에 각 3개씩, 총 24개 코스 시딩.
  - 테스트: `CourseRepositoryTest`, `CourseApiControllerTest`, `LessonServiceTest`(Mockito, 이전/다음 레슨 계산 검증)
- 레슨 콘텐츠·화면 개선 + 영역별 탭/레벨 여정 (dev 병합됨)
  - `Course.emoji` 추가(실제 삽화 도입 전까지 이모지로 시각 요소 대체), 레슨 `content`를 `INTRO:`/" — " 컨벤션으로 구조화해 카드 그리드로 렌더링(초등 코스는 여우 마스코트 인사 문구 포함).
  - `Lesson.lessonType`(VOCAB/READING/WRITING/LISTENING/SPEAKING) 추가 — 코스 상세 화면에 영역별 탭(전체/어휘/읽기/쓰기/듣기/말하기) 필터 + 탭별 개수 표시, 듣기·말하기는 콘텐츠 없어 "준비 중" 빈 상태로 정직하게 표시.
  - 코스 목록의 레벨 select를 클릭형 여정 경로(입문→초급→중급→고급)로 교체.
  - **마스코트 캐릭터(여우/고양이) SVG 시도는 보류** — 좌표만으로 그리다 보니 결과물이 기대에 못 미쳐(사용자 피드백: "쥐처럼 보인다" 등), 사용자가 AI 이미지 생성 도구로 직접 만들어 파일로 전달하면 그때 `static/images/`에 연결하기로 함. 지금은 이모지만 사용.
- 학습 진행 처리(Phase 3) 구현 (dev 병합됨)
  - **로그인 없는 "현재 회원" 식별**: 비밀번호 검증 없이 세션에 회원 id만 기억해두는 임시 방식.
    `common/web/CurrentMemberId`(파라미터 애너테이션) + `CurrentMemberIdArgumentResolver` + `CurrentMemberSession`(세션 read/write 유일 창구) + `WebMvcConfig`로 등록.
    나중에 토큰/AOP 기반 진짜 인증(Phase 6)으로 바꿀 때 이 세 파일만 손대면 되도록 컨트롤러 시그니처는 `@CurrentMemberId Long memberId`로 고정해둠.
    회원가입 성공 시 자동으로 세션 기록, `POST /members/{id}/select`("이 회원으로 학습 시작하기" 버튼)로 재선택 가능.
  - API: `POST /api/progress/complete` — `ProgressService.complete()`(멱등, 이미 완료면 재처리 안 함)
  - 화면: 레슨 페이지 "학습 완료" 버튼 실제 연결(`POST /lessons/{id}/complete`, 완료 시 "완료함 ✓"로 전환), 세션 없으면 회원가입 유도.
  - `GET /my` 마이페이지 — 회원이 손댄 코스별 진도(완료/전체, %, 진행바). `ProgressService.getCourseProgress()`가 LearningProgress→Lesson→Course를 조인해 집계.
  - 테스트: `ProgressServiceTest`(Mockito), `ProgressApiControllerTest`, `LearningProgressFlowTest`(세션 유지 e2e: 가입→마이페이지→완료→마이페이지 재확인)
- Boot 4.1 참고: 테스트 스타터가 기능별로 세분화됨 — MockMvc/Jackson용 `spring-boot-starter-webmvc-test`, JPA 테스트용 `spring-boot-starter-data-jpa-test` 추가 필요.
  Jackson은 3.x로 `tools.jackson.databind` 패키지 사용(`com.fasterxml.jackson` 아님).
  `@DataJpaTest`→`org.springframework.boot.data.jpa.test.autoconfigure`, `@AutoConfigureMockMvc`→`org.springframework.boot.webmvc.test.autoconfigure`로 패키지 이동.

**다음 단계 (예시, 우선순위 순)**
1. 사용자가 마스코트 이미지 파일을 주면 `static/images/`에 넣고 레슨 인트로/코스 카드에 연결
2. (선택) 회원가입 폼 검증 실패 시 입력값 유지 — 현재는 재입력 필요
3. 개인 코스(Phase 5, PRODUCT.md 3-2 참고) — 자가 선택 방식부터
4. Phase 6 진짜 인증(Spring Security) 도입 시 `CurrentMemberSession`/`CurrentMemberIdArgumentResolver` 내부만 교체
