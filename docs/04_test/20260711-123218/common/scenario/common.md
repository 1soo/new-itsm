# 통합 테스트 시나리오 — common (알림 확인처리, 보강 검증)

> 실행 타임스탬프: 20260711-123218 · 도메인: common
> 배경: dev-lead 재요청 — 직전 실행(20260711-122421, 20/20 PASS)에서 API-COM-001/002 핵심 동작·개별X·모두지우기·영구저장·원본데이터불변은 이미 검증 완료.
> 이번 실행은 dev-lead가 명시한 항목 중 직전 실행에서 명시적으로 다루지 않은 **사용자별 확인처리 이력 격리**와 **상세 보기 이동 회귀**에 집중한다.
> 참고: `docs/03_develop/plan/common.md` "알림 확인처리" 절, 직전 결과 `docs/04_test/common/20260711-122421/result/common.md`

## 사전 조건

- Backend(:8080)·Frontend(:5173) 기동, PostgreSQL healthy
- 직전 실행으로 cab@itsm.local(SRM-2026-0007·CHG-2026-0006)·am@itsm.local(자산 id=8) 확인처리 이력이 이미 존재(영구 저장 특성상 유지됨) — 본 실행은 이 상태를 그대로 전제로 진행
- am@itsm.local에는 미확인처리 자산 3건(id=5,6,7) 남아있어 회귀(상세 보기 이동) 검증에 사용

## 시나리오

### A. 사용자별 확인처리 이력 격리(신규 검증)

#### TC-ISO-001 · 서로 다른 사용자의 확인처리 이력 상호 미노출
- 근거: @docs/02_plan/api_spec/common.md (API-COM-002, "로그인 사용자 본인 전체 확인처리 이력 조회"), dev-lead 요청("다른 사용자 이력 노출 안 됨")
- 절차: 사용자A(END_USER 신규 계정)로 `POST /notifications/dismissals` {items:[{ASSET_EXPIRY, sourceId:777001}]} → 사용자B(다른 신규 계정)로 `GET /notifications/dismissals` 조회
- 기대 결과: 사용자A의 GET 결과에는 (ASSET_EXPIRY,777001) 포함, 사용자B의 GET 결과에는 미포함(빈 배열 또는 해당 항목 없음)

#### TC-ISO-002 · 동일 (notificationType, sourceId)를 서로 다른 사용자가 각자 확인처리 가능(사용자별 독립)
- 근거: @docs/02_plan/database/common.md (UNIQUE(user_id, notification_type, source_id) — user_id 포함 복합 유니크이므로 사용자마다 독립)
- 절차: 사용자B로 사용자A와 동일한 {ASSET_EXPIRY, sourceId:777001} 확인처리 요청
- 기대 결과: 200, {dismissedCount:1}(사용자A의 확인처리와 무관하게 사용자B 본인 기준 신규 처리로 인정, 409/충돌 없음)

---

### B. FE 회귀 — 상세 보기 이동 (개별 X와의 이벤트 분리 재확인)

#### TC-E2E-101 · 알림 라인의 "상세 보기" 클릭 시 정상적으로 상세 화면 이동
- 근거: @docs/02_plan/screen/common.md (SCR-COM-002, "상세 보기 버튼 클릭 시 해당 상세로 이동"), dev-lead 요청(회귀 — 상세 보기 이동)
- 전제: am@itsm.local 알림 팝오버에 자산 만료 미확인처리 3건(id=5,6,7) 존재
- 절차: am@itsm.local 로그인 → 알림 벨 클릭 → 임의 항목의 "상세 보기" 버튼 클릭(개별 X 아님)
- 기대 결과: 해당 자산 상세 화면(`/assets/{id}`)으로 정상 이동(개별 X 클릭과 달리 페이지 이동 발생 — TC-E2E-005의 미이동 동작과 대비되는 정상 이동 확인)
