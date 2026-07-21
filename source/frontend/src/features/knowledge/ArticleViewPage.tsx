import { useCallback, useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { ThumbsDown, ThumbsUp } from "lucide-react";

import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { deriveApprovalStatusDisplay, StatusBadge, toast } from "@/components/common";
import { FullscreenLoader } from "@/routes/FullscreenLoader";
import { hasAnyRole, ROLE_KNOWLEDGE_CONTRIBUTOR } from "@/features/auth/roles";
import { knowledgeApi } from "@/features/knowledge/api";
import { statusLabel, statusTone } from "@/features/knowledge/status";
import type { ArticleDetail } from "@/features/knowledge/types";
import { useAppSelector } from "@/store/hooks";
import { extractErrorMessage } from "@/lib/apiClient";

/*
 * 기사 열람(SCR-KM-002) — 게시 기사 셀프서비스 열람 + 유용성 평가 위젯.
 * 미게시 기사에 최종 사용자 접근 시 BE 403.
 */
export function ArticleViewPage() {
  const { t } = useTranslation("knowledge");
  const navigate = useNavigate();
  const id = Number(useParams().id);
  const roles = useAppSelector((s) => s.auth.user?.roles);
  const canEdit = hasAnyRole(roles, [ROLE_KNOWLEDGE_CONTRIBUTOR]);

  const [detail, setDetail] = useState<ArticleDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [notFound, setNotFound] = useState(false);
  const [busy, setBusy] = useState(false);
  const [comment, setComment] = useState("");

  const load = useCallback(() => {
    setLoading(true);
    knowledgeApi
      .get(id)
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

  const handleFeedback = async (helpful: boolean) => {
    setBusy(true);
    try {
      await knowledgeApi.submitFeedback(id, { helpful, comment: comment.trim() || undefined });
      toast.success(t("articleView.feedbackSaved", { defaultValue: "평가가 저장되었습니다" }));
      setComment("");
      load();
    } catch (err) {
      toast.error(extractErrorMessage(err));
    } finally {
      setBusy(false);
    }
  };

  if (loading) return <FullscreenLoader />;
  if (notFound || !detail) {
    return (
      <div className="mx-auto max-w-lg space-y-4 text-center">
        <p className="text-sm text-muted-foreground">{t("articleView.notFound", { defaultValue: "기사를 찾을 수 없습니다." })}</p>
        <Button onClick={() => navigate(-1)}>{t("articleView.back", { defaultValue: "이전으로" })}</Button>
      </div>
    );
  }

  const statusDisplay = deriveApprovalStatusDisplay(
    t,
    { tone: statusTone(detail.status), label: statusLabel(t, detail.status) },
    {
      status: detail.approval.status,
      targetStateLabel: detail.approval.targetState ? statusLabel(t, detail.approval.targetState) : null,
    },
  );

  return (
    <div className="mx-auto max-w-2xl space-y-4">
      <div className="space-y-2">
        <div className="flex flex-wrap items-center gap-2">
          <StatusBadge tone={statusDisplay.tone} label={statusDisplay.label} />
          <span className="text-sm text-muted-foreground">{detail.category}</span>
          {canEdit ? (
            <Button size="sm" variant="outline" className="ml-auto" onClick={() => navigate(`/knowledge/${id}/edit`)}>
              {t("articleView.editButton", { defaultValue: "편집" })}
            </Button>
          ) : null}
        </div>
        <h1 className="text-xl font-semibold text-foreground">{detail.title}</h1>
        {detail.labels.length > 0 ? (
          <div className="flex flex-wrap gap-1">
            {detail.labels.map((l) => (
              <StatusBadge key={l} tone="muted" label={l} />
            ))}
          </div>
        ) : null}
      </div>

      <Card>
        <CardContent className="pt-6">
          <p className="whitespace-pre-wrap text-sm text-foreground">{detail.body}</p>
        </CardContent>
      </Card>

      <Card>
        <CardHeader><CardTitle className="text-base">{t("articleView.helpfulQuestion", { defaultValue: "도움이 되었나요?" })}</CardTitle></CardHeader>
        <CardContent className="space-y-3">
          <p className="text-sm text-muted-foreground">
            {t("articleView.helpfulCount", {
              helpful: detail.helpful,
              notHelpful: detail.notHelpful,
              defaultValue: `도움됨 ${detail.helpful} · 도움 안 됨 ${detail.notHelpful}`,
            })}
          </p>
          <div className="space-y-1.5">
            <Label htmlFor="comment">{t("articleView.commentLabel", { defaultValue: "코멘트(선택)" })}</Label>
            <Input id="comment" value={comment} onChange={(e) => setComment(e.target.value)} />
          </div>
          <div className="flex gap-2">
            <Button
              className="bg-success text-success-foreground hover:bg-success/90"
              loading={busy}
              onClick={() => handleFeedback(true)}
            >
              <ThumbsUp />
              {t("articleView.helpfulButton", { defaultValue: "도움됨" })}
            </Button>
            <Button variant="outline" loading={busy} onClick={() => handleFeedback(false)}>
              <ThumbsDown />
              {t("articleView.notHelpfulButton", { defaultValue: "도움 안 됨" })}
            </Button>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
