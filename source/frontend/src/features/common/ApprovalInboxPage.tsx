import { type FormEvent, useCallback, useEffect, useState } from "react";

import { Button } from "@/components/ui/button";
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
  ApprovalStepProgress,
  type Column,
  DataTable,
  Modal,
  StatusBadge,
  TicketListLayout,
  toast,
} from "@/components/common";
import { commonApi } from "@/features/common/api";
import { formatDateTime } from "@/features/common/format";
import { ticketTypeLabel } from "@/features/common/status";
import type {
  ApprovalDetail,
  ApprovalListItem,
  TicketType,
} from "@/features/common/types";
import { extractErrorMessage } from "@/lib/apiClient";

/*
 * 승인 대기함(전 도메인 공용, SCR-COM-014) — 승인 프로세스 커스텀 기능(유지보수 요청)으로
 * 기존 SCR-SRM-006/SCR-CHG-004/SCR-KM-004를 대체한다. 로그인 사용자가 현재 차수에 필요한
 * 역할을 보유하고 아직 그 역할 슬롯을 결정하지 않은 건만 API-COM-003이 반환(역할 기반 공유 대기함).
 */
const ALL = "ALL";

const DOMAIN_OPTIONS: TicketType[] = [
  "SERVICE_REQUEST",
  "CHANGE",
  "KNOWLEDGE",
  "INCIDENT",
  "PROBLEM",
  "ASSET",
  "VULNERABILITY",
  "COMPLIANCE",
  "ESM",
];

type Decision = "APPROVE" | "REJECT";

export function ApprovalInboxPage() {
  const [domain, setDomain] = useState(ALL);
  const [items, setItems] = useState<ApprovalListItem[]>([]);
  const [loading, setLoading] = useState(true);

  const [target, setTarget] = useState<ApprovalListItem | null>(null);
  const [detail, setDetail] = useState<ApprovalDetail | null>(null);
  const [detailLoading, setDetailLoading] = useState(false);
  const [decision, setDecision] = useState<Decision>("APPROVE");
  const [reason, setReason] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  const load = useCallback(() => {
    setLoading(true);
    commonApi
      .listMyApprovals({ domain: domain === ALL ? undefined : (domain as TicketType) })
      .then((res) => setItems(res.content))
      .catch((err) => toast.error(extractErrorMessage(err)))
      .finally(() => setLoading(false));
  }, [domain]);

  useEffect(load, [load]);

  const openItem = (item: ApprovalListItem) => {
    setTarget(item);
    setDetail(null);
    setDecision("APPROVE");
    setReason("");
    setError(null);
    setDetailLoading(true);
    commonApi
      .getApproval(item.approvalRequestId)
      .then(setDetail)
      .catch((err) => toast.error(extractErrorMessage(err)))
      .finally(() => setDetailLoading(false));
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
      await commonApi.decide(target.approvalRequestId, {
        decision,
        reason: reason.trim() || undefined,
      });
      toast.success(decision === "APPROVE" ? "승인되었습니다" : "반려되었습니다");
      setTarget(null);
      load();
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setSubmitting(false);
    }
  };

  const columns: Column<ApprovalListItem>[] = [
    {
      header: "티켓 유형",
      cell: (a) => <StatusBadge tone="info" label={ticketTypeLabel(a.ticketType)} />,
    },
    { header: "티켓", cell: (a) => `${a.ticketKey} · ${a.ticketSummary}` },
    { header: "차수", cell: (a) => `${a.currentStepNo}차` },
    { header: "요청자", cell: (a) => a.requester },
    { header: "요청일", cell: (a) => formatDateTime(a.requestedAt) },
    {
      header: "",
      className: "text-right",
      cell: (a) => (
        <Button size="sm" variant="outline" onClick={() => openItem(a)}>
          상세
        </Button>
      ),
    },
  ];

  return (
    <TicketListLayout
      title="승인 대기함"
      description="보유한 역할이 필요한 승인 건을 도메인 구분 없이 확인하고 승인/반려합니다."
      filters={
        <div className="space-y-1">
          <Label>도메인</Label>
          <Select value={domain} onValueChange={setDomain}>
            <SelectTrigger className="w-48">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value={ALL}>전체</SelectItem>
              {DOMAIN_OPTIONS.map((d) => (
                <SelectItem key={d} value={d}>
                  {ticketTypeLabel(d)}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>
      }
    >
      <DataTable
        columns={columns}
        data={items}
        rowKey={(a) => a.approvalRequestId}
        loading={loading}
        emptyTitle="현재 대기 중인 승인이 없습니다"
      />

      <Modal
        open={target !== null}
        onOpenChange={(o) => !o && setTarget(null)}
        title="승인 처리"
        description={target ? `${target.ticketKey} · ${target.ticketSummary}` : undefined}
        className="sm:max-w-lg"
      >
        {detailLoading || !detail ? (
          <p className="text-sm text-muted-foreground">불러오는 중...</p>
        ) : (
          <div className="space-y-4">
            <ApprovalStepProgress steps={detail.steps} currentStepNo={detail.currentStepNo} />

            {detail.status === "IN_PROGRESS" ? (
              <form onSubmit={handleSubmit} className="space-y-3 border-t border-border pt-4" noValidate>
                <div className="flex gap-2">
                  <Button
                    type="button"
                    variant={decision === "APPROVE" ? "default" : "outline"}
                    className="flex-1"
                    onClick={() => setDecision("APPROVE")}
                  >
                    승인
                  </Button>
                  <Button
                    type="button"
                    variant={decision === "REJECT" ? "destructive" : "outline"}
                    className="flex-1"
                    onClick={() => setDecision("REJECT")}
                  >
                    반려
                  </Button>
                </div>
                <div className="space-y-1.5">
                  <Label htmlFor="reason">{decision === "APPROVE" ? "사유(선택)" : "반려 사유"}</Label>
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
            ) : null}
          </div>
        )}
      </Modal>
    </TicketListLayout>
  );
}
