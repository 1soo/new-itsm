# 통합 테스트 시나리오 — common (알림 확인처리: 모두 지우기·개별 X)

> 실행 타임스탬프: 20260711-122421 · 도메인: common · 범위: API-COM-001/002, SCR-COM-002 헤더 알림 팝오버 확인처리
> 참고: `docs/03_develop/plan/common.md` "알림 확인처리" 절(완료 기준), `docs/02_plan/api_spec/common.md`, `docs/02_plan/database/common.md` 4절

## 사전 조건

- 빌드 테스트 통과(Backend Gradle, Frontend Vite build)
- PostgreSQL(itsm-postgres) healthy, `notification_dismissal` 테이블 생성됨(`25_common_notification_dismissal.sql`)
- Backend(:8080)·Frontend(:5173) 기동
- 데모 계정: `cab@itsm.local`/`Admin@1234`(APPROVER, 승인 대기 서비스요청 1건 SRM-2026-0007/requestId=8 + 변경 1건 CHG-2026-0006/changeId=9 보유), `am@itsm.local`/`Admin@1234`(ASSET_MANAGER, 만료 임박 자산 4건 보유)
- 격리: playwright는 매 항목 새 context(새 창)·storage 초기화. API는 항목별 신규 토큰 발급. BE 단위 API 테스트는 실제 승인/자산 데이터를 건드리지 않도록 합성 sourceId(9990xx)를 사용한다.

## 시나리오

### A. 빌드 테스트

#### TC-BUILD-001 · Backend 빌드·단위테스트
- 근거: @docs/02_plan/api_spec/common.md (API-COM-001/002)
- 절차: `./gradlew clean test build`
- 기대 결과: BUILD SUCCESSFUL

#### TC-BUILD-002 · Frontend 빌드
- 근거: @docs/02_plan/screen/common.md (SCR-COM-002 확인처리 버튼)
- 절차: `npm run build`
- 기대 결과: 타입체크·번들 성공

---

### B. BE API — 알림 확인처리 (API-COM-001)

#### TC-COM-001 · 개별 확인처리 1건 200
- 근거: @docs/02_plan/api_spec/common.md (API-COM-001 200)
- 절차: 로그인 → `POST /notifications/dismissals` {items:[{notificationType:"ASSET_EXPIRY", sourceId:999001}]}
- 기대 결과: 200, {dismissedCount:1}

#### TC-COM-002 · 일괄 확인처리(8건) 200
- 근거: @docs/02_plan/api_spec/common.md (API-COM-001, "모두 지우기"=상위 8건 공용)
- 절차: 신규 사용자로 `POST /notifications/dismissals` {items: 8개 서로 다른 (type,sourceId) 조합}
- 기대 결과: 200, {dismissedCount:8}

#### TC-COM-003 · 중복 포함 시 멱등 처리
- 근거: @docs/02_plan/api_spec/common.md (API-COM-001, "이미 확인처리된 항목은 무시")
- 절차: TC-COM-001과 동일 {notificationType:"ASSET_EXPIRY", sourceId:999001}를 재요청
- 기대 결과: 200, {dismissedCount:0}(신규 저장 없음, 오류 아님)

#### TC-COM-004 · 신규+기존 혼합 시 신규 건수만 카운트
- 근거: @docs/02_plan/api_spec/common.md (API-COM-001 멱등)
- 절차: {items:[{ASSET_EXPIRY,999001}(기존), {ASSET_EXPIRY,999002}(신규)]} 요청
- 기대 결과: 200, {dismissedCount:1}

#### TC-COM-005 · items 누락(빈 배열) 400
- 근거: @docs/02_plan/api_spec/common.md (API-COM-001 400)
- 절차: `POST /notifications/dismissals` {items:[]}
- 기대 결과: 400, code=VALIDATION_ERROR

#### TC-COM-006 · items 필드 누락(요청 자체 누락) 400
- 근거: @docs/02_plan/api_spec/common.md (API-COM-001 400)
- 절차: `POST /notifications/dismissals` {}
- 기대 결과: 400, code=VALIDATION_ERROR

#### TC-COM-007 · 미인증 401
- 근거: @docs/02_plan/api_spec/common.md (API-COM-001 401)
- 절차: Authorization 없이 `POST /notifications/dismissals` {items:[{notificationType:"ASSET_EXPIRY",sourceId:999003}]}
- 기대 결과: 401

---

### C. BE API — 확인처리 이력 조회 (API-COM-002)

#### TC-COM-101 · 이력 없음(신규 사용자) 빈 배열
- 근거: @docs/02_plan/api_spec/common.md (API-COM-002 200, "이력 없으면 빈 배열")
- 절차: 확인처리 이력이 없는 신규 END_USER 계정으로 `GET /notifications/dismissals`
- 기대 결과: 200, {items:[]}

#### TC-COM-102 · 확인처리 이력 반영 조회
- 근거: @docs/02_plan/api_spec/common.md (API-COM-002 200)
- 절차: TC-COM-001~004로 확인처리한 사용자로 `GET /notifications/dismissals`
- 기대 결과: 200, items에 (ASSET_EXPIRY,999001)·(ASSET_EXPIRY,999002) 포함, 각 항목에 dismissedAt 포함

#### TC-COM-103 · 미인증 401
- 근거: @docs/02_plan/api_spec/common.md (API-COM-002 401)
- 절차: Authorization 없이 `GET /notifications/dismissals`
- 기대 결과: 401

---

### D. FE E2E — 헤더 알림 팝오버 확인처리 (playwright, 매 항목 새 context)

#### TC-E2E-001 · 승인자(cab@itsm.local) 알림 팝오버 — 개별 X로 1건만 제거(라인 클릭·상세보기와 분리)
- 근거: @docs/02_plan/screen/common.md (SCR-COM-002, 개별 확인처리(X) 버튼), @docs/03_develop/plan/common.md (완료 기준)
- 전제: cab@itsm.local 알림 팝오버에 서비스요청 승인(SRM-2026-0007)·변경 승인(CHG-2026-0006) 2건 표시
- 절차: 로그인 → 알림 벨 클릭 → 서비스요청 승인 항목의 개별 X 클릭
- 기대 결과: 해당 1건만 목록에서 즉시 사라짐(성공, 페이지 이동 없음 — 상세보기로 이동하지 않았는지 확인), 변경 승인 1건은 남음, 벨 뱃지 카운트 2→1

#### TC-E2E-002 · "모두 지우기"로 남은 알림 일괄 확인처리 → 빈 상태 전환
- 근거: @docs/02_plan/screen/common.md (SCR-COM-002, 모두 지우기 버튼)
- 전제: TC-E2E-001 이후 동일 세션(변경 승인 1건 남은 상태)
- 절차: 알림 팝오버 "모두 지우기" 클릭
- 기대 결과: 목록이 즉시 비워지고 "새로운 알림이 없습니다" 표시, 벨 뱃지 사라짐(0건)

#### TC-E2E-003 · 확인처리 영구 저장 — 재로그인 후에도 다시 나타나지 않음
- 근거: @docs/03_develop/plan/common.md (완료 기준, "재로그인/재조회 후에도 다시 나타나지 않음")
- 전제: TC-E2E-002로 cab@itsm.local의 2건 모두 확인처리 완료
- 절차: storage 초기화(새 context) → cab@itsm.local 재로그인 → 알림 벨 클릭
- 기대 결과: 서비스요청 승인(SRM-2026-0007)·변경 승인(CHG-2026-0006) 모두 미노출, "새로운 알림이 없습니다" 또는 벨 뱃지 0

#### TC-E2E-004 · 확인처리가 원본 승인 대기/자산 데이터에 영향 없음
- 근거: @docs/02_plan/api_spec/common.md (API-COM-001, "원본 승인 대기·자산 만료 데이터는 변경하지 않는다")
- 절차: TC-E2E-003 이후 `GET /api/v1/approvals?scope=mine&type=service-request`, `GET /api/v1/approvals?scope=mine&type=change`(cab 토큰)로 원본 데이터 재조회
- 기대 결과: requestId=8(SRM-2026-0007)·changeId=9(CHG-2026-0006) 원본 승인 대기 데이터가 그대로 존재(삭제·상태변경 없음), 확인처리는 표시 여부만 변경했음을 확인

#### TC-E2E-005 · 자산 관리자(am@itsm.local) 개별 X 확인
- 근거: @docs/02_plan/screen/common.md (SCR-COM-002, 자산 만료 알림)
- 절차: am@itsm.local 로그인 → 알림 팝오버에서 자산 만료 항목 1건 개별 X 클릭
- 기대 결과: 해당 1건만 제거, 나머지 자산 만료 항목은 유지
