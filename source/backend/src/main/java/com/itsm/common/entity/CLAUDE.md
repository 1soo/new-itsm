# CLAUDE.md

전 도메인 공통 엔티티 기반 클래스.

## 파일
- `BaseEntity.java` — 공통 컬럼(created_by/at, updated_by/at, is_deleted)을 담는 `@MappedSuperclass`. append-only 테이블(refresh_token, audit_log)은 상속하지 않는다
