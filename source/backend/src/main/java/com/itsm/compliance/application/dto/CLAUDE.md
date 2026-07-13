# CLAUDE.md

compliance 도메인 애플리케이션 계층 요청·응답 DTO(record).

## 파일
- `CreateRequirementRequest.java` — 요구사항 등록 요청(name·basis 필수, scope 선택)
- `RequirementCreatedResponse.java` — 등록 응답(id, requirementKey)
- `UpdateRequirementRequest.java` — 요구사항 수정 요청(name·basis 필수, scope 선택)
- `RequirementSummaryResponse.java` — 목록 요약 응답(complianceStatus 계산값 포함)
- `RequirementDetailResponse.java` — 상세 응답(시정조치·연계 변경 요청 포함)
- `LinkRequest.java` — 변경 요청 연계 요청(changeId)
- `OwnerRequest.java` — 책임자 지정 요청(ownerId)
- `OwnerResponse.java` — 책임자 지정 응답(id, owner)
- `CorrectiveActionCreateRequest.java` — 시정조치 등록 요청(description 필수)
- `CorrectiveActionCreatedResponse.java` — 시정조치 등록 응답(id, status)
- `CorrectiveActionStatusTransitionRequest.java` — 시정조치 상태 전이 요청(targetStatus)
- `CorrectiveActionStatusResponse.java` — 시정조치 상태 응답(id, status)
- `ComplianceAuditLogResponse.java` — 컴플라이언스 감사 로그 응답(eventType, actor, target, result, occurredAt)
- `ComplianceMetricsResponse.java` — 준수 현황 응답(totalRequirements/compliantCount/nonCompliantCount/openCorrectiveActionCount/complianceRate)
