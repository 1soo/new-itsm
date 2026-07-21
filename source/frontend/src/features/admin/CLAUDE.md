# CLAUDE.md

관리자(계정/역할/감사 로그/메뉴/승인 프로세스) 기능. SYSTEM_ADMIN 전용 화면·API·타입 제공(미보유 시 403). API 계약: auth.md(API-AUTH-006~029).

## 파일
- `api.ts` — admin API 호출(`adminApi`: 계정 목록/생성/상세/수정/상태변경, 역할 부여·회수·목록·생성, 감사 로그 조회, 메뉴(화면) 목록/생성/수정/삭제·역할 매핑 부여/회수, 승인 프로세스 대상 도메인/요청유형/적용 상태(`listApprovalStates`, API-AUTH-031, 2026-07-22 유지보수 요청) 후보 조회·CRUD). 빈 쿼리 파라미터 제거 헬퍼 포함.
- `types.ts` — admin 도메인 타입(`UserSummary`/`UserDetail`/`Role`/`AuditLog`/`Screen`(메뉴), 쿼리·생성 요청, 공통 `PageResponse<T>`, 승인 프로세스(`ApprovalDomain`/`ApprovalDomainOption`/`RequestSubtypeOption`/`TargetStateOption`(적용 상태 후보, 2026-07-22 신규)/`ApprovalProcessSummary`/`ApprovalProcessDetail`/`ApprovalProcessStep` 등, Summary·Detail·CreateApprovalProcessRequest에 `targetState`/`targetStateLabel` 추가)).
- `UserListPage.tsx` — 계정 목록(SCR-ADMIN-001).
- `UserCreatePage.tsx` — 계정 생성(SCR-ADMIN-002).
- `UserDetailPage.tsx` — 계정 상세·수정·역할 관리(SCR-ADMIN-003).
- `RoleManagementPage.tsx` — 역할 관리(SCR-ADMIN-004).
- `AuditLogPage.tsx` — 감사 로그 조회(SCR-ADMIN-005).
- `MenuManagementPage.tsx` — 메뉴 관리(SCR-ADMIN-006, Role-Menu 동적 매핑). 메뉴(화면) CRUD 모달(메뉴명·메뉴 영문명·그룹·그룹 영문명·경로·아이콘 등, 사이드바 메뉴 i18n 미적용 결함 수정으로 영문명 필드 추가 — 2026-07-13 유지보수 요청) + 역할 매핑 우측 슬라이드 패널(체크박스 토글마다 즉시 반영). 그룹 영문명은 기존 그룹 선택 시 저장된 값을 자동 표시 후 수정 가능(신규 그룹은 직접 입력). 아이콘 미리보기는 `lib/icon.ts`의 `resolveIcon` 재사용.
- `ApprovalProcessListPage.tsx` — 승인 프로세스 목록(SCR-ADMIN-007, 승인 프로세스 커스텀 기능). 도메인 필터 + 규칙명·도메인(null="전체")·**적용 상태**(null="전체 상태 공통", 2026-07-22 신규)·요청유형·요청자 역할·우선순위·차수 수·액션(삭제) 표. 우선순위 배지는 2026-07-15 유지보수 요청으로 tier 3종 라벨을 폐기하고, 행의 도메인/적용상태/요청유형/요청자 역할 지정 여부를 조합해 FE가 직접 라벨링(`priorityLabel`, `priorityTier`는 서버 정렬용으로만 존재, 화면 비노출). 삭제는 `MenuManagementPage.tsx`의 `handleDelete`/`deleteBusy`/`ConfirmDialog` 패턴 재사용(행 클릭=편집 이동과 분리 위해 버튼에서 `stopPropagation`).
- `ApprovalProcessFormPage.tsx` — 승인 프로세스 생성/편집(SCR-ADMIN-008). "규칙 정보" 카드(도메인·**적용 상태**·요청유형·규칙명·설명 순서, 메타데이터 분리 개편으로 도메인·요청유형 선택을 이 카드로 이관 — 2026-07-13 유지보수 요청)는 이 화면이 직접 렌더링, 아래 1(승인 요청자)~2(승인자 n차) 단계 카드 스택·역할 선택 슬라이드 패널·드래그 재정렬·박스별 필수역할 검증·승인자 0개 확인 다이얼로그는 공용 `ApprovalProcessFlow`(`components/common`) 담당. 이 화면은 도메인/적용상태/요청유형/이름 상태와 API 연동(도메인·역할·적용상태 후보 조회, 생성/수정 payload 조립: `requester.roleIds`/`approvers[].{roleIds,matchType}`는 string ID → number 변환) 조립. 편집 시 domain·targetState·requestSubtypeKey는 식별 스코프라 세 Select를 `disabled={isEdit}`로 비활성화(API-AUTH-028). 도메인 select는 API-AUTH-023 9개 목록 + 클라이언트 전용 "전체 도메인" 의사 옵션(`ALL_DOMAIN` 상수, 선택 시 `domain: null`로 전송)을 함께 렌더링(2026-07-15 우선순위 재설계). 요청유형 후보(API-AUTH-024) 조회는 도메인이 확정되고 `hasRequestSubtype=true`이면 생성/편집 모드 공통으로 실행(2026-07-15 결함 수정 — 이전에는 생성 모드에서만 조회해 편집 진입 시 select가 빈칸으로 보였음). **적용 상태(targetState, 2026-07-22 유지보수 요청 신규)**: 도메인이 "전체 도메인"이 아니면 도메인 다음 순서에 렌더링, 도메인 확정 시 API-AUTH-031 후보를 조회해 옵션을 채우고 클라이언트 전용 "전체 상태 공통" 의사 옵션(`ALL_STATES` 상수, 선택 시 `targetState: null`)을 함께 렌더링. 구체적인 상태를 선택하면 `requesterRoleRequired`를 `true`로 `ApprovalProcessFlow`에 전달해 요청자 박스 역할 1개 이상 필수 검증(미충족 시 인라인 에러+저장 버튼 비활성화)을 위임한다.
