import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";

import { Button } from "@/components/ui/button";
import {
  type Column,
  DataTable,
  StatusBadge,
  toast,
} from "@/components/common";
import { FullscreenLoader } from "@/routes/FullscreenLoader";
import { esmApi } from "@/features/esm/api";
import {
  checklistStatusLabel,
  checklistStatusTone,
  checklistTaskStatusLabel,
  checklistTaskStatusTone,
  departmentLabel,
} from "@/features/esm/status";
import type { ChecklistDetail, ChecklistTask } from "@/features/esm/types";
import { extractErrorMessage } from "@/lib/apiClient";

/*
 * 온보딩/오프보딩 체크리스트 상세(SCR-ESM-009) — 조회 전용(API-ESM-014 GET만).
 * 하위 작업 완료 처리는 내 하위 작업 목록(SCR-ESM-010)에서 수행한다.
 */
export function ChecklistDetailPage() {
  const params = useParams();
  const navigate = useNavigate();
  const id = Number(params.id);

  const [detail, setDetail] = useState<ChecklistDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [notFound, setNotFound] = useState(false);

  useEffect(() => {
    setLoading(true);
    esmApi
      .getChecklist(id)
      .then((d) => {
        setDetail(d);
        setNotFound(false);
      })
      .catch((err) => {
        toast.error(extractErrorMessage(err));
        setNotFound(true);
      })
      .finally(() => setLoading(false));
  }, [id]);

  if (loading) return <FullscreenLoader />;
  if (notFound || !detail) {
    return (
      <div className="mx-auto max-w-lg space-y-4 text-center">
        <p className="text-sm text-muted-foreground">체크리스트를 찾을 수 없습니다.</p>
        <Button onClick={() => navigate(-1)}>이전으로</Button>
      </div>
    );
  }

  const doneCount = detail.tasks.filter((t) => t.status === "DONE").length;

  const columns: Column<ChecklistTask>[] = [
    { header: "담당 부서", cell: (t) => <StatusBadge tone="info" label={departmentLabel(t.department)} /> },
    { header: "설명", cell: (t) => t.description },
    {
      header: "상태",
      cell: (t) => <StatusBadge tone={checklistTaskStatusTone(t.status)} label={checklistTaskStatusLabel(t.status)} />,
    },
    {
      header: "회수 자산",
      cell: (t) =>
        t.relatedAssetKey ? (
          <button
            type="button"
            className="text-primary hover:underline"
            onClick={() => navigate(`/assets/${t.relatedAssetId}`)}
          >
            {t.relatedAssetKey}
          </button>
        ) : (
          "-"
        ),
    },
  ];

  return (
    <div className="space-y-4">
      <div className="flex flex-wrap items-start justify-between gap-3 border-b border-border pb-4">
        <div className="space-y-1.5">
          <p className="text-xs font-medium text-muted-foreground">
            {detail.type === "ONBOARDING" ? "온보딩" : "오프보딩"} 체크리스트
          </p>
          <h1 className="text-heading-large font-bold text-foreground">{detail.targetUserName}</h1>
          <div className="flex flex-wrap items-center gap-2">
            <StatusBadge tone={checklistStatusTone(detail.status)} label={checklistStatusLabel(detail.status)} />
            <span className="text-sm text-muted-foreground">
              전체 진행률 {doneCount} / {detail.tasks.length}
            </span>
          </div>
        </div>
      </div>

      <DataTable
        columns={columns}
        data={detail.tasks}
        rowKey={(t) => t.id}
        emptyTitle="하위 작업이 없습니다"
        emptyDescription="정의된 하위 작업이 없습니다."
      />
    </div>
  );
}
