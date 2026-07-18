import type { ReactNode } from "react";

import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { cn } from "@/lib/utils";

/**
 * 범용 모달 — common.md SCR-COM-009.
 * 폼/상세 등 비파괴 콘텐츠 표시. 파괴적 동작 확인은 ConfirmDialog를 사용한다.
 */
export interface ModalProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  title: string;
  /** 타이틀 옆(우측)에 배치할 부가 콘텐츠(2026-07-18 유지보수 요청 8차 신규, 예: 체크박스 그룹). 미지정 시 기존과 동일한 마크업. */
  titleExtra?: ReactNode;
  description?: string;
  children: ReactNode;
  footer?: ReactNode;
  className?: string;
}

export function Modal({
  open,
  onOpenChange,
  title,
  titleExtra,
  description,
  children,
  footer,
  className,
}: ModalProps) {
  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className={cn(className)}>
        <DialogHeader>
          {titleExtra ? (
            <div className="flex items-center justify-between gap-3 pr-8">
              <DialogTitle>{title}</DialogTitle>
              {titleExtra}
            </div>
          ) : (
            <DialogTitle>{title}</DialogTitle>
          )}
          {description ? <DialogDescription>{description}</DialogDescription> : null}
        </DialogHeader>
        {children}
        {footer ? <DialogFooter>{footer}</DialogFooter> : null}
      </DialogContent>
    </Dialog>
  );
}
