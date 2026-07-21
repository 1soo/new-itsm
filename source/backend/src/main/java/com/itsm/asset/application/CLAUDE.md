# CLAUDE.md

asset 도메인의 애플리케이션 서비스 계층.

## 파일
- `AssetService.java` — 자산 목록/등록/상세/수정·생애주기 전이·폐기·티켓 연계, CI 목록/등록/관계·영향범위, 지표 유스케이스. Stage 5(2026-07-12)에서 생애주기 전이·폐기(retire) 시 공용 승인 게이트 연동. 2026-07-22 상태별 승인자 지정 확장: `transition()`은 targetStage 가드 없이 무조건 `checkGate(domain=ASSET, requestSubtypeKey=null, requesterId, ticketType=ASSET, ticketId, targetState=targetStage)` 호출(5단계 어떤 전이든 게이트 대상), requesterId는 호출자(`SecurityUtils.currentPrincipal().userId()`)로 통일(기존 "등록자 created_by 이메일 역조회" 방식 폐기, 그 방식을 위해서만 쓰이던 `AppUserRepository` 의존성도 제거). `create()`(API-ITAM-002)는 `TicketCreationGateSupport.createThenGate`로 REQUIRES_NEW 분리(생성시점 targetState="PLANNING"). 목록(API-ITAM-001)은 `pendingApprovalTargetStatesOf`로 배치 조회한 targetState를 `AssetSummaryResponse.pendingApprovalTargetState`에 채움(N+1 방지). 상세 조회(API-ITAM-003)의 `approval`(`ApprovalInfo`)에 `targetState` 포함
- `AssetApprovalTicketSummaryProvider.java` — 공용 승인 대기함·상세(API-COM-003/004)가 노출할 ASSET 티켓 요약(assetKey·이름·등록자명) 어댑터(`ApprovalTicketSummaryProvider` 구현)

## 하위 디렉토리
- `dto/` — 요청·응답 DTO(record)
