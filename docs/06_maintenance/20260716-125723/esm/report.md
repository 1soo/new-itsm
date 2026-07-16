---
date: 20260716-125723
domain: esm
change_type: [new, modified]
keywords: [상태전이 버튼 라벨, 타임라인 actor 표시, textarea 필드 유형, 부서요청]
---

# 유지보수 이력 — esm

> 유지보수 일시: 20260716-125723 · 도메인: esm

## 1. 요구사항

상태 변경 버튼의 문구가 도착 상태명으로만 표시되어 있어, 이를 실제 수행하는 행위에 맞는 동사형 문구로 변경해야 한다.
타임라인에 나타나는 상태 변경 표시가 코드가 아닌 사람이 읽을 수 있는 이름으로 나타나야 한다.
타임라인의 각 항목에 그 행위를 수행한 주체자가 표시되어야 한다.
동적 양식 필드 유형에 여러 줄 텍스트 항목이 추가되어야 한다(공용 컴포넌트 변경으로 SRM과 함께 적용).

## 2. 해결 방법

상태 변경 버튼 문구는 `features/esm/status.ts`에 `transitionLabel()`을 신규 추가해 동작 동사형으로 전환했다.
`EsmRequestDetailPage.tsx`의 전이 버튼 라벨을 `transitionLabel()`로 교체했다.
상태 변경 완료 토스트 문구는 기존 `statusLabel()`을 그대로 유지했다.
타임라인 코드→이름 전환을 위해 백엔드 `EsmRequestStatus`에 한글 상태 라벨을 반환하는 `label()`을 신규 추가했다.
상태 라벨을 홑따옴표+받침 유무에 맞는 조사(로/으로)로 조립하는 공용 유틸 `common.ticket.TimelineMessages.quotedWithParticle()`(SRM/ESM/INCIDENT 공용, srm 배치에서 신설)을 `EsmRequestService`의 상태 전이 타임라인 저장 로직에서 사용하도록 했다.
타임라인 행위 주체자 표시를 위해 `esm.application.dto.RequestDetailResponse.TimelineEntry`에 `actor` 필드를 추가했다.
`TimelineEvent`의 `createdBy`(인증 사용자 email)를 표시 이름으로 변환하는 공용 default 메서드 `AppUserRepository.resolveDisplayName(email)`(srm 배치에서 신설)을 재사용해 `EsmRequestService`의 타임라인 저장 시 actor를 채웠다.
이번 변경은 부서 요청(EsmRequest)에만 적용되며, HR 케이스(`HrCaseDetailPage.tsx`/`EsmHrCaseService.java`)는 별도의 상태 변경 이력 방식을 사용해 이번 범위에 포함되지 않았다.
여러 줄 텍스트 필드는 공용 컴포넌트(`components/common/form-schema.ts`)의 `FormFieldType`에 `textarea`가 추가되며 SRM과 동일한 동적 폼 계약을 공유하는 ESM 카탈로그 동적 양식에도 자동 적용되었다(ESM 전용 코드 변경 없음).
DB `esm_catalog_form_field.field_type` CHECK 제약에도 textarea 값이 추가되었다.

## 3. 변경 파일

- `source/frontend/src/features/esm/status.ts`
- `source/frontend/src/features/esm/EsmRequestDetailPage.tsx`
- `source/backend/src/main/java/com/itsm/esm/domain/EsmRequestStatus.java`
- `source/backend/src/main/java/com/itsm/esm/application/EsmRequestService.java`
- `source/backend/src/main/java/com/itsm/esm/application/dto/RequestDetailResponse.java`
- `source/db/sql/35_catalog_form_field_textarea_type.sql`(공용, srm과 공유)

## 4. 테스트 결과

배치1(상태변경 버튼·타임라인) 통합 테스트는 `docs/04_test/20260716-080731`에 기록되어 있으며 PASS했다.
배치3(textarea) 통합 테스트는 `docs/04_test/20260716-112347`에 기록되어 있으며 esm 포함 3개 도메인 전부 PASS했다.
커밋 `1759a25`(배치1), `69575e1`(배치3)으로 반영했다.
