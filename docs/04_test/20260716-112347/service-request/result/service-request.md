---
date: 20260716-112347
domain: service-request
result: pass
keywords: [양식 필드 textarea 유형, 개행 보존]
---

# 통합 테스트 결과 — service-request (20260716-112347, 배치3: textarea 필드 유형)

## 요약
- 총 3건 · 성공 3 · 실패 0(관찰 사항 1건 별도 기록)

## 상세

| TC ID | 결과 | 실제 동작 | 비고(스크린샷/로그) |
|-------|------|-----------|----------------------|
| TC-SRM-001 | PASS | 백엔드 `./gradlew build` BUILD SUCCESSFUL, 프론트엔드 `npm run build`(tsc+vite) 성공(2136 modules) | - |
| TC-SRM-002 | PASS | `/admin/service-catalog`에서 "배치3 재검증용 항목" 신규 생성, 필드 유형 Select에 "여러 줄 텍스트" 옵션 노출·선택·저장 성공, 저장 후 재조회 시 유형 유지됨(기존 "textarea 테스트 항목"에서도 동일 확인) | playwright snapshot |
| TC-SRM-003 | PASS | user@itsm.local로 해당 항목 제출 폼 진입 → DOM 확인(`document.querySelector('textarea')` → `TAGNAME=TEXTAREA`) 실제 `<textarea>`로 렌더링됨. 개행 포함 3줄 입력 후 제출 → 요청 상세 조회 시 원본 데이터에 `\n` 개행이 그대로 보존됨(`dd.textContent` 확인) | playwright evaluate |

## 관찰 사항(회귀 아님, 참고용)
- 요청 상세 화면의 "요청 내용" 표시 영역은 `white-space: normal`이라 저장된 개행이 화면에 시각적으로는 줄바꿈으로 표시되지 않고 한 줄로 붙어 보인다(데이터 자체는 개행 보존, 표시만 영향). 이번 배치 변경 범위는 입력 폼 컴포넌트(`components/common/dynamic-form.tsx` 등)에 한정되고 상세 조회의 읽기 전용 표시 컴포넌트는 이번 변경 대상이 아니며, 다른 필드 유형(text 등)도 동일한 표시 방식을 공유하는 기존 동작이라 회귀로 판단하지 않음. 참고로 공유.

## 실패 항목 분석
- 없음
