import { type FormEvent, useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { Plus } from "lucide-react";

import { Button } from "@/components/ui/button";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Label } from "@/components/ui/label";
import {
  type Column,
  DataTable,
  Pagination,
  StatusBadge,
  TicketListLayout,
  toast,
} from "@/components/common";
import { complianceApi } from "@/features/compliance/api";
import { formatDate } from "@/features/compliance/format";
import {
  COMPLIANCE_STATUSES,
  complianceStatusLabel,
  complianceStatusTone,
} from "@/features/compliance/status";
import type {
  ComplianceStatus,
  PageResponse,
  RequirementSummary,
} from "@/features/compliance/types";
import { extractErrorMessage } from "@/lib/apiClient";

/*
 * 컴플라이언스 요구사항 목록(SCR-COMP-001) — 공통 목록/필터 패턴(SCR-COM-007).
 * 필터(준수 상태·책임자 지정 여부) / 표(식별키·이름·근거·책임자·준수 상태·갱신일).
 */
const PAGE_SIZE = 14;
const ALL = "ALL";

interface Filters {
  complianceStatus: string;
  ownerAssigned: string;
}

const EMPTY: Filters = { complianceStatus: ALL, ownerAssigned: ALL };

export function ComplianceListPage() {
  const { t } = useTranslation("compliance");
  const navigate = useNavigate();
  const [inputs, setInputs] = useState<Filters>(EMPTY);
  const [applied, setApplied] = useState<Filters>(EMPTY);
  const [page, setPage] = useState(0);
  const [data, setData] = useState<PageResponse<RequirementSummary> | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let active = true;
    setLoading(true);
    complianceApi
      .list({
        complianceStatus:
          applied.complianceStatus === ALL ? undefined : (applied.complianceStatus as ComplianceStatus),
        ownerAssigned: applied.ownerAssigned === ALL ? undefined : applied.ownerAssigned === "true",
        page,
        size: PAGE_SIZE,
      })
      .then((res) => active && setData(res))
      .catch((err) => active && toast.error(extractErrorMessage(err)))
      .finally(() => active && setLoading(false));
    return () => {
      active = false;
    };
  }, [applied, page]);

  const handleSearch = (e: FormEvent) => {
    e.preventDefault();
    setPage(0);
    setApplied(inputs);
  };

  const columns: Column<RequirementSummary>[] = [
    { header: t("complianceList.columnRequirementKey", { defaultValue: "식별키" }), width: 130, cell: (r) => r.requirementKey },
    { header: t("complianceList.columnName", { defaultValue: "이름" }), cell: (r) => <span className="line-clamp-1">{r.name}</span> },
    { header: t("complianceList.columnBasis", { defaultValue: "근거" }), cell: (r) => <span className="line-clamp-1">{r.basis}</span> },
    {
      header: t("complianceList.columnOwner", { defaultValue: "책임자" }),
      width: 150,
      cell: (r) => (r.owner ? r.owner : <StatusBadge tone="danger" label={t("complianceList.ownerUnassigned", { defaultValue: "책임자 미지정" })} />),
    },
    {
      header: t("complianceList.columnComplianceStatus", { defaultValue: "준수 상태" }),
      width: 120,
      cell: (r) => (
        <StatusBadge tone={complianceStatusTone(r.complianceStatus)} label={complianceStatusLabel(t, r.complianceStatus)} />
      ),
    },
    { header: t("complianceList.columnUpdatedAt", { defaultValue: "갱신일" }), width: 110, cell: (r) => formatDate(r.updatedAt) },
  ];

  const totalPages = data ? Math.ceil(data.totalElements / PAGE_SIZE) : 0;

  return (
    <TicketListLayout
      title={t("complianceList.title", { defaultValue: "컴플라이언스 요구사항" })}
      actions={
        <Button onClick={() => navigate("/compliance/requirements/new")}>
          <Plus />
          {t("complianceList.createButton", { defaultValue: "요구사항 등록" })}
        </Button>
      }
      filters={
        <form onSubmit={handleSearch} className="flex flex-wrap items-end gap-2">
          <div className="space-y-1">
            <Label>{t("complianceList.filterComplianceStatus", { defaultValue: "준수 상태" })}</Label>
            <Select
              value={inputs.complianceStatus}
              onValueChange={(v) => setInputs((f) => ({ ...f, complianceStatus: v }))}
            >
              <SelectTrigger className="w-36"><SelectValue /></SelectTrigger>
              <SelectContent>
                <SelectItem value={ALL}>{t("complianceList.filterAll", { defaultValue: "전체" })}</SelectItem>
                {COMPLIANCE_STATUSES.map((s) => (
                  <SelectItem key={s} value={s}>{complianceStatusLabel(t, s)}</SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
          <div className="space-y-1">
            <Label>{t("complianceList.filterOwnerAssigned", { defaultValue: "책임자 지정 여부" })}</Label>
            <Select
              value={inputs.ownerAssigned}
              onValueChange={(v) => setInputs((f) => ({ ...f, ownerAssigned: v }))}
            >
              <SelectTrigger className="w-36"><SelectValue /></SelectTrigger>
              <SelectContent>
                <SelectItem value={ALL}>{t("complianceList.filterAll", { defaultValue: "전체" })}</SelectItem>
                <SelectItem value="true">{t("complianceList.filterAssigned", { defaultValue: "지정됨" })}</SelectItem>
                <SelectItem value="false">{t("complianceList.filterUnassigned", { defaultValue: "미지정" })}</SelectItem>
              </SelectContent>
            </Select>
          </div>
          <Button type="submit">{t("complianceList.searchButton", { defaultValue: "검색" })}</Button>
        </form>
      }
    >
      <DataTable
        columns={columns}
        data={data?.content ?? []}
        rowKey={(r) => r.id}
        loading={loading}
        onRowClick={(r) => navigate(`/compliance/requirements/${r.id}`)}
        emptyTitle={t("complianceList.emptyTitle", { defaultValue: "요구사항이 없습니다" })}
        emptyDescription={t("complianceList.emptyDescription", { defaultValue: "조건에 맞는 컴플라이언스 요구사항이 없습니다." })}
      />
      <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />
    </TicketListLayout>
  );
}
