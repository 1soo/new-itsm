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
  toast,
} from "@/components/common";
import { Badge } from "@/components/ui/badge";
import { adminApi } from "@/features/admin/api";
import type {
  PageResponse,
  Role,
  UserSummary,
} from "@/features/admin/types";
import type { UserStatus } from "@/features/auth/types";
import { extractErrorMessage } from "@/lib/apiClient";

/*
 * 계정 목록(SCR-ADMIN-001) — 공통 목록/필터 패턴(SCR-COM-007).
 * 필터(이메일·이름·상태·역할) → 표(이름·이메일·역할·상태·생성일) → 페이지네이션.
 * 행 클릭 시 상세 이동. "계정 생성" 버튼으로 생성 화면 이동.
 */
const PAGE_SIZE = 10;
const ALL = "ALL";

interface Filters {
  email: string;
  name: string;
  status: string;
  role: string;
}

const EMPTY_FILTERS: Filters = { email: "", name: "", status: ALL, role: ALL };

function formatDate(iso: string): string {
  const d = new Date(iso);
  return Number.isNaN(d.getTime()) ? iso : d.toLocaleDateString("ko-KR");
}

export function UserListPage() {
  const navigate = useNavigate();

  const [inputs, setInputs] = useState<Filters>(EMPTY_FILTERS);
  const [applied, setApplied] = useState<Filters>(EMPTY_FILTERS);
  const [page, setPage] = useState(0);

  const [data, setData] = useState<PageResponse<UserSummary> | null>(null);
  const [loading, setLoading] = useState(true);
  const [roles, setRoles] = useState<Role[]>([]);

  useEffect(() => {
    adminApi
      .listRoles()
      .then(setRoles)
      .catch((err) => toast.error(extractErrorMessage(err)));
  }, []);

  useEffect(() => {
    let active = true;
    setLoading(true);
    adminApi
      .listUsers({
        email: applied.email || undefined,
        name: applied.name || undefined,
        status: applied.status === ALL ? undefined : (applied.status as UserStatus),
        role: applied.role === ALL ? undefined : applied.role,
        page,
        size: PAGE_SIZE,
      })
      .then((res) => {
        if (active) setData(res);
      })
      .catch((err) => {
        if (active) toast.error(extractErrorMessage(err));
      })
      .finally(() => {
        if (active) setLoading(false);
      });
    return () => {
      active = false;
    };
  }, [applied, page]);

  const handleSearch = (e: FormEvent) => {
    e.preventDefault();
    setPage(0);
    setApplied(inputs);
  };

  const columns: Column<UserSummary>[] = [
    { header: "이름", cell: (u) => u.name },
    { header: "이메일", cell: (u) => u.email },
    {
      header: "역할",
      cell: (u) => (
        <span className="flex flex-wrap gap-1">
          {u.roles.map((r) => (
            <Badge key={r} variant="info">
              {r}
            </Badge>
          ))}
        </span>
      ),
    },
    {
      header: "상태",
      cell: (u) => (
        <StatusBadge
          tone={u.status === "ACTIVE" ? "success" : "warning"}
          label={u.status === "ACTIVE" ? "활성" : "비활성"}
        />
      ),
    },
    { header: "생성일", cell: (u) => formatDate(u.createdAt) },
  ];

  const totalPages = data ? Math.ceil(data.totalElements / PAGE_SIZE) : 0;

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-semibold text-foreground">계정 관리</h1>
        <Button onClick={() => navigate("/admin/users/new")}>
          <Plus />
          계정 생성
        </Button>
      </div>

      <form
        onSubmit={handleSearch}
        className="grid grid-cols-1 gap-3 rounded-lg border border-border bg-card p-4 sm:grid-cols-2 lg:grid-cols-5"
      >
        <div className="space-y-1">
          <Label htmlFor="f-email">이메일</Label>
          <Input
            id="f-email"
            value={inputs.email}
            onChange={(e) => setInputs((f) => ({ ...f, email: e.target.value }))}
          />
        </div>
        <div className="space-y-1">
          <Label htmlFor="f-name">이름</Label>
          <Input
            id="f-name"
            value={inputs.name}
            onChange={(e) => setInputs((f) => ({ ...f, name: e.target.value }))}
          />
        </div>
        <div className="space-y-1">
          <Label>상태</Label>
          <Select
            value={inputs.status}
            onValueChange={(v) => setInputs((f) => ({ ...f, status: v }))}
          >
            <SelectTrigger>
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value={ALL}>전체</SelectItem>
              <SelectItem value="ACTIVE">활성</SelectItem>
              <SelectItem value="INACTIVE">비활성</SelectItem>
            </SelectContent>
          </Select>
        </div>
        <div className="space-y-1">
          <Label>역할</Label>
          <Select
            value={inputs.role}
            onValueChange={(v) => setInputs((f) => ({ ...f, role: v }))}
          >
            <SelectTrigger>
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value={ALL}>전체</SelectItem>
              {roles.map((r) => (
                <SelectItem key={r.id} value={r.roleCode}>
                  {r.name}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>
        <div className="flex items-end">
          <Button type="submit" className="w-full">
            검색
          </Button>
        </div>
      </form>

      <DataTable
        columns={columns}
        data={data?.content ?? []}
        rowKey={(u) => u.id}
        loading={loading}
        onRowClick={(u) => navigate(`/admin/users/${u.id}`)}
        emptyTitle="계정이 없습니다"
        emptyDescription="검색 조건에 맞는 계정이 없습니다."
      />

      <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />
    </div>
  );
}
