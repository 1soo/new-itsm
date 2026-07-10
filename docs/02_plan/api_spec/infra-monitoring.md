# API 명세서 — IT 인프라 모니터링 & 용량관리 (Infra Monitoring & Capacity Management)

> 도메인: infra-monitoring · 버전: 0.1 · 작성일: 2026-07-10

## 공통 규약

- **Base Path**: `/api/v1` · 표준 오류 응답 본문은 [auth.md](auth.md) 공통 규약과 동일.
- 모든 API는 `Authorization: Bearer {accessToken}` **필요**.
- 지표 항목(`metricType`): `UPTIME|CPU|MEMORY|RESPONSE_TIME`

## 0. 설계 배경

- 실시간 수집 없이 수동 입력 기반이므로(범위 제외 사항), 지표는 `자산(assetId) + 지표 항목(metricType) + 측정 시각(measuredAt) + 값`의 단순 레코드로 저장한다. 대상 자산은 [asset.md](asset.md)의 자산을 참조한다.
- 임계치(REQ-IOM-003)는 **지표 항목 단위(전역)** 로 설정한다(요구사항이 "지표 항목별 임계치"로 명시, 자산별 개별 임계치는 범위 밖). 지표 등록 시점에 해당 항목의 임계치와 비교해 초과하면 알림 레코드를 생성한다. 임계치가 설정되지 않은 항목은 비교를 생략하고 알림을 생성하지 않는다(FEAT-IOM-003 Unwanted).
- 가동률 목표(SLA)는 자산별로 저장하며, 실제 가동률은 해당 자산의 `metricType='UPTIME'` 레코드 평균값을 조회 시점에 계산한다(별도 컬럼으로 캐시하지 않음).
- 용량 계획의 활용률(`utilizationRate` = demand/capacity)도 조회 시점 계산값이며 저장하지 않는다.

## 1. API 목록

| API ID | 기능 | Method | Endpoint | 인증 |
|--------|------|--------|----------|------|
| API-IOM-001 | 지표 등록 | POST | /api/v1/infra/metrics | 필요 |
| API-IOM-002 | 지표 시계열 조회 | GET | /api/v1/infra/metrics | 필요 |
| API-IOM-003 | 임계치 목록 조회 | GET | /api/v1/infra/metric-thresholds | 필요 |
| API-IOM-004 | 임계치 설정 | PUT | /api/v1/infra/metric-thresholds/{metricType} | 필요 |
| API-IOM-005 | 임계치 초과 알림 목록 조회 | GET | /api/v1/infra/metric-alerts | 필요 |
| API-IOM-006 | 임계치 초과 알림 확인 처리 | PATCH | /api/v1/infra/metric-alerts/{id}/acknowledge | 필요 |
| API-IOM-007 | 자산 가동률 목표 설정 | PUT | /api/v1/infra/assets/{assetId}/uptime-target | 필요 |
| API-IOM-008 | 자산 가동률 현황 조회 | GET | /api/v1/infra/assets/{assetId}/uptime | 필요 |
| API-IOM-009 | 용량 계획 등록 | POST | /api/v1/infra/capacity-plans | 필요 |
| API-IOM-010 | 용량 계획 목록 조회 | GET | /api/v1/infra/capacity-plans | 필요 |
| API-IOM-011 | 인프라 지표 리포팅 조회 | GET | /api/v1/infra/metrics/report | 필요 |

## 2. API 상세

### API-IOM-001 · 지표 등록

- **Endpoint**: `POST /api/v1/infra/metrics`
- **인증**: 필요
- **Header**: `Content-Type: application/json`
- **Request Body**: `{ "assetId": "number · 필수", "metricType": "UPTIME|CPU|MEMORY|RESPONSE_TIME · 필수", "value": "number · 필수", "measuredAt": "ISO-8601" }`
- **Response Body** (201): `{ "id": "number", "alertGenerated": "boolean · 임계치 초과 알림 생성 여부" }`
- **Response Code**: 201 / 400 자산·지표값 누락 / 404 존재하지 않는 자산

### API-IOM-002 · 지표 시계열 조회

- **Endpoint**: `GET /api/v1/infra/metrics?assetId=&metricType=&from=&to=`
- **인증**: 필요
- **Response Body** (200):
  ```json
  [ { "id": "number", "metricType": "string", "value": "number", "measuredAt": "ISO-8601" } ]
  ```
- **Response Code**: 200(기간 내 지표 없으면 빈 배열) / 401

### API-IOM-003 · 임계치 목록 조회

- **Endpoint**: `GET /api/v1/infra/metric-thresholds`
- **인증**: 필요
- **Response Body** (200): `[ { "metricType": "string", "upperLimit": "number|null", "lowerLimit": "number|null" } ]`
- **Response Code**: 200 / 401

### API-IOM-004 · 임계치 설정

- **Endpoint**: `PUT /api/v1/infra/metric-thresholds/{metricType}`
- **인증**: 필요
- **Request Body**: `{ "upperLimit": "number|null", "lowerLimit": "number|null" }`
- **Response Code**: 200 / 400 정의되지 않은 지표 항목

### API-IOM-005 · 임계치 초과 알림 목록 조회

- **Endpoint**: `GET /api/v1/infra/metric-alerts?assetId=&acknowledged=`
- **인증**: 필요
- **Response Body** (200):
  ```json
  [ { "id": "number", "assetKey": "string", "metricType": "string", "value": "number", "thresholdType": "UPPER|LOWER", "acknowledged": "boolean", "occurredAt": "ISO-8601" } ]
  ```
- **Response Code**: 200 / 401

### API-IOM-006 · 임계치 초과 알림 확인 처리

- **Endpoint**: `PATCH /api/v1/infra/metric-alerts/{id}/acknowledge`
- **인증**: 필요
- **Response Code**: 200 / 404

### API-IOM-007 · 자산 가동률 목표 설정

- **Endpoint**: `PUT /api/v1/infra/assets/{assetId}/uptime-target`
- **인증**: 필요
- **Request Body**: `{ "targetPercentage": "number · 필수" }`
- **Response Code**: 200 / 400 / 404 존재하지 않는 자산

### API-IOM-008 · 자산 가동률 현황 조회

- **Endpoint**: `GET /api/v1/infra/assets/{assetId}/uptime?from=&to=`
- **인증**: 필요
- **Response Body** (200): `{ "assetKey": "string", "targetPercentage": "number|null", "actualPercentage": "number|null", "met": "boolean|null · 목표 미설정 시 null" }`
- **Response Code**: 200 / 404

### API-IOM-009 · 용량 계획 등록

- **Endpoint**: `POST /api/v1/infra/capacity-plans`
- **인증**: 필요
- **Request Body**: `{ "teamOrService": "string · 필수", "capacity": "number · 필수", "demand": "number · 필수" }`
- **Response Body** (201): `{ "id": "number" }`
- **Response Code**: 201 / 400 역량·수요 누락

### API-IOM-010 · 용량 계획 목록 조회

- **Endpoint**: `GET /api/v1/infra/capacity-plans`
- **인증**: 필요
- **Response Body** (200):
  ```json
  [ { "id": "number", "teamOrService": "string", "capacity": "number", "demand": "number", "utilizationRate": "number" } ]
  ```
- **Response Code**: 200 / 401

### API-IOM-011 · 인프라 지표 리포팅 조회

- **Endpoint**: `GET /api/v1/infra/metrics/report?from=&to=&assetId=`
- **인증**: 필요
- **Response Body** (200):
  ```json
  { "avgUptime": "number", "avgCpu": "number", "avgMemory": "number", "avgResponseTime": "number", "avgCapacityUtilization": "number" }
  ```
- **Response Code**: 200(데이터 없으면 빈 결과) / 401
