import { type FormEvent, useEffect, useMemo, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { useTranslation } from "react-i18next";

import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import {
  DynamicForm,
  type FormErrors,
  type FormFieldSchema,
  type FormValues,
  toast,
  validateForm,
} from "@/components/common";
import { FullscreenLoader } from "@/routes/FullscreenLoader";
import { srmApi } from "@/features/service-request/api";
import type {
  CatalogItemDetail,
  KnowledgeSuggestion,
} from "@/features/service-request/types";
import { extractErrorMessage } from "@/lib/apiClient";

/*
 * 요청 제출(SCR-SRM-002) — 카탈로그 항목의 동적 양식을 작성해 제출.
 * 우측 "관련 지식 기사" 추천 패널(있을 때만). 필수 필드 미입력 시 제출 차단·인라인 오류.
 * 제출 성공 시 접수번호 토스트 + 상세 이동.
 */
export function RequestSubmitPage() {
  const { t } = useTranslation("service-request");
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const itemId = Number(searchParams.get("item"));

  const [catalog, setCatalog] = useState<CatalogItemDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [values, setValues] = useState<FormValues>({});
  const [errors, setErrors] = useState<FormErrors>({});
  const [submitting, setSubmitting] = useState(false);
  const [suggestions, setSuggestions] = useState<KnowledgeSuggestion[]>([]);

  const schema = useMemo<FormFieldSchema[]>(
    () => (catalog?.formSchema ?? []) as FormFieldSchema[],
    [catalog],
  );

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

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    if (!catalog) return;
    const validation = validateForm(schema, values, t);
    setErrors(validation);
    if (Object.keys(validation).length > 0) return;

    // 파일 값은 JSON 전송을 위해 파일명으로 대체(첨부 업로드는 이번 범위 밖).
    const formValues = Object.fromEntries(
      Object.entries(values).map(([k, v]) => [k, v instanceof File ? v.name : v]),
    );

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
            <form onSubmit={handleSubmit} className="space-y-6" noValidate>
              <DynamicForm schema={schema} values={values} onChange={setValues} errors={errors} />
              <div className="flex justify-end gap-2">
                <Button type="button" variant="outline" onClick={() => navigate("/portal")}>
                  {t("requestSubmit.cancel", { defaultValue: "취소" })}
                </Button>
                <Button type="submit" loading={submitting}>
                  {t("requestSubmit.submit", { defaultValue: "제출" })}
                </Button>
              </div>
            </form>
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
