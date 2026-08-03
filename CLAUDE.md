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
- 개인 코스(Phase 5, 자가 선택) 구현 (dev 병합됨)
  - `Course`에 `ownerId`(nullable, 소유 회원)/`focusAreas`(`Set<LessonType>`, `@ElementCollection`)/`criteriaSource`(신규 `CourseCriteriaSource`: SELF_SELECTED/HISTORY_BASED/DIAGNOSTIC_TEST) 추가.
    `LessonType`을 그대로 재사용(DESIGN.md의 SkillArea = 레슨 활동 유형과 동일 5종이라고 이미 정리해둔 대로).
  - `CourseRepository.search()`는 `ownerId is null`만 반환하도록 수정 — 개인 코스는 일반 코스 목록/필터에 노출되지 않음. `findByOwnerIdOrderByIdDesc`로 본인 코스만 조회.
  - `CourseService.createPersonalCourse()` — 회원의 대상·레벨과 같은 공식 코스들에서 선택한 영역(focusAreas)의 레슨만 걸러 **복사**해 새 코스에 담음(레슨 공유 없음, PRODUCT.md 3-2 결정 그대로). 듣기·말하기는 아직 레슨이 없어 골라도 빈 코스가 됨 — 정직하게 그대로 둠.
  - 화면: `GET /courses/personal/new`(영역 체크박스, 듣기/말하기는 "준비 중" 비활성), `POST /courses/personal` → 생성 후 상세로 리다이렉트. 코스 상세에 "개인 코스 · 자가 선택" 배지. 마이페이지에 "내 개인 코스" 목록 + 만들기 링크.
  - API: `POST /api/courses/personal` (DESIGN.md 문서화된 엔드포인트).
  - 테스트: `CourseServiceTest`(Mockito, 영역별 레슨 필터링/복사 검증), `CourseRepositoryTest`에 개인 코스 제외/소유자별 조회 케이스 추가.
- 전체 코드 리뷰 후 버그 4건 수정 (dev 병합됨)
  - `CourseViewController`에서 `MemberType/EnglishLevel/LessonType.valueOf()`를 직접 호출해 잘못된 쿼리 파라미터(`?target=BOGUS` 등)로 500이 나던 것을 `parseEnum()` 헬퍼(실패 시 null=필터 없음)로 수정. 재현 후 수정 확인함.
  - `ProgressService.complete()`가 존재하지 않는 memberId/lessonId도 그냥 받아 고아 `LearningProgress` row를 만들던 것을 회원/레슨 존재 검증 추가(`MemberNotFoundException`/`LessonNotFoundException` → API는 404). 재현 후 수정 확인함.
  - `MemberService.signUp()`의 이메일 중복 체크-저장 사이 레이스 컨디션 — `saveAndFlush()` + `DataIntegrityViolationException` 캐치로 `DuplicateEmailException`으로 변환.
  - `Lesson.content`가 스키마상 nullable인데 코드는 항상 값 있다고 가정하던 불일치 — `@Column(nullable = false)` 추가.
  - 테스트: `CourseViewControllerTest`(잘못된 필터 값 200 확인), `MemberServiceTest`(레이스 시뮬레이션), `ProgressServiceTest`/`ProgressApiControllerTest`에 존재하지 않는 회원/레슨 케이스 추가.
- Boot 4.1 참고: 테스트 스타터가 기능별로 세분화됨 — MockMvc/Jackson용 `spring-boot-starter-webmvc-test`, JPA 테스트용 `spring-boot-starter-data-jpa-test` 추가 필요.
  Jackson은 3.x로 `tools.jackson.databind` 패키지 사용(`com.fasterxml.jackson` 아님).
  `@DataJpaTest`→`org.springframework.boot.data.jpa.test.autoconfigure`, `@AutoConfigureMockMvc`→`org.springframework.boot.webmvc.test.autoconfigure`로 패키지 이동.
- 코스 필터·CSS·콘텐츠 보강 (dev 병합됨)
  - 코스 목록/상세에 영역별(어휘/읽기/쓰기/듣기/말하기) 필터 탭 추가 — `CourseRepository.search()`가 `MemberType/EnglishLevel/LessonType` 3개 필터를 조합해 처리(레슨 타입은 "해당 타입 레슨을 가진 코스"로 서브쿼리 매칭). `CourseViewController`/`CourseApiController`/`course/list.html`에 `type` 파라미터 관통.
  - **24개 공식 코스 전체를 레슨 2~4개 → 7개로 확장** (총 168개 레슨). 4개 백그라운드 에이전트로 대상×레벨 조합별 콘텐츠를 병렬 작성시켜 `SampleDataInitializer.COURSE_SEEDS`에 반영. 기존 콘텐츠 포맷 컨벤션(`INTRO:` 줄, "영어. — 한국어." 페어, 초등 여우 마스코트 인사말) 그대로 유지.
  - **배경/여백 CSS 전면 개편** — 기존엔 순백색 배경에 720px 폭 제한이라 "빈 공간이 너무 많다"는 피드백. `style.css`를 CSS 변수 기반 팔레트로 재작성(연한 블루→민트→크림 그라디언트 배경, 카드에 그림자, 페이지 폭을 880px/1080px(`page`/`page-wide`)로 확장해 코스 카드 그리드가 3~4열로 보이게 함).
  - **공용 헤더 내비 도입** — `templates/fragments/layout.html`에 `th:fragment="header"` 정의(로고 + 코스/마이페이지/회원가입 링크), 8개 템플릿 전부에서 `th:replace`로 삽입. 각 페이지 하단에 중복돼 있던 "홈으로" 링크 제거.
  - 랜딩 페이지(`index.html`)를 문구 3줄짜리 페이지에서 실제 히어로 섹션(헤드라인+CTA 2개+레벨 pill 4개+영역 소개 카드 4개)으로 재구성.
  - 빌드/테스트(`./gradlew build`) 통과, `run-dev.sh`로 띄워 브라우저에서 랜딩/코스목록/코스상세/레슨상세/회원가입 페이지 직접 확인 완료.
  - **레슨 카드 2차 품질 개선** — `LessonService.splitLeadingIcon()`을 추가해 콘텐츠 줄 맨 앞 이모지(예: "🍎 Red apple.")를 본문과 분리(첫 토큰에 알파벳이 없으면 이모지로 판단 — 영어 문장은 항상 알파벳으로 시작해 오탐 없음). `LessonDetailResponse.ContentLine`에 `icon` 필드 추가.
    구글 폰트 도입(`Noto Sans KR`: 본문/한글 전체, `Poppins`: 레슨 카드 영어 문장 `.en` 및 브랜드/뱃지용 `--font-display`) — 브라우저가 문자 단위로 폰트 폴백을 하므로 한/영 혼용 요소도 깨지지 않음.
  - **레슨 카드 3차 개선: 진짜 플래시카드로 전환** (사용자가 아동용 단어 플래시카드 이미지를 레퍼런스로 제시 — "카드 느낌이 나야지, 지금은 네모 영역에 텍스트랑 이미지 집어넣은거잖아"). 2차의 작은 유니코드 이모지 배지 방식은 폐기.
    - `IconCatalog`(`lesson/service`) 신규 — `openmoji-map.tsv`(클래스패스 리소스)를 읽어 이모지 → OpenMoji SVG 경로를 매핑. 콘텐츠에 실제로 쓰인 이모지 171종을 `github.com/hfg-gmuend/openmoji`(CC BY-SA 4.0)에서 스크립트로 내려받아 `static/images/openmoji/`에 저장(파일명은 유니코드 코드포인트지만 변형 선택자(FE0F) 포함 여부가 문자마다 달라 런타임 계산 대신 실제로 받아둔 매핑 그대로 사용).
    - `ContentLine`에 `iconImage` 필드 추가, `LessonService`가 아이콘을 큰 SVG 경로로 해석해 채움.
    - `lesson/detail.html` 카드 구조를 세로형 플래시카드로 전환 — 이미지가 있는 PHRASE/NOTE 카드는 위쪽에 큰 아이콘(88px) + 컬러 배경 밴드, 아래에 굵은 문장 + 번역, 카드 테두리는 4색(파랑/초록/노랑/분홍) 순환(`accent-0~3`). 그리드도 전체폭 1열 스택에서 여러 장이 나란히 보이는 카드 그리드로 변경. 이모지가 없는 성인 코스 문장(대부분)은 기존처럼 이미지 없는 가로형 텍스트 카드로 그대로 표시 — 확인함.
    - OpenMoji 라이선스 표기를 위해 `fragments/layout.html`에 `footer` 프래그먼트 추가, 8개 템플릿 전부에 삽입.
    - 테스트: `LessonServiceTest`에 `IconCatalog` 목(mock) 추가.
- 마이페이지 진도 카드 디자인 통일 + accent 컬러 버그 수정 (dev 병합됨)
  - "학습 현황" 진도 카드(`progress-card`)를 다른 카드 컴포넌트와 같은 디자인으로 통일 — 컬러 아이콘 타일, 굵은 제목, 카드 전체 클릭, 화살표.
  - 흩어져 있던 `accent-0~3` 컬러 규칙이 일부 컴포넌트(`.progress-card`)의 자체 `border` 선언보다 스타일시트 앞쪽에 있어 작성 순서에 밀려 색이 적용 안 되던 버그 발견·수정 — 규칙을 파일 맨 끝 한 곳으로 통합해 항상 이기도록 정리.
- 개인 코스 생성 방어 로직 보강 (dev 병합됨)
  - `CourseService.createPersonalCourse()`가 컨트롤러의 `@Valid` 검증에만 기대지 않고 `focusAreas`가 비었으면 `InvalidFocusAreasException`을 직접 던지도록 서비스 레벨 방어 추가.
  - 같은 `focusAreas`로 이미 만든 개인 코스가 있으면(중복 제출 등) 새로 만들지 않고 기존 코스를 그대로 반환 — API는 이 경우 201 대신 200 반환(`PersonalCourseCreationResult(course, created)`로 구분).
- 회원가입 폼 검증 실패 시 입력값 유지 (dev 병합됨)
  - `MemberViewController.signUp()`이 검증 실패/이메일 중복 두 실패 분기 모두에서 제출된 `MemberCreateRequest`를 `model.addAttribute("form", request)`로 다시 넘기고, `signup-form.html`이 `th:value="${form?.email}"` 등으로 이메일·닉네임·회원유형·레벨을 그대로 복원한다. curl로 두 실패 케이스 다 재현해 확인함.
- 개인 코스 기준 판단 고도화 1단계: 학습 이력 기반(HISTORY_BASED) 구현 (dev 병합됨)
  - `ProgressService.recommendFocusAreas(memberId)` 신규 — 완료율이 아니라 **회원 레벨에 존재하는 영역별 전체 레슨 수 대비 완료한 레슨 수(커버리지)**가 가장 낮은 영역(들, 동률 시 전부)을 약점으로 추천.
    처음엔 "완료율"(터치한 레슨 중 완료 비율)로 설계했다가, `LearningProgress`는 `ProgressService.complete()`를 통해서만 생성되고 그 메서드가 저장 직전에 항상 `complete()`를 호출하므로 **DB에 남는 진행 기록은 예외 없이 전부 완료 상태**라는 걸 실서버 curl 검증 중 발견 — "완료율"은 늘 100%라 신호가 되지 않아 폐기하고 커버리지 방식으로 다시 설계함. (교훈: 진단테스트/퀴즈 등으로 "시작했지만 미완료" 상태가 실제로 생기기 전까지는 완료율 기반 신호를 쓸 수 없음.)
    완료한 레슨이 3개 미만이면(`MIN_HISTORY_LESSONS`) 근거 부족으로 `InsufficientHistoryException`.
  - `CourseService.createPersonalCourse()`를 `buildPersonalCourse()` 공통 메서드로 리팩터링하고 `createPersonalCourseFromHistory(memberId)` 추가 — 추천된 focusAreas로 자가 선택과 동일한 방식(레슨 복사, 중복 시 기존 코스 재사용)으로 코스를 만들되 `criteriaSource=HISTORY_BASED`.
  - API: `POST /api/courses/personal/history-based` (`HistoryBasedCourseCreateRequest`), 화면: `POST /courses/personal/history-based` — `course/personal-new.html`에 "학습 이력 기반으로 만들기" 버튼 추가.
  - 테스트: `ProgressServiceTest`/`CourseServiceTest`에 커버리지 계산·동률·이력 부족 케이스 추가. curl로 실서버에서 VOCAB 전량 완료·WRITING 일부만 완료 → WRITING 추천 → 레슨 7개 복사 → 재요청 시 200(기존 코스 재사용) 전체 확인.
- Phase 6: 진짜 인증(Spring Security) 도입 (dev 병합됨)
  - CLAUDE.md에 미리 적어둔 대로 `CurrentMemberSession`/`CurrentMemberIdArgumentResolver`/`WebMvcConfig` 세 파일만 내부 교체 — 컨트롤러 시그니처(`@CurrentMemberId Long memberId`)는 그대로 유지. 리졸버는 이제 세션 attribute 대신 `SecurityContextHolder`에서 `MemberPrincipal`을 꺼내 읽는다.
  - `Member.password`(BCrypt 해시) 추가, `MemberCreateRequest`에 `password`(`@NotBlank` + `@Size(min=8)`, 확인 필드는 생략) 추가. `member/security` 패키지 신규: `MemberPrincipal implements UserDetails`(memberId 직접 보유, DB 재조회 없이 리졸버가 즉시 꺼내 씀), `MemberUserDetailsService`(이메일로 조회).
  - `SecurityConfig` 신규 — 이메일+비밀번호 `formLogin`(`/login`), `logout`. `/my`·`/courses/personal/**`·학습완료 POST는 `authenticated()`, 코스/레슨 브라우징(`GET /courses/**`, `GET /lessons/*`)과 `/api/**`는 계속 공개. **`/api/**`는 이번 범위에서 제외**(아래 후속 과제 참고).
  - **비밀번호 없이 아무 회원이나 "선택"할 수 있던 `POST /members/{id}/select` 제거.** 그 버튼이 유일한 진입점이었던 `GET /members/{id}` 페이지(`member/detail.html`)도 함께 삭제 — 가입 성공 리다이렉트를 `/members/{id}` → `/my`로 바꾸면서 완전히 죽은 페이지가 됐음을 `grep`으로 확인 후 제거.
  - 헤더 네비게이션에 로그인 상태 토글 추가(`sec:authorize="isAuthenticated()"`/`isAnonymous()`, `thymeleaf-extras-springsecurity6`). 닉네임 표시는 `sec:authentication="name"`이 이메일만 주기 때문에 별도 `@ModelAttribute` 컨트롤러 어드바이스가 필요해 이번엔 범위에서 뺌(로그인/로그아웃 링크만).
  - CSRF는 세션 폼 경로에서 유지, `/api/**`·`/h2-console/**`만 제외. 모든 POST 폼에 hidden CSRF input을 수동으로 추가했는데, 실행해보니 `thymeleaf-extras-springsecurity6`가 `RequestDataValueProcessor`로 **같은 값을 자동으로도 주입**하고 있었음(폼에 동일 토큰의 hidden input이 2개 렌더링됨, 브라우저 제출 시 무해). 계획 단계에서 "자동 주입이 이 조합에서 실제로 동작하는지 확신할 수 없다"고 플래그했던 부분이 실서버 검증으로 확인됨.
  - Security가 이미 `authenticated()`로 막는 라우트(`/my`, `POST /lessons/{id}/complete`, `/courses/personal/**`)에 남아있던 `if (memberId == null) return "redirect:/members/new"` 방어 코드 삭제 — 해당 경로는 Security 게이트를 통과해야만 컨트롤러에 도달하므로 도달 불가능한 코드였음.
  - `authorizeHttpRequests` 규칙 순서 주의: `/courses/personal/**`(authenticated) 같은 특정 규칙은 `GET /courses/**`(permitAll) 같은 일반 규칙보다 **먼저** 선언해야 한다 — 순서가 바뀌면 일반 규칙에 먼저 매치되어 의도와 달리 뚫린다. curl로 `/courses/personal/new`가 익명 접근 시 실제로 `/login`으로 막히는지 확인함.
  - 테스트: `LearningProgressFlowTest`(가입 폼에 password 추가, CSRF `.with(csrf())`, 네거티브 케이스의 기대 리다이렉트를 `/members/new` → `/login`으로 변경 — CSRF 필터가 인가 로직보다 먼저 걸리므로 "인증 없음"을 검증하려면 CSRF 토큰은 유효하게 줘야 함), `MemberApiControllerTest`/`MemberServiceTest`/`MemberRepositoryTest`/`ProgressApiControllerTest`에 password 필드 반영.
  - curl 세션 플로우로 가입(자동 로그인)→마이페이지→로그아웃→마이페이지(로그인 리다이렉트 확인)→재로그인→마이페이지, 잘못된 비밀번호 로그인 실패, 중복 이메일/짧은 비밀번호 검증, H2 콘솔·API 계속 공개 전부 실서버에서 확인.

- REST API의 memberId 신뢰 문제 보완 (dev 병합됨)
  - `/api/**`를 별도의 `SecurityFilterChain`(HTTP Basic, `SessionCreationPolicy.STATELESS`, CSRF 없음)으로 분리하고, 회원 데이터를 바꾸는 두 엔드포인트(`POST /api/progress/complete`, `POST /api/courses/personal`/`/history-based`)에 인증을 요구하도록 변경. `ProgressCompleteRequest`/`PersonalCourseCreateRequest`에서 `memberId` 필드를 아예 제거하고(`HistoryBasedCourseCreateRequest`는 필드가 없어져 클래스째 삭제), 세 API 모두 `@CurrentMemberId`(=인증된 사용자)로 회원을 식별 — 요청 본문에 다른 memberId를 넣어도 무시됨을 curl로 확인.
  - **버그 1**: `formLogin()`과 `httpBasic()`을 같은 필터체인에 함께 두면 인증 안 된 요청 전체(브라우저 라우트 포함)에 필터체인당 하나뿐인 기본 `AuthenticationEntryPoint`가 적용되어 `GET /my` 같은 브라우저 요청까지 401을 반환해버림 — `LearningProgressFlowTest`가 바로 잡아냄. `/api/**` 전용 체인과 그 외 전용 체인, 두 개의 `SecurityFilterChain`으로 분리해 해결(`@Order(1)`/`@Order(2)`, `securityMatcher("/api/**")`).
  - **버그 2**: 필터체인을 둘로 나눈 뒤에도 인증 실패 시 401과 `/login` 리다이렉트가 한 응답에 섞여 나오는 현상 발견 — `BasicAuthenticationEntryPoint`의 `response.sendError(401)`이 Tomcat의 `/error` 내부 포워드를 태우는데, `/error`가 catch-all 체인의 `anyRequest().authenticated()`에 걸려 그 체인의 로그인 리다이렉트가 다시 얹혀버리는 것이었음. `/error`를 permitAll로 열어 해결.
  - `spring-boot-starter-security-test`의 `SecurityMockMvcRequestPostProcessors.httpBasic(...)`으로 테스트 갱신. `ProgressApiControllerTest`는 `PasswordEncoder`로 인코딩한 비밀번호를 가진 회원을 만들어 인증 케이스를, 인증 없이 401이 나오는 케이스를 함께 검증하도록 재작성. `CourseServiceTest`는 `CourseService.createPersonalCourse(Long memberId, Set<LessonType> focusAreas)`로 시그니처가 바뀐 것만 반영(DTO의 memberId 필드 제거에 따른 연쇄 변경).
  - curl로 실서버에서 미인증 401, 잘못된 비밀번호 401, 인증 성공 204/201, 요청 본문에 다른 memberId를 끼워 넣어도 무시되고 인증된 사용자 기준으로 처리됨, 브라우저 라우트(`/my`)는 여전히 `/login`으로 정상 리다이렉트, H2 콘솔 계속 열림을 전부 확인.

- 듣기·말하기(LISTENING/SPEAKING) 콘텐츠 착수 (dev 병합됨)
  - **기존 VOCAB/READING/WRITING도 실제로는 채점 없는 "플래시카드 검토 + 수동 완료 체크"라는 걸 확인**하고
    (PRODUCT.md는 WRITING에 "작문 후 제출"이라 적어놨지만 실제 화면엔 입력창도 제출 로직도 없음), 이번
    작업도 같은 수준으로 단순화하기로 사용자와 합의: LISTENING 오디오는 서버에 mp3를 두지 않고 브라우저
    내장 `speechSynthesis`(Web Speech API)로 재생, SPEAKING은 마이크 캡처(`SpeechRecognition`) 없이
    TTS로 들려주고 "따라 말해보기"만 유도.
  - `LessonService.parseContent()`의 `INTRO:`/`"영어 — 한국어"` 줄 컨벤션은 손대지 않고 그대로 재사용 —
    새 `LineType`이나 파싱 규칙 추가 없음. `LessonDetailResponse`에 없던 `lessonType` 필드만 추가해서
    템플릿이 "이 레슨이 듣기/말하기일 때만 재생 버튼을 보여줄지" 판단하게 함.
  - 이 프로젝트 첫 클라이언트 JS: `static/js/lesson-audio.js` — 이벤트 위임(`document.addEventListener('click', ...)`)으로
    `[data-speak-text]` 버튼 클릭을 잡아 `SpeechSynthesisUtterance`를 큐잉. `lesson/detail.html`의 PHRASE
    카드에 `lesson.lessonType`이 LISTENING/SPEAKING일 때만 조건부로 버튼 렌더링(SPEAKING은 "🎤 듣고 따라
    말해보기", LISTENING은 "🔊 듣기" 문구로 구분).
  - **버그**: 새 `/js/**` 정적 리소스가 `SecurityConfig`의 permitAll 목록에 없어서 비로그인 사용자가
    레슨 페이지(공개 페이지)에 들어가도 스크립트 자체가 `/login`으로 리다이렉트되어 조용히 실패하던 문제 —
    claude-in-chrome으로 버튼 클릭 후 `speechSynthesis.speak()`가 실제로 호출되는지 확인하다가 발견.
    `/css/**`/`/images/**` 옆에 `/js/**`도 permitAll로 추가해 해결. (교훈: 새 정적 리소스 디렉터리를
    추가할 때마다 Security 설정에도 매번 추가해야 함 — 잊기 쉬움.)
  - 콘텐츠는 1차로 ADULT/BEGINNER에 LISTENING 코스 1개("듣기 연습: 일상 속 짧은 안내 듣기")·SPEAKING
    코스 1개("말하기 연습: 자주 쓰는 표현 따라 말하기")만 각 7레슨씩 추가해 기능을 끝까지 검증 —
    다른 대상·레벨 조합은 아직 "준비 중" 그대로. `SampleDataInitializer` 클래스 주석도 갱신.
  - 테스트: `LessonServiceTest`의 `getDetail_*` 두 케이스에 `lessonType` 단언 추가.
  - claude-in-chrome으로 실브라우저 검증: 코스 목록/상세에서 "준비 중" 대신 실제 레슨 노출, 재생 버튼
    클릭 시 `speechSynthesis`가 실제로 발화 시작(`onstart` 이벤트)하는지 몽키패치로 확인, VOCAB
    레슨엔 버튼이 안 뜨는지(조건부 렌더링) 확인.

- 듣기·말하기 콘텐츠를 전체 대상·레벨 조합으로 확장 (dev 병합됨)
  - 지난 작업에서 ADULT/BEGINNER 1쌍만 검증해두고 "다른 조합은 후속 과제"로 남겨뒀던 것을 마저 채움 —
    이제 8개 대상·레벨 조합(CHILD/ADULT × 4레벨) 전부에 LISTENING·SPEAKING 코스가 1개씩 있다.
    공식 코스 24개 → 38개, 레슨 168개 → 266개.
  - 기술 패턴(재생 버튼, `speechSynthesis`, 조건부 렌더링)은 이미 검증돼 있어 이번엔 순수 콘텐츠
    작성이라 판단 — 3개 배경 에이전트에 조합을 나눠 맡기고(CHILD 3개 조합 / CHILD 1개+ADULT 1개 조합 /
    ADULT 2개 조합), 각각 **파일을 직접 수정하지 않고 완성된 Java 코드만 텍스트로 반환**하게 해서
    동시 편집 충돌 없이 직접 `SampleDataInitializer.java`에 순서대로 붙여넣는 방식으로 처리.
  - 레벨별 톤 보정: CHILD는 `INTRO:` 마스코트 인사 + 이모지 아이콘 유지, ADULT는 인사말·아이콘 없이
    바로 "en — kr" 문장만. BEGINNER는 단어 수준의 짧은 명령문(예: "Stand up."), ADVANCED는 뉴스 앵커·
    면접 답변 수준의 복문(예: "Critics argue that the policy fails to address the root cause.").
  - **실수 하나 발견·수정**: 에이전트 3개 결과를 파일에 붙여넣던 중 ADULT/INTERMEDIATE+ADVANCED를
    맡은 에이전트가 반환한 4개 코스 중 뒤쪽 2개(ADULT/ADVANCED 몫)를 붙여넣는 걸 빠뜨림 — 코스/레슨
    개수를 계산해보고(38개/266개여야 하는데 실제론 코스 수가 안 맞음) API로 대상·레벨×타입 조합별
    개수를 전수 확인하다가 `ADULT/ADVANCED: LISTENING=0 SPEAKING=0`을 발견해 잡아냄. 챗봇이 직접
    데이터를 조립할 때도 결과를 다시 세어서 확인하는 게 중요하다는 교훈.
  - claude-in-chrome으로 CHILD/BEGINNER, ADULT/ADVANCED 각각 신규 코스 진입 → 카드 렌더링 → 재생
    버튼 클릭 시 `speechSynthesis.speak()` 정상 호출까지 재확인.

- 학습 대시보드 (마이페이지 확장) (dev 병합됨)
  - `/my`에 stat tile 3개(완료한 레슨 수·학습 중인 코스 수·전체 진도율)와 영역별(어휘/읽기/쓰기/듣기/말하기)
    진도 막대그래프를 코스 리스트 위에 추가. 새 추적 데이터(streak, 점수 등) 없이 기존 `LearningProgress`
    완료 여부만으로 구성 — dataviz 스킬 가이드에 따라 설계함.
  - `recommendFocusAreas()`가 이미 계산하던 "영역별 완료/가능 레슨 수" 로직을 `computeSkillAreaCounts()`
    private 헬퍼로 뽑아 새 `getSkillAreaProgress()`와 공유(중복 제거, 기존 테스트로 회귀 확인됨).
  - 영역별 막대는 5개를 카테고리 비교(색으로 구분)가 아니라 각자 이름표 붙은 독립 진도 막대로 봐서 새
    카테고리컬 팔레트를 안 만들고 사이트에 이미 있는 단일 강조색(`.progress-bar-track`/`-fill`, 코스
    진도 카드에서 쓰던 것 그대로)만 재사용 — 팔레트 검증 스크립트 안 돌려도 됨.
  - 새 DTO `SkillAreaProgressResponse.percentage()`는 100%로 캡 — 개인 코스가 공식 레슨을 복사해서 만들어
    (다른 id, 같은 lessonType) 완료 수가 분모(공식 커리큘럼 레슨 수)보다 커질 수 있음.
  - 테스트에서 실수 하나 발견: `getCourseProgress()`의 `touchedCourseIds`가 `HashMap` 스트림에서 나와
    순서가 보장 안 되는데, 새 `getDashboardSummary` 테스트가 `courseRepository.findAllById(List.of(100L, 200L))`처럼
    특정 순서로 스텁해놔서 실행할 때마다 순서가 바뀌어 간헐적으로 실패할 뻔함 — `any()` 매처로 바꿔 해결.
  - claude-in-chrome으로 신규 회원(전부 0%)과 VOCAB만 3개 완료한 회원 둘 다 확인 — 후자는 어휘만 막대가
    차고 듣기·말하기는 0%로 남아 "균형있게 학습하라"는 메시지가 실제로 전달되는지 눈으로 판단함.

- 개인 코스 기준 판단 고도화 2단계: 진단 테스트(DIAGNOSTIC_TEST) 구현 (dev 병합됨, 우선순위와 무관하게 사용자가 바로 착수 결정)
  - 새 `question/` 패키지(package-by-feature) — `Question` 엔티티는 특정 레슨이 아니라 **대상·레벨·영역(lessonType)**에 매인 4지선다 문항(`targetType`/`level`/`lessonType`/`prompt`/`audioText`(LISTENING 전용)/`options`/`correctOptionIndex`). `options`는 `Course.focusAreas`(`Set`)와 달리 순서가 채점에 직결되는 `List`라 `@OrderColumn` 필수 — `QuestionRepositoryTest`로 순서 보존을 별도 회귀 테스트함.
  - `QuestionService.determineFocusAreas()`가 `recommendFocusAreas()`(HISTORY_BASED)와 정확히 같은 "영역별 점수 최솟값 → EnumSet" 패턴으로 채점(이번엔 커버리지가 아니라 정답률 기준). 응시 기록(원시 답안)은 저장하지 않음 — 결과로 만들어진 개인 코스 자체가 기록이라 stateless로 채점하고 끝.
  - `CourseService.createPersonalCourseFromDiagnosticTest()`가 기존 `buildPersonalCourse()` 공통 헬퍼를 세 번째 진입점으로 그대로 재사용 — SELF_SELECTED/HISTORY_BASED와 동일하게 dedup(같은 focusAreas 조합이면 기존 코스 재사용) 동작.
  - LISTENING 문항은 새 JS/CSS 없이 레슨 상세에서 쓰던 `static/js/lesson-audio.js`의 `[data-speak-text]` 이벤트 위임을 그대로 재사용(`.listen-button` 클릭 → `speechSynthesis`).
  - 화면: `course/personal-new.html`에 세 번째 옵션(진단 테스트 링크) 추가, 신규 `course/diagnostic-test.html`(문항 10개 렌더링, `answer-{questionId}` 라디오 그룹) — 제출 시 서버가 전체 미답변을 감지하면 `DiagnosticTestIncompleteException`으로 에러 메시지와 함께 폼을 그대로 되돌려줌.
  - 시드: `common/config/QuestionDataInitializer.java` 신규(80문항 = 8개 대상·레벨 조합 × 5영역 × 2문항), 배경 에이전트로 병렬 작성 후 통합.
  - **버그**: 시드 데이터를 `private static final List<Question> QUESTIONS = List.of(...)` 필드로 두었더니 테스트 스위트 전체에서 `StaleObjectStateException`이 20건 발생 — 정적 필드의 엔티티가 첫 컨텍스트 부팅 때 IDENTITY id를 한 번 배정받고 나면, 같은 JVM 안에서 컨텍스트가 여러 번 재부팅되는 테스트 환경에서 `saveAll()`이 `persist()`가 아니라 `merge()`로 라우팅되어 터짐. 필드를 `buildQuestions()` 메서드로 바꿔 호출마다 새 `List.of(...)`를 반환하도록 수정해 해결.
  - 브라우저 검증 중 두 번째로 발견한 실수(버그 아님, 내 스크립트 실수): `document.querySelector('form')`으로 폼을 골라 제출했다가 헤더 프래그먼트의 숨은 로그아웃 폼이 DOM상 더 앞에 있어 그게 대신 제출되어 로그아웃되는 걸 겪음 — 이후 라디오 인풋에서 `.closest('form')`으로 실제 대상 폼을 특정해 재검증.
  - claude-in-chrome + curl로 종단 검증: `GET /api/questions/diagnostic-test`가 정답 인덱스 없이 10문항 반환, 폼 렌더링·LISTENING 재생 버튼의 `speechSynthesis` 호출 확인, WRITING을 일부러 틀리게 답 제출 → 결과 코스의 `criteriaSource`가 "진단 테스트"로 표시되고 focusAreas에 WRITING 반영 확인(ADULT/ELEMENTARY 조합에서 실제 레슨 7개 복사까지 확인), 전부 안 답하고 제출 시 에러 메시지와 함께 폼 유지 확인.
  - **콘텐츠 갭 발견(새 버그 아님, 기존 시드 데이터의 한계)**: ADULT/BEGINNER는 공식 코스에 READING·WRITING 레슨이 아예 없음(VOCAB×3, LISTENING×1, SPEAKING×1만 존재) — 이 조합에서 진단 테스트가 READING/WRITING을 약점으로 지목하면 개인 코스가 0레슨으로 만들어짐. 기존에 "듣기·말하기는 레슨이 없어 골라도 빈 코스가 됨 — 정직하게 그대로 둠"이라고 문서화해둔 것과 같은 패턴이라 별도 수정은 안 함. 다른 조합들도 5영역 중 1~2개씩 빠져있음(예: CHILD/BEGINNER는 WRITING 없음, ADULT/ELEMENTARY·INTERMEDIATE는 READING 없음) — 콘텐츠를 더 채울 때 참고.
  - 테스트: `QuestionServiceTest`(5개, 채점·동률·미답변·문항없음), `QuestionRepositoryTest`(2개, `@OrderColumn` 순서 보존·조회 필터), `CourseServiceTest`에 DIAGNOSTIC_TEST 케이스 3개 추가.

- 공식 코스 콘텐츠 커버리지 갭 채우기 (dev 병합됨)
  - 진단 테스트 검증 중 발견한 문제(바로 위 항목)를 이어서 해결 — `SampleDataInitializer.java`를 직접 파싱해
    8개 대상·레벨 조합 중 몇 개가 5영역(어휘/읽기/쓰기/듣기/말하기) 중 몇 개씩 비어있는지 정확히 확인:
    CHILD/BEGINNER·ELEMENTARY는 WRITING 없음, CHILD/INTERMEDIATE·ADVANCED는 VOCAB 없음,
    ADULT/BEGINNER는 READING·WRITING 둘 다 없음, ADULT/ELEMENTARY·INTERMEDIATE는 READING 없음,
    ADULT/ADVANCED는 WRITING 없음 — 총 9개 코스(각 7레슨=63레슨) 부족.
  - 새 도메인/API/화면 변경 없이 순수 콘텐츠 작업이라 판단, 이전 "듣기·말하기 확장" 때와 같은 패턴으로
    배경 에이전트 2개(CHILD 4개 코스 / ADULT 5개 코스)에 제목·설명·이모지·영역은 내가 고정해서 지시하고
    레슨 7개의 실제 문장·번역만 채우게 한 뒤, 결과를 텍스트로 받아 `SampleDataInitializer.COURSE_SEEDS`의
    해당 `// ---------- TARGET / LEVEL ----------` 섹션 끝에 직접 순서대로 붙여넣음(동시 편집 충돌 없음).
  - CHILD 신규 코스는 기존 톤 그대로: BEGINNER/ELEMENTARY WRITING은 자기소개·가족·반려동물 수준의 짧은
    베껴쓰기, INTERMEDIATE VOCAB은 감정 표현(대화문 수준), ADVANCED VOCAB은 동화에 어울리는 관용구
    (`It's raining cats and dogs` 등). ADULT 신규 코스도 기존 톤 그대로: BEGINNER READING/WRITING은
    표지판·양식 수준, ELEMENTARY/INTERMEDIATE READING은 업무 이메일·보고서 수준, ADVANCED WRITING은
    논증 에세이 수준(기존 토론/인터뷰 코스와 같은 격식).
  - 통합 직후 `grep -c "new CourseSeed("` = 49(기존 40+9), `grep -c "new LessonSeed("` = 343(기존 280+63)로
    개수를 스크립트로 검증(지난번 "듣기·말하기 전체 확장" 때 코스 2개를 붙여넣다 빠뜨렸던 실수를 반복하지
    않기 위해 매번 하는 습관). 붙여넣으며 보니 클래스 상단 Javadoc이 "듣기·말하기는 ADULT/BEGINNER에만",
    "다른 조합은 준비 중"이라고 여러 세션 전 상태로 outdated돼 있던 것도 발견해 이번 참에 정정.
  - 검증: curl로 8개 조합 전부 5영역 커버리지 확인(python 스크립트로 target×level별 `lessonType` 합집합
    계산), 그리고 지난번 실제로 빈 코스가 만들어졌던 바로 그 케이스(ADULT/BEGINNER 회원이 진단 테스트에서
    WRITING을 일부러 틀리게 답 제출)를 다시 재현해 이번엔 레슨 7개가 실제로 복사되는지 확인함(`/courses/50`
    → WRITING 레슨 7개 정상 생성). `./gradlew build`/`test` 전체 통과(데이터 시딩만 바뀐 변경이라 기존
    테스트에 영향 없음, 예상대로).

- 레슨 내 이해도 확인 퀴즈 (dev 병합됨)
  - PRODUCT.md 3-1은 WRITING을 "작문 후 제출"이라 정의하지만 실제론 VOCAB/READING/WRITING 전부 채점 없는
    플래시카드 검토 + 수동 "학습 완료" 버튼뿐이었음(듣기·말하기 콘텐츠 착수 때 이미 발견해뒀던 갭).
    `ProgressService`의 기존 Javadoc도 "정답률·오답 데이터 없음, 퀴즈 미도입"이라고 스스로 이 갭을 적어뒀었음.
  - **새 콘텐츠·엔티티·테이블 없이** 레슨에 이미 있는 "English — 한글" PHRASE 문장에서 핵심 단어 하나를
    빈칸으로 만들어 매 요청마다 그 자리에서 퀴즈를 만들어냄(stateless) — 진단 테스트가 원시 답안을 저장
    안 하고 그때그때 채점하는 것과 같은 원칙. 343개 레슨 전부에 수작업 문항을 채우는 건 진단 테스트
    (80문항)보다 몇 배 큰 일이라 배보다 배꼽이 큼.
  - `LessonService`에 순수 함수로 추가: 핵심 단어는 불용어(관사/대명사/be동사 등) 제외 최장 단어(동률이면
    먼저 나온 단어), 오답 보기는 같은 코스 다른 레슨들의 PHRASE 문장에서 모음. 정답 도출 로직
    (`deriveQuizAnswer`)과 오답 포함 전체 퀴즈 생성(`buildQuiz`)이 같은 문장·단어 선택 헬퍼
    (`chooseQuizWord`)를 공유해 GET에서 보여준 문제와 POST에서 검증하는 정답이 항상 일치.
  - PHRASE 줄이 없거나(CHILD 콘텐츠 일부, "A는 Apple(사과)의 A예요" 같은 NOTE 형식) 오답 후보가 하나도
    없으면(같은 코스에 다른 PHRASE 문장이 없음) 퀴즈 없이 조용히 생략 — 기존 완료 버튼만 그대로 보임,
    하위 호환 100% 유지.
  - `LessonViewController.complete()`가 `quizAnswer` 파라미터를 받아 `deriveQuizAnswer()`와 대소문자
    무시 비교 — 틀리면(또는 퀴즈가 있는데 답이 없으면) 같은 페이지를 에러 메시지와 함께 다시 그리고
    완료 처리를 하지 않음. `/api/progress/complete`(REST API)는 이번 범위에서 제외 — 렌더링된 퀴즈를
    보고 답하는 게 전제인 기능이라 웹 화면에만 게이트를 검.
  - 화면: `lesson/detail.html`의 "학습 완료" 폼 안에 빈칸 문장 + 라디오 보기 4개 추가 — 진단 테스트 때
    만든 `.quiz-question`/`.quiz-options`/`.error-message` CSS를 그대로 재사용해 새 CSS 없음.
  - 테스트: `LessonServiceTest`에 4개 추가(핵심 단어 추출, PHRASE 없으면 빈 값, 오답 보기가 다른 레슨의
    PHRASE에서 모이는지, 오답 후보 없으면 퀴즈 생략), `LearningProgressFlowTest`에 1개 추가(틀린 답 →
    미완료+에러, 맞는 답 → 완료). 기존 PHRASE 없는 콘텐츠("내용")로 쓰인 회귀 테스트들도 그대로 통과
    확인(하위 호환 검증).
  - curl로 실서버 검증: ADULT 레슨(`/lessons/141`, "The map ___ a hidden island.")에서 오답 제출 시
    200 + "정답이 아니에요" 노출 + 미완료 확인, 정답(`showed`) 제출 시 302 리다이렉트 + "완료함 ✓" 전환
    확인. PHRASE 없는 레슨(`/lessons/1`)은 퀴즈 없이 기존처럼 렌더링되는 것도 확인.

- 그림 퀴즈 + 매일매일 단어장/단어 퀴즈 (dev 병합됨)
  - 사용자가 "아이가 풀 수 있는 그림퀴즈, 매일매일 랜덤 단어 퀴즈, 매일매일 단어장" 세 가지를 한 번에
    제안 — 셋 다 새 콘텐츠 저장 없이 만들 수 있다고 판단(그림 퀴즈는 CHILD 레슨의 기존 OpenMoji 아이콘,
    단어 퀴즈/단어장은 회원 레벨의 기존 "English — 한글" 문장을 재료로 재사용). "매일"은 **오늘 날짜를
    시드로 쓰는 결정론적 셔플**로 구현 — DB에 "오늘 뭘 냈는지" 저장 없이 같은 날엔 항상 같은 문제,
    자정 지나면 자동으로 바뀜. 직전에 만든 레슨 이해도 퀴즈(레슨 id를 시드로 쓴 것)와 같은 철학을
    날짜 시드로 확장한 것.
  - 공용 기반 2개 신규: `common/util/DailySeed`(오늘 날짜를 `epochDay`로 시드 삼아 결정론적 셔플),
    `lesson/service/QuizWordPicker`(레슨 퀴즈에서 쓰던 `extractKeyWord`/`blankOut`을 `LessonService`
    private 메서드에서 뽑아내 공용 static 유틸로 전환 + 오답 뽑기 `pickDistractors` 추가). 순수 이동이라
    기존 `LessonServiceTest` 4개가 리팩터링 후에도 그대로 통과하는지 먼저 확인하고 다음 기능으로 넘어감.
  - **그림 퀴즈**(`/quiz/picture`, 로그인 불필요 — 헤더 네비에 링크 추가): CHILD 공식 코스 전체에서
    아이콘 붙은 PHRASE 문장의 핵심 단어·아이콘 쌍을 모아(`LessonService.collectIconPairs()` 신규,
    170여 개 재료 확보 확인) 오늘의 5문제를 만듦. 그림 하나 + 보기 4개(정답 1 + 오답 3, 오답은 다른
    CHILD 레슨의 단어에서). 정답은 절대 클라이언트에 안 보냄(`PictureQuizQuestion` DTO에서 제외 —
    진단 테스트 `QuestionResponse`와 같은 원칙). 채점은 `POST /quiz/picture` → 같은 날짜 시드로
    문제를 다시 만들어 비교 → `redirect:/quiz/picture/result?score=N&total=5`(폼을 다시 채울 필요가
    없는 1회성 채점이라 리다이렉트로 단순화, 레슨 퀴즈의 재렌더링 방식과 다르게 간 이유).
  - **매일 단어장 + 단어 퀴즈**(`/my/daily`, 로그인 필요 — `/my/**`가 이미 인증 필수라 보안 설정 변경
    없음, 마이페이지에 링크 추가): 회원의 대상·레벨에 맞는 공식 코스의 PHRASE 문장 풀에서 오늘의 단어
    5개(단순 목록) + 단어 퀴즈 1개(레슨 퀴즈와 같은 빈칸 방식)를 만듦. 시드는 날짜+대상·레벨 조합
    (개인별이 아니라 "오늘 이 레벨 회원은 다 같은 문제" — 회원마다 다르게 시딩할 이유가 없어 더 단순한
    쪽 선택). `POST /my/daily/quiz` → 정오답에 따라 `?quizResult=correct|wrong` 쿼리 파라미터로
    리다이렉트, 배너로 표시.
  - 시큐리티: `SecurityConfig`에 `.requestMatchers("/quiz/**").permitAll()` 한 줄만 추가(지난번 `/js/**`
    빠뜨렸던 실수를 반복하지 않으려고 계획 단계에서부터 체크리스트에 넣어둠).
  - 스트릭/연속 학습일 추적, 정답 이력 저장, 회원별 개인화된 문제는 의도적으로 범위에서 뺌 — 전부 상태
    저장이 필요해 지금의 "완전 스테이트리스" 원칙을 벗어남. 나중에 원하면 별도로 설계.
  - 테스트: `QuizWordPickerTest`(신규, 5개 — 핵심 단어 추출·동률·불용어뿐인 문장·빈칸 치환·오답 뽑기),
    `PictureQuizServiceTest`(신규, 5개 — 문제 생성·재료 부족·결정론·채점), `DailyWordServiceTest`(신규,
    4개), `PictureQuizViewControllerTest`/`DailyQuizViewControllerTest`(신규, MockMvc — 비로그인
    접근 가능/불가 확인).
  - curl로 실서버 종단 검증: 그림 퀴즈 비로그인 접근 확인, 같은 문제가 반복 호출에도 완전히 동일한지
    확인(옵션 순서까지), 보기 4개를 각각 제출해보며 올바른 정답 하나만 점수를 올리는지 확인(브루트포스로
    "instead"가 정답임을 검증). 매일 단어장/퀴즈는 ADULT/BEGINNER로 가입 → 단어장 5개·빈칸 문제 렌더링
    확인 → 오답 제출 시 "아쉬워요" 배너 → 정답(`report`) 제출 시 "정답이에요 🎉" 배너 확인, 비로그인
    시 `/my/daily`가 `/login`으로 리다이렉트되는 것도 확인.

- 코스 완료 시 다음 코스 자동 안내 (dev 병합됨)
  - PRODUCT.md 3-3 로드맵 컨셉("완료 → 다음 레슨/코스 자동 안내")에서 "다음 레슨"(레슨 상세 이전/다음
    네비게이션)은 이미 있었지만 "다음 코스"는 없었음 — 코스를 다 들으면 그냥 끝, 다음에 뭘 들어야
    할지 화면이 알려주지 않던 갭을 채움.
  - **"다음 코스"의 정의**: 공식 코스는 `SampleDataInitializer`가 대상별로 BEGINNER→ADVANCED 순서로
    저장돼 있어 id 오름차순이 곧 로드맵 순서라는 걸 시드 코드로 확인(레벨 섹션이 그 순서로 나열돼 있고
    단일 루프로 순차 저장). `CourseRepository.search(targetType, null, null)`가 이미 그 순서로 반환.
  - `ProgressService.isCourseFullyCompleted(memberId, courseId)` 신규 — 레슨이 하나도 없는 코스는
    완료로 안 침. `getCourseProgress()`가 쓰던 "회원 진행 기록 벌크 조회 후 completed lessonId Set으로
    비교" 패턴을 재사용해 레슨별로 `isCompleted()`를 N번 부르는 것보다 쿼리를 줄임.
  - `CourseService.recommendNextCourse(memberId, currentCourseId)` 신규 — 현재 코스 다음(id 기준)부터
    로드맵을 훑어 아직 다 안 끝낸 코스 중 첫 번째를 반환.
  - **버그 하나 발견·수정(테스트로 잡음)**: 현재 코스가 로드맵에 없는 경우(개인 코스)
    `currentIndex == -1`이 되는데, `currentIndex + 1`이 0이 돼서 "다음 코스부터 훑기"가 아니라
    "로드맵 처음부터 훑기"가 돼버리는 함정이 있었음 — 개인 코스를 완료한 회원한테 엉뚱하게 로드맵
    첫 번째 공식 코스를 "다음 코스"로 추천하는 버그. `recommendNextCourse_개인_코스는_로드맵에_없어_
    빈_값을_반환한다` 테스트가 처음 실행에서 바로 잡아냄 — `currentIndex == -1`이면 명시적으로 빈 값을
    반환하는 분기를 추가해 수정.
  - `CourseViewController.detail()`에 `@CurrentMemberId`/`ProgressService` 추가 — 로그인 && 공식
    코스(`!course.isPersonal()`)일 때만 완료 여부를 확인하고, 완료면 `nextCourse` 모델 속성을 채움.
    개인 코스 상세 페이지엔 이 기능 자체를 적용 안 함(로드맵 개념이 없으므로).
  - 화면: `course/detail.html`의 `.course-header` 바로 아래에 배너 추가 — 기존 `.quiz-question` 카드
    스타일 재사용(새 CSS 없음). 다음 코스가 없으면(로드맵 끝) "이 레벨의 로드맵을 모두 마쳤어요!" 축하
    메시지로 대체.
  - REST API는 이번 범위에서 제외(화면 전용 안내 기능).
  - 테스트: `ProgressServiceTest`에 `isCourseFullyCompleted` 3개, `CourseServiceTest`에
    `recommendNextCourse` 4개(그 중 하나가 위 버그를 잡음), `LearningProgressFlowTest`에 웹 플로우
    1개(완료 전엔 배너 없음 → 레슨 완료 → 배너+다음 코스 링크 확인).
  - curl로 실서버 검증: ADULT/BEGINNER 코스(21번, VOCAB 7레슨 전부 퀴즈 게이트 있음)의 레슨을 전부
    브루트포스로 완료 → 코스 상세에 "🎉 이 코스를 모두 완료했어요!" + 다음 코스(22번, 듣기 코스) 링크
    확인, 그 링크가 실제로 200으로 열리는지 확인. 완료 전엔 배너 없음도 확인.

- 복습 기능 + 그림 퀴즈 콘텐츠 보정 + N+1 쿼리 최적화 (dev 병합됨, 사용자가 남은 항목 세 개를 한 번에 지시)
  - **복습(간격 반복)**: PRODUCT.md가 MVP 이후 항목으로 명시해뒀던 마지막 미구현 기능. 새 추적 데이터
    없이 기존 `LearningProgress.completedAt`만 재사용 — 엔티티의 `complete()` 자체가 멱등성 가드 없이
    무조건 `completedAt = now()`로 덮어쓴다는 걸 확인하고(멱등성은 `ProgressService.complete()`가
    서비스 레벨에서만 보장), 이걸 그대로 "복습 완료 = completedAt을 지금으로 밀어내기"에 재사용함.
    `LearningProgressRepository`에 파생 쿼리(`findByMemberIdAndCompletedTrueAndCompletedAtBeforeOrderByCompletedAtAsc`)
    추가, `ProgressService.getLessonsDueForReview()`(3일 지난 완료 레슨을 오래된 순 최대 10개)/
    `markReviewed()` 신규. 화면: `GET/POST /my/review` — 새 `ReviewViewController`(`/my/**`가 이미
    인증 필수라 시큐리티 설정 변경 불필요), `templates/my/review.html`, 마이페이지에 링크 추가.
  - **그림 퀴즈 콘텐츠 보정**: `LessonService.collectIconPairs()`가 CHILD 전체 레벨(BEGINNER~ADVANCED)
    콘텐츠를 다 섞어 재료로 쓰던 걸 BEGINNER·ELEMENTARY로 제한 — 고급 관용구 레슨의 추상적인 단어가
    그림과 안 맞게 짝지어지던 문제 해결. 실서버에서 재검증: 이전엔 보기에 "instead"/"exciting" 같은
    단어가 섞여 나왔는데, 수정 후엔 "school"/"family"/"water"/"books" 같은 구체적인 저학년 어휘만 나옴.
  - **N+1 쿼리 최적화**: `LessonRepository.findByCourseIdIn(Collection<Long>)` 신규(파생 쿼리) —
    "코스 목록을 순회하며 코스마다 레슨을 따로 조회"하던 5곳(`CourseService.buildPersonalCourse`,
    `ProgressService.getCourseProgress`/`computeSkillAreaCounts`, `DailyWordService.collectPhrasePool`,
    `LessonService.collectIconPairs`)을 전부 "코스 id 리스트로 한 번에 조회 후 `groupingBy`로 맵
    구성" 패턴으로 교체. `QuestionRepository.findByTargetTypeAndLevel`도 파생 쿼리에서
    `@Query(... left join fetch q.options ...)`로 바꿔 진단 테스트 80문항 조회 시 문항마다 따로
    나가던 `question_option` select를 없앰 — `@OrderColumn` 순서가 fetch join에서도 보존되는지
    기존 `QuestionRepositoryTest`로 회귀 확인.
  - **테스트 작업의 대부분은 새 테스트보다 기존 테스트 스텁 수정** — `CourseServiceTest`/
    `ProgressServiceTest`/`DailyWordServiceTest`가 전부 `findByCourseIdOrderByOrderNoAsc(단일id)`를
    스텁하고 있어서 배치 조회로 바꾸자마자 11개 테스트가 한꺼번에 깨짐(예상된 일). 하나씩 돌려보며
    `findByCourseIdIn(...)` 기준으로 스텁을 고쳐 전부 통과시킴.
  - 실서버 검증: `build/bootRun.log`의 SQL 로그로 그림 퀴즈 요청 시 레슨 조회가 코스 개수(12개)만큼이
    아니라 `course_id in (?, ?, ..., ?)` 배치 쿼리 1번으로 줄어든 것 확인. 방금 완료한 레슨은
    `/my/review`에 안 뜨는지(3일 안 지남) 확인, `POST /my/review/{id}` 호출이 에러 없이 정상
    리다이렉트되는지 확인 — "3일 지난 완료"라는 조건 자체는 실시간으로 만들 수 없어 그 경계 로직은
    `ProgressServiceTest`의 Mockito 기반 테스트로 검증(실서버에서 시간을 조작할 순 없음).
  - 테스트: `LessonServiceTest`에 `collectIconPairs` 신규(BEGINNER/ELEMENTARY만 쓰는지, ADVANCED/
    INTERMEDIATE는 아예 조회 안 하는지 `verify(never())`로 확인), `ProgressServiceTest`에
    `getLessonsDueForReview`/`markReviewed` 4개, `LearningProgressFlowTest`에 비로그인
    `/my/review` 리다이렉트 확인 1개.

- 회원가입 시 레벨 배치 테스트 (dev 병합됨)
  - PRODUCT.md 7 "열린 질문"("레벨 판정: 자가 선택 vs 간단한 레벨 테스트?")에 답함 — 자가 선택은
    그대로 두고, 그 옆에 "레벨을 모르겠어요 → 간단 테스트" 선택지를 추가(양자택일 아님).
  - 새 문항을 만들지 않음 — 진단 테스트용으로 이미 시딩된 80개 `Question`을 그대로 재사용,
    `QuestionService.getLevelPlacementQuestions(targetType)`가 대상별로 레벨마다 앞의 2문항씩(영역
    무관) 뽑아 총 8문항 배치 테스트를 만듦. `recommendLevel(targetType, answers)`는 BEGINNER부터
    순서대로 레벨별 정답률을 확인해 과반 이상 맞힌 마지막 레벨을 추천 — 회원이 아직 존재하지 않는
    시점(가입 전)이라 기존 `determineFocusAreas`(레벨이 이미 정해졌다고 가정)와 달리 레벨 자체를
    추천하는 새 메서드로 분리. 미답변 문항은 오답 처리(진단 테스트처럼 전부 답해야 한다는 강제 없음 —
    판돈이 낮은 "제안"이라는 포지셔닝).
  - 화면: `/members/new/level-test`(대상 없이 접근하면 초등/성인 미니 선택 페이지, 대상 지정 시 8문항
    테스트 — 기존 `course/diagnostic-test.html`과 똑같은 구조·CSS 재사용) 신규 `LevelPlacementViewController`
    (`member` 패키지). 제출하면 `/members/new?target=X&recommendedLevel=Y`로 리다이렉트.
  - `MemberViewController.signUpForm()`이 `target`/`recommendedLevel` 쿼리 파라미터를 받아 가입 폼에
    "테스트 결과 추천 레벨: OOO" 배너를 보여주고, `memberType`/`level` select의 `th:selected` 조건에
    `(form == null and recommendedTarget == type.name())` 분기를 추가해 최초 진입 시 추천값을 미리
    선택해둠(검증 실패 재렌더링일 땐 기존처럼 `form` 값이 우선 — 조건 순서로 자연히 처리됨).
  - 시큐리티: `/members/new/level-test`(GET+POST 둘 다) permitAll 추가 — 가입 전 접근이라 로그인 불필요,
    `/members/new`처럼 정확한 경로 매칭이라 `/members/new/**`로 자동 커버 안 됨을 확인하고 명시적으로 추가.
  - 테스트: `QuestionServiceTest`에 5개(레벨당 2문항 총 8개, 중간에 막히면 그 직전 레벨 추천, 전부
    통과 시 ADVANCED, BEGINNER부터 막혀도 BEGINNER 유지, 미답변은 오답 처리), 신규
    `LevelPlacementViewControllerTest`(비로그인 접근·대상 없을 때 미니 페이지·제출 후 리다이렉트).
  - curl로 실서버 종단 검증: ADULT 대상으로 BEGINNER·ELEMENTARY는 정답, INTERMEDIATE는 일부러 오답
    제출 → `recommendedLevel=ELEMENTARY`로 리다이렉트 확인 → 가입 폼에 배너 + ADULT/ELEMENTARY가
    실제로 `selected` 속성과 함께 미리 선택된 것 확인 → 그대로 가입해 실제 ELEMENTARY 레벨 회원이
    만들어지는 것까지 확인. 쿼리 파라미터 없는 기존 가입 폼도 그대로 잘 열리는지(하위 호환) 확인.

- 회원 프로필 관리 (dev 병합됨)
  - 가입 후 닉네임·레벨·비밀번호를 바꿀 방법이 전혀 없던 기본 기능 갭을 채움. 이메일(로그인 아이디)과
    회원 유형(초등/성인 — 콘텐츠 카탈로그 자체가 갈리는 축)은 이번 범위에서 제외, 세 가지만 다룸.
  - `Member` 엔티티에 `changeNickname`/`changeLevel`/`changePassword` 상태 변경 메서드 신규 추가 —
    지금까지 세터 없이 `@Builder`로만 생성하던 엔티티에 처음으로 변경 메서드가 생김(CLAUDE.md
    컨벤션 "세터는 만들지 않는다, 상태 변경은 의미 있는 메서드로"를 그대로 따름).
  - 비밀번호 변경은 가입 때와 같은 `PasswordEncoder`(BCrypt) 빈 재사용 — `matches()`로 현재 비밀번호
    검증 후 `encode()`로 새 비밀번호 해시. 신규 `InvalidPasswordException`(현재 비밀번호 틀림).
  - **확인해둔 동작(버그 아님)**: `MemberPrincipal`이 로그인 시점의 불변 스냅샷이라 비밀번호를 바꿔도
    지금 로그인된 세션엔 영향이 없다 — 다음 로그인부터 새 비밀번호가 적용됨. 세션을 강제로 끊는 로직은
    이번 범위에서 만들지 않음(과한 엔지니어링으로 판단). curl로 직접 확인: 비밀번호 변경 직후에도 같은
    세션으로 계속 `/my` 접근 가능, 로그아웃 후 이전 비밀번호로는 로그인 실패·새 비밀번호로는 성공.
  - 화면: `GET/POST /my/profile`(기본 정보 폼: 닉네임 입력 + 레벨 select, 이메일은 읽기 전용),
    `POST /my/profile/password`(현재/새 비밀번호) — 신규 `MemberProfileViewController`(`/my/**`가
    이미 인증 필수라 시큐리티 설정 변경 불필요). 성공 시 `?updated`/`?passwordChanged` 쿼리 파라미터로
    리다이렉트해 배너 표시, 검증 실패·`InvalidPasswordException`은 가입 폼과 같은 패턴으로 폼 재렌더링.
    마이페이지에 "프로필 수정 →" 링크 추가.
  - 테스트: `MemberServiceTest`에 5개(닉네임·레벨 변경, 존재하지 않는 회원 예외, 비밀번호 변경 성공/실패
    각각 해시 값·예외 확인), 신규 `MemberProfileViewControllerTest`(비로그인 리다이렉트, 정상 수정,
    잘못된 현재 비밀번호로 폼 유지).
  - curl로 실서버 종단 검증: 닉네임·레벨 변경 후 마이페이지에 즉시 반영 확인 → 비밀번호 변경 시 틀린
    현재 비밀번호로 에러 확인 → 맞는 현재 비밀번호로 변경 성공 → 같은 세션 계속 유효 확인 → 로그아웃 →
    이전 비밀번호 로그인 실패·새 비밀번호 로그인 성공까지 전부 확인.

- 코스 검색 (dev 병합됨)
  - 공식 코스가 58개로 늘어난 뒤에도 대상·레벨·영역 3개 필터만 있고 제목·설명으로 자유 검색할 방법이
    없던 갭을 채움. `CourseRepository.search()`에 기존 `targetType`/`level`/`lessonType`과 같은
    `(:param is null or ...)` nullable 패턴으로 4번째 `keyword` 파라미터만 추가(새 쿼리 메서드 불필요) —
    `lower(title) like lower(concat('%',:keyword,'%')) or lower(description) like ...`로 제목·설명
    동시 매칭, 대소문자 무관.
  - `search()` 시그니처가 바뀌면서 호출부 6곳(`CourseService.list/buildPersonalCourse/recommendNextCourse`,
    `ProgressService.computeSkillAreaCounts`, `LessonService.collectIconPairs`,
    `DailyWordService.collectPhrasePool`) 전부 마지막 인자에 `null`만 추가— 필터 조합 로직 자체는 안 건드림.
    `CourseViewController`/`CourseApiController`의 `list()`만 실제로 `keyword` 파라미터를 받아 전달.
  - 화면: `course/list.html`의 `filter-form`에 검색 입력 추가. 레벨 경로(`level-path`)·영역 탭
    (`skill-tabs`)의 기존 필터 링크 11개 전부에 `keyword=${selectedKeyword}`를 같이 실어, 검색어를
    유지한 채로 다른 필터를 바꿀 수 있게 함(하나라도 빠뜨리면 그 링크를 탈 때만 검색어가 조용히 사라지는
    버그가 생기므로 grep으로 전수 확인).
  - 테스트: 기존 `CourseRepositoryTest`/`CourseServiceTest`/`ProgressServiceTest`/`DailyWordServiceTest`/
    `LessonServiceTest`의 `search(...)` 호출 스텁 22곳에 4번째 인자 추가(대부분 `null`, 동작 변경 없음).
    `CourseRepositoryTest`에 키워드 검색 신규 테스트 추가(제목/설명 부분·대소문자 무관 매칭, 필터 동시
    적용, 매칭 없으면 빈 결과). `./gradlew build` 전체 통과 확인.
  - curl로 실서버 검증: `GET /api/courses?keyword=...`가 제목/설명에 매칭되는 코스만 반환, 대상 필터와
    동시 적용, `/courses` 화면에서 검색어가 `input[name=keyword]`에 유지되고 레벨 경로 링크의 href에도
    `keyword` 쿼리 파라미터가 그대로 붙어나오는 것 확인, 매칭 없는 검색어에 "조건에 맞는 코스가
    없습니다" 빈 상태 메시지 확인.

- 실제 음성인식 SPEAKING (dev 병합됨)
  - PRODUCT.md 7 "열린 질문"에 계속 미결정으로 남아있던 "말하기 평가: 발음 점수화 범위와 방식?"에 답함.
    지금까지 SPEAKING 레슨은 TTS로 문장을 들려주고 "따라 말해보기" 문구로 유도만 할 뿐, 실제로 말했는지
    확인할 방법이 전혀 없었음. 브라우저 내장 `SpeechRecognition`(Web Speech API)으로 실제 발화를 텍스트로
    받아 목표 문장과 비교하는 "🎙️ 내 목소리로 확인하기" 버튼을 SPEAKING 레슨에 추가.
  - **서버 변경 전혀 없음** — 이 프로젝트 유일한 클라이언트 JS `static/js/lesson-audio.js`(기존 TTS
    재생 로직)에 같은 이벤트 위임 패턴으로 두 번째 리스너만 추가. 정답 판정은 인식된 텍스트와 목표
    문장을 정규화(소문자·구두점 제거·공백 정리) 후 완전 일치로 비교 — 이 프로젝트 기존 퀴즈들과 같은
    수준의 단순함, 새 채점 알고리즘 없음. 발음 점수화·음소 비교·녹음 저장은 과한 엔지니어링으로 판단해
    범위 제외, 정답/오답 이분법만.
  - "학습 완료" 처리와는 의도적으로 분리 — 음성 확인은 연습 보조 도구일 뿐 게이트가 아니라서, 마이크
    미지원 브라우저나 권한 거부 사용자도 완료 버튼은 그대로 누를 수 있음.
  - `window.SpeechRecognition || window.webkitSpeechRecognition`로 기존 `speechSynthesis` 지원 체크와
    같은 패턴으로 미지원 브라우저를 조용히 처리(안내 문구만 표시). CSS는 기존 `--color-accent`(정답)/
    `#dc2626`(오답, `.error-message`와 동일)를 재사용해 새 팔레트 검증 없이 결과 배지 스타일링.
  - **claude-in-chrome 검증 중 발견**: 실제 Chrome은 `window.SpeechRecognition`(무접두사)과
    `window.webkitSpeechRecognition`을 **둘 다** 노출한다 — 헤드리스 자동화 환경에서 마이크 목킹을 위해
    `webkitSpeechRecognition`만 오버라이드했더니 코드가 `SpeechRecognition || webkitSpeechRecognition`
    순서로 무접두사를 먼저 골라 실제 API(마이크 권한 없어 응답 없음)를 계속 타는 문제가 있었음 — 둘 다
    같이 오버라이드해서 해결, 실제 프로덕션 코드는 문제 없음(정상적인 폴백 순서).
  - JS 테스트 프레임워크가 이 프로젝트에 없음을 재확인(`lesson-audio.js` 최초 도입 때와 동일 결론) —
    이번에도 자동 테스트 없이 claude-in-chrome으로 `recognition.onresult`/`onerror` 콜백을 모킹 이벤트로
    강제 트리거해 정답("Good morning!" 그대로 인식 → ✅ 초록 배지)·오답("Good evening" 인식 → 빨간 안내+
    인식된 문장 노출)·미지원 브라우저(둘 다 undefined 처리 → 안내 문구) 세 경로 전부 확인, LISTENING
    레슨엔 마이크 버튼이 아예 렌더링 안 되는 것도 확인(`document.querySelectorAll('[data-check-text]')`
    로 개수 0 검증), 콘솔 에러 없음 확인.

- 학습 스트릭(연속 학습일) 추적 (dev 병합됨)
  - "매일매일 단어장/그림 퀴즈" 기능 때 "새 추적 데이터 필요해서 stateless 원칙 벗어남"이라며 범위에서
    뺐던 항목("나중에 원하면 별도로 설계"로 남겨뒀던 것) — 다시 보니 `LearningProgress.completedAt`이
    이미 레슨 완료 시각을 저장하고 있어서 **새 테이블 없이** 날짜 단위로 묶기만 하면 계산 가능함을 확인.
  - `ProgressService.getCurrentStreak(memberId)` 신규 — `findByMemberId` 결과에서 완료된 항목의
    `completedAt`을 `LocalDate`로 묶어 오늘부터 거꾸로 훑으며 연속 일수를 셈. 오늘 아직 학습을 안 했어도
    어제까지 이어져 있으면 스트릭이 살아있는 것으로 보는 하루 유예(`STREAK_GRACE_PERIOD_DAYS`)를 둠 —
    그렇지 않으면 자정 넘어가는 순간 스트릭이 매번 리셋되는 것처럼 보여 사용자 경험이 나쁨.
  - `DashboardSummaryResponse`에 `currentStreak` 필드 추가, `getDashboardSummary()`가 함께 계산해
    반환. 마이페이지 `.stat-tile-row`(기존 3개: 완료 레슨/학습 중인 코스/전체 진도율)에 4번째 타일로
    노출 — `grid-template-columns: repeat(auto-fit, minmax(150px, 1fr))`라 새 CSS 없이 자동 배치.
    0보다 크면 🔥 이모지를 붙임.
  - 스트릭 최고 기록·알림·프리즈(연속 유지권) 같은 게임화 장치는 전부 새 상태가 필요해 범위 제외 —
    "현재 스트릭"만 stateless로 즉석 계산.
  - 테스트: `ProgressServiceTest`에 `getCurrentStreak` 4개(연속 3일, 오늘 공백이어도 어제까지면 유지,
    어제·오늘 둘 다 공백이면 끊김, 기록 없음) — `completedAt`을 과거 날짜로 만들기 위해 리플렉션 기반
    `withCompletedAt` 헬퍼 신규 추가(먼저 `complete()`로 completed=true를 만든 뒤 시각만 덮어씀).
    기존 `getDashboardSummary_*` 2개에 `currentStreak` 단언 추가(레코드 필드 추가라 하위 호환, 기존
    스텁 재사용 가능해 추가 스텁 불필요).
  - curl 실서버 검증 중 시행착오: 회원가입 폼의 CSRF hidden input이 (Security 도입 때 이미 문서화해둔
    대로) 자동 주입 값과 수동 입력 값 2개가 나란히 렌더링되는데, `grep -o '_csrf...'`가 둘을 이어붙여
    깨진 토큰을 만드는 바람에 첫 두 번의 가입 시도가 403으로 실패 — `head -1`로 첫 번째 값만 골라
    해결. 레슨 완료도 VOCAB 레슨에 퀴즈 게이트가 걸려 있어 무작정 완료 POST가 200(미완료)으로 되돌아옴을
    보고 4개 보기를 브루트포스로 순회해 정답을 찾은 뒤에야 완료(302) 확인 — 이후 `/my`에서 "1 🔥"로
    정확히 반영되는 것 확인.

- 반응형/모바일 UI 점검 (dev 병합됨)
  - 지금까지 모든 UI 검증이 데스크톱 브라우저 기준이었던 것을 처음으로 좁은 화면 관점에서 점검함.
  - **가장 치명적으로 발견한 문제: 17개 템플릿 전부에 `<meta name="viewport">` 태그가 아예 없었음**
    (`grep -rL viewport templates/**/*.html`로 전수 확인). 이 태그 없이는 실제 모바일 브라우저가 페이지를
    ~980px 가상 레이아웃 뷰포트로 렌더링한 뒤 화면 크기에 맞춰 축소해버려서, 아래에서 확인한 반응형 CSS가
    있으나 마나가 됨(사용자는 전체 페이지가 작게 줄어든 채로 핀치 줌을 해야 함). 개별 페이지가 각자
    `<head>`를 갖는 구조라(레이아웃 다이얼렉트 미사용) 16개 페이지 템플릿(`fragments/layout.html`은
    `<head>` 자체가 없어 제외) 전부에 `<meta charset>` 바로 아래 한 줄씩 추가.
  - claude-in-chrome의 `resize_window`가 이 환경에서 500px 밑으로는 안 먹혀서(320px 요청해도 실제
    `window.innerWidth`는 500 유지) 500px 폭에서 랜딩·코스목록·코스상세·레슨상세(이미지 플래시카드·
    텍스트카드 둘 다)·로그인·회원가입·마이페이지 대시보드(스탯 타일 4개·영역별 막대)·그림퀴즈·개인코스
    만들기 등 주요 화면을 스크린샷으로 전수 확인 — **전부 깨짐 없이 잘 줄어듦**. `.card-grid`/
    `.lesson-content`/`.stat-tile-row`가 전부 `auto-fit`/`auto-fill` 그리드였고 `.lesson-phrase.
    has-image`도 원래 `flex-direction: column`(세로 카드)이라 폭에 안 흔들리고, 헤더도 이미
    `flex-wrap: wrap`이 있었음 — 이 프로젝트 CSS는 처음부터 반응형에 상당히 우호적으로 짜여 있었다는 것을
    확인.
  - **실제로 찾은 유일한 구체적 CSS 버그**: `.level-path`(코스 목록의 입문→초급→중급→고급 여정 경로
    pill들)만 `flex-wrap`도 `overflow-x`도 없었음(`filter-form`/`skill-tabs`는 이미 `flex-wrap: wrap`이
    있어 문제 없었음). 500px에서는 pill 5개+connector 4개가 간신히 들어맞았지만(여유 거의 없음), 자바
    스크립트로 컨테이너 폭을 320px로 강제 축소해 시뮬레이션한 결과 실제로 넘침(`scrollWidth 393 >
    clientWidth 280`)을 확인 — `body`/`html`엔 `overflow-x: hidden` 안전장치도 없어서 방치하면 페이지
    전체가 옆으로 밀리는 흔한 모바일 버그로 이어질 뻔했음. `.level-path`에 `overflow-x: auto` +
    `-webkit-overflow-scrolling: touch`를 추가하고 `.level-node`/`.level-connector`에 `flex-shrink: 0`을
    추가해 좁은 화면에서 pill이 찌그러지는 대신 이 경로 하나만 가로 스와이프되게 함(줄바꿈 대신 스크롤을
    택한 이유: "여정" 은유상 커넥터 선이 줄바꿈되면 어색함). 같은 320px 시뮬레이션으로 수정 후 재확인 —
    `.level-path`는 스크롤 가능(`overflowX: 'auto'`, 실제 스크롤바 렌더링 확인)한데 `document.
    documentElement`(페이지 전체)는 넘치지 않는 것 확인.
  - 500px 미만 실기기 검증은 이 환경의 `resize_window` 하한 때문에 직접은 불가능했지만, 두 수정 모두
    업계 표준 패턴(뷰포트 메타 태그)과 이미 사이트에 쓰이던 패턴(가로 스크롤 컨테이너)이라 실기기 없이도
    신뢰도 높다고 판단.
  - 서버 로직 변경 없음(템플릿 `<head>`·CSS만) — `./gradlew build` 기존 테스트 그대로 통과 확인용.

- 주간 학습 활동 히트맵 (dev 병합됨)
  - 스트릭(연속 학습일) 기능이 "며칠 연속"이라는 숫자 하나만 보여줬는데, "최근 7일 동안 어느 요일에
    얼마나 학습했는지"를 한눈에 보여주는 시각화를 마이페이지에 추가함 — 스트릭과 같은 데이터
    (`LearningProgress.completedAt`)를 다른 관점(요일별 강도)으로 보여주는 자연스러운 확장, 새 추적
    데이터 없음.
  - `ProgressService.getWeeklyActivity(memberId)` 신규 — 완료 기록을 `LocalDate`별로 묶어 최근 7일
    (오늘 포함, 오래된 날짜부터)을 배열로 반환. 요일 라벨("월"~"일")은 Thymeleaf `#temporals.format()`
    (로케일 의존적이라 영문으로 나올 위험)을 안 쓰고 서비스에서 `DayOfWeek`별로 직접 한글 매핑 —
    이 프로젝트가 지금까지 한글 라벨을 전부 Java 쪽에서 하드코딩해온 관례를 따름. 신규 DTO
    `DailyActivityResponse(date, dayLabel, completedCount)`.
  - dataviz 스킬 가이드에 따라 색상은 카테고리컬 팔레트가 아니라 **단일 hue(`--color-primary`)의 알파값
    4단계**로만 인코딩(요일별 학습량은 크기 비교이지 카테고리 비교가 아니므로) — 팔레트 검증 스크립트는
    카테고리컬/다이버징 대상이라 이번엔 스킵. **모바일 감사에서 배운 교훈을 바로 적용**: 값을 hover
    툴팁으로만 보여주면 터치 기기에서 안 보이므로, 완료 개수를 각 칸 아래 숫자로 항상 노출하고 색 강도는
    보조 신호로만 씀.
  - `.activity-heatmap`은 `.stat-tile-row`(auto-fit 그리드)와 달리 요일 순서가 반드시 월→일로 유지돼야
    해서 `auto-fit`으로 줄바꿈되면 안 됨 — grid 대신 `flex` 고정 7칸으로 구현.
  - 화면: `my/dashboard.html`의 스탯 타일 바로 아래 배치(스트릭과 인접해 같은 데이터의 다른 관점임을
    시각적으로 연결). `MyPageController`에 `weeklyActivity` 모델 속성 추가.
  - 테스트: `ProgressServiceTest`에 `getWeeklyActivity` 2개(연속 3일치 완료 기록이 정확한 순서·개수로
    나오는지, 기록 없으면 7일 전부 0인지) — 스트릭 테스트 때 추가해둔 `withCompletedAt`/`newCompleted`
    헬퍼 그대로 재사용.
  - curl+claude-in-chrome 실서버 검증: 신규 회원은 7칸 전부 `level-0`/count 0 확인 → VOCAB 레슨 2개를
    브루트포스로 완료(레슨마다 매 요청 새 `Random()`으로 오답 보기가 바뀌어(`LessonService.buildQuiz`)
    똑같은 4지선다도 요청마다 다르게 보임을 재확인, 정답 텍스트 자체는 결정론적이라 옵션이 바뀌어도
    브루트포스로 문제없이 통과) → 오늘 날짜(일요일) 칸이 `level-2`/count 2로 정확히 반영, 요일 순서도
    월화수목금토일로 오늘(일)이 맨 끝에 오는 것 확인. 500px 모바일 폭에서 7칸이 줄바꿈 없이 들어가고
    페이지 전체 가로 스크롤도 없는 것(`scrollWidth === clientWidth`) 확인.

- 다크모드 (dev 병합됨)
  - 대부분의 색을 이미 `:root`의 CSS 커스텀 프로퍼티 9개로 관리하고 있었다는 걸 확인 — 하드코딩된 hex
    39곳을 전수 확인해 실제로 다크 대응이 필요한 건 셋뿐이라고 판단(html/body 배경 그라디언트,
    `.error-message`/`.speech-result.incorrect`의 빨강, `button:disabled`의 회색). 새 시맨틱 변수
    3개(`--color-danger`/`--color-disabled-bg`/`--color-disabled-text`) 추가 후 이 셋을 변수로 치환.
    나머지 뱃지·영역 태그·플래시카드 이미지 밴드 등은 자기 배경+자기 글자색을 다 가진 "자기 완결적
    파스텔 칩"이라 페이지 테마와 무관하게 그대로 잘 보인다고 보고 범위에서 뺌.
  - `@media (prefers-color-scheme: dark) :root:not([data-theme="light"])`(시스템 설정 자동 반영)과
    `:root[data-theme="dark"]`(수동 토글, 시스템 설정보다 우선) 두 선택자에 같은 다크 값을 나란히 선언.
    `html`/`body`의 배경 그라디언트도 같은 패턴으로 다크 버전 추가.
  - 신규 `static/js/theme-toggle.js` — `lesson-audio.js`와 같은 이벤트 위임 스타일(전역
    `document.addEventListener('click', ...)`), `localStorage.theme`에 저장하고 `<html>`의
    `data-theme` 속성을 토글. `SecurityConfig`가 이미 `/js/**`를 permitAll로 열어둬서 시큐리티 설정
    변경 불필요. 헤더(`fragments/layout.html`)에 🌙/☀️ 토글 버튼 + 스크립트 태그 추가 —
    `header`/`footer` 프래그먼트가 모든 페이지에 공유되는 구조 덕분에 지난 뷰포트 메타 태그 작업과
    달리 17개 템플릿을 안 건드리고 이 파일 하나로 끝남. FOUC(전환 시 깜빡임) 완전 제거는 17개
    템플릿 `<head>`에 인라인 스크립트가 필요해 과한 엔지니어링으로 판단하고 범위에서 뺌 — 시스템
    다크 모드 자동 적용 자체는 순수 CSS 미디어 쿼리만으로 되므로 이 트레이드오프의 영향은 "수동
    토글 직후 새로고침할 때"로 한정됨.
  - **버그 1(구현 중 발견)**: 처음엔 `<script>` 태그를 `</header>` 바로 뒤, `th:fragment="header"`가
    붙은 `<header>` 엘리먼트의 형제로 넣었다가 완전히 렌더링에서 빠지는 걸 발견 — Thymeleaf의
    `th:replace="~{fragments/layout :: header}"`는 `th:fragment` 속성이 붙은 엘리먼트 "자기 자신과
    그 자손"만 선택하지, 형제 엘리먼트는 프래그먼트에 포함되지 않는다. `<script>`를 `<header>` 내부
    (`</nav>` 뒤, `</header>` 앞)로 옮겨 자손이 되게 해서 해결 — curl로 응답 HTML에 스크립트 태그가
    실제로 나오는지 확인하며 잡음.
  - **버그 2(실브라우저 검증 중 발견)**: `.badge`/`.level-pill`이 배경은 `var(--color-primary-light)`
    (다크 모드에서 남색으로 바뀜)를 쓰면서 글자색은 `#3730a3`(진보라)를 하드코딩하고 있어서, 다크
    모드에서 남색 배경 위에 진보라 글자가 겹쳐 거의 안 보이는 대비 사고가 날 뻔했음. `.personal-course-
    badge`도 배경은 `var(--color-accent-light)`, 글자는 `#065f46`(진녹) 하드코딩으로 같은 문제.
    처음 계획에서 "자기 완결적 파스텔 칩이라 안전하다"고 판단했던 게 틀렸던 경우 — 실제로는 배경만
    변수화돼 있고 글자색은 하드코딩인 "반쪽짜리" 칩이었음. `--color-badge-text`/`--color-badge-
    accent-text` 시맨틱 변수 신규 추가(라이트: 기존 값 그대로, 다크: 밝은 인디고/민트로) 후 세 곳
    전부 치환해 해결. **교훈**: `var(--color-*-light)`를 배경으로 쓰는 곳은 전부 grep으로 찾아
    글자색도 변수인지 하나씩 확인해야 한다 — "칩처럼 보이는 것"과 "실제로 자기 완결적인 것"은 다름.
  - **자동화 환경 이슈(버그 아님)**: claude-in-chrome의 `screenshot`/`find` 도구가 이 세션에서 계속
    타임아웃(확장 프로그램 자체의 메시지 채널 노이즈로 추정, 콘솔에 찍히는
    "message channel closed" 예외와 함께). `javascript_tool`로 `.click()`/`dispatchEvent`를 쏴도
    실제 페이지 스크립트의 `document.addEventListener('click', ...)` 리스너까지 이벤트가 전달되지
    않는 것도 확인(진단용 리스너로 재현) — 기존 메모리에 적어둔 "claude-in-chrome 클릭/타이핑 불안정"
    문제의 연장선. 우회: 토글 버튼 클릭을 시뮬레이션하는 대신 `document.documentElement.setAttribute
    ('data-theme','dark')`/`localStorage.setItem('theme','dark')`를 직접 호출해 하위 로직(CSS 변수
    적용, localStorage 기반 유지)만 골라 검증 — 클릭 핸들러 자체의 등록 여부는 코드 리뷰로 신뢰(기존
    검증된 `lesson-audio.js`와 동일한 패턴).
  - curl+claude-in-chrome 실서버 검증: 다크 속성 적용 시 카드/뱃지/헤더/네비 전부 `getComputedStyle`로
    올바른 다크 변수 값이 적용되는지 확인, `localStorage`에 저장된 테마가 페이지 이동 후에도 유지되는지
    (새 페이지 로드 시 스크립트가 `data-theme` 재적용) 확인, 개인 코스를 실제로 만들어
    `.personal-course-badge`가 새 변수로 올바르게 렌더링되는지 확인.

- 관리자 콘텐츠 관리 화면 (dev 병합됨)
  - 지금까지 공식 코스·레슨은 전부 `SampleDataInitializer.java`를 직접 편집(배경 에이전트로 텍스트를
    받아 손으로 붙여넣기)하는 방식으로만 늘려왔음. 코스·레슨을 화면에서 만들고 고치는 최소 관리자 UI를
    추가해 앞으로의 콘텐츠 작업을 코드 편집 없이 할 수 있게 함.
  - **회원 역할(Role) 신규 도입** — 지금까지 `MemberPrincipal.getAuthorities()`가 모든 회원에게 고정된
    `ROLE_MEMBER` 하나만 줬음(역할 개념 자체가 없었음). `member/domain/MemberRole`(USER/ADMIN) 신규,
    `Member`에 `role` 필드 추가(생성자에서 null이면 USER 기본값 — 기존 `Member.builder()...build()`
    호출부 전부 무변경으로 계속 동작). `MemberPrincipal`이 ADMIN이면 `ROLE_ADMIN`도 같이 부여.
    회원가입 폼(`MemberCreateRequest`/`MemberService.signUp()`)은 손대지 않아 자가 ADMIN 승격은 불가능.
  - `common/config/AdminAccountInitializer` 신규(`CommandLineRunner`, `SampleDataInitializer`와 같은
    멱등성 패턴) — H2가 인메모리라 재부팅마다 사라지므로 고정 관리자 계정을 매번 시딩.
    **로그인 정보: `admin@eduplatform.com` / `Admin1234!`.**
  - **관리자 화면을 만드는 김에 보안 허점 하나 같이 발견·마감**: `POST /api/courses`(공식 코스 생성
    API)가 Phase 6 인증 도입 때 개인 코스 관련 엔드포인트만 챙기고 이건 빠뜨려서 지금까지 **인증 없이
    열려 있었음**. `apiSecurityFilterChain`에 `hasRole("ADMIN")` 추가로 마감, `webSecurityFilterChain`에
    `/admin/**` → `hasRole("ADMIN")` 규칙 추가(다른 특정 규칙들처럼 `anyRequest()`보다 먼저).
  - 코스 생성은 기존 `CourseCreateRequest`/`CourseService.create()`를 그대로 재사용(필드가 이미
    정확히 일치). 수정만 신규 — `Course.updateDetails(...)` 상태 변경 메서드(세터 없음 컨벤션) +
    `CourseService.updateCourse()`.
  - 레슨은 지금까지 조회 메서드만 있고 쓰기 경로가 전혀 없었음(전부 `SampleDataInitializer`가
    `lessonRepository.save()`로 직접 저장) — `Lesson.updateDetails(...)` 신규, 신규 DTO
    `LessonAdminRequest`(생성·수정 폼 공용), `LessonService.createLesson/updateLesson/deleteLesson`
    신규. 레슨 수정 폼은 `LessonService.getDetail()`(파싱된 카드 형태)이 아니라 원본 콘텐츠 문자열이
    그대로 필요해서 신규 `LessonAdminResponse`/`LessonService.getLessonForEdit()`로 분리.
  - 화면: 기존 패키지-바이-피처 구조를 따라 새 `admin` 패키지를 만들지 않고 `course`/`lesson` 패키지
    안에 관리자 컨트롤러를 나란히 추가(`CourseAdminController`, `LessonAdminController`). 템플릿 4개
    신규(`templates/admin/`) — 기존 `.page`/`.card-grid`/`.lesson-list`/`button`/`.error-message` 스타일
    그대로 재사용, 새 CSS 거의 없음. 레슨 콘텐츠 textarea 아래에 `INTRO:`/"영어 — 한글" 컨벤션 안내
    문구 추가(새 파싱 규칙 없이 기존 컨벤션 그대로 따르게 유도). 헤더에 "관리자" 링크는
    `sec:authorize="hasRole('ADMIN')"`로 관리자에게만 노출.
  - **범위 제외**: 코스 삭제(고아 레코드 위험 대비 효용 낮음), 진단 테스트 문항 관리, 관리자 승격/강등
    UI(고정 시드 계정 하나로 충분), 콘텐츠 실시간 미리보기.
  - **다크모드 사이드 버그 발견·수정**: 레슨 콘텐츠 textarea에 스타일을 입히려고 CSS를 보다가,
    `input, select` 공용 규칙에 `color`가 아예 없었다는 걸 발견 — 라이트 모드에선 기본 검은 글자가
    `--color-surface`(흰 배경)와 우연히 맞아 문제가 없었지만, 지난 다크모드 작업 이후로 다크 모드에서는
    모든 입력 필드가 "검은 글자 on 남색 배경"이라 전혀 안 보였을 것(로그인/회원가입/검색창 등 사이트
    전체 영향). `textarea`를 같은 규칙에 합치면서 `color: var(--color-text)`를 같이 추가해 관리자
    화면뿐 아니라 사이트 전체 다크 모드 입력 필드를 한 번에 고침 — claude-in-chrome으로 다크 속성 적용 후
    로그인 폼 입력창 글자색이 밝은 회색으로 정상 표시되는 것 확인.
  - 테스트: `MemberRepositoryTest`(role 기본값 USER), `CourseServiceTest`(`updateCourse` 2개),
    `LessonServiceTest`(create/update/delete 6개), 신규 `CourseAdminControllerTest`/
    `LessonAdminControllerTest`(비로그인 리다이렉트, 일반 회원 403, 관리자 생성/수정/삭제 성공, 검증
    오류 시 폼 유지 — `POST /login` 폼 로그인으로 세션을 직접 만드는 새 헬퍼 패턴 사용, 기존
    `signUp()` 헬퍼는 항상 USER라 관리자 테스트엔 못 씀), `CourseApiControllerTest`(무인증 401·일반
    회원 403·관리자 201로 갱신).
  - curl+claude-in-chrome 실서버 종단 검증: 관리자 로그인(`Admin1234!`) → `/admin/courses`에서 새 코스
    생성 → 실제 `INTRO:`/"영어 — 한글" 컨벤션으로 레슨 작성 → 공개 레슨 페이지에서 INTRO+PHRASE 카드로
    정확히 파싱되는 것 확인 → 코스 검색(`keyword=관리자`)에 새 코스가 바로 잡히는 것 확인 → 레슨 수정·
    삭제 확인 → 무인증 `POST /api/courses` 401, 일반 회원 `/admin/courses` 403 확인.

- 진단 테스트 문항 관리 화면 (dev 병합됨)
  - 지난번 코스·레슨 관리자 화면을 만들 때 범위에서 뺐던 `Question`(진단 테스트/레벨 배치 테스트 공용
    80문항)을 이어서 추가 — 지금까지 전부 `QuestionDataInitializer.java`에 코드로만 박혀 있었음.
  - `SecurityConfig`의 `/admin/**` → `hasRole("ADMIN")` 규칙이 이미 모든 `/admin/**` 경로를 커버해서
    **시큐리티 설정 변경이 전혀 필요 없었음**(코스/레슨 때는 이 규칙 자체를 새로 추가해야 했던 것과 다름).
  - `options`(`@ElementCollection`+`@OrderColumn`)를 동적으로 늘렸다 줄였다 하는 대신, 이 프로젝트
    전체가 실제로 4지선다만 쓴다는 걸 확인하고 관리자 폼도 **고정 4개 입력 필드**(`option1~4`)로
    단순화 — Spring record 생성자로 `List<String>` 폼 바인딩하는 복잡함을 피함. `QuestionAdminRequest`에
    `options()` 편의 메서드만 추가해 서비스 계층엔 그대로 `List<String>`으로 전달.
  - `Question.updateDetails(...)` 신규 상태 변경 메서드, `QuestionService`에 CRUD 4개
    (`getAllQuestions`/`getQuestionForEdit`/`createQuestion`/`updateQuestion`/`deleteQuestion`),
    신규 `QuestionAdminController`(`/admin/questions`, 대상·레벨·영역 필터는 80건 정도라 새 동적 쿼리
    없이 스트림 필터링), 템플릿 2개(`question-list.html`/`question-form.html`) 신규. 코스 관리 목록
    페이지에 "문항 관리 →" 링크 추가(헤더에 새 항목 추가 대신).
  - **버그 발견·수정(테스트가 바로 잡음)**: `Question.updateDetails()`를 처음엔 `this.options.clear();
    this.options.addAll(options);`로 짰는데, 실서비스 흐름(Hibernate가 관리하는 컬렉션)에선 문제없이
    동작하지만 순수 Mockito 단위 테스트(`QuestionServiceTest`, ORM 개입 없음)에서
    `UnsupportedOperationException`으로 즉시 실패 — `QuestionAdminRequest.options()`가
    `List.of(...)`(불변)를 반환하는데, 생성자가 이 불변 리스트 참조를 그대로 필드에 박아두면
    이후 `clear()` 호출이 막힘. `this.options = new ArrayList<>(options);`로 재할당하는 방식으로 바꿔
    해결 — 원본이 뭐든(불변이든 가변이든) 항상 새 가변 리스트를 만들어 안전.
  - **두 번째 발견(통합 테스트가 잡음)**: 문항 생성 직후 컨트롤러 테스트에서
    `questionRepository.findAll()`로 방금 만든 문항을 찾아 `.getOptions()`를 바로 확인하려 했다가
    `LazyInitializationException`(세션 밖에서 지연 컬렉션 접근) 발생 — `options`가 지연 로딩 컬렉션이라
    트랜잭션(세션) 밖에서 건드리면 항상 이렇게 터진다는 걸 실제로 겪음. 실제 서비스 코드는
    `QuestionService`의 클래스 레벨 `@Transactional` 안에서만 `.getOptions()`에 접근해 문제없지만,
    테스트 코드가 그 경계 밖에서 엔티티를 직접 찌른 게 원인 — 수정 폼 렌더링 응답(HTTP 레벨, 서비스가
    트랜잭션 안에서 이미 DTO로 변환해 내려줌)에서 보기 값이 보이는지 확인하는 방식으로 테스트를 바꿔
    해결. (교훈: `@ElementCollection`/지연 연관관계가 있는 엔티티는 테스트에서도 리포지토리를 직접
    찔러보지 말고 서비스/컨트롤러 경계를 통해 확인해야 함.)
  - 테스트: `QuestionServiceTest`에 5개(`getAllQuestions`/`createQuestion`/`updateQuestion` 성공·실패/
    `deleteQuestion` 성공·실패), 신규 `QuestionAdminControllerTest`(비로그인 리다이렉트, 일반 회원 403,
    생성→수정→삭제 종단 흐름, 검증 오류 시 폼 유지 — 코스/레슨 관리자 테스트와 같은 `POST /login` 세션
    헬퍼 재사용).
  - curl 실서버 종단 검증: 관리자 로그인 → `/admin/questions`에서 80문항 전체 목록 확인 → LISTENING
    문항 신규 생성(audioText 포함) → **`GET /api/questions/diagnostic-test`(target=ADULT,
    level=BEGINNER)에서 LISTENING 문항 수가 2→3으로 실제로 늘어난 것 확인**(관리자 화면에서 만든 문항이
    진짜 진단 테스트에 반영됨을 종단으로 검증) → 문항 수정(폼 프리필 확인)·삭제 확인 → 대상·레벨·영역
    3중 필터 조합(CHILD/BEGINNER/SPEAKING) 정확히 2건 반환 확인 → 일반 회원 403 확인.

- 관리자 콘텐츠 커버리지 대시보드 (dev 병합됨)
  - CLAUDE.md 작업 이력에 반복해서 나오던 패턴("8개 대상·레벨 조합 중 몇 개가 5영역 중 몇 개씩
    비어있는지"를 그때그때 python 스크립트로 확인)을 관리자 화면의 상시 표로 대체.
  - Course/Lesson/Question 세 도메인을 동등하게 넘나드는 리포팅이라 어느 한 도메인 서비스에 얹지 않고,
    `HomeController`가 "특정 도메인에 안 속하는 화면"의 자리로 쓰이는 것과 같은 이유로 신규
    `common/service/ContentCoverageService` + `common/web/AdminDashboardController`(`GET /admin`)로 분리.
    새 쿼리 없이 기존 메서드 조합(`CourseRepository.search`/`LessonRepository.findByCourseIdIn`/
    `QuestionRepository.findByTargetTypeAndLevel`, 전부 이미 있던 것)만으로 8×5 집계.
  - `SecurityConfig`의 `/admin/**` → `hasRole("ADMIN")` 규칙이 이미 커버해서 이번에도 시큐리티 설정
    변경 없음(문항 관리 때와 동일한 패턴).
  - 레슨 수·문항 수 0인 칸은 dataviz 스킬의 상태색 원칙대로 `color-mix(in srgb, var(--color-danger)
    12%, transparent)`로 배경을 살짝 물들여 강조(텍스트 "0"도 항상 같이 보여 색만으로 정보를 전달하지
    않음) — `--color-danger`가 이미 다크모드 대응 변수라 새 다크 대응 없이 자동으로 맞음.
  - 헤더 "관리자" 링크를 `/admin/courses`에서 `/admin`(대시보드)으로 바꾸고, 코스/문항 관리 목록
    페이지 각각에 서로 오갈 수 있는 링크 추가 — 대시보드·코스 관리·문항 관리 세 화면이 서로 연결됨.
  - **버그 발견·수정(테스트가 아니라 실브라우저 렌더링에서 바로 잡음)**: 템플릿에서 `th:classappend`에
    `${(row.lessonCountByType[t] ?: 0) == 0} ? 'zero' : ''`처럼 SpringEL elvis(`?:`)를 `${...}` 안에
    쓰고 그 바깥에 Thymeleaf 자체 삼항(`? :`)을 또 겹쳤더니, attoparser가 표현식 경계를 잘못 잡아
    `[t]`의 `t`를 반복 변수 참조가 아니라 리터럴 문자열 `"t"`로 취급해버려
    `SpelEvaluationException`(String→LessonType 변환 실패)로 터짐 — `th:text`처럼 `${...} ?: 0`
    (elvis가 `${}` 밖에 있는 형태)는 문제없었는데, elvis를 `${}` **안**에 넣은 조합에서만 재현됨.
    `.get(t) != null ? ... : 0` / `.get(t) == null or ... == 0`처럼 elvis를 아예 안 쓰는 형태로 바꿔
    해결 — 교훈: Thymeleaf에서 SpEL elvis와 Thymeleaf 자체 삼항을 같은 속성에 섞어 쓰지 않는다.
  - 테스트: 신규 `ContentCoverageServiceTest`(Mockito, 8개 조합 전부 반환·영역별 카운트 정확성),
    신규 `AdminDashboardControllerTest`(비로그인 리다이렉트, 일반 회원 403, 관리자는 200+표 렌더링).
  - curl+claude-in-chrome 실서버 검증: 관리자 로그인 → `/admin`에서 레슨 수·문항 수 표 둘 다 렌더링
    확인 → **이미 지난 세션에서 8개 조합 전부 5영역을 채워둔 상태라 실제로는 0인 칸이 하나도 없음을
    확인**(대시보드가 정직하게 "갭 없음"을 보여준 것 — 기능이 실제로 맞게 집계한다는 방증) → `.zero`
    클래스의 실제 CSS 적용을 `getComputedStyle`로 직접 확인(배경 `color-mix` 결과, 빨간 텍스트, 굵은
    글씨 전부 정상 적용).

- 주간 히트맵 → 월간 캘린더 확장 (dev 병합됨)
  - 주간 활동 히트맵을 만들 때 "월 단위 GitHub 스타일 캘린더는 후속 작업"으로 남겨뒀던 것을 이어서 함
    — 7칸짜리 주간 스트립을 완전히 없애고 이번 달 전체(1일~말일)를 보여주는 진짜 달력 그리드로 교체.
  - `ProgressService.getWeeklyActivity()` → `getMonthlyActivity()`로 교체(날짜 범위만 "최근 7일"→
    "이번 달 1일~말일"로 바뀜, 반환 DTO `DailyActivityResponse`는 필드 변경 없이 그대로 재사용).
    `WEEKLY_ACTIVITY_DAYS` 상수 제거. 그리드 배치용 `leadingBlanks`(1일이 무슨 요일인지에 따른 앞쪽
    빈 칸 수)는 리포팅 데이터가 아니라 순수 화면 배치 문제라 서비스에 안 넣고 `MyPageController`에서
    바로 계산.
  - 셀 하나에 담을 정보량 조정: 주간 스트립(7칸)은 라벨+박스+숫자 3단 구성이 여유 있었지만 한 달
    (28~31칸+빈칸)엔 너무 길어져서, 실제 달력처럼 "칸 하나 = 날짜 숫자 + 완료 개수(작게) + 칸 배경색
    자체가 강도"로 압축(요일 라벨은 그리드 맨 위에 한 번만). 오늘 이후 미래 날짜는 "아직 안 함"과
    "0번 함"이 다른 의미라 흐리게 처리하고 완료 개수 자체를 안 보여줌. 오늘 칸엔 강조 테두리.
  - **다크모드 버그 발견·수정(이번 작업 중 코드를 다시 보다가 발견, 실행 중 에러는 아니었음)**: 기존
    주간 히트맵의 `.activity-cell-box.level-1~3`이 `rgba(37, 99, 235, 0.25)`처럼 라이트 모드
    `--color-primary` 값을 하드코딩하고 있었음 — 다크모드 작업 때 "자기 완결적 파스텔 칩이라 안전"으로
    오판하고 지나쳤던 부분(실제로는 그냥 변수를 안 쓴 케이스였음). 관리자 대시보드 `.zero` 칸에 썼던
    `color-mix(in srgb, var(--color-primary) N%, var(--color-surface))` 패턴으로 바꿔 새 `.cal-cell.
    level-*`에 적용 — 다크 모드에서 `--color-primary`가 `#3b82f6`으로 바뀌면 자동으로 반영됨을
    `getComputedStyle`로 직접 확인(라이트 하드코딩 값이 아니라 실제 다크 팔레트 색과 블렌드된 결과가
    나오는 것 확인).
  - Thymeleaf `th:if`+`th:each`를 같은 엘리먼트에 같이 쓸 때의 동작을 활용해 빈 칸 0개 케이스를 처리:
    `#numbers.sequence(1, leadingBlanks)`가 `leadingBlanks == 0`이면(from > to) 실제로는 빈 배열이
    아니라 `[1, 0]`(내림차순 2개)을 반환한다는 걸 미리 알고 있었기 때문에, `th:each`보다 나중에 매
    반복마다 평가되는 `th:if="${leadingBlanks > 0}"`를 같이 걸어 0일 때 전부 걸러지게 함(둘 다 있으면
    th:each가 먼저 돌고 th:if가 반복마다 평가되는 Thymeleaf 속성 우선순위를 이용).
  - 테스트: `ProgressServiceTest`의 `getWeeklyActivity_*` 2개를 `getMonthlyActivity_*`로 교체 —
    "오늘 기준 며칠 전"처럼 날짜에 의존하면 월초에 실행할 때 이전 달로 넘어가 깨질 수 있어서, 항상
    이번 달 안에 있는 "1일"/"3일"을 기준으로 재설계(달의 총 일수는 `LocalDate.now().lengthOfMonth()`로
    계산, 하드코딩 안 함 — 실행 시점과 무관하게 안정적).
  - curl+claude-in-chrome 실서버 검증: 회원가입 → `/my`에서 "2026년 8월 학습 활동" 제목과 함께 8월 1일
    (토요일)이 실제로 토요일 칸 위치에 오도록 빈 칸 5개가 정확히 앞에 붙는 것 확인(`grep -o`로 개수
    세어 검증 — `grep -c`는 줄 단위라 한 줄에 여러 span이 있으면 실수로 과소 카운트한다는 걸 검증
    중 직접 겪음), 레슨 완료 후 오늘 칸이 `level-0`→`level-1`+`today`로 바뀌는 것 확인, 미래 날짜
    칸엔 숫자가 안 보이는 것 확인, 다크 모드에서 오늘 칸 테두리·배경이 다크 팔레트 색으로 정확히
    나오는 것 확인.

- 접근성(a11y) 점검 (dev 병합됨)
  - 모바일 반응형·다크모드는 감사했지만 스크린리더·키보드 탐색·폼 라벨·색 대비는 한 번도 점검한 적이
    없었음. grep + WCAG 대비 공식 계산으로 실제 코드를 훑어 진짜 문제만 골라 고침(눈대중 없이).
  - **고친 것**:
    1) 라디오 퀴즈 그룹 5곳(`course/diagnostic-test.html`, `member/level-test.html`,
       `lesson/detail.html`, `quiz/picture.html`, `my/daily.html`)이 전부 `<div class="quiz-question">
       <p class="quiz-prompt">` 형태라 스크린리더가 "이 라디오들이 한 질문의 보기 묶음"이라는 걸 그룹으로
       announce 못 했음 — `<fieldset class="quiz-question"><legend class="quiz-prompt">`로 교체(클래스명은
       그대로라 CSS는 안 건드림, 브라우저 기본 fieldset/legend 여백만 리셋). 라디오 자체는 이미
       `<label>`로 잘 감싸져 있어 그룹핑만 빠졌던 상태.
    2) 관리자 문항 폼(`question-form.html`)의 보기 입력 4개가 `placeholder`만 쓰고 라벨이 아예 없었음 —
       `aria-label="보기 N"` 추가.
    3) 관리자 커버리지 대시보드 표 2개의 `<th>`에 `scope="col"` 추가.
    4) 스킵 링크 신규(`fragments/layout.html` 헤더 맨 앞) — 관리자는 헤더 내비가 6개까지 늘어나 있어
       키보드 사용자가 매 페이지 훑고 본문 가는 부담이 컸음. 22개 템플릿 `<main class="page">`/
       `<main class="page page-wide">`에 `id="main-content"` 일괄 추가(지난 뷰포트 메타 태그 작업과
       같은 기계적 다건 수정 — 이번엔 패턴이 정확히 2종류뿐이라 `sed`로 한 번에 처리).
  - **확인했지만 안 고친 것(중요)**: 색 대비를 라이트·다크 전부 WCAG 공식으로 직접 계산해 4.5:1 이상
    통과 확인(뱃지 텍스트까지 전부) — 유일하게 미달인 `button:disabled`(2.05:1/2.73:1)는 WCAG 1.4.3이
    **비활성 컨트롤을 대비 요구사항에서 명시적으로 제외**해서 그대로 둠(억지로 진하게 하면 "비활성"이라는
    신호 자체가 사라짐). 포커스 링은 `outline` 관련 규칙이 전무해 이미 안전. `onclick`/`@click`도 전무 —
    모든 상호작용이 진짜 `<button>`/`<a>`/폼이라 이미 키보드로 전부 조작 가능.
  - **의도적으로 범위 제외한 것**: 그림 퀴즈(`quiz/picture.html`) 이미지의 `alt=""` — 레슨 카드 아이콘은
    옆에 텍스트가 이미 있어 장식용이라 `alt=""`가 정답이지만, 그림 퀴즈의 이미지는 그 자체가 문제 내용이라
    `alt=""`가 스크린리더 사용자에게서 문제를 통째로 숨김. 다만 대체 텍스트에 그림 내용을 적으면 답을
    알려주는 꼴이라(보기 중 하나가 정확히 그 단어) 이 상호작용 자체가 본질적으로 시각 전용 — 별도 텍스트
    전용 퀴즈 모드 없이는 해결 불가라 이번엔 손대지 않음(과한 범위, 문서화만 해둠).
  - 서버 로직 변경 없음(템플릿·CSS만) — 새 단위 테스트 불필요, `./gradlew build`는 기존 테스트 회귀
    확인용으로만 실행, 153개 그대로 통과.
  - curl+claude-in-chrome 실서버 검증: fieldset 5곳 전부 legend 텍스트·테두리 두 겹 안 겹침(단일
    2px border)·legend가 카드 폭 100% 채워 안 튀어나오는 것 확인, 관리자 문항 폼 aria-label 4개·
    대시보드 th scope 4곳 확인, 22개 템플릿 중 대표로 랜딩·마이페이지에서 `id="main-content"` 존재
    확인. 스킵 링크는 `document.activeElement`로 실제 포커스 이동은 확인했지만, `:focus` 의사 클래스가
    라이브로 적용되는 걸 자동화 환경에서 관찰하는 데는 실패 — claude-in-chrome이 프로그래매틱
    `.focus()` 호출을 실제 브라우저 포커스와 다르게 다루는(이 세션에서 반복 관찰된) 자동화 한계로 판단,
    CSS 규칙 자체(`document.styleSheets`로 `.skip-link:focus { left: 8px; top: 8px; }` 존재)는 직접
    확인해 코드가 맞다는 건 검증함.

- 관리자 회원 관리 화면 (dev 병합됨)
  - 관리자 역할(Role)을 도입했을 때 "관리자 승격/강등 UI는 과한 범위"라며 고정 시드 계정
    (`admin@eduplatform.com`) 하나만 만들어두고 명시적으로 범위 제외해뒀던 것 — 지금까지 다른 회원을
    관리자로 만들 방법이 코드/DB를 직접 만지는 것 말고는 전혀 없었음. 겸사겸사 지금까지 아예 없었던
    회원 목록·검색 화면도 같이 만듦.
  - `Member.changeRole(MemberRole)` 신규 상태 변경 메서드. 기존 `MemberResponse`(일반 사용자 흐름
    전반에서 씀)엔 `role` 필드를 안 넣고, 코스/레슨/문항 관리자 화면 때와 같은 패턴으로 별도
    `MemberAdminResponse` 신규(관리자 전용 응답을 기존 공개 DTO와 분리 — 기존 호출부 무영향).
    `MemberRepository`엔 목록·검색 메서드가 아예 없어서
    `findByEmailContainingIgnoreCaseOrNicknameContainingIgnoreCase(...)` 파생 쿼리 신규(새 JPQL 없이
    이메일·닉네임 동시 검색).
  - **자기 자신 강등 방지(잠금 사고 예방)**: 관리자 계정이 사실상 하나뿐인 상태에서 실수로 자기 자신을
    일반 회원으로 강등하면 화면에서 다시 승격할 방법이 없어짐 — `MemberService.changeRole()`이
    `targetMemberId.equals(actingMemberId)`면 신규 `CannotChangeSelfRoleException`을 던지고, 템플릿도
    자기 자신 행엔 아예 강등 버튼을 안 그림(`(나)` 표시만) — 방어 두 겹.
  - **역할 변경도 비밀번호 변경과 똑같이 "다음 로그인부터 적용"이라는 걸 실서버로 재확인**: 이미
    `changePassword` 때 "세션의 인증 정보는 로그인 시점 스냅샷"이라고 문서화해뒀던 것과 정확히 같은
    이유로, 관리자가 다른 회원을 ADMIN으로 승격해도 **그 회원이 이미 로그인해 있던 세션에는 즉시
    반영되지 않음**(로그아웃 후 재로그인해야 `ROLE_ADMIN`이 실제로 부여됨) — curl로 승격 직후 그 회원
    세션으로 `/admin` 접근 시 여전히 403인 것까지 실제로 재현해 확인한 뒤, 로그아웃→재로그인하면 200이
    되는 것도 확인. 놀랄 만한 동작이 아니라 이미 알려진 패턴의 재확인.
  - 화면: `member/controller/MemberAdminController`(`/admin/members`, 기존 컨트롤러들처럼 도메인
    패키지 안에 배치), `admin/member-list.html`(코스 목록과 같은 검색 폼 패턴 + `.lesson-list` 재사용,
    새 CSS 없음). 시큐리티 설정은 `/admin/**` → `hasRole("ADMIN")` 규칙이 이미 커버해서 변경 없음
    (문항 관리·대시보드 때와 동일).
  - 테스트: `MemberServiceTest`에 `listMembers`/`changeRole` 5개(정상 승격/강등, 자기 자신이면 예외,
    존재하지 않으면 예외, 키워드 검색), 신규 `MemberAdminControllerTest`(비로그인 리다이렉트, 일반
    회원 403, 승격→강등 종단 흐름, 자기 자신 강등 시도는 에러 배너와 함께 실패하고 DB 값도 안 바뀜
    확인, 이메일 검색).
  - curl 실서버 종단 검증: 관리자 로그인 → 회원 목록에서 자기 자신 행엔 버튼 없이 "(나)"만 표시 확인 →
    테스트 회원 가입 → 목록에서 검색·승격 확인 → **바로 그 회원 세션으로 `/admin` 접근 시 여전히
    403(세션 스냅샷 확인)** → 로그아웃·재로그인 후 200(권한 실제 반영 확인) → 다시 강등 → 관리자
    자기 자신 강등 시도 시 에러 배너와 함께 역할 안 바뀌는 것 확인.

- 코스 검색을 레슨 내용까지 확장 (dev 병합됨)
  - 지금까지 `/courses` 검색은 코스 제목·설명만 봤음 — 코스가 58개, 레슨이 343개로 늘어난 뒤로는
    특정 레슨에만 나오는 단어를 검색해도 그 레슨이 속한 코스의 제목·설명에 그 단어가 없으면 아예
    안 잡히는 갭이 있었음.
  - `CourseRepository.search()`의 키워드 매칭을 "제목·설명 OR 레슨 내용에 매칭되는 레슨을 가진 코스"로
    확장 — 기존 `lessonType` 필터가 쓰던 것과 완전히 같은 `c.id in (select l.courseId from Lesson l
    where ...)` 서브쿼리 패턴을 그대로 재사용, 새 쿼리 인프라 없이 `/courses`·`GET /api/courses` 둘 다
    자동으로 넓어짐.
  - 코스 카드까지만 가면 사용자가 다시 레슨을 찾아 들어가야 해서, **레슨 레벨 검색 결과를 별도 섹션으로
    보여주고 `/lessons/{id}`로 바로 이동**할 수 있게 함. `Lesson`은 `Course`와 매핑된 연관관계가 없어서
    (id 참조만, CLAUDE.md 컨벤션) 개인 코스 레슨을 걸러내려면(`ownerId is null`) JPQL의 명시적
    `join Course c on c.id = l.courseId` ON 조인이 필요 — JPA 2.1부터 연관관계 없는 엔티티끼리도
    가능, 새 매핑 추가 없이 해결.
  - 검색 결과 스니펫(어떤 문장에서 매칭됐는지)은 새 파싱 로직 없이 기존 `LessonService.parseContent()`
    (INTRO/PHRASE/NOTE 구조화)를 그대로 재사용 — `text`/`subtext`(영어/한글) 중 키워드가 있는 쪽을
    찾아 "영어 — 한글" 형태로 보여줌. 레슨 검색은 의도적으로 대상·레벨·영역 필터에 안 묶음(전체
    카탈로그에서 검색 — 특정 단어를 찾는 사용자에게는 지금 고른 필터 밖의 레슨도 보여주는 게 더 유용
    하다고 판단).
  - **버그 발견·수정(전체 테스트 스위트가 광범위하게 잡음)**: `Lesson.content`가 `@Lob`(CLOB 매핑)인데
    새 쿼리에서 `lower(l.content)`를 바로 썼더니 Hibernate 7의 JPQL 검증기가
    `FunctionArgumentException`("lower()의 인자 1은 STRING 타입이어야 하는데 CLOB으로 매핑된
    java.lang.String이 들어왔다")으로 **애플리케이션 컨텍스트 자체가 뜨지 않아** `@SpringBootTest`
    기반 테스트 전부(66개)가 연쇄로 실패 — 실제 SQL 레벨에서는 H2가 CLOB에 LOWER()를 잘 지원하지만,
    Hibernate 6+의 HQL 검증 레이어가 더 엄격해진 탓. `lower(cast(l.content as string))`으로 명시적
    캐스팅을 끼워넣어 해결(두 쿼리 다). 교훈: `@Lob` 필드를 JPQL 문자열 함수(`lower`/`upper`/`like`
    등 일부)에 직접 넣기 전엔 캐스팅이 필요할 수 있다는 걸 이번에 처음 알게 됨 — 이 프로젝트에서
    `@Lob`가 붙은 필드가 지금까지 `content` 하나뿐이라 여태 안 부딪혔던 문제.
  - 테스트: `CourseRepositoryTest`에 신규(제목·설명엔 없고 레슨 내용에만 있는 키워드로도 코스가
    잡히는지), `LessonServiceTest`에 신규 4개(코스 정보+스니펫 반환, 한글 번역 쪽에만 키워드가 있어도
    찾는지, 키워드 없음/매칭 없음 각각 빈 목록).
  - curl 실서버 종단 검증: 실제 코스 설명엔 전혀 없는 단어("elevator")로 검색 → 코스 제목·설명 어디에도
    없는데도 그 코스가 결과에 뜨는 것 확인(레슨 내용에서만 매칭됐다는 증거) → "레슨에서 찾은 결과"
    섹션에 정확한 스니펫("The elevator is under maintenance today. — 오늘 엘리베이터는 점검
    중입니다.")과 함께 뜨는 것 확인 → 클릭하면 실제 그 레슨(`/lessons/240`)으로 바로 이동하는 것
    확인 → 매칭 없는 검색어는 기존처럼 빈 상태 메시지 그대로인 것(회귀 없음) 확인.

- 로그인 시도 제한(무차별 대입 방지) (dev 병합됨)
  - 지금까지 비밀번호를 몇 번 틀려도 아무 제한이 없었음 — 관리자 계정 이메일(`admin@eduplatform.com`)이
    CLAUDE.md에 공개돼 있어 같은 위험에 노출돼 있던 상태였음. 이메일별로 로그인 실패 횟수를 추적해
    5회 연속 실패하면 15분간 잠그는 기본 방어선 추가.
  - **SecurityConfig를 전혀 안 건드림** — Spring Security의 `ProviderManager`가 인증 성공/실패마다
    자동으로 발행하는 `AuthenticationFailureBadCredentialsEvent`/`AuthenticationSuccessEvent`를
    `@EventListener`로 구독하는 신규 `LoginAttemptService`(인메모리 `ConcurrentHashMap`, H2도
    인메모리로 쓰는 이 프로젝트 규모에 맞춘 단순함 — Redis 등 외부 저장소는 과함)만 추가하고,
    `MemberUserDetailsService.loadUserByUsername()`이 회원 조회 직후 `isLocked()`를 확인해
    `LockedException`을 던지는 식으로 끝 — Spring Security의 `DaoAuthenticationProvider`가 비밀번호
    검증 자체를 하기 전에 인증을 막아준다.
  - `UsernameNotFoundException`도 기본 설정(`hideUserNotFoundExceptions=true`)에선
    `AuthenticationFailureBadCredentialsEvent`로 통일 발행돼서, "존재하지 않는 이메일"과 "틀린
    비밀번호"가 구분 없이 똑같이 실패로 집계됨(사용자 열거 공격 방지 원칙과도 자연스럽게 맞음).
  - **잠긴 상태를 화면에 노출하지 않기로 의도적으로 결정** — `LoginViewController`가 `?error` 유무만
    보고 항상 같은 고정 문구를 보여주는데, `LockedException`도 같은 `/login?error` 경로를 타므로
    "지금 막 잠겼다"는 신호를 공격자에게 안 주게 됨(표준적인 관행). 대신 로그인 폼에 정책 자체(몇 번
    틀리면 얼마나 잠기는지)는 고정 안내 문구로 미리 알려줌.
  - **알려진 트레이드오프를 정직하게 문서화**: 계정 잠금 방식 자체가 "공격자가 남의 이메일만 알면
    일부러 틀린 비밀번호로 그 계정을 잠가버리는" DoS에 열려 있음 — IP 기반 제한을 같이 걸면 완화되지만
    이번엔 범위 제외(이 프로젝트 규모에서 과한 방어).
  - 테스트: 신규 `LoginAttemptServiceTest`(7개 — 잠김/안 잠김 경계, 성공 시 초기화, 이메일 대소문자
    무관, `forceState`로 실제 시간 대기 없이 잠금 만료 검증), 신규 `MemberUserDetailsServiceTest`(정상
    반환, 존재하지 않는 회원, 잠긴 계정은 `LockedException`).
  - curl 실서버 종단 검증: 테스트 계정에 일부러 틀린 비밀번호 5회 연속 제출 → **맞는 비밀번호로
    6번째 시도해도 여전히 `/login?error`로 리다이렉트되는 것 확인**(화면 메시지는 의도적으로 동일해서
    이 방식으로만 잠금 여부를 확인할 수 있음) → 완전히 다른 계정(`admin@eduplatform.com`)은 영향
    없이 정상 로그인되는 것 확인(잠금이 계정별로 격리됨을 증명).

- 관리자 레슨 순서 재배치 UI (dev 병합됨)
  - 관리자 코스 상세 화면에서 레슨의 `orderNo`(몇 과)를 바꾸려면 지금까지 레슨 수정 폼에 숫자를 직접
    타이핑해야 했음 — 순서를 하나 옮기려면 관련 레슨들의 orderNo를 손으로 계산해 각각 고쳐야 해 실수하기
    쉬웠던 갭을 채움. 목록에서 바로 위/아래로 옮기는 ▲/▼ 버튼 추가.
  - `Lesson`에 `changeOrderNo(int)` 신규 상태 변경 메서드 추가(세터 없이 의미 있는 메서드로 상태를
    바꾸는 프로젝트 컨벤션 그대로 — 이번엔 다른 필드는 안 건드리니 `updateDetails()`를 재사용하지 않고
    전용 메서드를 새로 둠). `LessonService.moveLesson(lessonId, direction)`은 `getDetail()`이 이전/다음
    레슨 계산에 이미 쓰던 "형제 레슨을 orderNo 순으로 조회 → `IntStream`으로 현재 레슨의 인덱스 찾기"
    패턴을 그대로 재사용해 인접한 레슨과 orderNo를 맞바꿈. 맨 위에서 위로, 맨 아래에서 아래로 요청하면
    조용히 무시(no-op) — 버튼이 화면엔 없어도 URL을 직접 호출하는 경우를 대비한 안전장치.
  - `LessonAdminController`에 `POST /admin/lessons/{id}/move`(`direction` 파라미터) 추가 — 기존
    `delete()`가 쓰던 private 헬퍼 `resolveCourseId()`를 그대로 재사용해 코스 상세로 리다이렉트.
    SecurityConfig 변경 없음(`/admin/**` → `hasRole("ADMIN")` 규칙이 이미 덮음, 이번 세션 admin 기능
    전부에서 반복 확인된 패턴).
  - `admin/course-detail.html`의 레슨 목록 `th:each`에 상태 변수(`lessonStat`)를 추가해 `.first`/`.last`로
    맨 위 레슨엔 ▲, 맨 아래 레슨엔 ▼ 버튼을 숨김. 기존 삭제 폼과 같은 인라인 `<form>` + CSRF hidden
    input 패턴 재사용, CSS는 `.lesson-delete-form` 옆에 `.lesson-move-form { display: inline; }` 한 줄만
    추가.
  - 테스트: `LessonServiceTest`에 `moveLesson` 5개(위/아래 이동, 맨 위/맨 아래 경계 no-op, 존재하지
    않는 레슨 예외), `LessonAdminControllerTest`에 2개(일반 회원 403, 관리자는 이동 후 orderNo가 실제로
    바뀌는지 리포지토리로 확인).
  - curl 실서버 종단 검증: 관리자로 로그인해 레슨 7개짜리 코스(`/admin/courses/25`)에서 2과를 위로
    이동 → 1과로 바뀌고 기존 1과가 2과로 밀리는 것 확인 → 공개 코스 상세(`/courses/25`)에도 바뀐 순서가
    그대로 반영되는 것 확인 → 다시 아래로 이동시켜 원래 순서로 복원 → 일반 회원 계정으로 같은 이동
    요청을 보내면 403이 나는 것 확인.

- 비밀번호 찾기(재설정) 플로우 (dev 병합됨)
  - 지금까지 비밀번호를 잊으면 복구할 방법이 전혀 없었음(로그인된 상태에서 현재 비밀번호를 아는 경우만
    `/my/profile/password`로 변경 가능) — 이메일로 재설정 링크를 받는 기본 플로우를 추가.
  - **이 프로젝트에는 실제 메일 발송 인프라(SMTP)가 없음** — 사용자와 상의해 재설정 링크를 실제 이메일
    대신 **서버 로그에 출력**하기로 결정(Rails/Django 개발 환경의 이메일 콘솔 출력과 같은 패턴). 화면엔
    "이메일을 보냈습니다" 안내만 하고 링크 자체는 노출하지 않아 보안상 올바른 동작 유지 — 나중에 실제
    SMTP를 연결하면 `MemberService.requestPasswordReset()`의 `log.info(...)` 한 줄만 실제 발송으로
    교체하면 됨. 이 프로젝트에 로거(`@Slf4j`) 사용이 이번이 처음이라 `MemberService`에 새로 추가.
  - `member/security/PasswordResetTokenService` 신규 — `LoginAttemptService`와 정확히 같은 인메모리
    `ConcurrentHashMap` 패턴(H2도 인메모리인 이 프로젝트 규모에 맞춘 단순함, 재부팅하면 초기화되는 것도
    동일하게 수용). 토큰 유효시간 30분, `peek()`(소모 없이 유효성만 확인, 재설정 폼 렌더링용)와
    `consume()`(1회용 소모, 실제 비밀번호 변경용)을 분리해 폼을 여러 번 열어봐도 토큰이 죽지 않게 함.
  - **이메일 존재 여부를 노출하지 않는 원칙**을 로그인 시도 제한 때와 동일하게 적용 — 등록된 이메일이든
    아니든 항상 같은 "메일을 보냈습니다" 문구를 보여주고, 실제로 존재하는 이메일일 때만 내부적으로 토큰
    발급·로그 출력(`MemberService.requestPasswordReset()`). `Member.changePassword(encodedPassword)`가
    이미 현재 비밀번호 검증 없이 그냥 덮어쓰는 메서드라 재설정 플로우에 그대로 재사용(현재 비밀번호를
    모르는 게 이 기능의 전제이므로 자연스럽게 맞음).
  - 화면: `GET/POST /password-reset`(이메일 입력) → `GET/POST /password-reset/confirm?token=...`(새
    비밀번호 입력, 무효/만료 토큰이면 에러 메시지와 "다시 요청하기" 링크만). 로그인 폼에 "비밀번호를
    잊으셨나요?" 링크 추가, 재설정 성공 후 `/login?resetSuccess`로 리다이렉트해 안내 문구 표시.
    `SecurityConfig`에 `/password-reset`, `/password-reset/**` permitAll 한 줄만 추가(로그인 전 접근
    경로라 `/members/new/level-test` 옆에 같은 패턴으로).
  - 테스트: `PasswordResetTokenServiceTest`(신규, `LoginAttemptServiceTest`와 같은 스타일 — 발급 직후
    유효, 소모 후 재사용 불가, `forceState` 헬퍼로 만료 경계 검증), `MemberServiceTest`에 6개(존재하는/
    존재하지 않는 이메일 요청, 유효/무효 토큰 확인, 유효 토큰으로 재설정 성공, 무효 토큰이면 비밀번호 안
    바뀜), 신규 `PasswordResetViewControllerTest`(MockMvc — 비로그인 접근 가능, 이메일 존재 여부와 무관
    하게 같은 리다이렉트, 유효/무효 토큰 확인 폼 렌더링, 재설정 후 실제 로그인 가능 + 토큰 재사용 불가).
  - curl 실서버 종단 검증: 회원가입 → `/password-reset`으로 재설정 요청 → `build/bootRun.log`에서 토큰
    포함 링크 확인 → 그 링크로 새 비밀번호 제출 → 이전 비밀번호 로그인 실패, 새 비밀번호 로그인 성공
    확인 → 존재하지 않는 이메일로 요청해도 동일한 리다이렉트인 것(열거 방지) 확인 → 무효 토큰으로 확인
    페이지 접근 시 에러 메시지 확인 → 이미 쓴 토큰 재사용 시도 시 에러 메시지 확인 → 로그인 폼에 링크
    노출 확인.

- 회원가입 이메일 인증 + 비회원 첫 강의만 미리보기 (dev 병합됨)
  - 아이디가 이메일 주소인 것은 이미 그랬음(`Member.email` unique, 로그인도 `email` 파라미터) — 이번엔
    "가입 시 이메일 인증번호 확인"과 "비로그인 사용자는 각 코스 첫 레슨만 열람"두 가지를 함께 추가.
  - **회원가입 인증**: `POST /members`(루트, 즉시 가입+로그인)를 완전히 제거하고 2단계로 교체 —
    `POST /members/new`(폼 검증 + 중복이메일 선확인 + `HttpSession`에 `MemberCreateRequest`를 그대로
    보관 + 인증번호 발급) → `GET/POST /members/new/verify`(6자리 코드 입력, 맞으면 그제서야
    `MemberService.signUp()` 호출 + 자동 로그인). **즉시 가입 경로를 남겨두면 그 URL을 직접 호출해
    인증을 통째로 우회할 수 있어 기능이 무의미해진다고 판단해 아예 제거** — 대신 REST API
    (`POST /api/members`)는 계약을 새로 설계해야 해 이번 범위에서 제외, 웹 화면만 인증을 거침.
    `HttpSession`에 검증된 폼 데이터를 그대로 담아두는 방식이라(Redis 등 외부 세션 저장소가 아니라
    Tomcat 기본 인메모리 세션) 새 pending-signup 저장소나 DTO를 따로 안 만들어도 됨.
  - 비밀번호 재설정 때처럼 **실제 SMTP가 없어 인증번호를 서버 로그에 출력**(같은 이미 합의된 패턴,
    나중에 메일 발송을 붙이면 로그 한 줄만 교체). 신규 `member/security/EmailVerificationService`가
    `PasswordResetTokenService`와 정확히 같은 인메모리 `ConcurrentHashMap` 패턴(이메일 → 6자리 코드 +
    만료시각 10분). 틀린 코드는 소모되지 않아(성공한 코드만 1회용 소모) 만료 전까지 재시도 가능.
    `MemberService`에 `ensureEmailAvailable`/`requestSignupVerification`(코드 발급+로깅)/
    `verifySignupCode` 3개 위임 메서드 추가 — 컨트롤러는 세션만 다루고 비즈니스 규칙·로깅은 서비스
    레이어라는 기존 분리를 그대로 따름.
  - **부작용**: 기존 테스트 5곳이 `POST /members`를 직접 호출해 테스트 계정을 만들고 있어서 전부
    2단계 플로우를 거치도록 고침 — `EmailVerificationService`를 테스트에 직접 autowire해 컨트롤러가
    로그로 내보낸 코드를 다시 조회하는 대신 `issueCode()`를 테스트에서 한 번 더 호출해 알려진 값으로
    받는 방식(`PasswordResetViewControllerTest`가 이미 쓴 패턴과 동일). `LearningProgressFlowTest`는
    3곳에 중복돼 있던 인라인 가입 블록을 공용 `signUp()` 헬퍼로 통합하면서 교체(리팩터링 겸함).
  - **비회원 첫 레슨만 미리보기**: `LessonViewController.detail()`이 이미 받고 있던
    `@CurrentMemberId Long memberId`로 `memberId == null && lesson.orderNo() != 1`이면 `locked`
    플래그를 모델에 담아 `lesson/detail.html`의 실제 콘텐츠(`lesson-content`)와 학습 네비게이션
    (`lesson-nav`)을 감추고 "회원가입 유도" 안내 카드만 보여줌(기존 `.quiz-question` 카드 스타일 재사용,
    새 CSS 없음). REST API 쪽엔 레슨 본문 전체를 주는 엔드포인트가 없어(`GET /api/courses/{id}/lessons`는
    제목 등 요약만 반환) 이 웹 라우트 하나만 막으면 충분함을 확인.
  - `CourseViewController.detail()`도 이미 받던 `memberId`를 모델에 `currentMemberId`로 그대로 노출해
    `course/detail.html`의 레슨 목록에서 `memberId == null and lesson.orderNo != 1`인 행에 🔒 배지
    (기존 `.badge` 클래스 재사용)를 붙임 — 링크 자체는 그대로 둬서 클릭하면 서버가 최종 방어선인 잠금
    화면으로 이어짐(목록의 배지는 UX 힌트일 뿐).
  - 테스트: 신규 `EmailVerificationServiceTest`(5개, `PasswordResetTokenServiceTest`와 같은 스타일 —
    성공/실패/재사용불가/만료), `MemberServiceTest`에 4개 신규, 신규 `MemberViewControllerTest`(5개 —
    유효 제출→인증페이지, 중복이메일 1단계 차단, 세션없이 인증페이지 접근시 리다이렉트, 올바른 코드→
    계정생성+로그인, 틀린 코드→에러), 신규 `LessonViewControllerTest`(3개 — 비로그인 1과 열람,
    비로그인 2과 잠금, 로그인 회원 2과 열람), `CourseViewControllerTest`에 배지 노출 케이스 1개 추가.
  - curl 실서버 종단 검증: 회원가입 폼 제출 → `build/bootRun.log`에서 6자리 인증번호 확인 → 틀린 코드
    제출 시 에러 확인 → 올바른 코드 제출 시 실제 계정 생성 + `/my` 접근 가능 확인 → **`POST /members`
    직접 호출 시 403(구 즉시가입 경로가 실제로 막혀 있음을 확인)** → 중복 이메일이면 1단계에서 바로
    에러 확인 → 비로그인으로 레슨 여러 개짜리 코스 상세 진입 → 1과는 링크로 정상 열람, 2과부터 🔒 배지
    확인 → 2과 URL 직접 접근 시 콘텐츠 대신 잠금 안내 확인 → 로그인 후 같은 2과 접근 시 정상 열람 확인.

- 회원 탈퇴(계정 삭제) 기능 (dev 병합됨)
  - 프로필 수정·비밀번호 변경·역할 관리는 있었지만 정작 회원이 스스로 계정을 지울 방법이 없었던
    갭을 채움 — `/my/profile`에 세 번째 섹션으로 추가, 기존 "비밀번호 변경"과 같은 확인 방식(현재
    비밀번호 재입력)을 재사용.
  - **연관 데이터 정리**: `LearningProgress`(memberId 참조)와 개인 코스(`Course.ownerId`, 전용으로
    복사된 `Lesson`들)를 함께 삭제. 개인 코스는 다른 회원과 절대 공유되지 않아(레슨도 복사본,
    PRODUCT.md 3-2 그대로) 완전히 지워도 안전 — 관리자 코스 삭제 기능을 "고아 레코드 위험"으로 범위
    제외했던 것과 반대로, 이번엔 정확히 그 고아를 없애는 게 목적이라 전부 지움. 기존
    `findByOwnerIdOrderByIdDesc`/`findByCourseIdIn`/`findByMemberId` 파생 쿼리로 조회 후
    `deleteAll`만 호출 — 새 쿼리 메서드 없음.
  - **순환 Bean 의존성을 피하려고 리포지토리를 직접 주입**: `ProgressService`/`CourseService`가 이미
    `MemberService`를 주입받고 있어서(회원 존재 검증용) `MemberService`가 거꾸로 그 서비스들을
    주입받으면 `BeanCurrentlyInCreationException`이 남. `ProgressService`가 이미 하던 대로
    `MemberService`도 `CourseRepository`/`LessonRepository`/`LearningProgressRepository`를 직접
    주입받는 방식으로 우회 — 이 프로젝트에서 검증된 서비스 간 순환 회피 패턴.
  - **관리자 자기 탈퇴 방지**: `changeRole()`의 `CannotChangeSelfRoleException`(자기 역할 변경 방지)과
    같은 이유 — 승격된 관리자가 자기 계정을 탈퇴시키면 서버 재시작(고정 시드 관리자 재생성) 전까지
    관리자가 아예 없어질 수 있음. 신규 `CannotWithdrawAdminException`으로 관리자 역할이면 탈퇴 자체를
    막음(비밀번호 확인보다 먼저 검사).
  - 탈퇴 완료 후 `SecurityContextLogoutHandler`(Spring Security 제공, `/logout`이 내부적으로 쓰는
    것과 같은 클래스)로 세션 무효화 + 컨텍스트 초기화 — 새 로그아웃 로직 없이 재사용. 비밀번호
    재설정 때 추가한 `resetSuccess` 패턴과 똑같이 `LoginViewController`에 `withdrawn` 파라미터를
    추가해 로그인 화면에 안내 문구만 표시.
  - 테스트: `MemberServiceTest`에 `withdraw` 4개(정상 탈퇴 시 회원·진행기록·개인코스·레슨 전부 삭제,
    개인 코스 없어도 정상 탈퇴, 비밀번호 틀리면 예외+아무것도 안 지워짐, 관리자면 예외+아무것도 안
    지워짐), `MemberProfileViewControllerTest`에 2개(정상 탈퇴 후 리다이렉트+계정 삭제 확인+같은
    세션으로 `/my` 접근 시 로그인 리다이렉트로 로그아웃 확인, 틀린 비밀번호는 에러+계정 유지).
  - curl 실서버 종단 검증: 회원가입 → 레슨 완료(진행기록 생성) → 개인 코스 생성 → 틀린 비밀번호로
    탈퇴 시도 시 에러 확인(계정 유지) → 올바른 비밀번호로 탈퇴 → `/login?withdrawn` 리다이렉트 +
    안내 문구 확인 → 같은 세션으로 `/my` 접근 시 로그인으로 리다이렉트(로그아웃 확인) → 방금 만든
    개인 코스(`/courses/50`) 직접 접근 시 더 이상 존재하지 않아 코스 목록으로 리다이렉트되는 것
    확인(레슨까지 함께 삭제됐다는 증거) → 관리자 계정으로 탈퇴 시도 시 에러 메시지 확인(실제로 계정
    유지).

- 코스 즐겨찾기/북마크 (dev 병합됨)
  - 공식 코스가 58개로 늘어난 뒤 관심 있는 코스를 표시해두고 모아볼 방법이 없던 갭을 채움 — 코스
    상세에 토글 버튼, 마이페이지에 "즐겨찾기한 코스" 섹션 추가.
  - 신규 `CourseBookmark`(`memberId`/`courseId` 느슨한 id 참조)를 `course` 패키지 안에 둠 —
    `LearningProgress`가 `progress`라는 독립 패키지를 쓴 건 그 기능 자체가 컸기 때문이고, 즐겨찾기는
    엔티티+리포지토리 하나짜리라 새 패키지를 만들지 않고 기존 `course` 패키지 규모에 비례하게 배치.
  - `CourseService.listPersonalCourses()`가 쓰던 "리포지토리 조회 → `CourseResponse::from` 매핑"
    패턴을 그대로 재사용해 `toggleBookmark`/`isBookmarked`/`listBookmarkedCourses` 3개 추가.
    `listBookmarkedCourses()`는 `courseRepository.findAllById()`가 **입력 순서를 보장하지 않는다**는
    걸 미리 알고 있어서 Map으로 재정렬해 북마크 최신순을 유지 — 부수 효과로 혹시 삭제된 코스를
    가리키는 오래된 북마크가 있어도 조용히 걸러짐(회원 탈퇴 때 개인 코스가 함께 삭제되는 것과
    무관하게, 어떤 코스든 삭제되면 그 북마크 항목은 표시만 안 될 뿐 별도 정리 없이도 안전).
  - **시큐리티 설정 변경 없음** — `SecurityConfig`의 코스 permitAll 규칙이 `HttpMethod.GET`으로
    한정돼 있어(레슨 잠금 기능 때도 활용했던 특성) 신규 `POST /courses/{id}/bookmark`는 자동으로
    맨 마지막 `anyRequest().authenticated()`에 걸려 로그인이 필요해짐 — 새 규칙 추가 없이 확인만 하고
    끝남.
  - 화면: 코스 상세(`course/detail.html`)의 헤더에 "☆ 즐겨찾기"/"★ 즐겨찾기 해제" 토글 버튼(비로그인은
    "로그인 필요" 비활성 링크), 마이페이지(`my/dashboard.html`)에 "내 개인 코스"와 완전히 같은
    카드 그리드 마크업으로 "즐겨찾기한 코스" 섹션 추가 — 새 CSS는 `.bookmark-form { display: inline; }`
    한 줄만(기존 `.lesson-move-form`과 같은 목적).
  - 테스트: 신규 `CourseBookmarkRepositoryTest`(`@DataJpaTest`, 존재확인·최신순 조회·삭제),
    `CourseServiceTest`에 5개(토글 추가/삭제, 존재하지 않는 코스 예외, 존재여부 확인, 최신순+삭제된
    코스 필터링), `CourseViewControllerTest`에 2개(로그인 회원 토글 두 번 왕복, 비로그인은 `/login`
    리다이렉트).
  - curl 실서버 종단 검증: 회원가입 → 코스 상세에서 "☆ 즐겨찾기" 클릭 → "★ 즐겨찾기 해제"로 바뀌는
    것 확인 → 마이페이지 "즐겨찾기한 코스"에 실제로 뜨는 것(제목·링크 포함) 확인 → 다시 클릭해 해제 →
    마이페이지에서 빈 상태 문구로 돌아가는 것 확인 → CSRF 없는 비인증 토글 요청은 403 확인.

**다음 단계 (예시, 우선순위 순)**
1. 사용자가 마스코트 이미지 파일을 주면 `static/images/`에 넣고 레슨 인트로/코스 카드에 연결
2. 운영 DB 전환/배포 준비 — 사용자가 우선순위 최후순위로 명시(콘텐츠·기능 개발이 아직 남아있어서 지금은 보류)
