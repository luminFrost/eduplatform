# eduplatform 개발 설계

초등학생·성인 대상 영어 학습 플랫폼의 개발 설계 문서.
Claude Code로 개발을 이어갈 때 이 문서를 기준으로 단계별로 구현한다.

---

## 1. 서비스 목표

- 초등학생과 성인 모두 사용하는 영어 학습 사이트.
- 영어 **입문(BEGINNER)** 단계부터 시작해 단계별로 학습을 제공.
- 회원의 유형(초등/성인)과 레벨에 맞는 코스를 추천·제공.
- 학습 진행 상황을 기록하고 진도율을 보여준다.

## 2. 사용자 유형

| 유형 | 설명 | 특징 |
|------|------|------|
| 초등학생(CHILD) | 어린이 학습자 | 쉬운 UI, 그림/음성 중심, 짧은 레슨 |
| 성인(ADULT) | 성인 학습자 | 실용 회화·문법 중심, 밀도 있는 레슨 |

레벨: `BEGINNER`(입문) → `ELEMENTARY`(초급) → `INTERMEDIATE`(중급) → `ADVANCED`(고급)

## 3. 도메인 모델

### Member (회원)
| 필드 | 타입 | 설명 |
|------|------|------|
| id | Long | PK |
| email | String | 로그인 ID, 유니크 |
| nickname | String | 표시 이름 |
| memberType | Enum | CHILD / ADULT |
| level | Enum | 현재 학습 레벨 |
| (추후) password | String | 인증 도입 시 |

### Course (코스)
| 필드 | 타입 | 설명 |
|------|------|------|
| id | Long | PK |
| title | String | 코스명 |
| description | String | 설명 |
| targetType | Enum | 대상(CHILD/ADULT) |
| level | Enum | 난이도 |
| ownerId | Long (nullable) | 개인 코스 소유 회원. **null이면 공식 코스**, 값이 있으면 그 회원의 개인 코스 |
| focusAreas | Set\<SkillArea\> (nullable) | 개인 코스가 비중을 두는 영역(복수 가능). 공식 코스는 비움 |
| criteriaSource | Enum (nullable) | 개인 코스 기준 판단 방식: `SELF_SELECTED` / `HISTORY_BASED` / `DIAGNOSTIC_TEST`. 공식 코스는 null |

> **공식 코스 vs 개인 코스**: 이 플랫폼은 불특정 다수 대상 오픈 서비스가 아니라 개개인의 약점 영역에 맞춘 학습을 지향한다.
> 공식 코스(레벨 기준)와 개인 코스(약점 영역 기준)는 동일한 Course/Lesson 구조를 그대로 쓰며, 화면·진도 추적 로직도 공유한다.
> 레슨은 여러 코스가 공유하지 않는다 — 개인 코스의 레슨은 새로 만들거나 기존 레슨을 복사해서 구성한다 (다대다 공유 구조 없음).
>
> `criteriaSource`(비중 기준 판단 방식) 구현 우선순위: ① `SELF_SELECTED`(자가 선택, 우선 구현) → ② `HISTORY_BASED`(학습 이력 기반) → ③ `DIAGNOSTIC_TEST`(진단 테스트, 최후순위). 셋 다 결과는 동일하게 "이 코스는 어떤 영역에 비중을 두는지"로 저장되므로, 방식이 추가돼도 Course 구조는 그대로 두고 판단 로직만 얹으면 된다.

### SkillArea (영역, 개인 코스 focusAreas / 레슨 활동 유형에 공용)
`VOCAB`(어휘) / `READING`(읽기) / `WRITING`(쓰기) / `LISTENING`(듣기) / `SPEAKING`(말하기) — PRODUCT.md 3-1의 활동 유형과 동일한 5종을 재사용한다.

### Lesson (레슨 — 코스 하위 학습 단위)
| 필드 | 타입 | 설명 |
|------|------|------|
| id | Long | PK |
| courseId | Long | 소속 코스 |
| title | String | 레슨명 |
| orderNo | int | 코스 내 순서 |
| content | Text | 본문/스크립트 |
| (추후) lessonType | Enum | VOCAB/READING/LISTENING/QUIZ |

### LearningProgress (학습 진행)
| 필드 | 타입 | 설명 |
|------|------|------|
| id | Long | PK |
| memberId | Long | 학습자 |
| lessonId | Long | 대상 레슨 |
| completed | boolean | 완료 여부 |
| completedAt | DateTime | 완료 시각 |

### 추후 확장 후보
- **Enrollment(수강신청)**: memberId, courseId — 회원이 코스 등록.
- **Word(단어)**: lessonId, word, meaning, example — 단어 학습.
- **Quiz/Question(문제)**: lessonId, question, options, answer — 이해도 점검.

> 연관관계는 현재 id 참조(Long)로 느슨하게 두고, 도메인이 안정되면 `@ManyToOne` 등으로 전환한다.

## 4. 화면 설계 (Thymeleaf)

| 경로 | 화면 | 설명 |
|------|------|------|
| `/` | 랜딩 | 서비스 소개 |
| `/courses` | 코스 목록 | 대상/레벨 필터 |
| `/courses/{id}` | 코스 상세 | 레슨 목록 |
| `/lessons/{id}` | 레슨 학습 | 본문 + 완료 처리 |
| `/my` | 마이페이지 | 내 진도율, 학습 이력 |
| `/h2-console` | (개발) DB 콘솔 | |

## 5. API 설계 (REST, /api)

| 메서드 | 경로 | 기능 |
|--------|------|------|
| POST | `/api/members` | 회원 가입 |
| GET | `/api/members/{id}` | 회원 조회 |
| GET | `/api/courses` | 코스 목록(대상/레벨 필터) |
| POST | `/api/courses` | 코스 등록(관리) |
| GET | `/api/courses/{id}/lessons` | 코스의 레슨 목록 |
| POST | `/api/progress/complete` | 레슨 완료 처리 |
| GET | `/api/members/{id}/progress` | 회원 진도 조회 |
| POST | `/api/courses/personal` | 개인 코스 생성 (기준: 자가선택/이력/진단테스트) — **Phase 5** |

요청/응답은 DTO로 분리하고, 엔티티를 직접 노출하지 않는다.
공통 응답 포맷(예: `ApiResponse<T>`)을 `common`에 두는 것을 권장.

## 6. 개발 단계 (로드맵)

### Phase 1 — 회원 기반 (현재 다음 작업)
- 회원 가입/조회 구현: `MemberService`, `MemberApiController`, 요청/응답 DTO.
- `@DataJpaTest`로 `MemberRepository` 검증, `@SpringBootTest`로 컨텍스트 검증.

### Phase 2 — 코스 · 레슨
- Course/Lesson CRUD(서비스·컨트롤러) + 관리자용 등록.
- 코스 목록/상세, 레슨 학습 페이지(Thymeleaf).

### Phase 3 — 학습 진행
- 레슨 완료 처리(`LearningProgress.complete()`), 코스별 진도율 계산.
- 마이페이지에서 진도 표시.

### Phase 4 — 학습 콘텐츠 강화
- 단어(Word), 퀴즈(Quiz) 도메인 추가.
- 레슨 타입별 학습 화면 분기.

### Phase 5 — 개인화
- 개인 코스 도입 (`Course.ownerId`, `focusAreas`, `criteriaSource`) — 공식 코스와 별개로 회원별 맞춤 코스 생성.
  - 비중 기준 판단 구현 순서: ① 자가 선택 → ② 학습 이력 기반 → ③ 진단 테스트(최후순위, 별도 문항 콘텐츠 필요).
- 대상(초등/성인)·레벨별 코스 추천·필터.
- 학습 대시보드.

### Phase 6 — 인증 · 배포
- Spring Security 도입(회원 로그인, 권한).
- 운영 DB 전환(H2 → MySQL/PostgreSQL), 프로파일 분리(dev/prod).
- 배포 파이프라인.

## 7. 기술 결정 메모

- 개발 DB는 H2 인메모리(`ddl-auto: create`). Phase 6에서 운영 DB로 전환하며 마이그레이션(Flyway 등) 검토.
- 뷰는 Thymeleaf 서버 렌더링 + 필요한 곳만 REST. 추후 프론트 분리 여부는 규모 보고 판단.
- 검증(Bean Validation), 예외 처리(@RestControllerAdvice)는 Phase 1~2에서 공통화.
