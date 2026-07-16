# 유지보수 이력 — common

> 유지보수 일시: 20260716-125723 · 도메인: common

## 1. 요구사항

공용 승인 대기함(SCR-COM-014)에서 각 티켓의 실제 상세 화면을 조회할 수 있는 버튼이 추가되어야 한다.
동적 양식 필드 유형에 여러 줄 텍스트 항목이 추가되어야 한다(SRM/ESM이 공유하는 공용 컴포넌트 대상).
타임라인 상태 변경 표시를 코드가 아닌 이름으로, 행위 수행 주체자를 표시하기 위한 공용 기반(도메인 간 중복 없는 공용 유틸)이 필요하다.

## 2. 해결 방법

`ApprovalInboxPage.tsx`의 행마다 기존 "상세"(승인/반려 모달을 여는 버튼)와 별개로 "상세보기" 버튼을 신규 추가했다.
"상세보기" 버튼은 이미 존재하던 `features/common/status.ts`의 `ticketDetailPath(type, ticketId)` 헬퍼를 그대로 재사용해 해당 티켓의 실제 상세 화면으로 이동한다(신규 API 없음).
동적 양식 필드의 여러 줄 텍스트 항목은 공용 컴포넌트 `components/common/form-schema.ts`의 `FormFieldType`에 `textarea`를 추가해 구현했다.
`dynamic-form.tsx`는 textarea 유형을 `ui/textarea.tsx`로 렌더링하고, `field-builder.tsx`의 유형 Select에 "여러 줄 텍스트"를 노출하도록 반영했다.
이 계약은 SRM/ESM 동적 폼이 공유하므로 두 도메인의 카탈로그 양식 빌더·제출 화면에 동시에 적용되었다.
DB `catalog_form_field`/`esm_catalog_form_field`의 `field_type` CHECK 제약에도 textarea 값을 추가했다.
타임라인 코드→이름·행위주체자 표시를 위한 공용 기반으로, 상태 라벨을 홑따옴표로 감싸고 받침 유무(+ㄹ받침 예외)에 맞는 조사(로/으로)를 붙여 조립하는 `common.ticket.TimelineMessages.quotedWithParticle()`을 신설했다(SRM/ESM/INCIDENT 공용, 코드리뷰에서 도메인별 중복 제거 목적으로 공용화).
`TimelineEvent`(BaseEntity 상속)의 `createdBy`(인증 사용자 email)를 표시 이름으로 변환하기 위해 `auth.domain.repository.AppUserRepository`에 `resolveDisplayName(email)` default 메서드를 신설했다(못 찾으면 email 폴백, 도메인 간 중복 제거 목적).

## 3. 변경 파일

- `source/frontend/src/features/common/ApprovalInboxPage.tsx`
- `source/frontend/src/components/common/form-schema.ts`
- `source/frontend/src/components/common/dynamic-form.tsx`
- `source/frontend/src/components/common/field-builder.tsx`
- `source/backend/src/main/java/com/itsm/common/ticket/TimelineMessages.java`(신규)
- `source/backend/src/main/java/com/itsm/auth/domain/repository/AppUserRepository.java`
- `source/db/sql/35_catalog_form_field_textarea_type.sql`

## 4. 테스트 결과

통합 테스트 결과는 `docs/04_test/20260716-112347`에 기록되어 있으며 service-request/esm/common 3개 도메인 전부 PASS했다.
커밋 `69575e1`로 반영했다.
테스트 중 범위 밖 결함을 발견했다.
service-request/incident/problem/change/vulnerability/asset/esm 7개 도메인(esm은 HrCaseDetailPage 포함)의 프론트엔드 상태 전이 완료 토스트 문구가 조사("'로")를 받침 유무와 무관하게 하드코딩해 문법 오류가 있다.
백엔드 타임라인 메시지는 이번 배치1에서 `TimelineMessages.quotedWithParticle()`로 이미 정상화되었으나, 프론트엔드 토스트 문구는 별도 하드코딩 부분이라 이번 수정 대상에서 빠졌다.
사용자가 이 결함을 별도 유지보수 항목으로 나중에 처리하기로 결정해 이번에는 수정하지 않았다.

## 5. 참고 — 전체 구조 점검

이번 유지보수는 여러 도메인과 공통 요소(status.ts 패턴·TimelineEntry·FormFieldType·ApprovalInboxPage)를 동시에 건드려 전체 구조 점검을 수행했다.
생성된 모든 디렉토리의 `CLAUDE.md` 인덱스를 훑어본 결과 중복이나 불일치는 발견되지 않았다.
