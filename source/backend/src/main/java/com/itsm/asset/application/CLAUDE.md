# CLAUDE.md

asset 도메인의 애플리케이션 서비스 계층.

## 파일
- `AssetService.java` — 자산 목록/등록/상세/수정·생애주기 전이·폐기·티켓 연계, CI 목록/등록/관계·영향범위, 지표 유스케이스. Stage 5(2026-07-12)에서 생애주기 전이(targetStage=RETIREMENT)·폐기(retire) 시 공용 승인 게이트(`ApprovalGateService.checkGate(domain=ASSET, requestSubtypeKey=null, requesterId, ...)`)를 연동했다(요청자는 자산 등록자 created_by 이메일로 역조회). 상세 조회(API-ITAM-003)의 `approval` 필드도 동일하게 노출
- `AssetApprovalTicketSummaryProvider.java` — 공용 승인 대기함·상세(API-COM-003/004)가 노출할 ASSET 티켓 요약(assetKey·이름·등록자명) 어댑터(`ApprovalTicketSummaryProvider` 구현)

## 하위 디렉토리
- `dto/` — 요청·응답 DTO(record)
