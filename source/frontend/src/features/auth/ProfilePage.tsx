import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";

import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { StatusBadge, toast } from "@/components/common";
import { authApi } from "@/features/auth/api";
import type { MeResponse } from "@/features/auth/types";
import { extractErrorMessage } from "@/lib/apiClient";

/*
 * 내 프로필(SCR-AUTH-002) — 진입 시 본인 정보(GET /auth/me)를 로드하여
 * 이름·이메일·상태·부여 역할을 읽기 전용으로 표시한다.
 */
export function ProfilePage() {
  const { t } = useTranslation("auth");
  const navigate = useNavigate();
  const [me, setMe] = useState<MeResponse | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let active = true;
    authApi
      .me()
      .then((data) => {
        if (active) setMe(data);
      })
      .catch((error) => {
        toast.error(extractErrorMessage(error));
      })
      .finally(() => {
        if (active) setLoading(false);
      });
    return () => {
      active = false;
    };
  }, []);

  return (
    <div className="mx-auto max-w-2xl space-y-4">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-semibold text-foreground">
          {t("profile.title", { defaultValue: "내 프로필" })}
        </h1>
        <Button onClick={() => navigate("/profile/password")}>
          {t("profile.changePasswordButton", { defaultValue: "비밀번호 변경" })}
        </Button>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>{t("profile.accountInfoTitle", { defaultValue: "계정 정보" })}</CardTitle>
        </CardHeader>
        <CardContent>
          {loading ? (
            <div className="space-y-3">
              <Skeleton className="h-5 w-48" />
              <Skeleton className="h-5 w-64" />
              <Skeleton className="h-5 w-40" />
            </div>
          ) : me ? (
            <dl className="grid grid-cols-[7rem_1fr] gap-y-4 text-sm">
              <dt className="text-muted-foreground">{t("profile.name", { defaultValue: "이름" })}</dt>
              <dd className="text-foreground">{me.name}</dd>

              <dt className="text-muted-foreground">{t("profile.email", { defaultValue: "이메일" })}</dt>
              <dd className="text-foreground">{me.email}</dd>

              <dt className="text-muted-foreground">{t("profile.status", { defaultValue: "상태" })}</dt>
              <dd>
                <StatusBadge
                  tone={me.status === "ACTIVE" ? "success" : "warning"}
                  label={
                    me.status === "ACTIVE"
                      ? t("profile.statusActive", { defaultValue: "활성" })
                      : t("profile.statusInactive", { defaultValue: "비활성" })
                  }
                />
              </dd>

              <dt className="text-muted-foreground">{t("profile.roles", { defaultValue: "역할" })}</dt>
              <dd className="flex flex-wrap gap-1">
                {me.roles.length === 0 ? (
                  <span className="text-muted-foreground">
                    {t("profile.noRoles", { defaultValue: "부여된 역할 없음" })}
                  </span>
                ) : (
                  me.roles.map((role) => (
                    <Badge key={role} variant="info">
                      {role}
                    </Badge>
                  ))
                )}
              </dd>
            </dl>
          ) : (
            <p className="text-sm text-muted-foreground">
              {t("profile.loadError", { defaultValue: "정보를 불러오지 못했습니다." })}
            </p>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
