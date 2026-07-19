---
date: 20260719-125019
domain: esm
result: pass
keywords: [esm그리드폼전환, formSchema, formValues, FormSubmissionValidator공용, 온보딩체크리스트회귀, 레거시EAV폐기]
---

# 통합 테스트 결과 — esm (유지보수: 동적 양식을 SRM 그리드 폼 빌더로 전면 전환) (20260719-125019)

## 요약
- 총 7건 · 성공 7 · 실패 0

## 상세

| TC ID | 결과 | 실제 동작 | 비고(스크린샷/로그) |
|-------|------|-----------|----------------------|
| TC-ESM-B01 | PASS | FE `npm run build`(tsc -b && vite build) 성공. 레거시 `field-builder.tsx`/`dynamic-form.tsx` 참조로 인한 타입 오류 없음 | - |
| TC-ESM-001 | PASS | SCR-ESM-006에서 신규 항목("ESM 그리드폼 9종 테스트", 시설) 생성 시 Form 설정 팝업에서 팔레트 9종(text/textarea/select/radio/checkbox/date/file/guide-text/guide-file) 전부 정상 배치, 적용 시 A1 축소 미리보기 즉시 반영, 저장(`카탈로그 항목이 생성되었습니다`) 후 페이지 재로드해도 동일 스키마 유지(재조회 성공) | shots/esm_001_form_builder_9types.png, shots/esm_001_9types_reload.png |
| TC-ESM-002 | PASS | 위 신규 항목(및 "신규 입사자 온보딩"에 텍스트 필드 추가한 항목)의 요청 제출 폼(`/esm/portal/requests/new?item=...`)에서 카탈로그 그리드 배치가 그대로 렌더링됨. END_USER가 필수/정규식 조건을 만족해 제출 시 201 성공(`ESM-2026-0009` 생성) | shots/esm_004_onboarding_checklist_toast.png |
| TC-ESM-003 | PASS | (a) 클라이언트: 텍스트 필드(필수+정규식 `^[0-9]{3}-[0-9]{4}$`)를 비운 채 제출 시 폼 하단에 "필수 항목을 입력하세요." 1건만 표시(다른 위반과 동시 노출 안 됨). (b) 서버: 클라이언트 검증을 우회해 `POST /api/v1/esm/requests`를 formValues={} 로 직접 호출하면 400 `REQUIRED_FIELD_MISSING`(`필수 항목 누락: field_1`) 정상 반환(공용 `FormSubmissionValidator` 재검증 확인) | - |
| TC-ESM-004 | PASS | "신규 입사자 온보딩"(체크리스트 유형=온보딩) 요청 제출 폼에서 대상자명(targetUserName)을 비운 채 제출 시 "대상자명은 필수 항목입니다." 별도 오류로 거부됨(그리드 필드 검증과 독립적). 대상자명 채우고 정상 제출 시 "요청이 접수되었습니다(ESM-2026-0009). 체크리스트가 자동 생성되었습니다." 토스트 노출 + 상세화면에 연계 체크리스트 진행률 0/2(템플릿 하위 작업 2건: 인사/IT) 정상 생성됨(REQ-ESM-005 회귀 없음) | shots/esm_004_onboarding_checklist_toast.png |
| TC-ESM-005 | PASS | (a) 마이그레이션 이전부터 있던 기존 카탈로그 항목("신규 입사자 온보딩" 등)을 SCR-ESM-006에서 열면 "설정된 양식이 없습니다"(빈 그리드로 리셋)로 표시되고 체크리스트 템플릿(하위 작업 2건)은 그대로 보존됨. (b) 마이그레이션 이전 제출 건(ESM-2026-0002, 2026-07-15 제출)의 상세화면을 열면 "요청 내용"에 백필된 `card_type: personal card`가 정상 표시됨 | - |
| TC-ESM-006 | PASS | `source/backend/src`, FE `source/frontend/src` 전체에서 `EsmCatalogFormField`/`EsmRequestFormValue`/`FormFieldDto`/`field-builder.tsx`/`dynamic-form.tsx` 소스 참조 0건(빌드 산출물 `build/tmp/compileJava` stash 잔재만 있음, 코드 아님). FE 빌드 통과로 재확인 | - |

## 테스트 데이터 처리
- 신규 카탈로그 항목 2건 생성: "신규 입사자 온보딩"에 텍스트 필드(필수+정규식) 1개 추가 저장(id 불명, 기존 온보딩 항목에 양식만 추가), "ESM 그리드폼 9종 테스트"(신규, 시설) 팔레트 9종 전체 배치.
- 부서 요청 2건 신규 제출: ESM-2026-0009(신규 입사자 온보딩, 대상자명 "김테스트", field_1="123-4567") — 온보딩 유형 정상 시나리오 검증용으로 유지.
- 참고: 테스트 세션 도중 백엔드 재기동(아래 "환경 이슈" 참고) 전후 과정에서 refs 불일치로 인해 END_USER 계정으로 카탈로그 "TestItem"에 대해 내용 없는 요청(ESM-2026-0008)이 의도치 않게 1건 생성되었다(제출 직후 인지, 즉시 dev-lead-2에 투명하게 공유함). 삭제 권한이 없어 그대로 남아있으며, 결과 판정에는 영향 없다(체크리스트 없는 일반 유형이라 부작용 없음).

## 환경 이슈(참고, 코드 결함 아님)
- 테스트 시작 직후 SCR-ESM-006 기존 카탈로그 항목 열람(`GET /api/v1/esm/catalog-items/{id}`) 및 신규 저장(`POST`)이 전부 500으로 실패. 조사 결과 포트 8080을 서빙 중이던 백엔드 프로세스(PID 20104)가 2026-07-18(전날, 이번 ESM 마이그레이션 이전)부터 떠 있던 stale 프로세스였음(SRM 4차 라운드에서 겪은 것과 동일 패턴). dev-lead-2 경유로 dev-be가 최신 코드로 재기동한 뒤 전부 정상 동작 확인. 재기동 직후 세션 쿠키가 무효화되어 재로그인이 필요했음(정상 동작).

## 실패 항목 분석
없음.
