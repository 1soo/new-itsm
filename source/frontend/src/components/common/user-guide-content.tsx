import ReactMarkdown, { type Components } from "react-markdown";
import { useTranslation } from "react-i18next";

import { StatusBadge } from "@/components/common/status-badge";
import {
  Accordion,
  AccordionContent,
  AccordionItem,
  AccordionTrigger,
} from "@/components/ui/accordion";
import { cn } from "@/lib/utils";

/**
 * 사용자 가이드 전용 화면(`/guide`, SCR-COM-012) 콘텐츠 섹션 — 프레젠테이션 전용.
 * `docs/01_analyze/feature/user-guide-content.md`(한국어)·`user-guide-content.en.md`(영어,
 * 2026-07-12 다국어 지원)를 가공 없이 그대로 옮긴 정적 데이터를 렌더링하며, `i18n.language`에
 * 따라 언어별 콘텐츠 세트를 선택한다. 페이지 레이아웃(문서 헤더·TOC·섹션 배치)은
 * FE(`features/guide/GuidePage.tsx`)가 담당한다.
 */
const markdownComponents: Components = {
  p: ({ ...props }) => <p className="mb-4 text-sm leading-relaxed text-foreground last:mb-0" {...props} />,
  strong: ({ ...props }) => <strong className="font-semibold text-foreground" {...props} />,
  ul: ({ ...props }) => <ul className="mb-4 list-disc space-y-1 pl-5 text-sm leading-relaxed text-foreground last:mb-0" {...props} />,
  li: ({ ...props }) => <li {...props} />,
};

function Markdown({ children, className }: { children: string; className?: string }) {
  return (
    <div className={cn(className)}>
      <ReactMarkdown components={markdownComponents}>{children}</ReactMarkdown>
    </div>
  );
}

const OVERVIEW_MARKDOWN_KO = `이 ITSM(IT Service Management) 플랫폼은 한 조직의 IT 운영과 사내 서비스 지원 업무 전체를 하나의 시스템 안에서 처리할 수 있도록 설계되었습니다.

전통적으로 IT 부서만 사용하던 서비스 데스크 기능(서비스 요청, 인시던트, 문제, 변경, 지식, 자산)에 더해, HR·법무·시설·재무 같은 IT 외 부서의 사내 서비스 요청까지 같은 방식(엔터프라이즈 서비스 관리, ESM)으로 처리할 수 있습니다. 여기에 더해 보안 취약점 관리, 규제·정책 준수(컴플라이언스) 관리, 인프라 가동률·용량 모니터링까지 하나의 플랫폼에서 다룹니다.

플랫폼을 사용하기 전에 알아두면 좋은 세 가지 원칙이 있습니다.

첫째, **모든 업무는 등록 → 처리 → 추적이라는 공통 흐름을 따릅니다.** 요청이든 인시던트든 변경이든, 먼저 등록되고 정의된 상태 값을 따라 전이되며, 그 과정은 타임라인과 감사 로그로 남습니다.

둘째, **접근 가능한 화면과 기능은 로그인한 사용자의 역할(Role)에 따라 달라집니다.** 이는 역할 기반 접근 제어(RBAC)라고 부르며, 사이드바 메뉴에는 본인의 역할이 접근할 수 있는 항목만 노출됩니다. 예를 들어 서비스 데스크 상담원에게는 "인시던트" 메뉴가 보이지만, 최종 사용자에게는 보이지 않습니다.

셋째, **헤더는 모든 화면에서 공통으로 사용하는 진입점입니다.** 헤더 중앙의 통합 검색으로 지식·서비스 요청·인시던트·문제·변경을 도메인을 넘나들며 한 번에 검색할 수 있고, 알림 벨을 클릭하면 여러 도메인에서 발생한 최근 알림(만료 임박, 임계치 초과 등)을 모아볼 수 있으며, 우측의 "?" 아이콘을 클릭하면 지금 보고 있는 이 사용자 가이드로 돌아올 수 있습니다.

아래 "2. 도메인 및 원칙"에서는 이 플랫폼이 다루는 11개 업무 영역이 각각 무엇을 위한 것이고 어떤 핵심 원칙으로 동작하는지 설명합니다. "3. 역할별 수행 내용과 방법"에서는 실제 인물을 예로 들어, 각 역할이 어떤 메뉴의 어떤 버튼을 눌러 무엇을 하는지 구체적으로 안내합니다. 본인의 역할에 해당하는 항목을 찾아 읽으면 실제 화면에서 바로 따라 할 수 있습니다.`;

const OVERVIEW_MARKDOWN_EN = `This ITSM (IT Service Management) platform is designed so that an organization's entire IT operations and internal service support work can be handled within a single system.

In addition to the service desk functions (service requests, incidents, problems, changes, knowledge, assets) traditionally used only by the IT department, it can also handle internal service requests from non-IT departments such as HR, Legal, Facilities, and Finance in the same way (Enterprise Service Management, ESM). On top of this, the platform also covers security vulnerability management, regulatory/policy compliance management, and infrastructure uptime/capacity monitoring — all within a single platform.

There are three principles worth knowing before using the platform.

First, **all work follows the common flow of register → process → track.** Whether it is a request, an incident, or a change, it is first registered and then transitions through defined status values, and that process is recorded in a timeline and audit log.

Second, **the screens and features you can access depend on the role of the logged-in user.** This is called role-based access control (RBAC), and the sidebar menu only shows items that your role can access. For example, a Service Desk Agent will see the "Incident" menu, but an End User will not.

Third, **the header is the common entry point shared by every screen.** The universal search in the center of the header lets you search across Knowledge, Service Requests, Incidents, Problems, and Changes at once, crossing domain boundaries. Clicking the notification bell lets you see recent notifications from multiple domains gathered in one place (expiry approaching, threshold exceeded, etc.), and clicking the "?" icon on the right takes you back to this very user guide you are reading now.

Section "2. Domains & Principles" below explains what each of the 11 business domains covered by this platform is for and what core principles it operates by. Section "3. Role-by-Role Guide" uses fictional personas as examples to concretely explain which button on which menu each role uses to do what. Find the item that matches your role and read it, and you can follow along directly on the actual screen.`;

export function UserGuideOverview({ className }: { className?: string }) {
  const { i18n } = useTranslation("common");
  const content = i18n.language === "en" ? OVERVIEW_MARKDOWN_EN : OVERVIEW_MARKDOWN_KO;
  return <Markdown className={className}>{content}</Markdown>;
}

interface GuideDomain {
  code: string;
  name: string;
  body: string;
}

const DOMAINS_KO: GuideDomain[] = [
  {
    code: "auth",
    name: "인증/계정/권한 (Auth & RBAC)",
    body: `모든 도메인의 공통 기반이 되는 영역입니다. 이메일·비밀번호로 로그인하면 시스템은 Access Token(단기)과 Refresh Token(장기)을 발급하고, Access Token이 만료되면 Refresh Token으로 자동 재발급을 시도합니다. 로그아웃하면 해당 세션의 토큰이 즉시 무효화되어 재사용할 수 없습니다.

**핵심 원칙**: 모든 화면 이동과 API 요청은 로그인 시 발급된 Access Token 안의 역할(role) 정보를 검사합니다. 접근 권한이 없는 화면이나 기능을 요청하면 시스템은 예외 없이 403(접근 거부) 응답을 반환합니다. 단, SYSTEM_ADMIN 역할만은 예외적으로 다른 모든 역할의 화면·기능에 항상 접근할 수 있습니다.`,
  },
  {
    code: "service-request",
    name: "서비스 요청 관리 (Service Request Management)",
    body: `직원이 접근 권한, 장비, 정보 등을 요청할 때 사용하는 셀프서비스 창구입니다. 요청은 **제출 → 검증 → 분류/라우팅 → 승인 → 이행 → 검증 → 종료**의 7단계를 거칩니다.

**핵심 원칙**: 요청 유형별로 SLA(응답 목표시간·해결 목표시간)가 설정되며, 시스템은 이 목표 대비 실제 처리 시간을 추적해 임박·위반 여부를 표시합니다. 승인이 필요하다고 정의된 요청 유형만 승인 단계를 거치며, 그렇지 않은 유형은 승인 없이 바로 이행됩니다.`,
  },
  {
    code: "incident",
    name: "인시던트 관리 (Incident Management)",
    body: `계획되지 않은 서비스 중단이나 성능 저하가 발생했을 때 이를 신속히 등록하고 대응·복구하는 영역입니다. 인시던트는 **신규 → 대응중 → 해결 → 종료**의 라이프사이클을 따릅니다.

**핵심 원칙**: 인시던트에는 심각도(SEV1~SEV3)가 부여되어 우선순위를 정하는 기준이 되며, Incident Manager가 Tech Lead·Communications Manager·Scribe 같은 대응 역할을 배정합니다. 해결 후에는 blameless(비난하지 않는) 원칙의 포스트모템을 작성해 근본 원인과 조치 항목을 남기고, 재발 방지를 위해 필요하면 문제(Problem)로 연계합니다.`,
  },
  {
    code: "problem",
    name: "문제 관리 (Problem Management)",
    body: `인시던트의 근본 원인을 찾아 재발을 막는 영역입니다. 문제는 **탐지 → 분류/우선순위 → 조사/진단 → 알려진 오류 기록 → 워크어라운드 → 해결/종료**의 6단계를 거칩니다.

**핵심 원칙**: 우선순위는 영향도 × 긴급도로 산정됩니다. 근본 원인이 아직 제거되지 않은 상태에서도 워크어라운드(임시 완화 조치)를 등록해 둘 수 있고, 근본 원인과 워크어라운드가 함께 기록되면 이는 "알려진 오류(Known Error)"로 KEDB(알려진 오류 데이터베이스)에 남아 향후 유사 문제 해결에 재사용됩니다.`,
  },
  {
    code: "change",
    name: "변경 관리 (Change Management)",
    body: `서비스에 영향을 주는 변경 작업(배포, 설정 변경 등)의 위험을 통제하는 영역입니다. 변경 요청(RFC)은 **요청 → 검토 → 계획 → 승인 → 구현 → 종료**의 6단계를 거칩니다.

**핵심 원칙**: 변경은 표준·일반·긴급 세 가지 유형으로 분류되며, 유형과 위험도에 따라 승인 경로가 달라집니다. 표준 변경(사전에 안전하다고 검증된 반복 작업)은 사전 승인되어 재승인 없이 진행할 수 있지만, 일반 변경은 위험도에 따라 동료 검토 또는 CAB(변경 자문 위원회) 승인을 거쳐야 하며, 승인이 완료되기 전에는 구현 단계로 전이할 수 없습니다.`,
  },
  {
    code: "knowledge",
    name: "지식 관리 (Knowledge Management)",
    body: `기술 정보와 해결 노하우를 문서화해 셀프서비스와 상담원 업무를 지원하는 영역입니다. 기사는 **초안 → 검토 → 게시**의 상태를 거칩니다.

**핵심 원칙**: 지식 기여자가 아무리 좋은 기사를 작성해도, 게이트키퍼(품질 검토자)의 검토·승인 없이는 게시 상태가 되지 않으며 최종 사용자에게 노출되지 않습니다. 이는 지식베이스의 정확성과 최신성을 지키는 품질 관문 역할을 합니다. 게시된 기사는 유용성 평가("도움이 되었나요?")를 받아 지속적으로 품질을 관리합니다.`,
  },
  {
    code: "asset",
    name: "IT 자산 관리 / CMDB (IT Asset Management)",
    body: `하드웨어·소프트웨어·클라우드 자산의 생애주기(계획 → 조달 → 운영 → 유지보수 → 폐기)를 관리하고, 구성 항목(CI) 간의 의존 관계를 CMDB로 관리하는 영역입니다.

**핵심 원칙**: 자산·CI는 서비스 요청·인시던트·문제·변경 티켓에 연결될 수 있으며, 이 연결 관계를 통해 특정 변경이 영향을 미치는 범위(다른 어떤 자산·서비스가 영향을 받는지)를 조회할 수 있습니다. 라이선스·보증·계약의 만료일이 임박하면 시스템이 자동으로 알림을 생성합니다.`,
  },
  {
    code: "esm",
    name: "엔터프라이즈 서비스 관리 (Enterprise Service Management, ESM)",
    body: `서비스 요청 관리와 동일한 원칙(카탈로그·워크플로우·SLA)을 HR·법무·시설·재무 등 IT 외 부서로 확장한 영역입니다. 신규 입사(온보딩)·퇴사(오프보딩) 처리처럼 여러 부서가 함께 움직여야 하는 업무는 체크리스트로 관리됩니다.

**핵심 원칙**: HR 케이스(민감한 인사 이슈)는 접수 → 기록 → 조사 → 해결의 정해진 순서로만 전이되며, **HR_CASE_MANAGER와 SYSTEM_ADMIN 두 역할만 접근할 수 있습니다.** 온보딩·오프보딩 요청이 제출되면 시스템이 체크리스트를 자동 생성하고 IT·시설·HR 등 관련 부서에 하위 작업을 자동으로 배정하며, 모든 하위 작업이 완료되어야 전체 체크리스트가 완료 상태가 됩니다.`,
  },
  {
    code: "vulnerability",
    name: "취약점 관리 (Vulnerability Management)",
    body: `보안 취약점을 발견부터 개선·검증·보고까지 체계적으로 추적하는 영역입니다. 취약점은 **발견 → 평가 → 우선순위화 → 개선 → 검증 → 보고**의 6단계를 거칩니다.

**핵심 원칙**: 취약점의 리스크 스코어는 심각도와 악용 가능성을 함께 고려해 산정되며, 이 점수를 기준으로 처리 우선순위를 정합니다. **담당자(소유자)가 배정되지 않은 상태에서는 개선(Remediation) 단계로 전이할 수 없습니다** — 이는 취약점이 방치되지 않도록 책임 소재를 명확히 하기 위한 통제입니다.`,
  },
  {
    code: "compliance",
    name: "컴플라이언스 관리 (Compliance Management)",
    body: `규제 의무나 사내 정책을 요구사항으로 등록하고 준수 여부를 관리하는 영역입니다.

**핵심 원칙**: 모든 컴플라이언스 요구사항에는 반드시 책임자(오너)가 지정되어야 하며, 책임자가 없는 요구사항은 목록·상세 화면에서 "책임자 미지정"으로 강조 표시됩니다. 요구사항 위반이나 이슈는 시정조치 항목으로 등록해 탐지 → 조치중 → 해결 상태로 추적하며, 요구사항 등록·상태 변경·조치 처리 등 관련 활동은 모두 감사 로그로 남아 추후 감사에 대비합니다.`,
  },
  {
    code: "infra-monitoring",
    name: "인프라 모니터링 & 용량관리 (IT Infrastructure Monitoring & Capacity Management)",
    body: `서버·네트워크 장비 같은 인프라 자산의 가동률·성능 지표를 수동으로 기록하고, 팀 단위의 용량(처리 역량 대비 수요)을 계획하는 영역입니다.

**핵심 원칙**: 지표 항목(가동률·CPU·메모리·응답시간)마다 임계치(상한/하한)를 설정할 수 있으며, 등록된 지표 값이 임계치를 벗어나면 시스템이 즉시 알림을 생성합니다. 가동률에는 SLA 목표치를 설정할 수 있어, 실제 가동률이 목표를 달성했는지 비교해 볼 수 있습니다.`,
  },
];

const DOMAINS_EN: GuideDomain[] = [
  {
    code: "auth",
    name: "Auth & RBAC",
    body: `This is the common foundation area for all domains. When you log in with your email and password, the system issues an Access Token (short-lived) and a Refresh Token (long-lived); when the Access Token expires, it automatically attempts to reissue it using the Refresh Token. When you log out, the tokens for that session are immediately invalidated and can no longer be reused.

**Core principle**: Every screen navigation and API request checks the role information contained in the Access Token issued at login. If a screen or feature is requested without access permission, the system returns a 403 (access denied) response without exception. However, only the SYSTEM_ADMIN role is exceptionally allowed to always access the screens and features of every other role.`,
  },
  {
    code: "service-request",
    name: "Service Request Management",
    body: `This is the self-service window employees use to request access permissions, equipment, information, and more. A request goes through 7 stages: **Submit → Validate → Classify/Route → Approve → Fulfill → Verify → Close**.

**Core principle**: An SLA (response target time, resolution target time) is set per request type, and the system tracks actual processing time against these targets to indicate whether they are approaching or in breach. Only request types defined as requiring approval go through an approval stage; other types are fulfilled immediately without approval.`,
  },
  {
    code: "incident",
    name: "Incident Management",
    body: `This is the area for quickly registering and responding to/recovering from unplanned service outages or performance degradation. An incident follows the lifecycle: **New → In Progress → Resolved → Closed**.

**Core principle**: A severity (SEV1–SEV3) is assigned to an incident as the basis for prioritization, and the Incident Manager assigns response roles such as Tech Lead, Communications Manager, and Scribe. After resolution, a blameless postmortem is written to record the root cause and action items, and if needed to prevent recurrence, it is linked to a Problem.`,
  },
  {
    code: "problem",
    name: "Problem Management",
    body: `This is the area for finding the root cause of incidents to prevent recurrence. A problem goes through 6 stages: **Detect → Classify/Prioritize → Investigate/Diagnose → Record Known Error → Workaround → Resolve/Close**.

**Core principle**: Priority is calculated as impact × urgency. A workaround (temporary mitigation) can be registered even while the root cause has not yet been removed, and once both the root cause and workaround are documented together, it is recorded as a "Known Error" in the KEDB (Known Error Database) for reuse in resolving similar problems in the future.`,
  },
  {
    code: "change",
    name: "Change Management",
    body: `This is the area for controlling the risk of change work (deployments, configuration changes, etc.) that affects services. A change request (RFC) goes through 6 stages: **Request → Review → Plan → Approve → Implement → Close**.

**Core principle**: Changes are classified into three types — standard, normal, and emergency — and the approval path differs depending on the type and risk level. A standard change (a repeatable task pre-verified as safe) is pre-approved and can proceed without re-approval, but a normal change must go through peer review or CAB (Change Advisory Board) approval depending on risk level, and cannot transition to the implementation stage before approval is complete.`,
  },
  {
    code: "knowledge",
    name: "Knowledge Management",
    body: `This is the area for documenting technical information and resolution know-how to support self-service and agent work. An article goes through the states: **Draft → Review → Published**.

**Core principle**: No matter how good an article a knowledge contributor writes, it does not become published and is not exposed to end users without review and approval by a gatekeeper (quality reviewer). This serves as a quality gate that maintains the accuracy and freshness of the knowledge base. Published articles receive helpfulness ratings ("Was this helpful?") for continuous quality management.`,
  },
  {
    code: "asset",
    name: "IT Asset Management / CMDB",
    body: `This is the area for managing the lifecycle (Plan → Procure → Operate → Maintain → Retire) of hardware, software, and cloud assets, and for managing dependency relationships between configuration items (CIs) via the CMDB.

**Core principle**: Assets/CIs can be linked to service request, incident, problem, and change tickets, and through this linkage you can look up the scope affected by a specific change (which other assets/services are affected). When a license, warranty, or contract expiry date approaches, the system automatically generates a notification.`,
  },
  {
    code: "esm",
    name: "Enterprise Service Management (ESM)",
    body: `This is the area that extends the same principles as Service Request Management (catalog, workflow, SLA) to non-IT departments such as HR, Legal, Facilities, and Finance. Work that requires multiple departments to move together, such as onboarding (new hire) and offboarding (departure) processing, is managed via checklists.

**Core principle**: An HR case (a sensitive personnel issue) can only transition in the fixed order Intake → Record → Investigate → Resolve, and **only the HR_CASE_MANAGER and SYSTEM_ADMIN roles can access it.** When an onboarding/offboarding request is submitted, the system automatically generates a checklist and automatically assigns subtasks to relevant departments such as IT, Facilities, and HR, and the overall checklist is completed only once all subtasks are completed.`,
  },
  {
    code: "vulnerability",
    name: "Vulnerability Management",
    body: `This is the area for systematically tracking security vulnerabilities from discovery through remediation, verification, and reporting. A vulnerability goes through 6 stages: **Discover → Assess → Prioritize → Remediate → Verify → Report**.

**Core principle**: A vulnerability's risk score is calculated considering both severity and exploitability, and this score determines processing priority. **It cannot transition to the Remediation stage while no assignee (owner) has been assigned** — this is a control to ensure clear accountability so that vulnerabilities are not left unaddressed.`,
  },
  {
    code: "compliance",
    name: "Compliance Management",
    body: `This is the area for registering regulatory obligations or internal policies as requirements and managing compliance.

**Core principle**: Every compliance requirement must have an owner assigned, and requirements without an owner are highlighted as "Owner not assigned" in the list/detail screens. Violations or issues of a requirement are registered as corrective action items and tracked through Detect → In Progress → Resolved states, and all related activity — requirement registration, status changes, action processing, etc. — is recorded in the audit log to prepare for future audits.`,
  },
  {
    code: "infra-monitoring",
    name: "IT Infrastructure Monitoring & Capacity Management",
    body: `This is the area for manually recording the uptime/performance metrics of infrastructure assets such as servers and network equipment, and for planning team-level capacity (demand versus processing capability).

**Core principle**: A threshold (upper/lower bound) can be set for each metric item (uptime, CPU, memory, response time), and when a registered metric value falls outside the threshold, the system immediately generates a notification. An SLA target can be set for uptime, allowing you to compare actual uptime against the target.`,
  },
];

export function UserGuideDomainSection({ className }: { className?: string }) {
  const { i18n } = useTranslation("common");
  const domains = i18n.language === "en" ? DOMAINS_EN : DOMAINS_KO;
  return (
    <Accordion type="multiple" className={cn("space-y-2", className)}>
      {domains.map((d) => (
        <AccordionItem key={d.code} value={d.code}>
          <AccordionTrigger>{d.name}</AccordionTrigger>
          <AccordionContent>
            <Markdown>{d.body}</Markdown>
          </AccordionContent>
        </AccordionItem>
      ))}
    </Accordion>
  );
}

interface GuideRole {
  code: string;
  name: string;
  body: string;
}

const ROLES_KO: GuideRole[] = [
  {
    code: "SYSTEM_ADMIN",
    name: "SYSTEM_ADMIN — 시스템 관리자",
    body: `**페르소나**: 박서준 팀장, IT 인프라팀 소속 시스템 관리자. 전사 계정과 권한 체계를 관리하고, 보안 사고 발생 시 감사 로그를 근거로 조사한다.

박서준 팀장은 로그인 후 사이드바 **"관리자"** 메뉴 그룹에서 업무를 시작한다.

- 신규 입사자의 계정을 만들 때는 **"계정 관리"**(계정 목록 화면) 메뉴로 들어가 우측 상단의 **"계정 생성"** 버튼을 클릭한다. 이메일·이름·초기 역할(하나 이상 선택)·초기 비밀번호를 입력하고 **"저장"** 버튼을 누르면 계정이 만들어지고, 신규 입사자는 최초 로그인 후 비밀번호를 직접 변경한다.
- 퇴사자나 휴직자의 계정을 막아야 할 때는 계정 목록에서 대상 행을 클릭해 상세 화면으로 들어간 뒤, **"비활성화"** 버튼을 클릭한다. 확인 다이얼로그가 뜨면 다시 한번 확인해 처리하며, 처리 즉시 해당 계정은 로그인이 차단된다.
- 역할이 바뀐 직원(예: 상담원에서 인시던트 관리자로 보직 변경)에게는 같은 계정 상세 화면의 역할 부여/회수 패널에서 새 역할을 추가하거나 기존 역할을 회수(칩 제거)한다.
- 조직 개편으로 새로운 역할이 필요해지면 **"역할 관리"** 메뉴에서 **"역할 생성"** 버튼을 눌러 역할명·설명을 입력해 새 역할을 정의한다.
- 보안 점검이나 이상 로그인이 의심될 때는 **"감사 로그"** 메뉴에서 이벤트 유형(로그인/로그아웃/재발급/계정·역할 변경) 필터로 좁혀 기간별 이력을 조회한다.

박서준 팀장은 이 역할 고유의 화면 외에도, 다른 모든 역할에게 정의된 화면과 기능에 예외적으로 접근할 수 있다. 즉 필요하다면 인시던트 상세, HR 케이스, 컴플라이언스 요구사항 등 어떤 도메인 화면이든 열람·조작할 수 있다.`,
  },
  {
    code: "END_USER",
    name: "END_USER — 최종 사용자 / 요청자",
    body: `**페르소나**: 이하은 사원, 마케팅팀 신입사원. 노트북 지급이나 사내 정책 관련 정보가 필요할 때 직접 시스템에 접속해 요청을 넣는다.

이하은 사원은 새 모니터가 필요할 때 사이드바 **"서비스 요청"** 그룹의 **"서비스 포털"** 메뉴로 들어간다. 카테고리 카드 중 원하는 요청 유형(예: "장비 지급 요청") 카드를 클릭하면 동적 양식 화면으로 이동하고, 필요한 항목을 입력한 뒤 **"제출"** 버튼을 클릭한다. 제출 화면 우측에는 관련 지식 기사가 추천되어, 굳이 요청을 넣지 않고도 스스로 해결할 수 있는 경우가 안내된다.

제출 후에는 같은 그룹의 **"요청 목록"** 메뉴에서 본인이 낸 요청의 처리 상태(제출/승인대기/이행중/종료)와 SLA 상태를 확인할 수 있다. 요청이 종료되면 상세 화면에 CSAT(만족도) 위젯이 나타나 별점과 코멘트를 남길 수 있다.

인사 관련 요청(예: 재직증명서 발급)이 필요할 때는 **"부서 서비스"** 그룹의 **"부서 서비스 포털"** 메뉴에서 HR 탭을 선택해 동일한 방식으로 요청을 제출하고, **"내 부서 요청"** 메뉴에서 진행 상황을 추적한다.

궁금한 점이 생기면 먼저 **"지식"** 그룹의 **"지식베이스"** 메뉴에서 키워드로 검색해 게시된 기사를 열람하고, 기사 하단의 "도움이 되었나요?" 위젯으로 평가를 남길 수 있다.`,
  },
  {
    code: "SERVICE_DESK_AGENT",
    name: "SERVICE_DESK_AGENT — 서비스 데스크 상담원",
    body: `**페르소나**: 김민수 대리, IT 서비스 데스크 1차 대응 담당자. 하루 종일 큐에 들어오는 요청과 장애 신고를 처리한다.

김민수 대리는 근무를 시작하면 사이드바 **"서비스 요청"** 그룹의 **"요청 큐"** 메뉴를 연다. 좌측 큐 목록에서 담당 큐를 확인하고, 처리할 요청 행에서 **"배정"** 버튼을 클릭해 본인에게 배정한다. 배정된 요청은 상세 화면으로 들어가 코멘트로 요청자와 소통하고, 처리가 끝나면 상태 전이 버튼(예: "이행완료")을 눌러 상태를 갱신한다.

전산 장애 신고 전화를 받으면 **"인시던트"** 그룹의 **"인시던트"** 메뉴에서 **"인시던트 등록"** 버튼을 눌러 요약·심각도·영향 서비스를 입력해 인시던트를 만든다. 이후 처리 경과를 상태 업데이트로 남기고, 본인 선에서 해결이 어려운 사안은 상세 화면의 **"에스컬레이션"** 버튼으로 상위 담당자에게 이관한다.

응대 중 자주 묻는 질문이 나오면 **"지식"** 그룹의 **"지식베이스"** 메뉴에서 관련 기사를 검색해 답변에 활용한다.`,
  },
  {
    code: "APPROVER",
    name: "APPROVER — 승인자 (CAB 멤버 포함)",
    body: `**페르소나**: 정우진 부장, 재무팀 예산 승인권자이자 변경관리위원회(CAB) 멤버. 고가 장비 구매 요청과 시스템 변경 요청을 승인한다.

정우진 부장은 서비스 요청 승인이 필요할 때 **"서비스 요청"** 그룹의 **"승인 대기함"** 메뉴로 들어가, 대기 목록에서 항목을 클릭해 요청 내용을 확인한 뒤 **"승인"** 또는 **"반려"** 버튼을 클릭한다. 반려할 때는 사유 입력이 필수다.

변경 요청 승인은 **"변경"** 그룹의 **"CAB 승인 대기함"** 메뉴에서 처리한다. 위험도가 높은 변경일수록 신중히 검토하며, 마찬가지로 **"승인"**/**"반려"** 버튼과 의견 입력으로 결정을 남긴다.`,
  },
  {
    code: "PROCESS_OWNER",
    name: "PROCESS_OWNER — 프로세스 오너",
    body: `**페르소나**: 한지수 매니저, 서비스 운영 프로세스를 표준화하는 담당자. 새로운 요청 유형을 만들고 SLA 목표를 관리한다.

한지수 매니저는 신입사원 온보딩 시즌마다 요청 유형을 정비하기 위해 **"서비스 요청"** 그룹의 **"서비스 카탈로그"** 메뉴로 들어가 카탈로그 항목 목록에서 새 항목을 추가하거나 기존 항목의 양식 필드·승인 필요 여부·담당 큐·SLA 목표(응답/해결 시간)를 편집한다.

부서 서비스 쪽 카탈로그가 필요하면 **"부서 서비스"** 그룹의 **"부서별 카탈로그 관리"** 메뉴에서 담당 부서(HR/법무/시설/재무)를 지정하고 양식 필드와 (온보딩/오프보딩이라면) 체크리스트 템플릿까지 함께 정의한다.

분기마다 **"요청 지표"**와 **"ESM 지표"** 메뉴에서 CSAT·응답/해결 시간·SLA 준수율·부서별 처리량을 확인해 카탈로그 개선에 반영한다.`,
  },
  {
    code: "INCIDENT_MANAGER",
    name: "INCIDENT_MANAGER — 인시던트 관리자",
    body: `**페르소나**: 최도윤 차장, 인시던트 대응을 총괄하는 IT 운영 매니저. 장애 발생 시 대응팀을 조직하고 재발 방지를 책임진다.

최도윤 차장은 심각한 장애가 접수되면 **"인시던트"** 그룹의 **"인시던트"** 메뉴에서 해당 건의 상세 화면으로 들어가, 심각도/우선순위 편집 영역에서 심각도를 재확인하고 역할 배정 패널에서 Tech Lead·Communications Manager·Scribe를 배정한다. 대응이 길어지면 **"에스컬레이션"** 버튼으로 상위 조직에 이관하고, 상태 업데이트 입력창에 내부/외부 구분을 지정해 진행 상황을 공지한다.

장애가 복구되면 해결 처리 영역에서 영향 시작·탐지·영향 종료 시각을 입력해 MTTD/MTTA/MTTR 지표를 산출하고, SEV1·SEV2 인시던트라면 **"포스트모템 편집"** 화면으로 이동해 5 Whys와 근본 원인, 조치 항목을 작성한 뒤 **"제출"** 버튼으로 저장한다. 재발 방지가 필요하면 상세 화면의 **"문제 연계"** 버튼으로 문제(Problem)를 새로 만들거나 기존 문제에 연결한다.

**"인시던트 지표"** 메뉴에서는 기간별 건수·심각도 분포·평균 MTTR을 확인해 팀 성과를 점검한다.`,
  },
  {
    code: "PROBLEM_MANAGER",
    name: "PROBLEM_MANAGER — 문제 관리자",
    body: `**페르소나**: 윤서연 과장, 반복되는 장애의 근본 원인을 추적하는 문제 관리 담당자.

윤서연 과장은 같은 장애가 두 번 이상 반복되면 **"문제"** 그룹의 **"문제"** 메뉴에서 문제를 등록하고, 상세 화면의 RCA 섹션에서 근본 원인과 5 Whys 분석을 기록한다. 원인 제거에 시간이 걸리면 워크어라운드 입력란에 임시 대응책을 남기고, 근본 원인과 워크어라운드가 모두 정리되면 **"알려진 오류 생성"** 버튼을 눌러 KEDB에 등록한다.

관련된 과거 인시던트는 **"인시던트 연결"** 버튼으로 양방향 연결하고, 근본적인 해결을 위해 시스템 변경이 필요하면 **"변경 연계"** 버튼으로 신규 또는 기존 변경 요청과 연결한다. 후속 조치 리스트에서 담당자·기한을 등록해 진행 상태를 관리하다가, 문제가 완전히 제거되면 **"종료"** 버튼을 눌러 마무리한다.

비슷한 증상의 문제가 재발했는지 확인할 때는 **"KEDB 검색"** 메뉴에서 키워드로 과거 알려진 오류를 조회한다.`,
  },
  {
    code: "CHANGE_MANAGER",
    name: "CHANGE_MANAGER — 변경 관리자",
    body: `**페르소나**: 강태민 매니저, 시스템 변경 프로세스를 총괄하는 변경 관리자. 승인 결정 자체는 APPROVER/CAB 권한이며, 강태민 매니저는 프로세스와 일정을 조율한다.

강태민 매니저는 배포·설정 변경이 필요하다는 요청을 받으면 **"변경"** 그룹의 **"변경"** 메뉴에서 변경 요청(RFC)을 생성하고, 변경 유형(표준/일반/긴급)과 위험도·예상 구현·영향 시스템·롤백 방법을 입력한다. 유형과 위험도에 따라 승인 경로(자동승인/동료검토/CAB)가 자동으로 결정된다.

승인이 완료되면 상세 화면에서 프로세스 상태를 "구현"으로 전이하고(승인 전에는 이 전이 자체가 차단된다), 구현이 끝나면 구현 결과 기록 영역에서 성공/실패 여부와 롤백 실행 여부를 남긴다. 관련 인시던트나 문제가 있으면 인시던트/문제 연계 버튼으로 연결한다.

전사 배포 일정을 조율할 때는 **"변경 일정"** 메뉴의 캘린더에서 예정된 변경들을 한눈에 확인하고, **"변경 지표"** 메뉴에서 변경 성공률·실패율·긴급 변경 비율을 모니터링해 프로세스 개선에 활용한다.`,
  },
  {
    code: "KNOWLEDGE_CONTRIBUTOR",
    name: "KNOWLEDGE_CONTRIBUTOR — 지식 기여자",
    body: `**페르소나**: 오세훈 사원, 반복적으로 발생하는 문의에 대한 해결 노하우를 문서화하는 담당자.

오세훈 사원은 같은 문의가 반복된다고 느끼면 **"지식"** 그룹의 **"기사 작성"** 메뉴로 들어가 제목·본문을 작성하고 카테고리·라벨을 지정한다. 초안을 완성하면 **"검토 요청"** 버튼을 눌러 게이트키퍼에게 검토를 요청한다(이 시점에 상태가 초안에서 검토로 바뀐다). 더 이상 유효하지 않은 기사는 **"삭제"** 버튼으로 제거한다.

또한 인시던트나 서비스 요청을 처리하는 도중에도 그 자리에서 새 기사를 작성하거나 기존 기사를 연결(KCS)해, 같은 문제를 겪는 다음 사용자가 빠르게 해결할 수 있도록 돕는다.`,
  },
  {
    code: "KNOWLEDGE_GATEKEEPER",
    name: "KNOWLEDGE_GATEKEEPER — 지식 게이트키퍼",
    body: `**페르소나**: 서지훈 선임, 지식베이스의 품질을 관리하는 검토 담당자.

서지훈 선임은 **"지식"** 그룹의 **"검토·게시 승인함"** 메뉴에서 검토 대기 중인 기사 목록을 확인하고, 항목을 클릭해 내용을 미리 보고 정확성과 최신성을 판단한다. 게시해도 좋다고 판단하면 **"승인"** 버튼을 눌러 기사를 게시 상태로 전환하고, 보완이 필요하면 **"반려"** 버튼을 눌러 사유를 입력한다(반려된 기사는 다시 초안 상태로 돌아가 기여자가 수정할 수 있다).

**"지식 지표"** 메뉴에서는 사용량·무결과 검색·유용성 평점·티켓 차단율을 확인해, 어떤 주제의 기사가 더 필요한지(무결과 검색 키워드 랭킹) 파악한다.`,
  },
  {
    code: "ASSET_MANAGER",
    name: "ASSET_MANAGER — 자산 관리자",
    body: `**페르소나**: 임하윤 주임, 회사의 모든 IT 장비와 소프트웨어 라이선스를 관리하는 자산 담당자.

임하윤 주임은 새 노트북이 입고되면 **"자산"** 그룹의 **"자산"** 메뉴에서 **"자산 등록"** 버튼을 눌러 유형(HW/SW/클라우드)을 선택하고 소유자·위치·구매일·비용·만료일 등을 입력한다. 자산의 상태가 바뀌면(예: 배치 완료, 유지보수 중) 상세 화면의 생애주기 단계 전이 버튼으로 갱신하고, 수명이 다한 장비는 **"폐기"** 버튼으로 처리한다(확인 다이얼로그 필요).

서버 간의 의존 관계를 파악해야 할 때는 **"CI·CMDB 관계"** 메뉴에서 CI를 등록하고 관계 추가 폼으로 의존 관계를 연결하며, 특정 변경이 어떤 자산에 영향을 주는지 확인할 때는 같은 화면의 영향 범위 패널을 활용한다. 장비가 특정 인시던트나 변경과 관련 있으면 자산 상세 화면의 **"티켓 연계"** 버튼으로 연결한다.

라이선스·보증 만료가 임박한 자산은 **"자산 지표"** 메뉴의 대시보드에서 확인해 갱신 여부를 결정한다.`,
  },
  {
    code: "HR_CASE_MANAGER",
    name: "HR_CASE_MANAGER — HR 케이스 담당자",
    body: `**페르소나**: 남궁예은 대리, 인사팀에서 민감한 인사 이슈(고충 상담, 징계 등)를 처리하는 담당자.

남궁예은 대리는 직원의 고충 신고가 접수되면 사이드바 **"HR 케이스"** 그룹의 **"HR 케이스"** 메뉴로 들어가 **"케이스 접수"** 버튼을 눌러 케이스를 생성한다. 이후 상세 화면에서 정해진 순서(접수 → 기록 → 조사 → 해결)에 따라 상태 전이 버튼을 눌러 진행 단계를 옮기며, 그 이력은 상태 이력 타임라인에 남는다.

이 메뉴와 화면은 민감 정보 보호를 위해 남궁예은 대리(HR_CASE_MANAGER)와 SYSTEM_ADMIN 외의 사용자에게는 사이드바에 아예 노출되지 않으며, 직접 접근을 시도해도 403으로 차단된다.`,
  },
  {
    code: "DEPT_COORDINATOR",
    name: "DEPT_COORDINATOR — 부서 처리 담당자",
    body: `**페르소나**: 배지호 사원, 시설팀에서 부서로 들어오는 요청과 온보딩/오프보딩 하위 작업을 처리하는 담당자.

배지호 사원은 사이드바 **"부서 서비스"** 그룹의 **"부서 요청 처리 큐"** 메뉴에서 본인 소속 부서(시설팀)로 들어온 요청만 필터링되어 보이는 목록을 확인하고, 항목을 클릭해 상세 화면에서 코멘트로 요청자와 소통한 뒤 상태 전이 버튼(처리중/완료/반려)으로 진행 상태를 갱신한다.

신규 입사자 온보딩이 진행 중이면 **"내 하위 작업"** 메뉴에서 본인 부서에 배정된 하위 작업(예: 좌석 배치, 출입증 발급)을 확인하고, 처리가 끝나면 **"완료 처리"** 버튼을 클릭한다. 모든 부서의 하위 작업이 완료되면 전체 체크리스트가 자동으로 완료 상태로 바뀐다.`,
  },
  {
    code: "VULNERABILITY_MANAGER",
    name: "VULNERABILITY_MANAGER — 취약점 관리 담당자",
    body: `**페르소나**: 홍유진 선임, 보안팀에서 발견된 취약점의 개선을 조율하는 담당자.

홍유진 선임은 보안 점검에서 취약점이 발견되면 **"취약점"** 그룹의 **"취약점"** 메뉴에서 제목·발견일·영향 자산/CI·심각도를 입력해 등록한다. 상세 화면에서는 리스크 스코어 산정 폼으로 심각도와 악용 가능성을 입력해 점수를 계산하고, 담당자 배정 영역에서 처리할 담당자를 지정한다(담당자가 지정되지 않으면 개선 단계로 넘어갈 수 없다).

담당자가 조치를 완료하면 개선 조치 등록 폼에 조치 유형(패치/구성 변경/보완 통제)과 조치일을 기록하고, 검증이 끝나면 검증 결과 등록 버튼으로 통과/실패를 기록한다(통과 시 자동으로 보고 상태로 넘어가고, 실패하면 다시 개선 단계로 돌아간다). 관련 자산이 있으면 자산/CI 연계 버튼으로 연결한다.

**"취약점 지표"** 메뉴에서는 단계별 건수·심각도 분포·평균 해결 시간을 확인해 팀의 대응 속도를 점검한다.`,
  },
  {
    code: "COMPLIANCE_OFFICER",
    name: "COMPLIANCE_OFFICER — 컴플라이언스 담당자",
    body: `**페르소나**: 문가영 부장, 법무팀에서 규제·정책 준수를 관리하는 컴플라이언스 담당자.

문가영 부장은 새로운 규제가 시행되면 **"컴플라이언스"** 그룹의 **"컴플라이언스 요구사항"** 메뉴에서 이름·근거(규제 조항 또는 내부 정책)·적용 범위를 입력해 요구사항을 등록한다. 상세 화면에서 책임자 지정 영역으로 해당 요구사항의 책임자(오너)를 지정하며, 책임자가 없으면 목록·상세에 "책임자 미지정"으로 강조 표시된다.

위반 사례나 이슈가 발견되면 시정조치 등록 폼에 내용을 입력해 항목을 만들고(자동으로 "탐지" 상태로 생성됨), 조치가 진행되는 동안 시정조치 상태 전이 버튼으로 탐지 → 조치중 → 해결 순서로 갱신한다. 관련된 시스템 변경이 있으면 변경 연계 버튼으로 연결하고, 요구사항과 관련된 모든 활동은 감사 로그 목록에서 확인할 수 있다.

**"준수 현황"** 메뉴의 대시보드에서는 전체 준수율과 미해결 시정조치 건수를 한눈에 확인해 경영진 보고에 활용한다.`,
  },
  {
    code: "INFRA_OPERATOR",
    name: "INFRA_OPERATOR — 인프라 운영 담당자",
    body: `**페르소나**: 조은우 사원, 서버·네트워크 장비의 가동 상태를 모니터링하는 인프라 운영 담당자.

조은우 사원은 매일 정기 점검 시간마다 **"인프라 모니터링"** 그룹의 **"지표 등록"** 메뉴에서 대상 자산을 선택하고 측정 시각과 지표 항목(가동률/CPU/메모리/응답시간)·값을 입력해 **"등록"** 버튼을 클릭한다. 값이 임계치를 벗어나면 등록 즉시 "임계치 초과 알림이 생성되었습니다" 토스트가 뜬다.

지표 추이가 궁금할 때는 **"지표 대시보드"** 메뉴에서 자산과 기간을 선택해 시계열 차트를 확인하고, SLA 대비 가동률 카드로 목표 대비 실제 가동률을 비교한다. 새로운 장비를 들여와 임계치를 처음 설정할 때는 **"임계치·알림"** 메뉴에서 지표 항목별 상한/하한을 입력하고, 발생한 초과 알림은 목록에서 확인한 뒤 **"확인 처리"** 버튼으로 처리 완료를 표시한다.

분기별로 팀 용량을 계획할 때는 **"용량 계획"** 메뉴에서 팀/서비스명·역량·예상 수요를 등록해 활용률을 파악하고, 경영 보고가 필요하면 **"인프라 리포팅"** 메뉴에서 기간별 평균 가동률·성능 지표·용량 활용률을 집계 조회한다.`,
  },
];

const ROLES_EN: GuideRole[] = [
  {
    code: "SYSTEM_ADMIN",
    name: "SYSTEM_ADMIN — System Administrator",
    body: `**Persona**: Team Lead Park Seo-jun, a system administrator in the IT Infrastructure team. Manages company-wide accounts and permission structures, and investigates security incidents based on audit logs.

Team Lead Park Seo-jun starts work in the sidebar's **"Administrator"** menu group after logging in.

- To create an account for a new hire, he goes into the **"Account Management"** menu (account list screen) and clicks the **"Create Account"** button in the upper right. He enters the email, name, initial role(s) (one or more), and initial password, then clicks the **"Save"** button to create the account; the new hire changes their password themselves after first login.
- When an account for a departing or leave-of-absence employee needs to be blocked, he clicks the target row in the account list to go to the detail screen, then clicks the **"Deactivate"** button. A confirmation dialog appears for a final confirmation before processing, and the account is immediately blocked from logging in once processed.
- For an employee whose role has changed (e.g., moved from Agent to Incident Manager), he adds a new role or revokes an existing role (removes the chip) in the role grant/revoke panel on the same account detail screen.
- When a reorganization requires a new role, he clicks the **"Create Role"** button on the **"Role Management"** menu and enters a role name and description to define the new role.
- During a security check or when a suspicious login is detected, he narrows down the period-based history on the **"Audit Log"** menu using the event type filter (login/logout/reissue/account & role change).

Beyond the screens unique to this role, Team Lead Park Seo-jun can also exceptionally access the screens and features defined for every other role. That is, if needed, he can view and operate any domain screen — incident details, HR cases, compliance requirements, and so on.`,
  },
  {
    code: "END_USER",
    name: "END_USER — End User / Requester",
    body: `**Persona**: Staff member Lee Ha-eun, a new hire in the Marketing team. Logs into the system directly to submit a request whenever she needs a laptop issued or information about a company policy.

When Staff member Lee Ha-eun needs a new monitor, she goes into the **"Service Portal"** menu in the sidebar's **"Service Requests"** group. Clicking the category card for the request type she wants (e.g., "Equipment Issuance Request") takes her to a dynamic form screen; she fills in the required fields and clicks the **"Submit"** button. Related knowledge articles are recommended on the right side of the submission screen, so cases she can resolve on her own without even submitting a request are surfaced.

After submitting, she can check the processing status of her own requests (submitted/pending approval/in fulfillment/closed) and SLA status in the **"My Requests"** menu in the same group. Once a request is closed, a CSAT (satisfaction) widget appears on the detail screen where she can leave a star rating and comment.

When she needs an HR-related request (e.g., issuing an employment certificate), she selects the HR tab in the **"Department Service Portal"** menu of the **"Department Services"** group and submits a request the same way, then tracks progress in the **"My Department Requests"** menu.

When she has a question, she first searches by keyword in the **"Knowledge Base"** menu of the **"Knowledge"** group to view published articles, and can leave a rating using the "Was this helpful?" widget at the bottom of an article.`,
  },
  {
    code: "SERVICE_DESK_AGENT",
    name: "SERVICE_DESK_AGENT — Service Desk Agent",
    body: `**Persona**: Assistant Manager Kim Min-su, a first-line responder on the IT service desk. Handles requests and incident reports coming into the queue all day long.

When Assistant Manager Kim Min-su starts his shift, he opens the **"Request Queue"** menu in the sidebar's **"Service Requests"** group. He checks his assigned queue in the queue list on the left, and clicks the **"Assign"** button on a request row to assign it to himself. He goes into the detail screen of an assigned request to communicate with the requester via comments, and once processing is done, clicks a status transition button (e.g., "Fulfilled") to update the status.

When he receives a call reporting a system failure, he clicks the **"Register Incident"** button on the **"Incident"** menu of the **"Incident"** group and enters a summary, severity, and affected service to create an incident. He then leaves progress updates as status updates, and for issues that are difficult to resolve himself, he hands off to a senior handler using the **"Escalate"** button on the detail screen.

When a frequently asked question comes up during a response, he searches the **"Knowledge Base"** menu in the **"Knowledge"** group for a relevant article to use in his answer.`,
  },
  {
    code: "APPROVER",
    name: "APPROVER — Approver (including CAB Members)",
    body: `**Persona**: General Manager Jeong Woo-jin, a budget approver in the Finance team and a Change Advisory Board (CAB) member. Approves high-value equipment purchase requests and system change requests.

When General Manager Jeong Woo-jin needs to approve a service request, he goes into the **"Approval Inbox"** menu of the **"Service Requests"** group, clicks an item in the pending list to review the request content, then clicks the **"Approve"** or **"Reject"** button. When rejecting, entering a reason is mandatory.

He processes change request approvals in the **"CAB Approval Inbox"** menu of the **"Change"** group. The higher the risk of a change, the more carefully he reviews it, and likewise he records his decision using the **"Approve"**/**"Reject"** buttons along with an opinion entry.`,
  },
  {
    code: "PROCESS_OWNER",
    name: "PROCESS_OWNER — Process Owner",
    body: `**Persona**: Manager Han Ji-su, in charge of standardizing service operation processes. Creates new request types and manages SLA targets.

Every new-hire onboarding season, Manager Han Ji-su goes into the **"Service Catalog"** menu of the **"Service Requests"** group to organize request types, adding new items to the catalog item list or editing existing items' form fields, whether approval is required, the assigned queue, and SLA targets (response/resolution time).

When a catalog for department services is needed, in the **"Department Catalog Management"** menu of the **"Department Services"** group, she designates the responsible department (HR/Legal/Facilities/Finance) and defines the form fields and, for onboarding/offboarding, the checklist template as well.

Every quarter, she checks CSAT, response/resolution time, SLA compliance rate, and per-department volume in the **"Request Metrics"** and **"ESM Metrics"** menus and reflects them in catalog improvements.`,
  },
  {
    code: "INCIDENT_MANAGER",
    name: "INCIDENT_MANAGER — Incident Manager",
    body: `**Persona**: Deputy General Manager Choi Do-yun, an IT operations manager who oversees incident response. Organizes the response team and is responsible for preventing recurrence when a failure occurs.

When a serious failure is received, Deputy General Manager Choi Do-yun goes into the detail screen of that item in the **"Incident"** menu of the **"Incident"** group, reconfirms the severity in the severity/priority edit area, and assigns Tech Lead, Communications Manager, and Scribe in the role assignment panel. If the response drags on, he hands it off to a higher organization using the **"Escalate"** button, and specifies internal/external in the status update input box to announce progress.

Once the failure is recovered, he enters the impact start, detection, and impact end times in the resolution area to calculate MTTD/MTTA/MTTR metrics, and for SEV1/SEV2 incidents, goes to the **"Edit Postmortem"** screen to write the 5 Whys, root cause, and action items, then saves with the **"Submit"** button. If recurrence prevention is needed, he uses the **"Link Problem"** button on the detail screen to create a new Problem or link to an existing one.

In the **"Incident Metrics"** menu, he checks period-based counts, severity distribution, and average MTTR to review team performance.`,
  },
  {
    code: "PROBLEM_MANAGER",
    name: "PROBLEM_MANAGER — Problem Manager",
    body: `**Persona**: Section Chief Yoon Seo-yeon, in charge of Problem Management, tracking the root causes of recurring failures.

When the same failure occurs two or more times, Section Chief Yoon Seo-yeon registers a problem in the **"Problem"** menu of the **"Problem"** group, and records the root cause and 5 Whys analysis in the RCA section of the detail screen. If removing the cause takes time, she leaves a temporary countermeasure in the workaround input field, and once both the root cause and workaround are organized, she clicks the **"Create Known Error"** button to register it in the KEDB.

She bidirectionally links related past incidents using the **"Link Incident"** button, and if a system change is needed for a fundamental fix, links it to a new or existing change request using the **"Link Change"** button. She registers assignees and due dates in the follow-up action list to manage progress, and once the problem is completely removed, clicks the **"Close"** button to finish.

When checking whether a problem with similar symptoms has recurred, she looks up past known errors by keyword in the **"KEDB Search"** menu.`,
  },
  {
    code: "CHANGE_MANAGER",
    name: "CHANGE_MANAGER — Change Manager",
    body: `**Persona**: Manager Kang Tae-min, overseeing the system change process. The approval decision itself is an APPROVER/CAB authority; Manager Kang Tae-min coordinates the process and schedule.

When Manager Kang Tae-min receives a request that a deployment or configuration change is needed, he creates a change request (RFC) in the **"Change"** menu of the **"Change"** group, and enters the change type (standard/normal/emergency), risk level, expected implementation, affected systems, and rollback method. The approval path (auto-approved/peer review/CAB) is automatically determined based on the type and risk level.

Once approval is complete, he transitions the process status to "Implementation" on the detail screen (this transition itself is blocked before approval), and once implementation is done, records success/failure and whether a rollback was executed in the implementation result record area. If there are related incidents or problems, he links them using the incident/problem linking buttons.

When coordinating a company-wide deployment schedule, he checks upcoming changes at a glance on the calendar in the **"Change Schedule"** menu, and monitors change success rate, failure rate, and emergency change ratio in the **"Change Metrics"** menu to use in process improvement.`,
  },
  {
    code: "KNOWLEDGE_CONTRIBUTOR",
    name: "KNOWLEDGE_CONTRIBUTOR — Knowledge Contributor",
    body: `**Persona**: Staff member Oh Se-hun, who documents resolution know-how for recurring inquiries.

When Staff member Oh Se-hun feels the same inquiry keeps recurring, he goes into the **"Write Article"** menu of the **"Knowledge"** group to write a title and body and assign a category and labels. Once the draft is complete, he clicks the **"Request Review"** button to ask a gatekeeper for review (at this point, the status changes from draft to review). Articles that are no longer valid are removed with the **"Delete"** button.

He also writes new articles on the spot, or links existing articles (KCS), while handling an incident or service request, so that the next user facing the same issue can resolve it quickly.`,
  },
  {
    code: "KNOWLEDGE_GATEKEEPER",
    name: "KNOWLEDGE_GATEKEEPER — Knowledge Gatekeeper",
    body: `**Persona**: Senior Associate Seo Ji-hun, who manages the quality of the knowledge base as a review lead.

Senior Associate Seo Ji-hun checks the list of articles pending review in the **"Review & Publish Inbox"** menu of the **"Knowledge"** group, clicks an item to preview the content and judge its accuracy and freshness. If he judges it fine to publish, he clicks the **"Approve"** button to transition the article to published status; if it needs improvement, he clicks the **"Reject"** button and enters a reason (a rejected article returns to draft status so the contributor can revise it).

In the **"Knowledge Metrics"** menu, he checks usage, zero-result searches, helpfulness ratings, and ticket deflection rate to identify (via the zero-result search keyword ranking) which topics need more articles.`,
  },
  {
    code: "ASSET_MANAGER",
    name: "ASSET_MANAGER — Asset Manager",
    body: `**Persona**: Associate Im Ha-yun, who manages all of the company's IT equipment and software licenses.

When a new laptop arrives, Associate Im Ha-yun clicks the **"Register Asset"** button in the **"Asset"** menu of the **"Asset"** group, selects the type (HW/SW/Cloud), and enters the owner, location, purchase date, cost, expiry date, and so on. When an asset's status changes (e.g., deployed, under maintenance), she updates it using the lifecycle stage transition button on the detail screen, and equipment that has reached end of life is processed with the **"Retire"** button (requires a confirmation dialog).

When dependency relationships between servers need to be understood, she registers CIs in the **"CI/CMDB Relationships"** menu and links dependency relationships via the relationship-add form, and when checking which assets a specific change affects, she uses the impact scope panel on the same screen. If equipment is related to a specific incident or change, she links it using the **"Link Ticket"** button on the asset detail screen.

Assets with license/warranty expiry approaching are checked in the dashboard of the **"Asset Metrics"** menu to decide whether to renew.`,
  },
  {
    code: "HR_CASE_MANAGER",
    name: "HR_CASE_MANAGER — HR Case Manager",
    body: `**Persona**: Assistant Manager Namgung Ye-eun, who handles sensitive personnel issues (grievances, disciplinary actions, etc.) in the HR team.

When an employee grievance report comes in, Assistant Manager Namgung Ye-eun goes into the **"HR Cases"** menu of the sidebar's **"HR Cases"** group and clicks the **"Intake Case"** button to create a case. She then clicks status transition buttons on the detail screen to move through the fixed order (Intake → Record → Investigate → Resolve), and that history is left in the status history timeline.

To protect sensitive information, this menu and screen are not exposed at all in the sidebar to anyone other than Assistant Manager Namgung Ye-eun (HR_CASE_MANAGER) and SYSTEM_ADMIN, and even a direct access attempt is blocked with a 403.`,
  },
  {
    code: "DEPT_COORDINATOR",
    name: "DEPT_COORDINATOR — Department Coordinator",
    body: `**Persona**: Staff member Bae Ji-ho, in the Facilities team, who handles requests coming into the department and onboarding/offboarding subtasks.

Staff member Bae Ji-ho checks a list filtered to show only requests that came into his own department (Facilities) in the **"Department Request Queue"** menu of the sidebar's **"Department Services"** group, clicks an item to go to the detail screen, communicates with the requester via comments, and updates the progress status using the status transition button (in progress/completed/rejected).

When new-hire onboarding is underway, he checks the subtasks assigned to his department (e.g., seat assignment, badge issuance) in the **"My Subtasks"** menu, and once processing is done, clicks the **"Mark Complete"** button. Once every department's subtasks are completed, the overall checklist automatically changes to completed status.`,
  },
  {
    code: "VULNERABILITY_MANAGER",
    name: "VULNERABILITY_MANAGER — Vulnerability Manager",
    body: `**Persona**: Senior Associate Hong Yu-jin, who coordinates remediation of vulnerabilities discovered by the security team.

When a vulnerability is found in a security check, Senior Associate Hong Yu-jin registers it in the **"Vulnerability"** menu of the **"Vulnerability"** group, entering a title, discovery date, affected asset/CI, and severity. On the detail screen, she uses the risk score calculation form to enter severity and exploitability to compute a score, and designates an assignee to handle it in the assignment area (it cannot move to the remediation stage until an assignee is designated).

Once the assignee completes the action, she records the action type (patch/configuration change/compensating control) and action date in the remediation registration form, and once verification is done, records pass/fail using the verification result registration button (on pass it automatically moves to the reporting stage; on fail it returns to the remediation stage). If there is a related asset, she links it using the asset/CI link button.

In the **"Vulnerability Metrics"** menu, she checks counts by stage, severity distribution, and average resolution time to review the team's response speed.`,
  },
  {
    code: "COMPLIANCE_OFFICER",
    name: "COMPLIANCE_OFFICER — Compliance Officer",
    body: `**Persona**: General Manager Moon Ga-young, in the Legal team, who manages regulatory/policy compliance.

When a new regulation takes effect, General Manager Moon Ga-young registers a requirement in the **"Compliance Requirements"** menu of the **"Compliance"** group, entering a name, basis (regulatory clause or internal policy), and scope of application. On the detail screen, she designates the owner of the requirement in the owner designation area; if there is no owner, it is highlighted as "Owner not assigned" in the list/detail screens.

When a violation case or issue is found, she creates an item by entering details in the corrective action registration form (automatically created in "Detected" status), and while the action is in progress, updates it in the order Detect → In Progress → Resolved using the corrective action status transition button. If there is a related system change, she links it using the change link button, and all activity related to a requirement can be checked in the audit log list.

In the dashboard of the **"Compliance Status"** menu, she checks the overall compliance rate and the number of unresolved corrective actions at a glance to use in management reporting.`,
  },
  {
    code: "INFRA_OPERATOR",
    name: "INFRA_OPERATOR — Infrastructure Operator",
    body: `**Persona**: Staff member Jo Eun-woo, who monitors the operational status of servers and network equipment.

At every regular inspection time each day, Staff member Jo Eun-woo selects the target asset in the **"Register Metric"** menu of the **"Infra Monitoring"** group, enters the measurement time and the metric item (uptime/CPU/memory/response time) and value, and clicks the **"Register"** button. If the value falls outside the threshold, a "Threshold exceeded alert has been generated" toast appears immediately upon registration.

When he wants to see metric trends, he selects the asset and period in the **"Metric Dashboard"** menu to check the time-series chart, and compares actual versus target uptime with the SLA-vs-uptime card. When bringing in new equipment and setting a threshold for the first time, he enters the upper/lower bound per metric item in the **"Threshold & Alerts"** menu, and checks generated threshold-exceeded alerts in the list, then marks them as processed with the **"Acknowledge"** button.

For quarterly team capacity planning, he registers the team/service name, capacity, and expected demand in the **"Capacity Planning"** menu to understand utilization, and when management reporting is needed, aggregates average uptime, performance metrics, and capacity utilization by period in the **"Infra Reporting"** menu.`,
  },
];

export interface UserGuideRoleSectionProps {
  /** 로그인 사용자의 보유 역할 코드(`roles.ts`의 `ROLE_*` 값). 상단 "내 역할" 고정 노출에 사용. */
  myRoles?: string[];
  className?: string;
}

export function UserGuideRoleSection({ myRoles = [], className }: UserGuideRoleSectionProps) {
  const { t, i18n } = useTranslation("common");
  const roles = i18n.language === "en" ? ROLES_EN : ROLES_KO;
  const myRoleSet = new Set(myRoles);
  const mine = roles.filter((r) => myRoleSet.has(r.code));
  const others = roles.filter((r) => !myRoleSet.has(r.code));
  const ordered = [...mine, ...others];

  return (
    <Accordion
      type="multiple"
      defaultValue={mine.map((r) => r.code)}
      className={cn("space-y-2", className)}
    >
      {ordered.map((r) => (
        <AccordionItem key={r.code} value={r.code}>
          <AccordionTrigger>
            <span className="flex items-center gap-2">
              {r.name}
              {myRoleSet.has(r.code) ? (
                <StatusBadge tone="info" label={t("userGuide.myRoleBadge")} />
              ) : null}
            </span>
          </AccordionTrigger>
          <AccordionContent>
            <Markdown>{r.body}</Markdown>
          </AccordionContent>
        </AccordionItem>
      ))}
    </Accordion>
  );
}
