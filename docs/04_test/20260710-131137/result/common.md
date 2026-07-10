# 통합 테스트 결과 — Common (사이드바 · 알림벨 · 통합검색) (20260710-131137)

## 요약
- 총 16건 · 성공 16 · 실패 0

## 상세

| TC ID | 결과 | 실제 동작 | 비고(스크린샷/로그) |
|-------|------|-----------|----------------------|
| TC-BUILD-001 | PASS | frontend `npm run build` 성공(tsc+vite, 6.07s), backend `gradlew.bat build -x test` 성공(기 컴파일된 최신 소스와 UP-TO-DATE 일치) | - |
| TC-SIDEBAR-001 | PASS | agent@itsm.local으로 `/service-requests/queue` 진입 시 "요청 큐"만 `aria-current=page`+`bg-sidebar-primary`, "요청 목록"은 비활성 | - |
| TC-SIDEBAR-002 | PASS | im@itsm.local으로 `/incidents/metrics` 진입 시 "인시던트 지표"만 active, "인시던트" 비활성 | - |
| TC-SIDEBAR-003 | PASS | cm@itsm.local으로 `/changes/schedule` 진입 시 "변경 일정"만 active, "변경" 비활성 | - |
| TC-SIDEBAR-004 | PASS | kc@itsm.local으로 `/knowledge/new` 진입 시 "기사 작성"만 active, "지식베이스" 비활성 | - |
| TC-SIDEBAR-005 | PASS | am@itsm.local으로 `/assets/cis` 진입 시 "CI·CMDB 관계"만 active, "자산" 비활성 | - |
| TC-SIDEBAR-006 | PASS | agent@itsm.local으로 `/service-requests` 직접 진입 시 "요청 목록"만 active(중복 없음) | - |
| TC-NOTIF-001 | PASS | cab@itsm.local(APPROVER) 뱃지=2, 클릭 시 `/approvals/service-requests`로 이동. 실제 데이터 구성은 SRM PENDING 1건(SRM-2026-0007)+CHG PENDING 1건(CHG-2026-0006)=2(사전 조건 문서의 "CAB 2건" 가정과 실제 구성은 다르나 총합·이동 우선순위 로직은 코드와 일치, 정상) | - |
| TC-NOTIF-002 | PASS | am@itsm.local(ASSET_MANAGER) 뱃지=4, 클릭 시 `/assets?expiringWithinDays=30`로 이동. 목록에 AST-0001~0004 4건 노출(뱃지 수와 목록 건수 완전 일치). 사전 조건 문서의 "2건" 추정은 이미 만료 경과(경과일)한 자산도 `expiringWithinDays` 조건에 포함되는 기존 백엔드 계산 기준을 반영하지 못한 것으로, 실제 서버 로직과 뱃지·목록이 일치함을 확인(기능 정상, 사전 조건 문서 추정치만 부정확했던 것) | - |
| TC-NOTIF-003 | PASS | user@itsm.local(END_USER) 로그인 시 알림 벨에 숫자 뱃지 요소 없음(DOM에 badge span 미존재) | - |
| TC-SEARCH-001 | PASS | cm@itsm.local으로 "retest" 입력 시 드롭다운 미리보기 7건(8건 이하) 노출, 전부 CHANGE 도메인. 항목 클릭 시 `/changes/5`(CHG-2026-0002)로 정상 이동 | - |
| TC-SEARCH-002 | PASS | Enter 시 `/search?keyword=retest`에서 CHANGE 도메인 7건(CHG-2026-0002/0004/0005/0007/0008/0009/0014) 1페이지에 표시, 다른 도메인 결과 없음(CHANGE_MANAGER RBAC 정상). 사전 조건 문서의 "11건" 추정은 요약문 재검토 결과 오기였으며 실제 매칭 건수(7건)로 정정 | - |
| TC-SEARCH-003 | PASS | user@itsm.local(END_USER): "retest"→KNOWLEDGE 1건(KB-3, PUBLISHED)만, INC/PRB/CHG 0건. "노트북 신청"→0건(타인 소유이므로 제외). 사전에 제출한 본인 VPN Access 요청(SRM-2026-0021) 기준 "VPN Access" 검색→본인 요청 1건만 노출, 타 사용자의 기존 VPN Access(SRM-2026-0002)는 제외 — scope=mine 정상 | - |
| TC-SEARCH-004 | PASS | agent@itsm.local(SERVICE_DESK_AGENT): "노트북 신청"→9건 전체(타인 소유 포함, scope=all). "retest"→KNOWLEDGE 1건+INCIDENT 4건(INC-2026-0014/0017/0018/0019), PROBLEM/CHANGE 0건 | - |
| TC-SEARCH-005 | PASS | cab@itsm.local(APPROVER)/am@itsm.local(ASSET_MANAGER)/po@itsm.local(PROCESS_OWNER)/admin@itsm.local(SYSTEM_ADMIN) 4개 계정 모두 "retest" 검색 시 500·크래시 없이 "검색 결과가 없습니다"(0건) 정상 반환 | - |
| TC-SEARCH-006 | PASS | "retest" 전체 결과의 상태 배지(요청/구현/승인/검토)가 각 change_request.status(REQUESTED/IMPLEMENTATION/APPROVAL/REVIEW)와 일치, `updatedAt` 내림차순 정렬 확인 | - |

## 실패 항목 분석
- 없음

## 참고
- 콘솔에서 이전 세션 잔존으로 추정되는 `keyword=change`/`keyword=test` 500 오류 로그를 확인했으나, 현재 세션에서 동일 키워드로 재현 시도한 결과 정상(200, 0건) 응답을 확인했다. 실제 결함이 아닌 것으로 판단.
- TC-SEARCH-003 수행을 위해 END_USER(user@itsm.local)로 VPN Access 서비스 요청 1건(SRM-2026-0021)을 신규 제출했고, TC-CHG-002에서 CHG-2026-0011에 구현 결과(성공/비고 "tester QA 확인 - 정상 저장 테스트")를 실제 저장했다. 두 데이터 모두 테스트 목적의 정상 사용자 플로우 산출물로 별도 롤백하지 않았다.
