---
date: 20260711-083648
domain: common
result: pass
keywords: [사용자가이드, 전용화면, TOC]
---

# 통합 테스트 결과 — Common (사용자 가이드 전용 화면 /guide, SCR-COM-012 v0.8) (20260711-083648)

## 요약
- 총 10건 · 성공 10 · 실패 0

## 상세

| TC ID | 결과 | 실제 동작 | 비고(스크린샷/로그) |
|-------|------|-----------|----------------------|
| TC-BUILD-001 | PASS | `npm run build`(tsc -b && vite build) 성공, `react-markdown` 신규 의존성·`tabs.tsx` 제거분 포함 타입/컴파일 오류 없음 | - |
| TC-GUIDE-001 | PASS | cab@itsm.local, "?" 클릭 시 모달이 아니라 `/guide`로 라우팅 이동(URL `/`→`/guide`), `dialog` role 요소 없음(visible=false). 브라우저 뒤로가기 시 `/`로 정상 복귀 | shots/tc-guide-001-navigate.png |
| TC-GUIDE-002 | PASS | `/guide` 진입 시 H1="사용자 가이드", `scrollY=0`(최상단), "개요" 섹션(`#overview`)이 진입 직후 뷰포트에 보임 | shots/tc-guide-002-003.png |
| TC-GUIDE-003 | PASS | 좌측 TOC 링크 정확히 3개("개요"/"도메인 및 원칙"/"역할별 수행 내용과 방법", 하위 링크 없음). "역할별 수행 내용과 방법" 클릭 시 해당 섹션(`#roles`)이 뷰포트에 들어오고 활성 링크가 클릭한 항목으로 전환 | 위와 동일 스크린샷 |
| TC-GUIDE-004 | PASS | "개요" 섹션 내 아코디언(버튼) 0개(순수 텍스트), 굵게 서식 3곳(`<strong>`) 모두 원칙 문장과 일치. 마지막 문단에서 "2. 도메인 및 원칙"/"3. 역할별..." 섹션명을 안내 문구로 언급하는 것은 원문 설계(`user-guide-content.md` 1절 마지막 문단)이며 실제 2·3절 본문 내용(핵심 원칙/페르소나 상세)은 개요 섹션에 렌더링되지 않음을 확인 | shots/tc-guide-004-overview.png |
| TC-GUIDE-005 | PASS | "도메인 및 원칙" 섹션 아코디언 11개(auth~infra-monitoring, v0.2 명칭과 일치). 최초 `aria-expanded="false"`(전부 접힘) → "인증/계정/권한 (Auth & RBAC)" 클릭 시 `true`로 전환, "핵심 원칙" 굵게 표기 렌더링 확인 | shots/tc-guide-005-domains.png |
| TC-GUIDE-006 | PASS | cab@itsm.local(APPROVER) — "역할별 수행 내용과 방법" 아코디언 총 16개, 최상단 "APPROVER — 승인자 (CAB 멤버 포함)"+"내 역할" 배지, `aria-expanded="true"`(기본 펼침), 2번째 항목 `false`(접힘). 본문에 "정우진 부장", "승인 대기함", "CAB 승인 대기함" 등 v0.2 서술형 문구 포함 확인 | shots/tc-guide-006-roles-cab.png |
| TC-GUIDE-007 | PASS | am@itsm.local(ASSET_MANAGER) — 아코디언 총 16개, 최상단 "ASSET_MANAGER — 자산 관리자"+"내 역할" 배지, 기본 펼침, 본문에 "임하윤 주임" 포함. cab과 다른 역할이 정확히 최상단 고정되어 계정별 데이터 분기가 하드코딩이 아님을 재확인 | shots/tc-guide-007-roles-am.png |
| TC-GUIDE-008 | PASS | `/guide` 진입 후 TOC 3개 순회 클릭 + 도메인/역할 아코디언 각 1개 펼침까지 상호작용하는 동안 캡처된 네트워크 요청 0건(정적 콘텐츠) | - |
| TC-GUIDE-009 | PASS | "?" 클릭 시 `role="dialog"` 요소 미노출(TC-001에서 확인). 소스 확인 결과 `user-guide-modal.tsx`·`components/ui/tabs.tsx` 파일 삭제됨(`ls` 확인), `header.tsx`에 `guideOpen` state·`UserGuideModal` 렌더링 잔존 없음(grep 확인, `onOpenGuide` 콜백으로 완전 대체) | - |

## 실패 항목 분석
- 없음

## 참고
- 이전 모달 버전 통합 테스트(`docs/04_test/common/20260711-075555/`)는 이번 전용 화면 전환으로 폐기 대상이 되어 더 이상 유효하지 않다.
- "내 역할" 다중 선택 로직은 이전 모달 버전과 동일(Set 멤버십 기반, 코드 이관)하며, 시드 데이터에 다중 역할 계정이 없는 제약도 동일하게 유지된다(이전 회차에서 이미 확인).
