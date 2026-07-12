import { type FormEvent, useCallback, useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { useTranslation } from "react-i18next";
import type { TFunction } from "i18next";

import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  ApprovalPanel,
  Rating,
  StatusBadge,
  TicketDetailLayout,
  Timeline,
  type TimelineItem,
  toast,
} from "@/components/common";
import type { ApprovalStep } from "@/components/common";
import { FullscreenLoader } from "@/routes/FullscreenLoader";
import {
  hasAnyRole,
  ROLE_END_USER,
  ROLE_SERVICE_DESK_AGENT,
} from "@/features/auth/roles";
import { srmApi } from "@/features/service-request/api";
import { formatDateTime } from "@/features/service-request/format";
import {
  slaLabel,
  slaTone,
  statusLabel,
  statusTone,
} from "@/features/service-request/status";
import type {
  RequestComment,
  RequestDetail,
  SlaStatus,
  TargetStatus,
} from "@/features/service-request/types";
import { commonApi } from "@/features/common/api";
import { useAppSelector } from "@/store/hooks";
import { extractErrorMessage } from "@/lib/apiClient";

/*
 * 요청 상세(SCR-SRM-005) — 검증·승인 상태·이행·종료·코멘트·SLA·CSAT 관리.
 * 허용 전이만 노출(BE allowedTransitions 우선, 없으면 status/역할/승인으로 유추),
 * 승인 대기 중 이행 버튼 숨김, 종료 요청 재종료 차단, CSAT는 종료+요청자에게만 노출.
 * 승인 상태(공용, API-COM-004)는 진행 상태 조회 전용(공용 ApprovalPanel) — 결정 처리는 SCR-COM-014에서 수행한다.
 */
const SLA_STATUS_SET = new Set(["OK", "WARNING", "BREACHED"]);

function slaBadgeProps(t: TFunction, value: string) {
  if (SLA_STATUS_SET.has(value)) {
    const v = value as SlaStatus;
    return { tone: slaTone(v), label: slaLabel(t, v) };
  }
  return { tone: "muted" as const, label: value };
}

function fallbackTransitions(detail: RequestDetail, isAgent: boolean, isEndUser: boolean): TargetStatus[] {
  const s = detail.status;
  if (s === "CLOSED" || s === "REJECTED") return [];
  const approvalPending = detail.approval?.status === "IN_PROGRESS";
  const out: TargetStatus[] = [];
  if (isAgent) {
    if (s === "SUBMITTED") out.push("VALIDATED");
    else if (s === "VALIDATED") out.push("ROUTED");
    else if (s === "ROUTED" && !approvalPending) out.push("IN_FULFILLMENT");
    else if (s === "APPROVAL_PENDING" && detail.approval?.status === "APPROVED") out.push("IN_FULFILLMENT");
    else if (s === "IN_FULFILLMENT") out.push("FULFILLED");
    else if (s === "FULFILLED") out.push("CLOSED");
  }
  // 요청자는 이행 완료된 요청을 종료 확인할 수 있다(요청자 범위).
  if (isEndUser && s === "FULFILLED" && !out.includes("CLOSED")) out.push("CLOSED");
  return out;
}

export function RequestDetailPage() {
  const { t } = useTranslation("service-request");
  const params = useParams();
  const navigate = useNavigate();
  const id = Number(params.id);
  const user = useAppSelector((s) => s.auth.user);
  const isAgent = hasAnyRole(user?.roles, [ROLE_SERVICE_DESK_AGENT]);
  const isEndUser = hasAnyRole(user?.roles, [ROLE_END_USER]);

  const [detail, setDetail] = useState<RequestDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [notFound, setNotFound] = useState(false);

  const [approvalSteps, setApprovalSteps] = useState<ApprovalStep[]>([]);
  const [approvalCurrentStepNo, setApprovalCurrentStepNo] = useState<number | null>(null);

  const [comment, setComment] = useState("");
  const [commenting, setCommenting] = useState(false);
  const [transitioning, setTransitioning] = useState<TargetStatus | null>(null);

  const [csatScore, setCsatScore] = useState(0);
  const [csatComment, setCsatComment] = useState("");
  const [csatSubmitting, setCsatSubmitting] = useState(false);
  const [csatDone, setCsatDone] = useState(false);

  const refreshDetail = useCallback(
    (silent: boolean) => {
      if (!silent) setLoading(true);
      return srmApi
        .getRequest(id)
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

  const handleTransition = async (target: TargetStatus) => {
    setTransitioning(target);
    try {
      await srmApi.transition(id, target);
      toast.success(
        t("requestDetail.transitionSuccess", {
          status: statusLabel(t, target),
          defaultValue: `상태가 '${statusLabel(t, target)}'로 변경되었습니다`,
        }),
      );
      load();
    } catch (err) {
      toast.error(extractErrorMessage(err));
      // 이행(IN_FULFILLMENT) 전이가 게이트(409)로 거부된 경우 BE가 이미 승인 인스턴스를 생성했을
      // 수 있으므로, 전체 로딩 화면 없이 조용히 다시 조회해 승인 패널에 반영한다.
      refreshDetail(true);
    } finally {
      setTransitioning(null);
    }
  };

  const handleComment = async (e: FormEvent) => {
    e.preventDefault();
    if (!comment.trim()) return;
    setCommenting(true);
    try {
      const created: RequestComment = await srmApi.addComment(id, comment.trim());
      setDetail((d) => (d ? { ...d, comments: [...d.comments, created] } : d));
      setComment("");
    } catch (err) {
      toast.error(extractErrorMessage(err));
    } finally {
      setCommenting(false);
    }
  };

  const handleCsat = async () => {
    if (csatScore < 1) {
      toast.error(t("requestDetail.csatRatingRequired", { defaultValue: "별점을 선택하세요." }));
      return;
    }
    setCsatSubmitting(true);
    try {
      await srmApi.submitCsat(id, csatScore, csatComment.trim() || undefined);
      toast.success(t("requestDetail.csatSuccess", { defaultValue: "평가가 제출되었습니다. 감사합니다." }));
      setCsatDone(true);
    } catch (err) {
      toast.error(extractErrorMessage(err));
    } finally {
      setCsatSubmitting(false);
    }
  };

  if (loading) return <FullscreenLoader />;
  if (notFound || !detail) {
    return (
      <div className="mx-auto max-w-lg space-y-4 text-center">
        <p className="text-sm text-muted-foreground">
          {t("requestDetail.notFound", { defaultValue: "요청을 찾을 수 없습니다." })}
        </p>
        <Button onClick={() => navigate(-1)}>{t("requestDetail.back", { defaultValue: "이전으로" })}</Button>
      </div>
    );
  }

  // 승인 대기 중(매칭 규칙 있고 미승인)에는 이행 전이 버튼을 숨긴다(service-request.md, BE가 제공하는
  // allowedTransitions에도 승인 게이트가 반영되지 않을 수 있어 FE에서 한 번 더 걸러낸다).
  const approvalPending = detail.approval.status === "IN_PROGRESS";
  const transitions = (detail.allowedTransitions ?? fallbackTransitions(detail, isAgent, isEndUser)).filter(
    (target) => !(target === "IN_FULFILLMENT" && approvalPending),
  );
  const showCsat = detail.status === "CLOSED" && isEndUser;

  const timelineItems: TimelineItem[] = detail.timeline.map((entry, i) => ({
    id: String(i),
    title: entry.message,
    timestamp: formatDateTime(entry.at),
  }));

  const formEntries = Object.entries(detail.formValues ?? {});

  return (
    <TicketDetailLayout
      ticketKey={detail.ticketKey}
      title={detail.catalogItemName}
      badges={<StatusBadge tone={statusTone(detail.status)} label={statusLabel(t, detail.status)} />}
      actions={transitions.map((target) => (
        <Button
          key={target}
          variant={target === "CLOSED" ? "outline" : "default"}
          loading={transitioning === target}
          onClick={() => handleTransition(target)}
        >
          {statusLabel(t, target)}
        </Button>
      ))}
      meta={
        <>
          <Card>
            <CardHeader>
              <CardTitle className="text-base">{t("requestDetail.infoTitle", { defaultValue: "정보" })}</CardTitle>
            </CardHeader>
            <CardContent className="space-y-2 text-sm">
              <MetaRow label={t("requestDetail.requester", { defaultValue: "요청자" })} value={detail.requester} />
              <MetaRow
                label={t("requestDetail.assignee", { defaultValue: "담당자" })}
                value={detail.assignee || t("requestQueue.unassigned", { defaultValue: "미배정" })}
              />
              <MetaRow label={t("requestDetail.queue", { defaultValue: "큐" })} value={detail.queue || "-"} />
            </CardContent>
          </Card>

          <ApprovalPanel
            matched={detail.approval.approvalRequestId != null}
            steps={approvalSteps}
            currentStepNo={approvalCurrentStepNo}
            emptyMessage={t("requestDetail.noApproval", { defaultValue: "이 요청에는 승인 절차가 없습니다" })}
          />

          <Card>
            <CardHeader>
              <CardTitle className="text-base">SLA</CardTitle>
            </CardHeader>
            <CardContent className="flex flex-col gap-2 text-sm">
              <div className="flex items-center justify-between">
                <span className="text-muted-foreground">
                  {t("requestDetail.slaResponse", { defaultValue: "응답" })}
                </span>
                <StatusBadge {...slaBadgeProps(t, detail.sla.responseStatus)} />
              </div>
              <div className="flex items-center justify-between">
                <span className="text-muted-foreground">
                  {t("requestDetail.slaResolve", { defaultValue: "해결" })}
                </span>
                <StatusBadge {...slaBadgeProps(t, detail.sla.resolveStatus)} />
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle className="text-base">
                {t("requestDetail.linkedAssetsTitle", { defaultValue: "연결 자산" })}
              </CardTitle>
            </CardHeader>
            <CardContent className="space-y-1.5 text-sm">
              {detail.linkedAssets.length === 0 ? (
                <p className="text-muted-foreground">
                  {t("requestDetail.noLinkedAssets", { defaultValue: "연결 없음" })}
                </p>
              ) : (
                detail.linkedAssets.map((a) => (
                  <span key={a.id} className="block text-foreground">{a.assetKey}</span>
                ))
              )}
            </CardContent>
          </Card>
        </>
      }
    >
      {/* 요청 양식 값 */}
      <Card>
        <CardHeader>
          <CardTitle className="text-base">{t("requestDetail.formValuesTitle", { defaultValue: "요청 내용" })}</CardTitle>
        </CardHeader>
        <CardContent>
          {formEntries.length === 0 ? (
            <p className="text-sm text-muted-foreground">
              {t("requestDetail.noFormValues", { defaultValue: "입력된 값이 없습니다." })}
            </p>
          ) : (
            <dl className="grid grid-cols-[8rem_1fr] gap-y-2 text-sm">
              {formEntries.map(([k, v]) => (
                <div key={k} className="contents">
                  <dt className="text-muted-foreground">{k}</dt>
                  <dd className="text-foreground">{v == null || v === "" ? "-" : String(v)}</dd>
                </div>
              ))}
            </dl>
          )}
        </CardContent>
      </Card>

      {/* CSAT — 종료 + 요청자 */}
      {showCsat ? (
        <Card>
          <CardHeader>
            <CardTitle className="text-base">{t("requestDetail.csatTitle", { defaultValue: "만족도 평가" })}</CardTitle>
          </CardHeader>
          <CardContent className="space-y-3">
            {csatDone ? (
              <p className="text-sm text-success">
                {t("requestDetail.csatThanks", { defaultValue: "평가해 주셔서 감사합니다." })}
              </p>
            ) : (
              <>
                <Rating value={csatScore} onChange={setCsatScore} size="lg" />
                <Input
                  value={csatComment}
                  onChange={(e) => setCsatComment(e.target.value)}
                  placeholder={t("requestDetail.csatCommentPlaceholder", { defaultValue: "의견(선택)" })}
                />
                <Button onClick={handleCsat} loading={csatSubmitting}>
                  {t("requestDetail.csatSubmit", { defaultValue: "평가 제출" })}
                </Button>
              </>
            )}
          </CardContent>
        </Card>
      ) : null}

      {/* 코멘트 */}
      <Card>
        <CardHeader>
          <CardTitle className="text-base">{t("requestDetail.commentsTitle", { defaultValue: "코멘트" })}</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          {detail.comments.length === 0 ? (
            <p className="text-sm text-muted-foreground">
              {t("requestDetail.noComments", { defaultValue: "코멘트가 없습니다." })}
            </p>
          ) : (
            <ul className="space-y-3">
              {detail.comments.map((c) => (
                <li key={c.id} className="rounded-md border border-border p-3">
                  <div className="mb-1 flex items-center justify-between text-xs text-muted-foreground">
                    <span className="font-medium text-foreground">{c.author}</span>
                    <span>{formatDateTime(c.createdAt)}</span>
                  </div>
                  <p className="whitespace-pre-wrap text-sm text-foreground">{c.body}</p>
                </li>
              ))}
            </ul>
          )}
          <form onSubmit={handleComment} className="flex items-end gap-2">
            <div className="flex-1 space-y-1">
              <Label htmlFor="comment">{t("requestDetail.commentWrite", { defaultValue: "코멘트 작성" })}</Label>
              <Input
                id="comment"
                value={comment}
                onChange={(e) => setComment(e.target.value)}
                placeholder={t("requestDetail.commentPlaceholder", { defaultValue: "메시지를 입력하세요" })}
              />
            </div>
            <Button type="submit" loading={commenting} disabled={!comment.trim()}>
              {t("requestDetail.commentSubmit", { defaultValue: "등록" })}
            </Button>
          </form>
        </CardContent>
      </Card>

      {/* 타임라인 */}
      <Card>
        <CardHeader>
          <CardTitle className="text-base">{t("requestDetail.timelineTitle", { defaultValue: "타임라인" })}</CardTitle>
        </CardHeader>
        <CardContent>
          {timelineItems.length === 0 ? (
            <p className="text-sm text-muted-foreground">
              {t("requestDetail.noTimeline", { defaultValue: "이력이 없습니다." })}
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
