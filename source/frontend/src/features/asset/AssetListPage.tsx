import { type FormEvent, useEffect, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
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
import { assetApi } from "@/features/asset/api";
import { formatDate } from "@/features/asset/format";
import {
  ASSET_STATUSES,
  ASSET_TYPES,
  expiryLabel,
  expiryTone,
  statusLabel,
  statusTone,
  typeLabel,
  typeTone,
} from "@/features/asset/status";
import type {
  AssetStatus,
  AssetSummary,
  AssetType,
  PageResponse,
} from "@/features/asset/types";
import { extractErrorMessage } from "@/lib/apiClient";

/*
 * 자산 목록(SCR-ITAM-001) — 공통 목록/필터 패턴(SCR-COM-007).
 * 필터(유형·상태·소유자·만료 임박·기간) / 표(식별키·이름·유형·상태·소유자·만료일).
 */
const PAGE_SIZE = 10;
const ALL = "ALL";

interface Filters {
  type: string;
  status: string;
  owner: string;
  expiringWithinDays: string;
}

const EMPTY: Filters = { type: ALL, status: ALL, owner: "", expiringWithinDays: "" };

export function AssetListPage() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  // 알림벨(만료 임박) 진입 시 URL 쿼리의 expiringWithinDays로 초기 필터를 세팅한다(최초 진입 1회만, 이후 동기화 없음).
  const initialFilters: Filters = { ...EMPTY, expiringWithinDays: searchParams.get("expiringWithinDays") ?? "" };
  const [inputs, setInputs] = useState<Filters>(initialFilters);
  const [applied, setApplied] = useState<Filters>(initialFilters);
  const [page, setPage] = useState(0);
  const [data, setData] = useState<PageResponse<AssetSummary> | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let active = true;
    setLoading(true);
    assetApi
      .list({
        type: applied.type === ALL ? undefined : (applied.type as AssetType),
        status: applied.status === ALL ? undefined : (applied.status as AssetStatus),
        owner: applied.owner || undefined,
        expiringWithinDays: applied.expiringWithinDays ? Number(applied.expiringWithinDays) : undefined,
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

  const columns: Column<AssetSummary>[] = [
    { header: "식별키", cell: (a) => a.assetKey },
    { header: "이름", cell: (a) => <span className="line-clamp-1">{a.name}</span> },
    { header: "유형", cell: (a) => <StatusBadge tone={typeTone(a.type)} label={typeLabel(a.type)} /> },
    { header: "상태", cell: (a) => <StatusBadge tone={statusTone(a.status)} label={statusLabel(a.status)} /> },
    { header: "소유자", cell: (a) => a.owner || "미지정" },
    {
      header: "만료일",
      cell: (a) => (
        <span className="flex items-center gap-1.5">
          {formatDate(a.expiryDate)}
          {a.expiryStatus !== "OK" ? (
            <StatusBadge tone={expiryTone(a.expiryStatus)} label={expiryLabel(a.expiryStatus)} />
          ) : null}
        </span>
      ),
    },
  ];

  const totalPages = data ? Math.ceil(data.totalElements / PAGE_SIZE) : 0;

  return (
    <TicketListLayout
      title="자산"
      actions={
        <Button onClick={() => navigate("/assets/new")}>
          <Plus />
          자산 등록
        </Button>
      }
      filters={
        <form onSubmit={handleSearch} className="flex flex-wrap items-end gap-2">
          <div className="space-y-1">
            <Label>유형</Label>
            <Select value={inputs.type} onValueChange={(v) => setInputs((f) => ({ ...f, type: v }))}>
              <SelectTrigger className="w-32"><SelectValue /></SelectTrigger>
              <SelectContent>
                <SelectItem value={ALL}>전체</SelectItem>
                {ASSET_TYPES.map((t) => (
                  <SelectItem key={t} value={t}>{typeLabel(t)}</SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
          <div className="space-y-1">
            <Label>상태</Label>
            <Select value={inputs.status} onValueChange={(v) => setInputs((f) => ({ ...f, status: v }))}>
              <SelectTrigger className="w-32"><SelectValue /></SelectTrigger>
              <SelectContent>
                <SelectItem value={ALL}>전체</SelectItem>
                {ASSET_STATUSES.map((s) => (
                  <SelectItem key={s} value={s}>{statusLabel(s)}</SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
          <div className="space-y-1">
            <Label htmlFor="owner">소유자</Label>
            <Input id="owner" className="w-32" value={inputs.owner} onChange={(e) => setInputs((f) => ({ ...f, owner: e.target.value }))} />
          </div>
          <div className="space-y-1">
            <Label htmlFor="expiring">만료 임박(일)</Label>
            <Input id="expiring" type="number" className="w-28" value={inputs.expiringWithinDays} onChange={(e) => setInputs((f) => ({ ...f, expiringWithinDays: e.target.value }))} />
          </div>
          <Button type="submit">검색</Button>
        </form>
      }
    >
      <DataTable
        columns={columns}
        data={data?.content ?? []}
        rowKey={(a) => a.id}
        loading={loading}
        onRowClick={(a) => navigate(`/assets/${a.id}`)}
        emptyTitle="자산이 없습니다"
        emptyDescription="조건에 맞는 자산이 없습니다."
      />
      <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />
    </TicketListLayout>
  );
}
