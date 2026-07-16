import { type FormEvent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { Plus } from "lucide-react";

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
  type Column,
  ConfirmDialog,
  DataTable,
  FieldBuilder,
  type FormFieldSchema,
  Modal,
  toast,
} from "@/components/common";
import { authApi } from "@/features/auth/api";
import type { Role } from "@/features/auth/types";
import { srmApi } from "@/features/service-request/api";
import type {
  CatalogItemInput,
  CatalogItemSummary,
  Category,
  Queue,
} from "@/features/service-request/types";
import { extractErrorMessage } from "@/lib/apiClient";
import { cn } from "@/lib/utils";

/*
 * 서비스 카탈로그 관리(SCR-SRM-007) — 프로세스 오너가 요청 유형(양식·SLA·큐·카테고리)을 정의.
 * 승인 여부·경로는 SCR-ADMIN-008(승인 프로세스 생성/편집)에서 별도 설정(승인 프로세스 커스텀 기능으로 대체).
 * 상단 탭("카탈로그 항목"/"카테고리 관리", Tabs 공통 컴포넌트 없어 버튼형 토글로 구현 — ESM DeptPortalPage와 동일 패턴).
 * "카탈로그 항목" 탭: 좌 카탈로그 목록 / 우 편집·생성 폼(FieldBuilder로 동적 필드 정의). 이름·양식 누락 시 400 인라인.
 * "카테고리 관리" 탭(SCR-SRM-009): 목록 표 + 생성/수정 모달 + 삭제 확인 다이얼로그.
 */
/** Select value sentinel — "미분류"/"선택 안 함"(Radix Select는 빈 문자열 value를 허용하지 않음). */
const NONE_VALUE = "__NONE__";

type ManageTab = "items" | "categories";

interface FormState {
  name: string;
  description: string;
  categoryId: string;
  queueId: string;
  assigneeRoleId: string;
  slaResponseMinutes: string;
  slaResolveMinutes: string;
  formSchema: FormFieldSchema[];
}

const EMPTY_FORM: FormState = {
  name: "",
  description: "",
  categoryId: NONE_VALUE,
  queueId: NONE_VALUE,
  assigneeRoleId: NONE_VALUE,
  slaResponseMinutes: "",
  slaResolveMinutes: "",
  formSchema: [],
};

interface CategoryFormState {
  name: string;
  sortOrder: string;
}

const EMPTY_CATEGORY_FORM: CategoryFormState = { name: "", sortOrder: "" };

export function CatalogManagePage() {
  const { t } = useTranslation("service-request");
  const [tab, setTab] = useState<ManageTab>("items");
  const [items, setItems] = useState<CatalogItemSummary[]>([]);
  const [queues, setQueues] = useState<Queue[]>([]);
  const [roles, setRoles] = useState<Role[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [selectedId, setSelectedId] = useState<number | null>(null);
  const [form, setForm] = useState<FormState>(EMPTY_FORM);
  const [error, setError] = useState<string | null>(null);
  const [saving, setSaving] = useState(false);

  const [categoryModalOpen, setCategoryModalOpen] = useState(false);
  const [editingCategory, setEditingCategory] = useState<Category | null>(null);
  const [categoryForm, setCategoryForm] = useState<CategoryFormState>(EMPTY_CATEGORY_FORM);
  const [categoryError, setCategoryError] = useState<string | null>(null);
  const [categorySaving, setCategorySaving] = useState(false);
  const [deletingCategory, setDeletingCategory] = useState<Category | null>(null);
  const [categoryDeleteBusy, setCategoryDeleteBusy] = useState(false);

  const loadList = () => {
    srmApi
      .listCatalog()
      .then(setItems)
      .catch((err) => toast.error(extractErrorMessage(err)));
  };

  const loadCategories = () => {
    srmApi
      .listCategories()
      .then(setCategories)
      .catch((err) => toast.error(extractErrorMessage(err)));
  };

  useEffect(loadList, []);
  useEffect(loadCategories, []);

  useEffect(() => {
    srmApi.listQueues().then(setQueues).catch((err) => toast.error(extractErrorMessage(err)));
    authApi.getRoles().then(setRoles).catch((err) => toast.error(extractErrorMessage(err)));
  }, []);

  const selectItem = async (id: number) => {
    setError(null);
    try {
      const detail = await srmApi.getCatalogItem(id);
      setSelectedId(id);
      setForm({
        name: detail.name,
        description: detail.description ?? "",
        categoryId: detail.categoryId != null ? String(detail.categoryId) : NONE_VALUE,
        queueId: detail.queueId != null ? String(detail.queueId) : NONE_VALUE,
        assigneeRoleId: detail.assigneeRoleId != null ? String(detail.assigneeRoleId) : NONE_VALUE,
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
      categoryId: form.categoryId === NONE_VALUE ? undefined : Number(form.categoryId),
      queueId: form.queueId === NONE_VALUE ? undefined : Number(form.queueId),
      assigneeRoleId: form.assigneeRoleId === NONE_VALUE ? undefined : Number(form.assigneeRoleId),
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

  const openCreateCategory = () => {
    setEditingCategory(null);
    setCategoryForm(EMPTY_CATEGORY_FORM);
    setCategoryError(null);
    setCategoryModalOpen(true);
  };

  const openEditCategory = (category: Category) => {
    setEditingCategory(category);
    setCategoryForm({ name: category.name, sortOrder: String(category.sortOrder ?? 0) });
    setCategoryError(null);
    setCategoryModalOpen(true);
  };

  const handleCategorySubmit = async (e: FormEvent) => {
    e.preventDefault();
    setCategoryError(null);

    if (!categoryForm.name.trim()) {
      setCategoryError(t("catalogManage.categoryNameRequiredError", { defaultValue: "이름은 필수입니다." }));
      return;
    }

    const payload = {
      name: categoryForm.name.trim(),
      sortOrder: categoryForm.sortOrder === "" ? undefined : Number(categoryForm.sortOrder),
    };

    setCategorySaving(true);
    try {
      if (editingCategory == null) {
        await srmApi.createCategory(payload);
        toast.success(t("catalogManage.categoryCreateSuccess", { defaultValue: "카테고리가 생성되었습니다" }));
      } else {
        await srmApi.updateCategory(editingCategory.id, payload);
        toast.success(t("catalogManage.categoryUpdateSuccess", { defaultValue: "카테고리가 수정되었습니다" }));
      }
      setCategoryModalOpen(false);
      loadCategories();
    } catch (err) {
      setCategoryError(
        extractErrorMessage(err, t("catalogManage.categorySaveFailed", { defaultValue: "저장에 실패했습니다." })),
      );
    } finally {
      setCategorySaving(false);
    }
  };

  const handleDeleteCategory = async () => {
    if (!deletingCategory) return;
    setCategoryDeleteBusy(true);
    try {
      await srmApi.deleteCategory(deletingCategory.id);
      toast.success(t("catalogManage.categoryDeleteSuccess", { defaultValue: "카테고리가 삭제되었습니다" }));
      setDeletingCategory(null);
      loadCategories();
    } catch (err) {
      toast.error(extractErrorMessage(err));
    } finally {
      setCategoryDeleteBusy(false);
    }
  };

  const categoryColumns: Column<Category>[] = [
    { header: t("catalogManage.categoryColumnName", { defaultValue: "이름" }), cell: (c) => c.name },
    {
      header: t("catalogManage.categoryColumnSortOrder", { defaultValue: "표시 순서" }),
      cell: (c) => c.sortOrder,
      className: "text-right",
      width: 110,
    },
    {
      header: t("catalogManage.categoryColumnItemCount", { defaultValue: "사용 항목 수" }),
      cell: (c) => c.itemCount ?? 0,
      className: "text-right",
      width: 110,
    },
    {
      header: t("catalogManage.categoryColumnActions", { defaultValue: "관리" }),
      cell: (c) => (
        <div className="flex justify-end gap-2">
          <Button
            type="button"
            size="sm"
            variant="outline"
            onClick={(e) => {
              e.stopPropagation();
              openEditCategory(c);
            }}
          >
            {t("catalogManage.categoryEdit", { defaultValue: "수정" })}
          </Button>
          <Button
            type="button"
            size="sm"
            variant="destructive"
            onClick={(e) => {
              e.stopPropagation();
              setDeletingCategory(c);
            }}
          >
            {t("catalogManage.categoryDelete", { defaultValue: "삭제" })}
          </Button>
        </div>
      ),
      className: "text-right",
      width: 180,
    },
  ];

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-semibold text-foreground">
          {t("catalogManage.title", { defaultValue: "서비스 카탈로그 관리" })}
        </h1>
        {tab === "items" ? (
          <Button onClick={startNew}>
            <Plus />
            {t("catalogManage.newItem", { defaultValue: "새 항목" })}
          </Button>
        ) : (
          <Button onClick={openCreateCategory}>
            <Plus />
            {t("catalogManage.newCategory", { defaultValue: "새 카테고리" })}
          </Button>
        )}
      </div>

      <div className="flex gap-1 border-b border-border">
        <ManageTabButton
          label={t("catalogManage.tabItems", { defaultValue: "카탈로그 항목" })}
          active={tab === "items"}
          onClick={() => setTab("items")}
        />
        <ManageTabButton
          label={t("catalogManage.tabCategories", { defaultValue: "카테고리 관리" })}
          active={tab === "categories"}
          onClick={() => setTab("categories")}
        />
      </div>

      {tab === "categories" ? (
        <DataTable
          columns={categoryColumns}
          data={categories}
          rowKey={(c) => c.id}
          emptyTitle={t("catalogManage.categoryEmptyTitle", { defaultValue: "등록된 카테고리가 없습니다" })}
        />
      ) : (
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
              <div className="grid grid-cols-2 gap-3">
                <div className="space-y-1.5">
                  <Label htmlFor="category">{t("catalogManage.categoryId", { defaultValue: "카테고리" })}</Label>
                  <Select
                    value={form.categoryId}
                    onValueChange={(v) => setForm((f) => ({ ...f, categoryId: v }))}
                  >
                    <SelectTrigger id="category">
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value={NONE_VALUE}>
                        {t("catalogManage.categoryUnassigned", { defaultValue: "미분류" })}
                      </SelectItem>
                      {categories.map((c) => (
                        <SelectItem key={c.id} value={String(c.id)}>
                          {c.name}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>
                <div className="space-y-1.5">
                  <Label htmlFor="queue">{t("catalogManage.queueId", { defaultValue: "담당 큐" })}</Label>
                  <Select
                    value={form.queueId}
                    onValueChange={(v) => setForm((f) => ({ ...f, queueId: v }))}
                  >
                    <SelectTrigger id="queue">
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value={NONE_VALUE}>
                        {t("catalogManage.queueUnassigned", { defaultValue: "미분류" })}
                      </SelectItem>
                      {queues.map((q) => (
                        <SelectItem key={q.id} value={String(q.id)}>
                          {q.name}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>
                <div className="space-y-1.5">
                  <Label htmlFor="assignee-role">
                    {t("catalogManage.assigneeRoleId", { defaultValue: "담당자 역할" })}
                  </Label>
                  <Select
                    value={form.assigneeRoleId}
                    onValueChange={(v) => setForm((f) => ({ ...f, assigneeRoleId: v }))}
                  >
                    <SelectTrigger id="assignee-role">
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value={NONE_VALUE}>
                        {t("catalogManage.assigneeRoleUnselected", { defaultValue: "선택 안 함" })}
                      </SelectItem>
                      {roles.map((r) => (
                        <SelectItem key={r.id} value={String(r.id)}>
                          {r.name}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>
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
      )}

      <Modal
        open={categoryModalOpen}
        onOpenChange={setCategoryModalOpen}
        title={
          editingCategory == null
            ? t("catalogManage.newCategoryTitle", { defaultValue: "새 카테고리" })
            : t("catalogManage.editCategoryTitle", { defaultValue: "카테고리 수정" })
        }
      >
        <form onSubmit={handleCategorySubmit} className="space-y-4" noValidate>
          <div className="space-y-1.5">
            <Label htmlFor="category-name">{t("catalogManage.categoryColumnName", { defaultValue: "이름" })}</Label>
            <Input
              id="category-name"
              value={categoryForm.name}
              onChange={(e) => setCategoryForm((f) => ({ ...f, name: e.target.value }))}
              required
              aria-invalid={!!categoryError}
            />
          </div>
          <div className="space-y-1.5">
            <Label htmlFor="category-sort-order">
              {t("catalogManage.categoryColumnSortOrder", { defaultValue: "표시 순서" })}
            </Label>
            <Input
              id="category-sort-order"
              type="number"
              value={categoryForm.sortOrder}
              onChange={(e) => setCategoryForm((f) => ({ ...f, sortOrder: e.target.value }))}
            />
          </div>
          {categoryError ? (
            <p role="alert" className="text-sm text-danger">
              {categoryError}
            </p>
          ) : null}
          <div className="flex justify-end gap-2">
            <Button type="button" variant="outline" onClick={() => setCategoryModalOpen(false)}>
              {t("catalogManage.categoryCancel", { defaultValue: "취소" })}
            </Button>
            <Button type="submit" loading={categorySaving}>
              {t("catalogManage.save", { defaultValue: "저장" })}
            </Button>
          </div>
        </form>
      </Modal>

      <ConfirmDialog
        open={!!deletingCategory}
        onOpenChange={(open) => !open && setDeletingCategory(null)}
        title={t("catalogManage.categoryDeleteDialogTitle", { defaultValue: "카테고리 삭제" })}
        description={t("catalogManage.categoryDeleteDialogDescription", {
          name: deletingCategory?.name ?? "",
          defaultValue: `"${deletingCategory?.name ?? ""}" 카테고리를 삭제하시겠습니까? 이 카테고리를 사용하는 카탈로그 항목이 있으면 삭제할 수 없습니다.`,
        })}
        confirmLabel={t("catalogManage.categoryDelete", { defaultValue: "삭제" })}
        loading={categoryDeleteBusy}
        onConfirm={handleDeleteCategory}
      />
    </div>
  );
}

function ManageTabButton({ label, active, onClick }: { label: string; active: boolean; onClick: () => void }) {
  return (
    <button
      type="button"
      onClick={onClick}
      className={cn(
        "-mb-px border-b-2 px-3 py-2 text-sm transition-colors",
        active
          ? "border-primary font-medium text-foreground"
          : "border-transparent text-muted-foreground hover:text-foreground",
      )}
    >
      {label}
    </button>
  );
}
