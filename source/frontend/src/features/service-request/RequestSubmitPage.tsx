import { useEffect, useMemo, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { useTranslation } from "react-i18next";

import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { DynamicFormRenderer, type GridFormSchema, type GridFormValues, toast } from "@/components/common";
import { FullscreenLoader } from "@/routes/FullscreenLoader";
import { srmApi } from "@/features/service-request/api";
import type {
  CatalogItemDetail,
  KnowledgeSuggestion,
} from "@/features/service-request/types";
import { extractErrorMessage } from "@/lib/apiClient";

const EMPTY_SCHEMA: GridFormSchema = { components: [] };

/*
 * 요청 제출(SCR-SRM-002) — 카탈로그 항목의 동적 양식(자체 8×n 그리드)을 작성해 제출.
 * 우측 "관련 지식 기사" 추천 패널(있을 때만). 필수·정규식 위반 필드는 DynamicFormRenderer 내장 검증이 제출을 차단.
 * 제출 성공 시 접수번호 토스트 + 상세 이동. 서버 재검증 실패(400)는 오류 토스트로 안내.
 */
export function RequestSubmitPage() {
  const { t } = useTranslation("service-request");
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const itemId = Number(searchParams.get("item"));

  const [catalog, setCatalog] = useState<CatalogItemDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [suggestions, setSuggestions] = useState<KnowledgeSuggestion[]>([]);

  const schema = useMemo<GridFormSchema>(() => catalog?.formSchema ?? EMPTY_SCHEMA, [catalog]);

  useEffect(() => {
    if (!itemId) {
      navigate("/portal", { replace: true });
      return;
    }
    let active = true;
    setLoading(true);
    srmApi
      .getCatalogItem(itemId)
      .then((data) => active && setCatalog(data))
      .catch((err) => {
        if (active) {
          toast.error(extractErrorMessage(err));
          navigate("/portal", { replace: true });
        }
      })
      .finally(() => active && setLoading(false));
    return () => {
      active = false;
    };
  }, [itemId, navigate]);

  useEffect(() => {
    if (!itemId) return;
    srmApi
      .suggestions({ catalogItemId: itemId })
      .then(setSuggestions)
      .catch(() => setSuggestions([]));
  }, [itemId]);

  const handleFormSubmit = async (formValues: GridFormValues) => {
    if (!catalog) return;

    setSubmitting(true);
    try {
      const created = await srmApi.createRequest({ catalogItemId: catalog.id, formValues });
      toast.success(
        t("requestSubmit.success", {
          ticketKey: created.ticketKey,
          defaultValue: `요청이 접수되었습니다 (${created.ticketKey})`,
        }),
      );
      navigate(`/service-requests/${created.id}`);
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
        {catalog.description ? (
          <p className="text-sm text-muted-foreground">{catalog.description}</p>
        ) : null}
      </div>

      <div className="grid gap-6 lg:grid-cols-[minmax(0,1fr)_20rem]">
        <Card>
          <CardHeader>
            <CardTitle className="text-base">
              {t("requestSubmit.formTitle", { defaultValue: "요청 양식" })}
            </CardTitle>
          </CardHeader>
          <CardContent>
            <DynamicFormRenderer
              schema={schema}
              onSubmit={handleFormSubmit}
              onCancel={() => navigate("/portal")}
              submitLabel={t("requestSubmit.submit", { defaultValue: "제출" })}
              cancelLabel={t("requestSubmit.cancel", { defaultValue: "취소" })}
              disabled={submitting}
            />
          </CardContent>
        </Card>

        {suggestions.length > 0 ? (
          <aside>
            <Card>
              <CardHeader>
                <CardTitle className="text-base">
                  {t("portal.relatedArticlesTitle", { defaultValue: "관련 지식 기사" })}
                </CardTitle>
              </CardHeader>
              <CardContent className="space-y-1">
                {suggestions.map((s) => (
                  <button
                    key={s.articleId}
                    type="button"
                    onClick={() => navigate(`/knowledge/${s.articleId}`)}
                    className="block text-left text-sm text-primary hover:underline"
                  >
                    {s.title}
                  </button>
                ))}
              </CardContent>
            </Card>
          </aside>
        ) : null}
      </div>
    </div>
  );
}
