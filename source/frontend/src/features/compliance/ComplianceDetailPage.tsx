import { type FormEvent, useCallback, useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";

import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { ApprovalPanel, StatusBadge, TicketDetailLayout, toast } from "@/components/common";
import type { ApprovalStep } from "@/components/common";
import { FullscreenLoader } from "@/routes/FullscreenLoader";
import { complianceApi } from "@/features/compliance/api";
import { formatDateTime } from "@/features/compliance/format";
import {
  actionStatusLabel,
  actionStatusTone,
  complianceStatusLabel,
  complianceStatusTone,
  nextActionTransition,
} from "@/features/compliance/status";
import type { ComplianceAuditLog, RequirementDetail, UpdateRequirementInput } from "@/features/compliance/types";
import { commonApi } from "@/features/common/api";
import { extractErrorMessage } from "@/lib/apiClient";

/** 시정조치 항목별 승인 진행 상태(approvalRequestId 기준, API-COM-004 조회 결과). */
type ActionApprovalState = { steps: ApprovalStep[]; currentStepNo: number | null };

/*
 * 요구사항 상세(SCR-COMP-003) — 책임자 지정·시정조치 등록/순서전이(탐지→조치중→해결)·
 * 변경 요청 연계·관련 감사 로그 조회·이름/근거/적용범위 인라인 수정(API-COMP-004).
 * 준수 상태는 BE 계산값을 그대로 표시.
 */
export function ComplianceDetailPage() {
  const navigate = useNavigate();
  const id = Number(useParams().id);

  const [detail, setDetail] = useState<RequirementDetail | null>(null);
  const [actionApprovals, setActionApprovals] = useState<Record<number, ActionApprovalState>>({});
  const [logs, setLogs] = useState<ComplianceAuditLog[]>([]);
  const [loading, setLoading] = useState(true);
  const [notFound, setNotFound] = useState(false);
  const [busy, setBusy] = useState<string | null>(null);
  const [editing, setEditing] = useState(false);

  const refreshDetail = useCallback(
    (silent: boolean) => {
      if (!silent) setLoading(true);
      return Promise.all([complianceApi.get(id), complianceApi.auditLogs({ requirementId: id })])
        .then(([d, l]) => {
          setDetail(d);
          setLogs(l);
          setNotFound(false);
          const matched = d.correctiveActions.filter((a) => a.approval.approvalRequestId != null);
          if (matched.length === 0) {
            setActionApprovals({});
            return;
          }
          return Promise.all(
            matched.map((a) => commonApi.getApproval(a.approval.approvalRequestId!).then((res) => [a.id, res] as const)),
          ).then((entries) => {
            setActionApprovals(
              Object.fromEntries(
                entries.map(([actionId, res]) => [actionId, { steps: res.steps, currentStepNo: res.currentStepNo }]),
              ),
            );
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
      // 시정조치 전이가 게이트(409)로 거부된 경우 BE가 이미 승인 인스턴스를 생성했을 수 있으므로,
      // 전체 로딩 화면 없이 조용히 다시 조회해 승인 패널에 반영한다.
      if (reloadOnError) refreshDetail(true);
    } finally {
      setBusy(null);
    }
  };

  const handleSaveEdit = async (body: UpdateRequirementInput) => {
    setBusy("edit");
    try {
      await complianceApi.update(id, body);
      toast.success("요구사항이 수정되었습니다");
      setEditing(false);
      load();
    } catch (err) {
      toast.error(extractErrorMessage(err));
    } finally {
      setBusy(null);
    }
  };

  if (loading) return <FullscreenLoader />;
  if (notFound || !detail) {
    return (
      <div className="mx-auto max-w-lg space-y-4 text-center">
        <p className="text-sm text-muted-foreground">요구사항을 찾을 수 없습니다.</p>
        <Button onClick={() => navigate("/compliance/requirements")}>목록으로</Button>
      </div>
    );
  }

  return (
    <TicketDetailLayout
      ticketKey={detail.requirementKey}
      title={detail.name}
      badges={
        <StatusBadge tone={complianceStatusTone(detail.complianceStatus)} label={complianceStatusLabel(detail.complianceStatus)} />
      }
      actions={
        !editing ? (
          <Button variant="outline" onClick={() => setEditing(true)}>수정</Button>
        ) : null
      }
      meta={
        <>
          <OwnerCard
            detail={detail}
            busy={busy === "owner"}
            onSubmit={(ownerId) => run("owner", () => complianceApi.setOwner(id, ownerId), "책임자가 지정되었습니다")}
          />
          <LinkCard
            detail={detail}
            busy={busy === "link"}
            onSubmit={(changeId) => run("link", () => complianceApi.link(id, changeId), "변경 요청과 연계되었습니다")}
          />
        </>
      }
    >
      <RequirementInfoCard
        detail={detail}
        editing={editing}
        busy={busy === "edit"}
        onSave={handleSaveEdit}
        onCancel={() => setEditing(false)}
      />

      <CorrectiveActionCard
        detail={detail}
        actionApprovals={actionApprovals}
        busy={busy}
        onAdd={(description) =>
          run("add-action", () => complianceApi.addCorrectiveAction(id, { description }), "시정조치가 등록되었습니다")
        }
        onTransition={(actionId, targetStatus) =>
          run(
            `action-${actionId}`,
            () => complianceApi.transitionAction(actionId, targetStatus),
            "시정조치 상태가 변경되었습니다",
            true,
          )
        }
      />

      <AuditLogCard logs={logs} />
    </TicketDetailLayout>
  );
}

function RequirementInfoCard({
  detail,
  editing,
  busy,
  onSave,
  onCancel,
}: {
  detail: RequirementDetail;
  editing: boolean;
  busy: boolean;
  onSave: (body: UpdateRequirementInput) => void;
  onCancel: () => void;
}) {
  const [name, setName] = useState(detail.name);
  const [basis, setBasis] = useState(detail.basis);
  const [scope, setScope] = useState(detail.scope ?? "");
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (editing) {
      setName(detail.name);
      setBasis(detail.basis);
      setScope(detail.scope ?? "");
      setError(null);
    }
  }, [editing, detail]);

  const handleSubmit = (e: FormEvent) => {
    e.preventDefault();
    if (!name.trim() || !basis.trim()) {
      setError("이름과 근거는 필수입니다.");
      return;
    }
    onSave({ name: name.trim(), basis: basis.trim(), scope: scope.trim() || undefined });
  };

  if (!editing) {
    return (
      <Card>
        <CardHeader><CardTitle className="text-base">근거·적용 범위</CardTitle></CardHeader>
        <CardContent className="space-y-2 text-sm">
          <p className="text-foreground">{detail.basis}</p>
          <p className="text-muted-foreground">{detail.scope || "적용 범위 없음"}</p>
        </CardContent>
      </Card>
    );
  }

  return (
    <Card>
      <CardHeader><CardTitle className="text-base">요구사항 수정</CardTitle></CardHeader>
      <CardContent>
        <form onSubmit={handleSubmit} className="space-y-3" noValidate>
          <div className="space-y-1.5">
            <Label htmlFor="edit-name">이름</Label>
            <Input id="edit-name" value={name} onChange={(e) => setName(e.target.value)} aria-invalid={!!error && !name.trim()} required />
          </div>
          <div className="space-y-1.5">
            <Label htmlFor="edit-basis">근거</Label>
            <Input id="edit-basis" value={basis} onChange={(e) => setBasis(e.target.value)} aria-invalid={!!error && !basis.trim()} required />
          </div>
          <div className="space-y-1.5">
            <Label htmlFor="edit-scope">적용 범위</Label>
            <Input id="edit-scope" value={scope} onChange={(e) => setScope(e.target.value)} />
          </div>
          {error ? <p role="alert" className="text-sm text-danger">{error}</p> : null}
          <div className="flex justify-end gap-2">
            <Button type="button" variant="outline" onClick={onCancel}>취소</Button>
            <Button type="submit" loading={busy}>저장</Button>
          </div>
        </form>
      </CardContent>
    </Card>
  );
}

function OwnerCard({
  detail,
  busy,
  onSubmit,
}: {
  detail: RequirementDetail;
  busy: boolean;
  onSubmit: (ownerId: number) => void;
}) {
  const [ownerId, setOwnerId] = useState("");

  const handleSubmit = (e: FormEvent) => {
    e.preventDefault();
    if (!ownerId) return;
    onSubmit(Number(ownerId));
    setOwnerId("");
  };

  return (
    <Card>
      <CardHeader><CardTitle className="text-base">책임자</CardTitle></CardHeader>
      <CardContent className="space-y-2 text-sm">
        <div className="flex items-center justify-between">
          <span className="text-muted-foreground">현재 책임자</span>
          {detail.owner ? (
            <span className="text-foreground">{detail.owner}</span>
          ) : (
            <StatusBadge tone="danger" label="미지정" />
          )}
        </div>
        <form onSubmit={handleSubmit} className="flex items-end gap-2">
          <div className="flex-1 space-y-1.5">
            <Label htmlFor="ownerId">사용자 ID</Label>
            <Input id="ownerId" type="number" value={ownerId} onChange={(e) => setOwnerId(e.target.value)} />
          </div>
          <Button type="submit" size="sm" loading={busy} disabled={!ownerId}>지정</Button>
        </form>
      </CardContent>
    </Card>
  );
}

function LinkCard({
  detail,
  busy,
  onSubmit,
}: {
  detail: RequirementDetail;
  busy: boolean;
  onSubmit: (changeId: number) => void;
}) {
  const [changeId, setChangeId] = useState("");

  const handleSubmit = (e: FormEvent) => {
    e.preventDefault();
    if (!changeId) return;
    onSubmit(Number(changeId));
    setChangeId("");
  };

  return (
    <Card>
      <CardHeader><CardTitle className="text-base">변경 연계</CardTitle></CardHeader>
      <CardContent className="space-y-3 text-sm">
        <div className="space-y-1.5">
          <span className="text-muted-foreground">연결된 변경 요청</span>
          {detail.linkedChanges.length === 0 ? (
            <p className="text-muted-foreground">연결 없음</p>
          ) : (
            detail.linkedChanges.map((c) => <span key={c.id} className="block text-foreground">{c.ticketKey}</span>)
          )}
        </div>
        <form onSubmit={handleSubmit} className="flex items-end gap-2 border-t border-border pt-3">
          <div className="flex-1 space-y-1.5">
            <Label htmlFor="changeId">변경 요청 ID</Label>
            <Input id="changeId" type="number" value={changeId} onChange={(e) => setChangeId(e.target.value)} />
          </div>
          <Button type="submit" size="sm" loading={busy} disabled={!changeId}>연계</Button>
        </form>
      </CardContent>
    </Card>
  );
}

function CorrectiveActionCard({
  detail,
  actionApprovals,
  busy,
  onAdd,
  onTransition,
}: {
  detail: RequirementDetail;
  actionApprovals: Record<number, { steps: ApprovalStep[]; currentStepNo: number | null }>;
  busy: string | null;
  onAdd: (description: string) => void;
  onTransition: (actionId: number, targetStatus: "IN_PROGRESS" | "RESOLVED") => void;
}) {
  const [description, setDescription] = useState("");

  const handleSubmit = (e: FormEvent) => {
    e.preventDefault();
    if (!description.trim()) return;
    onAdd(description.trim());
    setDescription("");
  };

  return (
    <Card>
      <CardHeader><CardTitle className="text-base">시정조치</CardTitle></CardHeader>
      <CardContent className="space-y-4">
        {detail.correctiveActions.length === 0 ? (
          <p className="text-sm text-muted-foreground">등록된 시정조치가 없습니다.</p>
        ) : (
          <ul className="space-y-2">
            {detail.correctiveActions.map((a) => {
              const next = nextActionTransition(a.status);
              const approved = a.approval.approvalRequestId == null || a.approval.status === "APPROVED";
              const blocked = next === "RESOLVED" && !approved;
              const matched = a.approval.approvalRequestId != null;
              return (
                <li key={a.id} className="space-y-2 rounded-md border border-border p-3 text-sm">
                  <div className="flex items-center justify-between gap-2">
                    <span className="min-w-0 flex-1">{a.description}</span>
                    <StatusBadge tone={actionStatusTone(a.status)} label={actionStatusLabel(a.status)} />
                    {next ? (
                      <Button
                        size="sm"
                        variant="outline"
                        loading={busy === `action-${a.id}`}
                        disabled={blocked}
                        title={blocked ? "승인 완료 전에는 해결 상태로 전이할 수 없습니다" : undefined}
                        onClick={() => onTransition(a.id, next)}
                      >
                        {actionStatusLabel(next)}로 전이
                      </Button>
                    ) : null}
                  </div>
                  {matched ? (
                    <ApprovalPanel
                      matched
                      steps={actionApprovals[a.id]?.steps ?? []}
                      currentStepNo={actionApprovals[a.id]?.currentStepNo ?? null}
                    />
                  ) : null}
                </li>
              );
            })}
          </ul>
        )}
        <form onSubmit={handleSubmit} className="flex items-end gap-2">
          <div className="flex-1 space-y-1.5">
            <Label htmlFor="action-desc">내용</Label>
            <Input id="action-desc" value={description} onChange={(e) => setDescription(e.target.value)} />
          </div>
          <Button type="submit" loading={busy === "add-action"} disabled={!description.trim()}>등록</Button>
        </form>
      </CardContent>
    </Card>
  );
}

function AuditLogCard({ logs }: { logs: ComplianceAuditLog[] }) {
  return (
    <Card>
      <CardHeader><CardTitle className="text-base">감사 로그</CardTitle></CardHeader>
      <CardContent>
        {logs.length === 0 ? (
          <p className="text-sm text-muted-foreground">관련 감사 로그가 없습니다.</p>
        ) : (
          <ul className="space-y-2">
            {logs.map((l, i) => (
              <li key={i} className="flex items-center justify-between gap-2 rounded-md border border-border p-3 text-sm">
                <span className="min-w-0 flex-1 text-foreground">{l.actor} · {l.eventType}</span>
                <span className="shrink-0 text-muted-foreground">{formatDateTime(l.occurredAt)}</span>
              </li>
            ))}
          </ul>
        )}
      </CardContent>
    </Card>
  );
}
