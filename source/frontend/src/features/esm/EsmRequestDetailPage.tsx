import { type FormEvent, useCallback, useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";

import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  StatusBadge,
  TicketDetailLayout,
  Timeline,
  type TimelineItem,
  toast,
} from "@/components/common";
import { FullscreenLoader } from "@/routes/FullscreenLoader";
import { hasAnyRole, ROLE_DEPT_COORDINATOR } from "@/features/auth/roles";
import { esmApi } from "@/features/esm/api";
import { formatDateTime } from "@/features/esm/format";
import {
  checklistStatusLabel,
  checklistStatusTone,
  departmentLabel,
  requestStatusLabel,
  requestStatusTone,
} from "@/features/esm/status";
import type { ChecklistDetail, EsmComment, EsmRequestDetail, EsmRequestTargetStatus } from "@/features/esm/types";
import { useAppSelector } from "@/store/hooks";
import { extractErrorMessage } from "@/lib/apiClient";

/*
 * 부서 요청 상세(SCR-ESM-005) — 상태 처리(담당 부서 처리자만)·코멘트·연계 체크리스트 요약.
 * BE 응답에 allowedTransitions가 없어 상태값 기준 FE 폴백으로 다음 전이를 계산한다.
 */
function fallbackTransitions(status: EsmRequestDetail["status"]): EsmRequestTargetStatus[] {
  if (status === "SUBMITTED") return ["IN_PROGRESS", "COMPLETED", "REJECTED"];
  if (status === "IN_PROGRESS") return ["COMPLETED", "REJECTED"];
  return [];
}

export function EsmRequestDetailPage() {
  const params = useParams();
  const navigate = useNavigate();
  const id = Number(params.id);
  const roles = useAppSelector((s) => s.auth.user?.roles);
  const isCoordinator = hasAnyRole(roles, [ROLE_DEPT_COORDINATOR]);

  const [detail, setDetail] = useState<EsmRequestDetail | null>(null);
  const [checklist, setChecklist] = useState<ChecklistDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [notFound, setNotFound] = useState(false);
  const [comment, setComment] = useState("");
  const [commenting, setCommenting] = useState(false);
  const [transitioning, setTransitioning] = useState<EsmRequestTargetStatus | null>(null);

  const load = useCallback(() => {
    setLoading(true);
    esmApi
      .getRequest(id)
      .then((d) => {
        setDetail(d);
        setNotFound(false);
        if (d.checklistId) {
          esmApi.getChecklist(d.checklistId).then(setChecklist).catch(() => setChecklist(null));
        } else {
          setChecklist(null);
        }
      })
      .catch((err) => {
        toast.error(extractErrorMessage(err));
        setNotFound(true);
      })
      .finally(() => setLoading(false));
  }, [id]);

  useEffect(load, [load]);

  const handleTransition = async (target: EsmRequestTargetStatus) => {
    setTransitioning(target);
    try {
      await esmApi.transition(id, target);
      toast.success(`상태가 '${requestStatusLabel(target)}'로 변경되었습니다`);
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
      const created: EsmComment = await esmApi.addComment(id, comment.trim());
      setDetail((d) => (d ? { ...d, comments: [...d.comments, created] } : d));
      setComment("");
    } catch (err) {
      toast.error(extractErrorMessage(err));
    } finally {
      setCommenting(false);
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

  const transitions = isCoordinator ? fallbackTransitions(detail.status) : [];
  const formEntries = Object.entries(detail.formValues ?? {});
  const timelineItems: TimelineItem[] = detail.timeline.map((t, i) => ({
    id: String(i),
    title: t.message,
    timestamp: formatDateTime(t.at),
  }));

  return (
    <TicketDetailLayout
      ticketKey={detail.ticketKey}
      title={detail.catalogItemName}
      badges={
        <>
          <StatusBadge tone="info" label={departmentLabel(detail.department)} />
          <StatusBadge tone={requestStatusTone(detail.status)} label={requestStatusLabel(detail.status)} />
        </>
      }
      actions={transitions.map((t) => (
        <Button
          key={t}
          variant={t === "REJECTED" ? "outline" : "default"}
          loading={transitioning === t}
          onClick={() => handleTransition(t)}
        >
          {requestStatusLabel(t)}
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
            </CardContent>
          </Card>

          {checklist ? (
            <Card
              role="button"
              tabIndex={0}
              onClick={() => navigate(`/esm/checklists/${checklist.id}`)}
              onKeyDown={(e) => {
                if (e.key === "Enter") navigate(`/esm/checklists/${checklist.id}`);
              }}
              className="cursor-pointer border-l-4 border-l-[color:var(--info)] transition-colors hover:border-primary"
            >
              <CardHeader>
                <CardTitle className="text-base">연계 체크리스트</CardTitle>
              </CardHeader>
              <CardContent className="space-y-2 text-sm">
                <div className="flex items-center justify-between">
                  <span className="text-muted-foreground">진행률</span>
                  <span className="text-foreground">
                    {checklist.tasks.filter((t) => t.status === "DONE").length} / {checklist.tasks.length}
                  </span>
                </div>
                <StatusBadge tone={checklistStatusTone(checklist.status)} label={checklistStatusLabel(checklist.status)} />
              </CardContent>
            </Card>
          ) : null}
        </>
      }
    >
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
