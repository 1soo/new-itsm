import { useCallback, useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";
import type { TFunction } from "i18next";

import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import {
  type Column,
  DataTable,
  Modal,
  Pagination,
  StatusBadge,
  toast,
} from "@/components/common";
import { srmApi } from "@/features/service-request/api";
import { formatDate } from "@/features/service-request/format";
import { slaLabel, slaTone, statusLabel, statusTone } from "@/features/service-request/status";
import type {
  AssigneeCandidate,
  PageResponse,
  Queue,
  RequestSummary,
} from "@/features/service-request/types";
import { extractErrorMessage } from "@/lib/apiClient";
import { useAppSelector } from "@/store/hooks";
import { cn } from "@/lib/utils";

/*
 * 요청 큐(SCR-SRM-004) — 좌측 큐 목록(건수 포함) + 우측 요청 표 + 배정.
 * 큐 선택 시 해당 큐 요청으로 필터. "나에게 배정"으로 담당자 갱신(권한 없으면 BE 403).
 * 큐 목록은 API-SRM-016(GET /queues) 사용.
 */
const PAGE_SIZE = 16;
const ALL_QUEUE = "__ALL__";
/** 라우팅(ROUTED) 이후 상태 — 배정 버튼 노출 조건 판정용(2026-07-15 유지보수 요청). */
const POST_ROUTING_STATUSES = new Set(["ROUTED", "IN_FULFILLMENT", "FULFILLED", "CLOSED"]);

function canAssign(r: RequestSummary, myId?: number): boolean {
  if (myId != null && r.assigneeId === myId) return false;
  return !POST_ROUTING_STATUSES.has(r.status);
}

function QueueButton({
  t,
  label,
  count,
  active,
  isDefault,
  onClick,
}: {
  t: TFunction;
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
        {isDefault ? (
          <Badge variant="outline" className="shrink-0">
            {t("requestQueue.defaultBadge", { defaultValue: "기본" })}
          </Badge>
        ) : null}
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
  const { t } = useTranslation("service-request");
  const navigate = useNavigate();
  const user = useAppSelector((s) => s.auth.user);
  const [queues, setQueues] = useState<Queue[]>([]);
  const [selected, setSelected] = useState<string>(ALL_QUEUE);
  const [page, setPage] = useState(0);
  const [data, setData] = useState<PageResponse<RequestSummary> | null>(null);
  const [loading, setLoading] = useState(true);

  const [assignTarget, setAssignTarget] = useState<RequestSummary | null>(null);
  const [candidates, setCandidates] = useState<AssigneeCandidate[]>([]);
  const [candidatesLoading, setCandidatesLoading] = useState(false);
  const [assigning, setAssigning] = useState(false);

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

  const openAssignModal = async (r: RequestSummary) => {
    setAssignTarget(r);
    setCandidates([]);
    setCandidatesLoading(true);
    try {
      const list = await srmApi.getAssigneeCandidates(r.id);
      setCandidates(list);
    } catch (err) {
      toast.error(extractErrorMessage(err));
    } finally {
      setCandidatesLoading(false);
    }
  };

  const closeAssignModal = () => {
    setAssignTarget(null);
    setCandidates([]);
  };

  const confirmAssign = async (assigneeId?: number) => {
    if (!assignTarget) return;
    setAssigning(true);
    try {
      await srmApi.assign(assignTarget.id, assigneeId);
      toast.success(t("requestQueue.assignSuccess", { defaultValue: "담당자가 배정되었습니다" }));
      closeAssignModal();
      loadRequests();
      loadQueues();
    } catch (err) {
      toast.error(extractErrorMessage(err));
    } finally {
      setAssigning(false);
    }
  };

  const canSelfAssign =
    candidates.length === 0 || (user != null && candidates.some((c) => c.id === user.id));

  const columns: Column<RequestSummary>[] = [
    { header: t("requestList.columnTicketKey", { defaultValue: "접수번호" }), width: 130, cell: (r) => r.ticketKey },
    {
      header: t("requestList.columnCatalogItem", { defaultValue: "유형" }),
      cell: (r) => <span className="truncate block">{r.catalogItemName}</span>,
    },
    {
      header: t("requestList.columnStatus", { defaultValue: "상태" }),
      width: 110,
      cell: (r) => <StatusBadge tone={statusTone(r.status)} label={statusLabel(t, r.status)} />,
    },
    {
      header: "SLA",
      width: 110,
      cell: (r) => <StatusBadge tone={slaTone(r.slaStatus)} label={slaLabel(t, r.slaStatus)} />,
    },
    {
      header: t("requestList.columnAssignee", { defaultValue: "담당자" }),
      width: 120,
      cell: (r) => r.assignee || t("requestQueue.unassigned", { defaultValue: "미배정" }),
    },
    { header: t("requestList.columnUpdatedAt", { defaultValue: "갱신일" }), width: 110, cell: (r) => formatDate(r.updatedAt) },
    {
      header: "",
      width: 140,
      className: "text-right",
      cell: (r) =>
        canAssign(r, user?.id) ? (
          <Button
            size="sm"
            variant="outline"
            onClick={(e) => {
              e.stopPropagation();
              openAssignModal(r);
            }}
          >
            {t("requestQueue.assign", { defaultValue: "배정" })}
          </Button>
        ) : null,
    },
  ];

  const totalPages = data ? Math.ceil(data.totalElements / PAGE_SIZE) : 0;

  return (
    <div className="space-y-4">
      <h1 className="text-xl font-semibold text-foreground">
        {t("requestQueue.title", { defaultValue: "요청 큐" })}
      </h1>

      <div className="grid gap-6 lg:grid-cols-[16rem_minmax(0,1fr)]">
        <aside className="space-y-1 rounded-lg border border-border bg-card p-2">
          <QueueButton
            t={t}
            label={t("requestQueue.allQueues", { defaultValue: "전체" })}
            active={selected === ALL_QUEUE}
            onClick={() => selectQueue(ALL_QUEUE)}
          />
          {queues.map((q) => (
            <QueueButton
              key={q.id}
              t={t}
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
            emptyTitle={t("requestQueue.emptyTitle", { defaultValue: "요청이 없습니다" })}
            emptyDescription={t("requestQueue.emptyDescription", {
              defaultValue: "이 큐에 처리할 요청이 없습니다.",
            })}
          />
          <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />
        </div>
      </div>

      <Modal
        open={assignTarget != null}
        onOpenChange={(open) => !open && closeAssignModal()}
        title={t("requestQueue.assignModalTitle", { defaultValue: "담당자 배정" })}
        description={assignTarget ? `${assignTarget.ticketKey} · ${assignTarget.catalogItemName}` : undefined}
      >
        {candidatesLoading ? (
          <p className="text-sm text-muted-foreground">
            {t("requestQueue.assignCandidatesLoading", { defaultValue: "후보를 불러오는 중..." })}
          </p>
        ) : (
          <div className="space-y-3">
            {candidates.length === 0 ? (
              <p className="text-sm text-muted-foreground">
                {t("requestQueue.assignNoCandidates", {
                  defaultValue: "지정된 담당자 역할이 없습니다.",
                })}
              </p>
            ) : (
              <ul className="space-y-1">
                {candidates.map((c) => (
                  <li key={c.id}>
                    <button
                      type="button"
                      disabled={assigning}
                      onClick={() => confirmAssign(c.id)}
                      className="w-full rounded-md px-3 py-2 text-left text-sm text-foreground transition-colors hover:bg-accent disabled:pointer-events-none disabled:opacity-50"
                    >
                      {c.name}
                    </button>
                  </li>
                ))}
              </ul>
            )}
            {canSelfAssign ? (
              <Button
                variant="outline"
                className="w-full"
                loading={assigning}
                onClick={() => confirmAssign(undefined)}
              >
                {t("requestQueue.assignToMe", { defaultValue: "나에게 배정" })}
              </Button>
            ) : null}
          </div>
        )}
      </Modal>
    </div>
  );
}
