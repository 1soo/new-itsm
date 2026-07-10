import { useMemo } from "react";

import { Modal } from "@/components/common/modal";
import { StatusBadge } from "@/components/common/status-badge";
import {
  Accordion,
  AccordionContent,
  AccordionItem,
  AccordionTrigger,
} from "@/components/ui/accordion";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";

/**
 * 사용자 가이드 모달 — common.md SCR-COM-012.
 * 정적 콘텐츠(docs/01_analyze/feature/user-guide-content.md 근거)만 표시하는 프레젠테이션 컴포넌트.
 * 역할 판별은 FE가 전달하는 `myRoles`(로그인 사용자의 역할 코드 배열)만 사용한다.
 */
export interface UserGuideModalProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  myRoles?: string[];
}

interface DomainGuide {
  code: string;
  name: string;
  purpose: string;
  principle: string;
}

interface RoleGuide {
  code: string;
  name: string;
  persona: string;
  description: string;
}

const OVERVIEW_TEXT =
  "이 ITSM 플랫폼은 IT 부서의 서비스 요청·인시던트·문제·변경·지식·자산 관리부터, HR/법무/시설/재무 등 IT 외 부서의 서비스 관리(ESM), 보안 취약점 관리, 컴플라이언스 관리, 인프라 모니터링까지 하나의 시스템에서 처리합니다. 로그인한 사용자는 부여된 역할에 따라 접근 가능한 화면과 기능이 다르며, 헤더의 통합 검색으로 지식·티켓을 도메인 교차 검색할 수 있고, 알림 벨로 각 도메인의 최근 알림을 확인할 수 있습니다.";

const DOMAINS: DomainGuide[] = [
  {
    code: "auth",
    name: "인증/계정/권한(auth)",
    purpose: "로그인·계정·역할 기반 접근 제어(RBAC)로 전 도메인의 공통 기반을 제공한다",
    principle: "모든 화면·API는 역할을 검증하며, 권한 없는 접근은 403으로 차단된다",
  },
  {
    code: "service-request",
    name: "서비스 요청 관리(service-request)",
    purpose: "직원의 일상적 요청(접근 권한·장비·정보 등)을 표준 워크플로우로 처리한다",
    principle:
      "요청은 제출→검증→분류/라우팅→승인→이행→검증→종료 7단계를 거치며, 유형별 SLA(응답·해결)를 추적한다",
  },
  {
    code: "incident",
    name: "인시던트 관리(incident)",
    purpose: "계획되지 않은 서비스 중단·저하를 신속히 탐지·대응·복구한다",
    principle: "심각도(SEV1~3)로 우선순위를 정하고, 해결 후 blameless 포스트모템으로 재발을 방지한다",
  },
  {
    code: "problem",
    name: "문제 관리(problem)",
    purpose: "인시던트의 근본 원인을 제거해 재발을 예방한다",
    principle:
      "근본원인분석(RCA)과 알려진 오류(KEDB)로 지식을 축적하고, 원인 제거 전에는 워크어라운드로 임시 완화한다",
  },
  {
    code: "change",
    name: "변경 관리(change)",
    purpose: "서비스 변경의 위험을 통제하며 변경 성공률을 높인다",
    principle:
      "변경 유형(표준/일반/긴급)과 위험도에 따라 승인 경로(자동승인/동료검토/CAB)가 달라지며, 표준 변경만 사전 승인되어 재승인이 필요 없다",
  },
  {
    code: "knowledge",
    name: "지식 관리(knowledge)",
    purpose: "기술 정보를 축적해 셀프서비스·티켓 차단(deflection)을 실현한다",
    principle: "기사는 초안→검토→게시 상태를 거치며, 게이트키퍼의 검토·승인 없이는 게시되지 않는다(품질 관문)",
  },
  {
    code: "asset",
    name: "IT 자산 관리/CMDB(asset)",
    purpose: "하드웨어·소프트웨어·클라우드 자산의 전 생애주기와 구성 항목(CI) 관계를 관리한다",
    principle:
      "자산/CI를 요청·인시던트·문제·변경 티켓에 연결해 영향 범위를 추적하고, 라이선스·보증 만료 임박 시 알림한다",
  },
  {
    code: "esm",
    name: "엔터프라이즈 서비스 관리(esm)",
    purpose: "ITSM의 서비스 관리 원칙을 HR·법무·시설·재무 등 IT 외 부서로 확장한다",
    principle:
      "HR 케이스는 민감 정보 보호를 위해 HR_CASE_MANAGER와 SYSTEM_ADMIN만 접근할 수 있고, 온보딩/오프보딩 요청 시 관련 부서에 하위 작업이 자동 배정된다",
  },
  {
    code: "vulnerability",
    name: "취약점 관리(vulnerability)",
    purpose: "보안 취약점을 발견부터 개선·검증·보고까지 추적한다",
    principle:
      "리스크 스코어(심각도·악용 가능성)로 우선순위를 정하고, 담당자 배정 없이는 개선(Remediation) 단계로 전이할 수 없다(책임 소재 명확화)",
  },
  {
    code: "compliance",
    name: "컴플라이언스 관리(compliance)",
    purpose: "규제 의무·내부 정책 준수를 서비스 운영에 내재화한다",
    principle: "모든 요구사항에는 책임자(오너)가 지정되어야 하고, 관련 활동(등록·상태 변경·조치 처리)은 감사 로그로 기록된다",
  },
  {
    code: "infra-monitoring",
    name: "인프라 모니터링 & 용량관리(infra-monitoring)",
    purpose: "인프라 자산의 가동률·성능 지표를 추적하고 용량을 계획한다",
    principle: "지표가 설정된 임계치를 벗어나면 알림이 생성되고, SLA 목표 대비 실제 가동률을 비교해 표시한다",
  },
];

const ROLES: RoleGuide[] = [
  {
    code: "SYSTEM_ADMIN",
    name: "시스템 관리자",
    persona: "시스템 관리자",
    description:
      "사이드바 \"관리자\" 메뉴에서 계정 목록/생성/상세로 계정을 활성화·비활성화하고 역할을 부여·회수한다. 역할 관리에서 역할을 정의하고, 감사 로그 조회에서 인증·인가·계정 변경 이력을 확인한다. 그 외에도 다른 모든 역할의 화면·API에 예외적으로 접근할 수 있다",
  },
  {
    code: "END_USER",
    name: "최종 사용자/요청자",
    persona: "최종 사용자/요청자",
    description:
      "서비스 포털에서 요청 유형을 선택해 동적 양식으로 요청을 제출하고, 내 요청 목록에서 진행 상황을 추적한다. 완료 시 CSAT를 제출한다. 지식베이스를 검색해 게시된 기사로 스스로 문제를 해결할 수 있다",
  },
  {
    code: "SERVICE_DESK_AGENT",
    name: "서비스 데스크 상담원",
    persona: "서비스 데스크 상담원",
    description:
      "요청 큐에서 요청을 배정받아 이행하고 상태를 갱신·코멘트로 소통한다. 장애 탐지 시 인시던트를 등록하고 상태 업데이트·에스컬레이션을 수행한다",
  },
  {
    code: "APPROVER",
    name: "승인자(CAB 멤버 포함)",
    persona: "승인자(CAB 멤버 포함)",
    description:
      "승인 대기함(서비스 요청) 또는 CAB 승인 대기함(변경)에서 대상을 검토해 승인 또는 반려한다(반려 시 사유 입력 필수)",
  },
  {
    code: "PROCESS_OWNER",
    name: "프로세스 오너",
    persona: "프로세스 오너",
    description:
      "서비스 카탈로그 관리에서 요청 유형·양식·SLA·승인 설정을 정의하고, 요청 지표 대시보드에서 SLA·CSAT 등 지표를 모니터링한다. ESM 부서별 카탈로그·지표도 동일하게 관리한다",
  },
  {
    code: "INCIDENT_MANAGER",
    name: "인시던트 관리자",
    persona: "인시던트 관리자",
    description:
      "인시던트 상세에서 심각도·우선순위를 설정하고 대응 역할(Tech Lead·Comms·Scribe)을 배정한다. 해결 처리 후 포스트모템(근본원인·조치항목)을 작성한다",
  },
  {
    code: "PROBLEM_MANAGER",
    name: "문제 관리자",
    persona: "문제 관리자",
    description:
      "문제를 등록하고 근본원인분석(RCA)을 기록한다. 워크어라운드와 알려진 오류(KEDB)를 등록하고, 인시던트·변경에 연결한 뒤 후속 조치를 추적해 종료한다",
  },
  {
    code: "CHANGE_MANAGER",
    name: "변경 관리자",
    persona: "변경 관리자",
    description:
      "변경 요청(RFC)을 생성하고 유형·위험에 따라 승인 경로를 분류한다. 변경 일정(캘린더)을 확인하고, 구현 결과(성공/실패·롤백 여부)를 기록한다(승인 결정 자체는 APPROVER/CAB 권한)",
  },
  {
    code: "KNOWLEDGE_CONTRIBUTOR",
    name: "지식 기여자",
    persona: "지식 기여자",
    description:
      "기사 작성·편집 화면에서 기사를 작성·수정하고 검토를 요청한다(초안→검토). 요청·인시던트·문제 처리 중 기사를 작성하거나 기존 기사를 연결한다(KCS)",
  },
  {
    code: "KNOWLEDGE_GATEKEEPER",
    name: "지식 게이트키퍼",
    persona: "지식 게이트키퍼",
    description:
      "검토·게시 승인함에서 검토 대기 기사를 확인해 게시 승인 또는 반려한다(반려 시 사유 입력, 기사는 초안으로 복귀)",
  },
  {
    code: "ASSET_MANAGER",
    name: "자산 관리자",
    persona: "자산 관리자",
    description:
      "자산 등록/수정에서 자산 정보를 입력하고 생애주기(계획~폐기)를 관리한다. CI·CMDB 관계 뷰에서 CI 간 의존 관계를 등록해 변경 영향 범위를 조회한다. 만료 임박 자산은 지표 대시보드에서 확인한다",
  },
  {
    code: "HR_CASE_MANAGER",
    name: "HR 케이스 담당자",
    persona: "HR 케이스 담당자",
    description:
      "HR 케이스 목록에서 케이스를 접수하고, 케이스 상세에서 기록→조사→해결 순서로 상태를 전이한다. HR 케이스는 HR_CASE_MANAGER와 SYSTEM_ADMIN만 접근할 수 있다",
  },
  {
    code: "DEPT_COORDINATOR",
    name: "부서 처리 담당자",
    persona: "부서 처리 담당자",
    description:
      "부서 요청 처리 큐에서 소속 부서로 들어온 요청을 확인해 상태를 전이·코멘트로 응대한다. 내 하위 작업 목록에서 온보딩/오프보딩 체크리스트 중 소속 부서에 배정된 하위 작업을 완료 처리한다",
  },
  {
    code: "VULNERABILITY_MANAGER",
    name: "취약점 관리 담당자",
    persona: "취약점 관리 담당자",
    description:
      "취약점을 등록하고 리스크 스코어(심각도·악용 가능성)를 산정한다. 담당자를 배정한 뒤(배정 없이는 개선 단계로 전이 불가) 개선 조치와 검증 결과를 기록해 종결한다",
  },
  {
    code: "COMPLIANCE_OFFICER",
    name: "컴플라이언스 담당자",
    persona: "컴플라이언스 담당자",
    description:
      "컴플라이언스 요구사항을 등록하고 책임자(오너)를 지정한다. 위반 이슈는 시정조치 항목으로 등록해 탐지→해결까지 추적하고, 준수 현황 대시보드에서 준수율을 확인한다",
  },
  {
    code: "INFRA_OPERATOR",
    name: "인프라 운영 담당자",
    persona: "인프라 운영 담당자",
    description:
      "인프라 지표 등록에서 가동률·성능 지표(CPU·메모리·응답시간 등)를 수동 입력한다. 임계치를 설정하고 초과 시 알림을 확인하며, 용량 계획 관리에서 팀/서비스별 처리 역량 대비 수요를 등록·조회한다",
  },
];

export function UserGuideModal({ open, onOpenChange, myRoles = [] }: UserGuideModalProps) {
  const myRoleSet = useMemo(() => new Set(myRoles), [myRoles]);
  const myRoleGuides = ROLES.filter((r) => myRoleSet.has(r.code));
  const otherRoleGuides = ROLES.filter((r) => !myRoleSet.has(r.code));
  const orderedRoles = [...myRoleGuides, ...otherRoleGuides];

  return (
    <Modal
      open={open}
      onOpenChange={onOpenChange}
      title="사용자 가이드"
      className="max-w-3xl"
    >
      <Tabs defaultValue="overview">
        <TabsList>
          <TabsTrigger value="overview">개요</TabsTrigger>
          <TabsTrigger value="domains">도메인 및 원칙</TabsTrigger>
          <TabsTrigger value="roles">역할별 수행 내용과 방법</TabsTrigger>
        </TabsList>

        <TabsContent value="overview" className="max-h-[60vh] overflow-y-auto">
          <p className="text-sm text-foreground">{OVERVIEW_TEXT}</p>
        </TabsContent>

        <TabsContent value="domains" className="max-h-[60vh] overflow-y-auto">
          <Accordion type="multiple" className="space-y-2">
            {DOMAINS.map((d) => (
              <AccordionItem key={d.code} value={d.code}>
                <AccordionTrigger>{d.name}</AccordionTrigger>
                <AccordionContent>
                  <p>
                    <span className="font-medium text-foreground">목적: </span>
                    {d.purpose}
                  </p>
                  <p className="mt-1">
                    <span className="font-medium text-foreground">핵심 원칙: </span>
                    {d.principle}
                  </p>
                </AccordionContent>
              </AccordionItem>
            ))}
          </Accordion>
        </TabsContent>

        <TabsContent value="roles" className="max-h-[60vh] overflow-y-auto">
          <Accordion
            type="multiple"
            defaultValue={myRoleGuides.map((r) => r.code)}
            className="space-y-2"
          >
            {orderedRoles.map((r) => (
              <AccordionItem key={r.code} value={r.code}>
                <AccordionTrigger>
                  <span className="flex items-center gap-2">
                    {r.name}
                    {myRoleSet.has(r.code) ? (
                      <StatusBadge tone="info" label="내 역할" />
                    ) : null}
                  </span>
                </AccordionTrigger>
                <AccordionContent>
                  <p>
                    <span className="font-medium text-foreground">페르소나: </span>
                    {r.persona}
                  </p>
                  <p className="mt-1">{r.description}</p>
                </AccordionContent>
              </AccordionItem>
            ))}
          </Accordion>
        </TabsContent>
      </Tabs>
    </Modal>
  );
}
