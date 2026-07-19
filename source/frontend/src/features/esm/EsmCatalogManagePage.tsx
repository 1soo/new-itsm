import { type FormEvent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import type { TFunction } from "i18next";
import { Plus, Trash2 } from "lucide-react";

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
import {
  DynamicFormBuilder,
  DynamicFormRenderer,
  EmptyState,
  EMPTY_GRID_SCHEMA,
  type GridFormSchema,
  Modal,
  toast,
} from "@/components/common";
import { esmApi } from "@/features/esm/api";
import { DEPARTMENTS, TASK_DEPARTMENTS, checklistTemplateTypeLabel, departmentLabel } from "@/features/esm/status";
import type {
  CatalogItemInput,
  CatalogItemSummary,
  ChecklistTemplateTask,
  ChecklistTemplateType,
  Department,
} from "@/features/esm/types";
import { extractErrorMessage } from "@/lib/apiClient";
import { cn } from "@/lib/utils";

/*
 * 부서별 카탈로그 관리(SCR-ESM-006) — 프로세스 오너가 담당 부서 지정 요청 유형(양식·체크리스트 템플릿)을 정의.
 * 좌: 카탈로그 목록 / 우: 편집·생성 폼("Form 설정" 버튼으로 SRM과 공용인 그리드 폼 빌더 팝업 오픈
 * + 축소 미리보기, 2026-07-19 유지보수 요청으로 레거시 EAV FieldBuilder에서 전환 + 체크리스트 템플릿
 * 빌더 반복 입력).
 * 담당 부서 미지정 400, 온보딩/오프보딩인데 템플릿 비어있으면 저장은 허용하되 경고만 표시(실제 거부는 제출 시점).
 */
const CHECKLIST_TEMPLATE_TYPES: ChecklistTemplateType[] = ["NONE", "ONBOARDING", "OFFBOARDING"];

/** Form 설정 축소 미리보기 비율(SRM CatalogManagePage A1과 동일 패턴). */
const PREVIEW_SCALE = 0.45;

interface FormState {
  name: string;
  description: string;
  department: Department | "";
  checklistTemplateType: ChecklistTemplateType;
  checklistTemplate: ChecklistTemplateTask[];
  formSchema: GridFormSchema;
}

const EMPTY_FORM: FormState = {
  name: "",
  description: "",
  department: "",
  checklistTemplateType: "NONE",
  checklistTemplate: [],
  formSchema: EMPTY_GRID_SCHEMA,
};

export function EsmCatalogManagePage() {
  const { t } = useTranslation("esm");
  const [items, setItems] = useState<CatalogItemSummary[]>([]);
  const [selectedId, setSelectedId] = useState<number | null>(null);
  const [form, setForm] = useState<FormState>(EMPTY_FORM);
  const [error, setError] = useState<string | null>(null);
  const [saving, setSaving] = useState(false);
  const [builderOpen, setBuilderOpen] = useState(false);

  const loadList = () => {
    esmApi
      .listCatalog()
      .then(setItems)
      .catch((err) => toast.error(extractErrorMessage(err)));
  };

  useEffect(loadList, []);

  const selectItem = async (id: number) => {
    setError(null);
    try {
      const detail = await esmApi.getCatalogItem(id);
      setSelectedId(id);
      setForm({
        name: detail.name,
        description: detail.description ?? "",
        department: detail.department,
        checklistTemplateType: detail.checklistTemplateType,
        checklistTemplate: detail.checklistTemplate,
        formSchema: detail.formSchema,
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

  const needsChecklist = form.checklistTemplateType !== "NONE";
  const checklistEmptyWarning = needsChecklist && form.checklistTemplate.length === 0;

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError(null);

    if (!form.name.trim()) {
      setError(t("esmCatalogManage.nameRequiredError", { defaultValue: "이름은 필수입니다." }));
      return;
    }
    if (!form.department) {
      setError(t("esmCatalogManage.departmentRequiredError", { defaultValue: "담당 부서는 필수입니다." }));
      return;
    }

    const payload: CatalogItemInput = {
      name: form.name.trim(),
      description: form.description.trim(),
      department: form.department,
      checklistTemplateType: form.checklistTemplateType,
      checklistTemplate: form.checklistTemplate,
      formSchema: form.formSchema,
    };

    setSaving(true);
    try {
      if (selectedId == null) {
        const created = await esmApi.createCatalogItem(payload);
        toast.success(t("esmCatalogManage.createSuccess", { defaultValue: "카탈로그 항목이 생성되었습니다" }));
        loadList();
        selectItem(created.id);
      } else {
        await esmApi.updateCatalogItem(selectedId, payload);
        toast.success(t("esmCatalogManage.updateSuccess", { defaultValue: "카탈로그 항목이 수정되었습니다" }));
        loadList();
      }
    } catch (err) {
      setError(extractErrorMessage(err, t("esmCatalogManage.saveFailed", { defaultValue: "저장에 실패했습니다." })));
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-semibold text-foreground">{t("esmCatalogManage.title", { defaultValue: "부서별 카탈로그 관리" })}</h1>
        <Button onClick={startNew}>
          <Plus />
          {t("esmCatalogManage.newItemButton", { defaultValue: "새 항목" })}
        </Button>
      </div>

      <div className="grid gap-6 lg:grid-cols-[16rem_minmax(0,1fr)]">
        <aside className="space-y-1">
          {items.length === 0 ? (
            <p className="text-sm text-muted-foreground">{t("esmCatalogManage.noItems", { defaultValue: "등록된 항목이 없습니다." })}</p>
          ) : (
            items.map((it) => (
              <button
                key={it.id}
                type="button"
                onClick={() => selectItem(it.id)}
                className={cn(
                  "flex w-full items-center justify-between gap-2 rounded-md px-3 py-2 text-left text-sm transition-colors hover:bg-accent",
                  selectedId === it.id ? "bg-accent font-medium text-foreground" : "text-muted-foreground",
                )}
              >
                <span className="truncate">{it.name}</span>
                <span className="shrink-0 text-xs">{departmentLabel(t, it.department)}</span>
              </button>
            ))
          )}
        </aside>

        <Card>
          <CardHeader>
            <CardTitle className="text-base">
              {selectedId == null
                ? t("esmCatalogManage.newItemCardTitle", { defaultValue: "새 요청 유형" })
                : t("esmCatalogManage.editItemCardTitle", { defaultValue: "요청 유형 편집" })}
            </CardTitle>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleSubmit} className="space-y-4" noValidate>
              <div className="space-y-1.5">
                <Label htmlFor="name">{t("esmCatalogManage.nameLabel", { defaultValue: "이름" })}</Label>
                <Input
                  id="name"
                  value={form.name}
                  onChange={(e) => setForm((f) => ({ ...f, name: e.target.value }))}
                  required
                />
              </div>
              <div className="space-y-1.5">
                <Label htmlFor="desc">{t("esmCatalogManage.descriptionLabel", { defaultValue: "설명" })}</Label>
                <Input
                  id="desc"
                  value={form.description}
                  onChange={(e) => setForm((f) => ({ ...f, description: e.target.value }))}
                />
              </div>
              <div className="grid grid-cols-2 gap-3">
                <div className="space-y-1.5">
                  <Label>{t("esmCatalogManage.departmentLabel", { defaultValue: "담당 부서" })}</Label>
                  <Select
                    value={form.department}
                    onValueChange={(v) => setForm((f) => ({ ...f, department: v as Department }))}
                  >
                    <SelectTrigger>
                      <SelectValue placeholder={t("esmCatalogManage.departmentPlaceholder", { defaultValue: "선택" })} />
                    </SelectTrigger>
                    <SelectContent>
                      {DEPARTMENTS.map((d) => (
                        <SelectItem key={d} value={d}>
                          {departmentLabel(t, d)}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>
                <div className="space-y-1.5">
                  <Label>{t("esmCatalogManage.checklistTypeLabel", { defaultValue: "체크리스트 유형" })}</Label>
                  <Select
                    value={form.checklistTemplateType}
                    onValueChange={(v) =>
                      setForm((f) => ({ ...f, checklistTemplateType: v as ChecklistTemplateType }))
                    }
                  >
                    <SelectTrigger>
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      {CHECKLIST_TEMPLATE_TYPES.map((ty) => (
                        <SelectItem key={ty} value={ty}>
                          {checklistTemplateTypeLabel(t, ty)}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>
              </div>

              {needsChecklist ? (
                <div className="space-y-1.5">
                  <Label>{t("esmCatalogManage.checklistTemplateLabel", { defaultValue: "체크리스트 템플릿(하위 작업)" })}</Label>
                  <ChecklistTemplateBuilder
                    t={t}
                    value={form.checklistTemplate}
                    onChange={(tasks) => setForm((f) => ({ ...f, checklistTemplate: tasks }))}
                  />
                  {checklistEmptyWarning ? (
                    <p className="text-xs text-warning">
                      {t("esmCatalogManage.checklistEmptyWarning", {
                        defaultValue: "하위 작업이 비어 있습니다. 저장은 가능하지만, 이 유형으로 실제 요청 제출 시 거부됩니다.",
                      })}
                    </p>
                  ) : null}
                </div>
              ) : null}

              <div className="space-y-1.5">
                <Label className="block">{t("esmCatalogManage.formSchema", { defaultValue: "양식 필드" })}</Label>
                <Button type="button" variant="outline" className="mt-4" onClick={() => setBuilderOpen(true)}>
                  {t("esmCatalogManage.formBuilder.openButton", { defaultValue: "Form 설정" })}
                </Button>
                <div
                  role="button"
                  tabIndex={0}
                  onClick={() => setBuilderOpen(true)}
                  onKeyDown={(e) => {
                    if (e.key === "Enter" || e.key === " ") setBuilderOpen(true);
                  }}
                  className="relative block w-full cursor-pointer overflow-hidden rounded-md border border-border bg-muted/20 text-left"
                  style={{ height: 160 }}
                >
                  {form.formSchema.components.length === 0 ? (
                    <p className="flex h-full items-center justify-center text-sm text-muted-foreground">
                      {t("esmCatalogManage.formBuilder.previewEmpty", {
                        defaultValue: "설정된 양식이 없습니다. 클릭하여 Form을 설정하세요.",
                      })}
                    </p>
                  ) : (
                    <div
                      className="pointer-events-none absolute left-0 top-0"
                      style={{
                        transform: `scale(${PREVIEW_SCALE})`,
                        transformOrigin: "top left",
                        width: `${100 / PREVIEW_SCALE}%`,
                      }}
                    >
                      <DynamicFormRenderer schema={form.formSchema} onSubmit={() => {}} disabled hideFooter />
                    </div>
                  )}
                </div>
              </div>

              {error ? (
                <p role="alert" className="text-sm text-danger">
                  {error}
                </p>
              ) : null}

              <div className="flex justify-end">
                <Button type="submit" loading={saving}>
                  {t("esmCatalogManage.saveButton", { defaultValue: "저장" })}
                </Button>
              </div>
            </form>
          </CardContent>
        </Card>
      </div>

      <Modal
        open={builderOpen}
        onOpenChange={setBuilderOpen}
        title={t("esmCatalogManage.formBuilder.modalTitle", { defaultValue: "Form 설정" })}
        className="flex h-[85vh] w-[90vw] max-w-[1600px] flex-col"
      >
        <DynamicFormBuilder
          initialSchema={form.formSchema}
          onApply={(schema) => {
            setForm((f) => ({ ...f, formSchema: schema }));
            setBuilderOpen(false);
          }}
          onCancel={() => setBuilderOpen(false)}
          className="flex-1"
        />
      </Modal>
    </div>
  );
}

function ChecklistTemplateBuilder({
  t,
  value,
  onChange,
}: {
  t: TFunction;
  value: ChecklistTemplateTask[];
  onChange: (tasks: ChecklistTemplateTask[]) => void;
}) {
  const update = (index: number, patch: Partial<ChecklistTemplateTask>) => {
    onChange(value.map((task, i) => (i === index ? { ...task, ...patch } : task)));
  };
  const add = () => {
    onChange([...value, { department: TASK_DEPARTMENTS[0], taskDescription: "" }]);
  };
  const remove = (index: number) => {
    onChange(value.filter((_, i) => i !== index));
  };

  return (
    <div className="flex flex-col gap-3">
      {value.length === 0 ? (
        <EmptyState
          title={t("esmCatalogManage.noTasksTitle", { defaultValue: "정의된 하위 작업이 없습니다" })}
          description={t("esmCatalogManage.noTasksDescription", { defaultValue: "아래 버튼으로 하위 작업을 추가하세요." })}
        />
      ) : (
        value.map((task, index) => (
          <div key={index} className="grid gap-3 rounded-lg border border-border bg-card p-3 sm:grid-cols-[10rem_1fr_auto]">
            <div className="flex flex-col gap-1.5">
              <Label>{t("esmCatalogManage.departmentLabel", { defaultValue: "담당 부서" })}</Label>
              <Select
                value={task.department as string}
                onValueChange={(v) => update(index, { department: v as Department })}
              >
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  {TASK_DEPARTMENTS.map((d) => (
                    <SelectItem key={d} value={d}>
                      {departmentLabel(t, d)}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            <div className="flex flex-col gap-1.5">
              <Label>{t("esmCatalogManage.taskDescriptionLabel", { defaultValue: "작업 설명" })}</Label>
              <Input
                value={task.taskDescription}
                onChange={(e) => update(index, { taskDescription: e.target.value })}
                placeholder={t("esmCatalogManage.taskDescriptionPlaceholder", { defaultValue: "예: 사원증 발급" })}
              />
            </div>
            <div className="flex items-end justify-end pb-1">
              <Button
                type="button"
                variant="ghost"
                size="icon"
                aria-label={t("esmCatalogManage.removeTaskAria", { defaultValue: "하위 작업 삭제" })}
                onClick={() => remove(index)}
              >
                <Trash2 className="text-destructive" />
              </Button>
            </div>
          </div>
        ))
      )}
      <Button type="button" variant="outline" onClick={add} className="self-start">
        <Plus /> {t("esmCatalogManage.addTaskButton", { defaultValue: "하위 작업 추가" })}
      </Button>
    </div>
  );
}
