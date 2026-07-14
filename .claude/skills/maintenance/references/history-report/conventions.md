# 유지보수 이력 생성 컨벤션

`dev-lead`가 전달한 유지보수 개발 완료 내역을 도메인별로 정리해 이력 리포트를 작성하고, 인덱스를 갱신한다.

## 작성 원칙

- 전달받은 수정 내역에 없는 내용은 추측해 덧붙이지 않는다.
- 도메인, 요구사항, 해결 방법 위주로 자세하고 직관적으로 작성한다.
- **문장마다 반드시 줄바꿈**한다. (한 줄에 여러 문장을 이어 쓰지 않는다)
- 표준 양식은 항상 [template.md](template.md)를 그대로 사용한다.

## 절차

1. `dev-lead`가 전달한 도메인별 수정 내역과 KST 타임스탬프(`yyyyMMdd-HHmmss`)를 확인한다.
2. 도메인별로 `docs/06_maintenance/{yyyyMMdd-HHmmss}/{domain}/report.md`를 template에 맞춰 작성한다. (`{yyyyMMdd-HHmmss}`는 `dev-lead`가 `Bash`의 `date +"%Y%m%d-%H%M%S"`로 확인해 전달한 실제 시각을 그대로 사용한다. 임의로 생성·추측하지 않는다. 도메인이 여러 개여도 같은 실행 회차는 동일한 타임스탬프를 공유한다)
3. 작성 후 해당 유지보수 내용을 `docs/06_maintenance/CLAUDE.md` 인덱스 표에 간결하고 직관성 높게 한 줄로 추가한다.

## 산출물

- 리포트: `docs/06_maintenance/{yyyyMMdd-HHmmss}/{domain}/report.md`
- 인덱스 갱신: `docs/06_maintenance/CLAUDE.md`
