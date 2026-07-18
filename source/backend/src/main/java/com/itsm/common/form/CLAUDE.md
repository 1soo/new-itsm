# CLAUDE.md

SRM 전용 동적 폼(자체 8×n 그리드) 제출 데이터 서버 재검증 모듈(2026-07-18 유지보수 요청, form.io 완전 제거).

## 파일
- `FormSubmissionValidator.java` — `form_schema.components`(평면 배열, 중첩 레이아웃 없음)를 배열 순서대로 순회해 컴포넌트별 `validation.required`/`validation.regex` 위반 시 첫 번째 위반에서 즉시 400(`BusinessException`, `REQUIRED_FIELD_MISSING`/`FORM_FIELD_INVALID`)으로 거부하는 순수 정적 유틸리티(여러 위반을 모아 반환하지 않음 — FE 순차 1건 표시와 동일 계약, 2026-07-18 유지보수 요청). `type=label`/`guide-text`/`guide-file`는 값이 없는 정적 컴포넌트라 검증 대상에서 제외한다(guide-text/guide-file 제외는 2026-07-18 그리드 폼 빌더 후속 개선, 이전 단일 `guide` 타입은 폐기됨). `required`와 `regex`가 함께 지정되면 값이 있을 때만 정규식을 평가한다(값 없으면 required 위반으로 이미 거부). `regex`가 null이거나 빈 문자열이면 형식 검증을 생략한다. SRM `ServiceRequestService`가 호출한다.
- `FormJsonMapper.java` — formSchema/formValues(`Map<String,Object>`) ↔ JSON 문자열 직렬화 정적 유틸(`writeJson`/`readJsonMap`). SRM `ServiceCatalogService`(formSchema)·`ServiceRequestService`(formSchema/formValues)가 각자 구현하던 동일 로직 중복 제거(2026-07-18 코드 리뷰).
