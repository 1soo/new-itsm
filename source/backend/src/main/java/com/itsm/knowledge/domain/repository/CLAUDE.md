# CLAUDE.md

knowledge 도메인의 리포지토리 인터페이스.

## 파일
- `KnowledgeArticleRepository.java` — 지식 기사(KnowledgeArticle) 저장·조회·역할 기반 검색
- `KnowledgeCategoryRepository.java` — 카테고리(KnowledgeCategory) 저장·조회
- `KnowledgeLabelRepository.java` — 라벨(KnowledgeLabel) 저장·이름 조회(find-or-create)
- `ArticleLabelRepository.java` — 기사-라벨 매핑(ArticleLabel) 저장·조회·삭제
- `KnowledgeFeedbackRepository.java` — 유용성 평가(KnowledgeFeedback) 저장
- `KnowledgeReviewRepository.java` — 검토 이력(KnowledgeReview) 저장
- `SearchLogRepository.java` — 검색 로그(SearchLog) 저장·집계 조회
