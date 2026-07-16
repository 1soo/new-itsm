---
date: 20260715-064012
domain: auth
result: pass
keywords: [로그인, 토큰 재발급, 감사 로그, 알림]
---

# 통합 테스트 결과 — auth/common (20260715-064012)

## 요약
- 총 4건 · 성공 4 · 실패 0

## 상세

| TC ID | 결과 | 실제 동작 | 비고(스크린샷/로그) |
|-------|------|-----------|----------------------|
| TC-AUTH-101 | PASS | admin@itsm.local/Admin@1234 로그인 성공. 로그인 화면에 POC 테스트 계정 표(전 역할 21개 계정, 비밀번호 Admin@1234 공통) 정상 노출. 로그인 후 사이드바에 12개 도메인 전체 메뉴(서비스 요청/인시던트/문제/변경/지식/자산/부서 서비스/HR 케이스/취약점/컴플라이언스/인프라 모니터링/관리자) 정상 렌더링 | snapshot 확인 |
| TC-AUTH-102 | PASS | `/` 하드 네비게이션 시 `/api/v1/auth/me` 401 1차 발생 → httpOnly Refresh Token 쿠키로 자동 재인증되어 "환영합니다, 시스템 관리자님" 대시보드 정상 렌더링. 감사 로그에 `로그인`(06:42:14) → `토큰 재발급`(06:42:32, 06:47:32, 5분 간격) 순으로 기록되어 재발급 주기 정상 동작 확인 | `/admin/audit-logs` snapshot |
| TC-AUTH-103 | PASS | `/admin/users` 21개 계정 목록(페이지네이션 포함) 정상 표시. `/admin/audit-logs` 로그인/토큰재발급 이벤트 정상 표시. 두 화면 모두 신규 콘솔 에러 없음 | snapshot 확인 |
| TC-COM-101 | PASS | 헤더 알림 버튼 클릭 시 최초 `/api/v1/notifications/dismissals` 401(토큰 갱신 타이밍) 발생했으나 패널에 "새로운 알림이 없습니다" 정상 렌더링(자동 재시도로 해소) | snapshot 확인 |

## 실패 항목 분석
- 없음 (실패 0건)

## 참고 사항 (실패는 아니나 발견한 사항)
- 세션 중 관찰된 `/api/v1/auth/me`, `/api/v1/notifications/dismissals` 401은 Access Token이 Client Memory에만 저장되는 기존 보안 설계(`docs/06_maintenance/20260712-111803/auth/report.md`)에 따라 하드 리로드·토큰 만료 시점에 발생하는 정상적인 1차 실패이며, httpOnly Refresh Token 쿠키 기반 자동 재인증으로 즉시 해소됨(감사 로그의 "토큰 재발급" 이벤트로 교차 확인). 런타임 업그레이드로 인한 회귀 아님.
