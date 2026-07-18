---
date: 20260712-014911
domain: knowledge
change_type: [new, modified, removed]
keywords: [공용승인엔진이관, 게이트키퍼검토승인, 자동상태전이, 전용검토화면제거]
---

# 유지보수 이력 — knowledge

> 유지보수 일시: 20260712-014911 · 도메인: knowledge

## 1. 요구사항

게시 승인(게이트키퍼 검토)을 공용 승인 엔진으로 이관해야 한다.
승인 완료 시 사용자의 별도 조작 없이 기사 상태가 자동으로 PUBLISHED로 전환되어야 한다.
반려 시 기사 상태가 자동으로 DRAFT로 전환되어야 한다.

## 2. 해결 방법

`KnowledgeService`를 공용 엔진 연동으로 수정하고, `KnowledgeApprovalDecisionCallback`(승인/반려 후속 자동 전이)과 `KnowledgeApprovalTicketSummaryProvider`를 신규 구현했다.
이 과정에서 공용 승인 엔진에 `ApprovalDecisionCallback` SPI를 신규 추가했다(common 도메인 확장).
기존 전용 검토 승인 도메인(KnowledgeReview, ReviewDecision)과 리포지토리, 전용 컨트롤러(KnowledgeReviewController), 전용 화면(ReviewInboxPage)을 제거했다.

## 3. 변경 파일

- `source/backend/.../common/approval/application/{ApprovalDecisionCallback.java(신규),ApprovalGateService.java,ApprovalInstanceService.java}`
- `source/backend/.../knowledge/application/KnowledgeService.java`
- `source/backend/.../knowledge/application/dto/*`
- `source/backend/.../knowledge/application/{KnowledgeApprovalDecisionCallback,KnowledgeApprovalTicketSummaryProvider}.java`(신규)
- `source/backend/.../knowledge/domain/{KnowledgeReview,ReviewDecision}.java`(제거, repository/persistence 포함)
- `source/backend/.../knowledge/presentation/{KnowledgeArticleController,KnowledgeReviewController(제거)}.java`
- `test/.../knowledge/application/{KnowledgeServiceTest,KnowledgeApprovalTicketSummaryProviderTest(신규)}.java`
- `test/.../knowledge/integration/KnowledgeIntegrationTest.java`
- `source/db/sql/29_knowledge_km004_cleanup.sql`
- `source/frontend/src/features/knowledge/{ArticleEditPage.tsx,ReviewInboxPage.tsx(제거),api.ts,types.ts}`
- `source/frontend/src/routes/index.tsx`

## 4. 테스트 결과

Stage 3 통합 테스트 총 9건 전부 PASS했다.
매칭 규칙이 없으면 게이트 없이 즉시 PUBLISHED, 매칭 규칙이 있으면 IN_REVIEW로 전환되고 게이트키퍼 승인 시 별도 전이 요청 없이 자동 PUBLISHED, 반려 시 자동 DRAFT로 전환됨을 확인했다.
반려 후 재검토 요청 시 신규 인스턴스가 생성되고 이전 반려 인스턴스는 이력으로 보존됨을 확인했다.
기존 전용 검토 승인 화면(SCR-KM-004)이 DB·사이드바에서 완전히 제거되었음을 확인했다.
커밋 `9c51fa0`으로 반영했다.
