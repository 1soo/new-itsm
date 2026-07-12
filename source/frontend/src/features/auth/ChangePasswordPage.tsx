import { type FormEvent, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";

import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { toast } from "@/components/common";
import { authApi } from "@/features/auth/api";
import { MIN_PASSWORD_LENGTH, validatePasswordPolicy } from "@/features/auth/password";
import { extractErrorMessage, getStatusCode } from "@/lib/apiClient";

/*
 * 비밀번호 변경(SCR-AUTH-003) — 현재 비밀번호 검증 후 새 비밀번호로 변경.
 * 새 비밀번호 정책(길이·영문/숫자) 미충족·확인 불일치는 인라인 오류, 현재 비밀번호 불일치(401)는 Danger 메시지.
 */

export function ChangePasswordPage() {
  const { t } = useTranslation("auth");
  const navigate = useNavigate();
  const [currentPassword, setCurrentPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError(null);

    const policyError = validatePasswordPolicy(t, newPassword);
    if (policyError) {
      setError(policyError);
      return;
    }
    if (newPassword !== confirmPassword) {
      setError(t("changePassword.mismatchError", { defaultValue: "새 비밀번호와 확인이 일치하지 않습니다." }));
      return;
    }

    setSubmitting(true);
    try {
      await authApi.changePassword({ currentPassword, newPassword });
      toast.success(t("changePassword.success", { defaultValue: "비밀번호가 변경되었습니다" }));
      navigate("/profile");
    } catch (err) {
      const status = getStatusCode(err);
      if (status === 401) {
        setError(t("changePassword.currentPasswordInvalid", { defaultValue: "현재 비밀번호가 올바르지 않습니다." }));
      } else {
        setError(
          extractErrorMessage(
            err,
            t("changePassword.failed", { defaultValue: "비밀번호 변경에 실패했습니다." }),
          ),
        );
      }
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="mx-auto max-w-md space-y-4">
      <h1 className="text-xl font-semibold text-foreground">
        {t("changePassword.title", { defaultValue: "비밀번호 변경" })}
      </h1>
      <Card>
        <CardHeader>
          <CardTitle>{t("changePassword.title", { defaultValue: "비밀번호 변경" })}</CardTitle>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit} className="space-y-4" noValidate>
            <div className="space-y-1.5">
              <Label htmlFor="current">
                {t("changePassword.currentPassword", { defaultValue: "현재 비밀번호" })}
              </Label>
              <Input
                id="current"
                type="password"
                autoComplete="current-password"
                value={currentPassword}
                onChange={(e) => setCurrentPassword(e.target.value)}
                required
              />
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="new">{t("changePassword.newPassword", { defaultValue: "새 비밀번호" })}</Label>
              <Input
                id="new"
                type="password"
                autoComplete="new-password"
                value={newPassword}
                onChange={(e) => setNewPassword(e.target.value)}
                required
              />
              <p className="text-xs text-muted-foreground">
                {t("changePassword.policyHint", {
                  minLength: MIN_PASSWORD_LENGTH,
                  defaultValue: `최소 ${MIN_PASSWORD_LENGTH}자 이상, 영문과 숫자를 포함하세요.`,
                })}
              </p>
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="confirm">
                {t("changePassword.confirmPassword", { defaultValue: "새 비밀번호 확인" })}
              </Label>
              <Input
                id="confirm"
                type="password"
                autoComplete="new-password"
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                required
              />
            </div>

            {error ? (
              <p role="alert" className="text-sm text-danger">
                {error}
              </p>
            ) : null}

            <div className="flex justify-end gap-2">
              <Button type="button" variant="outline" onClick={() => navigate("/profile")}>
                {t("changePassword.cancel", { defaultValue: "취소" })}
              </Button>
              <Button type="submit" loading={submitting}>
                {t("changePassword.save", { defaultValue: "저장" })}
              </Button>
            </div>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}
