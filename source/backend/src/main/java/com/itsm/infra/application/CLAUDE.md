# CLAUDE.md

infra 도메인의 애플리케이션 서비스 계층.

## 파일
- `InfraMonitoringService.java` — 지표 등록(임계치 초과 알림 생성)·시계열 조회·임계치 목록/설정·알림 목록/확인처리·자산 가동률 목표설정/현황조회·용량계획 등록/목록·인프라 지표 리포팅 유스케이스(INFRA_OPERATOR 전용)

## 하위 디렉토리
- `dto/` — 요청·응답 DTO(record)
