---
date: 20260718-120242
domain: common
change_type: [modified]
keywords: [404 catch-all 회귀, NoResourceFoundException, GlobalExceptionHandler, ENDPOINT_NOT_FOUND]
---

# 유지보수 이력 — Common (미매핑 경로 404 회귀 수정)

> 유지보수 일시: 20260718-120242 · 도메인: common

## 1. 요구사항

SRM 요청 큐 폐지 유지보수(20260718-114544) 통합테스트 중, 삭제된 경로 `/api/v1/queues` 호출 시 기대한 404가 아니라 500이 반환되는 결함이 발견됐다.
당시엔 이번 SRM 변경과 무관한 앱 전역의 기존 결함으로 판단해 범위 밖으로 분리했으나, Main이 직접 코드로 원인을 확인하고 즉시 수정을 지시했다(별도 분석·설계 단계 생략).
존재하지 않는 모든 경로 호출은 500이 아니라 404로 응답해야 한다.

## 2. 해결 방법

원인은 `common.exception.GlobalExceptionHandler`의 맨 아래 `@ExceptionHandler(Exception.class) handleUnexpected()` catch-all이었다.
이 catch-all이 존재하지 않는 경로 호출 시 Spring Boot 4.1(Spring Framework 7)이 던지는 `NoResourceFoundException`(및 `NoHandlerFoundException`)까지 가로채 500(`INTERNAL_ERROR`)으로 응답하고 있었다.
`ErrorCode`에 `ENDPOINT_NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 경로를 찾을 수 없습니다.")`를 신규 추가했다.
`GlobalExceptionHandler`에 `@ExceptionHandler({NoResourceFoundException.class, NoHandlerFoundException.class})` 핸들러를 catch-all보다 위에 추가해 404로 응답하도록 수정했다.

## 3. 변경 파일

- `source/backend/src/main/java/com/itsm/common/exception/ErrorCode.java`
- `source/backend/src/main/java/com/itsm/common/exception/GlobalExceptionHandler.java`

## 4. 테스트 결과

dev-backend가 로컬 임시 인스턴스(8099, 공유 8080 서버는 건드리지 않음)로 재현을 확인한 뒤 수정했다.
`/api/v1/queues`와 임의의 미존재 경로 모두 404(`ENDPOINT_NOT_FOUND`)로 응답함을 확인했다.
`./gradlew test`(Testcontainers 포함 전체 스위트)는 BUILD SUCCESSFUL이었다.
tester 재검증 결과 미매핑 경로 404 확인과 함께 기존 API 회귀는 없었다(카탈로그 조회 200, `categoryId=abc` 400 유지, 인증없음 401, 권한없음 403).
이번 수정은 common 전역 영역이라 전 도메인에 영향을 준다(모든 존재하지 않는 경로가 404로 정상 응답).
커밋 `770680d`로 origin/main에 push 완료됐다.
