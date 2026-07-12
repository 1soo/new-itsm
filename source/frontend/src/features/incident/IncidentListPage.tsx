import { type FormEvent, useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { Plus } from "lucide-react";

import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Badge } from "@/components/ui/badge";
import {
  type Column,
  DataTable,
  Pagination,
  StatusBadge,
  TicketListLayout,
  toast,
} from "@/components/common";
import { incidentApi } from "@/features/incident/api";
import { formatDate } from "@/features/incident/format";
import {
  INCIDENT_STATUSES,
  SEVERITIES,
  severityTone,
  statusLabel,
  statusTone,
} from "@/features/incident/status";
import type {
  IncidentStatus,
  IncidentSummary,
  PageResponse,
  Severity,
} from "@/features/incident/types";
import { extractErrorMessage } from "@/lib/apiClient";

/*
 * 인시던트 목록(SCR-INC-001) — 공통 목록/필터 패턴(SCR-COM-007).
 * 필터(상태·심각도·담당자·키워드·기간) / 표(식별키·요약·SEV·상태·담당자·갱신일·PM필요) / 등록 버튼.
 */
const PAGE_SIZE = 10;
const ALL = "ALL";

interface Filters {
  status: string;
  severity: string;
  assignee: string;
  keyword: string;
  from: string;
  to: string;
}

const EMPTY: Filters = { status: ALL, severity: ALL, assignee: "", keyword: "", from: "", to: "" };

export function IncidentListPage() {
  const { t } = useTranslation("incident");
  const navigate = useNavigate();
  const [inputs, setInputs] = useState<Filters>(EMPTY);
  const [applied, setApplied] = useState<Filters>(EMPTY);
  const [page, setPage] = useState(0);
  const [data, setData] = useState<PageResponse<IncidentSummary> | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let active = true;
    setLoading(true);
    incidentApi
      .list({
        status: applied.status === ALL ? undefined : (applied.status as IncidentStatus),
        severity: applied.severity === ALL ? undefined : (applied.severity as Severity),
        assignee: applied.assignee || undefined,
        keyword: applied.keyword || undefined,
        from: applied.from || undefined,
        to: applied.to || undefined,
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

  const columns: Column<IncidentSummary>[] = [
    { header: t("incidentList.columnTicketKey", { defaultValue: "식별키" }), cell: (i) => i.ticketKey },
    {
      header: t("incidentList.columnSummary", { defaultValue: "요약" }),
      cell: (i) => <span className="line-clamp-1">{i.summary}</span>,
    },
    {
      header: t("incidentList.columnSeverity", { defaultValue: "심각도" }),
      cell: (i) => <StatusBadge tone={severityTone(i.severity)} label={i.severity} />,
    },
    {
      header: t("incidentList.columnStatus", { defaultValue: "상태" }),
      cell: (i) => <StatusBadge tone={statusTone(i.status)} label={statusLabel(t, i.status)} />,
    },
    {
      header: t("incidentList.columnAssignee", { defaultValue: "담당자" }),
      cell: (i) => i.assignee || t("incidentList.unassigned", { defaultValue: "미배정" }),
    },
    {
      header: "PM",
      cell: (i) =>
        i.postmortemRequired ? (
          <Badge variant="warning">{t("incidentList.pmRequired", { defaultValue: "PM 필요" })}</Badge>
        ) : null,
    },
    { header: t("incidentList.columnUpdatedAt", { defaultValue: "갱신일" }), cell: (i) => formatDate(i.updatedAt) },
  ];

  const totalPages = data ? Math.ceil(data.totalElements / PAGE_SIZE) : 0;

  return (
    <TicketListLayout
      title={t("incidentList.title", { defaultValue: "인시던트" })}
      actions={
        <Button onClick={() => navigate("/incidents/new")}>
          <Plus />
          {t("incidentList.createButton", { defaultValue: "인시던트 등록" })}
        </Button>
      }
      filters={
        <form onSubmit={handleSearch} className="flex flex-wrap items-end gap-2">
          <div className="space-y-1">
            <Label>{t("incidentList.columnStatus", { defaultValue: "상태" })}</Label>
            <Select value={inputs.status} onValueChange={(v) => setInputs((f) => ({ ...f, status: v }))}>
              <SelectTrigger className="w-32"><SelectValue /></SelectTrigger>
              <SelectContent>
                <SelectItem value={ALL}>{t("incidentList.filterAll", { defaultValue: "전체" })}</SelectItem>
                {INCIDENT_STATUSES.map((s) => (
                  <SelectItem key={s} value={s}>{statusLabel(t, s)}</SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
          <div className="space-y-1">
            <Label>{t("incidentList.columnSeverity", { defaultValue: "심각도" })}</Label>
            <Select value={inputs.severity} onValueChange={(v) => setInputs((f) => ({ ...f, severity: v }))}>
              <SelectTrigger className="w-28"><SelectValue /></SelectTrigger>
              <SelectContent>
                <SelectItem value={ALL}>{t("incidentList.filterAll", { defaultValue: "전체" })}</SelectItem>
                {SEVERITIES.map((s) => (
                  <SelectItem key={s} value={s}>{s}</SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
          <div className="space-y-1">
            <Label htmlFor="assignee">{t("incidentList.columnAssignee", { defaultValue: "담당자" })}</Label>
            <Input id="assignee" value={inputs.assignee} onChange={(e) => setInputs((f) => ({ ...f, assignee: e.target.value }))} />
          </div>
          <div className="space-y-1">
            <Label htmlFor="keyword">{t("incidentList.filterKeyword", { defaultValue: "키워드" })}</Label>
            <Input id="keyword" value={inputs.keyword} onChange={(e) => setInputs((f) => ({ ...f, keyword: e.target.value }))} />
          </div>
          <Button type="submit">{t("incidentList.searchButton", { defaultValue: "검색" })}</Button>
        </form>
      }
    >
      <DataTable
        columns={columns}
        data={data?.content ?? []}
        rowKey={(i) => i.id}
        loading={loading}
        onRowClick={(i) => navigate(`/incidents/${i.id}`)}
        emptyTitle={t("incidentList.emptyTitle", { defaultValue: "인시던트가 없습니다" })}
        emptyDescription={t("incidentList.emptyDescription", { defaultValue: "조건에 맞는 인시던트가 없습니다." })}
      />
      <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />
    </TicketListLayout>
  );
}
