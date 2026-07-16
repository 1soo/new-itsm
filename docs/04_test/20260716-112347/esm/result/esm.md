# 통합 테스트 결과 — esm (20260716-112347, 배치3: textarea 필드 유형)

## 요약
- 총 3건 · 성공 3 · 실패 0

## 상세

| TC ID | 결과 | 실제 동작 | 비고(스크린샷/로그) |
|-------|------|-----------|----------------------|
| TC-ESM-001 | PASS | service-request 시나리오 TC-SRM-001과 동일 빌드 결과 공유(BUILD SUCCESSFUL) | - |
| TC-ESM-002 | PASS | "부서별 카탈로그 관리"에서 "배치3 ESM 재검증용"(법무) 신규 생성, 필드 유형 Select에 "여러 줄 텍스트" 옵션 노출·선택·저장 성공(SRM과 동일한 공통 컴포넌트 재사용 확인) | playwright snapshot |
| TC-ESM-003 | PASS | user@itsm.local로 부서 서비스 포털에서 해당 항목 제출 폼 진입 → DOM 확인 실제 `<textarea>`로 렌더링됨. 개행 포함 2줄 입력 후 제출 → 부서 요청 상세 조회 시 원본 데이터에 개행이 그대로 보존됨(SRM과 동일한 결과, service-request 결과의 "관찰 사항"과 동일하게 화면 표시상 개행은 보이지 않으나 데이터는 보존) | playwright evaluate |

## 실패 항목 분석
- 없음
