import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { useTranslation } from "react-i18next";

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
  checklistTypeLabel,
  departmentLabel,
} from "@/features/esm/status";
import type { ChecklistDetail, ChecklistTask } from "@/features/esm/types";
import { extractErrorMessage } from "@/lib/apiClient";

/*
 * 온보딩/오프보딩 체크리스트 상세(SCR-ESM-009) — 조회 전용(API-ESM-014 GET만).
 * 하위 작업 완료 처리는 내 하위 작업 목록(SCR-ESM-010)에서 수행한다.
 */
export function ChecklistDetailPage() {
  const { t } = useTranslation("esm");
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
        <p className="text-sm text-muted-foreground">{t("checklistDetail.notFound", { defaultValue: "체크리스트를 찾을 수 없습니다." })}</p>
        <Button onClick={() => navigate(-1)}>{t("checklistDetail.back", { defaultValue: "이전으로" })}</Button>
      </div>
    );
  }

  const doneCount = detail.tasks.filter((task) => task.status === "DONE").length;

  const columns: Column<ChecklistTask>[] = [
    {
      header: t("checklistDetail.columnDepartment", { defaultValue: "담당 부서" }),
      cell: (task) => <StatusBadge tone="info" label={departmentLabel(t, task.department)} />,
    },
    { header: t("checklistDetail.columnDescription", { defaultValue: "설명" }), cell: (task) => task.description },
    {
      header: t("checklistDetail.columnStatus", { defaultValue: "상태" }),
      cell: (task) => <StatusBadge tone={checklistTaskStatusTone(task.status)} label={checklistTaskStatusLabel(t, task.status)} />,
    },
    {
      header: t("checklistDetail.columnRecoveredAsset", { defaultValue: "회수 자산" }),
      cell: (task) =>
        task.relatedAssetKey ? (
          <button
            type="button"
            className="text-primary hover:underline"
            onClick={() => navigate(`/assets/${task.relatedAssetId}`)}
          >
            {task.relatedAssetKey}
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
            {t("checklistDetail.typeSuffix", {
              type: checklistTypeLabel(t, detail.type),
              defaultValue: `${checklistTypeLabel(t, detail.type)} 체크리스트`,
            })}
          </p>
          <h1 className="text-heading-large font-bold text-foreground">{detail.targetUserName}</h1>
          <div className="flex flex-wrap items-center gap-2">
            <StatusBadge tone={checklistStatusTone(detail.status)} label={checklistStatusLabel(t, detail.status)} />
            <span className="text-sm text-muted-foreground">
              {t("checklistDetail.overallProgress", {
                done: doneCount,
                total: detail.tasks.length,
                defaultValue: `전체 진행률 ${doneCount} / ${detail.tasks.length}`,
              })}
            </span>
          </div>
        </div>
      </div>

      <DataTable
        columns={columns}
        data={detail.tasks}
        rowKey={(task) => task.id}
        emptyTitle={t("checklistDetail.emptyTitle", { defaultValue: "하위 작업이 없습니다" })}
        emptyDescription={t("checklistDetail.emptyDescription", { defaultValue: "정의된 하위 작업이 없습니다." })}
      />
    </div>
  );
}
