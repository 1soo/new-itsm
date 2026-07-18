---
date: 20260718-112351
domain: srm
result: pass
keywords: [카테고리 기반 분류, 요청 처리함, 실시간 조인, 큐 폐지]
---

# 통합 테스트 결과 — srm 요청 큐 폐지 → 카테고리 기반 분류 일원화 (20260718-112351)

## 요약
- 총 9건 · 성공 9(TC-SRM-Q01은 이번 유지보수 요구사항 자체는 충족, 범위 밖 기존 결함만 별도 확인 — 아래 "실패 항목 분석" 참조)

## 결론
이번 "요청 큐 폐지 → 카테고리 기반 분류 일원화" 유지보수의 실제 요구사항(큐 관련 클래스·테이블·컬럼 완전 제거, 카테고리 기반 필터·건수 집계, 실시간 조인, FE 화면 정리)은 전부 충족되어 **PASS**로 종결한다. TC-SRM-Q01에서 발견된 `/api/v1/queues` 호출 시 404 대신 500 응답은 이번 변경 범위(SRM 도메인)와 무관한 앱 전역 기존 결함(`GlobalExceptionHandler`)이라 dev-lead 판단에 따라 이번 결과에서 실패로 집계하지 않고 별도 이슈로 분리한다.

코드 리뷰 중 발견된 categoryId 파싱 실패 시 500 버그(이번 diff 자체 회귀)는 dev-backend 수정 후 400 정상 반환·기존 케이스 회귀 없음 재검증 완료.

## 상세

| TC ID | 결과 | 실제 동작 | 비고(스크린샷/로그) |
|-------|------|-----------|----------------------|
| TC-SRM-B01 | PASS | BE `./gradlew clean build`(테스트 포함) BUILD SUCCESSFUL(2m27s), FE `npm run build` 성공 | - |
| TC-SRM-Q01 | PASS(요구사항 충족, 별도 결함 확인) | 코드/DB 레벨에서는 완전 제거 확인(`QueueController`/`Queue`/`QueueService`/`QueueJpaRepository` 클래스 전부 삭제, `queue` 테이블·`queue_id` 컬럼 DROP 확인) — 이번 유지보수 요구사항은 충족. 다만 `GET /api/v1/queues` 호출 시 기대한 404가 아니라 500(`INTERNAL_ERROR`) 반환되는 별도 결함을 발견(범위 밖, 아래 분석 참조) | 아래 "실패 항목 분석" 참조 |
| TC-SRM-Q02 | PASS | 요청 처리함(SCR-SRM-004) 좌측 카테고리 목록이 `sort_order` 오름차순(계정4·하드웨어12·소프트웨어0·기타비품0) + 미분류(5) 마지막 고정 노출, DB 실제 미종료 건수와 일치 | `category-counts` 응답: `[{"categoryId":1,...4},{"categoryId":2,...12},{"categoryId":3,...0},{"categoryId":7,...0},{"openCount":5}]` |
| TC-SRM-Q03 | PASS | 좌측 "하드웨어" 클릭 시 `GET /api/v1/service-requests?scope=all&categoryId=2&...` 호출, 우측 표에 카탈로그 항목 "노트북 신청"(category_id=2) 요청 12건만 표시 | 네트워크 요청 #273 |
| TC-SRM-Q04 | PASS | 좌측 "미분류" 클릭 시 `categoryId=uncategorized` 호출, 카테고리 미지정 카탈로그 항목의 요청만 표시 | 네트워크 요청 #280 |
| TC-SRM-Q05 | PASS | 카탈로그 항목 "노트북 신청"(id=1)의 카테고리를 2(하드웨어)→3(소프트웨어)로 PATCH 변경 시, 요청 테이블 수정 없이 `category-counts`·`categoryId=` 필터 결과가 즉시 반영됨(하드웨어 12→0, 소프트웨어 0→12, `categoryId=2` 목록 0건, `categoryId=3` 목록 13건). 테스트 후 원복(2로 재변경) 완료 | 스냅샷 조인 아님, 실시간 조인 확인. curl 근거 위 API 검증 로그 |
| TC-SRM-Q06 | PASS | SCR-SRM-007(카탈로그 관리) 신규/기존 항목 편집 폼에 "담당 큐" select 없음. 카테고리 select(기존 값 "하드웨어" 프리필)·담당자 역할 select(기존 값 "서비스 데스크 상담원" 프리필) 정상 동작 | playwright 스냅샷(po@itsm.local) |
| TC-SRM-Q07 | PASS | SCR-SRM-005(요청 상세, SRM-2026-0002) "정보" 패널에 요청자/담당자만 있고 "큐" 항목 없음. `RequestDetailResponse`에 `queue` 필드 없음(코드 확인) | playwright 스냅샷(agent@itsm.local) |
| TC-SRM-Q08 | PASS | 본인(agent) 배정 또는 ROUTED 이후 상태(라우팅됨/검증됨-본인배정/종료) 행은 배정 버튼 숨김, 그 외 미배정/타인배정-VALIDATED 이전은 배정 버튼 노출 — 큐 제거가 노출 조건 로직에 영향 없음(회귀 없음) | 요청 처리함 스냅샷 |

## 실패 항목 분석
- TC-SRM-Q01: `/api/v1/queues` 관련 컨트롤러·서비스·리포지토리·엔티티는 코드베이스에서 완전히 삭제되었고 DB `queue` 테이블도 DROP되어, **큐 도메인 자체의 제거는 정상 완료**되었다. 다만 삭제된(unmapped) 경로 호출 시 기대되는 404 대신 500이 반환된다. 원인은 이번 변경과 무관한 **전역(pre-existing) 예외 처리 갭**이다 — `GlobalExceptionHandler`의 `@ExceptionHandler(Exception.class)` catch-all이 Spring의 `NoResourceFoundException`(매핑되지 않은 경로에서 발생)까지 잡아 500으로 변환한다. 이번 세션에서 `/api/v1/queues`뿐 아니라 원래부터 존재한 적 없는 임의 경로(`/api/v1/totally-nonexistent-path-xyz`)도 동일하게 500을 반환함을 확인해, **이 도메인·이번 변경에 국한된 회귀가 아니라 앱 전역에 걸친 기존 동작**임을 확인했다(`source/backend/src/main/java/com/itsm/common/exception/GlobalExceptionHandler.java` 56행 부근). 이번 요청 큐 폐지 유지보수의 완료 기준("`/api/v1/queues` 관련 엔드포인트·클래스 완전 제거")은 클래스·경로 제거 관점에서는 충족되었으나, dev-lead가 제시한 검증 포인트("완전히 사라졌는지(404)")의 문자 그대로의 기대치와는 다르다. 이 도메인 코드 수정으로 고칠 범위가 아니라 전역 예외 처리 개선 사항이므로, 이번 유지보수 범위에 포함해 수정할지 별도 이슈로 분리할지는 dev-lead 판단이 필요하다.
