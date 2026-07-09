import { type FormEvent, useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";

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
import { hasAnyRole, ROLE_SERVICE_DESK_AGENT } from "@/features/auth/roles";
import { srmApi } from "@/features/service-request/api";
import { formatDate } from "@/features/service-request/format";
import {
  slaLabel,
  slaTone,
  statusLabel,
  statusTone,
} from "@/features/service-request/status";
import type {
  PageResponse,
  RequestSummary,
  SrStatus,
} from "@/features/service-request/types";
import { useAppSelector } from "@/store/hooks";
import { extractErrorMessage } from "@/lib/apiClient";

/*
 * 요청 목록(SCR-SRM-003) — 공통 목록/필터 패턴(SCR-COM-007).
 * END_USER는 본인 요청(scope=mine), SERVICE_DESK_AGENT는 전체(scope=all)를 조회한다.
 */
const PAGE_SIZE = 10;
const ALL = "ALL";

const STATUS_OPTIONS: SrStatus[] = [
  "SUBMITTED",
  "VALIDATED",
  "ROUTED",
  "APPROVAL_PENDING",
  "IN_FULFILLMENT",
  "FULFILLED",
  "CLOSED",
  "REJECTED",
];

interface Filters {
  status: string;
  from: string;
  to: string;
}

const EMPTY: Filters = { status: ALL, from: "", to: "" };

export function RequestListPage() {
  const navigate = useNavigate();
  const roles = useAppSelector((s) => s.auth.user?.roles);
  const isAgent = hasAnyRole(roles, [ROLE_SERVICE_DESK_AGENT]);
  const scope = isAgent ? "all" : "mine";

  const [inputs, setInputs] = useState<Filters>(EMPTY);
  const [applied, setApplied] = useState<Filters>(EMPTY);
  const [page, setPage] = useState(0);
  const [data, setData] = useState<PageResponse<RequestSummary> | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let active = true;
    setLoading(true);
    srmApi
      .listRequests({
        scope,
        status: applied.status === ALL ? undefined : (applied.status as SrStatus),
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
  }, [scope, applied, page]);

  const handleSearch = (e: FormEvent) => {
    e.preventDefault();
    setPage(0);
    setApplied(inputs);
  };

  const columns: Column<RequestSummary>[] = useMemo(
    () => [
      { header: "접수번호", cell: (r) => r.ticketKey },
      { header: "유형", cell: (r) => r.catalogItemName },
      {
        header: "상태",
        cell: (r) => <StatusBadge tone={statusTone(r.status)} label={statusLabel(r.status)} />,
      },
      {
        header: "SLA",
        cell: (r) => <StatusBadge tone={slaTone(r.slaStatus)} label={slaLabel(r.slaStatus)} />,
      },
      { header: "담당자", cell: (r) => r.assignee || "-" },
      { header: "갱신일", cell: (r) => formatDate(r.updatedAt) },
    ],
    [],
  );

  const totalPages = data ? Math.ceil(data.totalElements / PAGE_SIZE) : 0;

  return (
    <TicketListLayout
      title={isAgent ? "요청 목록" : "내 요청"}
      description={isAgent ? "전체 서비스 요청을 조회합니다." : "본인이 제출한 요청을 추적합니다."}
      filters={
        <form onSubmit={handleSearch} className="flex flex-wrap items-end gap-2">
          <div className="space-y-1">
            <Label>상태</Label>
            <Select value={inputs.status} onValueChange={(v) => setInputs((f) => ({ ...f, status: v }))}>
              <SelectTrigger className="w-40">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value={ALL}>전체</SelectItem>
                {STATUS_OPTIONS.map((s) => (
                  <SelectItem key={s} value={s}>
                    {statusLabel(s)}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
          <div className="space-y-1">
            <Label htmlFor="from">시작일</Label>
            <Input id="from" type="date" value={inputs.from} onChange={(e) => setInputs((f) => ({ ...f, from: e.target.value }))} />
          </div>
          <div className="space-y-1">
            <Label htmlFor="to">종료일</Label>
            <Input id="to" type="date" value={inputs.to} onChange={(e) => setInputs((f) => ({ ...f, to: e.target.value }))} />
          </div>
          <Button type="submit">검색</Button>
        </form>
      }
    >
      <DataTable
        columns={columns}
        data={data?.content ?? []}
        rowKey={(r) => r.id}
        loading={loading}
        onRowClick={(r) => navigate(`/service-requests/${r.id}`)}
        emptyTitle="요청이 없습니다"
        emptyDescription="조건에 맞는 요청이 없습니다."
      />
      <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />
    </TicketListLayout>
  );
}
