# CLAUDE.md

승인 엔진 REST 컨트롤러.

## 파일
- `ApprovalController.java` — `/api/v1/approvals` 전 도메인 공용 대기함·상세·결정 API(API-COM-003~005) + 반려 후 재승인요청 API(API-COM-006 `POST /resubmit`, 2026-07-22 신규, `ApprovalGateService.resubmit` 위임, requesterId는 `SecurityUtils.currentPrincipal().userId()`)
