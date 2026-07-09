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
import { knowledgeApi } from "@/features/knowledge/api";
import { formatDateTime } from "@/features/knowledge/format";
import type { Decision, ReviewQueueItem } from "@/features/knowledge/types";
import { extractErrorMessage } from "@/lib/apiClient";

/*
 * 검토·게시 승인함(SCR-KM-004) — 게이트키퍼가 검토 요청된 기사를 승인/반려.
 * 반려 시 사유 필수(누락 시 BE 400). 승인 시 게시, 반려 시 초안 복귀.
 */
export function ReviewInboxPage() {
  const navigate = useNavigate();
  const [items, setItems] = useState<ReviewQueueItem[]>([]);
  const [loading, setLoading] = useState(true);

  const [target, setTarget] = useState<ReviewQueueItem | null>(null);
  const [decision, setDecision] = useState<Decision>("APPROVE");
  const [reason, setReason] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  const load = useCallback(() => {
    setLoading(true);
    knowledgeApi
      .listReviews()
      .then(setItems)
      .catch((err) => toast.error(extractErrorMessage(err)))
      .finally(() => setLoading(false));
  }, []);

  useEffect(load, [load]);

  const openDecision = (item: ReviewQueueItem, d: Decision) => {
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
      await knowledgeApi.review(target.articleId, decision, reason.trim() || undefined);
      toast.success(decision === "APPROVE" ? "게시되었습니다" : "반려되었습니다");
      setTarget(null);
      load();
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setSubmitting(false);
    }
  };

  const columns: Column<ReviewQueueItem>[] = [
    { header: "제목", cell: (a) => <span className="line-clamp-1">{a.title}</span> },
    { header: "기여자", cell: (a) => a.author },
    { header: "요청일", cell: (a) => formatDateTime(a.requestedAt) },
    {
      header: "",
      className: "text-right",
      cell: (a) => (
        <span className="flex justify-end gap-2">
          <Button size="sm" variant="outline" onClick={() => navigate(`/knowledge/${a.articleId}`)}>
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
    <TicketListLayout title="검토·게시 승인함" description="검토 요청된 지식 기사를 승인/반려합니다.">
      <DataTable
        columns={columns}
        data={items}
        rowKey={(a) => a.articleId}
        loading={loading}
        emptyTitle="검토 대기 기사가 없습니다"
      />

      <Modal
        open={target !== null}
        onOpenChange={(o) => !o && setTarget(null)}
        title={decision === "APPROVE" ? "기사 게시 승인" : "기사 반려"}
        description={target ? `${target.title} · ${target.author}` : undefined}
      >
        <form onSubmit={handleSubmit} className="space-y-4" noValidate>
          <div className="space-y-1.5">
            <Label htmlFor="reason">
              {decision === "APPROVE" ? "의견(선택)" : "반려 사유"}
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
