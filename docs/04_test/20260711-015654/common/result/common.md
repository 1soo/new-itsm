---
date: 20260711-015654
domain: common
result: pass
keywords: [알림팝오버, 상세보기, 뱃지]
---

# 통합 테스트 결과 — Common (헤더 알림 팝오버) (20260711-015654)

## 요약
- 총 10건 · 성공 10 · 실패 0

## 상세

| TC ID | 결과 | 실제 동작 | 비고(스크린샷/로그) |
|-------|------|-----------|----------------------|
| TC-BUILD-001 | PASS | `npm run build` 성공(tsc -b && vite build), 타입/컴파일 오류 없음 | - |
| TC-NOTIF-001 | PASS | user@itsm.local(END_USER) 로그인 시 알림 벨 `aria-label="알림"`(건수 접미사 없음, 뱃지 미노출). 벨 클릭 시 팝오버 오픈, 텍스트 "새로운 알림이 없습니다" 노출 | shots/tc-notif-001-empty.png |
| TC-NOTIF-002 | PASS | cab@itsm.local(APPROVER) 로그인 시 `aria-label="알림 2건"`. 팝오버 순서: 1) "서비스요청 승인" · "SRM-2026-0007 · req 승인 요청" 2) "변경 승인" · "CHG-2026-0006 · 표준/- · cm@itsm.local 승인 …"(42자 원문이 40자+말줄임표로 truncate). 각 항목에 "상세 보기" 버튼 존재 | shots/tc-notif-002-mixed.png |
| TC-NOTIF-003 | PASS | am@itsm.local(ASSET_MANAGER) 로그인 시 `aria-label="알림 4건"`. 팝오버에 "자산 만료" 라벨 4건, 각각 `{assetKey} · {name} · {expiryDate} 만료 예정` 포맷 40자 초과로 truncate(AST-0004~0001). 팝오버 폭 320px(boundingBox 측정) 확인 | shots/tc-notif-003-assets.png |
| TC-NOTIF-004 | PASS | cab 계정, "서비스요청 승인" 항목 "상세 보기" 클릭 시 `/service-requests/8`로 이동, 팝오버 닫힘(visible=false) | shots/tc-notif-004-sr-detail.png |
| TC-NOTIF-005 | PASS | cab 계정, "변경 승인" 항목 "상세 보기" 클릭 시 `/changes/9`로 이동, 팝오버 닫힘 | shots/tc-notif-005-chg-detail.png |
| TC-NOTIF-006 | PASS | am 계정, AST-0004 항목의 "상세 보기" 버튼이 아닌 라인(도메인 배지) 클릭 시에도 `/assets/8`로 이동, 팝오버 닫힘(라인 클릭도 상세 이동 트리거 확인) | shots/tc-notif-006-asset-line-click.png |
| TC-NOTIF-007 | PASS | cab 계정, 알림 벨 클릭 직후 URL 불변(`http://localhost:5173/` 유지, 기존 즉시 이동 동작 없음), 팝오버만 오픈(visible=true) | shots/tc-notif-007-no-immediate-navigate.png |
| TC-NOTIF-008 | PASS | 팝오버 오픈 상태에서 외부 영역(헤더 로고 아이콘) 클릭 시 팝오버 닫힘(visible=false) | shots/tc-notif-008-outside-click-close.png |
| TC-NOTIF-009 | PASS | admin@itsm.local(SYSTEM_ADMIN) 로그인 시 `aria-label="알림 4건"`(개인 배정 승인 대기 0건, 자산 만료 임박 4건). 팝오버 항목 전부 "자산 만료" 카테고리만 노출, 서비스요청/변경 승인 항목 없음(정상 — 개인 배정 없음) | shots/tc-notif-009-admin.png |

## 실패 항목 분석
- 없음

## 참고 · 데이터 제약
- "8건 상한(slice)" 자체는 현재 시드 데이터로 한 계정이 3개 알림 유형 합산 8건을 초과하지 않아 UI로 실증하지 못함. `source/frontend/src/routes/AppLayout.tsx`의 `items.slice(0, NOTIFICATION_PREVIEW_SIZE)` 코드 검토로 로직 존재만 확인(코드 리뷰, 별도 TC 미실행). 회귀 방지를 위해 추후 시드 데이터가 확장되면 실증 테스트 추가를 권장한다.
