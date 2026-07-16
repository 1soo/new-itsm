import { type FormEvent, useCallback, useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { useTranslation } from "react-i18next";
import type { TFunction } from "i18next";
import { AlertTriangle } from "lucide-react";

import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
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
  ApprovalPanel,
  StatusBadge,
  TicketDetailLayout,
  Timeline,
  type ApprovalStep,
  type TimelineItem,
  toast,
} from "@/components/common";
import { FullscreenLoader } from "@/routes/FullscreenLoader";
import { hasAnyRole, ROLE_INCIDENT_MANAGER } from "@/features/auth/roles";
import { incidentApi } from "@/features/incident/api";
import { formatDateTime, formatMinutes } from "@/features/incident/format";
import {
  PRIORITIES,
  SEVERITIES,
  severityTone,
  statusLabel,
  statusTone,
  transitionLabel,
} from "@/features/incident/status";
import type {
  IncidentDetail,
  IncidentTargetStatus,
  Priority,
  ResponderRole,
  Severity,
  Visibility,
} from "@/features/incident/types";
import { commonApi } from "@/features/common/api";
import { useAppSelector } from "@/store/hooks";
import { extractErrorMessage } from "@/lib/apiClient";

const RESPONDER_ROLE_LABEL: Record<ResponderRole, string> = {
  TECH_LEAD: "Tech Lead",
  COMMS: "Comms",
  SCRIBE: "Scribe",
};

const STATUS_ORDER: Record<string, IncidentTargetStatus | undefined> = {
  NEW: "IN_PROGRESS",
  IN_PROGRESS: "RESOLVED",
  RESOLVED: "CLOSED",
};

function fallbackTransitions(status: string): IncidentTargetStatus[] {
  const next = STATUS_ORDER[status];
  return next ? [next] : [];
}

/*
 * 인시던트 상세(SCR-INC-003) — 심각도/우선순위 편집·상태전이·역할배정(IM)·에스컬레이션·
 * 타임라인 업데이트·해결/시간지표·문제 연계(problem 단계 전 비활성). 포스트모템 필요 배너.
 */
export function IncidentDetailPage() {
  const { t } = useTranslation("incident");
  const navigate = useNavigate();
  const id = Number(useParams().id);
  const roles = useAppSelector((s) => s.auth.user?.roles);
  const isIM = hasAnyRole(roles, [ROLE_INCIDENT_MANAGER]);

  const [detail, setDetail] = useState<IncidentDetail | null>(null);
  const [approvalSteps, setApprovalSteps] = useState<ApprovalStep[]>([]);
  const [approvalCurrentStepNo, setApprovalCurrentStepNo] = useState<number | null>(null);
  const [loading, setLoading] = useState(true);
  const [notFound, setNotFound] = useState(false);
  const [busy, setBusy] = useState<string | null>(null);

  // 편집 상태
  const [sev, setSev] = useState<Severity>("SEV3");
  const [pri, setPri] = useState<Priority>("P3");
  const [updateMsg, setUpdateMsg] = useState("");
  const [updateVis, setUpdateVis] = useState<Visibility>("INTERNAL");
  const [assignUserId, setAssignUserId] = useState("");
  const [assignRole, setAssignRole] = useState<ResponderRole>("TECH_LEAD");
  const [escUserId, setEscUserId] = useState("");
  const [escType, setEscType] = useState<"HIERARCHICAL" | "FUNCTIONAL">("HIERARCHICAL");
  const [escReason, setEscReason] = useState("");

  const refreshDetail = useCallback(
    (silent: boolean) => {
      if (!silent) setLoading(true);
      return incidentApi
        .get(id)
        .then((d) => {
          setDetail(d);
          setSev(d.severity);
          setPri(d.priority);
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
          {t("incidentDetail.notFound", { defaultValue: "인시던트를 찾을 수 없습니다." })}
        </p>
        <Button onClick={() => navigate("/incidents")}>
          {t("incidentDetail.backToList", { defaultValue: "목록으로" })}
        </Button>
      </div>
    );
  }

  const transitions = detail.allowedTransitions ?? fallbackTransitions(detail.status);
  const isSev12 = detail.severity === "SEV1" || detail.severity === "SEV2";
  const showPmBanner = detail.postmortemRequired ?? (isSev12 && detail.status === "RESOLVED");
  const approved = detail.approval.approvalRequestId == null || detail.approval.status === "APPROVED";

  const timelineItems: TimelineItem[] = detail.timeline.map((entry, i) => ({
    id: String(i),
    title: entry.message,
    description:
      entry.visibility === "EXTERNAL"
        ? t("incidentDetail.visibilityExternal", { defaultValue: "외부 공개" })
        : t("incidentDetail.visibilityInternal", { defaultValue: "내부" }),
    timestamp: formatDateTime(entry.at),
    actor: entry.actor,
  }));

  const toIso = (v: string) => (v ? new Date(v).toISOString() : undefined);

  const handleUpdate = (e: FormEvent) => {
    e.preventDefault();
    if (!updateMsg.trim()) return;
    run(
      "update",
      () => incidentApi.addUpdate(id, updateMsg.trim(), updateVis),
      t("incidentDetail.updateRecorded", { defaultValue: "업데이트가 기록되었습니다" }),
    ).then(() => setUpdateMsg(""));
  };

  return (
    <TicketDetailLayout
      ticketKey={detail.ticketKey}
      title={detail.summary}
      badges={
        <>
          <StatusBadge tone={severityTone(detail.severity)} label={detail.severity} />
          <StatusBadge tone={statusTone(detail.status)} label={statusLabel(t, detail.status)} />
        </>
      }
      actions={transitions.map((target) => {
        const blocked = target === "RESOLVED" && !approved;
        return (
          <Button
            key={target}
            loading={busy === `st-${target}`}
            disabled={blocked}
            title={
              blocked
                ? t("incidentDetail.resolveBlockedTooltip", {
                    defaultValue: "승인 완료 전에는 해결 상태로 전이할 수 없습니다",
                  })
                : undefined
            }
            onClick={() =>
              run(
                `st-${target}`,
                () => incidentApi.transition(id, target),
                t("incidentDetail.transitionSuccess", {
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
      meta={
        <>
          <Card>
            <CardHeader>
              <CardTitle className="text-base">
                {t("incidentDetail.severityPriorityTitle", { defaultValue: "심각도·우선순위" })}
              </CardTitle>
            </CardHeader>
            <CardContent className="space-y-3">
              <div className="space-y-1.5">
                <Label>{t("incidentList.columnSeverity", { defaultValue: "심각도" })}</Label>
                <Select value={sev} onValueChange={(v) => setSev(v as Severity)}>
                  <SelectTrigger><SelectValue /></SelectTrigger>
                  <SelectContent>{SEVERITIES.map((s) => <SelectItem key={s} value={s}>{s}</SelectItem>)}</SelectContent>
                </Select>
              </div>
              <div className="space-y-1.5">
                <Label>{t("incidentDetail.priority", { defaultValue: "우선순위" })}</Label>
                <Select value={pri} onValueChange={(v) => setPri(v as Priority)}>
                  <SelectTrigger><SelectValue /></SelectTrigger>
                  <SelectContent>{PRIORITIES.map((p) => <SelectItem key={p} value={p}>{p}</SelectItem>)}</SelectContent>
                </Select>
              </div>
              <Button
                size="sm"
                className="w-full"
                loading={busy === "sev"}
                disabled={sev === detail.severity && pri === detail.priority}
                onClick={() =>
                  run(
                    "sev",
                    () => incidentApi.updateSeverity(id, sev, pri),
                    t("incidentDetail.severityPrioritySaved", { defaultValue: "심각도/우선순위가 변경되었습니다" }),
                  )
                }
              >
                {t("incidentDetail.save", { defaultValue: "저장" })}
              </Button>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle className="text-base">
                {t("incidentDetail.timeMetricsTitle", { defaultValue: "시간 지표" })}
              </CardTitle>
            </CardHeader>
            <CardContent className="space-y-1.5 text-sm">
              <MetaRow label="MTTD" value={formatMinutes(t, detail.metrics?.mttdMinutes)} />
              <MetaRow label="MTTA" value={formatMinutes(t, detail.metrics?.mttaMinutes)} />
              <MetaRow label="MTTR" value={formatMinutes(t, detail.metrics?.mttrMinutes)} />
            </CardContent>
          </Card>

          <ApprovalPanel
            matched={detail.approval.approvalRequestId != null}
            steps={approvalSteps}
            currentStepNo={approvalCurrentStepNo}
          />

          <Card>
            <CardHeader>
              <CardTitle className="text-base">{t("incidentDetail.respondersTitle", { defaultValue: "대응 역할" })}</CardTitle>
            </CardHeader>
            <CardContent className="space-y-2 text-sm">
              {detail.responders.length === 0 ? (
                <p className="text-muted-foreground">
                  {t("incidentDetail.noResponders", { defaultValue: "배정된 역할 없음" })}
                </p>
              ) : (
                detail.responders.map((r) => (
                  <MetaRow key={`${r.userId}-${r.role}`} label={RESPONDER_ROLE_LABEL[r.role] ?? r.role} value={r.name} />
                ))
              )}
            </CardContent>
          </Card>

          {(detail.affectedService || detail.affectedProduct) ? (
            <Card>
              <CardHeader>
                <CardTitle className="text-base">
                  {t("incidentDetail.affectedScopeTitle", { defaultValue: "영향 범위" })}
                </CardTitle>
              </CardHeader>
              <CardContent className="space-y-1.5 text-sm">
                {detail.affectedService ? (
                  <MetaRow
                    label={t("incidentDetail.affectedServiceLabel", { defaultValue: "서비스" })}
                    value={detail.affectedService}
                  />
                ) : null}
                {detail.affectedProduct ? (
                  <MetaRow
                    label={t("incidentDetail.affectedProductLabel", { defaultValue: "제품" })}
                    value={detail.affectedProduct}
                  />
                ) : null}
              </CardContent>
            </Card>
          ) : null}

          <Card>
            <CardHeader>
              <CardTitle className="text-base">
                {t("incidentDetail.linksTitle", { defaultValue: "연결 문제/자산" })}
              </CardTitle>
            </CardHeader>
            <CardContent className="space-y-1.5 text-sm">
              {detail.links.length === 0 ? (
                <p className="text-muted-foreground">
                  {t("incidentDetail.noLinks", { defaultValue: "연결 없음" })}
                </p>
              ) : (
                detail.links.map((l, i) => (
                  <span key={i} className="block text-foreground">{l.type} · {l.targetKey}</span>
                ))
              )}
            </CardContent>
          </Card>
        </>
      }
    >
      {showPmBanner ? (
        <div className="flex items-center justify-between gap-3 rounded-lg border border-warning/40 bg-warning/10 p-4">
          <div className="flex items-center gap-2 text-sm">
            <AlertTriangle className="size-5 text-warning" />
            <span>
              {t("incidentDetail.pmBanner", {
                defaultValue: "SEV1·2 해결 인시던트입니다. 포스트모템 작성이 필요합니다.",
              })}
            </span>
          </div>
          <Button variant="outline" onClick={() => navigate(`/incidents/${id}/postmortem`)}>
            {t("incidentDetail.pmWriteButton", { defaultValue: "포스트모템 작성" })}
          </Button>
        </div>
      ) : null}

      <Card>
        <CardHeader><CardTitle className="text-base">{t("incidentDetail.descriptionTitle", { defaultValue: "설명" })}</CardTitle></CardHeader>
        <CardContent>
          <p className="whitespace-pre-wrap text-sm text-foreground">
            {detail.description || t("incidentDetail.noDescription", { defaultValue: "설명 없음" })}
          </p>
        </CardContent>
      </Card>

      {/* 역할 배정 — IM 전용 */}
      {isIM ? (
        <Card>
          <CardHeader>
            <CardTitle className="text-base">
              {t("incidentDetail.assignResponderTitle", { defaultValue: "대응 역할 배정" })}
            </CardTitle>
          </CardHeader>
          <CardContent>
            <form
              className="flex flex-wrap items-end gap-2"
              onSubmit={(e) => {
                e.preventDefault();
                if (!assignUserId) return;
                run(
                  "assign",
                  () => incidentApi.assignRole(id, Number(assignUserId), assignRole),
                  t("incidentDetail.assignSuccess", { defaultValue: "역할이 배정되었습니다" }),
                ).then(() => setAssignUserId(""));
              }}
            >
              <div className="space-y-1">
                <Label htmlFor="au">{t("incidentDetail.userId", { defaultValue: "사용자 ID" })}</Label>
                <Input id="au" type="number" className="w-28" value={assignUserId} onChange={(e) => setAssignUserId(e.target.value)} />
              </div>
              <div className="space-y-1">
                <Label>{t("incidentDetail.role", { defaultValue: "역할" })}</Label>
                <Select value={assignRole} onValueChange={(v) => setAssignRole(v as ResponderRole)}>
                  <SelectTrigger className="w-36"><SelectValue /></SelectTrigger>
                  <SelectContent>
                    {(Object.keys(RESPONDER_ROLE_LABEL) as ResponderRole[]).map((r) => (
                      <SelectItem key={r} value={r}>{RESPONDER_ROLE_LABEL[r]}</SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
              <Button type="submit" loading={busy === "assign"} disabled={!assignUserId}>
                {t("incidentDetail.assign", { defaultValue: "배정" })}
              </Button>
            </form>
          </CardContent>
        </Card>
      ) : null}

      {/* 에스컬레이션 */}
      <Card>
        <CardHeader><CardTitle className="text-base">{t("incidentDetail.escalationTitle", { defaultValue: "에스컬레이션" })}</CardTitle></CardHeader>
        <CardContent>
          <form
            className="flex flex-wrap items-end gap-2"
            onSubmit={(e) => {
              e.preventDefault();
              if (!escUserId) return;
              run(
                "esc",
                () => incidentApi.escalate(id, Number(escUserId), escType, escReason.trim() || undefined),
                t("incidentDetail.escalateSuccess", { defaultValue: "에스컬레이션되었습니다" }),
              ).then(() => {
                setEscUserId("");
                setEscReason("");
              });
            }}
          >
            <div className="space-y-1">
              <Label htmlFor="eu">{t("incidentDetail.escalateTargetUserId", { defaultValue: "대상 사용자 ID" })}</Label>
              <Input id="eu" type="number" className="w-28" value={escUserId} onChange={(e) => setEscUserId(e.target.value)} />
            </div>
            <div className="space-y-1">
              <Label>{t("incidentDetail.escalateType", { defaultValue: "유형" })}</Label>
              <Select value={escType} onValueChange={(v) => setEscType(v as "HIERARCHICAL" | "FUNCTIONAL")}>
                <SelectTrigger className="w-36"><SelectValue /></SelectTrigger>
                <SelectContent>
                  <SelectItem value="HIERARCHICAL">
                    {t("incidentDetail.escalateHierarchical", { defaultValue: "계층적" })}
                  </SelectItem>
                  <SelectItem value="FUNCTIONAL">
                    {t("incidentDetail.escalateFunctional", { defaultValue: "기능적" })}
                  </SelectItem>
                </SelectContent>
              </Select>
            </div>
            <div className="flex-1 space-y-1">
              <Label htmlFor="er">{t("incidentDetail.escalateReason", { defaultValue: "사유" })}</Label>
              <Input id="er" value={escReason} onChange={(e) => setEscReason(e.target.value)} />
            </div>
            <Button type="submit" variant="outline" loading={busy === "esc"} disabled={!escUserId}>
              {t("incidentDetail.escalationTitle", { defaultValue: "에스컬레이션" })}
            </Button>
          </form>
        </CardContent>
      </Card>

      {/* 해결 처리 */}
      {detail.status !== "CLOSED" ? (
        <Card>
          <CardHeader>
            <CardTitle className="text-base">
              {t("incidentDetail.resolveTitle", { defaultValue: "해결 처리 (시간 지표)" })}
            </CardTitle>
          </CardHeader>
          <CardContent>
            <ResolveForm
              t={t}
              busy={busy === "resolve"}
              approved={approved}
              onSubmit={(vals) =>
                run(
                  "resolve",
                  () => incidentApi.resolve(id, {
                    impactStartAt: toIso(vals.impactStartAt),
                    detectedAt: toIso(vals.detectedAt),
                    impactEndAt: toIso(vals.impactEndAt),
                    resolutionNote: vals.resolutionNote.trim() || undefined,
                  }),
                  t("incidentDetail.resolveSuccess", { defaultValue: "해결 처리되었습니다" }),
                  true,
                )
              }
            />
          </CardContent>
        </Card>
      ) : null}

      {/* 문제 연계 */}
      <LinkProblemCard
        t={t}
        busy={busy === "link"}
        onLink={(problemId, createNew) =>
          run(
            "link",
            () => incidentApi.linkProblem(id, problemId, createNew),
            t("incidentDetail.linkProblemSuccess", { defaultValue: "문제가 연계되었습니다" }),
          )
        }
      />

      {/* 상태 업데이트 + 타임라인 */}
      <Card>
        <CardHeader>
          <CardTitle className="text-base">
            {t("incidentDetail.updateTimelineTitle", { defaultValue: "상태 업데이트·타임라인" })}
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <form onSubmit={handleUpdate} className="flex flex-wrap items-end gap-2">
            <div className="flex-1 space-y-1">
              <Label htmlFor="um">{t("incidentDetail.updateMessage", { defaultValue: "업데이트 메시지" })}</Label>
              <Input
                id="um"
                value={updateMsg}
                onChange={(e) => setUpdateMsg(e.target.value)}
                placeholder={t("incidentDetail.updateMessagePlaceholder", { defaultValue: "진행 상황을 입력하세요" })}
              />
            </div>
            <div className="space-y-1">
              <Label>{t("incidentDetail.visibility", { defaultValue: "공개 범위" })}</Label>
              <Select value={updateVis} onValueChange={(v) => setUpdateVis(v as Visibility)}>
                <SelectTrigger className="w-32"><SelectValue /></SelectTrigger>
                <SelectContent>
                  <SelectItem value="INTERNAL">
                    {t("incidentDetail.visibilityInternal", { defaultValue: "내부" })}
                  </SelectItem>
                  <SelectItem value="EXTERNAL">
                    {t("incidentDetail.visibilityExternal", { defaultValue: "외부" })}
                  </SelectItem>
                </SelectContent>
              </Select>
            </div>
            <Button type="submit" loading={busy === "update"} disabled={!updateMsg.trim()}>
              {t("incidentDetail.updateSubmit", { defaultValue: "기록" })}
            </Button>
          </form>

          {timelineItems.length === 0 ? (
            <p className="text-sm text-muted-foreground">
              {t("incidentDetail.noTimeline", { defaultValue: "이력이 없습니다." })}
            </p>
          ) : (
            <Timeline items={timelineItems} />
          )}
        </CardContent>
      </Card>
    </TicketDetailLayout>
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

function ResolveForm({
  t,
  busy,
  approved,
  onSubmit,
}: {
  t: TFunction;
  busy: boolean;
  approved: boolean;
  onSubmit: (v: { impactStartAt: string; detectedAt: string; impactEndAt: string; resolutionNote: string }) => void;
}) {
  const [impactStartAt, setImpactStartAt] = useState("");
  const [detectedAt, setDetectedAt] = useState("");
  const [impactEndAt, setImpactEndAt] = useState("");
  const [resolutionNote, setResolutionNote] = useState("");
  return (
    <form
      className="space-y-3"
      onSubmit={(e) => {
        e.preventDefault();
        onSubmit({ impactStartAt, detectedAt, impactEndAt, resolutionNote });
      }}
    >
      <div className="grid gap-3 sm:grid-cols-3">
        <div className="space-y-1">
          <Label htmlFor="is">{t("incidentDetail.impactStart", { defaultValue: "영향 시작" })}</Label>
          <Input id="is" type="datetime-local" value={impactStartAt} onChange={(e) => setImpactStartAt(e.target.value)} />
        </div>
        <div className="space-y-1">
          <Label htmlFor="dt">{t("incidentDetail.detectedAt", { defaultValue: "탐지" })}</Label>
          <Input id="dt" type="datetime-local" value={detectedAt} onChange={(e) => setDetectedAt(e.target.value)} />
        </div>
        <div className="space-y-1">
          <Label htmlFor="ie">{t("incidentDetail.impactEnd", { defaultValue: "영향 종료" })}</Label>
          <Input id="ie" type="datetime-local" value={impactEndAt} onChange={(e) => setImpactEndAt(e.target.value)} />
        </div>
      </div>
      <div className="space-y-1">
        <Label htmlFor="rn">{t("incidentDetail.resolutionNote", { defaultValue: "해결 메모" })}</Label>
        <Input id="rn" value={resolutionNote} onChange={(e) => setResolutionNote(e.target.value)} />
      </div>
      <div className="flex justify-end">
        <Button
          type="submit"
          loading={busy}
          disabled={!approved}
          title={
            !approved
              ? t("incidentDetail.resolveBlockedFormTooltip", { defaultValue: "승인 완료 전에는 해결 처리할 수 없습니다" })
              : undefined
          }
        >
          {t("incidentDetail.resolveSubmit", { defaultValue: "해결 처리" })}
        </Button>
      </div>
    </form>
  );
}

function LinkProblemCard({
  t,
  busy,
  onLink,
}: {
  t: TFunction;
  busy: boolean;
  onLink: (problemId: number | undefined, createNew: boolean) => void;
}) {
  const [problemId, setProblemId] = useState("");

  return (
    <Card>
      <CardHeader><CardTitle className="text-base">{t("incidentDetail.linkProblemTitle", { defaultValue: "문제 연계" })}</CardTitle></CardHeader>
      <CardContent>
        <form
          className="flex flex-wrap items-end gap-2"
          onSubmit={(e) => {
            e.preventDefault();
            if (!problemId) return;
            onLink(Number(problemId), false);
            setProblemId("");
          }}
        >
          <div className="space-y-1.5">
            <Label htmlFor="prbId">{t("incidentDetail.problemId", { defaultValue: "문제 ID" })}</Label>
            <Input id="prbId" type="number" className="w-40" value={problemId} onChange={(e) => setProblemId(e.target.value)} />
          </div>
          <Button type="submit" loading={busy} disabled={!problemId}>
            {t("incidentDetail.linkExistingProblem", { defaultValue: "기존 문제 연계" })}
          </Button>
          <Button type="button" variant="outline" loading={busy} onClick={() => onLink(undefined, true)}>
            {t("incidentDetail.createAndLinkProblem", { defaultValue: "신규 문제 생성·연계" })}
          </Button>
        </form>
      </CardContent>
    </Card>
  );
}
