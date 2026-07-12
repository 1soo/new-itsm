import { useEffect } from "react";
import { useTranslation } from "react-i18next";
import Swal from "sweetalert2";

/**
 * 확인 다이얼로그 — common.md SCR-COM-009(2026-07-12 SweetAlert2 도입).
 * 파괴적/비가역 동작(폐기·삭제·반려·로그아웃) 확인. 확인 시에만 onConfirm 호출.
 * 선언형 컴포넌트 API(props/시그니처)는 그대로 유지하고, 내부에서 open 변화를 감지해
 * SweetAlert2를 명령형으로 호출하는 래퍼 패턴으로 구현한다(호출부 수정 불필요).
 */
export interface ConfirmDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  title: string;
  description?: string;
  confirmLabel?: string;
  cancelLabel?: string;
  /** 파괴적 동작이면 확인 버튼을 Danger로 표시 */
  destructive?: boolean;
  loading?: boolean;
  onConfirm: () => void;
}

export function ConfirmDialog({
  open,
  onOpenChange,
  title,
  description,
  confirmLabel,
  cancelLabel,
  destructive = true,
  loading = false,
  onConfirm,
}: ConfirmDialogProps) {
  const { t } = useTranslation("common");

  useEffect(() => {
    if (!open) return;

    let confirmed = false;
    void Swal.fire({
      title,
      text: description,
      showCancelButton: true,
      reverseButtons: true,
      focusCancel: !destructive,
      confirmButtonText: confirmLabel ?? t("dialog.confirm"),
      cancelButtonText: cancelLabel ?? t("dialog.cancel"),
      buttonsStyling: false,
      allowOutsideClick: () => !Swal.isLoading(),
      allowEscapeKey: () => !Swal.isLoading(),
      customClass: {
        popup: "itsm-swal-popup",
        title: "itsm-swal-title",
        htmlContainer: "itsm-swal-html",
        actions: "itsm-swal-actions",
        confirmButton: destructive
          ? "itsm-swal-btn itsm-swal-btn--destructive"
          : "itsm-swal-btn itsm-swal-btn--primary",
        cancelButton: "itsm-swal-btn itsm-swal-btn--outline",
      },
      showClass: { popup: "itsm-swal-modal-show" },
      hideClass: { popup: "itsm-swal-modal-hide" },
      preConfirm: () => {
        confirmed = true;
        onConfirm();
        // 확인 시 즉시 닫지 않는다 — 로딩 상태는 loading prop으로 표시하고,
        // 실제 닫힘은 호출측이 onConfirm 처리 후 onOpenChange(false)로 제어한다.
        return false;
      },
    }).then((result) => {
      if (!confirmed && !result.isConfirmed) {
        onOpenChange(false);
      }
    });

    return () => {
      if (Swal.isVisible()) Swal.close();
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [open]);

  useEffect(() => {
    if (!open || !Swal.isVisible()) return;
    if (loading) {
      Swal.showLoading();
    } else {
      Swal.hideLoading();
    }
  }, [loading, open]);

  return null;
}
