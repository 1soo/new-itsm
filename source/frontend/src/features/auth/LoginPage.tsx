import { type FormEvent, useState } from "react";
import { Navigate, useNavigate } from "react-router-dom";
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
const AUTH_FAIL_MESSAGE = "이메일 또는 비밀번호가 올바르지 않습니다.";

// POC 데모용 안내 — 역할별 테스트 계정. 실서비스 전환 시 이 표는 제거한다.
const TEST_ACCOUNTS = [
  { email: "admin@itsm.local", role: "시스템 관리자 (SYSTEM_ADMIN)" },
  { email: "user@itsm.local", role: "최종 사용자 (END_USER)" },
  { email: "agent@itsm.local", role: "서비스 데스크 상담원 (SERVICE_DESK_AGENT)" },
  { email: "cab@itsm.local", role: "승인자 (APPROVER)" },
  { email: "im@itsm.local", role: "인시던트 관리자 (INCIDENT_MANAGER)" },
  { email: "pm@itsm.local", role: "문제 관리자 (PROBLEM_MANAGER)" },
  { email: "cm@itsm.local", role: "변경 관리자 (CHANGE_MANAGER)" },
  { email: "kc@itsm.local", role: "지식 기여자 (KNOWLEDGE_CONTRIBUTOR)" },
  { email: "kg@itsm.local", role: "지식 게이트키퍼 (KNOWLEDGE_GATEKEEPER)" },
  { email: "am@itsm.local", role: "자산 관리자 (ASSET_MANAGER)" },
  { email: "po@itsm.local", role: "프로세스 오너 (PROCESS_OWNER)" },
  { email: "hr@itsm.local", role: "HR 케이스 담당자 (HR_CASE_MANAGER)" },
  { email: "legal-coord@itsm.local", role: "법무 처리 담당자 (DEPT_COORDINATOR)" },
  { email: "facilities-coord@itsm.local", role: "시설 처리 담당자 (DEPT_COORDINATOR)" },
  { email: "it-coord@itsm.local", role: "IT 처리 담당자 (DEPT_COORDINATOR)" },
] as const;
const TEST_ACCOUNT_PASSWORD = "Admin@1234";

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
    <div className="flex min-h-screen flex-col items-center justify-center gap-6 bg-background p-4">
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

      <Card className="w-full max-w-2xl">
        <CardHeader>
          <CardTitle className="text-sm text-muted-foreground">
            POC 테스트 계정 (역할별 데모용 — 실서비스 전환 시 제거)
          </CardTitle>
        </CardHeader>
        <CardContent>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>이메일</TableHead>
                <TableHead>비밀번호</TableHead>
                <TableHead>역할</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {TEST_ACCOUNTS.map((account) => (
                <TableRow key={account.email}>
                  <TableCell className="font-mono text-xs">{account.email}</TableCell>
                  <TableCell className="font-mono text-xs">{TEST_ACCOUNT_PASSWORD}</TableCell>
                  <TableCell>{account.role}</TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </CardContent>
      </Card>
    </div>
  );
}
