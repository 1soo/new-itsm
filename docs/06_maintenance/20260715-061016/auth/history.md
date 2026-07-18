---
date: 20260715-061016
domain: auth
change_type: [new]
keywords: [역할 목록 조회 API, 공개 API, 타 도메인 재사용]
---

# 유지보수 이력 — auth

> 유지보수 일시: 20260715-061016 · 도메인: auth

## 1. 요구사항

서비스 카탈로그 관리 화면에서 요청 유형별 담당자를 역할 기반으로 지정하려면, 프론트엔드에서 선택 가능한 전체 역할 목록을 조회할 수 있어야 했다.
기존에는 역할 목록 조회가 승인 프로세스 관리(SYSTEM_ADMIN 전용) 용도로만 제한적으로 제공되어, 서비스 카탈로그 관리 화면(PROCESS_OWNER) 등 다른 도메인 화면에서 재사용할 수 있는 공용 역할 목록 API가 없었다.

## 2. 해결 방법

인증된 사용자면 누구나 호출 가능한 역할 목록 조회 API(API-AUTH-030, `GET /api/v1/roles`)를 신규로 추가했다.
`RoleService`와 응답 DTO(`RoleOptionResponse`)를 신규 구현하고, `RoleController`를 신설해 엔드포인트를 노출했다.
프론트엔드 auth 도메인의 `api.ts`/`types.ts`에 이 API 호출을 위한 함수·타입을 추가해, service-request 도메인(카탈로그 관리 화면의 담당자 역할 선택)에서 재사용할 수 있도록 했다.

## 3. 변경 파일

- `docs/02_plan/api_spec/auth.md`
- `docs/03_develop/plan/auth.md`
- `source/backend/src/main/java/com/itsm/auth/application/RoleService.java`(신규)
- `source/backend/src/main/java/com/itsm/auth/application/CLAUDE.md`
- `source/backend/src/main/java/com/itsm/auth/application/dto/RoleOptionResponse.java`(신규)
- `source/backend/src/main/java/com/itsm/auth/application/dto/CLAUDE.md`
- `source/backend/src/main/java/com/itsm/auth/presentation/RoleController.java`(신규)
- `source/backend/src/main/java/com/itsm/auth/presentation/CLAUDE.md`
- `source/frontend/src/features/auth/{api.ts,types.ts,CLAUDE.md}`

## 4. 테스트 결과

통합 테스트 결과는 `docs/04_test/20260715-142838/auth/`에 기록되어 있다.
최종 발견 사항 없이 전부 PASS했다.
커밋 `fb092ef`로 반영했다.
