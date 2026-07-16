---
date: 20260711-075555
domain: common
result: pass
keywords: [사용자가이드, 모달, 아코디언]
---

# 통합 테스트 결과 — Common (사용자 가이드 모달, SCR-COM-012) (20260711-075555)

## 요약
- 총 10건 · 성공 10 · 실패 0

## 상세

| TC ID | 결과 | 실제 동작 | 비고(스크린샷/로그) |
|-------|------|-----------|----------------------|
| TC-BUILD-001 | PASS | `npm run build`(tsc -b && vite build) 성공, 신규 의존성(`@radix-ui/react-tabs`/`@radix-ui/react-accordion`) 포함 타입/컴파일 오류 없음 | - |
| TC-GUIDE-001 | PASS | cab@itsm.local, "?" 클릭 시 모달 오픈. 타이틀="사용자 가이드", 기본 활성 탭="개요", 본문 텍스트가 `user-guide-content.md` 1절 문단과 정확히 일치 | shots/tc-guide-001-overview.png |
| TC-GUIDE-002 | PASS | "도메인 및 원칙" 탭 아코디언 11개, 목록(auth~infra-monitoring)이 콘텐츠 문서 2절과 일치. 최초 `aria-expanded="false"`(전부 접힘) → "인증/계정/권한(auth)" 클릭 시 `aria-expanded="true"`로 전환, 내용 "모든 화면·API는 역할을 검증하며, 권한 없는 접근은 403으로 차단된다" 일치 | shots/tc-guide-002-domains.png |
| TC-GUIDE-003 | PASS | cab@itsm.local(APPROVER) — "역할별 수행 내용과 방법" 탭 아코디언 총 16개, 최상단 항목 "승인자(CAB 멤버 포함)"+"내 역할" 배지, `aria-expanded="true"`(기본 펼침). 2번째 항목은 `aria-expanded="false"`(접힘) | shots/tc-guide-003-roles-cab.png |
| TC-GUIDE-004 | PASS | am@itsm.local(ASSET_MANAGER) — 아코디언 총 16개, 최상단 "자산 관리자"+"내 역할" 배지, 기본 펼침. cab과 다른 역할이 정확히 최상단 고정되어 계정별 데이터 분기가 하드코딩이 아님을 확인 | shots/tc-guide-004-roles-am.png |
| TC-GUIDE-005 | PASS | 모달 오픈 확인 후 배경(좌상단 오버레이 영역) 클릭 시 모달 닫힘(dialog visible false) | shots/tc-guide-005-006-close.png |
| TC-GUIDE-006 | PASS | 모달 오픈 확인 후 Esc 키 입력 시 모달 닫힘 | 위와 동일 스크린샷 |
| TC-GUIDE-007 | PASS | "도메인 및 원칙" 탭으로 전환 후 닫기(X) 버튼 클릭 시 닫힘, "?" 재클릭으로 재오픈 시 활성 탭이 "개요"로 리셋됨(직전 탭 유지 아님) | shots/tc-guide-007-close-reset.png |
| TC-GUIDE-008 | PASS | 모달 오픈 후 개요→도메인 및 원칙→역할별 수행 내용과 방법→개요 순 탭 전환 동안 캡처된 네트워크 요청 0건(정적 콘텐츠, 서버 재조회 없음) | - |
| TC-GUIDE-009 | PASS | 헤더 우측 아이콘 버튼 DOM 순서: "사용자 가이드" → "테마 전환" → "알림 2건" → "사용자 메뉴" — 설계 명시 순서(통합검색-"?"-테마 토글-알림 벨-사용자 메뉴)와 일치 | - |

## 실패 항목 분석
- 없음

## 참고 · 데이터 제약
- 현재 시드는 모든 POC 계정이 정확히 1개 역할만 보유(다중 역할 계정 없음). "역할 여러 개" 케이스는 실 데이터로 재현하지 못했다. `user-guide-modal.tsx`의 `ROLES.filter(r => myRoleSet.has(r.code))` 로직이 역할 개수와 무관하게 Set 멤버십 기반으로 동일하게 동작함을 코드 검토로 확인했고, TC-GUIDE-003/004에서 서로 다른 단일 역할 계정(cab=APPROVER, am=ASSET_MANAGER) 각각에 대해 해당 역할만 정확히 최상단 고정되는 것을 실증해 회귀 위험은 낮다고 판단했다.
- FEAT-COM-002의 "콘텐츠 로드 실패 시 오류 안내" 예외 처리 인수 기준은 콘텐츠가 전부 FE 정적 상수(하드코딩)라 런타임 로드 실패 시나리오 자체가 발생하지 않아(N/A) 별도 TC를 실행하지 않았다.
