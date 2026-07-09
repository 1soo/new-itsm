import { type FormEvent, useEffect, useMemo, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { X } from "lucide-react";

import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { ConfirmDialog, StatusBadge, toast } from "@/components/common";
import { FullscreenLoader } from "@/routes/FullscreenLoader";
import { adminApi } from "@/features/admin/api";
import type { Role, UserDetail } from "@/features/admin/types";
import { extractErrorMessage } from "@/lib/apiClient";

/*
 * 계정 상세·수정(SCR-ADMIN-003) — 정보 수정, 역할 부여/회수(즉시 반영), 비활성화(확인 다이얼로그).
 * 역할 회수 시 역할명 → roleId 매핑이 필요하므로 역할 목록을 함께 로드한다.
 */
export function UserDetailPage() {
  const navigate = useNavigate();
  const params = useParams();
  const userId = Number(params.id);

  const [detail, setDetail] = useState<UserDetail | null>(null);
  const [roles, setRoles] = useState<Role[]>([]);
  const [loading, setLoading] = useState(true);
  const [notFound, setNotFound] = useState(false);

  const [name, setName] = useState("");
  const [savingName, setSavingName] = useState(false);
  const [roleBusy, setRoleBusy] = useState(false);
  const [statusOpen, setStatusOpen] = useState(false);
  const [statusBusy, setStatusBusy] = useState(false);

  useEffect(() => {
    let active = true;
    setLoading(true);
    Promise.all([adminApi.getUser(userId), adminApi.listRoles()])
      .then(([u, r]) => {
        if (!active) return;
        setDetail(u);
        setName(u.name);
        setRoles(r);
      })
      .catch((err) => {
        if (!active) return;
        toast.error(extractErrorMessage(err));
        setNotFound(true);
      })
      .finally(() => {
        if (active) setLoading(false);
      });
    return () => {
      active = false;
    };
  }, [userId]);

  // user.roles는 역할 코드 배열이므로 코드로 매핑한다.
  const roleIdByCode = useMemo(() => {
    const map = new Map<string, number>();
    roles.forEach((r) => map.set(r.roleCode, r.id));
    return map;
  }, [roles]);

  const nameByCode = useMemo(() => {
    const map = new Map<string, string>();
    roles.forEach((r) => map.set(r.roleCode, r.name));
    return map;
  }, [roles]);

  const unassignedRoles = useMemo(
    () => roles.filter((r) => !detail?.roles.includes(r.roleCode)),
    [roles, detail],
  );

  const handleSaveName = async (e: FormEvent) => {
    e.preventDefault();
    if (!detail) return;
    setSavingName(true);
    try {
      const updated = await adminApi.updateUser(detail.id, { name });
      setDetail(updated);
      toast.success("계정 정보가 수정되었습니다");
    } catch (err) {
      toast.error(extractErrorMessage(err));
    } finally {
      setSavingName(false);
    }
  };

  const handleAssignRole = async (roleIdStr: string) => {
    if (!detail) return;
    setRoleBusy(true);
    try {
      const res = await adminApi.assignRole(detail.id, Number(roleIdStr));
      setDetail({ ...detail, roles: res.roles });
      toast.success("역할이 부여되었습니다");
    } catch (err) {
      toast.error(extractErrorMessage(err));
    } finally {
      setRoleBusy(false);
    }
  };

  const handleRevokeRole = async (roleCode: string) => {
    if (!detail) return;
    const roleId = roleIdByCode.get(roleCode);
    if (roleId === undefined) return;
    setRoleBusy(true);
    try {
      const res = await adminApi.revokeRole(detail.id, roleId);
      setDetail({ ...detail, roles: res.roles });
      toast.success("역할이 회수되었습니다");
    } catch (err) {
      toast.error(extractErrorMessage(err));
    } finally {
      setRoleBusy(false);
    }
  };

  const handleToggleStatus = async () => {
    if (!detail) return;
    const next = detail.status === "ACTIVE" ? "INACTIVE" : "ACTIVE";
    setStatusBusy(true);
    try {
      const res = await adminApi.setUserStatus(detail.id, next);
      setDetail({ ...detail, status: res.status });
      toast.success(next === "INACTIVE" ? "계정이 비활성화되었습니다" : "계정이 활성화되었습니다");
    } catch (err) {
      toast.error(extractErrorMessage(err));
    } finally {
      setStatusBusy(false);
      setStatusOpen(false);
    }
  };

  if (loading) return <FullscreenLoader />;

  if (notFound || !detail) {
    return (
      <div className="mx-auto max-w-lg space-y-4 text-center">
        <p className="text-sm text-muted-foreground">계정을 찾을 수 없습니다.</p>
        <Button onClick={() => navigate("/admin/users")}>목록으로</Button>
      </div>
    );
  }

  const isActive = detail.status === "ACTIVE";

  return (
    <div className="mx-auto max-w-2xl space-y-4">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-semibold text-foreground">계정 상세</h1>
        <Button variant="outline" onClick={() => navigate("/admin/users")}>
          목록으로
        </Button>
      </div>

      {/* 계정 요약 */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            {detail.email}
            <StatusBadge
              tone={isActive ? "success" : "warning"}
              label={isActive ? "활성" : "비활성"}
            />
          </CardTitle>
        </CardHeader>
        <CardContent className="text-sm text-muted-foreground">
          계정 ID {detail.id}
        </CardContent>
      </Card>

      {/* 정보 수정 */}
      <Card>
        <CardHeader>
          <CardTitle>정보 수정</CardTitle>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSaveName} className="space-y-4">
            <div className="space-y-1.5">
              <Label htmlFor="name">이름</Label>
              <Input id="name" value={name} onChange={(e) => setName(e.target.value)} required />
            </div>
            <div className="flex justify-end">
              <Button type="submit" loading={savingName} disabled={name === detail.name}>
                저장
              </Button>
            </div>
          </form>
        </CardContent>
      </Card>

      {/* 역할 부여/회수 */}
      <Card>
        <CardHeader>
          <CardTitle>역할 관리</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="flex flex-wrap gap-2">
            {detail.roles.length === 0 ? (
              <span className="text-sm text-muted-foreground">부여된 역할 없음</span>
            ) : (
              detail.roles.map((role) => (
                <Badge key={role} variant="info" className="gap-1 pr-1">
                  {nameByCode.get(role) ?? role}
                  <button
                    type="button"
                    onClick={() => handleRevokeRole(role)}
                    disabled={roleBusy}
                    aria-label={`${nameByCode.get(role) ?? role} 역할 회수`}
                    className="rounded-full p-0.5 hover:bg-danger hover:text-danger-foreground disabled:opacity-50"
                  >
                    <X className="size-3" />
                  </button>
                </Badge>
              ))
            )}
          </div>

          <div className="max-w-xs space-y-1.5">
            <Label>역할 부여</Label>
            <Select value="" onValueChange={handleAssignRole} disabled={roleBusy || unassignedRoles.length === 0}>
              <SelectTrigger>
                <SelectValue placeholder={unassignedRoles.length === 0 ? "부여할 역할 없음" : "역할 선택"} />
              </SelectTrigger>
              <SelectContent>
                {unassignedRoles.map((r) => (
                  <SelectItem key={r.id} value={String(r.id)}>
                    {r.name}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
        </CardContent>
      </Card>

      {/* 상태 변경 */}
      <Card>
        <CardHeader>
          <CardTitle>계정 상태</CardTitle>
        </CardHeader>
        <CardContent className="flex items-center justify-between">
          <p className="text-sm text-muted-foreground">
            {isActive
              ? "비활성화하면 해당 계정의 로그인이 차단됩니다."
              : "활성화하면 해당 계정이 다시 로그인할 수 있습니다."}
          </p>
          {isActive ? (
            <Button variant="destructive" onClick={() => setStatusOpen(true)}>
              비활성화
            </Button>
          ) : (
            <Button onClick={handleToggleStatus} loading={statusBusy}>
              활성화
            </Button>
          )}
        </CardContent>
      </Card>

      <ConfirmDialog
        open={statusOpen}
        onOpenChange={setStatusOpen}
        title="계정 비활성화"
        description="이 계정을 비활성화하시겠습니까? 비활성화된 계정은 로그인할 수 없습니다."
        confirmLabel="비활성화"
        loading={statusBusy}
        onConfirm={handleToggleStatus}
      />
    </div>
  );
}
