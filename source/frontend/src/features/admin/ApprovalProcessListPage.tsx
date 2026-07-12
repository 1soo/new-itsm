import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { Plus } from "lucide-react";

import { Button } from "@/components/ui/button";
import { Label } from "@/components/ui/label";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { type Column, DataTable, StatusBadge, TicketListLayout, toast } from "@/components/common";
import { adminApi } from "@/features/admin/api";
import type {
  ApprovalDomain,
  ApprovalDomainOption,
  ApprovalProcessSummary,
} from "@/features/admin/types";
import { extractErrorMessage } from "@/lib/apiClient";

/*
 * 승인 프로세스 목록(SCR-ADMIN-007) — 등록된 승인 프로세스 규칙을 도메인별로 조회하고
 * 생성/편집 화면(SCR-ADMIN-008)으로 진입한다. System Admin 전용(라우트 가드에서 처리).
 */
const ALL = "ALL";

const TIER_LABEL: Record<number, string> = {
  1: "도메인 기본",
  2: "요청유형 전용",
  3: "요청자 역할 전용",
};

export function ApprovalProcessListPage() {
  const { t } = useTranslation("auth");
  const navigate = useNavigate();
  const [domains, setDomains] = useState<ApprovalDomainOption[]>([]);
  const [domain, setDomain] = useState(ALL);
  const [items, setItems] = useState<ApprovalProcessSummary[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    adminApi
      .listApprovalDomains()
      .then(setDomains)
      .catch((err) => toast.error(extractErrorMessage(err)));
  }, []);

  useEffect(() => {
    let active = true;
    setLoading(true);
    adminApi
      .listApprovalProcesses({ domain: domain === ALL ? undefined : (domain as ApprovalDomain) })
      .then((res) => {
        if (active) setItems(res.content);
      })
      .catch((err) => {
        if (active) toast.error(extractErrorMessage(err));
      })
      .finally(() => {
        if (active) setLoading(false);
      });
    return () => {
      active = false;
    };
  }, [domain]);

  const domainLabel = (d: string) => domains.find((o) => o.domain === d)?.label ?? d;

  const tierLabel = (tier: number): string =>
    t(`admin.approvalProcessList.tier.${tier}`, {
      defaultValue: TIER_LABEL[tier] ?? `tier ${tier}`,
    });

  const columns: Column<ApprovalProcessSummary>[] = [
    { header: t("admin.approvalProcessList.columnName", { defaultValue: "규칙명" }), cell: (p) => p.name },
    { header: t("admin.approvalProcessList.columnDomain", { defaultValue: "도메인" }), cell: (p) => domainLabel(p.domain) },
    {
      header: t("admin.approvalProcessList.columnRequestSubtype", { defaultValue: "요청유형" }),
      cell: (p) => p.requestSubtypeLabel ?? t("admin.approvalProcessList.all", { defaultValue: "전체" }),
    },
    {
      header: t("admin.approvalProcessList.columnRequesterRoles", { defaultValue: "요청자 역할" }),
      cell: (p) =>
        p.requesterRoles.length === 0 ? (
          <span className="text-muted-foreground">{t("admin.approvalProcessList.all", { defaultValue: "전체" })}</span>
        ) : (
          <span className="flex flex-wrap gap-1">
            {p.requesterRoles.map((r) => (
              <StatusBadge key={r} tone="info" label={r} />
            ))}
          </span>
        ),
    },
    {
      header: t("admin.approvalProcessList.columnPriorityTier", { defaultValue: "우선순위" }),
      cell: (p) => <StatusBadge tone="muted" label={tierLabel(p.priorityTier)} />,
    },
    {
      header: t("admin.approvalProcessList.columnStepCount", { defaultValue: "차수" }),
      cell: (p) =>
        p.stepCount === 0
          ? t("admin.approvalProcessList.noApprovers", { defaultValue: "승인자 없음" })
          : t("admin.approvalProcessList.stepCount", {
              count: p.stepCount,
              defaultValue: `${p.stepCount}차`,
            }),
    },
  ];

  return (
    <TicketListLayout
      title={t("admin.approvalProcessList.title", { defaultValue: "승인 프로세스" })}
      description={t("admin.approvalProcessList.description", {
        defaultValue: "도메인·요청유형·요청자별 커스텀 승인 프로세스를 관리합니다.",
      })}
      actions={
        <Button onClick={() => navigate("/admin/approval-processes/new")}>
          <Plus />
          {t("admin.approvalProcessList.createButton", { defaultValue: "프로세스 생성" })}
        </Button>
      }
      filters={
        <div className="space-y-1">
          <Label>{t("admin.approvalProcessList.columnDomain", { defaultValue: "도메인" })}</Label>
          <Select value={domain} onValueChange={setDomain}>
            <SelectTrigger className="w-48">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value={ALL}>{t("admin.approvalProcessList.all", { defaultValue: "전체" })}</SelectItem>
              {domains.map((d) => (
                <SelectItem key={d.domain} value={d.domain}>
                  {d.label}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>
      }
    >
      <DataTable
        columns={columns}
        data={items}
        rowKey={(p) => p.id}
        loading={loading}
        onRowClick={(p) => navigate(`/admin/approval-processes/${p.id}`)}
        emptyTitle={t("admin.approvalProcessList.emptyTitle", {
          defaultValue: "등록된 승인 프로세스가 없습니다",
        })}
      />
    </TicketListLayout>
  );
}
