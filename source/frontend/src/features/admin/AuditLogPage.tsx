import { type FormEvent, useEffect, useState } from "react";
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
  toast,
} from "@/components/common";
import { adminApi } from "@/features/admin/api";
import type {
  AuditEventType,
  AuditLog,
  PageResponse,
} from "@/features/admin/types";
import { extractErrorMessage } from "@/lib/apiClient";

/*
 * 감사 로그 조회(SCR-ADMIN-005) — 인증·계정/역할 변경 이벤트 이력.
 * 필터(이벤트 유형·주체·대상·기간) → 표(시각·이벤트·주체·대상·결과) → 페이지네이션. 읽기 전용.
 */
const PAGE_SIZE = 14;
const ALL = "ALL";

const EVENT_LABELS: Record<AuditEventType, string> = {
  LOGIN: "로그인",
  LOGOUT: "로그아웃",
  REFRESH: "토큰 재발급",
  USER_CHANGE: "계정 변경",
  ROLE_CHANGE: "역할 변경",
};

interface Filters {
  eventType: string;
  actor: string;
  target: string;
  from: string;
  to: string;
}

const EMPTY_FILTERS: Filters = {
  eventType: ALL,
  actor: "",
  target: "",
  from: "",
  to: "",
};

function formatDateTime(iso: string): string {
  const d = new Date(iso);
  return Number.isNaN(d.getTime()) ? iso : d.toLocaleString("ko-KR");
}

export function AuditLogPage() {
  const { t } = useTranslation("auth");
  const [inputs, setInputs] = useState<Filters>(EMPTY_FILTERS);
  const [applied, setApplied] = useState<Filters>(EMPTY_FILTERS);
  const [page, setPage] = useState(0);
  const [data, setData] = useState<PageResponse<AuditLog> | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let active = true;
    setLoading(true);
    adminApi
      .listAuditLogs({
        eventType:
          applied.eventType === ALL ? undefined : (applied.eventType as AuditEventType),
        actor: applied.actor || undefined,
        target: applied.target || undefined,
        from: applied.from || undefined,
        to: applied.to || undefined,
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

  const eventLabel = (eventType: AuditEventType): string =>
    t(`admin.auditLog.eventLabel.${eventType}`, {
      defaultValue: EVENT_LABELS[eventType] ?? eventType,
    });

  const columns: Column<AuditLog>[] = [
    { header: t("admin.auditLog.columnOccurredAt", { defaultValue: "시각" }), width: 160, cell: (l) => formatDateTime(l.occurredAt) },
    { header: t("admin.auditLog.columnEvent", { defaultValue: "이벤트" }), width: 130, cell: (l) => eventLabel(l.eventType) },
    {
      header: t("admin.auditLog.columnActor", { defaultValue: "주체" }),
      width: 160,
      cell: (l) => <span className="truncate block">{l.actor}</span>,
    },
    {
      header: t("admin.auditLog.columnTarget", { defaultValue: "대상" }),
      cell: (l) => <span className="truncate block">{l.target}</span>,
    },
    {
      header: t("admin.auditLog.columnResult", { defaultValue: "결과" }),
      width: 100,
      cell: (l) => (
        <StatusBadge
          tone={l.result === "SUCCESS" ? "success" : "danger"}
          label={
            l.result === "SUCCESS"
              ? t("admin.auditLog.resultSuccess", { defaultValue: "성공" })
              : t("admin.auditLog.resultFailure", { defaultValue: "실패" })
          }
        />
      ),
    },
  ];

  const totalPages = data ? Math.ceil(data.totalElements / PAGE_SIZE) : 0;

  return (
    <div className="space-y-4">
      <h1 className="text-xl font-semibold text-foreground">
        {t("admin.auditLog.title", { defaultValue: "감사 로그" })}
      </h1>

      <form
        onSubmit={handleSearch}
        className="grid grid-cols-1 gap-3 rounded-lg border border-border bg-card p-4 sm:grid-cols-2 lg:grid-cols-6"
      >
        <div className="space-y-1">
          <Label>{t("admin.auditLog.filterEventType", { defaultValue: "이벤트 유형" })}</Label>
          <Select
            value={inputs.eventType}
            onValueChange={(v) => setInputs((f) => ({ ...f, eventType: v }))}
          >
            <SelectTrigger>
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value={ALL}>{t("admin.auditLog.filterAll", { defaultValue: "전체" })}</SelectItem>
              {(Object.keys(EVENT_LABELS) as AuditEventType[]).map((k) => (
                <SelectItem key={k} value={k}>
                  {eventLabel(k)}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>
        <div className="space-y-1">
          <Label htmlFor="a-actor">{t("admin.auditLog.filterActor", { defaultValue: "주체" })}</Label>
          <Input
            id="a-actor"
            value={inputs.actor}
            onChange={(e) => setInputs((f) => ({ ...f, actor: e.target.value }))}
          />
        </div>
        <div className="space-y-1">
          <Label htmlFor="a-target">{t("admin.auditLog.filterTarget", { defaultValue: "대상" })}</Label>
          <Input
            id="a-target"
            value={inputs.target}
            onChange={(e) => setInputs((f) => ({ ...f, target: e.target.value }))}
          />
        </div>
        <div className="space-y-1">
          <Label htmlFor="a-from">{t("admin.auditLog.filterFrom", { defaultValue: "시작일" })}</Label>
          <Input
            id="a-from"
            type="date"
            value={inputs.from}
            onChange={(e) => setInputs((f) => ({ ...f, from: e.target.value }))}
          />
        </div>
        <div className="space-y-1">
          <Label htmlFor="a-to">{t("admin.auditLog.filterTo", { defaultValue: "종료일" })}</Label>
          <Input
            id="a-to"
            type="date"
            value={inputs.to}
            onChange={(e) => setInputs((f) => ({ ...f, to: e.target.value }))}
          />
        </div>
        <div className="flex items-end">
          <Button type="submit" className="w-full">
            {t("admin.auditLog.searchButton", { defaultValue: "검색" })}
          </Button>
        </div>
      </form>

      <DataTable
        columns={columns}
        data={data?.content ?? []}
        rowKey={(l) => l.id}
        loading={loading}
        emptyTitle={t("admin.auditLog.emptyTitle", { defaultValue: "감사 로그가 없습니다" })}
        emptyDescription={t("admin.auditLog.emptyDescription", {
          defaultValue: "조건에 맞는 이벤트 이력이 없습니다.",
        })}
      />

      <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />
    </div>
  );
}
