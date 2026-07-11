# CLAUDE.md

관리자(계정/역할/감사 로그/메뉴/승인 프로세스) 기능. SYSTEM_ADMIN 전용 화면과 API·타입을 제공한다(미보유 시 403). API 계약은 auth.md(API-AUTH-006~029) 기준.

## 파일
- `api.ts` — admin API 호출(`adminApi`: 계정 목록/생성/상세/수정/상태변경, 역할 부여·회수·목록·생성, 감사 로그 조회, 메뉴(화면) 목록/생성/수정/삭제·역할 매핑 부여/회수, 승인 프로세스 대상 도메인/요청유형 후보 조회·CRUD). 빈 쿼리 파라미터 제거 헬퍼 포함.
- `types.ts` — admin 도메인 타입(`UserSummary`/`UserDetail`/`Role`/`AuditLog`/`Screen`(메뉴), 쿼리·생성 요청, 공통 `PageResponse<T>`, 승인 프로세스(`ApprovalDomain`/`ApprovalDomainOption`/`RequestSubtypeOption`/`ApprovalProcessSummary`/`ApprovalProcessDetail`/`ApprovalProcessStep` 등) 기준).
- `UserListPage.tsx` — 계정 목록(SCR-ADMIN-001).
- `UserCreatePage.tsx` — 계정 생성(SCR-ADMIN-002).
- `UserDetailPage.tsx` — 계정 상세·수정·역할 관리(SCR-ADMIN-003).
- `RoleManagementPage.tsx` — 역할 관리(SCR-ADMIN-004).
- `AuditLogPage.tsx` — 감사 로그 조회(SCR-ADMIN-005).
- `MenuManagementPage.tsx` — 메뉴 관리(SCR-ADMIN-006, Role-Menu 동적 매핑). 메뉴(화면) CRUD 모달 + 역할 매핑 우측 슬라이드 패널(체크박스 토글마다 즉시 반영). 아이콘 미리보기는 `lib/icon.ts`의 `resolveIcon` 재사용.
- `ApprovalProcessListPage.tsx` — 승인 프로세스 목록(SCR-ADMIN-007, 승인 프로세스 커스텀 기능). 도메인 필터 + 규칙명·도메인·요청유형·요청자 역할·우선순위 tier·차수 수 표.
- `ApprovalProcessFormPage.tsx` — 승인 프로세스 생성/편집(SCR-ADMIN-008). "규칙 정보"(규칙명·설명 입력) 카드는 이 화면이 직접 렌더링하고(공용 컴포넌트에 없는 필드), 그 아래 0~3단계 카드 스택·역할 선택 슬라이드 패널·드래그 재정렬·박스별 필수역할 검증·승인자 0개 확인 다이얼로그는 공용 `ApprovalProcessFlow`(`components/common`)가 담당한다. 이 화면은 도메인/요청유형/이름 상태와 API 연동(도메인·역할 후보 조회, 생성/수정 payload 조립: `requester.roleIds`/`approvers[].{roleIds,matchType}`는 string ID → number 변환)을 조립한다. 편집 시 domain·requestSubtypeKey는 식별 스코프라 `domainDisabled`/`requestSubtypeDisabled` prop으로 선택을 비활성화한다(API-AUTH-028).
