import { type FormEvent, useState } from "react";
import { Navigate, useNavigate } from "react-router-dom";
import { Eye, EyeOff } from "lucide-react";

import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import type { LoginError } from "@/store/authSlice";
import { login } from "@/store/authSlice";
import { roleHome } from "@/features/auth/roles";
import { useAppDispatch, useAppSelector } from "@/store/hooks";

/*
 * 로그인(SCR-AUTH-001) — 이메일·비밀번호 인증 후 역할 기본 홈으로 이동.
 * 계정 열거 방지를 위해 401(불일치)·403(비활성)은 동일 메시지로 통일 표기한다.
 */
const AUTH_FAIL_MESSAGE = "이메일 또는 비밀번호가 올바르지 않습니다.";

export function LoginPage() {
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
        setError(AUTH_FAIL_MESSAGE);
      } else {
        setError(err.message ?? "로그인 중 오류가 발생했습니다.");
      }
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="flex min-h-screen items-center justify-center bg-background p-4">
      <Card className="w-full max-w-sm">
        <CardHeader className="items-center text-center">
          <span className="mb-2 flex size-10 items-center justify-center rounded-md bg-primary text-base font-bold text-primary-foreground">
            IT
          </span>
          <CardTitle>ITSM 플랫폼 로그인</CardTitle>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit} className="space-y-4" noValidate>
            <div className="space-y-1.5">
              <Label htmlFor="email">이메일</Label>
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
              <Label htmlFor="password">비밀번호</Label>
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
                  aria-label={showPassword ? "비밀번호 숨기기" : "비밀번호 표시"}
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
              로그인
            </Button>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}
