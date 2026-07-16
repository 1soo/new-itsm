# 통합 테스트 결과 — common (20260716-112347, 배치3: 승인 대기함 상세보기 버튼)

## 요약
- 총 2건 · 성공 2 · 실패 0(범위 밖 발견 사항 1건 별도 기록)

## 상세

| TC ID | 결과 | 실제 동작 | 비고(스크린샷/로그) |
|-------|------|-----------|----------------------|
| TC-COM-001 | PASS | service-request 시나리오 TC-SRM-001과 동일 빌드 결과 공유(BUILD SUCCESSFUL) | - |
| TC-COM-002 | PASS | SRM-2026-0019(노트북 신청) 제출→검증→배정→라우팅→이행 시작 시도로 승인 대기(1차) 생성. admin@itsm.local로 `/approvals` 진입 시 해당 행에 기존 "상세"(승인/반려 모달) 버튼과 별개로 "상세보기" 버튼이 노출됨. "상세보기" 클릭 시 모달 없이 URL이 `/service-requests/19`로 이동(현재 탭)해 실제 요청 상세 화면(타임라인·요청 내용·승인 현황 패널 포함)이 표시됨 | playwright snapshot |

## 배치3 범위 밖 발견 사항 (참고 — dev-lead 별도 확인 필요)

TC-COM-002 준비 중 SRM 상세 화면에서 상태 전이 토스트 문구의 조사(로/으로) 오류를 발견했다. 배치1 코드리뷰에서 수정된 것은 **백엔드 타임라인 메시지**(`TimelineMessages.quotedWithParticle`)뿐이고, **프론트엔드 토스트**는 8개 도메인 모두 조사를 `'로`로 하드코딩하고 있어 받침 있는 라벨(ㄹ받침 제외)에서 문법이 틀린다.

- 재현: agent@itsm.local로 SRM-2026-0019를 ROUTED로 전이 → 토스트 "상태가 '라우팅됨'로 변경되었습니다"(오류, "으로"가 맞음). 같은 화면 타임라인은 정확히 "상태가 '라우팅됨'으로 변경되었습니다."로 표시됨(BE는 정상).
- 근거 코드(전부 동일한 하드코딩 패턴, `grep "'로 변경되었습니다"` 결과):
  - `source/frontend/src/features/service-request/RequestDetailPage.tsx:152`
  - `source/frontend/src/features/incident/IncidentDetailPage.tsx:229`
  - `source/frontend/src/features/problem/ProblemDetailPage.tsx:185`
  - `source/frontend/src/features/change/ChangeDetailPage.tsx:162`
  - `source/frontend/src/features/vulnerability/VulnerabilityDetailPage.tsx:166`
  - `source/frontend/src/features/asset/AssetDetailPage.tsx:180`
  - `source/frontend/src/features/esm/EsmRequestDetailPage.tsx:112`
  - `source/frontend/src/features/esm/HrCaseDetailPage.tsx:68`
- 이번 배치3(textarea·상세보기 버튼) 범위와 무관하고, 배치1에서 이미 커밋·푸시된 코드의 기존 결함이라 이번 결과의 PASS/FAIL 판정에는 반영하지 않음. dev-lead 판단에 따라 별도 수정 배치로 처리 필요.

## 실패 항목 분석
- 없음(배치3 범위 내)
