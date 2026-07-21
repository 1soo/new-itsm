import { type FormEvent, useCallback, useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { useTranslation } from "react-i18next";

import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
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
import { ApprovalPanel, ConfirmDialog, deriveApprovalStatusDisplay, StatusBadge, toast } from "@/components/common";
import type { ApprovalStep } from "@/components/common";
import { FullscreenLoader } from "@/routes/FullscreenLoader";
import { knowledgeApi } from "@/features/knowledge/api";
import { statusLabel, statusTone } from "@/features/knowledge/status";
import type { ArticleDetail, Category } from "@/features/knowledge/types";
import { commonApi } from "@/features/common/api";
import { extractErrorMessage } from "@/lib/apiClient";

/*
 * 기사 작성·편집(SCR-KM-003) — 제목/본문 필수, 카테고리/라벨 지정, 검토 요청(DRAFT→IN_REVIEW), 삭제.
 * id 없으면 신규 작성 모드, 저장 성공 시 동일 화면(편집 모드)으로 이동.
 * 승인 상태(공용, API-COM-004)는 기사 상세의 approval.approvalRequestId로 조회해 공용 ApprovalPanel로
 * 표시한다(SRM/CHANGE와 동일 패턴) — 페이지 재진입·새로고침에도 항상 복원됨. 매칭되는 승인 프로세스가
 * 없으면(approvalRequestId=null) "검토 요청" 즉시 게시되어 패널이 렌더링되지 않는다.
 */
export function ArticleEditPage() {
  const { t } = useTranslation("knowledge");
  const navigate = useNavigate();
  const params = useParams();
  const id = params.id ? Number(params.id) : null;

  const [detail, setDetail] = useState<ArticleDetail | null>(null);
  const [categories, setCategories] = useState<Category[]>([]);
  const [loading, setLoading] = useState(!!id);
  const [notFound, setNotFound] = useState(false);

  const [title, setTitle] = useState("");
  const [body, setBody] = useState("");
  const [categoryId, setCategoryId] = useState("");
  const [labels, setLabels] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [saving, setSaving] = useState(false);
  const [busy, setBusy] = useState<string | null>(null);
  const [confirmDelete, setConfirmDelete] = useState(false);
  const [resubmitting, setResubmitting] = useState(false);

  const [approvalSteps, setApprovalSteps] = useState<ApprovalStep[]>([]);
  const [approvalCurrentStepNo, setApprovalCurrentStepNo] = useState<number | null>(null);

  useEffect(() => {
    knowledgeApi.listCategories().then(setCategories).catch((err) => toast.error(extractErrorMessage(err)));
  }, []);

  const load = useCallback(() => {
    if (!id) return;
    setLoading(true);
    knowledgeApi
      .get(id)
      .then((d) => {
        setDetail(d);
        setTitle(d.title);
        setBody(d.body);
        setLabels(d.labels.join(", "));
        setNotFound(false);
        if (d.approval.approvalRequestId == null) {
          setApprovalSteps([]);
          setApprovalCurrentStepNo(null);
          return;
        }
        return commonApi.getApproval(d.approval.approvalRequestId).then((a) => {
          setApprovalSteps(a.steps);
          setApprovalCurrentStepNo(a.currentStepNo);
        });
      })
      .catch((err) => {
        toast.error(extractErrorMessage(err));
        setNotFound(true);
      })
      .finally(() => setLoading(false));
  }, [id]);

  useEffect(load, [load]);

  useEffect(() => {
    if (detail && categories.length > 0) {
      const match = categories.find((c) => c.name === detail.category);
      if (match) setCategoryId(String(match.id));
    }
  }, [detail, categories]);

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError(null);
    if (!title.trim() || !body.trim()) {
      setError(t("articleEdit.requiredError", { defaultValue: "제목과 본문은 필수입니다." }));
      return;
    }
    const payload = {
      title: title.trim(),
      body: body.trim(),
      categoryId: categoryId ? Number(categoryId) : undefined,
      labels: labels.trim() ? labels.split(",").map((l) => l.trim()).filter(Boolean) : undefined,
    };
    setSaving(true);
    try {
      if (id) {
        await knowledgeApi.update(id, payload);
        toast.success(t("articleEdit.saveSuccess", { defaultValue: "기사가 저장되었습니다" }));
        load();
      } else {
        const created = await knowledgeApi.create(payload);
        toast.success(t("articleEdit.createSuccess", { defaultValue: "기사가 작성되었습니다" }));
        navigate(`/knowledge/${created.id}/edit`);
      }
    } catch (err) {
      setError(extractErrorMessage(err, t("articleEdit.saveFailed", { defaultValue: "저장에 실패했습니다." })));
    } finally {
      setSaving(false);
    }
  };

  const handleRequestReview = async () => {
    if (!id) return;
    setBusy("review");
    try {
      const result = await knowledgeApi.transition(id, "IN_REVIEW");
      toast.success(
        result.status === "PUBLISHED"
          ? t("articleEdit.publishedWithoutApproval", { defaultValue: "승인 절차 없이 게시되었습니다" })
          : t("articleEdit.reviewRequested", { defaultValue: "검토가 요청되었습니다. 승인 대기 중입니다." }),
      );
      load();
    } catch (err) {
      toast.error(extractErrorMessage(err));
    } finally {
      setBusy(null);
    }
  };

  const handleDelete = async () => {
    if (!id) return;
    setBusy("delete");
    try {
      await knowledgeApi.remove(id);
      toast.success(t("articleEdit.deleteSuccess", { defaultValue: "기사가 삭제되었습니다" }));
      navigate("/knowledge");
    } catch (err) {
      toast.error(extractErrorMessage(err));
    } finally {
      setBusy(null);
      setConfirmDelete(false);
    }
  };

  const handleResubmit = async () => {
    if (!id) return;
    setResubmitting(true);
    try {
      const result = await commonApi.resubmitApproval({ ticketType: "KNOWLEDGE", ticketId: id });
      toast.success(
        result.status === "NO_RULE_MATCHED"
          ? t("articleEdit.resubmitNoRuleMatched", {
              defaultValue: "매칭되는 승인 규칙이 없어 승인 없이 진행할 수 있습니다",
            })
          : t("articleEdit.resubmitSuccess", { defaultValue: "재승인요청이 접수되었습니다" }),
      );
      load();
    } catch (err) {
      toast.error(extractErrorMessage(err));
    } finally {
      setResubmitting(false);
    }
  };

  if (loading) return <FullscreenLoader />;
  if (id && (notFound || !detail)) {
    return (
      <div className="mx-auto max-w-lg space-y-4 text-center">
        <p className="text-sm text-muted-foreground">{t("articleEdit.notFound", { defaultValue: "기사를 찾을 수 없습니다." })}</p>
        <Button onClick={() => navigate("/knowledge")}>{t("articleEdit.backToList", { defaultValue: "목록으로" })}</Button>
      </div>
    );
  }

  const approvalTargetStateLabel = detail?.approval.targetState ? statusLabel(t, detail.approval.targetState) : null;
  const statusDisplay = detail
    ? deriveApprovalStatusDisplay(
        t,
        { tone: statusTone(detail.status), label: statusLabel(t, detail.status) },
        { status: detail.approval.status, targetStateLabel: approvalTargetStateLabel },
      )
    : null;

  return (
    <>
      <div className="space-y-4">
        <div className="flex items-center justify-between">
          <h1 className="text-xl font-semibold text-foreground">
            {id
              ? t("articleEdit.titleEdit", { defaultValue: "기사 편집" })
              : t("articleEdit.titleCreate", { defaultValue: "기사 작성" })}
          </h1>
          {statusDisplay ? <StatusBadge tone={statusDisplay.tone} label={statusDisplay.label} /> : null}
        </div>

        <div className="grid gap-4 lg:grid-cols-[minmax(0,1fr)_18rem]">
          <Card>
            <CardHeader><CardTitle className="text-base">{t("articleEdit.contentCardTitle", { defaultValue: "내용" })}</CardTitle></CardHeader>
            <CardContent>
              <form onSubmit={handleSubmit} className="space-y-4" noValidate>
                <div className="space-y-1.5">
                  <Label htmlFor="title">{t("articleEdit.titleLabel", { defaultValue: "제목" })}</Label>
                  <Input id="title" value={title} onChange={(e) => setTitle(e.target.value)} aria-invalid={!!error && !title.trim()} required />
                </div>
                <div className="space-y-1.5">
                  <Label htmlFor="body">{t("articleEdit.bodyLabel", { defaultValue: "본문" })}</Label>
                  <Textarea id="body" value={body} onChange={(e) => setBody(e.target.value)} rows={12} aria-invalid={!!error && !body.trim()} required />
                </div>
                {error ? (
                  <p role="alert" className="text-sm text-danger">{error}</p>
                ) : null}
                <div className="flex justify-end">
                  <Button type="submit" loading={saving}>{t("articleEdit.saveButton", { defaultValue: "저장" })}</Button>
                </div>
              </form>
            </CardContent>
          </Card>

          <div className="space-y-4">
            <Card>
              <CardHeader><CardTitle className="text-base">{t("articleEdit.classificationCardTitle", { defaultValue: "분류" })}</CardTitle></CardHeader>
              <CardContent className="space-y-3">
                <div className="space-y-1.5">
                  <Label>{t("articleEdit.categoryLabel", { defaultValue: "카테고리" })}</Label>
                  <Select value={categoryId} onValueChange={setCategoryId}>
                    <SelectTrigger><SelectValue placeholder={t("articleEdit.categoryPlaceholder", { defaultValue: "카테고리 선택" })} /></SelectTrigger>
                    <SelectContent>
                      {categories.map((c) => (
                        <SelectItem key={c.id} value={String(c.id)}>{c.name}</SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>
                <div className="space-y-1.5">
                  <Label htmlFor="labels">{t("articleEdit.labelsLabel", { defaultValue: "라벨 (쉼표로 구분)" })}</Label>
                  <Input
                    id="labels"
                    value={labels}
                    onChange={(e) => setLabels(e.target.value)}
                    placeholder={t("articleEdit.labelsPlaceholder", { defaultValue: "예: 네트워크, VPN" })}
                  />
                </div>
              </CardContent>
            </Card>

            {id ? (
              <Card>
                <CardHeader><CardTitle className="text-base">{t("articleEdit.actionsCardTitle", { defaultValue: "작업" })}</CardTitle></CardHeader>
                <CardContent className="space-y-2">
                  {detail?.status === "DRAFT" ? (
                    <Button
                      className="w-full bg-warning text-warning-foreground hover:bg-warning/90"
                      loading={busy === "review"}
                      onClick={handleRequestReview}
                    >
                      {t("articleEdit.requestReviewButton", { defaultValue: "검토 요청" })}
                    </Button>
                  ) : null}
                  <Button
                    variant="destructive"
                    className="w-full"
                    onClick={() => setConfirmDelete(true)}
                  >
                    {t("articleEdit.deleteButton", { defaultValue: "삭제" })}
                  </Button>
                </CardContent>
              </Card>
            ) : null}

            {detail ? (
              <ApprovalPanel
                matched={detail.approval.approvalRequestId != null}
                steps={approvalSteps}
                currentStepNo={approvalCurrentStepNo}
                targetStateLabel={approvalTargetStateLabel}
                status={detail.approval.status}
                onResubmit={detail.approval.status === "REJECTED" ? handleResubmit : undefined}
                resubmitting={resubmitting}
              />
            ) : null}
          </div>
        </div>
      </div>

      <ConfirmDialog
        open={confirmDelete}
        onOpenChange={setConfirmDelete}
        title={t("articleEdit.deleteConfirmTitle", { defaultValue: "기사를 삭제하시겠습니까?" })}
        description={t("articleEdit.deleteConfirmDescription", { defaultValue: "삭제된 기사는 복구할 수 없습니다." })}
        confirmLabel={t("articleEdit.deleteButton", { defaultValue: "삭제" })}
        loading={busy === "delete"}
        onConfirm={handleDelete}
      />
    </>
  );
}
