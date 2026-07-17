---
date: 20260717-153547
domain: srm
result: pass
keywords: [중복 Submit 버튼 수정, 제출/취소 버튼 배치, dynamic-form-renderer]
---

# 통합 테스트 결과 — srm (재테스트: 중복 Submit 버튼 결함 수정 확인) (20260717-153547)

## 요약
- 총 1건 · 성공 1 · 실패 0

## 상세

| TC ID | 결과 | 실제 동작 | 비고(스크린샷/로그) |
|-------|------|-----------|----------------------|
| TC-SRM-201 | PASS | `/portal/requests/new?item=7` 진입 시 화면에는 취소(좌측)·제출(우측, primary) 버튼만 노출됨. 레거시 Form.io "Submit" 버튼은 DOM에는 남아있으나 부모(`.formio-component-submit`)가 `display:none`으로 완전히 숨겨져 렌더 크기 0x0(비노출·비활성). 신규 "제출" 버튼 클릭 시 정상 제출 성공(201, `/service-requests/25` 생성 확인) | `docs/04_test/20260717-153547/srm/result/srm-button-placement-fixed.png`, computedStyle/getBoundingClientRect 확인 |

## 실패 항목 분석
- 없음
