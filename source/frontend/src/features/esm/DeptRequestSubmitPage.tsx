import { useEffect, useMemo, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { useTranslation } from "react-i18next";

import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  DynamicFormRenderer,
  EMPTY_GRID_SCHEMA,
  type GridFormSchema,
  type GridFormValues,
  toast,
} from "@/components/common";
import { FullscreenLoader } from "@/routes/FullscreenLoader";
import { esmApi } from "@/features/esm/api";
import type { CatalogItemDetail } from "@/features/esm/types";
import { extractErrorMessage } from "@/lib/apiClient";

/*
 * 부서 요청 제출(SCR-ESM-002) — 카탈로그 항목의 동적 양식(SRM과 공용인 자체 8×n 그리드,
 * 2026-07-19 유지보수 요청으로 레거시 EAV DynamicForm에서 전환)을 작성해 제출.
 * 온보딩/오프보딩 유형은 대상자명(targetUserName) 입력을 그리드 폼과 별도로 요구하므로,
 * DynamicFormRenderer 자체 검증(필수·정규식) 통과 후 호출되는 onSubmit 콜백에서 함께 검사한다.
 * 제출 성공 시(checklistId 존재) 체크리스트 자동 생성 안내 토스트를 노출한다.
 */
export function DeptRequestSubmitPage() {
  const { t } = useTranslation("esm");
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const itemId = Number(searchParams.get("item"));

  const [catalog, setCatalog] = useState<CatalogItemDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [targetUserName, setTargetUserName] = useState("");
  const [targetUserNameError, setTargetUserNameError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  const schema = useMemo<GridFormSchema>(() => catalog?.formSchema ?? EMPTY_GRID_SCHEMA, [catalog]);
  const needsTargetUser = catalog?.checklistTemplateType === "ONBOARDING" || catalog?.checklistTemplateType === "OFFBOARDING";

  useEffect(() => {
    if (!itemId) {
      navigate("/esm/portal", { replace: true });
      return;
    }
    let active = true;
    setLoading(true);
    esmApi
      .getCatalogItem(itemId)
      .then((data) => active && setCatalog(data))
      .catch((err) => {
        if (active) {
          toast.error(extractErrorMessage(err));
          navigate("/esm/portal", { replace: true });
        }
      })
      .finally(() => active && setLoading(false));
    return () => {
      active = false;
    };
  }, [itemId, navigate]);

  const handleFormSubmit = async (formValues: GridFormValues) => {
    if (!catalog) return;

    const targetUserNameInvalid = needsTargetUser && !targetUserName.trim();
    setTargetUserNameError(
      targetUserNameInvalid
        ? t("deptRequestSubmit.targetUserNameRequiredError", { defaultValue: "대상자명은 필수 항목입니다." })
        : null,
    );
    if (targetUserNameInvalid) return;

    setSubmitting(true);
    try {
      const created = await esmApi.createRequest({
        catalogItemId: catalog.id,
        formValues,
        targetUserName: needsTargetUser ? targetUserName.trim() : undefined,
      });
      toast.success(
        created.checklistId
          ? t("deptRequestSubmit.successWithChecklist", {
              ticketKey: created.ticketKey,
              defaultValue: `요청이 접수되었습니다 (${created.ticketKey}). 체크리스트가 자동 생성되었습니다.`,
            })
          : t("deptRequestSubmit.successWithoutChecklist", {
              ticketKey: created.ticketKey,
              defaultValue: `요청이 접수되었습니다 (${created.ticketKey})`,
            }),
      );
      navigate(`/esm/requests/${created.id}`);
    } catch (err) {
      toast.error(extractErrorMessage(err));
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) return <FullscreenLoader />;
  if (!catalog) return null;

  return (
    <div className="space-y-4">
      <div className="space-y-1">
        <h1 className="text-xl font-semibold text-foreground">{catalog.name}</h1>
        {catalog.description ? <p className="text-sm text-muted-foreground">{catalog.description}</p> : null}
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="text-base">{t("deptRequestSubmit.formCardTitle", { defaultValue: "요청 양식" })}</CardTitle>
        </CardHeader>
        <CardContent className="space-y-6">
          {needsTargetUser ? (
            <div className="flex flex-col gap-1.5">
              <Label htmlFor="targetUserName">
                {t("deptRequestSubmit.targetUserNameLabel", { defaultValue: "대상자명" })}
                <span className="ml-0.5 text-destructive" aria-hidden="true">*</span>
              </Label>
              <Input
                id="targetUserName"
                value={targetUserName}
                onChange={(e) => setTargetUserName(e.target.value)}
                aria-invalid={!!targetUserNameError}
                placeholder={t("deptRequestSubmit.targetUserNamePlaceholder", { defaultValue: "온보딩/오프보딩 대상자 이름" })}
                disabled={submitting}
              />
              {targetUserNameError ? (
                <p className="text-xs text-destructive">{targetUserNameError}</p>
              ) : null}
            </div>
          ) : null}

          <DynamicFormRenderer
            schema={schema}
            onSubmit={handleFormSubmit}
            onCancel={() => navigate("/esm/portal")}
            submitLabel={t("deptRequestSubmit.submitButton", { defaultValue: "제출" })}
            cancelLabel={t("deptRequestSubmit.cancelButton", { defaultValue: "취소" })}
            disabled={submitting}
          />
        </CardContent>
      </Card>
    </div>
  );
}
