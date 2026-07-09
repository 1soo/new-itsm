import { type FormEvent, useCallback, useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";

import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  Rating,
  StatusBadge,
  TicketDetailLayout,
  Timeline,
  type TimelineItem,
  toast,
} from "@/components/common";
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
import { useAppSelector } from "@/store/hooks";
import { extractErrorMessage } from "@/lib/apiClient";

/*
 * 요청 상세(SCR-SRM-005) — 검증·승인 상태·이행·종료·코멘트·SLA·CSAT 관리.
 * 허용 전이만 노출(BE allowedTransitions 우선, 없으면 status/역할/승인으로 유추),
 * 승인 대기 중 이행 버튼 숨김, 종료 요청 재종료 차단, CSAT는 종료+요청자에게만 노출.
 */
const APPROVAL_LABEL: Record<string, string> = {
  PENDING: "승인 대기",
  APPROVED: "승인됨",
  REJECTED: "반려됨",
};

const SLA_STATUS_SET = new Set(["OK", "WARNING", "BREACHED"]);

function slaBadgeProps(value: string) {
  if (SLA_STATUS_SET.has(value)) {
    const v = value as SlaStatus;
    return { tone: slaTone(v), label: slaLabel(v) };
  }
  return { tone: "muted" as const, label: value };
}

function fallbackTransitions(detail: RequestDetail, isAgent: boolean, isEndUser: boolean): TargetStatus[] {
  const s = detail.status;
  if (s === "CLOSED" || s === "REJECTED") return [];
  const approvalPending = detail.approval?.required && detail.approval?.status === "PENDING";
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
  const params = useParams();
  const navigate = useNavigate();
  const id = Number(params.id);
  const user = useAppSelector((s) => s.auth.user);
  const isAgent = hasAnyRole(user?.roles, [ROLE_SERVICE_DESK_AGENT]);
  const isEndUser = hasAnyRole(user?.roles, [ROLE_END_USER]);

  const [detail, setDetail] = useState<RequestDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [notFound, setNotFound] = useState(false);

  const [comment, setComment] = useState("");
  const [commenting, setCommenting] = useState(false);
  const [transitioning, setTransitioning] = useState<TargetStatus | null>(null);

  const [csatScore, setCsatScore] = useState(0);
  const [csatComment, setCsatComment] = useState("");
  const [csatSubmitting, setCsatSubmitting] = useState(false);
  const [csatDone, setCsatDone] = useState(false);

  const load = useCallback(() => {
    setLoading(true);
    srmApi
      .getRequest(id)
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

  useEffect(load, [load]);

  const handleTransition = async (target: TargetStatus) => {
    setTransitioning(target);
    try {
      await srmApi.transition(id, target);
      toast.success(`상태가 '${statusLabel(target)}'로 변경되었습니다`);
      load();
    } catch (err) {
      toast.error(extractErrorMessage(err));
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
      toast.error("별점을 선택하세요.");
      return;
    }
    setCsatSubmitting(true);
    try {
      await srmApi.submitCsat(id, csatScore, csatComment.trim() || undefined);
      toast.success("평가가 제출되었습니다. 감사합니다.");
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
        <p className="text-sm text-muted-foreground">요청을 찾을 수 없습니다.</p>
        <Button onClick={() => navigate(-1)}>이전으로</Button>
      </div>
    );
  }

  const transitions = detail.allowedTransitions ?? fallbackTransitions(detail, isAgent, isEndUser);
  const showCsat = detail.status === "CLOSED" && isEndUser;

  const timelineItems: TimelineItem[] = detail.timeline.map((t, i) => ({
    id: String(i),
    title: t.message,
    timestamp: formatDateTime(t.at),
  }));

  const formEntries = Object.entries(detail.formValues ?? {});

  return (
    <TicketDetailLayout
      ticketKey={detail.ticketKey}
      title={detail.catalogItemName}
      badges={<StatusBadge tone={statusTone(detail.status)} label={statusLabel(detail.status)} />}
      actions={transitions.map((t) => (
        <Button
          key={t}
          variant={t === "CLOSED" ? "outline" : "default"}
          loading={transitioning === t}
          onClick={() => handleTransition(t)}
        >
          {statusLabel(t)}
        </Button>
      ))}
      meta={
        <>
          <Card>
            <CardHeader>
              <CardTitle className="text-base">정보</CardTitle>
            </CardHeader>
            <CardContent className="space-y-2 text-sm">
              <MetaRow label="요청자" value={detail.requester} />
              <MetaRow label="담당자" value={detail.assignee || "미배정"} />
              <MetaRow label="큐" value={detail.queue || "-"} />
            </CardContent>
          </Card>

          {detail.approval?.required ? (
            <Card>
              <CardHeader>
                <CardTitle className="text-base">승인</CardTitle>
              </CardHeader>
              <CardContent className="space-y-2 text-sm">
                <MetaRow
                  label="상태"
                  value={detail.approval.status ? (APPROVAL_LABEL[detail.approval.status] ?? detail.approval.status) : "-"}
                />
                {detail.approval.status === "REJECTED" && detail.approval.reason ? (
                  <p className="text-sm text-danger">반려 사유: {detail.approval.reason}</p>
                ) : null}
              </CardContent>
            </Card>
          ) : null}

          <Card>
            <CardHeader>
              <CardTitle className="text-base">SLA</CardTitle>
            </CardHeader>
            <CardContent className="flex flex-col gap-2 text-sm">
              <div className="flex items-center justify-between">
                <span className="text-muted-foreground">응답</span>
                <StatusBadge {...slaBadgeProps(detail.sla.responseStatus)} />
              </div>
              <div className="flex items-center justify-between">
                <span className="text-muted-foreground">해결</span>
                <StatusBadge {...slaBadgeProps(detail.sla.resolveStatus)} />
              </div>
            </CardContent>
          </Card>
        </>
      }
    >
      {/* 요청 양식 값 */}
      <Card>
        <CardHeader>
          <CardTitle className="text-base">요청 내용</CardTitle>
        </CardHeader>
        <CardContent>
          {formEntries.length === 0 ? (
            <p className="text-sm text-muted-foreground">입력된 값이 없습니다.</p>
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
            <CardTitle className="text-base">만족도 평가</CardTitle>
          </CardHeader>
          <CardContent className="space-y-3">
            {csatDone ? (
              <p className="text-sm text-success">평가해 주셔서 감사합니다.</p>
            ) : (
              <>
                <Rating value={csatScore} onChange={setCsatScore} size="lg" />
                <Input
                  value={csatComment}
                  onChange={(e) => setCsatComment(e.target.value)}
                  placeholder="의견(선택)"
                />
                <Button onClick={handleCsat} loading={csatSubmitting}>
                  평가 제출
                </Button>
              </>
            )}
          </CardContent>
        </Card>
      ) : null}

      {/* 코멘트 */}
      <Card>
        <CardHeader>
          <CardTitle className="text-base">코멘트</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          {detail.comments.length === 0 ? (
            <p className="text-sm text-muted-foreground">코멘트가 없습니다.</p>
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
              <Label htmlFor="comment">코멘트 작성</Label>
              <Input
                id="comment"
                value={comment}
                onChange={(e) => setComment(e.target.value)}
                placeholder="메시지를 입력하세요"
              />
            </div>
            <Button type="submit" loading={commenting} disabled={!comment.trim()}>
              등록
            </Button>
          </form>
        </CardContent>
      </Card>

      {/* 타임라인 */}
      <Card>
        <CardHeader>
          <CardTitle className="text-base">타임라인</CardTitle>
        </CardHeader>
        <CardContent>
          {timelineItems.length === 0 ? (
            <p className="text-sm text-muted-foreground">이력이 없습니다.</p>
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
