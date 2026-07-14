import { type FormEvent, useEffect, useState } from "react";
import * as DialogPrimitive from "@radix-ui/react-dialog";
import { useTranslation } from "react-i18next";
import type { TFunction } from "i18next";
import { Plus, X } from "lucide-react";

import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Checkbox } from "@/components/ui/checkbox";
import {
  Dialog,
  DialogClose,
  DialogOverlay,
  DialogPortal,
  DialogTitle,
} from "@/components/ui/dialog";
import {
  type Column,
  ConfirmDialog,
  DataTable,
  Modal,
  Pagination,
  toast,
} from "@/components/common";
import { Badge } from "@/components/ui/badge";
import { adminApi } from "@/features/admin/api";
import type { Role, Screen } from "@/features/admin/types";
import { extractErrorMessage } from "@/lib/apiClient";
import { resolveIcon } from "@/lib/icon";
import { cn } from "@/lib/utils";

/*
 * 메뉴 관리(SCR-ADMIN-006, Role-Menu 동적 매핑) — 메뉴(화면) 마스터 CRUD + 역할별 노출 매핑.
 * 목록/생성·수정 모달은 계정·역할 관리와 동일 패턴, 역할 매핑은 우측 슬라이드 패널(체크박스,
 * 토글마다 즉시 API 호출)로 처리한다. 삭제·역할 매핑은 sortOrder 오름차순 목록에 즉시 반영된다.
 */
const PAGE_SIZE = 16;
const NO_GROUP = "";
const NEW_GROUP = "__new__";

interface MenuFormState {
  screenCode: string;
  screenName: string;
  screenNameEn: string;
  path: string;
  domain: string;
  iconName: string;
  groupOption: string;
  newGroupCode: string;
  newGroupLabel: string;
  /** 그룹 영문명(신규/기존 그룹 공통 편집 필드, 기존 그룹 선택 시 자동 표시 후 수정 가능). */
  groupLabelEn: string;
  sortOrder: string;
  navVisible: boolean;
}

const EMPTY_FORM: MenuFormState = {
  screenCode: "",
  screenName: "",
  screenNameEn: "",
  path: "",
  domain: "",
  iconName: "",
  groupOption: NO_GROUP,
  newGroupCode: "",
  newGroupLabel: "",
  groupLabelEn: "",
  sortOrder: "0",
  navVisible: true,
};

export function MenuManagementPage() {
  const { t } = useTranslation("auth");
  const [data, setData] = useState<{ content: Screen[]; totalElements: number }>({
    content: [],
    totalElements: 0,
  });
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(true);
  const [roles, setRoles] = useState<Role[]>([]);

  const [formOpen, setFormOpen] = useState(false);
  const [editing, setEditing] = useState<Screen | null>(null);
  const [form, setForm] = useState<MenuFormState>(EMPTY_FORM);
  const [formError, setFormError] = useState<string | null>(null);
  const [saving, setSaving] = useState(false);

  const [deleting, setDeleting] = useState<Screen | null>(null);
  const [deleteBusy, setDeleteBusy] = useState(false);

  const [rolePanel, setRolePanel] = useState<Screen | null>(null);
  const [roleBusyId, setRoleBusyId] = useState<number | null>(null);

  // 그룹 선택 후보(선택 또는 신규 입력) — 페이지네이션과 무관하게 전체 목록에서 뽑는다.
  const [existingGroups, setExistingGroups] = useState<
    { groupCode: string; groupLabel: string; groupLabelEn: string }[]
  >([]);

  const loadScreens = () => {
    setLoading(true);
    adminApi
      .listScreens({ page, size: PAGE_SIZE })
      .then(setData)
      .catch((err) => toast.error(extractErrorMessage(err)))
      .finally(() => setLoading(false));
  };

  const loadExistingGroups = () => {
    adminApi
      .listScreens({ page: 0, size: 200 })
      .then((res) => {
        const map = new Map<string, { groupLabel: string; groupLabelEn: string }>();
        for (const s of res.content) {
          if (s.groupCode) {
            map.set(s.groupCode, {
              groupLabel: s.groupLabel ?? s.groupCode,
              groupLabelEn: s.groupLabelEn ?? "",
            });
          }
        }
        setExistingGroups(
          Array.from(map.entries()).map(([groupCode, v]) => ({ groupCode, ...v })),
        );
      })
      .catch(() => {});
  };

  useEffect(loadScreens, [page]);
  useEffect(loadExistingGroups, []);

  useEffect(() => {
    adminApi
      .listRoles()
      .then(setRoles)
      .catch((err) => toast.error(extractErrorMessage(err)));
  }, []);

  const openCreate = () => {
    setEditing(null);
    setForm(EMPTY_FORM);
    setFormError(null);
    setFormOpen(true);
  };

  const openEdit = (screen: Screen) => {
    setEditing(screen);
    setForm({
      screenCode: screen.screenCode,
      screenName: screen.screenName,
      screenNameEn: screen.screenNameEn ?? "",
      path: screen.path,
      domain: screen.domain,
      iconName: screen.iconName ?? "",
      groupOption: screen.groupCode ?? NO_GROUP,
      newGroupCode: "",
      newGroupLabel: "",
      groupLabelEn: screen.groupLabelEn ?? "",
      sortOrder: String(screen.sortOrder),
      navVisible: screen.navVisible,
    });
    setFormError(null);
    setFormOpen(true);
  };

  /** 그룹 선택 변경 시 그룹 영문명을 함께 갱신한다(기존 그룹은 저장된 값 자동 표시, 신규/없음은 초기화). */
  const handleGroupOptionChange = (groupOption: string) => {
    const found = existingGroups.find((g) => g.groupCode === groupOption);
    setForm((f) => ({ ...f, groupOption, groupLabelEn: found?.groupLabelEn ?? "" }));
  };

  const resolvedGroup = (): { groupCode?: string; groupLabel?: string; groupLabelEn?: string } => {
    if (form.groupOption === NO_GROUP) return {};
    if (form.groupOption === NEW_GROUP) {
      return {
        groupCode: form.newGroupCode.trim(),
        groupLabel: form.newGroupLabel.trim(),
        groupLabelEn: form.groupLabelEn.trim() || undefined,
      };
    }
    const found = existingGroups.find((g) => g.groupCode === form.groupOption);
    return {
      groupCode: form.groupOption,
      groupLabel: found?.groupLabel,
      groupLabelEn: form.groupLabelEn.trim() || found?.groupLabelEn || undefined,
    };
  };

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setFormError(null);
    setSaving(true);
    const { groupCode, groupLabel, groupLabelEn } = resolvedGroup();
    try {
      if (editing) {
        await adminApi.updateScreen(editing.id, {
          screenName: form.screenName,
          screenNameEn: form.screenNameEn,
          path: form.path,
          iconName: form.iconName || undefined,
          groupCode,
          groupLabel,
          groupLabelEn,
          sortOrder: Number(form.sortOrder) || 0,
          navVisible: form.navVisible,
        });
        toast.success(t("admin.menu.updateSuccess", { defaultValue: "메뉴가 수정되었습니다" }));
      } else {
        await adminApi.createScreen({
          screenCode: form.screenCode,
          screenName: form.screenName,
          screenNameEn: form.screenNameEn,
          path: form.path,
          domain: form.domain,
          iconName: form.iconName || undefined,
          groupCode,
          groupLabel,
          groupLabelEn,
          sortOrder: Number(form.sortOrder) || 0,
          navVisible: form.navVisible,
        });
        toast.success(t("admin.menu.createSuccess", { defaultValue: "메뉴가 생성되었습니다" }));
      }
      setFormOpen(false);
      loadScreens();
      loadExistingGroups();
    } catch (err) {
      setFormError(extractErrorMessage(err, t("admin.menu.saveFailed", { defaultValue: "메뉴 저장에 실패했습니다." })));
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async () => {
    if (!deleting) return;
    setDeleteBusy(true);
    try {
      await adminApi.deleteScreen(deleting.id);
      toast.success(t("admin.menu.deleteSuccess", { defaultValue: "메뉴가 삭제되었습니다" }));
      setDeleting(null);
      loadScreens();
    } catch (err) {
      toast.error(extractErrorMessage(err));
    } finally {
      setDeleteBusy(false);
    }
  };

  const handleToggleRole = async (role: Role, checked: boolean) => {
    if (!rolePanel) return;
    setRoleBusyId(role.id);
    try {
      const res = checked
        ? await adminApi.assignScreenRole(rolePanel.id, role.id)
        : await adminApi.revokeScreenRole(rolePanel.id, role.id);
      setRolePanel({ ...rolePanel, roles: res.roles });
      setData((prev) => ({
        ...prev,
        content: prev.content.map((s) => (s.id === rolePanel.id ? { ...s, roles: res.roles } : s)),
      }));
    } catch (err) {
      toast.error(extractErrorMessage(err));
    } finally {
      setRoleBusyId(null);
    }
  };

  const columns: Column<Screen>[] = [
    {
      header: t("admin.menu.columnGroup", { defaultValue: "그룹" }),
      width: 110,
      cell: (s) => s.groupLabel ?? s.groupCode ?? "-",
    },
    {
      header: t("admin.menu.columnName", { defaultValue: "메뉴명" }),
      cell: (s) => <span className="truncate block">{s.screenName}</span>,
    },
    {
      header: t("admin.menu.columnPath", { defaultValue: "경로" }),
      cell: (s) => <span className="truncate block">{s.path}</span>,
    },
    {
      header: t("admin.menu.columnIcon", { defaultValue: "아이콘" }),
      width: 140,
      cell: (s) => {
        const Icon = resolveIcon(s.iconName);
        return (
          <span className="flex items-center gap-1.5 text-muted-foreground">
            <Icon className="size-4" />
            {s.iconName ?? "-"}
          </span>
        );
      },
    },
    {
      header: t("admin.menu.columnSortOrder", { defaultValue: "순서" }),
      width: 80,
      cell: (s) => s.sortOrder,
      className: "text-right",
    },
    {
      header: t("admin.menu.columnRoleCount", { defaultValue: "노출 역할 수" }),
      width: 130,
      cell: (s) =>
        s.roles.length === 0 ? (
          <Badge variant="muted">{t("admin.menu.publicToAll", { defaultValue: "전체 공개" })}</Badge>
        ) : (
          <Badge variant="info">
            {t("admin.menu.roleCount", { count: s.roles.length, defaultValue: `${s.roles.length}개 역할` })}
          </Badge>
        ),
    },
    {
      header: t("admin.menu.columnActions", { defaultValue: "액션" }),
      width: 260,
      cell: (s) => (
        <div className="flex justify-end gap-2">
          <Button size="sm" variant="outline" onClick={() => setRolePanel(s)}>
            {t("admin.menu.roleMapping", { defaultValue: "역할 매핑" })}
          </Button>
          <Button size="sm" variant="outline" onClick={() => openEdit(s)}>
            {t("admin.menu.edit", { defaultValue: "수정" })}
          </Button>
          <Button size="sm" variant="destructive" onClick={() => setDeleting(s)}>
            {t("admin.menu.delete", { defaultValue: "삭제" })}
          </Button>
        </div>
      ),
      className: "text-right",
    },
  ];

  const totalPages = Math.ceil(data.totalElements / PAGE_SIZE);

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-semibold text-foreground">
          {t("admin.menu.title", { defaultValue: "메뉴 관리" })}
        </h1>
        <Button onClick={openCreate}>
          <Plus />
          {t("admin.menu.createButton", { defaultValue: "메뉴 생성" })}
        </Button>
      </div>

      <DataTable
        columns={columns}
        data={data.content}
        rowKey={(s) => s.id}
        loading={loading}
        emptyTitle={t("admin.menu.emptyTitle", { defaultValue: "메뉴가 없습니다" })}
      />

      <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />

      <Modal
        open={formOpen}
        onOpenChange={setFormOpen}
        title={
          editing
            ? t("admin.menu.editModalTitle", { defaultValue: "메뉴 수정" })
            : t("admin.menu.createModalTitle", { defaultValue: "메뉴 생성" })
        }
      >
        <form onSubmit={handleSubmit} className="space-y-4" noValidate>
          {!editing && (
            <>
              <div className="space-y-1.5">
                <Label htmlFor="menu-code">{t("admin.menu.screenCode", { defaultValue: "화면 코드" })}</Label>
                <Input
                  id="menu-code"
                  value={form.screenCode}
                  onChange={(e) => setForm((f) => ({ ...f, screenCode: e.target.value.toUpperCase() }))}
                  placeholder={t("admin.menu.screenCodePlaceholder", { defaultValue: "예: SCR-ADMIN-006" })}
                  required
                  aria-invalid={!!formError}
                />
              </div>
              <div className="space-y-1.5">
                <Label htmlFor="menu-domain">{t("admin.menu.domain", { defaultValue: "도메인" })}</Label>
                <Input
                  id="menu-domain"
                  value={form.domain}
                  onChange={(e) => setForm((f) => ({ ...f, domain: e.target.value }))}
                  placeholder={t("admin.menu.domainPlaceholder", { defaultValue: "예: auth" })}
                  required
                  aria-invalid={!!formError}
                />
              </div>
            </>
          )}
          <div className="space-y-1.5">
            <Label htmlFor="menu-name">{t("admin.menu.columnName", { defaultValue: "메뉴명" })}</Label>
            <Input
              id="menu-name"
              value={form.screenName}
              onChange={(e) => setForm((f) => ({ ...f, screenName: e.target.value }))}
              required
              aria-invalid={!!formError}
            />
          </div>
          <div className="space-y-1.5">
            <Label htmlFor="menu-name-en">{t("admin.menu.screenNameEn", { defaultValue: "메뉴 영문명" })}</Label>
            <Input
              id="menu-name-en"
              value={form.screenNameEn}
              onChange={(e) => setForm((f) => ({ ...f, screenNameEn: e.target.value }))}
              placeholder={t("admin.menu.screenNameEnPlaceholder", { defaultValue: "예: Menu Management" })}
              required
              aria-invalid={!!formError}
            />
          </div>
          <div className="space-y-1.5">
            <Label htmlFor="menu-path">{t("admin.menu.columnPath", { defaultValue: "경로" })}</Label>
            <Input
              id="menu-path"
              value={form.path}
              onChange={(e) => setForm((f) => ({ ...f, path: e.target.value }))}
              placeholder={t("admin.menu.pathPlaceholder", { defaultValue: "예: /admin/menus" })}
              required
              aria-invalid={!!formError}
            />
          </div>
          <div className="space-y-1.5">
            <Label htmlFor="menu-group">{t("admin.menu.columnGroup", { defaultValue: "그룹" })}</Label>
            <select
              id="menu-group"
              value={form.groupOption}
              onChange={(e) => handleGroupOptionChange(e.target.value)}
              className="flex h-9 w-full rounded-md border border-input bg-background px-3 py-1 text-sm shadow-sm focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-1"
            >
              <option value={NO_GROUP}>{t("admin.menu.noGroup", { defaultValue: "그룹 없음" })}</option>
              {existingGroups.map((g) => (
                <option key={g.groupCode} value={g.groupCode}>
                  {g.groupLabel}
                </option>
              ))}
              <option value={NEW_GROUP}>{t("admin.menu.addNewGroup", { defaultValue: "새 그룹 추가..." })}</option>
            </select>
            {form.groupOption === NEW_GROUP && (
              <div className="grid grid-cols-2 gap-2 pt-1">
                <Input
                  value={form.newGroupCode}
                  onChange={(e) => setForm((f) => ({ ...f, newGroupCode: e.target.value }))}
                  placeholder={t("admin.menu.newGroupCodePlaceholder", { defaultValue: "그룹 코드(예: admin)" })}
                  required
                />
                <Input
                  value={form.newGroupLabel}
                  onChange={(e) => setForm((f) => ({ ...f, newGroupLabel: e.target.value }))}
                  placeholder={t("admin.menu.newGroupLabelPlaceholder", { defaultValue: "그룹 라벨(예: 관리자)" })}
                  required
                />
              </div>
            )}
            {form.groupOption !== NO_GROUP && (
              <div className="pt-1">
                <Input
                  value={form.groupLabelEn}
                  onChange={(e) => setForm((f) => ({ ...f, groupLabelEn: e.target.value }))}
                  placeholder={t("admin.menu.groupLabelEnPlaceholder", { defaultValue: "그룹 영문명(예: Admin)" })}
                  required
                />
              </div>
            )}
          </div>
          <div className="grid grid-cols-2 gap-3">
            <div className="space-y-1.5">
              <Label htmlFor="menu-icon">{t("admin.menu.iconLabel", { defaultValue: "아이콘(lucide 이름)" })}</Label>
              <div className="flex items-center gap-2">
                <Input
                  id="menu-icon"
                  value={form.iconName}
                  onChange={(e) => setForm((f) => ({ ...f, iconName: e.target.value }))}
                  placeholder={t("admin.menu.iconPlaceholder", { defaultValue: "예: ListTree" })}
                />
                {(() => {
                  const Icon = resolveIcon(form.iconName || undefined);
                  return <Icon className="size-5 shrink-0 text-muted-foreground" />;
                })()}
              </div>
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="menu-order">{t("admin.menu.columnSortOrder", { defaultValue: "순서" })}</Label>
              <Input
                id="menu-order"
                type="number"
                value={form.sortOrder}
                onChange={(e) => setForm((f) => ({ ...f, sortOrder: e.target.value }))}
              />
            </div>
          </div>
          <div className="flex items-center gap-2">
            <Checkbox
              id="menu-visible"
              checked={form.navVisible}
              onCheckedChange={(v) => setForm((f) => ({ ...f, navVisible: v === true }))}
            />
            <Label htmlFor="menu-visible" className="font-normal">
              {t("admin.menu.navVisible", { defaultValue: "사이드바에 노출" })}
            </Label>
          </div>
          {formError ? (
            <p role="alert" className="text-sm text-danger">
              {formError}
            </p>
          ) : null}
          <div className="flex justify-end gap-2">
            <Button type="button" variant="outline" onClick={() => setFormOpen(false)}>
              {t("admin.menu.cancel", { defaultValue: "취소" })}
            </Button>
            <Button type="submit" loading={saving}>
              {t("admin.menu.save", { defaultValue: "저장" })}
            </Button>
          </div>
        </form>
      </Modal>

      <ConfirmDialog
        open={!!deleting}
        onOpenChange={(open) => !open && setDeleting(null)}
        title={t("admin.menu.deleteDialogTitle", { defaultValue: "메뉴 삭제" })}
        description={t("admin.menu.deleteDialogDescription", {
          name: deleting?.screenName ?? "",
          defaultValue: `"${deleting?.screenName ?? ""}" 메뉴를 삭제하시겠습니까? 사이드바에서 즉시 사라집니다.`,
        })}
        confirmLabel={t("admin.menu.delete", { defaultValue: "삭제" })}
        loading={deleteBusy}
        onConfirm={handleDelete}
      />

      <RoleMappingPanel
        t={t}
        screen={rolePanel}
        roles={roles}
        busyRoleId={roleBusyId}
        onOpenChange={(open) => !open && setRolePanel(null)}
        onToggleRole={handleToggleRole}
      />
    </div>
  );
}

interface RoleMappingPanelProps {
  t: TFunction;
  screen: Screen | null;
  roles: Role[];
  busyRoleId: number | null;
  onOpenChange: (open: boolean) => void;
  onToggleRole: (role: Role, checked: boolean) => void;
}

/** 역할 매핑 우측 슬라이드 패널 — 기존 Dialog(Radix)의 토큰(Overlay·Close)을 재사용해 우측 슬라이드 스타일만 직접 구현. */
function RoleMappingPanel({
  t,
  screen,
  roles,
  busyRoleId,
  onOpenChange,
  onToggleRole,
}: RoleMappingPanelProps) {
  return (
    <Dialog open={!!screen} onOpenChange={onOpenChange}>
      <DialogPortal>
        <DialogOverlay />
        <DialogPrimitive.Content
          className={cn(
            "fixed inset-y-0 right-0 z-50 flex h-full w-full max-w-sm flex-col gap-4 border-l border-border bg-popover p-6 shadow-overlay",
            "data-[state=open]:animate-in data-[state=closed]:animate-out",
            "data-[state=open]:slide-in-from-right data-[state=closed]:slide-out-to-right",
            "data-[state=open]:duration-[250ms] data-[state=open]:ease-[var(--motion-modal-enter-easing)]",
            "data-[state=closed]:duration-[200ms] data-[state=closed]:ease-[var(--motion-modal-exit-easing)]",
          )}
        >
          <DialogTitle>
            {screen
              ? t("admin.menu.roleMappingTitleWithScreen", {
                  name: screen.screenName,
                  defaultValue: `역할 매핑 — ${screen.screenName}`,
                })
              : t("admin.menu.roleMapping", { defaultValue: "역할 매핑" })}
          </DialogTitle>
          <p className="text-sm text-muted-foreground">
            {t("admin.menu.roleMappingHint", {
              defaultValue:
                "체크된 역할만 사이드바에서 이 메뉴를 볼 수 있습니다. 아무 역할도 체크하지 않으면 전체 인증 사용자에게 공개됩니다.",
            })}
          </p>
          <div className="flex-1 space-y-2 overflow-y-auto">
            {roles.map((role) => {
              const checked = !!screen?.roles.includes(role.roleCode);
              return (
                <label
                  key={role.id}
                  className="flex items-center gap-2 rounded-md px-2 py-1.5 hover:bg-accent"
                >
                  <Checkbox
                    checked={checked}
                    disabled={busyRoleId === role.id}
                    onCheckedChange={(v) => onToggleRole(role, v === true)}
                  />
                  <span className="text-sm">{role.name}</span>
                </label>
              );
            })}
          </div>
          <DialogClose className="absolute right-4 top-4 rounded-sm opacity-70 ring-offset-background transition-opacity hover:opacity-100 focus:outline-none focus:ring-2 focus:ring-ring focus:ring-offset-2">
            <X className="size-4" />
            <span className="sr-only">{t("admin.menu.close", { defaultValue: "닫기" })}</span>
          </DialogClose>
        </DialogPrimitive.Content>
      </DialogPortal>
    </Dialog>
  );
}
