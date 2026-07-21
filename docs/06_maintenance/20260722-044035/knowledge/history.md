---
date: 20260722-044035
domain: knowledge
change_type: [new, modified]
keywords: [targetState, 상태별승인게이트, non-throwing게이트]
---

# 유지보수 이력 — knowledge

> 유지보수 일시: 20260722-044035 · 도메인: knowledge

## 1. 요구사항

지식 문서의 "초안-검토중-게시" 각 상태 전이마다 독립적으로 승인자를 지정할 수 있어야 한다.
최초 상태(초안)에도 승인 게이트를 걸 수 있어야 하며, 마스터 레코드는 제출 즉시 생성하고 승인 전까지는 임시/승인대기로 표시해야 한다.
승인 반려 시 재승인요청이 가능해야 한다.

## 2. 해결 방법

기존 검토요청 non-throwing 게이트(`evaluateAndCreateIfNeeded`)가 `targetState="IN_REVIEW"`를 전달하도록 확장했다(호출 지점 이관만 필요, 신규 게이트 지점 없음).
`create()`(초안 생성)에는 신규로 throwing 방식의 `TicketCreationGateSupport` 기반 `DRAFT`(최초 상태) 게이트를 추가했다.
게이트 호출 시 requesterId를 티켓의 원 요청자 필드가 아니라 그 전이를 지금 호출하는 현재 사용자로 통일했다.
상세 응답 DTO에 `targetState`를 추가하고, 프론트엔드 상세/목록 화면에 공용 `deriveApprovalStatusDisplay` 기반 파생 승인대기/반려 배지를 적용했다.

## 3. 변경 파일

- `source/backend/src/main/java/com/itsm/knowledge/application/{KnowledgeService,dto/KnowledgeArticleDetailResponse}.java`
- `source/frontend/src/features/knowledge/{types.ts,status.ts,KnowledgeDetailPage.tsx,KnowledgeListPage.tsx}`
- `docs/02_plan/api_spec/knowledge.md`

## 4. 테스트 결과

공용 승인엔진 통합 테스트 라운드에 포함되어 검증됐다(`docs/04_test/20260722-040618`, `docs/04_test/20260722-042424`).
KNOWLEDGE 도메인 고유 결함은 발견되지 않았다.
코드 리뷰 발견 사항 없었다.
커밋 `c8f9386`으로 반영했다.
