import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { useTranslation } from "react-i18next";

import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import {
  ApprovalProcessFlow,
  type ApprovalRoleOption,
  type ApprovalStepBoxValue,
  toast,
} from "@/components/common";
import { FullscreenLoader } from "@/routes/FullscreenLoader";
import { adminApi } from "@/features/admin/api";
import type {
  ApprovalDomain,
  ApprovalDomainOption,
  DecisionMode,
  RequestSubtypeOption,
  Role,
} from "@/features/admin/types";
import { extractErrorMessage } from "@/lib/apiClient";

/*
 * 승인 프로세스 생성/편집(SCR-ADMIN-008) — "규칙 정보" 카드(도메인·요청유형·규칙명·설명)와
 * 1(승인 요청자)~2(승인자 n차) 단계 카드 스택으로 구성한다. 메타데이터 분리 개편(유지보수 요청
 * 2026-07-13)으로 도메인·요청유형 선택은 이 화면의 "규칙 정보" 카드가 직접 렌더링하고(카드 스택
 * 안에 있던 구 0/1단계를 이관), 승인 단계 카드 스택은 승인 요청자부터 1단계로 재시작한다. 편집 시
 * domain·requestSubtypeKey는 식별 스코프라 수정 대상에서 제외한다(API-AUTH-028). 카드 스택
 * 레이아웃·드래그 재정렬·역할 선택 패널·박스별 필수역할 검증·승인자 0개 확인 다이얼로그는 공용
 * `ApprovalProcessFlow`(components/common)가 담당한다.
 */
const NO_SUBTYPE = "__ALL__";

function newRequesterBox(roleIds: string[] = []): ApprovalStepBoxValue {
  return { id: "requester", roleIds, matchType: "AND" };
}

export function ApprovalProcessFormPage() {
  const { t } = useTranslation("auth");
  const params = useParams();
  const navigate = useNavigate();
  const id = params.id ? Number(params.id) : null;
  const isEdit = id != null;

  const [loading, setLoading] = useState(isEdit);
  const [saving, setSaving] = useState(false);
  const [formError, setFormError] = useState<string | null>(null);

  const [domains, setDomains] = useState<ApprovalDomainOption[]>([]);
  const [roles, setRoles] = useState<Role[]>([]);
  const [subtypes, setSubtypes] = useState<RequestSubtypeOption[]>([]);

  const [domain, setDomain] = useState<ApprovalDomain | "">("");
  const [requestSubtypeKey, setRequestSubtypeKey] = useState<string>(NO_SUBTYPE);
  const [name, setName] = useState("");
  const [description, setDescription] = useState("");
  const [requester, setRequester] = useState<ApprovalStepBoxValue>(newRequesterBox());
  const [approvers, setApprovers] = useState<ApprovalStepBoxValue[]>([]);

  useEffect(() => {
    adminApi
      .listApprovalDomains()
      .then(setDomains)
      .catch((err) => toast.error(extractErrorMessage(err)));
    adminApi
      .listRoles()
      .then(setRoles)
      .catch((err) => toast.error(extractErrorMessage(err)));
  }, []);

  useEffect(() => {
    if (!isEdit || id == null) return;
    adminApi
      .getApprovalProcess(id)
      .then((detail) => {
        setDomain(detail.domain);
        setRequestSubtypeKey(detail.requestSubtypeKey ?? NO_SUBTYPE);
        setName(detail.name);
        setDescription(detail.description ?? "");
        setRequester(newRequesterBox(detail.requesterRoleIds.map(String)));
        setApprovers(
          detail.steps.map((s, i) => ({
            id: `apv_existing_${i}`,
            roleIds: s.roleIds.map(String),
            matchType: s.decisionMode,
          })),
        );
      })
      .catch((err) => {
        toast.error(extractErrorMessage(err));
        navigate("/admin/approval-processes");
      })
      .finally(() => setLoading(false));
  }, [isEdit, id, navigate]);

  const selectedDomainOption = domains.find((d) => d.domain === domain) ?? null;
  const hasRequestSubtype = !!selectedDomainOption?.hasRequestSubtype;

  // 신규 생성 시에만 도메인 선택에 따라 요청유형 후보를 갱신한다(편집은 스코프 고정이라 재조회 불필요).
  useEffect(() => {
    if (isEdit) return;
    if (!domain || !hasRequestSubtype) {
      setSubtypes([]);
      return;
    }
    adminApi
      .listRequestSubtypes(domain)
      .then(setSubtypes)
      .catch((err) => toast.error(extractErrorMessage(err)));
  }, [isEdit, domain, hasRequestSubtype]);

  const roleOptions: ApprovalRoleOption[] = roles.map((r) => ({ id: String(r.id), label: r.name }));

  const doSubmit = async () => {
    setSaving(true);
    setFormError(null);
    try {
      const stepsPayload = approvers.map((a) => ({
        decisionMode: a.matchType as DecisionMode,
        roleIds: a.roleIds.map(Number),
      }));
      if (isEdit && id != null) {
        await adminApi.updateApprovalProcess(id, {
          name: name.trim(),
          description: description.trim() || undefined,
          requesterRoleIds: requester.roleIds.map(Number),
          steps: stepsPayload,
        });
        toast.success(t("admin.approvalProcessForm.updateSuccess", { defaultValue: "승인 프로세스가 저장되었습니다" }));
      } else {
        await adminApi.createApprovalProcess({
          domain: domain as ApprovalDomain,
          requestSubtypeKey: hasRequestSubtype && requestSubtypeKey !== NO_SUBTYPE ? requestSubtypeKey : null,
          name: name.trim(),
          description: description.trim() || undefined,
          requesterRoleIds: requester.roleIds.map(Number),
          steps: stepsPayload,
        });
        toast.success(t("admin.approvalProcessForm.createSuccess", { defaultValue: "승인 프로세스가 생성되었습니다" }));
      }
      navigate("/admin/approval-processes");
    } catch (err) {
      setFormError(extractErrorMessage(err));
    } finally {
      setSaving(false);
    }
  };

  const handleSubmit = () => {
    setFormError(null);
    if (!domain) {
      setFormError(t("admin.approvalProcessForm.domainRequiredError", { defaultValue: "도메인을 선택하세요." }));
      return;
    }
    if (!name.trim()) {
      setFormError(t("admin.approvalProcessForm.nameRequiredError", { defaultValue: "규칙명을 입력하세요." }));
      return;
    }
    doSubmit();
  };

  if (loading) return <FullscreenLoader />;

  return (
    <div className="space-y-4">
      <Card>
        <CardHeader>
          <CardTitle className="text-base">
            {t("admin.approvalProcessForm.ruleInfoTitle", { defaultValue: "규칙 정보" })}
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-3">
          <div className="space-y-1.5">
            <Label htmlFor="ap-domain">{t("admin.approvalProcessForm.domainLabel", { defaultValue: "도메인" })}</Label>
            {/* 편집 시 domain은 식별 스코프라 변경을 허용하지 않는다(API-AUTH-028). */}
            <Select value={domain} onValueChange={(v) => setDomain(v as ApprovalDomain)} disabled={isEdit}>
              <SelectTrigger id="ap-domain" className="max-w-xs">
                <SelectValue
                  placeholder={t("admin.approvalProcessForm.domainPlaceholder", { defaultValue: "도메인 선택" })}
                />
              </SelectTrigger>
              <SelectContent>
                {domains.map((d) => (
                  <SelectItem key={d.domain} value={d.domain}>
                    {d.label}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
          {hasRequestSubtype ? (
            <div className="space-y-1.5">
              <Label htmlFor="ap-subtype">
                {t("admin.approvalProcessForm.requestSubtypeLabel", { defaultValue: "요청 유형" })}
              </Label>
              {/* 편집 시 requestSubtypeKey는 식별 스코프라 변경을 허용하지 않는다(API-AUTH-028). */}
              <Select value={requestSubtypeKey} onValueChange={setRequestSubtypeKey} disabled={isEdit}>
                <SelectTrigger id="ap-subtype" className="max-w-xs">
                  <SelectValue
                    placeholder={t("admin.approvalProcessForm.requestSubtypePlaceholder", {
                      defaultValue: "요청 유형 선택",
                    })}
                  />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value={NO_SUBTYPE}>
                    {t("admin.approvalProcessForm.allSubtypes", { defaultValue: "전체" })}
                  </SelectItem>
                  {subtypes.map((s) => (
                    <SelectItem key={s.key} value={s.key}>
                      {s.label}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
          ) : null}
          <div className="space-y-1.5">
            <Label htmlFor="ap-name">{t("admin.approvalProcessForm.name", { defaultValue: "규칙명" })}</Label>
            <Input id="ap-name" value={name} onChange={(e) => setName(e.target.value)} required />
          </div>
          <div className="space-y-1.5">
            <Label htmlFor="ap-desc">
              {t("admin.approvalProcessForm.description", { defaultValue: "설명(선택)" })}
            </Label>
            <Input id="ap-desc" value={description} onChange={(e) => setDescription(e.target.value)} />
          </div>
        </CardContent>
      </Card>

      <ApprovalProcessFlow
        domain={domain}
        roleOptions={roleOptions}
        requester={requester}
        onRequesterChange={setRequester}
        approvers={approvers}
        onApproversChange={setApprovers}
        submitLabel={
          isEdit
            ? t("admin.approvalProcessForm.save", { defaultValue: "저장" })
            : t("admin.approvalProcessForm.createComplete", { defaultValue: "생성 완료" })
        }
        onSubmit={handleSubmit}
        submitting={saving}
        formError={formError}
      />
    </div>
  );
}
