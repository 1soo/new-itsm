---
date: 20260718-145952
domain: srm
result: pass
keywords: [label 컴포넌트, date/file 아이콘화, 순차 단일 오류, 배열 순서, 서버 재검증]
---

# 통합 테스트 결과 — srm 그리드 폼 빌더 세부 개선 (20260718-145952)

## 요약
- 총 8건 · 성공 8 · 실패 0

## 상세

| TC ID | 결과 | 실제 동작 | 비고(스크린샷/로그) |
|-------|------|-----------|----------------------|
| TC-GF-B01 | PASS | BE `./gradlew clean build`(테스트 포함) BUILD SUCCESSFUL(2m32s), FE `npm run build` 성공 | - |
| TC-GF-001 | PASS | 팔레트 8종(text/textarea/select/radio/checkbox/date/file/label) 노출. label 팝오버는 "표시 텍스트"+정렬(좌/가운데/우)만 존재. 나머지 7종(text 등) 팝오버에는 "라벨 텍스트"/"label 정렬" 항목 없음(input 폭%/정렬/기본값/읽기전용/필수/정규식만) | gf_label_settings.png, gf_text_settings.png |
| TC-GF-002 | PASS | label 컴포넌트를 빈 셀로 이동(정상), 다른 컴포넌트가 점유한 셀로 이동 시도 시 겹침 경고("이미 배치된 컴포넌트와 겹칩니다")+원위치 복귀, 리사이즈 시도 시 폭 1~2·높이 1~2로 캡(다른 비-textarea 컴포넌트와 동일 규칙) | pointer 이벤트 자동화로 재현 |
| TC-GF-003 | PASS | 요청 제출 화면·pre-view 둘 다 date/file이 아이콘 전용("날짜 선택"/"파일 선택" 버튼)으로 렌더링, 브라우저 기본 input 노출 없음. 아이콘 클릭 시 숨겨진 네이티브 input의 피커/파일선택 트리거 확인(date는 값 설정 시 "2026-08-01" 아이콘 옆 표시, file은 업로드 시 "gf_test_upload.txt" 파일명 아이콘 옆 표시) | gf_labeltest_preview.png |
| TC-GF-004 | PASS | 배열 순서(field_1 required, 시각적으로 우측col1) vs 그리드 시각 순서(field_2 regex 위반, 시각적으로 좌측col0)가 다른 케이스로 검증 — 둘 다 위반 상태로 제출 시 폼 하단에 "필수 항목을 입력하세요."(field_1, 배열 첫 번째) 1건만 표시(그리드 좌측인 field_2의 regex 오류가 아님). field_1만 채우고 재제출하니 "입력 형식이 올바르지 않습니다."(field_2, 배열 두 번째)로 전환 — 배열 순서 기준·순차 재검사·단일 표시 모두 확인 | - |
| TC-GF-005 | PASS | `POST /api/v1/service-requests`에 field_1(필수 누락)·field_2(regex 위반) 동시 위반 페이로드 직접 호출 → 400 `REQUIRED_FIELD_MISSING`("필수 항목 누락: field_1")만 반환, field_2의 regex 위반은 별도 반환되지 않음(배열 순서상 첫 번째만 즉시 응답) | curl 근거 |
| TC-GF-006 | PASS | label 컴포넌트가 포함된 스키마로 값 없이(label은 애초에 값이 없음) 제출 시 label로 인한 400 없음. 실제 입력 필드(field_1) 누락 시에만 400, 채우면 201 정상 생성 | curl 근거 |
| TC-GF-007 | PASS | 요청 처리함(SCR-SRM-004) 카테고리 목록·건수·미분류 마지막 고정 정상(회귀 없음), 팔레트 기존 기능·겹침방지·리사이즈·pre-view 라운드트립(TC-GF-002/003 확인 과정에서 함께 재확인)도 이전 통합 테스트(`20260718-135109`)와 동일하게 정상 | - |

## 테스트 데이터 처리
- item 8("그리드 빌더 테스트")에 순차 오류·label 제외 검증용 임시 스키마를 API PATCH로 적용 후 테스트, 완료 후 원본 스키마로 복원 완료(백업 JSON 기반 재적용, 최종 상태 원본과 동일 확인).
- item 10("라벨아이콘테스트")은 dev-fe가 남긴 테스트 픽스처를 읽기 전용으로만 활용(변경 없음, Form 설정 팝업에서 이동/겹침/리사이즈 테스트 후 "취소"로 미저장 종료).
- 테스트 중 생성된 SRM-2026-0034~0036 요청 티켓은 기존 세션들과 동일하게 테스트 잔여 데이터로 유지(별도 정리 불필요, 이전 통합 테스트들과 동일 패턴).

## 실패 항목 분석
없음.
