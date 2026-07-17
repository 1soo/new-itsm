---
date: 20260717-152015
domain: srm
result: partial
keywords: [FormSubmissionValidator pattern 버그 수정, 폼 빌더 팔레트, 제출/취소 버튼 배치, 중복 Submit 버튼]
---

# 통합 테스트 결과 — srm (재테스트: 폼 빌더 pattern 버그 수정, 팔레트 구성, 제출/취소 버튼 배치) (20260717-152015)

## 요약
- 총 5건 · 성공 4 · 실패 1

## 상세

| TC ID | 결과 | 실제 동작 | 비고(스크린샷/로그) |
|-------|------|-----------|----------------------|
| TC-SRM-101 | PASS | 백엔드 `./gradlew build -x test` BUILD SUCCESSFUL, 프론트엔드 `npm run build`(tsc+vite) 성공(3993 modules) | - |
| TC-SRM-102 | PASS | 8080 백엔드 재시작(dev-be) 후 카탈로그 항목 id=7("폼빌더 회귀 테스트 항목")에 유효한 값(`requestSummary`="재테스트 유효 제출 데이터입니다", `priorityScore`=42)으로 제출 시 201 Created(`/service-requests/23` 생성). 이전 실행(20260717-145302)에서 재현된 pattern 버그(빈 문자열 pattern을 정규식으로 취급해 항상 실패) 더 이상 재현 안 됨 | network request 확인(201) |
| TC-SRM-103 | PASS | 1) minLength 위반("ab", 2자) → 400(`FORM_FIELD_INVALID`, "요청 요약: 최소 길이(5자) 미만입니다") 2) max 위반(priorityScore=500, 유효한 requestSummary 동반) → 400(`FORM_FIELD_INVALID`, "Priority Score: 최대값(100.0)을 초과했습니다" — 더 이상 텍스트 필드 pattern 오류에 가려지지 않고 실제 위반 필드의 메시지가 정상 반환됨) 3) 정상 대조군은 TC-SRM-102에서 201 확인 완료 | fetch 응답 확인 |
| TC-SRM-104 | PASS | `/admin/service-catalog` 폼 빌더 팔레트 재확인 — Advanced 그룹에 Email/Phone Number/Date-Time/File 모두 포함, Premium 그룹은 팔레트 탭 목록에서 완전히 사라짐(Basic/Advanced/Layout 3개만 노출) | snapshot |
| TC-SRM-105 | FAIL | 요청 제출 화면(`/portal/requests/new?item=7`) 하단에 신규 푸터(`취소` 좌측·`제출` 우측, 우측 정렬)는 정상 추가됐으나, Form.io 스키마 내장 "Submit" 버튼이 여전히 화면에 노출됨(파란색, 폼 좌측 하단, `display:inline-block`·`visibility:visible`·`disabled:false` 확인 — 실제 클릭 가능한 상태로 중복 노출). 요구사항("schema 자체에 보이는 submit 컴포넌트는 렌더링하지 않도록 숨김 처리")과 불일치 | `docs/04_test/20260717-152015/srm/result/srm-button-placement.png`, computedStyle 확인 |

## 실패 항목 분석
- TC-SRM-105: `dynamic-form-renderer.tsx`가 신규 하단 푸터(취소/제출)는 추가했으나 Form.io 스키마의 기존 submit 컴포넌트를 숨기는 처리가 누락된 것으로 보인다(`docs/03_develop/plan/common.md` "추가 요구사항(2026-07-17)" 절 UI 담당 항목 참조). 화면에 제출 버튼이 2개(중복) 노출되는 상태이므로 수정 필요.
