---
date: 20260715-031710
domain: common
change_type: [modified]
keywords: [domain nullable화, 우선순위매칭, tier재계산]
---

# 유지보수 이력 — common

> 유지보수 일시: 20260715-031710 · 도메인: common

## 1. 요구사항

승인 프로세스 범위별 우선순위 재설계(auth 도메인 요구사항)를 지원하기 위해, 공용 승인 엔진이 도메인을 지정하지 않은(전체 도메인 적용) 규칙을 스키마·매칭 로직 양쪽에서 지원해야 한다.

## 2. 해결 방법

`approval_process` 테이블의 `domain` 컬럼을 NOT NULL에서 nullable로 변경했다.
기존 데이터의 `priority_tier` 값을 신규 산정식(축개수×10+역할4+요청유형2+도메인1) 기준으로 재계산해 백필했다.
기존 tier=1/2 부분 유니크 인덱스(`is_deleted=false` 조건 포함)를 신규 tier값 기준(tier=0/11/23) 3종 인덱스로 재작성했다.
`ApprovalGateService.matchProcess`가 티켓의 도메인과 정확히 일치하는 규칙뿐 아니라 도메인이 null인(전체 도메인) 규칙도 함께 후보로 조회하도록 수정했다.
후보 중 `priorityTier`가 가장 큰 규칙을 선택하도록 했다.

## 3. 변경 파일

- `source/db/sql/32_approval_process_priority_redesign.sql`
- `source/backend/src/main/java/com/itsm/common/approval/domain/ApprovalProcess.java`
- `source/backend/src/main/java/com/itsm/common/approval/application/ApprovalGateService.java`
- `source/backend/src/main/java/com/itsm/common/approval/domain/repository/ApprovalProcessRepository.java`

## 4. 테스트 결과

통합 테스트 14건 전부 PASS했다(`docs/04_test/20260715-112437/common/`).
전체 구조 점검 과정에서 change/knowledge/srm 도메인의 기존 통합 테스트가 raw SQL로 구 tier 체계(1/2/3) 값을 하드코딩하고 있던 것을 발견해, 신규 산정값(23/25) 기준으로 정리했다(기능 영향 없음, 테스트 정확성 개선).
커밋 `35c2375`(본 기능), `82a173c`(stale tier 하드코딩 정리)로 반영했다.
