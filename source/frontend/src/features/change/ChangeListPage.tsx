import { type FormEvent, useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
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
const PAGE_SIZE = 10;
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
    { header: "식별키", cell: (c) => c.ticketKey },
    { header: "요약", cell: (c) => <span className="line-clamp-1">{c.summary}</span> },
    { header: "유형", cell: (c) => <StatusBadge tone={typeTone(c.type)} label={typeLabel(c.type)} /> },
    { header: "상태", cell: (c) => <StatusBadge tone={statusTone(c.status)} label={statusLabel(c.status)} /> },
    {
      header: "위험도",
      cell: (c) =>
        c.risk ? (
          <StatusBadge tone={riskTone(c.risk)} label={riskLabel(c.risk)} />
        ) : (
          <span className="text-sm text-muted-foreground">미평가</span>
        ),
    },
    { header: "예정일", cell: (c) => (c.scheduledAt ? formatDate(c.scheduledAt) : "-") },
  ];

  const totalPages = data ? Math.ceil(data.totalElements / PAGE_SIZE) : 0;

  return (
    <TicketListLayout
      title="변경"
      actions={
        <Button onClick={() => navigate("/changes/new")}>
          <Plus />
          변경 요청 생성
        </Button>
      }
      filters={
        <form onSubmit={handleSearch} className="flex flex-wrap items-end gap-2">
          <div className="space-y-1">
            <Label>유형</Label>
            <Select value={inputs.type} onValueChange={(v) => setInputs((f) => ({ ...f, type: v }))}>
              <SelectTrigger className="w-28"><SelectValue /></SelectTrigger>
              <SelectContent>
                <SelectItem value={ALL}>전체</SelectItem>
                {CHANGE_TYPES.map((t) => (
                  <SelectItem key={t} value={t}>{typeLabel(t)}</SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
          <div className="space-y-1">
            <Label>상태</Label>
            <Select value={inputs.status} onValueChange={(v) => setInputs((f) => ({ ...f, status: v }))}>
              <SelectTrigger className="w-36"><SelectValue /></SelectTrigger>
              <SelectContent>
                <SelectItem value={ALL}>전체</SelectItem>
                {CHANGE_STATUSES.map((s) => (
                  <SelectItem key={s} value={s}>{statusLabel(s)}</SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
          <div className="space-y-1">
            <Label>위험도</Label>
            <Select value={inputs.risk} onValueChange={(v) => setInputs((f) => ({ ...f, risk: v }))}>
              <SelectTrigger className="w-28"><SelectValue /></SelectTrigger>
              <SelectContent>
                <SelectItem value={ALL}>전체</SelectItem>
                {RISKS.map((r) => (
                  <SelectItem key={r} value={r}>{riskLabel(r)}</SelectItem>
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
        rowKey={(c) => c.id}
        loading={loading}
        onRowClick={(c) => navigate(`/changes/${c.id}`)}
        emptyTitle="변경이 없습니다"
        emptyDescription="조건에 맞는 변경이 없습니다."
      />
      <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />
    </TicketListLayout>
  );
}
