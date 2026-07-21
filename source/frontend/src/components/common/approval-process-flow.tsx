import { useId, useMemo, useState, type DragEvent } from "react";
import { Check, GripVertical, Plus, X } from "lucide-react";
import { useTranslation } from "react-i18next";
import type { TFunction } from "i18next";

import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Checkbox } from "@/components/ui/checkbox";
import { Badge } from "@/components/ui/badge";
import {
  Sheet,
  SheetContent,
  SheetDescription,
  SheetHeader,
  SheetTitle,
} from "@/components/ui/sheet";
import { ConfirmDialog } from "@/components/common/confirm-dialog";
import { cn } from "@/lib/utils";
import type { ApprovalMatchType } from "@/components/common/approval-schema";

/**
 * 승인 프로세스 생성/편집 플로우 — admin.md SCR-ADMIN-008.
 * 1단계(승인 요청자)~2단계(승인자 n차) 카드 스택을 구성한다(도메인·요청유형 선택은
 * 메타데이터 분리 개편으로 FE의 "규칙 정보" 카드가 담당, 유지보수 요청 2026-07-13).
 * 제어 컴포넌트: 각 단계 값과 변경 콜백을 FE(라우팅·API·검증 조합)가 주입하고, 이
 * 컴포넌트는 카드 스택 레이아웃·드래그 앤 드롭 재정렬·역할 선택 사이드 패널만 담당한다.
 * 단, "박스 0개 역할" 인라인 오류와 "승인자 0개 저장" 확인 다이얼로그는 이 컴포넌트가 직접 처리한다
 * (단순 필수 입력 검증이라 프레젠테이션 레이어에서 완결하는 편이 FE 중복 구현을 줄인다).
 * 문구는 `auth:admin.approvalProcessForm.flow.*` 키(common.md 6.8절, i18n 커버리지 결함 수정).
 */
export interface ApprovalRoleOption {
  id: string;
  label: string;
}

export interface ApprovalStepBoxValue {
  /** 클라이언트 측 안정 키(승인자 박스 재정렬·드래그 대상 식별용) */
  id: string;
  roleIds: string[];
  matchType: ApprovalMatchType;
}

export interface ApprovalProcessFlowProps {
  /** 도메인 미선택 시 하단 단계 카드 스택을 비활성화하기 위한 값(선택 UI 자체는 FE의 "규칙 정보" 카드가 담당) */
  domain: string;

  roleOptions: ApprovalRoleOption[];

  /** 항상 1개(1단계) */
  requester: ApprovalStepBoxValue;
  onRequesterChange: (value: ApprovalStepBoxValue) => void;
  /**
   * true면 요청자 박스 역할이 최소 1개 이상이어야 저장 가능(적용 상태를 구체적으로 지정한 경우,
   * 2026-07-22 유지보수 요청). 미충족 시 인라인 에러 표시 + 저장 버튼 비활성화.
   */
  requesterRoleRequired?: boolean;

  /** 배열 순서 = 차수(1차부터, 2단계) */
  approvers: ApprovalStepBoxValue[];
  onApproversChange: (value: ApprovalStepBoxValue[]) => void;

  /** 기본 "생성 완료", 편집 시 "저장" */
  submitLabel?: string;
  onSubmit: () => void;
  submitting?: boolean;
  /** 409 우선순위 충돌 등 폼 상단 인라인 오류 */
  formError?: string | null;
  className?: string;
}

type RolePanelTarget = { kind: "requester" | "approver"; id: string } | null;

const ROLE_MIME = "application/x-itsm-role-id";
const APPROVER_MIME = "application/x-itsm-approver-index";
const MAX_APPROVER_STEPS = 10;

export function ApprovalProcessFlow({
  domain,
  roleOptions,
  requester,
  onRequesterChange,
  requesterRoleRequired = false,
  approvers,
  onApproversChange,
  submitLabel,
  onSubmit,
  submitting = false,
  formError,
  className,
}: ApprovalProcessFlowProps) {
  const { t } = useTranslation("auth");
  const resolvedSubmitLabel =
    submitLabel ?? t("admin.approvalProcessForm.createComplete", { defaultValue: "생성 완료" });
  const [rolePanelTarget, setRolePanelTarget] = useState<RolePanelTarget>(null);
  const [roleSearch, setRoleSearch] = useState("");
  const [dragOverIndex, setDragOverIndex] = useState<number | null>(null);
  const [attemptedSubmit, setAttemptedSubmit] = useState(false);
  const [confirmOpen, setConfirmOpen] = useState(false);

  const domainSelected = domain !== "";

  const filteredRoles = useMemo(
    () =>
      roleOptions.filter((r) =>
        r.label.toLowerCase().includes(roleSearch.trim().toLowerCase()),
      ),
    [roleOptions, roleSearch],
  );

  const activeBoxRoleIds =
    rolePanelTarget?.kind === "requester"
      ? requester.roleIds
      : (approvers.find((a) => a.id === rolePanelTarget?.id)?.roleIds ?? []);

  const addRole = (target: NonNullable<RolePanelTarget>, roleId: string) => {
    if (target.kind === "requester") {
      if (!requester.roleIds.includes(roleId)) {
        onRequesterChange({ ...requester, roleIds: [...requester.roleIds, roleId] });
      }
      return;
    }
    onApproversChange(
      approvers.map((a) =>
        a.id === target.id && !a.roleIds.includes(roleId)
          ? { ...a, roleIds: [...a.roleIds, roleId] }
          : a,
      ),
    );
  };

  const removeRole = (kind: "requester" | "approver", id: string, roleId: string) => {
    if (kind === "requester") {
      onRequesterChange({ ...requester, roleIds: requester.roleIds.filter((r) => r !== roleId) });
      return;
    }
    onApproversChange(
      approvers.map((a) => (a.id === id ? { ...a, roleIds: a.roleIds.filter((r) => r !== roleId) } : a)),
    );
  };

  const toggleMatch = (kind: "requester" | "approver", id: string, matchType: ApprovalMatchType) => {
    if (kind === "requester") {
      onRequesterChange({ ...requester, matchType });
      return;
    }
    onApproversChange(approvers.map((a) => (a.id === id ? { ...a, matchType } : a)));
  };

  const addApprover = () => {
    if (approvers.length >= MAX_APPROVER_STEPS) return;
    onApproversChange([
      ...approvers,
      { id: `apv_${Date.now()}_${Math.random().toString(36).slice(2, 7)}`, roleIds: [], matchType: "AND" },
    ]);
  };

  const removeApprover = (id: string) => {
    onApproversChange(approvers.filter((a) => a.id !== id));
  };

  const swapApprovers = (fromIndex: number, toIndex: number) => {
    if (fromIndex === toIndex || fromIndex < 0 || fromIndex >= approvers.length) return;
    const next = [...approvers];
    [next[fromIndex], next[toIndex]] = [next[toIndex], next[fromIndex]];
    onApproversChange(next);
  };

  const handleRequesterDrop = (e: DragEvent) => {
    e.preventDefault();
    const roleId = e.dataTransfer.getData(ROLE_MIME);
    if (roleId) addRole({ kind: "requester", id: requester.id }, roleId);
  };

  const handleApproverDrop = (e: DragEvent, targetIndex: number) => {
    e.preventDefault();
    setDragOverIndex(null);
    const roleId = e.dataTransfer.getData(ROLE_MIME);
    if (roleId) {
      addRole({ kind: "approver", id: approvers[targetIndex].id }, roleId);
      return;
    }
    const fromRaw = e.dataTransfer.getData(APPROVER_MIME);
    if (fromRaw !== "") swapApprovers(Number(fromRaw), targetIndex);
  };

  // 요청자 축은 0개(전체 요청자, 우선순위 미지정 축)를 허용한다(2026-07-15 우선순위 재설계 —
  // 승인자 박스(steps)만 roleIds 1개 이상이 API-AUTH-027 계약상 필수, 요청자는 아님). 단, 적용
  // 상태를 구체적으로 지정한 경우(requesterRoleRequired)에는 요청자 박스도 1개 이상 필수(2026-07-22 유지보수 요청).
  const hasEmptyApproverBox = approvers.some((a) => a.roleIds.length === 0);
  const requesterRoleMissing = requesterRoleRequired && requester.roleIds.length === 0;

  const handleSubmitClick = () => {
    setAttemptedSubmit(true);
    if (hasEmptyApproverBox || requesterRoleMissing) return;
    if (approvers.length === 0) {
      setConfirmOpen(true);
      return;
    }
    onSubmit();
  };

  return (
    <div className={cn("flex flex-col gap-8", className)}>
      <div className="sticky top-0 z-10 flex items-center justify-between gap-3 border-b border-border bg-background/95 py-3 backdrop-blur">
        <h1 className="text-heading-large font-bold text-foreground">
          {t("admin.approvalProcessForm.flow.title", { defaultValue: "승인 프로세스" })}
        </h1>
        <Button type="button" loading={submitting} disabled={requesterRoleMissing} onClick={handleSubmitClick}>
          {resolvedSubmitLabel}
        </Button>
      </div>

      {formError ? (
        <p className="rounded-md border border-danger-subtle-foreground/30 bg-danger-subtle px-3 py-2 text-sm text-danger-subtle-foreground">
          {formError}
        </p>
      ) : null}

      <div className={cn("flex flex-col gap-8", !domainSelected && "pointer-events-none opacity-50")}>
        <Card>
          <CardHeader>
            <CardTitle>
              {t("admin.approvalProcessForm.flow.requesterStepTitle", { defaultValue: "1단계 · 승인 요청자" })}
            </CardTitle>
          </CardHeader>
          <CardContent className="pt-0">
            <div
              onDragOver={(e) => e.preventDefault()}
              onDrop={handleRequesterDrop}
              className="flex flex-col gap-3 rounded-lg border-2 border-solid border-info/40 bg-card p-4"
            >
              <RoleChipArea
                roleOptions={roleOptions}
                roleIds={requester.roleIds}
                onOpenPicker={() => setRolePanelTarget({ kind: "requester", id: requester.id })}
                onRemoveRole={(roleId) => removeRole("requester", requester.id, roleId)}
                showError={requesterRoleMissing}
                errorMessage={t("admin.approvalProcessForm.flow.requesterRoleRequiredError", {
                  defaultValue: "이 상태에서 요청할 역할을 지정하세요",
                })}
                t={t}
              />
              {requester.roleIds.length >= 2 ? (
                <MatchTypeCheckbox
                  matchType={requester.matchType}
                  onChange={(m) => toggleMatch("requester", requester.id, m)}
                  t={t}
                />
              ) : null}
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>
              {t("admin.approvalProcessForm.flow.approverStepTitle", { defaultValue: "2단계 · 승인자" })}
            </CardTitle>
          </CardHeader>
          <CardContent className="flex flex-col gap-4 pt-0">
            {approvers.map((box, index) => (
              <div
                key={box.id}
                draggable
                onDragStart={(e) => e.dataTransfer.setData(APPROVER_MIME, String(index))}
                onDragOver={(e) => {
                  e.preventDefault();
                  setDragOverIndex(index);
                }}
                onDragLeave={() => setDragOverIndex((prev) => (prev === index ? null : prev))}
                onDrop={(e) => handleApproverDrop(e, index)}
                className={cn(
                  "flex flex-col gap-3 rounded-lg border-2 border-dashed border-border bg-card p-4 transition-colors animate-in fade-in slide-in-from-top-4 duration-300",
                  dragOverIndex === index && "border-primary bg-accent",
                )}
              >
                <div className="flex items-center gap-2">
                  <GripVertical
                    className="size-4 shrink-0 cursor-grab text-muted-foreground"
                    aria-hidden="true"
                  />
                  <Badge variant="info">
                    {t("admin.approvalProcessForm.flow.stepBadge", { count: index + 1, defaultValue: `${index + 1}차` })}
                  </Badge>
                  <Button
                    type="button"
                    variant="ghost"
                    size="icon"
                    className="ml-auto"
                    aria-label={t("admin.approvalProcessForm.flow.removeApproverAria", { defaultValue: "승인자 박스 삭제" })}
                    onClick={() => removeApprover(box.id)}
                  >
                    <X className="text-destructive" />
                  </Button>
                </div>
                <RoleChipArea
                  roleOptions={roleOptions}
                  roleIds={box.roleIds}
                  onOpenPicker={() => setRolePanelTarget({ kind: "approver", id: box.id })}
                  onRemoveRole={(roleId) => removeRole("approver", box.id, roleId)}
                  showError={attemptedSubmit && box.roleIds.length === 0}
                  t={t}
                />
                {box.roleIds.length >= 2 ? (
                  <MatchTypeCheckbox
                    matchType={box.matchType}
                    onChange={(m) => toggleMatch("approver", box.id, m)}
                    t={t}
                  />
                ) : null}
              </div>
            ))}

            <Button
              type="button"
              variant="outline"
              className="self-start"
              disabled={approvers.length >= MAX_APPROVER_STEPS}
              title={
                approvers.length >= MAX_APPROVER_STEPS
                  ? t("admin.approvalProcessForm.flow.maxStepsTooltip", {
                      defaultValue: "최대 10차까지 추가할 수 있습니다",
                    })
                  : undefined
              }
              onClick={addApprover}
            >
              <Plus /> {t("admin.approvalProcessForm.flow.addApprover", { defaultValue: "승인자 추가" })}
            </Button>
          </CardContent>
        </Card>
      </div>

      <Sheet open={rolePanelTarget !== null} onOpenChange={(open) => !open && setRolePanelTarget(null)}>
        <SheetContent className="gap-3">
          <SheetHeader>
            <SheetTitle>{t("admin.approvalProcessForm.flow.roleSheetTitle", { defaultValue: "역할 선택" })}</SheetTitle>
            <SheetDescription>
              {t("admin.approvalProcessForm.flow.roleSheetDescription", {
                defaultValue: "목록을 박스 영역으로 드래그하거나 클릭하여 추가하세요.",
              })}
            </SheetDescription>
          </SheetHeader>
          <Input
            placeholder={t("admin.approvalProcessForm.flow.roleSearchPlaceholder", { defaultValue: "역할 검색" })}
            value={roleSearch}
            onChange={(e) => setRoleSearch(e.target.value)}
          />
          <ul className="flex flex-1 flex-col gap-1 overflow-auto">
            {filteredRoles.length === 0 ? (
              <li className="px-2 py-1.5 text-sm text-muted-foreground">
                {t("admin.approvalProcessForm.flow.noMatchingRoles", { defaultValue: "일치하는 역할이 없습니다" })}
              </li>
            ) : (
              filteredRoles.map((role) => {
                const added = activeBoxRoleIds.includes(role.id);
                return (
                  <li key={role.id}>
                    <button
                      type="button"
                      draggable={!added}
                      onDragStart={(e) => e.dataTransfer.setData(ROLE_MIME, role.id)}
                      disabled={added}
                      onClick={() => rolePanelTarget && addRole(rolePanelTarget, role.id)}
                      className="flex w-full cursor-grab items-center justify-between rounded-sm px-2 py-1.5 text-left text-sm outline-none hover:bg-accent focus-visible:bg-accent disabled:cursor-default disabled:opacity-50"
                    >
                      {role.label}
                      {added ? <Check className="size-3.5 text-muted-foreground" /> : null}
                    </button>
                  </li>
                );
              })
            )}
          </ul>
        </SheetContent>
      </Sheet>

      <ConfirmDialog
        open={confirmOpen}
        onOpenChange={setConfirmOpen}
        title={t("admin.approvalProcessForm.flow.noApproverConfirmTitle", { defaultValue: "승인자 없이 진행하시겠습니까?" })}
        description={t("admin.approvalProcessForm.flow.noApproverConfirmDescription", {
          defaultValue: "승인자 없이 바로 진행됩니다.",
        })}
        confirmLabel={resolvedSubmitLabel}
        destructive={false}
        onConfirm={() => {
          setConfirmOpen(false);
          onSubmit();
        }}
      />
    </div>
  );
}

function RoleChipArea({
  roleOptions,
  roleIds,
  onOpenPicker,
  onRemoveRole,
  showError,
  errorMessage,
  t,
}: {
  roleOptions: ApprovalRoleOption[];
  roleIds: string[];
  onOpenPicker: () => void;
  onRemoveRole: (roleId: string) => void;
  showError: boolean;
  errorMessage?: string;
  t: TFunction;
}) {
  const labelOf = (id: string) => roleOptions.find((r) => r.id === id)?.label ?? id;
  return (
    <div className="flex flex-col gap-2">
      <div className="flex flex-wrap items-center gap-1.5">
        {roleIds.map((id) => (
          <Badge key={id} variant="secondary" className="gap-1 pr-1">
            {labelOf(id)}
            <button
              type="button"
              aria-label={t("admin.approvalProcessForm.flow.removeRoleAria", {
                role: labelOf(id),
                defaultValue: `${labelOf(id)} 역할 제거`,
              })}
              onClick={() => onRemoveRole(id)}
              className="rounded-full hover:bg-black/10"
            >
              <X className="size-3" />
            </button>
          </Badge>
        ))}
        <Button type="button" variant="outline" size="sm" onClick={onOpenPicker}>
          {t("admin.approvalProcessForm.flow.selectRole", { defaultValue: "역할 선택" })}
        </Button>
      </div>
      {showError ? (
        <p className="text-xs text-destructive">
          {errorMessage ??
            t("admin.approvalProcessForm.flow.roleRequiredError", { defaultValue: "역할을 1개 이상 선택하세요" })}
        </p>
      ) : null}
    </div>
  );
}

function MatchTypeCheckbox({
  matchType,
  onChange,
  t,
}: {
  matchType: ApprovalMatchType;
  onChange: (matchType: ApprovalMatchType) => void;
  t: TFunction;
}) {
  const id = useId();
  return (
    <div className="flex items-center gap-2">
      <Checkbox
        id={id}
        checked={matchType === "AND"}
        onCheckedChange={(c) => onChange(c === true ? "AND" : "OR")}
      />
      <Label htmlFor={id} className="font-normal">
        {t("admin.approvalProcessForm.flow.matchTypeAnd", { defaultValue: "모두 승인 필요 (AND)" })}
      </Label>
    </div>
  );
}
