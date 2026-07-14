import { useEffect, useState } from "react";
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
  Pagination,
  StatusBadge,
  TicketListLayout,
  toast,
} from "@/components/common";
import { esmApi } from "@/features/esm/api";
import { checklistTaskStatusLabel, checklistTaskStatusTone, checklistTypeLabel } from "@/features/esm/status";
import type { ChecklistTaskStatus, MyChecklistTask, PageResponse } from "@/features/esm/types";
import { extractErrorMessage } from "@/lib/apiClient";

/*
 * 내 하위 작업 목록(SCR-ESM-010) — 본인 소속 부서에 배정된 체크리스트 하위 작업(scope=mine).
 * 대기 상태만 "완료 처리" 버튼 노출.
 */
const PAGE_SIZE = 13;
const ALL = "ALL";

const STATUS_OPTIONS: ChecklistTaskStatus[] = ["PENDING", "DONE"];

export function MyChecklistTasksPage() {
  const { t } = useTranslation("esm");
  const navigate = useNavigate();
  const [status, setStatus] = useState(ALL);
  const [page, setPage] = useState(0);
  const [data, setData] = useState<PageResponse<MyChecklistTask> | null>(null);
  const [loading, setLoading] = useState(true);
  const [completingId, setCompletingId] = useState<number | null>(null);

  const load = () => {
    let active = true;
    setLoading(true);
    esmApi
      .listMyChecklistTasks({ status: status === ALL ? undefined : (status as ChecklistTaskStatus), page, size: PAGE_SIZE })
      .then((res) => active && setData(res))
      .catch((err) => active && toast.error(extractErrorMessage(err)))
      .finally(() => active && setLoading(false));
    return () => {
      active = false;
    };
  };

  useEffect(load, [status, page]);

  const handleComplete = async (taskId: number) => {
    setCompletingId(taskId);
    try {
      await esmApi.completeChecklistTask(taskId);
      toast.success(t("myChecklistTasks.completeSuccess", { defaultValue: "완료 처리되었습니다" }));
      load();
    } catch (err) {
      toast.error(extractErrorMessage(err));
    } finally {
      setCompletingId(null);
    }
  };

  const columns: Column<MyChecklistTask>[] = [
    {
      header: t("myChecklistTasks.columnChecklistType", { defaultValue: "체크리스트 유형" }),
      width: 110,
      cell: (task) => checklistTypeLabel(t, task.checklistType),
    },
    {
      header: t("myChecklistTasks.columnTargetUser", { defaultValue: "대상자" }),
      width: 120,
      cell: (task) => task.targetUserName,
    },
    {
      header: t("myChecklistTasks.columnDescription", { defaultValue: "작업 설명" }),
      cell: (task) => <span className="truncate block">{task.description}</span>,
    },
    {
      header: t("myChecklistTasks.columnStatus", { defaultValue: "상태" }),
      width: 110,
      cell: (task) => <StatusBadge tone={checklistTaskStatusTone(task.status)} label={checklistTaskStatusLabel(t, task.status)} />,
    },
    {
      header: "",
      width: 140,
      className: "text-right",
      cell: (task) =>
        task.status === "PENDING" ? (
          <Button
            size="sm"
            loading={completingId === task.id}
            onClick={(e) => {
              e.stopPropagation();
              handleComplete(task.id);
            }}
          >
            {t("myChecklistTasks.completeButton", { defaultValue: "완료 처리" })}
          </Button>
        ) : null,
    },
  ];

  const totalPages = data ? Math.ceil(data.totalElements / PAGE_SIZE) : 0;

  return (
    <TicketListLayout
      title={t("myChecklistTasks.title", { defaultValue: "내 하위 작업" })}
      description={t("myChecklistTasks.description", { defaultValue: "소속 부서에 배정된 온보딩/오프보딩 하위 작업을 처리합니다." })}
      filters={
        <div className="space-y-1">
          <Label>{t("myChecklistTasks.columnStatus", { defaultValue: "상태" })}</Label>
          <Select value={status} onValueChange={(v) => { setPage(0); setStatus(v); }}>
            <SelectTrigger className="w-32">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value={ALL}>{t("myChecklistTasks.filterAll", { defaultValue: "전체" })}</SelectItem>
              {STATUS_OPTIONS.map((s) => (
                <SelectItem key={s} value={s}>
                  {checklistTaskStatusLabel(t, s)}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>
      }
    >
      <DataTable
        columns={columns}
        data={data?.content ?? []}
        rowKey={(task) => task.id}
        loading={loading}
        onRowClick={(task) => navigate(`/esm/checklists/${task.checklistId}`)}
        emptyTitle={t("myChecklistTasks.emptyTitle", { defaultValue: "하위 작업이 없습니다" })}
        emptyDescription={t("myChecklistTasks.emptyDescription", { defaultValue: "배정된 하위 작업이 없습니다." })}
      />
      <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />
    </TicketListLayout>
  );
}
