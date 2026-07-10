# 역할 정의 — INFRA_OPERATOR

> 역할: INFRA_OPERATOR · 버전: 0.1 · 작성일: 2026-07-10

## 공통 기본 접근 (전 역할 공통)

앱 셸/공통 컴포넌트(SCR-COM-*), 로그인·프로필·비밀번호(SCR-AUTH-001~003), 404, 자기 인증 API(API-AUTH-003/004/005), 통합 검색 API(API-SEARCH-001, 접근 가능 도메인만 결과 포함). SYSTEM_ADMIN은 모든 역할의 화면·API에 예외적으로 접근 가능하다(system_admin.md 2절 참조). 이하 역할 고유 접근만 기술한다.

## 1. 페르소나

- **역할명**: INFRA_OPERATOR (인프라 운영 담당자)
- **설명**: 인프라 자산의 가동률·성능 지표를 수동 기록하고 임계치·SLA를 관리하며, 팀/서비스 용량 계획을 수립하는 운영 담당자.
- **주요 목표**: 지표 이상 징후를 조기에 포착하고 SLA 준수·용량 여유를 유지한다.

## 2. 접근 가능 화면

| 화면 ID | 화면명 | 비고 |
|---------|--------|------|
| SCR-IOM-001 | 인프라 지표 등록 | |
| SCR-IOM-002 | 지표 대시보드 | SLA 대비 가동률 포함 |
| SCR-IOM-003 | 임계치 설정·알림 목록 | |
| SCR-IOM-004 | 용량 계획 관리 | |
| SCR-IOM-005 | 인프라 지표 리포팅 | |

## 3. 접근 가능 API

| API ID | Endpoint | Method | 비고 |
|--------|----------|--------|------|
| API-IOM-001 | /api/v1/infra/metrics | POST | 지표 등록 |
| API-IOM-002 | /api/v1/infra/metrics | GET | 시계열 조회 |
| API-IOM-003 | /api/v1/infra/metric-thresholds | GET | 임계치 목록 |
| API-IOM-004 | /api/v1/infra/metric-thresholds/{metricType} | PUT | 임계치 설정 |
| API-IOM-005 | /api/v1/infra/metric-alerts | GET | 알림 목록 |
| API-IOM-006 | /api/v1/infra/metric-alerts/{id}/acknowledge | PATCH | 알림 확인 처리 |
| API-IOM-007 | /api/v1/infra/assets/{assetId}/uptime-target | PUT | 가동률 목표 설정 |
| API-IOM-008 | /api/v1/infra/assets/{assetId}/uptime | GET | 가동률 현황 조회 |
| API-IOM-009 | /api/v1/infra/capacity-plans | POST | 용량 계획 등록 |
| API-IOM-010 | /api/v1/infra/capacity-plans | GET | 용량 계획 목록 |
| API-IOM-011 | /api/v1/infra/metrics/report | GET | 지표 리포팅 |

## 4. 접근 제어 규칙

- 로그인 필요 화면/요청 접근 시 Access Token의 `role`을 검증한다.
- 이 역할에 없는 화면/요청 → **403**, 있는 경우 → 정상 수행.
- 화면 이동마다 Backend에 권한 확인 API를 호출한다.
