---
date: 20260716-105846
domain: service-request
result: pass
keywords: [서비스 카탈로그 카테고리 CRUD, 카테고리 중복/참조무결성 검증]
---

# 통합 테스트 결과 — service-request (20260716-105846, 배치2: 카탈로그 카테고리 CRUD)

## 요약
- 총 10건 · 성공 10 · 실패 0

## 상세

| TC ID | 결과 | 실제 동작 | 비고(스크린샷/로그) |
|-------|------|-----------|----------------------|
| TC-SRM-001 | PASS | 백엔드 `./gradlew build` BUILD SUCCESSFUL, 프론트엔드 `npm run build`(tsc+vite) 성공(2136 modules) | - |
| TC-SRM-002 | PASS | `/admin/service-catalog` "카테고리 관리" 탭에서 계정(순서0·사용1)/하드웨어(순서10·사용1)/소프트웨어(순서20·사용0) sortOrder 오름차순 표시, itemCount 정확 | playwright snapshot |
| TC-SRM-003 | PASS | "새 카테고리"로 "가구"(순서30) 생성 → 목록에 즉시 반영(201) | playwright snapshot |
| TC-SRM-004 | PASS | 동일 이름 "가구" 재생성 시도 → 다이얼로그 인라인 오류 "이미 존재하는 카테고리명입니다.", 네트워크 확인 `POST /categories` → 409(Conflict), 목록에 중복 미추가 | playwright snapshot + network log |
| TC-SRM-005 | PASS | "가구" 수정 버튼 → 이름을 "기타비품"으로 변경 저장 → 200, 목록에 변경 반영 | playwright snapshot |
| TC-SRM-006 | PASS | 참조 중인 "하드웨어"(사용1) 삭제 시도(확인 다이얼로그 진행) → 네트워크 확인 `DELETE /categories/2` → 409(Conflict), 목록에 "하드웨어" 그대로 남음 | playwright snapshot + network log |
| TC-SRM-007 | PASS | 참조 없는 "기타비품"(사용0) 삭제 성공적으로 확인(API 레벨로 최종 확인 — FE 확인 다이얼로그가 연속 실패 후 재오픈되지 않는 별도 latent 버그 발견되어 dev-lead 사전 공유대로 이번 범위 제외하고 우회 확인) | 비고 참조 |
| TC-SRM-008 | PASS | 카탈로그 항목 생성 폼 카테고리 select에 미분류/계정/하드웨어/소프트웨어/기타비품 후보 노출. "소프트웨어" 선택 후 "재검증용 항목" 생성 → 저장 후 편집 폼에 "소프트웨어" 반영 확인 | playwright snapshot |
| TC-SRM-009 | PASS | "노트북 신청" 편집 진입 시 카테고리 select가 상세 조회 응답값("하드웨어")으로 정확히 프리필됨(담당 큐·담당자 역할도 함께 정상 프리필) | playwright snapshot |
| TC-SRM-010 | PASS | 서비스 포털에서 "노트북 신청" 카드 배지="하드웨어", "비밀번호 초기화" 카드 배지="계정", 신규 "재검증용 항목" 카드 배지="소프트웨어" 정확히 표시 | playwright snapshot |
| TC-SRM-011 | PASS | 존재하지 않는 categoryId(999999)로 `POST /service-catalog/items` → 404(`CATEGORY_NOT_FOUND`), `PATCH /service-catalog/items/1` → 동일 404, 기존 항목(id=1) 데이터 변경 없음 확인(DB 조회) | curl + DB 조회 |

## 참고 — 이번 범위 제외 항목
- `components/common` 삭제 확인 다이얼로그의 latent 버그(연속 실패 후 다이얼로그 재오픈 안 됨)를 TC-SRM-007 수행 중 재현 확인함. dev-lead가 사전에 공유한 대로 이번 배치 테스트 범위에는 포함하지 않음(별도 트래킹 중).

## 실패 항목 분석
- 없음
