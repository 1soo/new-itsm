import { type FormEvent, useCallback, useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { useTranslation } from "react-i18next";
import type { TFunction } from "i18next";
import { Plus, Trash2 } from "lucide-react";

import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import {
  ApprovalPanel,
  ConfirmDialog,
  deriveApprovalStatusDisplay,
  PriorityBadge,
  StatusBadge,
  TicketDetailLayout,
  toast,
} from "@/components/common";
import type { ApprovalStep } from "@/components/common";
import { FullscreenLoader } from "@/routes/FullscreenLoader";
import { problemApi } from "@/features/problem/api";
import {
  actionStatusLabel,
  fallbackTransitions,
  levelLabel,
  originLabel,
  statusLabel,
  statusTone,
  transitionLabel,
} from "@/features/problem/status";
import type { ProblemDetail } from "@/features/problem/types";
import { commonApi } from "@/features/common/api";
import { extractErrorMessage } from "@/lib/apiClient";

/*
 * 문제 상세(SCR-PRB-003) — 6단계 전이·RCA(5 Whys)·워크어라운드·KE 생성·인시던트/변경 연계·
 * 후속 조치·종료(미해결 경고 다이얼로그). 변경 연계는 change 도메인 도입 전까지 비활성/안내.
 */
export function ProblemDetailPage() {
  const { t } = useTranslation("problem");
  const navigate = useNavigate();
  const id = Number(useParams().id);

  const [detail, setDetail] = useState<ProblemDetail | null>(null);
  const [approvalSteps, setApprovalSteps] = useState<ApprovalStep[]>([]);
  const [approvalCurrentStepNo, setApprovalCurrentStepNo] = useState<number | null>(null);
  const [loading, setLoading] = useState(true);
  const [notFound, setNotFound] = useState(false);
  const [busy, setBusy] = useState<string | null>(null);
  const [resubmitting, setResubmitting] = useState(false);

  const [closeWarning, setCloseWarning] = useState<string | null>(null);

  const refreshDetail = useCallback(
    (silent: boolean) => {
      if (!silent) setLoading(true);
      return problemApi
        .get(id)
        .then((d) => {
          setDetail(d);
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
          if (!silent) {
            toast.error(extractErrorMessage(err));
            setNotFound(true);
          }
        })
        .finally(() => {
          if (!silent) setLoading(false);
        });
    },
    [id],
  );

  const load = useCallback(() => {
    void refreshDetail(false);
  }, [refreshDetail]);

  useEffect(load, [load]);

  const run = async (
    key: string,
    fn: () => Promise<unknown>,
    successMsg?: string,
    reloadOnError = false,
  ) => {
    setBusy(key);
    try {
      await fn();
      if (successMsg) toast.success(successMsg);
      load();
    } catch (err) {
      toast.error(extractErrorMessage(err));
      // 상태 전이가 게이트(409)로 거부된 경우 BE가 이미 승인 인스턴스를 생성했을 수 있으므로,
      // 전체 로딩 화면 없이 조용히 다시 조회해 승인 패널에 반영한다.
      if (reloadOnError) refreshDetail(true);
    } finally {
      setBusy(null);
    }
  };

  if (loading) return <FullscreenLoader />;
  if (notFound || !detail) {
    return (
      <div className="mx-auto max-w-lg space-y-4 text-center">
        <p className="text-sm text-muted-foreground">
          {t("problemDetail.notFound", { defaultValue: "문제를 찾을 수 없습니다." })}
        </p>
        <Button onClick={() => navigate("/problems")}>
          {t("problemDetail.backToList", { defaultValue: "목록으로" })}
        </Button>
      </div>
    );
  }

  const transitions = detail.allowedTransitions ?? fallbackTransitions(detail.status);
  const isClosed = detail.status === "RESOLVED_CLOSED";
  const approved = detail.approval.approvalRequestId == null || detail.approval.status === "APPROVED";
  const approvalTargetStateLabel = detail.approval.targetState ? statusLabel(t, detail.approval.targetState) : null;
  const statusDisplay = deriveApprovalStatusDisplay(
    t,
    { tone: statusTone(detail.status), label: statusLabel(t, detail.status) },
    { status: detail.approval.status, targetStateLabel: approvalTargetStateLabel },
  );

  const handleResubmit = async () => {
    setResubmitting(true);
    try {
      const result = await commonApi.resubmitApproval({ ticketType: "PROBLEM", ticketId: id });
      toast.success(
        result.status === "NO_RULE_MATCHED"
          ? t("problemDetail.resubmitNoRuleMatched", {
              defaultValue: "매칭되는 승인 규칙이 없어 승인 없이 진행할 수 있습니다",
            })
          : t("problemDetail.resubmitSuccess", { defaultValue: "재승인요청이 접수되었습니다" }),
      );
      refreshDetail(true);
    } catch (err) {
      toast.error(extractErrorMessage(err));
    } finally {
      setResubmitting(false);
    }
  };

  const handleClose = async (force: boolean) => {
    setBusy("close");
    try {
      const res = await problemApi.close(id, force);
      if (!force && res.warning) {
        setCloseWarning(res.warning);
        return;
      }
      setCloseWarning(null);
      toast.success(t("problemDetail.closeSuccess", { defaultValue: "문제가 종료되었습니다" }));
      load();
    } catch (err) {
      toast.error(extractErrorMessage(err));
    } finally {
      setBusy(null);
    }
  };

  return (
    <>
      <TicketDetailLayout
        ticketKey={detail.ticketKey}
        title={detail.summary}
        badges={
          <>
            <StatusBadge tone={statusDisplay.tone} label={statusDisplay.label} />
            {detail.priority ? (
              <PriorityBadge priority={detail.priority} />
            ) : (
              <StatusBadge tone="muted" label={t("problemDetail.priorityNotCalculated", { defaultValue: "우선순위 미산정" })} />
            )}
          </>
        }
        actions={
          <>
            {transitions.map((target) => {
              const blocked = target === "RESOLVED_CLOSED" && !approved;
              return (
                <Button
                  key={target}
                  loading={busy === `st-${target}`}
                  disabled={blocked}
                  title={
                    blocked
                      ? t("problemDetail.closeBlockedTooltip", {
                          defaultValue: "승인 완료 전에는 종료 상태로 전이할 수 없습니다",
                        })
                      : undefined
                  }
                  onClick={() =>
                    run(
                      `st-${target}`,
                      () => problemApi.transition(id, target),
                      t("problemDetail.transitionSuccess", {
                        status: statusLabel(t, target),
                        defaultValue: `상태가 '${statusLabel(t, target)}'로 변경되었습니다`,
                      }),
                      true,
                    )
                  }
                >
                  {transitionLabel(t, target)}
                </Button>
              );
            })}
            {!isClosed ? (
              <Button
                className="bg-success text-success-foreground hover:bg-success/90"
                loading={busy === "close"}
                onClick={() => handleClose(false)}
              >
                {t("problemDetail.close", { defaultValue: "종료" })}
              </Button>
            ) : null}
          </>
        }
        meta={
          <>
            <Card>
              <CardHeader><CardTitle className="text-base">{t("problemDetail.classificationTitle", { defaultValue: "분류" })}</CardTitle></CardHeader>
              <CardContent className="space-y-1.5 text-sm">
                <MetaRow
                  label={t("problemList.columnPriority", { defaultValue: "우선순위" })}
                  value={detail.priority ?? t("problemList.priorityNotCalculated", { defaultValue: "미산정" })}
                />
                <MetaRow
                  label={t("problemCreate.impact", { defaultValue: "영향도" })}
                  value={detail.impact ? levelLabel(t, detail.impact) : "-"}
                />
                <MetaRow
                  label={t("problemCreate.urgency", { defaultValue: "긴급도" })}
                  value={detail.urgency ? levelLabel(t, detail.urgency) : "-"}
                />
                {detail.origin ? (
                  <MetaRow label={t("problemList.columnOrigin", { defaultValue: "출처" })} value={originLabel(t, detail.origin)} />
                ) : null}
                {detail.component ? (
                  <MetaRow label={t("problemCreate.component", { defaultValue: "구성요소" })} value={detail.component} />
                ) : null}
              </CardContent>
            </Card>

            <ApprovalPanel
              matched={detail.approval.approvalRequestId != null}
              steps={approvalSteps}
              currentStepNo={approvalCurrentStepNo}
              targetStateLabel={approvalTargetStateLabel}
              status={detail.approval.status}
              onResubmit={detail.approval.status === "REJECTED" ? handleResubmit : undefined}
              resubmitting={resubmitting}
            />

            <Card>
              <CardHeader><CardTitle className="text-base">{t("problemDetail.linkedIncidentsTitle", { defaultValue: "연결 인시던트" })}</CardTitle></CardHeader>
              <CardContent className="space-y-1.5 text-sm">
                {detail.linkedIncidents.length === 0 ? (
                  <p className="text-muted-foreground">{t("problemDetail.noLinks", { defaultValue: "연결 없음" })}</p>
                ) : (
                  detail.linkedIncidents.map((l) => (
                    <button
                      key={l.id}
                      className="block text-left text-info hover:underline"
                      onClick={() => navigate(`/incidents/${l.id}`)}
                    >
                      {l.ticketKey}
                    </button>
                  ))
                )}
              </CardContent>
            </Card>

            <Card>
              <CardHeader><CardTitle className="text-base">{t("problemDetail.linkedChangesTitle", { defaultValue: "연결 변경" })}</CardTitle></CardHeader>
              <CardContent className="space-y-1.5 text-sm">
                {detail.linkedChanges.length === 0 ? (
                  <p className="text-muted-foreground">{t("problemDetail.noLinks", { defaultValue: "연결 없음" })}</p>
                ) : (
                  detail.linkedChanges.map((l) => (
                    <span key={l.id} className="block text-foreground">{l.ticketKey}</span>
                  ))
                )}
              </CardContent>
            </Card>

            <Card>
              <CardHeader><CardTitle className="text-base">{t("problemDetail.linkedAssetsTitle", { defaultValue: "연결 자산" })}</CardTitle></CardHeader>
              <CardContent className="space-y-1.5 text-sm">
                {detail.linkedAssets.length === 0 ? (
                  <p className="text-muted-foreground">{t("problemDetail.noLinks", { defaultValue: "연결 없음" })}</p>
                ) : (
                  detail.linkedAssets.map((l) => (
                    <button
                      key={l.id}
                      className="block text-left text-info hover:underline"
                      onClick={() => navigate(`/assets/${l.id}`)}
                    >
                      {l.ticketKey}
                    </button>
                  ))
                )}
              </CardContent>
            </Card>
          </>
        }
      >
        <Card>
          <CardHeader><CardTitle className="text-base">{t("problemDetail.descriptionTitle", { defaultValue: "설명" })}</CardTitle></CardHeader>
          <CardContent>
            <p className="whitespace-pre-wrap text-sm text-foreground">
              {detail.description || t("problemDetail.noDescription", { defaultValue: "설명 없음" })}
            </p>
          </CardContent>
        </Card>

        <RcaCard
          t={t}
          detail={detail}
          busy={busy === "rca"}
          onSave={(body) => run("rca", () => problemApi.saveRca(id, body), t("problemDetail.rcaSaved", { defaultValue: "RCA가 저장되었습니다" }))}
        />

        <WorkaroundCard
          t={t}
          detail={detail}
          busy={busy === "workaround"}
          onSubmit={(body) =>
            run(
              "workaround",
              () => problemApi.addWorkaround(id, body),
              t("problemDetail.workaroundSaved", { defaultValue: "워크어라운드가 등록되었습니다" }),
            )
          }
        />

        <KnownErrorCard
          t={t}
          detail={detail}
          busy={busy === "ke"}
          onSubmit={(body) =>
            run(
              "ke",
              () => problemApi.createKnownError(id, body),
              t("problemDetail.keSaved", { defaultValue: "알려진 오류가 등록되었습니다" }),
            )
          }
        />

        <LinkCard
          t={t}
          detail={detail}
          busy={busy === "link"}
          onLinkIncident={(targetId) =>
            run(
              "link",
              () => problemApi.link(id, { targetType: "INCIDENT", targetId }),
              t("problemDetail.linkIncidentSuccess", { defaultValue: "인시던트가 연계되었습니다" }),
            )
          }
          onLinkChange={(targetId) =>
            run(
              "link",
              () => problemApi.link(id, { targetType: "CHANGE", targetId }),
              t("problemDetail.linkChangeSuccess", { defaultValue: "변경이 연계되었습니다" }),
            )
          }
        />

        <ActionsCard
          t={t}
          detail={detail}
          busyKey={busy}
          onAdd={(body) =>
            run(
              "action-add",
              () => problemApi.addAction(id, body),
              t("problemDetail.actionAdded", { defaultValue: "후속 조치가 등록되었습니다" }),
            )
          }
          onToggle={(actionId, status) =>
            run(
              `action-${actionId}`,
              () => problemApi.updateActionStatus(id, actionId, status),
              t("problemDetail.actionStatusChanged", { defaultValue: "조치 상태가 변경되었습니다" }),
            )
          }
        />
      </TicketDetailLayout>

      <ConfirmDialog
        open={!!closeWarning}
        onOpenChange={(o) => !o && setCloseWarning(null)}
        title={t("problemDetail.closeWarningTitle", { defaultValue: "미해결 후속 조치가 있습니다" })}
        description={closeWarning ?? undefined}
        confirmLabel={t("problemDetail.closeAnyway", { defaultValue: "그래도 종료" })}
        loading={busy === "close"}
        onConfirm={() => handleClose(true)}
      />
    </>
  );
}

function MetaRow({ label, value }: { label: string; value: string }) {
  return (
    <div className="flex items-center justify-between gap-2">
      <span className="text-muted-foreground">{label}</span>
      <span className="text-right text-foreground">{value}</span>
    </div>
  );
}

function RcaCard({
  t,
  detail,
  busy,
  onSave,
}: {
  t: TFunction;
  detail: ProblemDetail;
  busy: boolean;
  onSave: (body: { rootCause: string; fiveWhys: string[]; category: string }) => void;
}) {
  const [rootCause, setRootCause] = useState(detail.rca?.rootCause ?? "");
  const [category, setCategory] = useState(detail.rca?.category ?? "");
  const [whys, setWhys] = useState<string[]>(detail.rca?.fiveWhys?.length ? detail.rca.fiveWhys : [""]);

  const setWhy = (i: number, v: string) => setWhys((arr) => arr.map((w, idx) => (idx === i ? v : w)));
  const addWhy = () => setWhys((arr) => [...arr, ""]);
  const removeWhy = (i: number) => setWhys((arr) => (arr.length > 1 ? arr.filter((_, idx) => idx !== i) : arr));

  const handleSubmit = (e: FormEvent) => {
    e.preventDefault();
    onSave({
      rootCause: rootCause.trim(),
      fiveWhys: whys.map((w) => w.trim()).filter(Boolean),
      category: category.trim(),
    });
  };

  return (
    <Card>
      <CardHeader><CardTitle className="text-base">{t("problemDetail.rcaTitle", { defaultValue: "근본 원인 분석 (RCA)" })}</CardTitle></CardHeader>
      <CardContent>
        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="space-y-1.5">
            <Label htmlFor="rc">{t("problemDetail.rootCause", { defaultValue: "근본 원인" })}</Label>
            <Textarea
              id="rc"
              value={rootCause}
              onChange={(e) => setRootCause(e.target.value)}
              rows={2}
              placeholder={t("problemDetail.rootCausePlaceholder", {
                defaultValue: "사람이 아닌 시스템/프로세스 관점으로 기술",
              })}
            />
          </div>
          <div className="space-y-2">
            <Label>5 Whys</Label>
            {whys.map((w, i) => (
              <div key={i} className="flex items-center gap-2">
                <span className="w-6 text-sm text-muted-foreground">#{i + 1}</span>
                <Input
                  value={w}
                  onChange={(e) => setWhy(i, e.target.value)}
                  placeholder={t("problemDetail.whyPlaceholder", { index: i + 1, defaultValue: `왜? ${i + 1}` })}
                />
                <Button
                  type="button"
                  variant="ghost"
                  size="icon"
                  onClick={() => removeWhy(i)}
                  disabled={whys.length <= 1}
                  aria-label={t("problemDetail.delete", { defaultValue: "삭제" })}
                >
                  <Trash2 />
                </Button>
              </div>
            ))}
            <Button type="button" variant="outline" size="sm" onClick={addWhy}>
              <Plus />
              {t("problemDetail.addWhyStep", { defaultValue: "단계 추가" })}
            </Button>
          </div>
          <div className="space-y-1.5">
            <Label htmlFor="cat">{t("problemDetail.category", { defaultValue: "카테고리" })}</Label>
            <Input id="cat" value={category} onChange={(e) => setCategory(e.target.value)} />
          </div>
          <div className="flex justify-end">
            <Button type="submit" loading={busy}>{t("problemDetail.rcaSave", { defaultValue: "RCA 저장" })}</Button>
          </div>
        </form>
      </CardContent>
    </Card>
  );
}

function WorkaroundCard({
  t,
  detail,
  busy,
  onSubmit,
}: {
  t: TFunction;
  detail: ProblemDetail;
  busy: boolean;
  onSubmit: (body: { content: string; linkedArticleId?: number }) => void;
}) {
  const [content, setContent] = useState("");
  const [articleId, setArticleId] = useState("");

  const handleSubmit = (e: FormEvent) => {
    e.preventDefault();
    if (!content.trim()) return;
    onSubmit({ content: content.trim(), linkedArticleId: articleId ? Number(articleId) : undefined });
    setContent("");
    setArticleId("");
  };

  return (
    <Card>
      <CardHeader><CardTitle className="text-base">{t("problemDetail.workaroundTitle", { defaultValue: "워크어라운드" })}</CardTitle></CardHeader>
      <CardContent className="space-y-3">
        {detail.workaround ? (
          <div className="rounded-md border border-border bg-muted/40 p-3 text-sm">
            <p className="whitespace-pre-wrap text-foreground">{detail.workaround}</p>
          </div>
        ) : (
          <p className="text-sm text-muted-foreground">
            {t("problemDetail.noWorkaround", { defaultValue: "등록된 워크어라운드가 없습니다." })}
          </p>
        )}
        <form onSubmit={handleSubmit} className="space-y-3">
          <div className="space-y-1.5">
            <Label htmlFor="wa">{t("problemDetail.workaroundContent", { defaultValue: "임시 대응책" })}</Label>
            <Textarea id="wa" value={content} onChange={(e) => setContent(e.target.value)} rows={2} />
          </div>
          <div className="flex items-end gap-2">
            <div className="space-y-1.5">
              <Label htmlFor="art">{t("problemDetail.linkedArticleId", { defaultValue: "지식 문서 ID (선택)" })}</Label>
              <Input id="art" type="number" className="w-40" value={articleId} onChange={(e) => setArticleId(e.target.value)} />
            </div>
            <Button type="submit" loading={busy} disabled={!content.trim()}>
              {t("problemDetail.workaroundSubmit", { defaultValue: "등록" })}
            </Button>
          </div>
        </form>
      </CardContent>
    </Card>
  );
}

function KnownErrorCard({
  t,
  detail,
  busy,
  onSubmit,
}: {
  t: TFunction;
  detail: ProblemDetail;
  busy: boolean;
  onSubmit: (body: { title: string; rootCause: string; workaround: string }) => void;
}) {
  const [title, setTitle] = useState("");

  const handleSubmit = (e: FormEvent) => {
    e.preventDefault();
    if (!title.trim()) return;
    onSubmit({
      title: title.trim(),
      rootCause: detail.rca?.rootCause ?? "",
      workaround: detail.workaround ?? "",
    });
    setTitle("");
  };

  return (
    <Card>
      <CardHeader><CardTitle className="text-base">{t("problemDetail.keTitle", { defaultValue: "알려진 오류(KEDB) 등록" })}</CardTitle></CardHeader>
      <CardContent>
        <form onSubmit={handleSubmit} className="flex flex-wrap items-end gap-2">
          <div className="flex-1 space-y-1.5">
            <Label htmlFor="ket">{t("problemDetail.keTitleField", { defaultValue: "제목" })}</Label>
            <Input
              id="ket"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              placeholder={t("problemDetail.keTitlePlaceholder", { defaultValue: "근본 원인·워크어라운드가 함께 등록됩니다" })}
            />
          </div>
          <Button type="submit" variant="outline" loading={busy} disabled={!title.trim()}>
            {t("problemDetail.keCreate", { defaultValue: "KE 생성" })}
          </Button>
        </form>
      </CardContent>
    </Card>
  );
}

function LinkCard({
  t,
  busy,
  onLinkIncident,
  onLinkChange,
}: {
  t: TFunction;
  detail: ProblemDetail;
  busy: boolean;
  onLinkIncident: (targetId: number) => void;
  onLinkChange: (targetId: number) => void;
}) {
  const [incidentId, setIncidentId] = useState("");
  const [changeId, setChangeId] = useState("");

  return (
    <Card>
      <CardHeader><CardTitle className="text-base">{t("problemDetail.linkCardTitle", { defaultValue: "인시던트 / 변경 연계" })}</CardTitle></CardHeader>
      <CardContent className="space-y-4">
        <form
          className="flex items-end gap-2"
          onSubmit={(e) => {
            e.preventDefault();
            if (!incidentId) return;
            onLinkIncident(Number(incidentId));
            setIncidentId("");
          }}
        >
          <div className="space-y-1.5">
            <Label htmlFor="incId">{t("problemDetail.incidentId", { defaultValue: "인시던트 ID" })}</Label>
            <Input id="incId" type="number" className="w-40" value={incidentId} onChange={(e) => setIncidentId(e.target.value)} />
          </div>
          <Button type="submit" loading={busy} disabled={!incidentId}>
            {t("problemDetail.linkIncident", { defaultValue: "인시던트 연계" })}
          </Button>
        </form>
        <form
          className="flex items-end gap-2 border-t border-border pt-3"
          onSubmit={(e) => {
            e.preventDefault();
            if (!changeId) return;
            onLinkChange(Number(changeId));
            setChangeId("");
          }}
        >
          <div className="space-y-1.5">
            <Label htmlFor="chgId">{t("problemDetail.changeId", { defaultValue: "변경 ID" })}</Label>
            <Input id="chgId" type="number" className="w-40" value={changeId} onChange={(e) => setChangeId(e.target.value)} />
          </div>
          <Button type="submit" loading={busy} disabled={!changeId}>
            {t("problemDetail.linkChange", { defaultValue: "변경 연계" })}
          </Button>
        </form>
      </CardContent>
    </Card>
  );
}

function ActionsCard({
  t,
  detail,
  busyKey,
  onAdd,
  onToggle,
}: {
  t: TFunction;
  detail: ProblemDetail;
  busyKey: string | null;
  onAdd: (body: { description: string; owner?: string; dueDate?: string }) => void;
  onToggle: (actionId: number, status: "IN_PROGRESS" | "DONE") => void;
}) {
  const [description, setDescription] = useState("");
  const [owner, setOwner] = useState("");
  const [dueDate, setDueDate] = useState("");

  const handleAdd = (e: FormEvent) => {
    e.preventDefault();
    if (!description.trim()) return;
    onAdd({
      description: description.trim(),
      owner: owner.trim() || undefined,
      dueDate: dueDate ? new Date(dueDate).toISOString() : undefined,
    });
    setDescription("");
    setOwner("");
    setDueDate("");
  };

  return (
    <Card>
      <CardHeader><CardTitle className="text-base">{t("problemDetail.actionsTitle", { defaultValue: "후속 조치" })}</CardTitle></CardHeader>
      <CardContent className="space-y-4">
        {detail.actions.length === 0 ? (
          <p className="text-sm text-muted-foreground">
            {t("problemDetail.noActions", { defaultValue: "등록된 후속 조치가 없습니다." })}
          </p>
        ) : (
          <ul className="space-y-2">
            {detail.actions.map((a) => (
              <li key={a.id} className="flex items-center justify-between gap-2 rounded-md border border-border p-3 text-sm">
                <span className="min-w-0 flex-1">{a.description}</span>
                <StatusBadge tone={a.status === "DONE" ? "success" : "warning"} label={actionStatusLabel(t, a.status)} />
                <Button
                  variant="outline"
                  size="sm"
                  loading={busyKey === `action-${a.id}`}
                  onClick={() => onToggle(a.id, a.status === "DONE" ? "IN_PROGRESS" : "DONE")}
                >
                  {a.status === "DONE"
                    ? t("problemDetail.actionMarkInProgress", { defaultValue: "진행중으로" })
                    : t("problemDetail.actionMarkDone", { defaultValue: "완료 처리" })}
                </Button>
              </li>
            ))}
          </ul>
        )}
        <form onSubmit={handleAdd} className="flex flex-wrap items-end gap-2">
          <div className="flex-1 space-y-1.5">
            <Label htmlFor="actd">{t("problemDetail.actionDescription", { defaultValue: "조치 내용" })}</Label>
            <Input id="actd" value={description} onChange={(e) => setDescription(e.target.value)} />
          </div>
          <div className="space-y-1.5">
            <Label htmlFor="acto">{t("problemDetail.actionOwner", { defaultValue: "담당" })}</Label>
            <Input id="acto" className="w-28" value={owner} onChange={(e) => setOwner(e.target.value)} />
          </div>
          <div className="space-y-1.5">
            <Label htmlFor="actdue">{t("problemDetail.actionDueDate", { defaultValue: "기한" })}</Label>
            <Input id="actdue" type="date" value={dueDate} onChange={(e) => setDueDate(e.target.value)} />
          </div>
          <Button type="submit" loading={busyKey === "action-add"} disabled={!description.trim()}>
            {t("problemDetail.actionAdd", { defaultValue: "추가" })}
          </Button>
        </form>
      </CardContent>
    </Card>
  );
}
