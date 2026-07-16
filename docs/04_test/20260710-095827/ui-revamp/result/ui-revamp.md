---
date: 20260710-095827
domain: ui-revamp
result: pass
keywords: [다크모드 토글, Lozenge 배지, 접근성(포커스링·대비), 공통 컴포넌트 API, 도메인 화면 회귀]
---

# 통합 테스트 결과 — ui-revamp (UI/UX 개편) (20260710-095827)

> 환경: React CSR(localhost:5173) / Spring Boot(:8080) / PostgreSQL(itsm-postgres, healthy)
> 도구: Playwright(Chromium, 실 브라우저 자동화) — 매 TC마다 새 browser context, storage 초기화(테마 유지 검증 TC 제외)
> 대상: dev-ui Task #1~#6(토큰/Lozenge/공통 프리미티브/공통 패턴/레이아웃 셸+테마 토글/아이콘·접근성) 완료분 + dev-frontend Task #9 검증분

## 요약
- 총 **28건** · 성공 **28** · 실패 **0**
- 완료 기준(`docs/03_develop/plan/ui-revamp.md` 5절) 전 항목 충족 확인

## 상세

### A. 빌드
| TC ID | 결과 | 실제 동작 | 비고 |
|-------|------|-----------|------|
| TC-BUILD-001 (FE npm run build) | PASS | `tsc -b && vite build` 정상 완료(1867 modules, TS 오류 없음) | |
| TC-BUILD-002 (BE gradlew test) | PASS | `BUILD SUCCESSFUL`(UP-TO-DATE, FE 전용 변경으로 BE 코드/테스트 변경 없음, 기존 그린 상태 유지) | |

### B. 다크모드 토글 (REQ-UIX-002, SCR-COM-010)
| TC ID | 결과 | 실제 동작 |
|-------|------|-----------|
| TC-THEME-001 | PASS | 최초 진입 시 `data-theme` 미설정(라이트 기본), shots/theme-001-light-default.png |
| TC-THEME-002 | PASS | 테마 토글(`aria-label="테마 전환"`) 클릭 즉시 `data-theme="dark"`, `--background`=#161a1f로 전환, 아이콘 달→해 |
| TC-THEME-003 | PASS | 새로고침 후 `data-theme="dark"` 유지, `localStorage` 키 `itsm-theme` 확인 |
| TC-THEME-004 | PASS | `localStorage`에 잘못된 값(`purple-invalid`) 주입 후에도 라이트 기본값으로 정상 폴백 |

### C. Lozenge 배지 (REQ-UIX-007)
| TC ID | 결과 | 실제 동작 |
|-------|------|-----------|
| TC-BADGE-001 | PASS | 인시던트 목록 상태/심각도 배지 8개 전수 `border-radius: 4px`, `border-width: 1px`(테두리 유지), subtle 배경 확인 |
| TC-BADGE-002 | PASS | 우선순위(SEV1~3)·상태 배지 동일 규격, 텍스트 라벨 병행(색상 단독 아님) |

### D. 공통 컴포넌트 API 불변 (REQ-UIX-008, REQ-UIX-009)
| TC ID | 결과 | 실제 동작 |
|-------|------|-----------|
| TC-COMP-001 | PASS | 계정관리·인시던트 목록 등 여러 화면에서 Button 클릭·페이지 전환 시 브라우저 콘솔 에러 0건, TC-BUILD-001(tsc)로 prop 인터페이스 불변 확인 |
| TC-COMP-002 | PASS | 로그아웃 확인 다이얼로그(`role=alertdialog`) `background-color: rgb(255,255,255)`(`--popover`), `box-shadow: 0 4px 12px rgba(31,41,55,0.12)` 적용(overlay elevation) — **비고 참고** |
| TC-COMP-003 | PASS | `header`(56px)/`aside`(240px)/`footer` 구조 유지, 사이드바 접기 토글 동작 |

### E. 접근성 — 포커스 링 · 아이콘 라벨 (REQ-UIX-010, REQ-UIX-011)
| TC ID | 결과 | 실제 동작 |
|-------|------|-----------|
| TC-A11Y-001 | PASS | Tab 포커스 시 `outline-width: 2px` + `outline-color: rgb(59,130,246)`(`--ring`) 동시 렌더링, shots/a11y-001-focus-ring.png |
| TC-A11Y-002 | PASS | 사이드바 토글·테마 전환·알림·이전/다음 페이지 아이콘 단독 버튼 전부 `aria-label` 보유 |

### F. 모션 (REQ-UIX-006)
| TC ID | 결과 | 실제 동작 |
|-------|------|-----------|
| TC-MOTION-001 | PASS | 확인 다이얼로그 `transition-property: all`(transform 적용, `width`/`height` 레이아웃 애니메이션 없음, `matrix3d` transform 확인) |
| TC-MOTION-002 | PASS | `prefers-reduced-motion: reduce` 시 `transition-duration: 0.01ms`(≈즉시). `index.css` 272~281행에서 `0.01ms !important`로 의도적 구현 확인(0으로 하면 일부 라이브러리의 `transitionend` 이벤트가 발화하지 않는 문제를 피하기 위한 접근성 모범 패턴) — 최초 자동화 스크립트는 엄격 동등 비교(`===0`)로 오탐 FAIL 처리했으나 재검증 결과 결함 아님 |

### G. 색상 대비 (REQ-UIX-012)
| TC ID | 결과 | 실제 동작 |
|-------|------|-----------|
| TC-CONTRAST-001 | PASS | 라이트 `--foreground`(#1F2937) on `--background`(#F9FAFB) = 14.05:1 (기준 4.5:1) |
| TC-CONTRAST-002 | PASS | 다크 `--foreground`(#E5E7EB) on `--background`(#161A1F) = 14.12:1 |
| TC-CONTRAST-003 | PASS | 보조 텍스트 라이트 4.63:1 / 다크 6.88:1, 모두 4.5:1 이상 |

### H. 8개 도메인 화면 회귀 스모크
| TC ID | 결과 | 실제 동작 |
|-------|------|-----------|
| TC-DOM-001 (auth/admin) | PASS | `/admin/users` 목록 정상 렌더링(역할 배지 Lozenge 반영), 로그인 기능 회귀 없음 |
| TC-DOM-002 (service-request) | PASS | `/service-requests` 목록 정상 렌더링(agent@itsm.local) |
| TC-DOM-003 (incident) | PASS | `/incidents` 목록 정상 렌더링(im@itsm.local), 심각도/상태 배지 확인 |
| TC-DOM-004 (problem) | PASS | `/problems` 목록 정상 렌더링(pm@itsm.local) |
| TC-DOM-005 (change) | PASS | `/changes` 목록 정상 렌더링(cm@itsm.local) |
| TC-DOM-006 (knowledge) | PASS | `/knowledge` 목록 정상 렌더링(kc@itsm.local) |
| TC-DOM-007 (asset) | PASS | `/assets` 목록 정상 렌더링(am@itsm.local), 생애주기 배지(운영/폐기/임박/경과) Lozenge 확인 |
| TC-DOM-008 (RBAC 403) | PASS | 권한 없는 화면(kc@itsm.local → `/admin/users`) 접근 시 `/403`으로 이동, 403 화면 토큰 반영 확인(shots/dom-008-403.png) |

### I. 다크모드 도메인 스팟체크
| TC ID | 결과 | 실제 동작 |
|-------|------|-----------|
| TC-DARK-001 | PASS | 인시던트 목록 다크 렌더링 정상, 배지·텍스트 대비 유지 |
| TC-DARK-002 | PASS | 자산 목록 다크 렌더링 정상(사이드바 `--sidebar`=#10141a, 카드/배지 다크 값 적용), shots/dark-002-assets.png |

## 실패 항목 분석
- 없음

## 비고 (경미한 관찰 사항 — 실패 아님)
- **Overlay elevation 그림자 알파값 편차**: `docs/02_plan/screen/common.md` 2.5절은 라이트 overlay 그림자를 `rgba(31,41,55,0.16)`로 명시하나, 실제 구현(`source/frontend/src/index.css:176`)은 `0.12`를 사용한다. 다크 테마 배지 subtle 배경은 WCAG 재검증에 따른 의도적 조정(0.16→0.12, index.css 217~219행 주석)으로 문서화되어 있으나, 라이트 테마 overlay 그림자 값에는 동일한 조정 근거 주석이 없다. Elevation 레벨 혼용(같은 컴포넌트 내 raised/overlay 토큰 혼용, REQ-UIX-005 Unwanted 조건)은 발견되지 않았고 시각적 계층 효과는 정상 작동하므로 **결함으로 처리하지 않았다**. 의도된 값인지 dev-ui 확인을 권장한다.

## 결론
- ui-revamp 이니셔티브 통합/회귀 테스트 **전 항목 통과(28/28)**.
- 완료 기준(개발 계획 5절) 충족: 8개 도메인 화면 토큰·컴포넌트 반영 및 기존 기능(RBAC, 목록 조회, 로그인) 회귀 없음, 다크모드 토글 정상 동작(즉시 전환+재방문 유지+잘못된 값 폴백), Button/Dialog 등 공통 컴포넌트 API 불변(콘솔 에러 없음, 빌드 통과), Lozenge 배지(4px+테두리+텍스트 라벨) 렌더링, 포커스 링 2px+색상 토큰 동시 렌더링, WCAG AA 대비 충족(라이트/다크 모두 4.5:1 이상 큰 폭 만족), 모션이 transform/opacity 위주(레이아웃 속성 미사용)이며 `prefers-reduced-motion` 시 즉시 전환.
- 실패 항목 없음 — 커밋/푸시 진행 가능.
