import { type FormEvent, useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";

import { Button } from "@/components/ui/button";
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
  deriveApprovalStatusDisplay,
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
 * 부서 요청 처리 큐(SCR-ESM-004) — 담당 부서 처리자(DEPT_COORDINATOR)용.
 * 사용자당 소속 부서가 하나뿐이라(department 컬럼) 별도 부서 선택 UI 없이
 * scope=all로 조회하면 BE가 로그인 사용자의 department로 강제 스코프한다.
 */
const PAGE_SIZE = 13;
const ALL = "ALL";

const STATUS_OPTIONS: EsmRequestStatus[] = ["SUBMITTED", "IN_PROGRESS", "COMPLETED", "REJECTED"];

export function EsmRequestQueuePage() {
  const { t } = useTranslation("esm");
  const navigate = useNavigate();
  const [statusInput, setStatusInput] = useState(ALL);
  const [status, setStatus] = useState(ALL);
  const [page, setPage] = useState(0);
  const [data, setData] = useState<PageResponse<EsmRequestSummary> | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let active = true;
    setLoading(true);
    esmApi
      .listRequests({
        scope: "all",
        status: status === ALL ? undefined : (status as EsmRequestStatus),
        page,
        size: PAGE_SIZE,
      })
      .then((res) => active && setData(res))
      .catch((err) => active && toast.error(extractErrorMessage(err)))
      .finally(() => active && setLoading(false));
    return () => {
      active = false;
    };
  }, [status, page]);

  const handleSearch = (e: FormEvent) => {
    e.preventDefault();
    setPage(0);
    setStatus(statusInput);
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
      cell: (r) => {
        const display = deriveApprovalStatusDisplay(
          t,
          { tone: requestStatusTone(r.status), label: requestStatusLabel(t, r.status) },
          {
            status: r.pendingApprovalTargetState ? "IN_PROGRESS" : null,
            targetStateLabel: r.pendingApprovalTargetState ? requestStatusLabel(t, r.pendingApprovalTargetState) : null,
          },
        );
        return <StatusBadge tone={display.tone} label={display.label} />;
      },
    },
    { header: t("myEsmRequests.columnUpdatedAt", { defaultValue: "갱신일" }), width: 110, cell: (r) => formatDate(r.updatedAt) },
  ];

  const totalPages = data ? Math.ceil(data.totalElements / PAGE_SIZE) : 0;

  return (
    <TicketListLayout
      title={t("esmRequestQueue.title", { defaultValue: "부서 요청 처리 큐" })}
      description={t("esmRequestQueue.description", { defaultValue: "소속 부서로 접수된 요청을 처리합니다." })}
      filters={
        <form onSubmit={handleSearch} className="flex flex-wrap items-end gap-2">
          <div className="space-y-1">
            <Label>{t("myEsmRequests.columnStatus", { defaultValue: "상태" })}</Label>
            <Select value={statusInput} onValueChange={setStatusInput}>
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
        emptyTitle={t("esmRequestQueue.emptyTitle", { defaultValue: "요청이 없습니다" })}
        emptyDescription={t("esmRequestQueue.emptyDescription", { defaultValue: "처리할 부서 요청이 없습니다." })}
      />
      <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />
    </TicketListLayout>
  );
}
