import { type FormEvent, useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { Plus, Search } from "lucide-react";

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
  PriorityBadge,
  StatusBadge,
  TicketListLayout,
  toast,
} from "@/components/common";
import { problemApi } from "@/features/problem/api";
import { formatDate } from "@/features/problem/format";
import {
  ORIGINS,
  PROBLEM_PRIORITIES,
  PROBLEM_STATUSES,
  originLabel,
  statusLabel,
  statusTone,
} from "@/features/problem/status";
import type {
  Origin,
  PageResponse,
  ProblemPriority,
  ProblemStatus,
  ProblemSummary,
} from "@/features/problem/types";
import { extractErrorMessage } from "@/lib/apiClient";

/*
 * 문제 목록(SCR-PRB-001) — 공통 목록/필터 패턴(SCR-COM-007).
 * 필터(상태·우선순위·출처·담당자·기간) / 표(식별키·요약·상태·우선순위·출처·담당자·갱신일).
 */
const PAGE_SIZE = 10;
const ALL = "ALL";

interface Filters {
  status: string;
  priority: string;
  origin: string;
  assignee: string;
  from: string;
  to: string;
}

const EMPTY: Filters = { status: ALL, priority: ALL, origin: ALL, assignee: "", from: "", to: "" };

export function ProblemListPage() {
  const navigate = useNavigate();
  const [inputs, setInputs] = useState<Filters>(EMPTY);
  const [applied, setApplied] = useState<Filters>(EMPTY);
  const [page, setPage] = useState(0);
  const [data, setData] = useState<PageResponse<ProblemSummary> | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let active = true;
    setLoading(true);
    problemApi
      .list({
        status: applied.status === ALL ? undefined : (applied.status as ProblemStatus),
        priority: applied.priority === ALL ? undefined : (applied.priority as ProblemPriority),
        origin: applied.origin === ALL ? undefined : (applied.origin as Origin),
        assignee: applied.assignee || undefined,
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

  const columns: Column<ProblemSummary>[] = [
    { header: "식별키", cell: (p) => p.ticketKey },
    { header: "요약", cell: (p) => <span className="line-clamp-1">{p.summary}</span> },
    { header: "상태", cell: (p) => <StatusBadge tone={statusTone(p.status)} label={statusLabel(p.status)} /> },
    {
      header: "우선순위",
      cell: (p) =>
        p.priority ? (
          <PriorityBadge priority={p.priority} />
        ) : (
          <span className="text-sm text-muted-foreground">미산정</span>
        ),
    },
    { header: "출처", cell: (p) => originLabel(p.origin) },
    { header: "담당자", cell: (p) => p.assignee || "미배정" },
    { header: "갱신일", cell: (p) => formatDate(p.updatedAt) },
  ];

  const totalPages = data ? Math.ceil(data.totalElements / PAGE_SIZE) : 0;

  return (
    <TicketListLayout
      title="문제"
      actions={
        <div className="flex gap-2">
          <Button variant="outline" onClick={() => navigate("/known-errors")}>
            <Search />
            KEDB 검색
          </Button>
          <Button onClick={() => navigate("/problems/new")}>
            <Plus />
            문제 등록
          </Button>
        </div>
      }
      filters={
        <form onSubmit={handleSearch} className="flex flex-wrap items-end gap-2">
          <div className="space-y-1">
            <Label>상태</Label>
            <Select value={inputs.status} onValueChange={(v) => setInputs((f) => ({ ...f, status: v }))}>
              <SelectTrigger className="w-36"><SelectValue /></SelectTrigger>
              <SelectContent>
                <SelectItem value={ALL}>전체</SelectItem>
                {PROBLEM_STATUSES.map((s) => (
                  <SelectItem key={s} value={s}>{statusLabel(s)}</SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
          <div className="space-y-1">
            <Label>우선순위</Label>
            <Select value={inputs.priority} onValueChange={(v) => setInputs((f) => ({ ...f, priority: v }))}>
              <SelectTrigger className="w-28"><SelectValue /></SelectTrigger>
              <SelectContent>
                <SelectItem value={ALL}>전체</SelectItem>
                {PROBLEM_PRIORITIES.map((p) => (
                  <SelectItem key={p} value={p}>{p}</SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
          <div className="space-y-1">
            <Label>출처</Label>
            <Select value={inputs.origin} onValueChange={(v) => setInputs((f) => ({ ...f, origin: v }))}>
              <SelectTrigger className="w-28"><SelectValue /></SelectTrigger>
              <SelectContent>
                <SelectItem value={ALL}>전체</SelectItem>
                {ORIGINS.map((o) => (
                  <SelectItem key={o} value={o}>{originLabel(o)}</SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
          <div className="space-y-1">
            <Label htmlFor="assignee">담당자</Label>
            <Input id="assignee" className="w-32" value={inputs.assignee} onChange={(e) => setInputs((f) => ({ ...f, assignee: e.target.value }))} />
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
        rowKey={(p) => p.id}
        loading={loading}
        onRowClick={(p) => navigate(`/problems/${p.id}`)}
        emptyTitle="문제가 없습니다"
        emptyDescription="조건에 맞는 문제가 없습니다."
      />
      <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />
    </TicketListLayout>
  );
}
