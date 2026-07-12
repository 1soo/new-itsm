import { type FormEvent, useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { Eye, EyeOff } from "lucide-react";

import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { MultiSelect, type MultiSelectOption, toast } from "@/components/common";
import { adminApi } from "@/features/admin/api";
import type { Role } from "@/features/admin/types";
import { MIN_PASSWORD_LENGTH, validatePasswordPolicy } from "@/features/auth/password";
import { extractErrorMessage, getStatusCode } from "@/lib/apiClient";

/*
 * 계정 생성(SCR-ADMIN-002) — 이메일·이름·초기 역할(1개 이상)로 신규 계정 생성.
 * 이메일 중복(409) 시 인라인 오류. 성공 시 토스트 후 목록 복귀.
 */
export function UserCreatePage() {
  const { t } = useTranslation("auth");
  const navigate = useNavigate();
  const [email, setEmail] = useState("");
  const [name, setName] = useState("");
  const [initialPassword, setInitialPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [roleIds, setRoleIds] = useState<string[]>([]);
  const [roles, setRoles] = useState<Role[]>([]);
  const [emailError, setEmailError] = useState<string | null>(null);
  const [formError, setFormError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    adminApi
      .listRoles()
      .then(setRoles)
      .catch((err) => toast.error(extractErrorMessage(err)));
  }, []);

  const roleOptions: MultiSelectOption[] = roles.map((r) => ({
    value: String(r.id),
    label: r.name,
  }));

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setEmailError(null);
    setFormError(null);

    const policyError = validatePasswordPolicy(t, initialPassword);
    if (policyError) {
      setFormError(policyError);
      return;
    }
    if (roleIds.length === 0) {
      setFormError(t("admin.userCreate.roleRequiredError", { defaultValue: "초기 역할을 하나 이상 선택하세요." }));
      return;
    }

    setSubmitting(true);
    try {
      await adminApi.createUser({
        email,
        name,
        initialPassword,
        roleIds: roleIds.map(Number),
      });
      toast.success(t("admin.userCreate.success", { defaultValue: "계정이 생성되었습니다" }));
      navigate("/admin/users");
    } catch (err) {
      if (getStatusCode(err) === 409) {
        setEmailError(t("admin.userCreate.emailDuplicateError", { defaultValue: "이미 사용 중인 이메일입니다." }));
      } else {
        setFormError(
          extractErrorMessage(err, t("admin.userCreate.failed", { defaultValue: "계정 생성에 실패했습니다." })),
        );
      }
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="mx-auto max-w-lg space-y-4">
      <h1 className="text-xl font-semibold text-foreground">
        {t("admin.userCreate.title", { defaultValue: "계정 생성" })}
      </h1>
      <Card>
        <CardHeader>
          <CardTitle>{t("admin.userCreate.cardTitle", { defaultValue: "신규 계정" })}</CardTitle>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit} className="space-y-4" noValidate>
            <div className="space-y-1.5">
              <Label htmlFor="email">{t("admin.userCreate.email", { defaultValue: "이메일" })}</Label>
              <Input
                id="email"
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
                aria-invalid={!!emailError}
              />
              {emailError ? (
                <p role="alert" className="text-sm text-danger">
                  {emailError}
                </p>
              ) : null}
            </div>

            <div className="space-y-1.5">
              <Label htmlFor="name">{t("admin.userCreate.name", { defaultValue: "이름" })}</Label>
              <Input
                id="name"
                value={name}
                onChange={(e) => setName(e.target.value)}
                required
              />
            </div>

            <div className="space-y-1.5">
              <Label htmlFor="initial-password">
                {t("admin.userCreate.initialPassword", { defaultValue: "초기 비밀번호" })}
              </Label>
              <div className="relative">
                <Input
                  id="initial-password"
                  type={showPassword ? "text" : "password"}
                  autoComplete="new-password"
                  value={initialPassword}
                  onChange={(e) => setInitialPassword(e.target.value)}
                  required
                  className="pr-10"
                />
                <button
                  type="button"
                  onClick={() => setShowPassword((v) => !v)}
                  className="absolute right-2 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground"
                  aria-label={
                    showPassword
                      ? t("admin.userCreate.hidePasswordAria", { defaultValue: "비밀번호 숨기기" })
                      : t("admin.userCreate.showPasswordAria", { defaultValue: "비밀번호 표시" })
                  }
                >
                  {showPassword ? <EyeOff className="size-4" /> : <Eye className="size-4" />}
                </button>
              </div>
              <p className="text-xs text-muted-foreground">
                {t("admin.userCreate.passwordHint", {
                  minLength: MIN_PASSWORD_LENGTH,
                  defaultValue: `최소 ${MIN_PASSWORD_LENGTH}자 이상, 영문·숫자 포함. 사용자에게 안전하게 전달하세요.`,
                })}
              </p>
            </div>

            <div className="space-y-1.5">
              <Label>{t("admin.userCreate.initialRoles", { defaultValue: "초기 역할" })}</Label>
              <MultiSelect
                options={roleOptions}
                value={roleIds}
                onChange={setRoleIds}
                placeholder={t("admin.userCreate.rolesPlaceholder", { defaultValue: "역할 선택 (1개 이상)" })}
              />
            </div>

            {formError ? (
              <p role="alert" className="text-sm text-danger">
                {formError}
              </p>
            ) : null}

            <div className="flex justify-end gap-2">
              <Button type="button" variant="outline" onClick={() => navigate("/admin/users")}>
                {t("admin.userCreate.cancel", { defaultValue: "취소" })}
              </Button>
              <Button type="submit" loading={submitting}>
                {t("admin.userCreate.save", { defaultValue: "저장" })}
              </Button>
            </div>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}
