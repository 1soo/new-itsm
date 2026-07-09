import { type FormEvent, useCallback, useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  type Column,
  DataTable,
  Modal,
  StatusBadge,
  TicketListLayout,
  toast,
} from "@/components/common";
import { changeApi } from "@/features/change/api";
import { riskLabel, riskTone, typeLabel, typeTone } from "@/features/change/status";
import type { ApprovalQueueItem, Decision } from "@/features/change/types";
import { extractErrorMessage } from "@/lib/apiClient";

/*
 * CAB 승인 대기함(SCR-CHG-004) — 승인 권한자(CAB/Approver)가 배정된 변경 승인/반려.
 * 반려 시 사유 필수(누락 시 BE 400). 역할 기반 공유 대기함(먼저 처리한 사용자가 결정자).
 */
export function ChangeApprovalInboxPage() {
  const navigate = useNavigate();
  const [items, setItems] = useState<ApprovalQueueItem[]>([]);
  const [loading, setLoading] = useState(true);

  const [target, setTarget] = useState<ApprovalQueueItem | null>(null);
  const [decision, setDecision] = useState<Decision>("APPROVE");
  const [opinion, setOpinion] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  const load = useCallback(() => {
    setLoading(true);
    changeApi
      .listApprovals()
      .then(setItems)
      .catch((err) => toast.error(extractErrorMessage(err)))
      .finally(() => setLoading(false));
  }, []);

  useEffect(load, [load]);

  const openDecision = (item: ApprovalQueueItem, d: Decision) => {
    setTarget(item);
    setDecision(d);
    setOpinion("");
    setError(null);
  };

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    if (!target) return;
    if (decision === "REJECT" && !opinion.trim()) {
      setError("반려 사유를 입력하세요.");
      return;
    }
    setSubmitting(true);
    try {
      await changeApi.decideApproval(target.changeId, decision, opinion.trim() || undefined);
      toast.success(decision === "APPROVE" ? "승인되었습니다" : "반려되었습니다");
      setTarget(null);
      load();
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setSubmitting(false);
    }
  };

  const columns: Column<ApprovalQueueItem>[] = [
    { header: "접수번호", cell: (a) => a.ticketKey },
    { header: "유형", cell: (a) => <StatusBadge tone={typeTone(a.type)} label={typeLabel(a.type)} /> },
    {
      header: "위험도",
      cell: (a) =>
        a.risk ? (
          <StatusBadge tone={riskTone(a.risk)} label={riskLabel(a.risk)} />
        ) : (
          <span className="text-sm text-muted-foreground">미평가</span>
        ),
    },
    { header: "요청자", cell: (a) => a.requester },
    {
      header: "",
      className: "text-right",
      cell: (a) => (
        <span className="flex justify-end gap-2">
          <Button size="sm" variant="outline" onClick={() => navigate(`/changes/${a.changeId}`)}>
            상세
          </Button>
          <Button size="sm" onClick={() => openDecision(a, "APPROVE")}>
            승인
          </Button>
          <Button size="sm" variant="destructive" onClick={() => openDecision(a, "REJECT")}>
            반려
          </Button>
        </span>
      ),
    },
  ];

  return (
    <TicketListLayout title="CAB 승인 대기함" description="배정된 변경 요청 승인 건을 처리합니다.">
      <DataTable
        columns={columns}
        data={items}
        rowKey={(a) => a.changeId}
        loading={loading}
        emptyTitle="승인 대기 건이 없습니다"
      />

      <Modal
        open={target !== null}
        onOpenChange={(o) => !o && setTarget(null)}
        title={decision === "APPROVE" ? "변경 승인" : "변경 반려"}
        description={target ? `${target.ticketKey} · ${target.requester}` : undefined}
      >
        <form onSubmit={handleSubmit} className="space-y-4" noValidate>
          <div className="space-y-1.5">
            <Label htmlFor="opinion">
              {decision === "APPROVE" ? "의견(선택)" : "반려 사유"}
            </Label>
            <Input
              id="opinion"
              value={opinion}
              onChange={(e) => setOpinion(e.target.value)}
              aria-invalid={!!error}
              required={decision === "REJECT"}
            />
            {error ? (
              <p role="alert" className="text-sm text-danger">
                {error}
              </p>
            ) : null}
          </div>
          <div className="flex justify-end gap-2">
            <Button type="button" variant="outline" onClick={() => setTarget(null)}>
              취소
            </Button>
            <Button
              type="submit"
              variant={decision === "REJECT" ? "destructive" : "default"}
              loading={submitting}
            >
              {decision === "APPROVE" ? "승인" : "반려"}
            </Button>
          </div>
        </form>
      </Modal>
    </TicketListLayout>
  );
}
