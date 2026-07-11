import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";

import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
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
 * 승인 프로세스 생성/편집(SCR-ADMIN-008) — 0(도메인)~3(승인자 n차) 단계를 거쳐 커스텀 승인
 * 프로세스를 정의한다. 편집 시 domain·requestSubtypeKey는 식별 스코프라 수정 대상에서 제외한다
 * (API-AUTH-028). 카드 스택 레이아웃·드래그 재정렬·역할 선택 패널·박스별 필수역할 검증·승인자
 * 0개 확인 다이얼로그는 공용 `ApprovalProcessFlow`(components/common)가 담당하고, 이 화면은
 * 도메인/요청유형/이름 등 나머지 폼 필드와 API 연동만 조립한다.
 */
const NO_SUBTYPE = "__ALL__";

function newRequesterBox(roleIds: string[] = []): ApprovalStepBoxValue {
  return { id: "requester", roleIds, matchType: "AND" };
}

export function ApprovalProcessFormPage() {
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
        toast.success("승인 프로세스가 저장되었습니다");
      } else {
        await adminApi.createApprovalProcess({
          domain: domain as ApprovalDomain,
          requestSubtypeKey: hasRequestSubtype && requestSubtypeKey !== NO_SUBTYPE ? requestSubtypeKey : null,
          name: name.trim(),
          description: description.trim() || undefined,
          requesterRoleIds: requester.roleIds.map(Number),
          steps: stepsPayload,
        });
        toast.success("승인 프로세스가 생성되었습니다");
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
      setFormError("도메인을 선택하세요.");
      return;
    }
    if (!name.trim()) {
      setFormError("규칙명을 입력하세요.");
      return;
    }
    doSubmit();
  };

  if (loading) return <FullscreenLoader />;

  return (
    <div className="space-y-4">
      <Card>
        <CardHeader>
          <CardTitle className="text-base">규칙 정보</CardTitle>
        </CardHeader>
        <CardContent className="space-y-3">
          <div className="space-y-1.5">
            <Label htmlFor="ap-name">규칙명</Label>
            <Input id="ap-name" value={name} onChange={(e) => setName(e.target.value)} required />
          </div>
          <div className="space-y-1.5">
            <Label htmlFor="ap-desc">설명(선택)</Label>
            <Input id="ap-desc" value={description} onChange={(e) => setDescription(e.target.value)} />
          </div>
        </CardContent>
      </Card>

      <ApprovalProcessFlow
        domainOptions={domains.map((d) => ({ value: d.domain, label: d.label }))}
        domain={domain}
        onDomainChange={(v) => setDomain(v as ApprovalDomain)}
        // 편집 시 domain·requestSubtypeKey는 식별 스코프라 변경을 허용하지 않는다(API-AUTH-028).
        domainDisabled={isEdit}
        requestSubtypeOptions={
          hasRequestSubtype
            ? [{ value: NO_SUBTYPE, label: "전체" }, ...subtypes.map((s) => ({ value: s.key, label: s.label }))]
            : null
        }
        requestSubtype={requestSubtypeKey}
        onRequestSubtypeChange={setRequestSubtypeKey}
        requestSubtypeDisabled={isEdit}
        roleOptions={roleOptions}
        requester={requester}
        onRequesterChange={setRequester}
        approvers={approvers}
        onApproversChange={setApprovers}
        submitLabel={isEdit ? "저장" : "생성 완료"}
        onSubmit={handleSubmit}
        submitting={saving}
        formError={formError}
      />
    </div>
  );
}
