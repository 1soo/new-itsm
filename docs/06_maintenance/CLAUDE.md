# docs/06_maintenance — 유지보수 이력 인덱스

이 디렉토리는 개발 완료 후 발생한 추가 요구사항(유지보수)의 이력을 도메인별로 기록한다.

## 구조

- `{yyyyMMdd-HHmmss}/{domain}/report.md`: 각 유지보수 건의 상세 이력 (요구사항 · 해결 방법 · 변경 파일 · 테스트 결과)

## 인덱스

`maintainer` 에이전트는 유지보수 이력을 생성할 때마다 아래 표에 한 줄씩 추가한다.
신규 유지보수 요청을 분석할 때는 **이 표만 먼저 확인**하여 유사 이력이 있는지 판단하고, 관련 있어 보이는 항목의 `report.md`만 선택적으로 열람한다. (전체 report를 전수 조사하지 않는다 — 토큰 최소화)

| 일시 | 도메인 | 요약 | 경로 |
|------|--------|------|------|
| 20260711-140000 | auth | 역할-메뉴 동적 매핑(screen/screen_role 확장, 메뉴 관리 화면 신규) | docs/06_maintenance/20260711-140000/auth/report.md |
| 20260711-140000 | common | 헤더 알림 확인처리(모두 지우기/개별 X, notification_dismissal 신규) | docs/06_maintenance/20260711-140000/common/report.md |
| 20260711-160000 | common | 헤더 알림 5초 polling 전환(merge 정책·8건 cap 폐지·백그라운드 정지) | docs/06_maintenance/20260711-160000/common/report.md |
