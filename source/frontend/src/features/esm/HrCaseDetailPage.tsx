import { useCallback, useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { useTranslation } from "react-i18next";

import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import {
  StatusBadge,
  TicketDetailLayout,
  Timeline,
  type TimelineItem,
  toast,
} from "@/components/common";
import { FullscreenLoader } from "@/routes/FullscreenLoader";
import { esmApi } from "@/features/esm/api";
import { formatDateTime } from "@/features/esm/format";
import {
  hrCaseNextStatus,
  hrCaseStatusLabel,
  hrCaseStatusTone,
  hrCaseTransitionLabel,
} from "@/features/esm/status";
import type { HrCaseDetail } from "@/features/esm/types";
import { extractErrorMessage } from "@/lib/apiClient";

/*
 * HR 케이스 상세(SCR-ESM-008) — 접수→기록→조사→해결 4단계 순차 전이만 허용.
 * 연결 항목 패널은 민감정보 특성상 제외(TicketDetailLayout meta에 대상자·상태만).
 */
export function HrCaseDetailPage() {
  const { t } = useTranslation("esm");
  const params = useParams();
  const navigate = useNavigate();
  const id = Number(params.id);

  const [detail, setDetail] = useState<HrCaseDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [notFound, setNotFound] = useState(false);
  const [transitioning, setTransitioning] = useState(false);

  const load = useCallback(() => {
    setLoading(true);
    esmApi
      .getHrCase(id)
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

  const handleTransition = async () => {
    if (!detail) return;
    const next = hrCaseNextStatus(detail.status);
    if (!next) return;
    setTransitioning(true);
    try {
      await esmApi.transitionHrCase(id, next);
      toast.success(
        t("hrCaseDetail.transitionSuccess", {
          status: hrCaseStatusLabel(t, next),
          defaultValue: `상태가 '${hrCaseStatusLabel(t, next)}'로 변경되었습니다`,
        }),
      );
      load();
    } catch (err) {
      toast.error(extractErrorMessage(err));
    } finally {
      setTransitioning(false);
    }
  };

  if (loading) return <FullscreenLoader />;
  if (notFound || !detail) {
    return (
      <div className="mx-auto max-w-lg space-y-4 text-center">
        <p className="text-sm text-muted-foreground">{t("hrCaseDetail.notFound", { defaultValue: "케이스를 찾을 수 없습니다." })}</p>
        <Button onClick={() => navigate(-1)}>{t("hrCaseDetail.back", { defaultValue: "이전으로" })}</Button>
      </div>
    );
  }

  const next = hrCaseNextStatus(detail.status);
  const historyItems: TimelineItem[] = detail.history.map((h, i) => ({
    id: String(i),
    title: `${hrCaseStatusLabel(t, h.status)} · ${h.changedBy}`,
    timestamp: formatDateTime(h.at),
  }));

  return (
    <TicketDetailLayout
      ticketKey={`HR-${detail.id}`}
      title={detail.title}
      badges={<StatusBadge tone={hrCaseStatusTone(detail.status)} label={hrCaseStatusLabel(t, detail.status)} />}
      actions={
        next ? (
          <Button loading={transitioning} onClick={handleTransition}>
            {hrCaseTransitionLabel(t, next)}
          </Button>
        ) : undefined
      }
      meta={
        <Card>
          <CardHeader>
            <CardTitle className="text-base">{t("hrCaseDetail.infoTitle", { defaultValue: "정보" })}</CardTitle>
          </CardHeader>
          <CardContent className="space-y-2 text-sm">
            <div className="flex items-center justify-between gap-2">
              <span className="text-muted-foreground">{t("hrCaseDetail.subjectLabel", { defaultValue: "대상자" })}</span>
              <span className="text-right text-foreground">{detail.subjectUserName || "-"}</span>
            </div>
          </CardContent>
        </Card>
      }
    >
      <Card>
        <CardHeader>
          <CardTitle className="text-base">{t("hrCaseDetail.descriptionTitle", { defaultValue: "내용" })}</CardTitle>
        </CardHeader>
        <CardContent>
          <p className="whitespace-pre-wrap text-sm text-foreground">
            {detail.description || t("hrCaseDetail.noDescription", { defaultValue: "내용 없음" })}
          </p>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle className="text-base">{t("hrCaseDetail.historyTitle", { defaultValue: "상태 이력" })}</CardTitle>
        </CardHeader>
        <CardContent>
          {historyItems.length === 0 ? (
            <p className="text-sm text-muted-foreground">{t("hrCaseDetail.noHistory", { defaultValue: "이력이 없습니다." })}</p>
          ) : (
            <Timeline items={historyItems} />
          )}
        </CardContent>
      </Card>
    </TicketDetailLayout>
  );
}
