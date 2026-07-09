import { type FormEvent, useCallback, useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  type Column,
  DataTable,
  Modal,
  TicketListLayout,
  toast,
} from "@/components/common";
import { srmApi } from "@/features/service-request/api";
import { formatDateTime } from "@/features/service-request/format";
import type { ApprovalItem } from "@/features/service-request/types";
import { extractErrorMessage } from "@/lib/apiClient";

/*
 * 승인 대기함(SCR-SRM-006) — 승인자가 배정된 승인 건을 승인/반려한다.
 * 반려 시 사유 필수(누락 시 BE 400). 지정 승인자만 처리(그 외 403).
 */
type Decision = "APPROVE" | "REJECT";

export function ApprovalInboxPage() {
  const navigate = useNavigate();
  const [items, setItems] = useState<ApprovalItem[]>([]);
  const [loading, setLoading] = useState(true);

  const [target, setTarget] = useState<ApprovalItem | null>(null);
  const [decision, setDecision] = useState<Decision>("APPROVE");
  const [reason, setReason] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  const load = useCallback(() => {
    setLoading(true);
    srmApi
      .listApprovals()
      .then(setItems)
      .catch((err) => toast.error(extractErrorMessage(err)))
      .finally(() => setLoading(false));
  }, []);

  useEffect(load, [load]);

  const openDecision = (item: ApprovalItem, d: Decision) => {
    setTarget(item);
    setDecision(d);
    setReason("");
    setError(null);
  };

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    if (!target) return;
    if (decision === "REJECT" && !reason.trim()) {
      setError("반려 사유를 입력하세요.");
      return;
    }
    setSubmitting(true);
    try {
      await srmApi.decideApproval(target.requestId, decision, reason.trim() || undefined);
      toast.success(decision === "APPROVE" ? "승인되었습니다" : "반려되었습니다");
      setTarget(null);
      load();
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setSubmitting(false);
    }
  };

  const columns: Column<ApprovalItem>[] = [
    { header: "접수번호", cell: (a) => a.ticketKey },
    { header: "요청자", cell: (a) => a.requester },
    { header: "요청 시각", cell: (a) => formatDateTime(a.requestedAt) },
    {
      header: "",
      className: "text-right",
      cell: (a) => (
        <span className="flex justify-end gap-2">
          <Button size="sm" variant="outline" onClick={() => navigate(`/service-requests/${a.requestId}`)}>
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
    <TicketListLayout title="승인 대기함" description="배정된 서비스 요청 승인 건을 처리합니다.">
      <DataTable
        columns={columns}
        data={items}
        rowKey={(a) => a.requestId}
        loading={loading}
        emptyTitle="승인 대기 건이 없습니다"
      />

      <Modal
        open={target !== null}
        onOpenChange={(o) => !o && setTarget(null)}
        title={decision === "APPROVE" ? "요청 승인" : "요청 반려"}
        description={target ? `${target.ticketKey} · ${target.requester}` : undefined}
      >
        <form onSubmit={handleSubmit} className="space-y-4" noValidate>
          <div className="space-y-1.5">
            <Label htmlFor="reason">
              {decision === "APPROVE" ? "사유(선택)" : "반려 사유"}
            </Label>
            <Input
              id="reason"
              value={reason}
              onChange={(e) => setReason(e.target.value)}
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
