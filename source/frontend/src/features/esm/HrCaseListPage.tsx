import { type FormEvent, useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { Plus } from "lucide-react";

import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
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
  Modal,
  Pagination,
  StatusBadge,
  TicketListLayout,
  toast,
} from "@/components/common";
import { esmApi } from "@/features/esm/api";
import { formatDate } from "@/features/esm/format";
import { hrCaseStatusLabel, hrCaseStatusTone } from "@/features/esm/status";
import type { HrCaseStatus, HrCaseSummary, PageResponse } from "@/features/esm/types";
import { extractErrorMessage } from "@/lib/apiClient";

/*
 * HR 케이스 목록(SCR-ESM-007) — HR 담당자 전용. 필터(상태) + 우측 상단 "케이스 접수" 모달.
 */
const PAGE_SIZE = 13;
const ALL = "ALL";

const STATUS_OPTIONS: HrCaseStatus[] = ["INTAKE", "DOCUMENTATION", "INVESTIGATION", "RESOLUTION"];

const EMPTY_CREATE = { subjectUserName: "", title: "", description: "" };

export function HrCaseListPage() {
  const { t } = useTranslation("esm");
  const navigate = useNavigate();
  const [status, setStatus] = useState(ALL);
  const [page, setPage] = useState(0);
  const [data, setData] = useState<PageResponse<HrCaseSummary> | null>(null);
  const [loading, setLoading] = useState(true);

  const [createOpen, setCreateOpen] = useState(false);
  const [createForm, setCreateForm] = useState(EMPTY_CREATE);
  const [createError, setCreateError] = useState<string | null>(null);
  const [creating, setCreating] = useState(false);

  const load = () => {
    let active = true;
    setLoading(true);
    esmApi
      .listHrCases({ status: status === ALL ? undefined : status, page, size: PAGE_SIZE })
      .then((res) => active && setData(res))
      .catch((err) => active && toast.error(extractErrorMessage(err)))
      .finally(() => active && setLoading(false));
    return () => {
      active = false;
    };
  };

  useEffect(load, [status, page]);

  const handleCreate = async (e: FormEvent) => {
    e.preventDefault();
    setCreateError(null);
    if (!createForm.title.trim()) {
      setCreateError(t("hrCaseList.titleRequiredError", { defaultValue: "제목은 필수입니다." }));
      return;
    }
    setCreating(true);
    try {
      const created = await esmApi.createHrCase({
        subjectUserName: createForm.subjectUserName.trim(),
        title: createForm.title.trim(),
        description: createForm.description.trim() || undefined,
      });
      toast.success(t("hrCaseList.createSuccess", { defaultValue: "케이스가 접수되었습니다" }));
      setCreateOpen(false);
      setCreateForm(EMPTY_CREATE);
      navigate(`/esm/hr-cases/${created.id}`);
    } catch (err) {
      setCreateError(extractErrorMessage(err, t("hrCaseList.createFailed", { defaultValue: "접수에 실패했습니다." })));
    } finally {
      setCreating(false);
    }
  };

  const columns: Column<HrCaseSummary>[] = [
    { header: t("hrCaseList.columnCaseId", { defaultValue: "케이스ID" }), width: 130, cell: (c) => `HR-${c.id}` },
    {
      header: t("hrCaseList.columnTitle", { defaultValue: "제목" }),
      cell: (c) => <span className="truncate block">{c.title}</span>,
    },
    {
      header: t("hrCaseList.columnStatus", { defaultValue: "상태" }),
      width: 110,
      cell: (c) => <StatusBadge tone={hrCaseStatusTone(c.status)} label={hrCaseStatusLabel(t, c.status)} />,
    },
    { header: t("hrCaseList.columnUpdatedAt", { defaultValue: "최종 갱신일" }), width: 110, cell: (c) => formatDate(c.updatedAt) },
  ];

  const totalPages = data ? Math.ceil(data.totalElements / PAGE_SIZE) : 0;

  return (
    <TicketListLayout
      title={t("hrCaseList.title", { defaultValue: "HR 케이스" })}
      description={t("hrCaseList.description", { defaultValue: "민감한 인사 이슈를 접수·처리합니다." })}
      actions={
        <Button onClick={() => setCreateOpen(true)}>
          <Plus />
          {t("hrCaseList.createButton", { defaultValue: "케이스 접수" })}
        </Button>
      }
      filters={
        <div className="space-y-1">
          <Label>{t("hrCaseList.columnStatus", { defaultValue: "상태" })}</Label>
          <Select value={status} onValueChange={(v) => { setPage(0); setStatus(v); }}>
            <SelectTrigger className="w-36">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value={ALL}>{t("hrCaseList.filterAll", { defaultValue: "전체" })}</SelectItem>
              {STATUS_OPTIONS.map((s) => (
                <SelectItem key={s} value={s}>
                  {hrCaseStatusLabel(t, s)}
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
        rowKey={(c) => c.id}
        loading={loading}
        onRowClick={(c) => navigate(`/esm/hr-cases/${c.id}`)}
        emptyTitle={t("hrCaseList.emptyTitle", { defaultValue: "케이스가 없습니다" })}
        emptyDescription={t("hrCaseList.emptyDescription", { defaultValue: "접수된 HR 케이스가 없습니다." })}
      />
      <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />

      <Modal
        open={createOpen}
        onOpenChange={setCreateOpen}
        title={t("hrCaseList.createModalTitle", { defaultValue: "케이스 접수" })}
        description={t("hrCaseList.createModalDescription", { defaultValue: "민감 정보이므로 HR 케이스 담당자만 조회·처리할 수 있습니다." })}
      >
        <form onSubmit={handleCreate} className="space-y-4">
          <div className="space-y-1.5">
            <Label htmlFor="hr-subject">{t("hrCaseList.subjectLabel", { defaultValue: "대상자" })}</Label>
            <Input
              id="hr-subject"
              value={createForm.subjectUserName}
              onChange={(e) => setCreateForm((f) => ({ ...f, subjectUserName: e.target.value }))}
            />
          </div>
          <div className="space-y-1.5">
            <Label htmlFor="hr-title">
              {t("hrCaseList.titleLabel", { defaultValue: "제목" })}
              <span className="ml-0.5 text-destructive" aria-hidden="true">*</span>
            </Label>
            <Input
              id="hr-title"
              value={createForm.title}
              onChange={(e) => setCreateForm((f) => ({ ...f, title: e.target.value }))}
              required
            />
          </div>
          <div className="space-y-1.5">
            <Label htmlFor="hr-desc">{t("hrCaseList.descriptionLabel", { defaultValue: "내용" })}</Label>
            <Textarea
              id="hr-desc"
              rows={4}
              value={createForm.description}
              onChange={(e) => setCreateForm((f) => ({ ...f, description: e.target.value }))}
            />
          </div>
          {createError ? (
            <p role="alert" className="text-sm text-danger">
              {createError}
            </p>
          ) : null}
          <div className="flex justify-end gap-2">
            <Button type="button" variant="outline" onClick={() => setCreateOpen(false)}>
              {t("hrCaseList.cancelButton", { defaultValue: "취소" })}
            </Button>
            <Button type="submit" loading={creating}>
              {t("hrCaseList.submitButton", { defaultValue: "접수" })}
            </Button>
          </div>
        </form>
      </Modal>
    </TicketListLayout>
  );
}
