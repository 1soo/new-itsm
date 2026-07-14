---
name: maintenance
description: 개발 완료된 시스템에 대한 추가 요구사항(유지보수 요청) 분석과 유지보수 이력 기록 방법. 유지보수 요청 접수 시, 유지보수 개발 완료 후 이력 기록이 필요할 때 사용한다.
---

# 유지보수

`maintainer`가 사용하는 유지보수 skill. 처리 단계에 따라 `references`의 하위 폴더를 참조한다.

## 참조 안내

| 단계 | 참조 시점 | 참고 문서 |
|------|-----------|-----------|
| 요구사항 분석 | 사용자가 개발 완료된 시스템에 추가 요구사항을 제시했을 때, `designer`에게 전달하기 전 | [references/request-analysis/conventions.md](references/request-analysis/conventions.md) |
| 이력 생성 | `dev-lead`로부터 유지보수 개발+테스트 완료 알림(도메인별 수정 내역, KST 타임스탬프)을 받았을 때 | [references/history-report/conventions.md](references/history-report/conventions.md), [references/history-report/template.md](references/history-report/template.md) |

## 공통 원칙

- 요구사항·전달받은 수정 내역에 명시된 범위 내에서만 다룬다.
