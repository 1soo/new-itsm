import { type FormEvent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { Plus } from "lucide-react";

import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  type Column,
  DataTable,
  Modal,
  toast,
} from "@/components/common";
import { adminApi } from "@/features/admin/api";
import type { Role } from "@/features/admin/types";
import { extractErrorMessage, getStatusCode } from "@/lib/apiClient";

/*
 * 역할 관리(SCR-ADMIN-004) — 역할 목록(역할명·설명·부여 사용자 수) 조회 및 역할 생성 모달.
 * 역할명 중복(409) 시 인라인 오류.
 */
export function RoleManagementPage() {
  const { t } = useTranslation("auth");
  const [roles, setRoles] = useState<Role[]>([]);
  const [loading, setLoading] = useState(true);

  const [open, setOpen] = useState(false);
  const [roleCode, setRoleCode] = useState("");
  const [name, setName] = useState("");
  const [description, setDescription] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  const loadRoles = () => {
    setLoading(true);
    adminApi
      .listRoles()
      .then(setRoles)
      .catch((err) => toast.error(extractErrorMessage(err)))
      .finally(() => setLoading(false));
  };

  useEffect(loadRoles, []);

  const openCreate = () => {
    setRoleCode("");
    setName("");
    setDescription("");
    setError(null);
    setOpen(true);
  };

  const handleCreate = async (e: FormEvent) => {
    e.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      await adminApi.createRole({ roleCode, name, description });
      toast.success(t("admin.role.createSuccess", { defaultValue: "역할이 생성되었습니다" }));
      setOpen(false);
      loadRoles();
    } catch (err) {
      if (getStatusCode(err) === 409) {
        setError(t("admin.role.duplicateError", { defaultValue: "이미 존재하는 역할 코드 또는 역할명입니다." }));
      } else {
        setError(extractErrorMessage(err, t("admin.role.createFailed", { defaultValue: "역할 생성에 실패했습니다." })));
      }
    } finally {
      setSubmitting(false);
    }
  };

  const columns: Column<Role>[] = [
    { header: t("admin.role.columnCode", { defaultValue: "역할 코드" }), cell: (r) => r.roleCode },
    { header: t("admin.role.columnName", { defaultValue: "역할명" }), cell: (r) => r.name },
    { header: t("admin.role.columnDescription", { defaultValue: "설명" }), cell: (r) => r.description },
    {
      header: t("admin.role.columnUserCount", { defaultValue: "부여 사용자 수" }),
      cell: (r) => r.userCount ?? 0,
      className: "text-right",
    },
  ];

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-semibold text-foreground">
          {t("admin.role.title", { defaultValue: "역할 관리" })}
        </h1>
        <Button onClick={openCreate}>
          <Plus />
          {t("admin.role.createButton", { defaultValue: "역할 생성" })}
        </Button>
      </div>

      <DataTable
        columns={columns}
        data={roles}
        rowKey={(r) => r.id}
        loading={loading}
        emptyTitle={t("admin.role.emptyTitle", { defaultValue: "역할이 없습니다" })}
      />

      <Modal open={open} onOpenChange={setOpen} title={t("admin.role.createModalTitle", { defaultValue: "역할 생성" })}>
        <form onSubmit={handleCreate} className="space-y-4" noValidate>
          <div className="space-y-1.5">
            <Label htmlFor="role-code">{t("admin.role.columnCode", { defaultValue: "역할 코드" })}</Label>
            <Input
              id="role-code"
              value={roleCode}
              onChange={(e) => setRoleCode(e.target.value.toUpperCase())}
              placeholder={t("admin.role.codePlaceholder", { defaultValue: "예: INCIDENT_MANAGER" })}
              required
              aria-invalid={!!error}
            />
            <p className="text-xs text-muted-foreground">
              {t("admin.role.codeHint", { defaultValue: "대문자·언더스코어(스네이크) 형식." })}
            </p>
          </div>
          <div className="space-y-1.5">
            <Label htmlFor="role-name">{t("admin.role.columnName", { defaultValue: "역할명" })}</Label>
            <Input
              id="role-name"
              value={name}
              onChange={(e) => setName(e.target.value)}
              required
              aria-invalid={!!error}
            />
          </div>
          <div className="space-y-1.5">
            <Label htmlFor="role-desc">{t("admin.role.columnDescription", { defaultValue: "설명" })}</Label>
            <Input
              id="role-desc"
              value={description}
              onChange={(e) => setDescription(e.target.value)}
            />
          </div>
          {error ? (
            <p role="alert" className="text-sm text-danger">
              {error}
            </p>
          ) : null}
          <div className="flex justify-end gap-2">
            <Button type="button" variant="outline" onClick={() => setOpen(false)}>
              {t("admin.role.cancel", { defaultValue: "취소" })}
            </Button>
            <Button type="submit" loading={submitting}>
              {t("admin.role.create", { defaultValue: "생성" })}
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  );
}
