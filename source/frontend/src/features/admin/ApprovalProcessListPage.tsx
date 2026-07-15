import { useEffect, useRef, useState } from "react";
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
import { type Column, ConfirmDialog, DataTable, StatusBadge, TicketListLayout, toast } from "@/components/common";
import { adminApi } from "@/features/admin/api";
import type {
  ApprovalDomain,
  ApprovalDomainOption,
  ApprovalProcessSummary,
} from "@/features/admin/types";
import { extractErrorMessage } from "@/lib/apiClient";

/*
 * 승인 프로세스 목록(SCR-ADMIN-007) — 등록된 승인 프로세스 규칙을 도메인별로 조회하고
 * 생성/편집 화면(SCR-ADMIN-008)으로 진입, 불필요한 규칙을 삭제한다. System Admin 전용(라우트 가드에서 처리).
 */
const ALL = "ALL";

export function ApprovalProcessListPage() {
  const { t } = useTranslation("auth");
  const navigate = useNavigate();
  const [domains, setDomains] = useState<ApprovalDomainOption[]>([]);
  const [domain, setDomain] = useState(ALL);
  const [items, setItems] = useState<ApprovalProcessSummary[]>([]);
  const [loading, setLoading] = useState(true);

  const [deleting, setDeleting] = useState<ApprovalProcessSummary | null>(null);
  const [deleteBusy, setDeleteBusy] = useState(false);

  useEffect(() => {
    adminApi
      .listApprovalDomains()
      .then(setDomains)
      .catch((err) => toast.error(extractErrorMessage(err)));
  }, []);

  // requestSeq로 최신 요청만 반영한다(active 플래그와 동일한 목적 — 도메인 필터를 빠르게 연속
  // 변경했을 때 늦게 응답한 이전 요청이 최종 화면을 덮어쓰는 race와 언마운트 후 setState를 방지).
  const requestSeq = useRef(0);

  const loadItems = () => {
    const seq = ++requestSeq.current;
    setLoading(true);
    adminApi
      .listApprovalProcesses({ domain: domain === ALL ? undefined : (domain as ApprovalDomain) })
      .then((res) => {
        if (seq === requestSeq.current) setItems(res.content);
      })
      .catch((err) => {
        if (seq === requestSeq.current) toast.error(extractErrorMessage(err));
      })
      .finally(() => {
        if (seq === requestSeq.current) setLoading(false);
      });
  };

  useEffect(() => {
    loadItems();
    return () => {
      requestSeq.current++;
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [domain]);

  const allLabel = t("admin.approvalProcessList.all", { defaultValue: "전체" });
  const domainLabel = (d: string | null) => (d == null ? allLabel : domains.find((o) => o.domain === d)?.label ?? d);

  // 2026-07-15 우선순위 재설계: tier 3종 라벨을 폐기하고, 행 데이터(도메인·요청유형·요청자 역할)에서
  // 지정된 축을 직접 조합해 라벨링한다(priorityTier는 서버 매칭·정렬용으로만 사용, 화면 비노출).
  const priorityLabel = (p: ApprovalProcessSummary): string => {
    const axes = [
      p.domain != null && t("admin.approvalProcessList.priorityAxisDomain", { defaultValue: "도메인" }),
      p.requestSubtypeKey != null &&
        t("admin.approvalProcessList.priorityAxisSubtype", { defaultValue: "요청유형" }),
      p.requesterRoles.length > 0 &&
        t("admin.approvalProcessList.priorityAxisRole", { defaultValue: "역할" }),
    ].filter((v): v is string => !!v);
    return axes.length > 0 ? axes.join("+") : t("admin.approvalProcessList.priorityAll", { defaultValue: "전체 적용" });
  };

  const handleDelete = async () => {
    if (!deleting) return;
    setDeleteBusy(true);
    try {
      await adminApi.deleteApprovalProcess(deleting.id);
      toast.success(t("admin.approvalProcessList.deleteSuccess", { defaultValue: "승인 프로세스가 삭제되었습니다" }));
      setDeleting(null);
      loadItems();
    } catch (err) {
      toast.error(extractErrorMessage(err));
    } finally {
      setDeleteBusy(false);
    }
  };

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
      cell: (p) => <StatusBadge tone="muted" label={priorityLabel(p)} />,
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
    {
      header: t("admin.approvalProcessList.columnActions", { defaultValue: "액션" }),
      className: "text-right",
      cell: (p) => (
        <div className="flex justify-end">
          <Button
            size="sm"
            variant="destructive"
            onClick={(e) => {
              e.stopPropagation();
              setDeleting(p);
            }}
          >
            {t("admin.approvalProcessList.deleteButton", { defaultValue: "삭제" })}
          </Button>
        </div>
      ),
    },
  ];

  return (
    <>
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

      <ConfirmDialog
        open={!!deleting}
        onOpenChange={(open) => !open && setDeleting(null)}
        title={t("admin.approvalProcessList.deleteDialogTitle", { defaultValue: "승인 프로세스 삭제" })}
        description={t("admin.approvalProcessList.deleteDialogDescription", {
          name: deleting?.name ?? "",
          defaultValue: `"${deleting?.name ?? ""}" 승인 프로세스를 삭제하시겠습니까? 진행 중인 승인 인스턴스는 삭제 영향을 받지 않습니다.`,
        })}
        confirmLabel={t("admin.approvalProcessList.deleteButton", { defaultValue: "삭제" })}
        loading={deleteBusy}
        onConfirm={handleDelete}
      />
    </>
  );
}
