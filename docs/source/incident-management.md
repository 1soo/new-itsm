# 인시던트 관리 (Incident Management)

> **인시던트 관리는 개발·IT 운영 팀이 계획되지 않은 이벤트나 서비스 중단에 대응하여 서비스를 정상 상태로 신속히 복구하는 프로세스다.** 핵심 목표는 "장애를 없애는 것"이 아니라 "장애가 불가피함을 전제로, 빠르게 탐지·복구하고 재발을 막는 것"이다.

본 문서는 Atlassian Incident Management 마이크로사이트(핸드북·인시던트 대응·온콜·포스트모템·KPI·DevOps·ITSM 등 31개 페이지)를 분석하여 재구성한 종합 정리다.

---

## 📌 개요 (무엇을 & 왜)

### 인시던트란?

Atlassian은 인시던트를 **"서비스의 품질을 저하시키거나 중단시키는, 긴급 대응이 필요한 이벤트"** 로 정의한다. ITIL/ITSM을 따르는 팀은 이를 **주요 인시던트(major incident)** 라고 부르기도 한다.

- 애플리케이션이 다운되면 인시던트다.
- "죽지는 않았지만 기어가는" 느린 웹 서버도 인시던트다 (생산성 저하 + 완전 장애의 위험).
- 심각도는 전 세계 서비스 붕괴부터 소수 사용자의 간헐적 오류까지 매우 다양하다.
- **인시던트는 영향 복구에 필요한 작업이 끝나 서비스가 정상 상태로 돌아왔을 때 "해결(resolved)"된다.** 근본 원인 제거는 이후 포스트모템에서 다룬다.

### 왜 중요한가

> 서비스 중단은 비즈니스에 막대한 비용을 초래한다. 다운타임은 **분당 평균 $5,600~$9,000**, 시간당 평균 **$300,000**에 이르며, 북미에서만 연간 **$700 billion**의 손실을 유발한다. (예: Delta 항공 2017년 IT 장애로 약 $150M 손실)

인시던트 대응 시 팀에게 필요한 계획의 4가지 축:

| 축 | 설명 |
|---|---|
| **Respond (대응)** | 효과적으로 대응하여 빠르게 복구 |
| **Communicate (소통)** | 고객·이해관계자·서비스 오너에게 명확히 전달 |
| **Collaborate (협업)** | 팀으로 함께 문제를 더 빨리 해결하고 장애물 제거 |
| **Continuously Improve (지속 개선)** | 장애로부터 학습하여 서비스와 프로세스 개선 |

---

## 🧩 핵심 개념 및 용어

### 인시던트 vs 문제 vs 서비스 요청

| 구분 | 정의 | 성격 | 예시 |
|---|---|---|---|
| **인시던트 (Incident)** | 서비스 중단을 일으키는 단일·계획되지 않은 이벤트 | 실시간 소방(firefighting), 즉시 대응 | Delta 5시간 정전, Apple 앱스토어 12시간 다운 |
| **문제 (Problem)** | 하나 이상의 인시던트를 유발하는 "원인 또는 잠재적 원인" (ITIL 정의) | 예방·근본 원인 분석, 장기적 | 운영센터 정전 + 백업 부재, DNS 이슈 |
| **서비스 요청 (Service Request)** | 표준 IT 업무 (비상 아님) | 계획된 일상 업무 | 비밀번호 재설정, 소프트웨어 업데이트 요청 |

> 비유: 편두통으로 병원에 급히 가는 것 = **인시던트**, 편두통의 원인(알레르기·시력·스트레스) = **문제**. 하나의 문제가 여러 인시던트를 낳을 수 있고, 하나의 인시던트가 여러 문제에서 비롯될 수도 있다.

### 심각도(Severity) vs 우선순위(Priority)

- **심각도 = 영향(impact)의 측정.** 사용자에게 얼마나 큰 영향을 주는가?
- **우선순위 = 긴급성(urgency)의 측정.** 얼마나 빨리 고쳐야 하는가?

이 둘은 대개 일치하지만 항상 그런 것은 아니다.

| 사례 | 심각도 | 우선순위 |
|---|---|---|
| 홈페이지 헤드라인 오타 | 낮음 (기능 영향 없음) | 높을 수 있음 (브랜드 기준) |
| 앱 크래시지만 0.05% 사용자만 영향 | 높음 | 상대적으로 낮을 수 있음 |

> Atlassian은 **우선순위가 심각도보다 더 실행 가능(actionable)** 하므로 우선순위를 주 지표로 삼되, 심각도가 우선순위를 결정하는 핵심 요인이라고 본다.

### 인시던트 유형 (대응 대상)

- **보안 인시던트**: 사이버 공격, 데이터 유출, 무단 접근, 멀웨어
- **운영 인시던트**: 시스템 다운, 하드웨어 장애, 네트워크 중단
- **컴플라이언스 인시던트**: 규제·정책 위반
- **성능 인시던트**: 성능 저하, 느린 로딩, 연결 끊김
- **휴먼 에러 인시던트**: 잘못된 구성, 실수로 인한 삭제

---

## 🔄 인시던트 대응 라이프사이클

인시던트 대응은 **직선이 아니라 순환(cycle)** 이다. 서비스가 "정상"으로 돌아와도 팀의 일은 끝나지 않는다 — 포스트모템 결과가 로드맵·예방책으로 환류되는 끝없는 개선의 순환이다. 사이트에는 세 가지 관점이 병존한다.

### ① Atlassian 핸드북 프로세스 (실무 단계별)

| 단계 | 핵심 활동 |
|---|---|
| **Detect (탐지)** | 모니터링/알림·고객 신고·직접 관찰 → 인시던트 티켓(Jira 이슈) 생성. Summary, Description, Severity, Faulty service, Affected products 필드 기입 |
| **Raise (신규 인시던트 등록)** | 아직 IM 미배정 상태(new). Jira webhook이 major incident 생성 시 담당 IMOC(Incident Manager On Call)에게 page 알림 |
| **Open comms (소통 개시)** | IM이 이슈를 본인에게 배정하고 fixing 상태로 전환. 3대 채널 개설 — ①Slack 채팅룸(이슈키와 동일 이름, 예 HOT-1234) ②화상회의 ③Confluence "incident state document" |
| **Assess (평가)** | 고객 영향·규모·시작 시각·지원 케이스 수·기타 요인(보안/데이터 손실) 확인 → 심각도 확정 |
| **Send initial comms (초기 공지)** | Statuspage(내부/외부 분리) + 이메일로 신속 공지. 모든 내부 커뮤니케이션에 Jira 이슈키 포함 |
| **Escalate (에스컬레이션)** | OpsGenie 등 페이징 도구로 필요한 팀 호출. 온콜 로스터 기반 (특정 개인 "Bob 또 불러" 방식보다 우월) |
| **Delegate (역할 위임)** | IM이 Tech Lead·Communications Manager 등 역할 지정. 채팅룸 토픽에 역할 표시 |
| **Send followup comms (후속 공지)** | 내부: 현재 상태 요약 + Current Status + Next Steps + 다음 공지 시점. 외부: "짧고 명료하게" |
| **Review (검토·반복)** | 관찰 → 이론 수립 → 실험(검증) → 반복. IM은 팀 규율·피로도·핸드오버 관리. "당황하지 말 것" |
| **Resolve (해결)** | 현재·임박한 비즈니스 영향 종료 시 해결. 클린업 + 포스트모템으로 전환. TTR/TTD 계산 |

> **핵심 시간 지표**: Atlassian은 Jira 커스텀 필드로 start-of-impact / detection / end-of-impact 시각을 기록하여 **TTR(Time-to-Recovery)** 와 **TTD(Time-to-Detect)** 를 계산한다.

### ② NIST 인시던트 대응 라이프사이클 (4단계)

1. **Preparation (준비)** — 도구·자원 확보, 팀 훈련, 예방 활동
2. **Detection & Analysis (탐지·분석)** — NIST가 가장 어렵다고 보는 단계
3. **Containment, Eradication & Recovery (봉쇄·근절·복구)** — 영향 최소화
4. **Post-Event Activity (사후 활동)** — 학습·개선 (가장 자주 무시되는 단계)

### ③ 6단계 인시던트 대응 라이프사이클 (보안 지향)

Preparation → Identification → Containment → Eradication → Recovery → Lessons Learned. 각 단계는 사이버보안 인시던트 탐지·대응·복구를 위한 구조화된 접근을 제공한다.

> 세 관점 모두 공통 구성요소를 공유한다: **인시던트 정의·탐지 → 신속 대응·완화 → 분석·미래 개선.** 모두 비선형적이며 지속적 학습으로 순환을 닫는다.

---

## 👥 역할과 책임

역할을 개인이 아닌 **"역할(role)"** 로 정의하는 이유: 사람이 교체 가능(interchangeable)해지기 때문. 해당 역할 수행법만 알면 누구든 그 역할을 맡을 수 있다.

| 역할 | 주요 책임 | 별칭 |
|---|---|---|
| **Incident Manager / Commander (IM/IC)** | 인시던트 전체에 대한 최종 책임·권한. 자원·계획·소통 총괄. 조직 내 누구든 page 가능. 위임 전까지 모든 역할 겸임 | Incident Commander, Major Incident Manager |
| **Tech Lead** | 시니어 기술 책임자. 무엇이·왜 고장났는지 이론 개발, 변경 결정, 기술팀 운영. IM과 긴밀 협력 | On-call Engineer, SME |
| **Communications Manager** | 내부·외부 커뮤니케이션 작성·발송, Statuspage 업데이트 | Communications Officer/Lead |
| **Customer Support Lead** | 인입 티켓·전화·트윗에 대한 적시 대응 관리 | Help Desk Lead |
| **Subject Matter Expert (SME)** | 해당 시스템에 정통한 기술 대응자, 수정 제안·구현 | Technical Lead |
| **Social Media Lead** | 소셜 채널 대응, 실시간 고객 피드백 공유 | Social Media Manager |
| **Scribe (서기)** | 핵심 정보·타임라인 기록 유지 | — |
| **Problem Manager** | 해결을 넘어 근본 원인·재발 방지 변경 식별, 포스트모템 주관 | Root Cause Analyst |

### Incident Commander(IC)의 핵심 자질

IC의 3대 핵심 책임은 **자원 관리·커뮤니케이션·문제 해결**이다. 인턴부터 리더십까지 누구든 자질만 있으면 가능하다.

- 강한 커뮤니케이션·문제 해결 능력, 빠르고 확신 있는 의사결정, 경청·종합력, 리더십
- 주요 임무: 사전 준비, 의사결정, 위임, **큰 그림 감독**(개발자가 코드에 파묻힐 때 IC는 전체를 봄), 팀 정렬, **패닉 관리**(스트레스 받은 사람은 나쁜 결정을 내림 — 필요시 CEO/상사도 통화에서 배제), 에스컬레이션, 계획, 포스트모템
- 통상 다른 IC를 그림자처럼 따라다니며(shadow) 학습

---

## 📣 인시던트 커뮤니케이션 & 온콜/에스컬레이션

### 인시던트 커뮤니케이션

> **인시던트 커뮤니케이션 = 서비스 장애/성능 저하를 사용자에게 알리는 프로세스.** 다운타임은 불가피하므로 사전 준비가 핵심이다. "고객을 계속 루프 안에 두면(keep in the loop)" 부정적 반응이 크게 줄어든다.

**5대 커뮤니케이션 채널** (+ SMS): ①전용 Status Page ②임베디드 상태 위젯 ③이메일 ④업무용 채팅(Slack/Teams) ⑤소셜 미디어 ⑥SMS. 어느 하나가 만능이 아니며, **여러 채널을 겹쳐(layer) 하나의 주(primary) 채널로 유도**한다.

**5대 커뮤니케이션 대상 그룹**:

| 대상 | 특징 |
|---|---|
| Core on-call team | 가장 먼저 인지 (모니터링/알림) |
| Front-line support team | 고객에게 직접 답변 |
| Managers & executives | 영향·예상 기간 파악 필요 |
| General employees | 의존 서비스 상태 공유로 중복 티켓 방지 |
| External customers | **최소 1시간마다 업데이트**, 다음 업데이트 시점 명시. 보안·데이터 손실 시 legal/HR/security 즉시 투입 |

- **템플릿 사전 준비**: 인시던트 중 문구를 고민하지 않도록 승인된 공통 언어를 미리 저장.
- **3막 구조**: ①First contact(가장 중요, 톤 결정) ②규칙적 업데이트("새 소식 없음"도 침묵보다 낫다) ③해결·포스트모템.
- **Facebook 2010 사례**: 2.5시간 다운 후 395단어 요약 게시 — 문제 인정·공감·사과 → 원인 설명 → 수정·예방책 설명 → 재차 사과.

### 온콜(On-call)

> **온콜 = 정식 근무 시간이 아니어도 긴급 서비스 이슈 발생 시 대응하도록 특정 인원을 특정 시간에 대기시키는 관행.** 24/7 가용성을 요구하는 팀에 필수.

- **"You build it, you run it"**: DevOps에서 개발자가 자기 코드의 신뢰성·가용성 책임. 코드에 가장 익숙하므로 최단 시간 트러블슈팅 가능 + 더 나은 코드 작성 유인.
- **개발자 친화 온콜 5원칙**: ①책임 명확화 ②올바른 담당자에게 알림 배정 ③primary/secondary 대응자 확보 ④스케줄 지속 조정 ⑤진단 도구 접근·숙달.
- **온콜 보상 4모델**: ①Incentivized(자원자에게 휴가·유연근무·기본급) ②Paid for scheduled overtime(대기 자체 보상) ③Paid for time on issues(이슈 처리 시간만 보상 — 단, 알림 감소 유인이 약해질 위험) ④위 둘의 결합. FLSA에 따라 자유롭게 시간을 쓸 수 있으면 "대기 중"으로 비근무, 자유가 제약되면 "근무 시간"으로 간주될 수 있음.

### 에스컬레이션(Escalation)

> **에스컬레이션 = 직원이 스스로 인시던트를 해결할 수 없을 때 더 경험 많거나 전문화된 직원에게 넘기는 것.** 에스컬레이션 정책은 "누가·언제·어떻게" 인계할지를 규정.

**3가지 경로**:

| 유형 | 설명 |
|---|---|
| **Hierarchical (계층적)** | 경험·직급 기준으로 상위자에게 전달 (주니어 → 시니어) |
| **Functional (기능적)** | 직급이 아닌 기술·시스템 지식 기준으로 최적 담당자에게 전달 |
| **Automatic (자동)** | primary 담당자가 확인/종료하지 않으면 OpsGenie 등이 자동 에스컬레이션 |

- **에스컬레이션 매트릭스**: 언제·누가 각 레벨을 처리할지 정의하는 문서/시스템.
- **좋은 정책 원칙**: 규칙이 아닌 가이드라인으로 취급 / 온콜 스케줄 정기 감사 / 스마트한 임계값 설정(가용성 vs 번아웃 균형) / 명확한 프로세스.

### IT 알림(Alerting) 모범 사례

모니터링 도구가 IT 환경의 변화·고위험 행동·장애를 팀에 알림. **다운타임의 첫 방어선.**

- 모니터링 자동화 / 스마트 임계값(너무 낮으면 알림 폭주·피로, 너무 높으면 중대 이슈 누락) / **알림 중복 제거**(연구상 중복 알림마다 주의력 30% 하락) / 우선순위·심각도 설정 / **실행 가능한(actionable) 알림**(항공기 대시보드처럼 체크리스트 첨부) / 알림 강화(차트·로그·런북 첨부) / 다중 채널 / 알림 생명주기 추적 / **모니터링을 위한 모니터링**(OpsGenie의 Heartbeats).

---

## 🔍 포스트모템 (Blameless, 5 Whys)

### 포스트모템이란

> **포스트모템(사후 검토, post-incident review) = 인시던트의 영향·완화 조치·근본 원인·재발 방지 후속 조치를 기록한 문서.** 문제를 진보로 바꾸는 학습 프레임워크이자 고객·동료 신뢰 구축 수단.

Atlassian은 **severity 1·2 인시던트에 대해 blameless 포스트모템을 의무화**한다(그 외는 선택). 목표는 ①모든 기여 근본 원인 이해 ②미래 참조·패턴 발견을 위한 문서화 ③재발 가능성·영향을 줄이는 효과적 예방 조치 실행.

### 포스트모템 프로세스

1. 포스트모템 이슈 생성 후 인시던트에 연결
2. 필드 작성 (요약·Leadup·Fault·Impact·Detection·Response·Recovery·Timeline·5 Whys·Root cause·Backlog check·Recurrence·Lessons learned·Corrective actions)
3. **Five Whys**로 근본 원인까지 인과 사슬 추적
4. 포스트모템 회의 소집·진행
5. dev 매니저와 구체적 조치 약속
6. 각 조치를 Jira 백로그에 등록 → "Priority Action"(근본 원인 수정) 또는 "Improvement Action"(기타 개선)으로 연결
7. 승인자 지정 → 승인 요청 → 승인 후 Confluence 블로그로 공유

**포스트모템 회의 의제**: blameless 상기 → 타임라인 확인 → 근본 원인 확인 → "이 유형의 인시던트를 어떻게 예방할까?" 개방적 사고로 조치 생성 → **"무엇이 잘됐나 / 무엇이 더 나을 수 있었나 / 어디서 운이 좋았나"**.

### Proximate cause vs Root cause

- **Proximate cause(근접 원인)**: 이 인시던트로 직접 이어진 이유.
- **Root cause(근본 원인)**: 인과 사슬에서 변경 시 이 **유형 전체**를 예방할 수 있는 최적 지점. Five Whys로 사슬을 "거슬러 올라가" 찾음.

**근본 원인 카테고리**: Bug(코드 변경) / Change(코드 외 변경) / Scale(확장 실패) / Architecture(설계 부정합) / Dependency(3rd party 장애) / Unknown(관측성 개선 필요).

### 블레임리스(Blameless) 포스트모템

> 잘못을 저지른 사람을 찾는 것은 인간 본성이나 의식적으로 극복해야 한다. **"왜 개인 X가 이 일을 했는가"가 아니라 "왜 시스템이 그것을 허용했거나 옳다고 믿게 했는가"를 물어야 한다.**

- 비난은 ①진실 은폐 유발(개인의 지위/커리어 방어가 회사 이익보다 우선) ②불친절·공포·불신 문화 조성.
- **개인 안전 확보 기법**: 회의 시작 시 blameless 선언 / 이름 대신 역할로 지칭("온콜 Widgets 엔지니어") / 시스템·프로세스·역할 맥락으로 프레이밍.
- **성공 사례**: 한 엔지니어의 config 파일 문법 실수로 전사 45분 다운(수십만 달러 손실). 비난 대신 blameless 포스트모템 → "인간 오류를 어떻게 덜 가능하게 만들까?" → config 로딩 전 자동 "will it start" 체크 도입, 결국 사람 개입 제거. 해당 엔지니어는 지금도 재직 중.
- 존 올스파(John Allspaw)의 "blameless postmortems"·"second stories" 개념에서 영감.
- **문화 전제조건**: 개방적·실수 친화적 태도 사전 공지 / 정직·실패 수용 장려 / 타임라인 공유 / 일관되게 blameless / **C-suite 동의** / 협업(보안·법무·리스크 팀 초대) / 승인 절차.

### 5 Whys

1930년대 도요타 사키치 도요다가 창안. 문제를 식별하고 "왜?"를 반복(통상 5회)하여 진짜 근본 원인에 도달.

**주요 반론과 재반박**:

| 반론 | 재반박 |
|---|---|
| 비난 문화를 조장한다 | 핵심 규칙은 **"절대 사람을 근본 원인으로 지목하지 않기"**. "휴먼 에러"에서 멈추면 충분히 파고들지 않은 것 |
| 5는 너무 많다/적다 | 5는 문자 그대로가 아닌, 깨진 프로세스에 도달할 때까지 파는 과정의 약칭 |
| 근본 원인이 하나가 아니다 | 최종 답이 단일 원인일 필요 없음 — 여러 요인의 트리도 가능 |
| 총체적이지 않다 | 핵심은 늘 기술 결함/개인이 아닌 **깨진 프로세스** |
| 모르는 것을 알 수 없다 | 5 Whys는 **팀 노력** — 관련자 전원 참여 |

> 결론: "5 Whys가 죽었는가? 잘못 하고 있을 때만 그렇다."

### 포스트모템 실무 팁

임계값 설정(Sev-1 이상 트리거) / 미루지 말 것(24~48시간 내, 최대 5영업일) / 역할·오너 지정 / 템플릿 활용 / **타임라인 포함**(구체적 타임스탬프) / 디테일 / 메트릭 캡처(다운타임 분, 심각도, MTTR). **가장 중요한 팁: 어떤 단계도 건너뛰지 말 것.**

**조치 문구 작성 3원칙** (Google의 Lueder & Beyer): ①**Actionable**(동사로 시작, 결과 지향) ②**Specific**(범위 최대한 좁게) ③**Bounded**(완료 판단 기준 명시). 예: "모니터링 조사하기" ❌ → "이 서비스가 1% 초과 오류를 반환하는 모든 경우에 알림 추가하기" ✅

---

## 📊 주요 지표(KPI)

### KPI의 가치와 한계

KPI는 목표 달성 여부를 판단하는 지표(인시던트 수, 평균 해결 시간, 인시던트 간 평균 시간 등). **문제의 위치를 진단하는 출발점**이지 해결책 자체는 아니다.

> ⚠️ **경고**: 얕은 데이터에 과의존 금물. KPI는 "왜"를 설명하지 못한다. 존 올스파: *"인시던트는 통념보다 훨씬 고유하다. 같은 길이의 두 인시던트도 불확실성·위험이 극적으로 다르다. 인시던트는 제조되는 위젯이 아니다."*

### 공통 시간 지표

| 지표 | 정의 | 공식 |
|---|---|---|
| **MTBF** (Mean Time Between Failures) | 수리 가능 시스템의 고장 간 평균 시간 (가용성·신뢰성 추적) | 총 가동시간 ÷ 고장 횟수 |
| **MTTF** (Mean Time To Failure) | **수리 불가** 시스템의 고장까지 평균 수명 | 총 가동시간 ÷ 고장 횟수 (수리 불가 대상) |
| **MTTA** (Mean Time To Acknowledge) | 알림 발생 → 대응 착수까지 평균 시간 (응답성·알림 시스템 효과) | 확인까지 시간 ÷ 인시던트 수 |
| **MTTD** (Mean Time To Detect) | 이슈 발견까지 평균 시간 (보안에서 자주 사용) | — |
| **MTTR** (아래 4종) | R = Repair/Recovery/Respond/Resolve | 총 다운타임 ÷ 인시던트 수 |

**MTTR의 4가지 얼굴** (팀이 어느 것을 말하는지 반드시 합의):

- **Repair(수리)**: 실제 수리 시간 + 테스트 시간. 유지보수팀 효율 측정.
- **Recovery(복구)**: 장애 시작 → 완전 가동까지 전체 시간. **DORA의 핵심 DevOps 안정성 지표.**
- **Respond(응답)**: 알림 인지 시점부터 복구까지 (알림 지연 제외). 사이버보안에서 활용.
- **Resolve(해결)**: 탐지·진단·수리 + **재발 방지 조치까지** 포함. "불 끄기 + 집 방화 처리". 고객 만족과 강한 상관.

> **시스템 가용성 = MTBF ÷ (MTBF + MTTR).** 높은 MTBF + 낮은 MTTR = 더 나은 가동시간.
> - 좋은 MTBF 예시: SSD 200만 시간, 서버 약 15,000시간, 컨베이어 모터 4,000시간.
> - 좋은 MTTR: 제조 < 5시간, IT·보안은 거의 0(1시간 미만이면 우수).

기타 KPI: Alerts created, Incidents over time, On-call time, Uptime(업계 표준 99.9% 우수, 99.99% 탁월), Timestamps/Timeline.

### SLA vs SLO vs SLI

| 용어 | 정의 | 성격 | 예시 |
|---|---|---|---|
| **SLA** (Service Level Agreement) | 제공자–고객 간 측정 가능 지표 합의 (법무·사업개발 작성) | **외부 계약**, 위반 시 벌금·서비스 크레딧·라이선스 연장 | 99.95% 가동 보장 |
| **SLO** (Service Level Objective) | SLA를 충족하기 위한 **내부 목표치** (SLA보다 엄격) | 내부 목표, 조기 경보 | 30일간 99.99% 가동 |
| **SLI** (Service Level Indicator) | SLO 준수 여부의 **실제 측정값** | 실측 지표 | 실제 측정된 99.9% 가동 |

**SLO 3요소**: metric(측정 대상, 예 지연·다운타임) + target(목표 수치) + time window(측정 기간). 관계 흐름: 고객과 **SLA** 합의 → 이를 만족시킬 **SLO** 정의 → **SLI**(예: 지원 응답 시간)를 타깃.

### Error Budget (오류 예산)

> **오류 예산 = 계약적 불이익 없이 기술 시스템이 실패할 수 있는 최대 시간.** SLA는 절대 100% 가동을 약속해선 안 된다. 초과 달성분(예 99% 약속인데 99.5% 달성)은 팀이 **위험을 감수(혁신)** 하는 데 쓸 수 있는 예산이다.

| SLA 목표 | 연간 허용 다운타임 | 월간 허용 다운타임 |
|---|---|---|
| 99.99% | 52분 35초 | 4분 23초 |
| 99.95% | 4시간 22분 48초 | 21분 54초 |
| 99.9% | 8시간 45분 57초 | 43분 50초 |
| 99.5% | 43시간 49분 45초 | 3시간 39분 |
| 99% | 87시간 39분 | 7시간 18분 |

> 운영 원리: 오류 예산이 건강하면 개발팀은 무엇이든 언제든 출시 가능. 예산을 초과/소진하면 **오류를 줄일 때까지 모든 출시 동결**. 개발(혁신·민첩성)과 운영(안정성·보안)의 간극을 메운다.

### 심각도(Severity) 등급

Atlassian 3-tier 기준 (숫자가 낮을수록 심각):

| 심각도 | 설명 | 예시 | 대응 |
|---|---|---|---|
| **SEV 1** | 매우 높은 영향의 치명적 인시던트 | 전체 고객 대상 서비스(Jira Cloud) 다운, 기밀/개인정보 침해, 고객 데이터 손실 | **즉시 page**, 시간 불문 |
| **SEV 2** | 상당한 영향의 주요 인시던트 | 일부 고객 대상 서비스 다운, 핵심 기능(git push, 이슈 생성) 심각 저하 | **즉시 page** |
| **SEV 3** | 낮은 영향의 경미한 인시던트 | 워크어라운드 있는 사소한 불편, 성능 저하 | 업무 시간 중 처리 |

4-tier·5-tier로 확장 가능 (예: SEV 4 = 사용성에 영향 주는 경미 인시던트, SEV 5 = 사용성 무관 버그/로고 위치 오류). 심각도 정의 시 팀 규모·온콜 스케줄·트래픽 시간대·인시던트 빈도를 함께 고려.

---

## 🚨 주요 인시던트(Major Incident) & 재해 복구/IT 서비스 연속성(ITSCM)

### 주요 인시던트 관리

> **주요 인시던트 = 긴급 수준(emergency-level)의 장애/서비스 손실.** Atlassian에서는 SEV 1·2가 모두 major incident에 해당하며 즉시 대응 필요. SEV 3은 major incident가 아님.

프로세스는 핸드북과 동일: 탐지 → 신규 등록 → open comms → 평가 → 초기 공지 → 에스컬레이션 → 위임 → 후속 공지 → 검토 → 해결 → 포스트모템. 시작 전 반드시 정의할 것: 무엇이 major인가 / 심각도·우선순위 정의 / 담당·역할 / 프로세스 / 소통 계획 / 온콜 스케줄(새벽 2시·주말·연휴 담당?).

추가 역할: **Major Incident Investigation Board**(조사·변경 관리 담당 그룹).

### 재해 복구 (Disaster Recovery)

> **재해 복구 = 사이버 공격·하드웨어 장애·자연재해 등 파괴적 사건 후 IT 시스템·데이터·핵심 운영을 복원하는 계획·프로세스·기술의 집합.** 목표는 다운타임 감소·데이터 손실 제한. (업무 연속성 계획은 더 넓게 "중단 중·후에도 사업이 계속되는 법"을 다룸.)

**핵심 지표**: **RTO(Recovery Time Objective, 복구 목표 시간)**, **RPO(Recovery Point Objective, 복구 목표 시점=허용 데이터 손실량)**.

**7단계 프레임워크**: ①"재해" 정의 및 선언 주체 결정(RTO/RPO 기반 의사결정 트리) ②위협 식별 리스크 평가(가능성×영향 점수화) ③비즈니스 영향 분석(BIA, 시스템 티어링) ④복구 전략 선택 ⑤런북 문서화·중앙 저장 ⑥커뮤니케이션 워크플로 수립 ⑦테스트·측정·개선(분기 탁상훈련, 반기 부분 페일오버, 연간 전체 시뮬레이션).

**복구 전략**: Backup & Restore(저렴) vs Replication(비쌈, 낮은 RTO). 사이트 유형 — **Hot**(완전 복제, 최속·최고가) / **Warm**(사전 구성, 일부 수동) / **Cold**(최저가·최장 복구).

### IT 서비스 연속성 관리 (ITSCM)

> **ITSCM = ITIL 서비스 딜리버리의 핵심 구성요소.** 재해 수준 인시던트 전·중·후에 서비스 가용성·성능을 최고 수준으로 유지하기 위한 예방·예측·관리 계획. ITIL 4에서는 **업무 연속성 관리(BCM)** 를 지원하는 프로세스.

- **ITSCM vs 인시던트 관리**: 인시던트 관리는 다양한 영향 수준을 다루고, ITSCM은 **대규모 재해**를 계획. ("재해" = 조직이 사전 정의된 최소 기간 동안 핵심 기능을 제공하지 못하게 하는 갑작스러운 계획 외 사건 — Business Continuity Institute)
- **BCM과의 관계**: BCM은 IT 외부에서 관리되며 ITSCM을 포함. IT팀은 BCM팀과 협력해 **BCP(사업 연속성 계획)** 와 **BIA(사업 영향 분석)** 작성.
- **프로세스 (순환)**: 계획 → 명확한 책임 → 커뮤니케이션 → 테스트 → 평가·개선. Atlassian은 SRE·리스크/컴플라이언스 팀과 정기 재해 복구 회의 진행.
- **역할**: **Service Continuity Manager(SCM)** — 프로세스 A~Z 소유 / **Service Continuity Recovery Team** — 테스트·드릴·지속 개선.

---

## 🤝 DevOps/SRE/ChatOps 관점

### DevOps 인시던트 관리

> DevOps의 핵심은 사일로 해체·투명성·개발-운영 간 개방적 소통으로 비즈니스 가치 전달. ITIL(26개 프로세스의 규범적 집합)과 달리 **공식 문서가 없다.**

| | ITIL | DevOps |
|---|---|---|
| 성격 | 규범적, 서비스 품질·일관성 | 경량 프로세스, 민첩성 |
| 장점 | 템플릿화된 모범 사례로 시작 가능 | 즉시 효과, 개발팀 초기 투입 |
| 단점 | 공식 변경 관리·컨설턴트로 개선 지연 가능 | 팀별 신뢰성 관행 편차 |

**DevOps IM 3대 신념**: ①**온콜 순번제**(전원이 부담 공유) ②**만든 사람이 고치기 최적**(you build it, you run it) ③**속도로 개발하되 책임감**(장애 시 본인이 불려 나오므로 품질 코드 유인).

**DevOps 5단계**: Detection → Response(단일 온콜이 아닌 다수 대기, 런북 활용) → Resolution(코드 작성자라 빠름) → Analysis(blameless 포스트모템) → **Readiness**(런북·모니터링 업데이트, 사람·팀도 개선).

### SRE (Site Reliability Engineering)

> **SRE = 소프트웨어 엔지니어링 관행을 운영 업무에 적용해 신뢰성 있고 확장 가능한 시스템을 구축·유지하는 엔지니어링 분야.** Ben Treynor(Google): *"소프트웨어 엔지니어가 과거 운영이라 불리던 일을 맡을 때 벌어지는 일."*

| 접근 | 주 초점 | 팀 구조 | 한계 |
|---|---|---|---|
| 전통 IT 운영 | 릴리스 안정성·리스크 감소 | 기능별 전문팀 | 사일로·병목·느린 딜리버리 |
| DevOps | 자동화 통한 민첩성·효율 | 교차기능 협업 | 팀별 신뢰성 편차 |
| **SRE** | 엔지니어링·자동화·관측성 통한 신뢰성 | Dev-Ops를 잇는 엔지니어 | 기술 성숙도·명확한 지표·코딩 역량 필요 |

**4대 기둥(축)**: ①**Measurement**(SLI·SLO·SLA·Error Budget) ②**Response**(인시던트 대응·심각도·온콜) ③**Learning**(blameless 포스트모템·템플릿·지식 공유) ④**Improvement**(**Toil 감소**·자동화·용량 계획).

**팀 구조**: Centralized / Embedded(제품팀 내부, 빠른 대응) / **Hybrid**(둘의 절충, 권장). **리더십 지원**이 장기 성공의 핵심.

**도입 신호**: 반복 수작업으로 인한 번아웃 / 잦은 성능·다운타임 불만·SLA 위반 / 느린 배포·잦은 장애. → 저위험 파일럿으로 단계적 도입.

### ChatOps

> Sean Regan(Atlassian): *"ChatOps = 일하게 만든 대화(conversations put to work). 사람·도구·프로세스·자동화를 투명한 워크플로로 연결."*

인시던트 워크플로를 한곳에 모아 팀을 민첩하게 유지. **이점**: 모두 동일 정보 접근 / 실시간 대화 / 컨텍스트 스위칭 감소 / "말 전하기 게임" 제거 / **포스트모템용 내장 기록**(타임스탬프 완비).

**모범 사례**: 알림 시스템을 채팅에 연결 / 스마트 임계값(모든 알림이 아닌) / 주요 인시던트마다 별도 룸 / **채팅에서 액션 실행**(Slack+OpsGenie로 알림 배정·소유·음소거·생성) / 다수 팀 초대 / 보안 우선 / 채팅 기록 저장.

### 문제 관리 vs 인시던트 관리 (DevOps 관점의 변화)

전통 ITIL은 둘을 **별개로** 관리(인시던트 관리 = 실시간 대응, 문제 관리 = 예방). 이는 근본 원인 방치("불만 끄기")를 막는 장점이 있으나, 긴밀히 연결된 둘을 분리하면 지식 격차·소통 단절 위험. DevOps는 둘을 **하나의 총체적 관점의 겹치는 반쪽**으로 봄. **가교는 blameless 포스트모템** — 긴급성이 식으면 인시던트 매니저가 탐정으로 전환해 문제 관리·예방에 착수. 핵심 과제: 덜 긴급하지만 가치 있는 문제 관리가 인시던트의 즉각적 긴급성에 밀려 후순위화되지 않게 하는 것.

---

## 🛠️ 도구 & Jira Service Management 연계

### 인시던트 관리 도구 카테고리

> 인시던트 관리는 도구만으로 되지 않으며, **도구·관행·사람의 올바른 조합**이 필요하다.

| 카테고리 | 역할 | Atlassian 도구 |
|---|---|---|
| **Incident tracking** | 모든 인시던트 추적·문서화 (트렌드 분석) | Jira (커스터마이즈) |
| **Chat room** | 실시간 텍스트 협업 + 분석용 데이터 | Slack / Teams |
| **Video chat** | 빠른 공동 이해 형성 | (Blue Jeans 등) |
| **Alerting system** | 모니터링 연동·온콜 로테이션·에스컬레이션 | Opsgenie / JSM |
| **Documentation** | 인시던트 상태 문서·포스트모템 | Confluence |
| **Status page** | 내부·외부 상태 커뮤니케이션 | Statuspage |

### 보안 인시던트 대응 도구 (통합 시 효과 극대화)

**ASM**(공격 표면 관리), **EDR**(엔드포인트 탐지·대응), **SIEM**(보안 정보·이벤트 관리), **SOAR**(보안 오케스트레이션·자동화·대응), **XDR**(확장 탐지·대응). 사일로화된 시스템은 대시보드 전환·수동 이관을 강요 → 통합 시 응답 시간 단축·오류 감소.

### Jira Service Management(JSM)의 역할

JSM은 **인시던트 대응의 중앙 허브**로 반복 언급된다:
- 모든 모니터링·로깅·CI/CD 도구의 알림 중앙화·필터링 → 신속 swarm, 알림 피로 방지
- 심각도 할당 시 자동 액션·알림 트리거, 워크플로 안내
- 관련 티켓 그룹화·협업자 추가, 리치 타임라인 자동 기록
- 다중 커뮤니케이션 채널(위젯·Statuspage·이메일·채팅·소셜·SMS) 통합
- SLA/SLO 생성·자동 에스컬레이션으로 SLA 위반 방지
- 포스트모템 템플릿 → Confluence로 리포트·타임라인 내보내기
- KPI 리포팅·대시보드, IT 지원 레벨별 티켓 큐

### IT 지원 5레벨 (에스컬레이션 구조)

| 레벨 | 명칭 | 담당 |
|---|---|---|
| **0** | Self-service | FAQ·지식베이스·챗봇 (사용자 자가 해결, SEV 3급) |
| **1** | Basic help desk | 최소 경험 인력, 경미 문제 |
| **2** | Technical support | 원격 접근·전문성, SEV 1·2 이슈, 문서화 엄격 |
| **3** | Expert support | 사내 최고 티어, 심각/복잡 인시던트, 고급 학위·자격 |
| **4** | External support | 벤더·외부 전문가 (독점 하드웨어·보증 수리) |

> 많은 팀은 2~3개 티어로 충분. 티어링은 조직 부담을 늘리지만 사용자 경험 개선·응답 시간 단축의 이점.

---

## 🔗 문제 관리·변경 관리와의 관계

- **문제 관리(Problem Management)**: 인시던트의 근본 원인. 인시던트 관리(실시간 대응)와 상호 보완. 포스트모템이 인시던트 관리 → 문제 관리로 넘어가는 다리. 많은 엔터프라이즈는 별도 팀·프로세스로 운영하나, Atlassian은 IT Ops·개발팀이 문제 관리를 인시던트 관행에 **혼합(blended)** 하는 방식을 권장 (분석이 인시던트 직후 이뤄지도록).
- **변경 관리(Change Management)**: 근본 원인 카테고리 "Change"에서 드러나듯, 부주의한 변경이 인시던트의 흔한 원인. 조치로 "변경 방식(변경 리뷰·변경 관리 프로세스) 개선"이 처방됨. Major Incident Investigation Board가 변경 관리를 담당.
- **의존성(Dependency) 처리**: 내부 의존성은 **SLO** 기준으로 책임 판단(SLO 위반 → 의존성 책임 / SLO 준수했는데도 실패 → 자기 서비스 회복력 강화). 3rd party는 계약 SLA가 대개 무용(예: AWS EC2는 SLA 거의 없음)하므로 **"합리적 기대(reasonable expectation)"** 로 판단.
- **탈중앙화 흐름**: 과거엔 IT(NOC)가 전담했으나, 이제 DevOps·SecOps·아키텍처 팀이 참여. "you build it, you run it"으로 이동하되, 리더십을 위한 리포트·문서화 중앙화 필요 → 자율성과 중앙 가시성을 동시에 지원하는 기술이 답. 반응적(reactive) → 선제적(proactive) 전환, 지표도 MTT**Recovery** → MTT**Resolve**로.

---

## 📚 하위 주제 요약

| 주제 | 한 줄 요약 |
|---|---|
| **Handbook** | 20만+ 고객을 가진 Atlassian의 실제 인시던트 관리 프로세스. "고객이 알기 전에 우리가 안다" 등 5대 값 |
| **Incident Response** | 6단계 라이프사이클·명확한 역할·통합 도구(SIEM/EDR/SOAR/XDR)로 중단을 신속 대응 |
| **Lifecycle** | Atlassian 7단계 + NIST 4단계 — 모두 비선형 순환 |
| **Best Practices** | jump bag·runbook·Chaos Engineering·알림 집약·blameless 등 13가지 |
| **Incident Commander** | 자원·소통·문제해결 총괄, 패닉 관리, 큰 그림 감독 |
| **Roles & Responsibilities** | IM·Tech Lead·Comms Manager·Scribe·Problem Manager 등 (역할=교체 가능) |
| **Support Levels** | 0~4 (자가서비스→외부) 5단계 티어 |
| **Incident Communication** | 6채널·5대상 그룹, 템플릿 사전 준비, 3막 구조 |
| **On-call** | 대기 관행, you-build-it-you-run-it, 4가지 보상 모델 |
| **Escalation Policies** | 계층적·기능적·자동 3경로, 에스컬레이션 매트릭스 |
| **IT Alerting** | 다운타임 첫 방어선, 스마트 임계값·중복 제거·actionable |
| **Postmortem** | 학습·신뢰 구축 프레임워크, Sev-1↑ 트리거, 타임라인·메트릭 |
| **Blameless** | 시스템을 탓하고 사람을 탓하지 않음, C-suite 동의 필수 |
| **5 Whys** | 도요타 기원, 팀 노력, 사람 아닌 프로세스 지목 |
| **KPIs / Common Metrics** | MTBF·MTTR(4종)·MTTF·MTTA, 가용성=MTBF/(MTBF+MTTR) |
| **Severity Levels** | 영향의 측정(우선순위=긴급성과 구분), 3~5티어 |
| **SLA/SLO/SLI** | 외부 계약/내부 목표/실측, SLO가 SLA보다 엄격 |
| **Error Budget** | 계약 위반 없이 실패 가능한 시간 = 혁신 위험 예산 |
| **DevOps** | 사일로 해체·투명성·blameless, 5단계(+Readiness) |
| **SRE** | 4기둥(측정·대응·학습·개선), Toil 감소, 하이브리드 팀 |
| **ChatOps** | 대화를 일하게 — 컨텍스트 스위칭 제거·포스트모템 기록 |
| **Incident vs Problem** | 인시던트=증상 대응, 문제=근본 원인. DevOps는 통합 |
| **IT Incident Management (future)** | 탈중앙화·교차팀 협업, reactive→proactive, 문화가 핵심 |
| **Major Incident** | SEV 1·2 긴급 장애, 조사 보드, 즉시 대응 |
| **Disaster Recovery** | RTO/RPO, 7단계, Backup/Replication, Hot/Warm/Cold |
| **Crisis Management** | 사이버공격·정전·유출·버그·자연재해 대비 6단계 |
| **ITSCM** | ITIL 서비스 연속성, BCM 지원, SCM·복구팀 역할 |

---

## 🧭 요약 및 시사점

1. **장애는 불가피하다는 전제** — Atlassian·NIST·Google 모두 "if가 아니라 when"의 관점. 예방보다 **탐지·복구·학습의 순환**을 설계하는 것이 핵심.
2. **역할 기반 조직** — 개인이 아닌 역할(IM·Tech Lead·Comms 등)로 정의하여 대응자를 교체 가능하게. 특히 Incident Commander가 단일 진실 원천(single source of truth).
3. **커뮤니케이션이 신뢰다** — 초기 공지의 톤이 대응 전체 인식을 좌우. 침묵보다 "새 소식 없음"이 낫다. 내부/외부 채널 분리, 최소 1시간 간격.
4. **심각도·우선순위·SLA/SLO/SLI를 사전 정의** — 인시던트 발생 후 정의하느라 시간 낭비하지 말 것. Error Budget으로 신뢰성과 혁신의 균형.
5. **Blameless 문화가 학습의 전제** — "왜 시스템이 이를 허용했나"를 물어야 진실이 드러난다. C-suite 동의와 일관성이 필수.
6. **탈중앙화·통합의 시대** — IT 전담에서 DevOps·SRE·SecOps 공동 책임으로. "you build it, you run it". 자율성과 중앙 가시성을 동시에 제공하는 도구(JSM)가 이를 뒷받침.
7. **KPI는 출발점일 뿐** — 얕은 데이터 과의존 경계. 지표는 "어디를 더 파야 하는가"를 알려줄 뿐, "왜"에 대한 통찰(insight)은 사람의 몫.
8. **문제 관리와의 연결** — 빠른 복구(불 끄기)에 그치지 말고 근본 원인 제거(방화 처리)까지. MTTResolve 지표와 blameless 포스트모템이 그 다리.

> **한 문장 결론**: 성숙한 인시던트 관리는 "빠른 복구 + 명확한 소통 + 비난 없는 학습 + 지속 개선"의 순환을 조직 문화와 도구에 내재화하는 것이다.

---

## 📎 참고 출처 (전체 URL)

**CORE (핸드북·대응·온콜·포스트모템)**
- https://www.atlassian.com/incident-management
- https://www.atlassian.com/incident-management/handbook
- https://www.atlassian.com/incident-management/handbook/incident-response
- https://www.atlassian.com/incident-management/handbook/postmortems
- https://www.atlassian.com/incident-management/incident-response
- https://www.atlassian.com/incident-management/incident-response/lifecycle
- https://www.atlassian.com/incident-management/incident-response/best-practices
- https://www.atlassian.com/incident-management/incident-response/incident-commander
- https://www.atlassian.com/incident-management/incident-response/roles-responsibilities
- https://www.atlassian.com/incident-management/incident-response/support-levels
- https://www.atlassian.com/incident-management/incident-communication
- https://www.atlassian.com/incident-management/on-call
- https://www.atlassian.com/incident-management/on-call/escalation-policies
- https://www.atlassian.com/incident-management/on-call/it-alerting
- https://www.atlassian.com/incident-management/postmortem
- https://www.atlassian.com/incident-management/postmortem/blameless
- https://www.atlassian.com/incident-management/postmortem/5-whys

**SECTION (KPI·DevOps·ITSM·위기/재해)**
- https://www.atlassian.com/incident-management/kpis
- https://www.atlassian.com/incident-management/kpis/common-metrics
- https://www.atlassian.com/incident-management/kpis/severity-levels
- https://www.atlassian.com/incident-management/kpis/sla-vs-slo-vs-sli
- https://www.atlassian.com/incident-management/kpis/error-budget
- https://www.atlassian.com/incident-management/devops
- https://www.atlassian.com/incident-management/devops/sre
- https://www.atlassian.com/incident-management/devops/chatops
- https://www.atlassian.com/incident-management/devops/incident-vs-problem-management
- https://www.atlassian.com/incident-management/itsm/it-incident-management
- https://www.atlassian.com/incident-management/itsm/major-incident-management
- https://www.atlassian.com/incident-management/itsm/disaster-recovery
- https://www.atlassian.com/incident-management/crisis-management
- https://www.atlassian.com/itsm/incident-management/itscm
