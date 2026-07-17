---
date: 20260717-145302
domain: srm
result: partial
keywords: [form.io 폼 빌더, 동적 폼 렌더러, 서버 재검증, FormSubmissionValidator]
---

# 통합 테스트 결과 — srm (서비스 카탈로그 커스텀 폼 빌더, form.io 유지보수 2026-07-17) (20260717-145302)

## 요약
- 총 10건 · 성공 7 · 실패 3

## 상세

| TC ID | 결과 | 실제 동작 | 비고(스크린샷/로그) |
|-------|------|-----------|----------------------|
| TC-SRM-001 | PASS | 백엔드 `./gradlew build -x test` BUILD SUCCESSFUL, 프론트엔드 `npm run build`(prebuild formio css 생성 포함, tsc+vite) 성공(3994 modules) | - |
| TC-SRM-002 | FAIL | 컬럼/패널 중첩 레이아웃 설계·저장·재조회 유지 자체는 정상 동작(`GET /service-catalog/items/7` 응답에 Panel > Number 중첩 구조 그대로 보존 확인). 다만 팔레트 구성이 `docs/02_plan/screen/common.md` 8.2절과 다름 — 숨겨야 할 "Premium" 그룹이 노출되고 그 안에 "File"이 위치함(스펙: Advanced 그룹에 File 포함, Premium/Resource는 숨김). Advanced 그룹은 Email/Phone Number/Date-Time만 있고 File이 빠져있음 | api 응답 JSON 확인, palette 그룹별 클릭 확인 |
| TC-SRM-003 | PASS | user@itsm.local로 `/portal/requests/new?item=7` 진입 시 `DynamicFormRenderer`가 컬럼 없이도(테스트 폼은 Panel만 사용) 저장된 레이아웃(텍스트 필드 + Panel > Number) 그대로 렌더링됨 | snapshot |
| TC-SRM-004 | FAIL(critical) | 모든 필드에 유효한 값(minLength/max 등 규칙 만족)을 입력해 제출해도 서버가 항상 400(`FORM_FIELD_INVALID`, "형식이 올바르지 않습니다")으로 거부. 원인: `FormSubmissionValidator.java:84`의 `rules.get("pattern") instanceof String pattern && !str.matches(pattern)` 체크가 Form.io Builder가 항상 직렬화하는 기본값 `"pattern":""`(패턴 미설정 시에도 빈 문자열로 항상 존재)을 정규식으로 취급함. `"".matches("")`는 빈 문자열에만 true이므로, 값이 있는 모든 텍스트 필드 제출은 항상 이 체크에서 실패함. 폼 빌더로 만든 텍스트 필드가 포함된 폼은 사실상 제출이 불가능(신규 기능의 핵심 시나리오가 막힘). 기존 마이그레이션된 카탈로그 항목(예: `비밀번호 초기화`, id=2)은 `pattern` 키 자체가 없어 이 버그의 영향을 받지 않고 정상 제출됨(대조 확인, SRM-2026-0021 생성 성공) | network request #275 response body, `FormSubmissionValidator.java` 코드 확인 |
| TC-SRM-005 | PASS | 클라이언트 검증 우회(직접 `POST /api/v1/service-requests` 호출) 후 필수 필드(`requestSummary`) 빈 값 제출 시 400(`REQUIRED_FIELD_MISSING`, "필수 항목 누락: 요청 요약") 정상 반환 | fetch 응답 확인 |
| TC-SRM-006 | FAIL | minLength 위반(2자, 규칙 5자 미만) 값은 400(`FORM_FIELD_INVALID`, "최소 길이(5자) 미만입니다")으로 올바르게 거부됨(pattern 체크보다 먼저 평가되어 정상 동작). 그러나 "정상 값으로는 201 성공(대조군 1회 확인)" 항목이 TC-SRM-004와 동일한 pattern 버그로 실패(항상 400). 또한 텍스트 필드가 스키마에 선행하면 그 필드의 pattern 버그가 먼저 발생해 뒤따르는 숫자 필드의 min/max 위반 검증에 도달하지 못함(예: `requestSummary` 유효값 + `priorityScore=500`(max=100 초과) 제출 시 max 초과 메시지가 아니라 `requestSummary`의 "형식이 올바르지 않습니다"가 먼저 반환됨) — min/max 검증 로직 자체(`FormSubmissionValidator.java:90-98`)는 코드상 정상이나 실전 폼에서는 텍스트 필드의 pattern 버그에 가려 도달 불가 | fetch 응답 확인 |
| TC-SRM-007 | PASS | 카테고리 생성("폼빌더 회귀 카테고리") 성공, 목록 노출 확인, 미사용 카테고리 삭제 성공, 사용 중인 카테고리("계정", 참조 1건) 삭제 시도 시 차단(목록에서 삭제되지 않음, 콘솔에 요청 실패 로그 확인 — 409 추정) | snapshot 비교(삭제 전/후 목록) |
| TC-SRM-008 | PASS | agent@itsm.local로 `/service-requests/queue`에서 SRM-2026-0021 "배정" 클릭 → 담당자 역할 미지정 항목이라 "나에게 배정" 버튼 노출 → 클릭 시 배정 성공, 목록/상세에 "서비스데스크 담당자" 반영 | snapshot |
| TC-SRM-009 | PASS | SRM-2026-0021 상세에서 검증 완료→라우팅 처리→이행 시작→이행 완료 처리→종료 처리까지 순차 전이. 각 버튼 라벨이 동작 동사형으로 정상 표시, 타임라인에 상태 라벨(홑따옴표+조사)과 행위 주체자(서비스데스크 담당자/최종 사용자)가 매 항목 정상 기록됨 | body innerText 캡처 |
| TC-SRM-010 | PASS | 담당자 배정 후 라우팅→이행 전이 시점에 승인 게이트가 평가되어(해당 카탈로그 항목에 매칭 승인 프로세스 없음) "이 요청에는 승인 절차가 없습니다" 문구로 정상 전환, 이행 전이 차단 없이 진행됨. 승인 게이트 자체의 상세 동작(승인 대기 차단, 반려 등)은 이번 유지보수 범위 밖(승인 엔진 미변경)이며 `docs/04_test/20260711-195405` 등 기존 approval-engine 전용 테스트에서 이미 충분히 검증됨(회귀 없음 확인) | body innerText 캡처 |

## 관찰 사항(참고용)
- 한글 라벨로 텍스트 필드 추가 시 Form.io `FormBuilder`가 Property Name(key)을 라벨에서 자동 생성하는데, 한글이 그대로 들어가 "The property name must only contain alphanumeric characters..." 오류로 저장이 차단된다. 영문/영숫자 키를 API 탭에서 수동 입력하면 우회 가능하지만, 전체 UI가 한국어인 시스템 특성상 실사용 시 관리자가 매 필드마다 수동으로 키를 고쳐야 하는 사용성 문제가 있다. 산출물(`docs/02_plan`)에 이 이슈에 대한 언급이 없어 결함으로 볼지 기존 Form.io 라이브러리 한계로 볼지는 dev-lead/designer 판단 필요.
- 테스트 중 동일 Playwright 브라우저 세션을 다른 teammate(개발자, DynamicFormRenderer 버튼 배치 작업 추정 — task #13/#14)가 동시 사용해 탭이 겹치는 현상 발생(무관한 `localhost:5185/smoke-test.html`로 전환됨). 새 탭을 열어 회피했으며 테스트 결과에는 영향 없음. 향후 병행 브라우저 테스트 시 세션 충돌 방지가 필요하다는 점만 참고로 공유.

## 실패 항목 분석
- TC-SRM-002: 팔레트 구성이 `docs/02_plan/screen/common.md` 8.2절과 불일치. File 컴포넌트가 Advanced가 아닌 Premium 그룹에 위치, Premium 그룹이 숨겨지지 않고 노출됨. FE(`dynamic-form-builder.tsx`)의 `FormBuilder` `options.builder` 팔레트 그룹 설정 확인 필요.
- TC-SRM-004 / TC-SRM-006(대조군 실패): `source/backend/src/main/java/com/itsm/common/form/FormSubmissionValidator.java` 84행. `pattern`이 빈 문자열("")인 경우를 정규식 미설정으로 처리하지 않고 그대로 `String.matches("")`에 넣어, 값이 있는 모든 텍스트 필드 제출이 항상 실패한다. 폼 빌더로 생성된 텍스트 필드를 포함하는 모든 신규 카탈로그 항목의 요청 제출이 막히는 심각한 결함으로, dev-lead에게 즉시 전달해 BE 수정이 필요하다(예: `pattern instanceof String pattern && !pattern.isBlank() && !str.matches(pattern)`).
