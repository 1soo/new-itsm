import { useCallback, useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import {
  type Column,
  DataTable,
  Pagination,
  StatusBadge,
  toast,
} from "@/components/common";
import { srmApi } from "@/features/service-request/api";
import { formatDate } from "@/features/service-request/format";
import { slaLabel, slaTone, statusLabel, statusTone } from "@/features/service-request/status";
import type { PageResponse, Queue, RequestSummary } from "@/features/service-request/types";
import { extractErrorMessage } from "@/lib/apiClient";
import { cn } from "@/lib/utils";

/*
 * 요청 큐(SCR-SRM-004) — 좌측 큐 목록(건수 포함) + 우측 요청 표 + 배정.
 * 큐 선택 시 해당 큐 요청으로 필터. "나에게 배정"으로 담당자 갱신(권한 없으면 BE 403).
 * 큐 목록은 API-SRM-016(GET /queues) 사용.
 */
const PAGE_SIZE = 10;
const ALL_QUEUE = "__ALL__";

function QueueButton({
  label,
  count,
  active,
  isDefault,
  onClick,
}: {
  label: string;
  count?: number;
  active: boolean;
  isDefault?: boolean;
  onClick: () => void;
}) {
  return (
    <button
      type="button"
      onClick={onClick}
      className={cn(
        "flex w-full items-center justify-between gap-2 rounded-md px-3 py-2 text-left text-sm transition-colors hover:bg-accent",
        active ? "bg-accent font-medium text-foreground" : "text-muted-foreground",
      )}
    >
      <span className="flex items-center gap-1.5 truncate">
        {label}
        {isDefault ? <Badge variant="outline" className="shrink-0">기본</Badge> : null}
      </span>
      {count != null ? (
        <Badge variant="secondary" className="shrink-0">
          {count}
        </Badge>
      ) : null}
    </button>
  );
}

export function RequestQueuePage() {
  const navigate = useNavigate();
  const [queues, setQueues] = useState<Queue[]>([]);
  const [selected, setSelected] = useState<string>(ALL_QUEUE);
  const [page, setPage] = useState(0);
  const [data, setData] = useState<PageResponse<RequestSummary> | null>(null);
  const [loading, setLoading] = useState(true);
  const [assigningId, setAssigningId] = useState<number | null>(null);

  const loadQueues = useCallback(() => {
    srmApi
      .listQueues()
      .then(setQueues)
      .catch((err) => toast.error(extractErrorMessage(err)));
  }, []);

  useEffect(loadQueues, [loadQueues]);

  const loadRequests = useCallback(() => {
    let active = true;
    setLoading(true);
    srmApi
      .listRequests({
        scope: "all",
        queue: selected === ALL_QUEUE ? undefined : selected,
        page,
        size: PAGE_SIZE,
      })
      .then((res) => active && setData(res))
      .catch((err) => active && toast.error(extractErrorMessage(err)))
      .finally(() => active && setLoading(false));
    return () => {
      active = false;
    };
  }, [selected, page]);

  useEffect(() => loadRequests(), [loadRequests]);

  const selectQueue = (key: string) => {
    setPage(0);
    setSelected(key);
  };

  const handleAssign = async (id: number) => {
    setAssigningId(id);
    try {
      await srmApi.assign(id);
      toast.success("본인에게 배정되었습니다");
      loadRequests();
      loadQueues();
    } catch (err) {
      toast.error(extractErrorMessage(err));
    } finally {
      setAssigningId(null);
    }
  };

  const columns: Column<RequestSummary>[] = [
    { header: "접수번호", cell: (r) => r.ticketKey },
    { header: "유형", cell: (r) => r.catalogItemName },
    { header: "상태", cell: (r) => <StatusBadge tone={statusTone(r.status)} label={statusLabel(r.status)} /> },
    { header: "SLA", cell: (r) => <StatusBadge tone={slaTone(r.slaStatus)} label={slaLabel(r.slaStatus)} /> },
    { header: "담당자", cell: (r) => r.assignee || "미배정" },
    { header: "갱신일", cell: (r) => formatDate(r.updatedAt) },
    {
      header: "",
      className: "text-right",
      cell: (r) => (
        <Button
          size="sm"
          variant="outline"
          loading={assigningId === r.id}
          onClick={(e) => {
            e.stopPropagation();
            handleAssign(r.id);
          }}
        >
          나에게 배정
        </Button>
      ),
    },
  ];

  const totalPages = data ? Math.ceil(data.totalElements / PAGE_SIZE) : 0;

  return (
    <div className="space-y-4">
      <h1 className="text-xl font-semibold text-foreground">요청 큐</h1>

      <div className="grid gap-6 lg:grid-cols-[16rem_minmax(0,1fr)]">
        <aside className="space-y-1 rounded-lg border border-border bg-card p-2">
          <QueueButton
            label="전체"
            active={selected === ALL_QUEUE}
            onClick={() => selectQueue(ALL_QUEUE)}
          />
          {queues.map((q) => (
            <QueueButton
              key={q.id}
              label={q.name}
              count={q.openCount}
              isDefault={q.isDefault}
              active={selected === String(q.id)}
              onClick={() => selectQueue(String(q.id))}
            />
          ))}
        </aside>

        <div className="flex flex-col gap-4">
          <DataTable
            columns={columns}
            data={data?.content ?? []}
            rowKey={(r) => r.id}
            loading={loading}
            onRowClick={(r) => navigate(`/service-requests/${r.id}`)}
            emptyTitle="요청이 없습니다"
            emptyDescription="이 큐에 처리할 요청이 없습니다."
          />
          <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />
        </div>
      </div>
    </div>
  );
}
