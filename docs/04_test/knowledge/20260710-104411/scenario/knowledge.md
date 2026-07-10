# 통합 테스트 시나리오 — knowledge (KM) 재테스트

> 실행 타임스탬프: 20260710-104411 · 도메인: knowledge
> 목적: 지식베이스 목록 테이블 컬럼 줄바꿈 버그 수정(`source/frontend/src/features/knowledge/KnowledgeListPage.tsx` 카테고리/상태/유용성 컬럼 `whitespace-nowrap` 추가) 회귀 검증
> 근거: `docs/01_analyze/feature/knowledge.md`(SCR-KM-001 관련 FEAT), `docs/02_plan/screen/common.md`(SCR-COM-007 공통 목록 패턴)

## 사전 조건
- BE(:8080)·FE dev(:5173) 기동, PostgreSQL(`itsm-postgres`) healthy(기 기동 환경 재사용)
- 계정: kc@itsm.local/Admin@1234(KNOWLEDGE_CONTRIBUTOR)

## 시나리오

### A. 빌드 (재검증)
- **TC-BUILD-001** · FE `npm run build`(tsc -b && vite build) 통과 — 수정분 포함

### B. 컬럼 줄바꿈 수정 검증
- **TC-KM-COL-001** · 라이트 테마에서 카테고리/상태/유용성 컬럼 줄바꿈 없이 한 줄 표시 — @docs/02_plan/screen/common.md (SCR-COM-007)
  - 절차: kc@itsm.local 로그인 → `/knowledge` 목록 진입 → 카테고리("네트워크" 등)/상태 배지("게시" 등)/유용성(%) 셀의 computed `white-space` 및 렌더링 높이(줄바꿈 시 셀 높이 증가) 확인
  - 기대 결과: 세 컬럼 모두 `white-space: nowrap` 적용, 텍스트가 한 줄로 표시(줄바꿈 없음)
- **TC-KM-COL-002** · 다크 테마에서 동일하게 줄바꿈 없이 정상 표시 — REQ-UIX-002 관련 회귀 없음
  - 절차: 테마 토글로 다크 전환 후 `/knowledge` 재확인
  - 기대 결과: 라이트와 동일하게 줄바꿈 없음, 다크 색상 정상 렌더링

### C. 기존 기능 회귀 (검색/필터/페이지네이션/행 클릭)
- **TC-KM-REG-001** · 키워드 검색 — @docs/01_analyze/feature/knowledge.md
  - 절차: 키워드 입력 후 검색 버튼 클릭
  - 기대 결과: 필터링된 결과 정상 반환, 컬럼 레이아웃 유지
- **TC-KM-REG-002** · 카테고리/상태 필터 — SCR-KM-001
  - 절차: 카테고리 셀렉트에서 특정 카테고리 선택 + 상태 셀렉트에서 특정 상태 선택 후 검색
  - 기대 결과: 필터 조건에 맞는 목록만 표시
- **TC-KM-REG-003** · 페이지네이션 — SCR-COM-007
  - 절차: 다음 페이지 이동
  - 기대 결과: 페이지 전환 정상 동작, 목록 데이터 갱신
- **TC-KM-REG-004** · 행 클릭 시 상세 이동 — SCR-KM-002
  - 절차: 목록의 임의 행 클릭
  - 기대 결과: `/knowledge/{id}` 상세 화면으로 정상 이동
