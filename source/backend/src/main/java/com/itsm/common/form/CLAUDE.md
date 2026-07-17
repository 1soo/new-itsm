# CLAUDE.md

SRM/ESM 공용 동적 폼(form.io) 제출 데이터 서버 재검증 모듈(2026-07-17 유지보수 요청).

## 파일
- `FormSubmissionValidator.java` — `form_schema`(Form.io Form JSON)를 재귀 순회해 `input:true` 리프 컴포넌트만 수집(레이아웃 컴포넌트는 하위만 펼침) 후 `validate` 규칙(required/minLength/maxLength/min/max/pattern) 위반 시 400(`BusinessException`)으로 거부하는 순수 정적 유틸리티. `conditional`/`calculateValue`/`custom`은 해석하지 않는다. SRM `ServiceRequestService`가 호출한다. `pattern`은 빈 문자열("")이면 검증을 생략한다(Form.io Builder가 패턴 미설정 필드도 `"pattern":""`로 직렬화, TC-SRM-004/006 회귀 수정 2026-07-17).
- `FormJsonMapper.java` — formSchema/formValues(`Map<String,Object>`) ↔ JSON 문자열 직렬화 정적 유틸(`writeJson`/`readJsonMap`). SRM `ServiceCatalogService`(formSchema)·`ServiceRequestService`(formSchema/formValues)가 각자 구현하던 동일 로직 중복 제거(2026-07-18 코드 리뷰).
