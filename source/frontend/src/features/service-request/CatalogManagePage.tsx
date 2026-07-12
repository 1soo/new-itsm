import { type FormEvent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { Plus } from "lucide-react";

import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { FieldBuilder, type FormFieldSchema, toast } from "@/components/common";
import { srmApi } from "@/features/service-request/api";
import type {
  CatalogItemInput,
  CatalogItemSummary,
} from "@/features/service-request/types";
import { extractErrorMessage } from "@/lib/apiClient";
import { cn } from "@/lib/utils";

/*
 * 서비스 카탈로그 관리(SCR-SRM-007) — 프로세스 오너가 요청 유형(양식·SLA·큐)을 정의.
 * 승인 여부·경로는 SCR-ADMIN-008(승인 프로세스 생성/편집)에서 별도 설정(승인 프로세스 커스텀 기능으로 대체).
 * 좌: 카탈로그 목록 / 우: 편집·생성 폼(FieldBuilder로 동적 필드 정의). 이름·양식 누락 시 400 인라인.
 */
interface FormState {
  name: string;
  description: string;
  queueId: string;
  slaResponseMinutes: string;
  slaResolveMinutes: string;
  formSchema: FormFieldSchema[];
}

const EMPTY_FORM: FormState = {
  name: "",
  description: "",
  queueId: "",
  slaResponseMinutes: "",
  slaResolveMinutes: "",
  formSchema: [],
};

export function CatalogManagePage() {
  const { t } = useTranslation("service-request");
  const [items, setItems] = useState<CatalogItemSummary[]>([]);
  const [selectedId, setSelectedId] = useState<number | null>(null);
  const [form, setForm] = useState<FormState>(EMPTY_FORM);
  const [error, setError] = useState<string | null>(null);
  const [saving, setSaving] = useState(false);

  const loadList = () => {
    srmApi
      .listCatalog()
      .then(setItems)
      .catch((err) => toast.error(extractErrorMessage(err)));
  };

  useEffect(loadList, []);

  const selectItem = async (id: number) => {
    setError(null);
    try {
      const detail = await srmApi.getCatalogItem(id);
      setSelectedId(id);
      setForm({
        name: detail.name,
        description: detail.description ?? "",
        queueId: "",
        slaResponseMinutes: String(detail.slaResponseMinutes ?? ""),
        slaResolveMinutes: String(detail.slaResolveMinutes ?? ""),
        formSchema: detail.formSchema as FormFieldSchema[],
      });
    } catch (err) {
      toast.error(extractErrorMessage(err));
    }
  };

  const startNew = () => {
    setSelectedId(null);
    setForm(EMPTY_FORM);
    setError(null);
  };

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError(null);

    if (!form.name.trim()) {
      setError(t("catalogManage.nameRequiredError", { defaultValue: "이름은 필수입니다." }));
      return;
    }
    if (form.formSchema.length === 0) {
      setError(t("catalogManage.schemaRequiredError", { defaultValue: "양식 필드를 하나 이상 정의하세요." }));
      return;
    }

    const payload: CatalogItemInput = {
      name: form.name.trim(),
      description: form.description.trim(),
      queueId: form.queueId ? Number(form.queueId) : undefined,
      slaResponseMinutes: Number(form.slaResponseMinutes) || 0,
      slaResolveMinutes: Number(form.slaResolveMinutes) || 0,
      formSchema: form.formSchema,
    };

    setSaving(true);
    try {
      if (selectedId == null) {
        const created = await srmApi.createCatalogItem(payload);
        toast.success(t("catalogManage.createSuccess", { defaultValue: "카탈로그 항목이 생성되었습니다" }));
        loadList();
        selectItem(created.id);
      } else {
        await srmApi.updateCatalogItem(selectedId, payload);
        toast.success(t("catalogManage.updateSuccess", { defaultValue: "카탈로그 항목이 수정되었습니다" }));
        loadList();
      }
    } catch (err) {
      setError(extractErrorMessage(err, t("catalogManage.saveFailed", { defaultValue: "저장에 실패했습니다." })));
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-semibold text-foreground">
          {t("catalogManage.title", { defaultValue: "서비스 카탈로그 관리" })}
        </h1>
        <Button onClick={startNew}>
          <Plus />
          {t("catalogManage.newItem", { defaultValue: "새 항목" })}
        </Button>
      </div>

      <div className="grid gap-6 lg:grid-cols-[16rem_minmax(0,1fr)]">
        <aside className="space-y-1">
          {items.length === 0 ? (
            <p className="text-sm text-muted-foreground">
              {t("catalogManage.emptyList", { defaultValue: "등록된 항목이 없습니다." })}
            </p>
          ) : (
            items.map((it) => (
              <button
                key={it.id}
                type="button"
                onClick={() => selectItem(it.id)}
                className={cn(
                  "block w-full rounded-md px-3 py-2 text-left text-sm transition-colors hover:bg-accent",
                  selectedId === it.id ? "bg-accent font-medium text-foreground" : "text-muted-foreground",
                )}
              >
                {it.name}
              </button>
            ))
          )}
        </aside>

        <Card>
          <CardHeader>
            <CardTitle className="text-base">
              {selectedId == null
                ? t("catalogManage.newItemTitle", { defaultValue: "새 요청 유형" })
                : t("catalogManage.editItemTitle", { defaultValue: "요청 유형 편집" })}
            </CardTitle>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleSubmit} className="space-y-4" noValidate>
              <div className="space-y-1.5">
                <Label htmlFor="name">{t("catalogManage.name", { defaultValue: "이름" })}</Label>
                <Input
                  id="name"
                  value={form.name}
                  onChange={(e) => setForm((f) => ({ ...f, name: e.target.value }))}
                  required
                />
              </div>
              <div className="space-y-1.5">
                <Label htmlFor="desc">{t("catalogManage.description", { defaultValue: "설명" })}</Label>
                <Input
                  id="desc"
                  value={form.description}
                  onChange={(e) => setForm((f) => ({ ...f, description: e.target.value }))}
                />
              </div>
              <div className="grid grid-cols-2 gap-3">
                <div className="space-y-1.5">
                  <Label htmlFor="sla-res">
                    {t("catalogManage.slaResponseMinutes", { defaultValue: "응답 SLA(분)" })}
                  </Label>
                  <Input
                    id="sla-res"
                    type="number"
                    value={form.slaResponseMinutes}
                    onChange={(e) => setForm((f) => ({ ...f, slaResponseMinutes: e.target.value }))}
                  />
                </div>
                <div className="space-y-1.5">
                  <Label htmlFor="sla-rsv">
                    {t("catalogManage.slaResolveMinutes", { defaultValue: "해결 SLA(분)" })}
                  </Label>
                  <Input
                    id="sla-rsv"
                    type="number"
                    value={form.slaResolveMinutes}
                    onChange={(e) => setForm((f) => ({ ...f, slaResolveMinutes: e.target.value }))}
                  />
                </div>
              </div>
              <div className="space-y-1.5">
                <Label htmlFor="queue">{t("catalogManage.queueId", { defaultValue: "담당 큐 ID(선택)" })}</Label>
                <Input
                  id="queue"
                  type="number"
                  value={form.queueId}
                  onChange={(e) => setForm((f) => ({ ...f, queueId: e.target.value }))}
                />
              </div>
              <div className="space-y-1.5">
                <Label>{t("catalogManage.formSchema", { defaultValue: "양식 필드" })}</Label>
                <FieldBuilder
                  value={form.formSchema}
                  onChange={(fields) => setForm((f) => ({ ...f, formSchema: fields }))}
                />
              </div>

              {error ? (
                <p role="alert" className="text-sm text-danger">
                  {error}
                </p>
              ) : null}

              <div className="flex justify-end">
                <Button type="submit" loading={saving}>
                  {t("catalogManage.save", { defaultValue: "저장" })}
                </Button>
              </div>
            </form>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
