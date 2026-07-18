---
date: 20260716-125723
domain: srm
change_type: [new, modified]
keywords: [상태전이 버튼 라벨, 타임라인 actor 표시, 서비스 카탈로그 카테고리 CRUD, textarea 양식 필드]
---

# 유지보수 이력 — srm

> 유지보수 일시: 20260716-125723 · 도메인: srm

## 1. 요구사항

상태 변경 버튼의 문구가 도착 상태명(예: "이행 중")으로만 표시되어 있어, 이를 실제 수행하는 행위에 맞는 동사형 문구로 변경해야 한다.
타임라인에 나타나는 상태 변경 표시가 코드가 아닌 사람이 읽을 수 있는 이름으로 나타나야 한다.
타임라인의 각 항목에 그 행위를 수행한 주체자가 표시되어야 한다.
서비스 카탈로그의 카테고리 할당 기준을 확인하고, 카탈로그 관리 화면에서 카테고리를 설정할 수 있어야 한다.
카테고리는 자유 텍스트가 아니라 관리자가 통제하는 고정 목록으로 관리(추가/수정/삭제)할 수 있어야 하고, 카탈로그 항목은 그 목록에서 선택하는 구조여야 한다.
서비스 카탈로그의 동적 양식 필드 유형에 여러 줄 텍스트 항목을 추가해야 한다.

## 2. 해결 방법

상태 변경 버튼 문구는 `features/service-request/status.ts`에 `transitionLabel()`을 신규 추가해 동작 동사형으로 전환했다.
`RequestDetailPage.tsx`의 전이 버튼 라벨을 `transitionLabel()`로 교체했다.
상태 변경 완료 토스트 문구는 기존 `statusLabel()`을 그대로 유지했다.
타임라인 코드→이름 전환을 위해 백엔드 `RequestStatus`에 한글 상태 라벨을 반환하는 `label()`을 신규 추가했다.
상태 라벨을 홑따옴표로 감싸고 받침 유무(+ㄹ받침 예외)에 맞는 조사(로/으로)를 붙여 조립하는 공용 유틸 `common.ticket.TimelineMessages.quotedWithParticle()`을 신설해(SRM/ESM/INCIDENT 공용) `ServiceRequestService`의 상태 전이 타임라인 저장 로직에서 사용하도록 했다.
타임라인 행위 주체자 표시를 위해 `RequestDetailResponse.TimelineEntry`에 `actor` 필드를 추가했다.
`TimelineEvent`(BaseEntity 상속)가 이미 갖고 있는 `createdBy`(인증 사용자 email)를 표시 이름으로 변환하기 위해 `auth.domain.repository.AppUserRepository`에 `resolveDisplayName(email)` default 메서드를 신규 추가했다(못 찾으면 email 폴백, 도메인 간 중복 제거 목적).
서비스 카탈로그 카테고리는 자유 텍스트(`category`)를 폐기하고, 관리자가 통제하는 고정 목록으로 전환했다.
DB에 `service_catalog_category`(name UNIQUE, sort_order) 테이블을 신규 생성하고, 기존 `category` 값을 백필해 매핑한 뒤 `service_catalog_item.category_id`(FK) 컬럼으로 대체하고 `category` 컬럼은 제거했다.
백엔드에 `ServiceCatalogCategory` 엔티티·리포지토리·`ServiceCatalogCategoryService`·`ServiceCatalogCategoryController`를 신규 구현했다.
카테고리 목록 조회는 sortOrder 오름차순 정렬과 참조 중인 카탈로그 항목 수(itemCount)를 함께 반환한다.
생성/수정 시 이름 중복은 409(CATEGORY_NAME_DUPLICATE)로 막고, 삭제 시 참조 중인 카탈로그 항목이 있으면 409(CATEGORY_IN_USE)로 막는다.
`ServiceCatalogService`/`ServiceCatalogItem`을 자유 텍스트 category에서 categoryId(FK) 기준으로 전환했고, 생성/수정 시 존재 검증(404 CATEGORY_NOT_FOUND)을 추가했다.
카탈로그 목록/상세 응답에는 categoryName을 resolve해 포함시켰다.
프론트엔드 `CatalogManagePage.tsx`에 "카테고리 관리" 탭(SCR-SRM-009, 목록 표 + 생성/수정 모달 + 삭제 확인 다이얼로그)을 신규 추가했다.
카탈로그 항목 등록/수정 폼의 카테고리 입력을 자유 텍스트에서 카테고리 목록 Select로 전환했다.
`PortalPage.tsx`의 카탈로그 카드에는 categoryName 배지를 표시하도록 반영했다.
동적 양식 필드의 여러 줄 텍스트 항목은 공용 컴포넌트(`components/common/form-schema.ts`)의 `FormFieldType`에 `textarea`를 추가해 구현했다(SRM/ESM 동적 폼 공용 계약이라 두 도메인에 동시 적용됨).
`dynamic-form.tsx`는 textarea 유형을 `ui/textarea.tsx`로 렌더링하고, `field-builder.tsx`의 유형 Select에 "여러 줄 텍스트"를 노출하도록 반영했다.
DB `catalog_form_field.field_type` CHECK 제약에도 textarea 값을 추가했다.

## 3. 변경 파일

- `source/frontend/src/features/service-request/status.ts`
- `source/frontend/src/features/service-request/RequestDetailPage.tsx`
- `source/frontend/src/features/service-request/CatalogManagePage.tsx`
- `source/frontend/src/features/service-request/PortalPage.tsx`
- `source/frontend/src/features/service-request/api.ts`
- `source/frontend/src/features/service-request/types.ts`
- `source/backend/src/main/java/com/itsm/srm/domain/RequestStatus.java`
- `source/backend/src/main/java/com/itsm/srm/domain/ServiceCatalogItem.java`
- `source/backend/src/main/java/com/itsm/srm/domain/ServiceCatalogCategory.java`(신규)
- `source/backend/src/main/java/com/itsm/srm/domain/repository/ServiceCatalogCategoryRepository.java`(신규)
- `source/backend/src/main/java/com/itsm/srm/infrastructure/persistence/ServiceCatalogCategoryJpaRepository.java`(신규)
- `source/backend/src/main/java/com/itsm/srm/application/ServiceCatalogCategoryService.java`(신규)
- `source/backend/src/main/java/com/itsm/srm/application/ServiceCatalogService.java`
- `source/backend/src/main/java/com/itsm/srm/application/ServiceRequestService.java`
- `source/backend/src/main/java/com/itsm/srm/application/dto/RequestDetailResponse.java`
- `source/backend/src/main/java/com/itsm/srm/application/dto/CatalogItemSummaryResponse.java`
- `source/backend/src/main/java/com/itsm/srm/application/dto/CatalogItemDetailResponse.java`
- `source/backend/src/main/java/com/itsm/srm/application/dto/CreateCatalogItemRequest.java`
- `source/backend/src/main/java/com/itsm/srm/application/dto/UpdateCatalogItemRequest.java`
- `source/backend/src/main/java/com/itsm/srm/application/dto/CategoryResponse.java`(신규)
- `source/backend/src/main/java/com/itsm/srm/application/dto/CategoryListResponse.java`(신규)
- `source/backend/src/main/java/com/itsm/srm/application/dto/CategoryCreateRequest.java`(신규)
- `source/backend/src/main/java/com/itsm/srm/application/dto/CategoryUpdateRequest.java`(신규)
- `source/backend/src/main/java/com/itsm/srm/presentation/ServiceCatalogCategoryController.java`(신규)
- `source/backend/src/main/java/com/itsm/auth/domain/repository/AppUserRepository.java`
- `source/backend/src/main/java/com/itsm/common/ticket/TimelineMessages.java`(신규, SRM/ESM/INCIDENT 공용)
- `source/frontend/src/components/common/form-schema.ts`
- `source/frontend/src/components/common/dynamic-form.tsx`
- `source/frontend/src/components/common/field-builder.tsx`
- `source/db/sql/34_srm_catalog_category.sql`(신규)
- `source/db/sql/35_catalog_form_field_textarea_type.sql`(신규)
- `docs/02_plan/database/service-request.md`

## 4. 테스트 결과

배치1(상태변경 버튼·타임라인) 통합 테스트는 `docs/04_test/20260716-080731`에 기록되어 있으며 7개 도메인 전부 PASS했다.
배치2(카테고리 CRUD) 통합 테스트는 `docs/04_test/20260716-105846`에 기록되어 있으며 PASS했다.
배치3(textarea·승인대기함 상세보기) 통합 테스트는 `docs/04_test/20260716-112347`에 기록되어 있으며 service-request/esm/common 3개 도메인 전부 PASS했다.
커밋 `1759a25`(배치1), `6f04594`(배치2), `69575e1`(배치3)으로 반영했다.
