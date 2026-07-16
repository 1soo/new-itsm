---
date: 20260710-071918
domain: asset
result: pass
keywords: [자산-티켓 양방향 연계, 회귀테스트]
---

# 통합 테스트 결과 — asset (ITAM) 재테스트 (20260710-071918)

> 환경: React CSR(localhost:5173, 재빌드) / Spring Boot(:8080, 재기동) / PostgreSQL(itsm-postgres, healthy)
> 대상: 직전 실행(20260710-064414) 실패 1건(TC-REG-002) 수정분 + 4개 도메인(인시던트/문제/변경/서비스요청) 상세 조회 회귀

## 요약

- 총 **12건** (빌드 2 · 양방향 연계 4 · 회귀 2 · FE 회귀 3, 자산측 회귀 1) · **성공 12 · 실패 0**
- 직전 실패 1건(TC-REG-002) **수정 확인**

## 상세

| TC ID | 결과 | 실제 동작 | 비고 |
|-------|------|-----------|------|
| TC-BUILD-001 (BE gradlew test --rerun-tasks) | PASS | `BUILD SUCCESSFUL`(4 tasks executed), AssetService 양방향 저장 + problem/change/srm DTO 변경분 포함 전체 그린 | |
| TC-BUILD-002 (FE npm run build) | PASS | `tsc -b && vite build` 정상 완료(1866 modules, TS 오류 없음), problem/change/srm 상세 "연결 자산" UI 추가분 포함 | |
| TC-REG-002-1 (인시던트 상세 ASSET 링크) | PASS | 자산(6)→인시던트(23) 재연계 후 `GET /incidents/23` → `links:[{"type":"ASSET","targetKey":"AST-0002"}]` 노출 확인 | 이전 FAIL(빈 배열) → 이번 PASS |
| TC-REG-002-2 (문제 상세 linkedAssets) | PASS | 자산(6)→문제(27) 재연계 후 `GET /problems/27` → `linkedAssets:[{"id":6,"ticketKey":"AST-0002"}]` 노출(신설 필드) | 이전 FAIL → 이번 PASS |
| TC-REG-002-3 (변경 상세 ASSET 링크) | PASS | 자산(6)→변경(18) 재연계 후 `GET /changes/18` → `links:[{"type":"ASSET","targetKey":"AST-0002"}]`(계약 확장: INCIDENT\|PROBLEM→INCIDENT\|PROBLEM\|ASSET) | 이전 FAIL → 이번 PASS |
| TC-REG-002-4 (서비스요청 상세 linkedAssets) | PASS | 자산(6)→SR(21) 재연계 후 `GET /service-requests/21` → `linkedAssets:[{"id":6,"assetKey":"AST-0002"}]`(신설 필드) | 이전 FAIL → 이번 PASS |
| TC-REG-002-5 (미연계 문제 상세 회귀) | PASS | 자산 미연계 문제(25) 상세 조회 시 `linkedAssets:[]`, 200 정상 렌더링(오류 없음). 기존 linkedIncidents/linkedChanges도 정상 유지 | 회귀 없음 |
| TC-REG-002-6 (자산 측 조회 회귀) | PASS | 자산(6) 상세 `linkedTickets`에 CHANGE/INCIDENT/PROBLEM/SERVICE_REQUEST 4종 모두 여전히 정상 반영(양방향 저장 후에도 자산 측 조회 영향 없음) | 회귀 없음 |
| TC-E2E-REG-001 (문제 상세 FE) | PASS | `/problems/27`에 "연결 자산" 사이드바 섹션 신설, "AST-0002" 버튼 표시 | |
| TC-E2E-REG-002 (변경 상세 FE) | PASS | `/changes/18`의 "연계 항목"에 "ASSET · AST-0002" 표시 | |
| TC-E2E-REG-003 (서비스요청 상세 FE) | PASS | `/service-requests/21`에 "연결 자산" 섹션 신설, "AST-0002" 표시 | |

## 실패 항목 분석

- 없음 (TC-REG-002 해소, 4개 도메인 상세 조회 회귀 없음)

## 결론

- **TC-REG-002(자산-티켓 단방향 연계) 수정 확인.** `AssetService.linkAsset()`이 이제 양방향(`ASSET↔ticketType`)으로 링크를 저장하며, 인시던트(계약 그대로 재사용)·변경(계약 확장: `links[].type`에 ASSET 추가)·문제/서비스요청(신설 `linkedAssets` 필드)에서 모두 정상 노출됨을 확인했다.
- 미연계 티켓 상세 조회, 자산 측 `linkedTickets` 조회 모두 회귀 없이 정상 동작.
- FE 3개 도메인(문제/변경/서비스요청) 상세 화면에 "연결 자산" 표시가 추가되어 정상 렌더링됨을 확인.
- asset 도메인(7/7 마지막) 재테스트 전건 통과. 잔여 실패 없음 — 7개 도메인 전체 완료로 판단.
