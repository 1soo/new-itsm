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
import {
  type Column,
  DataTable,
  Pagination,
  StatusBadge,
  TicketListLayout,
  toast,
} from "@/components/common";
import { changeApi } from "@/features/change/api";
import { formatDate } from "@/features/change/format";
import {
  CHANGE_STATUSES,
  CHANGE_TYPES,
  RISKS,
  riskLabel,
  riskTone,
  statusLabel,
  statusTone,
  typeLabel,
  typeTone,
} from "@/features/change/status";
import type {
  ChangeStatus,
  ChangeSummary,
  ChangeType,
  PageResponse,
  Risk,
} from "@/features/change/types";
import { extractErrorMessage } from "@/lib/apiClient";

/*
 * 변경 목록(SCR-CHG-001) — 공통 목록/필터 패턴(SCR-COM-007).
 * 필터(유형·상태·위험도·기간) / 표(식별키·요약·유형·상태·위험도·예정일).
 */
const PAGE_SIZE = 14;
const ALL = "ALL";

interface Filters {
  type: string;
  status: string;
  risk: string;
  from: string;
  to: string;
}

const EMPTY: Filters = { type: ALL, status: ALL, risk: ALL, from: "", to: "" };

export function ChangeListPage() {
  const { t } = useTranslation("change");
  const navigate = useNavigate();
  const [inputs, setInputs] = useState<Filters>(EMPTY);
  const [applied, setApplied] = useState<Filters>(EMPTY);
  const [page, setPage] = useState(0);
  const [data, setData] = useState<PageResponse<ChangeSummary> | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let active = true;
    setLoading(true);
    changeApi
      .list({
        type: applied.type === ALL ? undefined : (applied.type as ChangeType),
        status: applied.status === ALL ? undefined : (applied.status as ChangeStatus),
        risk: applied.risk === ALL ? undefined : (applied.risk as Risk),
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

  const columns: Column<ChangeSummary>[] = [
    { header: t("changeList.columnTicketKey", { defaultValue: "식별키" }), width: 130, cell: (c) => c.ticketKey },
    {
      header: t("changeList.columnSummary", { defaultValue: "요약" }),
      cell: (c) => <span className="line-clamp-1">{c.summary}</span>,
    },
    {
      header: t("changeList.columnType", { defaultValue: "유형" }),
      width: 110,
      cell: (c) => <StatusBadge tone={typeTone(c.type)} label={typeLabel(t, c.type)} />,
    },
    {
      header: t("changeList.columnStatus", { defaultValue: "상태" }),
      width: 110,
      cell: (c) => <StatusBadge tone={statusTone(c.status)} label={statusLabel(t, c.status)} />,
    },
    {
      header: t("changeList.columnRisk", { defaultValue: "위험도" }),
      width: 110,
      cell: (c) =>
        c.risk ? (
          <StatusBadge tone={riskTone(c.risk)} label={riskLabel(t, c.risk)} />
        ) : (
          <span className="text-sm text-muted-foreground">
            {t("changeList.riskNotAssessed", { defaultValue: "미평가" })}
          </span>
        ),
    },
    {
      header: t("changeList.columnScheduledAt", { defaultValue: "예정일" }),
      width: 110,
      cell: (c) => (c.scheduledAt ? formatDate(c.scheduledAt) : "-"),
    },
  ];

  const totalPages = data ? Math.ceil(data.totalElements / PAGE_SIZE) : 0;

  return (
    <TicketListLayout
      title={t("changeList.title", { defaultValue: "변경" })}
      actions={
        <Button onClick={() => navigate("/changes/new")}>
          <Plus />
          {t("changeList.createButton", { defaultValue: "변경 요청 생성" })}
        </Button>
      }
      filters={
        <form onSubmit={handleSearch} className="flex flex-wrap items-end gap-2">
          <div className="space-y-1">
            <Label>{t("changeList.columnType", { defaultValue: "유형" })}</Label>
            <Select value={inputs.type} onValueChange={(v) => setInputs((f) => ({ ...f, type: v }))}>
              <SelectTrigger className="w-28"><SelectValue /></SelectTrigger>
              <SelectContent>
                <SelectItem value={ALL}>{t("changeList.filterAll", { defaultValue: "전체" })}</SelectItem>
                {CHANGE_TYPES.map((ty) => (
                  <SelectItem key={ty} value={ty}>{typeLabel(t, ty)}</SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
          <div className="space-y-1">
            <Label>{t("changeList.columnStatus", { defaultValue: "상태" })}</Label>
            <Select value={inputs.status} onValueChange={(v) => setInputs((f) => ({ ...f, status: v }))}>
              <SelectTrigger className="w-36"><SelectValue /></SelectTrigger>
              <SelectContent>
                <SelectItem value={ALL}>{t("changeList.filterAll", { defaultValue: "전체" })}</SelectItem>
                {CHANGE_STATUSES.map((s) => (
                  <SelectItem key={s} value={s}>{statusLabel(t, s)}</SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
          <div className="space-y-1">
            <Label>{t("changeList.columnRisk", { defaultValue: "위험도" })}</Label>
            <Select value={inputs.risk} onValueChange={(v) => setInputs((f) => ({ ...f, risk: v }))}>
              <SelectTrigger className="w-28"><SelectValue /></SelectTrigger>
              <SelectContent>
                <SelectItem value={ALL}>{t("changeList.filterAll", { defaultValue: "전체" })}</SelectItem>
                {RISKS.map((r) => (
                  <SelectItem key={r} value={r}>{riskLabel(t, r)}</SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
          <div className="space-y-1">
            <Label htmlFor="from">{t("changeList.filterFrom", { defaultValue: "시작일" })}</Label>
            <Input id="from" type="date" value={inputs.from} onChange={(e) => setInputs((f) => ({ ...f, from: e.target.value }))} />
          </div>
          <div className="space-y-1">
            <Label htmlFor="to">{t("changeList.filterTo", { defaultValue: "종료일" })}</Label>
            <Input id="to" type="date" value={inputs.to} onChange={(e) => setInputs((f) => ({ ...f, to: e.target.value }))} />
          </div>
          <Button type="submit">{t("changeList.searchButton", { defaultValue: "검색" })}</Button>
        </form>
      }
    >
      <DataTable
        columns={columns}
        data={data?.content ?? []}
        rowKey={(c) => c.id}
        loading={loading}
        onRowClick={(c) => navigate(`/changes/${c.id}`)}
        emptyTitle={t("changeList.emptyTitle", { defaultValue: "변경이 없습니다" })}
        emptyDescription={t("changeList.emptyDescription", { defaultValue: "조건에 맞는 변경이 없습니다." })}
      />
      <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />
    </TicketListLayout>
  );
}
