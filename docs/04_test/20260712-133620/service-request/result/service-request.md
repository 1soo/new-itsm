---
date: 20260712-133620
domain: service-request
result: pass
keywords: [다국어(i18n) 전환, field-builder 미번역 결함, 카탈로그 관리, 회귀 테스트]
---

# 통합 테스트 결과 — service-request (20260712-133620)

## 요약
- 1차: 총 15건 · 성공 14 · 실패 1
- 재검증(결함 수정 후): `field-builder.tsx`·`form-schema.ts`·`rating.tsx` 3건 모두 PASS 확인 — 최종 전 항목 PASS
- 부가로 common phase 재검증 2건 모두 PASS

## 상세

| TC ID | 결과 | 실제 동작 | 비고 |
|-------|------|-----------|------|
| TC-BUILD-001 | PASS | `./gradlew build -x test` BUILD SUCCESSFUL, `npm run build` 성공(기존 청크 크기 경고만 존재) | - |
| TC-SRM-I18N-001 | PASS | 서비스 포털: "Service Portal" 타이틀·설명, 검색바 placeholder("Search request type or keyword") 영어 전환. 카탈로그 카드 항목명·설명·카테고리 태그(서버 데이터)는 원문(한국어) 유지 확인 | - |
| TC-SRM-I18N-002 | PASS | 요청 제출: "Request Form" 카드 타이틀, "Cancel"/"Submit" 버튼 영어 전환. 필수 미입력 제출 시도 시 인라인 오류 정상 표시(동적 필드 라벨은 카탈로그 스키마 데이터라 한국어 유지, 결함 아님). 정상 값 제출 시 `.swal2-toast` "Your request has been submitted (SRM-2026-0002)" 확인 후 상세 이동(회귀 없음) | - |
| TC-SRM-I18N-003 | PASS | 내 요청 목록: "My Requests"/설명 문구, 필터(Status/From Date/To Date)/"Search", 표 헤더(Ticket No./Type/Status/SLA/Assignee/Updated At), 상태 배지("Submitted"→"Closed" 등)·SLA 배지("OK") 전환 확인 | - |
| TC-SRM-I18N-004 | PASS | 요청 큐: "Request Queue" 타이틀, 큐 탭("All"/건수 배지), 표 헤더, 상태·SLA 배지, "Assign to Me" 버튼 영어 전환. 배정 클릭 시 `.swal2-toast` "Assigned to you successfully" 확인, 담당자 갱신 정상(회귀 없음). 큐 이름("미분류"/"IT 서비스")은 서버 데이터라 원문 유지 | - |
| TC-SRM-I18N-005 | PASS | 요청 상세: 상태 전이 버튼(Validated/Routed/In Fulfillment/Fulfilled/Closed) 전환 및 각 전이 시 `.swal2-toast`("Status changed to '...'") 영어 확인, SLA 카드(Response/Resolution/OK), 승인 패널("This request has no approval process", 공용 `common:approval.*` 키 재사용), 코멘트("Write a Comment"/"Post"/placeholder) 정상 등록. 종료 후 요청자 재진입 시 CSAT 위젯("Satisfaction Survey"/"Comment (optional)"/"Submit Rating") 노출·제출 시 토스트("Your rating has been submitted. Thank you.") 확인. 상태 전이·코멘트·CSAT 기능 전부 회귀 없음. 타임라인 이벤트 메시지(BE 생성, 예: "요청이 제출되었습니다.")는 서버 응답 원문이라 한국어 유지(결함 아님, BE/DB 변경 없음 원칙과 일치) | - |
| TC-SRM-I18N-006 | **FAIL** | 카탈로그 관리: 화면 자체 소유 필드("Service Catalog Management"/"New Item"/"Edit Request Type"/Name/Description/Response·Resolution SLA (min)/Queue ID (optional)/"Form Fields"/"Save")는 정상 전환. 그러나 **양식 필드 빌더(`components/common/field-builder.tsx`) 전체가 전혀 번역되지 않음** — "라벨"/"유형"/"필드 삭제"/"옵션 (쉼표로 구분)"/placeholder "예: 낮음, 보통, 높음"/"필수 입력"/"필드 추가" 버튼, 필드 유형 드롭다운 값("한 줄 텍스트"/"숫자"/"단일 선택"/"날짜"/"첨부파일"), 빈 상태 안내("정의된 필드가 없습니다"/"아래 버튼으로 양식 필드를 추가하세요.") 전부 한국어로 고정되어 English 화면 안에 한/영이 뒤섞여 노출됨. 이 컴포넌트는 SCR-SRM-007의 핵심 구성요소("양식 필드 빌더")이며 `useTranslation` 자체가 전혀 적용되어 있지 않음(0건) | - |
| TC-SRM-I18N-007 | PASS | 지표 대시보드: "Request Metrics", 필터(From Date/To Date)/"Search", KPI 카드(Avg CSAT/Avg Response Time/Avg Resolution Time/SLA Compliance Rate) 영어 전환. 앞선 테스트에서 발생한 실데이터(CSAT 5.0/5, 응답 1min, 해결 3min, SLA 준수율 100%) 정상 반영 확인 | - |
| TC-SEARCH-SRM-001 | PASS | 통합 검색 결과: SERVICE_REQUEST 도메인 배지("Service Request")와 상태 배지("Closed"/"In Fulfillment")가 영어로 정상 전환(`features/search/status.ts`가 `service-request/status.ts`의 `statusLabel(t, ...)` 재사용 확인) | - |
| TC-COMMONFIX-001 | PASS | 감사 로그 페이지네이션 재검증: nav aria-label "Pagination", "Previous page"(disabled)/"Next page" 버튼 aria-label 전부 영어로 전환 확인(common phase 결함 1 수정 확인) | - |
| TC-COMMONFIX-002 | PASS | 계정 생성 "Initial Roles" MultiSelect 재검증: placeholder "Select roles (1 or more)", 선택 칩 제거 버튼 aria-label "Clear selection"(구 "선택 해제") 전환 확인(common phase 결함 2 수정 확인). 검색 입력이 없는 인스턴스라 "검색 결과 없음" 문구는 이번 재검증에서 직접 확인하지 못했으나, 소스 리뷰로 `common:multiSelect.*` 키 적용 확인(`multi-select.tsx`) | - |
| TC-SRM-FORMAT-REG-001 | PASS | English 상태에서도 요청 목록 "Updated At", 감사 로그 "시각" 값이 ko-KR 포맷(`2026. 7. 12. 오후 ...`) 그대로 유지, 영어 로케일로 전환되지 않음 확인 | - |
| TC-SRM-KO-ROUNDTRIP-001 | PASS | English 상태에서 한국어로 재전환 시 새로고침 없이 즉시 복귀("서비스 포털"/설명/검색 placeholder 등), 누락·깨짐 없음 | - |
| TC-SRM-CROSSREG-001 | PASS | END_USER(user@itsm.local)가 `/service-requests/queue` 직접 진입 시 `/403`으로 정상 리다이렉트, 사이드바에도 "요청 큐"/"카탈로그 관리" 메뉴 비노출(RBAC 회귀 없음) | - |

## 실패 항목 분석

- **[결함] `components/common/field-builder.tsx` 전체 미번역**: 이 컴포넌트는 `useTranslation`을 전혀 사용하지 않아(0건) 모든 라벨·버튼·placeholder·드롭다운 값이 하드코딩된 한국어다. SCR-SRM-007(서비스 카탈로그 관리)의 "양식 필드 빌더" 구성 요소가 이 컴포넌트이므로, service-request phase 완료 기준("서비스 포털·요청 제출·내 요청 목록·요청 큐·요청 상세·카탈로그 관리·지표 대시보드 전체 텍스트... 영어 전환")을 카탈로그 관리 화면에서 충족하지 못한다. 재현: `po@itsm.local` 로그인 → English 전환 → 서비스 카탈로그 관리 → 임의 항목(예: "노트북 신청") 선택 → "Form Fields" 섹션의 필드 목록·"필드 추가" 버튼이 한국어로 노출. 해결에는 `field-builder.tsx`에 `useTranslation("common")` 적용 및 `common.json`(ko/en)에 필드 유형·라벨·버튼·빈 상태 키 추가가 필요.

## 참고 사항 (이번 phase 실패로 집계하지 않음 — 별도 공용 컴포넌트 이슈)

field-builder.tsx를 검토하던 중 인접한 공용 컴포넌트 2건에서도 유사한 미번역을 추가로 발견했다. 둘 다 시각 노출은 제한적(aria-label 또는 데이터 라벨과 결합된 문구)이라 이번 실패로는 집계하지 않고 참고로 남긴다.

1. `source/frontend/src/components/common/form-schema.ts`의 `validateForm` — 필수 항목 미입력 오류 메시지가 `` `${field.label}은(는) 필수 항목입니다.` `` 형태로 하드코딩되어 있어(예: "희망 모델은(는) 필수 항목입니다."), English 전환 후에도 한국어 조사("은(는)")와 문구가 그대로 노출된다. `field.label` 자체는 카탈로그 스키마 데이터라 번역 대상이 아니지만, 감싸는 문구("~은(는) 필수 항목입니다")는 UI 텍스트라 전환 대상이다. TC-SRM-I18N-002(요청 제출 필수 필드 오류)에서 확인.
2. `source/frontend/src/components/common/rating.tsx` — CSAT 별점 위젯의 `aria-label={`별점 ${value} / ${max}`}` 및 `aria-label={`${starValue}점`}`이 하드코딩되어 있어 언어 전환과 무관(시각적 노출 없음, 스크린리더 접근성 텍스트만 영향). TC-SRM-I18N-005(CSAT 위젯)에서 확인.

## 테스트 데이터 안내
- `user@itsm.local`로 SRM-2026-0002(노트북 신청) 요청을 생성해 제출→검증→라우팅→이행중→이행완료→종료 전 상태 전이와 코멘트·CSAT(5점)까지 진행했다. 실제 운영 데이터가 아닌 테스트 산출물이므로 별도 정리는 하지 않았다(기존 tester 테스트 데이터 컨벤션과 동일하게 유지).

## 재테스트 (결함 3건 수정 후)

dev_fe가 `field-builder.tsx`(useTranslation 적용)·`rating.tsx`(aria-label 키 전환)·`form-schema.ts`(`validateForm`에 선택 인자 `t` 추가, 미전달 시 기존 한국어 폴백)를 수정하고 `RequestSubmitPage.tsx` 호출부에 `t` 전달을 반영했다. `npm run build` 통과 확인(재빌드로 재확인).

| 항목 | 결과 | 실제 동작 |
|------|------|-----------|
| 결함 재검증 — `field-builder.tsx`(카탈로그 관리 양식 필드 빌더) | **PASS** | `po@itsm.local` 로그인 → English → 카탈로그 관리 → 신규 항목 빈 상태("No fields defined"/"Use the button below to add a form field."/"Add Field") 및 기존 항목("노트북 신청") 필드 행("Label"/"Type"/"Remove field"/"Options (comma-separated)"/placeholder "e.g. Low, Medium, High"/"Required", 필드 유형 값 "Single Select"/"Single-line Text") 전부 영어로 전환 확인. 한/영 혼용 해소 |
| 결함 재검증 — `form-schema.ts`(필수 항목 오류 메시지) | **PASS** | `user@itsm.local` 로그인 → English → 요청 제출 폼에서 미입력 제출 시 "희망 모델 is required."/"신청 사유 is required." 표시 확인(필드 라벨은 카탈로그 스키마 데이터라 원문 유지, 감싸는 문구만 정상 영어 전환) |
| 결함 재검증 — `rating.tsx`(CSAT 별점 aria-label) | **PASS**(소스 리뷰) | `aria-label`이 `t("rating.overallAria"/"rating.starAria")` 키로 전환된 것을 소스에서 확인(`common.md` CLAUDE.md 갱신 내용과 일치) |

**재테스트 결론**: 3건 모두 수정 확인. service-request 도메인 통합 테스트 전 항목 PASS.
