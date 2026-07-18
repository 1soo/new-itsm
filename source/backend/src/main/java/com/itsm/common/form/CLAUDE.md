# CLAUDE.md

SRM 전용 동적 폼(자체 8×n 그리드) 제출 데이터 서버 재검증 모듈(2026-07-18 유지보수 요청, form.io 완전 제거).

## 파일
- `FormSubmissionValidator.java` — `form_schema.components`(평면 배열, 중첩 레이아웃 없음)를 순회해 컴포넌트별 `validation.required`/`validation.regex` 위반 시 400(`BusinessException`, `REQUIRED_FIELD_MISSING`/`FORM_FIELD_INVALID`)으로 거부하는 순수 정적 유틸리티. `required`와 `regex`가 함께 지정되면 값이 있을 때만 정규식을 평가한다(값 없으면 required 위반으로 이미 거부). `regex`가 null이거나 빈 문자열이면 형식 검증을 생략한다. SRM `ServiceRequestService`가 호출한다.
- `FormJsonMapper.java` — formSchema/formValues(`Map<String,Object>`) ↔ JSON 문자열 직렬화 정적 유틸(`writeJson`/`readJsonMap`). SRM `ServiceCatalogService`(formSchema)·`ServiceRequestService`(formSchema/formValues)가 각자 구현하던 동일 로직 중복 제거(2026-07-18 코드 리뷰).
