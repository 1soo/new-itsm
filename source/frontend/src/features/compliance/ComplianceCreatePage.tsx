import { type FormEvent, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";

import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { toast } from "@/components/common";
import { complianceApi } from "@/features/compliance/api";
import { extractErrorMessage } from "@/lib/apiClient";

/*
 * 요구사항 등록(SCR-COMP-002) — 이름·근거(필수)·적용 범위(선택).
 * 성공 시 상세 이동.
 */
export function ComplianceCreatePage() {
  const { t } = useTranslation("compliance");
  const navigate = useNavigate();
  const [name, setName] = useState("");
  const [basis, setBasis] = useState("");
  const [scope, setScope] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError(null);
    if (!name.trim() || !basis.trim()) {
      setError(t("complianceCreate.requiredError", { defaultValue: "이름과 근거는 필수입니다." }));
      return;
    }
    setSubmitting(true);
    try {
      const created = await complianceApi.create({
        name: name.trim(),
        basis: basis.trim(),
        scope: scope.trim() || undefined,
      });
      toast.success(
        t("complianceCreate.success", {
          requirementKey: created.requirementKey,
          defaultValue: `요구사항이 등록되었습니다 (${created.requirementKey})`,
        }),
      );
      navigate(`/compliance/requirements/${created.id}`);
    } catch (err) {
      setError(extractErrorMessage(err, t("complianceCreate.failed", { defaultValue: "등록에 실패했습니다." })));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="mx-auto max-w-lg space-y-4">
      <h1 className="text-xl font-semibold text-foreground">{t("complianceCreate.title", { defaultValue: "요구사항 등록" })}</h1>
      <Card>
        <CardHeader>
          <CardTitle>{t("complianceCreate.cardTitle", { defaultValue: "새 컴플라이언스 요구사항" })}</CardTitle>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit} className="space-y-4" noValidate>
            <div className="space-y-1.5">
              <Label htmlFor="name">{t("complianceCreate.nameLabel", { defaultValue: "이름" })}</Label>
              <Input id="name" value={name} onChange={(e) => setName(e.target.value)} aria-invalid={!!error && !name.trim()} required />
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="basis">{t("complianceCreate.basisLabel", { defaultValue: "근거 (규제 조항/내부 정책)" })}</Label>
              <Input id="basis" value={basis} onChange={(e) => setBasis(e.target.value)} aria-invalid={!!error && !basis.trim()} required />
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="scope">{t("complianceCreate.scopeLabel", { defaultValue: "적용 범위" })}</Label>
              <Input id="scope" value={scope} onChange={(e) => setScope(e.target.value)} />
            </div>

            {error ? (
              <p role="alert" className="text-sm text-danger">{error}</p>
            ) : null}

            <div className="flex justify-end gap-2">
              <Button type="button" variant="outline" onClick={() => navigate("/compliance/requirements")}>
                {t("complianceCreate.cancelButton", { defaultValue: "취소" })}
              </Button>
              <Button type="submit" loading={submitting}>{t("complianceCreate.submitButton", { defaultValue: "등록" })}</Button>
            </div>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}
