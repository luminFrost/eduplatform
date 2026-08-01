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

**다음 단계 (예시, 우선순위 순)**
1. 사용자가 마스코트 이미지 파일을 주면 `static/images/`에 넣고 레슨 인트로/코스 카드에 연결
2. 운영 DB 전환/배포 준비 — 지금까지 전부 H2 인메모리 + 로컬 개발
3. (참고, 새 버그 아님) N+1 쿼리 몇 곳(`CourseService.createPersonalCourse`/`createPersonalCourseFromHistory`/`createPersonalCourseFromDiagnosticTest`, `ProgressService.getCourseProgress`/`recommendFocusAreas`/`getSkillAreaProgress`, `QuestionService.getDiagnosticTestQuestions`) — 지금 데이터량에선 무해하지만 코스/레슨이 크게 늘면 최적화 고려
