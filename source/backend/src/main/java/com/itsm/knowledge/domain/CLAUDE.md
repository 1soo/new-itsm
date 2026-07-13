# CLAUDE.md

knowledge 도메인 엔티티·enum·리포지토리 계약.

## 파일
- `KnowledgeArticle.java` — 지식 기사 엔티티(제목/본문·상태·분류·유용성/조회 집계)
- `KnowledgeCategory.java` — 카테고리 엔티티
- `KnowledgeLabel.java` — 라벨 엔티티
- `ArticleLabel.java` — 기사-라벨 매핑(N:M) 엔티티
- `KnowledgeFeedback.java` — 유용성 평가 엔티티
- `SearchLog.java` — 검색 로그 엔티티(append-only, BaseEntity 미상속)
- `ArticleStatus.java` — 기사 상태 enum(DRAFT, IN_REVIEW, PUBLISHED)

> `KnowledgeReview`/`ReviewDecision`(검토·게시 승인/반려 이력)은 승인 프로세스 커스텀 기능(2026-07-11)으로 제거 — `knowledge_review` DB 테이블은 유지되나 대응 JPA 엔티티 없음(공용 승인 엔진 `common.approval`의 `approval_decision`이 대체).

## 하위 디렉토리
- `repository/` — 리포지토리 인터페이스
