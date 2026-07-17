# 통합 테스트 시나리오 — srm (재테스트: 제출/취소 버튼 중복 Submit 결함 수정 확인)

## 사전 조건
- 계정: user@itsm.local(END_USER) — 비밀번호 `Admin@1234`
- 서버: 백엔드 `localhost:8080`(PID 34280, 최신 코드로 재기동됨), 프론트엔드 `localhost:5173`
- 참고: 이전 실행 `docs/04_test/20260717-152015/srm/result/srm.md`(TC-SRM-105 FAIL — 중복 Submit 버튼)

## 시나리오

### TC-SRM-201 · 중복 Submit 버튼 제거 확인(TC-SRM-105 재검증)
- 근거: @docs/03_develop/plan/common.md "추가 요구사항(2026-07-17) — 폼 렌더러 제출/취소 버튼 우측 하단 배치" 절, 이전 실행 TC-SRM-105(FAIL)
- 전제: `/portal/requests/new?item=7`("폼빌더 회귀 테스트 항목")
- 절차: 1. 화면 스냅샷/스크린샷으로 버튼 구성 확인 2. Form.io 내장 Submit 컴포넌트의 실제 렌더 크기·부모 display 확인 3. 신규 푸터 "제출" 버튼으로 실제 제출 시도
- 기대 결과: 화면에 취소·제출(우측 정렬, 취소 좌측·제출 우측) 버튼만 노출, 레거시 Submit 버튼은 부모 컨테이너가 `display:none`으로 완전히 숨겨짐(0x0 크기). 제출 버튼 클릭 시 정상 제출(201)
