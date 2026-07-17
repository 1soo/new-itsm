import { useRef } from "react";
import { Form } from "@formio/react";
import type { FormSource, Submission } from "@formio/react";
import type { Webform } from "@formio/js";

import "@formio/js/dist/formio.form.min.css";

import { Button } from "@/components/ui/button";
import type { FormIoSchema, FormIoSubmissionData } from "@/components/common/form-schema";

/**
 * 요청자용 폼 렌더 컴포넌트 — SCR-SRM-002/SCR-ESM-002(← dynamic-form.tsx 대체).
 * `@formio/react` Form 래핑. `src`에 저장된 formSchema를 그대로 주입, `onSubmit`으로
 * 제출 데이터(submission.data)를 상위로 전달한다. 클라이언트 검증은 form.io 내장 기능을
 * 사용하고, 서버 재검증은 FormSubmissionValidator(BE)가 담당한다(api_spec/common.md 0-2절).
 *
 * 제출/취소 버튼은 컴포넌트 자체 하단 푸터가 담당한다(우측 정렬, 취소→제출 순, sticky 아님,
 * 유지보수 요청 2026-07-17). 제출은 `onFormReady`로 얻은 인스턴스의 `submit()`을 직접
 * 호출해 검증·제출을 트리거한다. `button`은 관리자 팔레트에서 숨겨져 있고(common.md 8.2절)
 * `FORM_BUILDER_OPTIONS`의 `noAddSubmitButton`/`noDefaultSubmitButton`으로 신규 저장분에는
 * form.io 기본 제출 버튼이 더 이상 섞이지 않지만, 그 전에 저장된 기존 스키마에는 여전히
 * button 컴포넌트가 남아있을 수 있어(TC-SRM-105 결함) `.formio-render-scope` 아래 렌더된
 * button 컴포넌트는 CSS로 숨긴다(index.css).
 */
export interface DynamicFormRendererProps {
  schema: FormIoSchema;
  submissionData?: FormIoSubmissionData;
  onSubmit: (data: FormIoSubmissionData) => void;
  onCancel?: () => void;
  submitLabel?: string;
  cancelLabel?: string;
  disabled?: boolean;
  className?: string;
}

export function DynamicFormRenderer({
  schema,
  submissionData,
  onSubmit,
  onCancel,
  submitLabel = "제출",
  cancelLabel = "취소",
  disabled,
  className,
}: DynamicFormRendererProps) {
  const instanceRef = useRef<Webform | null>(null);

  return (
    <div className={`formio-scope formio-render-scope ${className ?? ""}`}>
      <Form
        src={schema as unknown as FormSource}
        submission={submissionData ? ({ data: submissionData } as unknown as Submission) : undefined}
        options={{ readOnly: disabled }}
        onFormReady={(instance) => {
          instanceRef.current = instance;
        }}
        onSubmit={(submission: Submission) => onSubmit(submission.data as FormIoSubmissionData)}
      />
      <div className="mt-4 flex justify-end gap-2">
        {onCancel ? (
          <Button type="button" variant="outline" onClick={onCancel} disabled={disabled}>
            {cancelLabel}
          </Button>
        ) : null}
        <Button type="button" onClick={() => instanceRef.current?.submit()} disabled={disabled}>
          {submitLabel}
        </Button>
      </div>
    </div>
  );
}
