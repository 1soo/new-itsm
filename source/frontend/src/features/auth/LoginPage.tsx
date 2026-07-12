import { type FormEvent, useState } from "react";
import { Navigate, useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { Eye, EyeOff } from "lucide-react";

import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import type { LoginError } from "@/store/authSlice";
import { login } from "@/store/authSlice";
import { roleHome } from "@/features/auth/roles";
import { useAppDispatch, useAppSelector } from "@/store/hooks";

/*
 * 로그인(SCR-AUTH-001) — 이메일·비밀번호 인증 후 역할 기본 홈으로 이동.
 * 계정 열거 방지를 위해 401(불일치)·403(비활성)은 동일 메시지로 통일 표기한다.
 */

// POC 데모용 안내 — 역할별 테스트 계정. 실서비스 전환 시 이 표는 제거한다.
const TEST_ACCOUNTS = [
  { email: "admin@itsm.local", roleKey: "admin" },
  { email: "user@itsm.local", roleKey: "user" },
  { email: "agent@itsm.local", roleKey: "agent" },
  { email: "cab@itsm.local", roleKey: "cab" },
  { email: "im@itsm.local", roleKey: "im" },
  { email: "pm@itsm.local", roleKey: "pm" },
  { email: "cm@itsm.local", roleKey: "cm" },
  { email: "kc@itsm.local", roleKey: "kc" },
  { email: "kg@itsm.local", roleKey: "kg" },
  { email: "am@itsm.local", roleKey: "am" },
  { email: "po@itsm.local", roleKey: "po" },
  { email: "hr@itsm.local", roleKey: "hr" },
  { email: "legal-coord@itsm.local", roleKey: "legalCoord" },
  { email: "facilities-coord@itsm.local", roleKey: "facilitiesCoord" },
  { email: "it-coord@itsm.local", roleKey: "itCoord" },
  { email: "vm@itsm.local", roleKey: "vm" },
  { email: "co@itsm.local", roleKey: "co" },
  { email: "io@itsm.local", roleKey: "io" },
] as const;
const TEST_ACCOUNT_PASSWORD = "Admin@1234";

export function LoginPage() {
  const { t } = useTranslation("auth");
  const dispatch = useAppDispatch();
  const navigate = useNavigate();
  const status = useAppSelector((s) => s.auth.status);
  const user = useAppSelector((s) => s.auth.user);

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  // 이미 인증된 상태로 로그인 화면 접근 시 역할 기본 홈으로 이동.
  if (status === "authenticated" && user) {
    return <Navigate to={roleHome(user.roles)} replace />;
  }

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      const loggedIn = await dispatch(login({ email, password })).unwrap();
      navigate(roleHome(loggedIn.roles), { replace: true });
    } catch (rejected) {
      const err = rejected as LoginError;
      if (err.status === 401 || err.status === 403) {
        setError(t("login.authFailMessage", { defaultValue: "이메일 또는 비밀번호가 올바르지 않습니다." }));
      } else {
        setError(err.message ?? t("login.genericError", { defaultValue: "로그인 중 오류가 발생했습니다." }));
      }
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="flex min-h-screen flex-col items-center justify-center gap-6 bg-background p-4">
      <Card className="w-full max-w-sm">
        <CardHeader className="items-center text-center">
          <span className="mb-2 flex size-10 items-center justify-center rounded-md bg-primary text-base font-bold text-primary-foreground">
            IT
          </span>
          <CardTitle>{t("login.title", { defaultValue: "ITSM 플랫폼 로그인" })}</CardTitle>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit} className="space-y-4" noValidate>
            <div className="space-y-1.5">
              <Label htmlFor="email">{t("login.email", { defaultValue: "이메일" })}</Label>
              <Input
                id="email"
                type="email"
                autoComplete="username"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
                aria-invalid={!!error}
              />
            </div>

            <div className="space-y-1.5">
              <Label htmlFor="password">{t("login.password", { defaultValue: "비밀번호" })}</Label>
              <div className="relative">
                <Input
                  id="password"
                  type={showPassword ? "text" : "password"}
                  autoComplete="current-password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  required
                  aria-invalid={!!error}
                  className="pr-10"
                />
                <button
                  type="button"
                  onClick={() => setShowPassword((v) => !v)}
                  className="absolute right-2 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground"
                  aria-label={
                    showPassword
                      ? t("login.hidePasswordAria", { defaultValue: "비밀번호 숨기기" })
                      : t("login.showPasswordAria", { defaultValue: "비밀번호 표시" })
                  }
                >
                  {showPassword ? <EyeOff className="size-4" /> : <Eye className="size-4" />}
                </button>
              </div>
            </div>

            {error ? (
              <p role="alert" className="text-sm text-danger">
                {error}
              </p>
            ) : null}

            <Button type="submit" className="w-full" loading={submitting}>
              {t("login.submit", { defaultValue: "로그인" })}
            </Button>
          </form>
        </CardContent>
      </Card>

      <Card className="w-full max-w-2xl">
        <CardHeader>
          <CardTitle className="text-sm text-muted-foreground">
            {t("login.testAccounts.title", {
              defaultValue: "POC 테스트 계정 (역할별 데모용 — 실서비스 전환 시 제거)",
            })}
          </CardTitle>
        </CardHeader>
        <CardContent>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>{t("login.testAccounts.columnEmail", { defaultValue: "이메일" })}</TableHead>
                <TableHead>{t("login.testAccounts.columnPassword", { defaultValue: "비밀번호" })}</TableHead>
                <TableHead>{t("login.testAccounts.columnRole", { defaultValue: "역할" })}</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {TEST_ACCOUNTS.map((account) => (
                <TableRow key={account.email}>
                  <TableCell className="font-mono text-xs">{account.email}</TableCell>
                  <TableCell className="font-mono text-xs">{TEST_ACCOUNT_PASSWORD}</TableCell>
                  <TableCell>{t(`login.testAccounts.roleLabel.${account.roleKey}`)}</TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </CardContent>
      </Card>
    </div>
  );
}
