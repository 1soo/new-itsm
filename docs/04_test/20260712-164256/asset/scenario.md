# 통합 테스트 시나리오 — asset (다국어(i18n) 텍스트 전환, 유지보수 요청 2026-07-12)

## 사전 조건
- 빌드 테스트 통과(backend/frontend)
- backend(:8080)/frontend(:5173) dev 서버 기동
- 테스트 계정(공통 비밀번호 `Admin@1234`): `am@itsm.local`(ASSET_MANAGER)
- 본 phase는 **레이아웃/컴포넌트 변경 없이 텍스트만 번역 키로 치환**(BE/DB 변경 없음). asset은 통합검색(SCR-COM-011) 대상 도메인이 아니므로(`SearchDomain`에 ASSET 미포함) 통합검색 TC는 시나리오에서 제외
- `status.ts`(typeLabel/statusLabel/expiryLabel/ticketTypeLabel/relationTypeLabel)에 falsy 가드 적용 확인, `format.ts`는 라벨 없는 순수 `ko-KR` 포맷터로 변경 없음(사전 확인 완료)
- dev-lead 사전 공지: `AssetDetailPage.tsx` 연결 티켓 나열의 티켓 유형, `CiRelationPage.tsx` 영향 범위 나열의 CI 관계 유형에서 change phase와 유사한 원시값 노출 결함을 developer가 자체 발견·선제 수정함(소스 리뷰로 확인 완료 — `ticketTypeLabel(t, lt.type)`/`relationTypeLabel(t, it.relationType)` 정상 적용)
- playwright는 매 TC마다 새 창(새 context)에서 수행, storage 초기화

## 시나리오

### TC-BUILD-001 · 백엔드/프론트엔드 빌드 테스트
- 근거: @docs/02_plan/screen/common.md (6절 i18n 아키텍처), @docs/03_develop/plan/asset.md ("i18n 다국어 전환" 절)
- 절차: 1) `./gradlew build -x test`(backend) 2) `npm run build`(frontend)
- 기대 결과: 두 빌드 모두 오류 없이 성공

### TC-ITAM-I18N-001 · 자산 목록(SCR-ITAM-001) 텍스트 전환 — 유형/상태/만료 배지 포함
- 근거: @docs/02_plan/screen/asset.md (SCR-ITAM-001), `features/asset/status.ts`
- 전제: am@itsm.local 로그인, English 전환
- 절차: 1) 자산 목록 진입 2) 필터(유형/상태/소유자/만료 임박/기간)·표 헤더 확인 3) 상태·유형·만료 배지 확인
- 기대 결과: 필터·표 헤더(식별키/이름/유형/상태/소유자/만료일) 영어 전환. 유형 배지 3종(Hardware/Software/Cloud), 상태 배지 5종(생애주기: Planning/Procurement/Operation/Maintenance/Retirement), 만료 배지(OK/Expiring/Expired류) 전부 전환 확인

### TC-ITAM-I18N-002 · 자산 등록/수정(SCR-ITAM-002) 텍스트 전환 및 회귀 — 만료일 과거 경고 포함
- 근거: @docs/02_plan/screen/asset.md (SCR-ITAM-002)
- 전제: am@itsm.local 로그인, English 전환
- 절차: 1) 자산 등록 화면 진입 2) 이름/유형 미입력 상태로 저장 시도 3) 이름·유형(Hardware) 입력, 만료일에 과거 날짜 입력 4) 저장
- 기대 결과: 폼 라벨(이름/유형/소유자/위치/상태/구매일/비용/계약·라이선스·보증 만료일) 영어 전환, 유형 선택 시 유형별 속성 필드 라벨 전환. 필수 미입력 오류 영어 전환. 만료일 과거 입력 시 경고 문구 영어 전환. 저장 성공 토스트 영어 전환 후 상세 이동(회귀 없음)

### TC-ITAM-I18N-003 · 자산 상세(SCR-ITAM-003) 텍스트 전환 — 생애주기·만료·티켓 연계·CI 포함
- 근거: @docs/02_plan/screen/asset.md (SCR-ITAM-003)
- 전제: am@itsm.local 로그인, English 전환, TC-ITAM-I18N-002에서 등록한 자산으로 진입
- 절차: 1) 생애주기 단계 전이(계획→구매 등) 2) 티켓 연계 폼에서 유형별 옵션 확인 후 연계 생성 3) 연결 티켓 나열에서 유형 라벨 확인(원시값 노출 없는지) 4) 승인 패널(공용) 확인(폐기 전이 시도, 매칭 프로세스 없으면 패널 미노출·즉시 전이)
- 기대 결과: 생애주기 전이 버튼·만료 정보·"연결 티켓" 섹션 라벨 영어 전환. 티켓 연계 유형 드롭다운(Service Request/Incident/Problem/Change) 영어 전환. 연결 티켓 나열이 원시 enum("INCIDENT" 등)이 아닌 번역된 라벨("Incident" 등)로 표시. 폐기 시도 시 매칭 프로세스 없으면 기존처럼 즉시 전이(회귀 없음), 있으면 승인 패널에 차수 진행 상태 영어 전환 표시

### TC-ITAM-I18N-004 · CI·CMDB 관계 뷰(SCR-ITAM-004) 텍스트 전환 — 관계 추가·영향 범위 포함
- 근거: @docs/02_plan/screen/asset.md (SCR-ITAM-004)
- 전제: am@itsm.local 로그인, English 전환
- 절차: 1) CI 목록에서 CI 선택(또는 신규 등록) 2) "관계 추가" 폼에서 관계 유형 드롭다운 확인 3) 대상 CI ID 입력 후 관계 추가 4) "영향 범위" 패널에서 관계 유형 라벨 확인(원시값 노출 없는지)
- 기대 결과: "CI 목록"/"관계 추가"/"영향 범위" 섹션 타이틀, CI 등록 폼 라벨, 관계 유형 드롭다운(Depends On/Runs On/Connects To 류) 영어 전환. 영향 범위 나열이 원시 enum("DEPENDS_ON" 등)이 아닌 번역된 라벨로 표시(단, 기존 UI 설계상 라벨 옆에 원시 코드 병기("(DEPENDS_ON)")가 있다면 이는 이번 phase 이전부터 있던 디자인이므로 정상). 관계 없는 CI 선택 시 "연결된 CI가 없습니다"류 안내 영어 전환

### TC-ITAM-I18N-005 · 자산 지표 대시보드(SCR-ITAM-005) 텍스트 전환
- 근거: @docs/02_plan/screen/asset.md (SCR-ITAM-005)
- 전제: am@itsm.local 로그인, English 전환
- 절차: 1) 지표 대시보드 진입 2) 기간 필터·KPI 카드(활용률/만료 임박)·유형 분포 차트 확인
- 기대 결과: 기간 필터 라벨·KPI 카드 라벨·유형 분포 차트 라벨(Hardware/Software/Cloud) 전부 영어 전환

### TC-ITAM-FORMAT-REG-001 · 날짜 포맷 회귀(ko-KR 유지)
- 근거: @docs/02_plan/screen/common.md (SCR-COM-015 "확정된 결정 2")
- 전제: English 상태
- 절차: 1) 자산 목록/상세의 만료일·구매일 등 날짜 표시 확인
- 기대 결과: 날짜/시각 포맷이 ko-KR 로케일 그대로 유지, 영어 로케일 포맷으로 바뀌지 않음

### TC-ITAM-KO-ROUNDTRIP-001 · 한국어 재전환 라운드트립 확인
- 근거: @docs/02_plan/screen/common.md (SCR-COM-015)
- 전제: English 상태
- 절차: 1) 지구본 아이콘으로 한국어 재전환 2) 자산 목록/상세/CI 관계 뷰 중 1~2개 재확인
- 기대 결과: 새로고침 없이 즉시 한국어로 복귀, 기존 문구와 동일(누락·깨짐 없음)
