import { type FormEvent, useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";

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
import { esmApi } from "@/features/esm/api";
import { formatDate } from "@/features/esm/format";
import { departmentLabel, requestStatusLabel, requestStatusTone } from "@/features/esm/status";
import type { EsmRequestStatus, EsmRequestSummary, PageResponse } from "@/features/esm/types";
import { extractErrorMessage } from "@/lib/apiClient";

/*
 * 내 부서 요청 목록(SCR-ESM-003) — 요청자 본인 요청만 조회(scope=mine).
 * 공통 목록/필터 패턴(SCR-COM-007) 재사용.
 */
const PAGE_SIZE = 13;
const ALL = "ALL";

const STATUS_OPTIONS: EsmRequestStatus[] = ["SUBMITTED", "IN_PROGRESS", "COMPLETED", "REJECTED"];

interface Filters {
  status: string;
  from: string;
  to: string;
}

const EMPTY: Filters = { status: ALL, from: "", to: "" };

export function MyEsmRequestsPage() {
  const { t } = useTranslation("esm");
  const navigate = useNavigate();
  const [inputs, setInputs] = useState<Filters>(EMPTY);
  const [applied, setApplied] = useState<Filters>(EMPTY);
  const [page, setPage] = useState(0);
  const [data, setData] = useState<PageResponse<EsmRequestSummary> | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let active = true;
    setLoading(true);
    esmApi
      .listRequests({
        scope: "mine",
        status: applied.status === ALL ? undefined : (applied.status as EsmRequestStatus),
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

  const columns: Column<EsmRequestSummary>[] = [
    { header: t("myEsmRequests.columnTicketKey", { defaultValue: "접수번호" }), width: 130, cell: (r) => r.ticketKey },
    {
      header: t("myEsmRequests.columnDepartment", { defaultValue: "부서" }),
      width: 100,
      cell: (r) => <StatusBadge tone="info" label={departmentLabel(t, r.department)} />,
    },
    {
      header: t("myEsmRequests.columnType", { defaultValue: "유형" }),
      cell: (r) => <span className="truncate block">{r.catalogItemName}</span>,
    },
    {
      header: t("myEsmRequests.columnStatus", { defaultValue: "상태" }),
      width: 110,
      cell: (r) => <StatusBadge tone={requestStatusTone(r.status)} label={requestStatusLabel(t, r.status)} />,
    },
    { header: t("myEsmRequests.columnUpdatedAt", { defaultValue: "갱신일" }), width: 110, cell: (r) => formatDate(r.updatedAt) },
  ];

  const totalPages = data ? Math.ceil(data.totalElements / PAGE_SIZE) : 0;

  return (
    <TicketListLayout
      title={t("myEsmRequests.title", { defaultValue: "내 부서 요청" })}
      description={t("myEsmRequests.description", { defaultValue: "본인이 제출한 부서 요청을 추적합니다." })}
      filters={
        <form onSubmit={handleSearch} className="flex flex-wrap items-end gap-2">
          <div className="space-y-1">
            <Label>{t("myEsmRequests.columnStatus", { defaultValue: "상태" })}</Label>
            <Select value={inputs.status} onValueChange={(v) => setInputs((f) => ({ ...f, status: v }))}>
              <SelectTrigger className="w-36">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value={ALL}>{t("myEsmRequests.filterAll", { defaultValue: "전체" })}</SelectItem>
                {STATUS_OPTIONS.map((s) => (
                  <SelectItem key={s} value={s}>
                    {requestStatusLabel(t, s)}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
          <div className="space-y-1">
            <Label htmlFor="from">{t("myEsmRequests.filterFrom", { defaultValue: "시작일" })}</Label>
            <Input id="from" type="date" value={inputs.from} onChange={(e) => setInputs((f) => ({ ...f, from: e.target.value }))} />
          </div>
          <div className="space-y-1">
            <Label htmlFor="to">{t("myEsmRequests.filterTo", { defaultValue: "종료일" })}</Label>
            <Input id="to" type="date" value={inputs.to} onChange={(e) => setInputs((f) => ({ ...f, to: e.target.value }))} />
          </div>
          <Button type="submit">{t("myEsmRequests.searchButton", { defaultValue: "검색" })}</Button>
        </form>
      }
    >
      <DataTable
        columns={columns}
        data={data?.content ?? []}
        rowKey={(r) => r.id}
        loading={loading}
        onRowClick={(r) => navigate(`/esm/requests/${r.id}`)}
        emptyTitle={t("myEsmRequests.emptyTitle", { defaultValue: "요청이 없습니다" })}
        emptyDescription={t("myEsmRequests.emptyDescription", { defaultValue: "조건에 맞는 요청이 없습니다." })}
      />
      <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />
    </TicketListLayout>
  );
}
