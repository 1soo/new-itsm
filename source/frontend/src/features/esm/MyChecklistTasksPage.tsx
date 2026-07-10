import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

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
import { checklistTaskStatusLabel, checklistTaskStatusTone } from "@/features/esm/status";
import type { ChecklistTaskStatus, MyChecklistTask, PageResponse } from "@/features/esm/types";
import { extractErrorMessage } from "@/lib/apiClient";

/*
 * 내 하위 작업 목록(SCR-ESM-010) — 본인 소속 부서에 배정된 체크리스트 하위 작업(scope=mine).
 * 대기 상태만 "완료 처리" 버튼 노출.
 */
const PAGE_SIZE = 10;
const ALL = "ALL";

const STATUS_OPTIONS: ChecklistTaskStatus[] = ["PENDING", "DONE"];

export function MyChecklistTasksPage() {
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
      toast.success("완료 처리되었습니다");
      load();
    } catch (err) {
      toast.error(extractErrorMessage(err));
    } finally {
      setCompletingId(null);
    }
  };

  const columns: Column<MyChecklistTask>[] = [
    { header: "체크리스트 유형", cell: (t) => (t.checklistType === "ONBOARDING" ? "온보딩" : "오프보딩") },
    { header: "대상자", cell: (t) => t.targetUserName },
    { header: "작업 설명", cell: (t) => t.description },
    {
      header: "상태",
      cell: (t) => <StatusBadge tone={checklistTaskStatusTone(t.status)} label={checklistTaskStatusLabel(t.status)} />,
    },
    {
      header: "",
      className: "text-right",
      cell: (t) =>
        t.status === "PENDING" ? (
          <Button
            size="sm"
            loading={completingId === t.id}
            onClick={(e) => {
              e.stopPropagation();
              handleComplete(t.id);
            }}
          >
            완료 처리
          </Button>
        ) : null,
    },
  ];

  const totalPages = data ? Math.ceil(data.totalElements / PAGE_SIZE) : 0;

  return (
    <TicketListLayout
      title="내 하위 작업"
      description="소속 부서에 배정된 온보딩/오프보딩 하위 작업을 처리합니다."
      filters={
        <div className="space-y-1">
          <Label>상태</Label>
          <Select value={status} onValueChange={(v) => { setPage(0); setStatus(v); }}>
            <SelectTrigger className="w-32">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value={ALL}>전체</SelectItem>
              {STATUS_OPTIONS.map((s) => (
                <SelectItem key={s} value={s}>
                  {checklistTaskStatusLabel(s)}
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
        rowKey={(t) => t.id}
        loading={loading}
        onRowClick={(t) => navigate(`/esm/checklists/${t.checklistId}`)}
        emptyTitle="하위 작업이 없습니다"
        emptyDescription="배정된 하위 작업이 없습니다."
      />
      <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />
    </TicketListLayout>
  );
}
