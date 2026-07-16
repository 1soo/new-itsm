import { type FormEvent, useCallback, useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { useTranslation } from "react-i18next";
import type { TFunction } from "i18next";

import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { ApprovalPanel, ConfirmDialog, StatusBadge, toast } from "@/components/common";
import type { ApprovalStep } from "@/components/common";
import { FullscreenLoader } from "@/routes/FullscreenLoader";
import { assetApi } from "@/features/asset/api";
import { formatDate, formatDateTime } from "@/features/asset/format";
import {
  expiryLabel,
  expiryTone,
  statusLabel,
  statusTone,
  ticketTypeLabel,
  transitionLabel,
  typeLabel,
  typeTone,
} from "@/features/asset/status";
import type { AssetDetail, AssetStatus, ExpiryField, TicketType } from "@/features/asset/types";
import { commonApi } from "@/features/common/api";
import { extractErrorMessage } from "@/lib/apiClient";

const NON_TERMINAL_STAGES: AssetStatus[] = ["PLANNING", "PROCUREMENT", "OPERATION", "MAINTENANCE"];

/*
 * 자산 상세(SCR-ITAM-003) — 생애주기 전이(계획~유지보수)·폐기(확인 다이얼로그)·만료 강조·
 * 티켓 연계·연결 CI 표시.
 */
export function AssetDetailPage() {
  const { t } = useTranslation("asset");
  const navigate = useNavigate();
  const id = Number(useParams().id);

  const [detail, setDetail] = useState<AssetDetail | null>(null);
  const [approvalSteps, setApprovalSteps] = useState<ApprovalStep[]>([]);
  const [approvalCurrentStepNo, setApprovalCurrentStepNo] = useState<number | null>(null);
  const [loading, setLoading] = useState(true);
  const [notFound, setNotFound] = useState(false);
  const [busy, setBusy] = useState<string | null>(null);
  const [confirmRetire, setConfirmRetire] = useState(false);

  const [ticketType, setTicketType] = useState<TicketType>("INCIDENT");
  const [ticketId, setTicketId] = useState("");

  const refreshDetail = useCallback(
    (silent: boolean) => {
      if (!silent) setLoading(true);
      return assetApi
        .get(id)
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
      // 전이/폐기가 게이트(409)로 거부된 경우 BE가 이미 승인 인스턴스를 생성했을 수 있으므로,
      // 전체 로딩 화면 없이 조용히 다시 조회해 승인 패널에 반영한다.
      if (reloadOnError) refreshDetail(true);
    } finally {
      setBusy(null);
    }
  };

  const handleRetire = async () => {
    setBusy("retire");
    try {
      await assetApi.retire(id);
      toast.success(t("assetDetail.retireSuccess", { defaultValue: "자산이 폐기되었습니다" }));
      load();
    } catch (err) {
      toast.error(extractErrorMessage(err));
      refreshDetail(true);
    } finally {
      setBusy(null);
      setConfirmRetire(false);
    }
  };

  const handleLink = (e: FormEvent) => {
    e.preventDefault();
    if (!ticketId) return;
    run(
      "link",
      () => assetApi.link(id, { ticketType, ticketId: Number(ticketId) }),
      t("assetDetail.linkSuccess", { defaultValue: "연계되었습니다" }),
    ).then(() => setTicketId(""));
  };

  if (loading) return <FullscreenLoader />;
  if (notFound || !detail) {
    return (
      <div className="mx-auto max-w-lg space-y-4 text-center">
        <p className="text-sm text-muted-foreground">{t("assetDetail.notFound", { defaultValue: "자산을 찾을 수 없습니다." })}</p>
        <Button onClick={() => navigate("/assets")}>{t("assetDetail.backToList", { defaultValue: "목록으로" })}</Button>
      </div>
    );
  }

  const isRetired = detail.status === "RETIREMENT";
  const transitions = NON_TERMINAL_STAGES.filter((s) => s !== detail.status);
  const approved = detail.approval.approvalRequestId == null || detail.approval.status === "APPROVED";

  return (
    <>
      <div className="mx-auto max-w-4xl space-y-4">
        <div className="flex flex-wrap items-start justify-between gap-3">
          <div className="space-y-1">
            <p className="text-sm text-muted-foreground">{detail.assetKey}</p>
            <h1 className="text-xl font-semibold text-foreground">{detail.name}</h1>
            <div className="flex flex-wrap gap-2">
              <StatusBadge tone={typeTone(detail.type)} label={typeLabel(t, detail.type)} />
              <StatusBadge tone={statusTone(detail.status)} label={statusLabel(t, detail.status)} />
            </div>
          </div>
          {!isRetired ? (
            <div className="flex flex-wrap gap-2">
              {transitions.map((target) => (
                <Button
                  key={target}
                  size="sm"
                  loading={busy === `st-${target}`}
                  onClick={() =>
                    run(
                      `st-${target}`,
                      () => assetApi.transition(id, target),
                      t("assetDetail.transitionSuccess", {
                        status: statusLabel(t, target),
                        defaultValue: `상태가 '${statusLabel(t, target)}'로 변경되었습니다`,
                      }),
                    )
                  }
                >
                  {transitionLabel(t, target)}
                </Button>
              ))}
              <Button
                size="sm"
                variant="destructive"
                disabled={!approved}
                title={!approved ? t("assetDetail.retireBlockedTooltip", { defaultValue: "승인 완료 전에는 폐기할 수 없습니다" }) : undefined}
                onClick={() => setConfirmRetire(true)}
              >
                {t("assetDetail.retireButton", { defaultValue: "폐기" })}
              </Button>
            </div>
          ) : null}
        </div>

        <div className="grid gap-4 lg:grid-cols-[minmax(0,1fr)_18rem]">
          <div className="space-y-4">
            <Card>
              <CardHeader><CardTitle className="text-base">{t("assetDetail.basicInfoTitle", { defaultValue: "기본 정보" })}</CardTitle></CardHeader>
              <CardContent className="space-y-1.5 text-sm">
                <MetaRow
                  label={t("assetDetail.ownerLabel", { defaultValue: "소유자" })}
                  value={detail.owner || t("assetDetail.ownerUnassigned", { defaultValue: "미지정" })}
                />
                <MetaRow label={t("assetDetail.locationLabel", { defaultValue: "위치" })} value={detail.location || "-"} />
              </CardContent>
            </Card>

            {Object.keys(detail.attributes ?? {}).length > 0 ? (
              <Card>
                <CardHeader><CardTitle className="text-base">{t("assetDetail.attributesTitle", { defaultValue: "유형별 속성" })}</CardTitle></CardHeader>
                <CardContent className="space-y-1.5 text-sm">
                  {Object.entries(detail.attributes).map(([k, v]) => (
                    <MetaRow key={k} label={k} value={v} />
                  ))}
                </CardContent>
              </Card>
            ) : null}

            <Card>
              <CardHeader><CardTitle className="text-base">{t("assetDetail.lifecycleTitle", { defaultValue: "생애주기 이력" })}</CardTitle></CardHeader>
              <CardContent className="space-y-2 text-sm">
                {detail.lifecycleHistory.length === 0 ? (
                  <p className="text-muted-foreground">{t("assetDetail.noHistory", { defaultValue: "이력 없음" })}</p>
                ) : (
                  detail.lifecycleHistory.map((h, i) => (
                    <div key={i} className="flex items-center justify-between gap-2">
                      <span className="text-foreground">{statusLabel(t, h.stage as AssetStatus)}</span>
                      <span className="text-xs text-muted-foreground">{formatDateTime(h.at)}</span>
                    </div>
                  ))
                )}
              </CardContent>
            </Card>

            <Card>
              <CardHeader><CardTitle className="text-base">{t("assetDetail.linkTicketTitle", { defaultValue: "티켓 연계" })}</CardTitle></CardHeader>
              <CardContent>
                <form onSubmit={handleLink} className="flex flex-wrap items-end gap-2">
                  <div className="space-y-1.5">
                    <Label>{t("assetDetail.ticketTargetTypeLabel", { defaultValue: "대상 유형" })}</Label>
                    <Select value={ticketType} onValueChange={(v) => setTicketType(v as TicketType)}>
                      <SelectTrigger className="w-40"><SelectValue /></SelectTrigger>
                      <SelectContent>
                        <SelectItem value="SERVICE_REQUEST">{ticketTypeLabel(t, "SERVICE_REQUEST")}</SelectItem>
                        <SelectItem value="INCIDENT">{ticketTypeLabel(t, "INCIDENT")}</SelectItem>
                        <SelectItem value="PROBLEM">{ticketTypeLabel(t, "PROBLEM")}</SelectItem>
                        <SelectItem value="CHANGE">{ticketTypeLabel(t, "CHANGE")}</SelectItem>
                      </SelectContent>
                    </Select>
                  </div>
                  <div className="space-y-1.5">
                    <Label htmlFor="tid">{t("assetDetail.ticketTargetIdLabel", { defaultValue: "대상 ID" })}</Label>
                    <Input id="tid" type="number" className="w-32" value={ticketId} onChange={(e) => setTicketId(e.target.value)} />
                  </div>
                  <Button type="submit" loading={busy === "link"} disabled={!ticketId}>
                    {t("assetDetail.linkButton", { defaultValue: "연계" })}
                  </Button>
                </form>
              </CardContent>
            </Card>
          </div>

          <div className="space-y-4">
            <Card>
              <CardHeader><CardTitle className="text-base">{t("assetDetail.expiryTitle", { defaultValue: "만료 정보" })}</CardTitle></CardHeader>
              <CardContent className="space-y-1.5 text-sm">
                <ExpiryRow t={t} label={t("assetDetail.expiryLicense", { defaultValue: "라이선스" })} value={detail.expiry.license} />
                <ExpiryRow t={t} label={t("assetDetail.expiryWarranty", { defaultValue: "보증" })} value={detail.expiry.warranty} />
                <ExpiryRow t={t} label={t("assetDetail.expiryContract", { defaultValue: "계약" })} value={detail.expiry.contract} />
              </CardContent>
            </Card>

            <ApprovalPanel
              matched={detail.approval.approvalRequestId != null}
              steps={approvalSteps}
              currentStepNo={approvalCurrentStepNo}
            />

            <Card>
              <CardHeader><CardTitle className="text-base">{t("assetDetail.linkedTicketsTitle", { defaultValue: "연결 티켓" })}</CardTitle></CardHeader>
              <CardContent className="space-y-1.5 text-sm">
                {detail.linkedTickets.length === 0 ? (
                  <p className="text-muted-foreground">{t("assetDetail.noLinks", { defaultValue: "연결 없음" })}</p>
                ) : (
                  detail.linkedTickets.map((lt, i) => (
                    <span key={i} className="block text-foreground">{ticketTypeLabel(t, lt.type)} · {lt.ticketKey}</span>
                  ))
                )}
              </CardContent>
            </Card>

            <Card>
              <CardHeader><CardTitle className="text-base">{t("assetDetail.linkedCisTitle", { defaultValue: "연결 CI" })}</CardTitle></CardHeader>
              <CardContent className="space-y-1.5 text-sm">
                {detail.linkedCis.length === 0 ? (
                  <p className="text-muted-foreground">{t("assetDetail.noLinks", { defaultValue: "연결 없음" })}</p>
                ) : (
                  detail.linkedCis.map((c) => (
                    <span key={c.ciId} className="block text-foreground">{c.name}</span>
                  ))
                )}
              </CardContent>
            </Card>
          </div>
        </div>
      </div>

      <ConfirmDialog
        open={confirmRetire}
        onOpenChange={setConfirmRetire}
        title={t("assetDetail.retireConfirmTitle", { defaultValue: "자산을 폐기하시겠습니까?" })}
        description={t("assetDetail.retireConfirmDescription", { defaultValue: "폐기 후에는 되돌릴 수 없습니다." })}
        confirmLabel={t("assetDetail.retireButton", { defaultValue: "폐기" })}
        loading={busy === "retire"}
        onConfirm={handleRetire}
      />
    </>
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

function ExpiryRow({ t, label, value }: { t: TFunction; label: string; value: ExpiryField }) {
  if (!value.date) {
    return (
      <div className="flex items-center justify-between gap-2">
        <span className="text-muted-foreground">{label}</span>
        <span className="text-muted-foreground">-</span>
      </div>
    );
  }
  return (
    <div className="flex items-center justify-between gap-2">
      <span className="text-muted-foreground">{label}</span>
      <span className="flex items-center gap-1.5">
        {formatDate(value.date)}
        {value.status && value.status !== "OK" ? (
          <StatusBadge tone={expiryTone(value.status)} label={expiryLabel(t, value.status)} />
        ) : null}
      </span>
    </div>
  );
}
