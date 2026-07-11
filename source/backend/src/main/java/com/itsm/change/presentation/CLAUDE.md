# CLAUDE.md

change 도메인의 REST 컨트롤러. 승인/반려·승인대기 API는 2026-07-11 승인 프로세스 커스텀 기능으로
`common.approval.presentation.ApprovalController`(`/api/v1/approvals`)로 이전되었다.

## 파일
- `ChangeController.java` — `/api/v1/changes` 변경 요청(RFC) CRUD·상태전이·분류·구현결과·연계·일정·지표 API
- `ChangeTemplateController.java` — `/api/v1/change-templates` 표준 변경 템플릿 목록 API
