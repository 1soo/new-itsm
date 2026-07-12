import Swal from "sweetalert2";

/**
 * 토스트 helper — common.md SCR-COM-009(2026-07-12 SweetAlert2 도입).
 * 성공(Success)·오류(Danger)·정보(Info) 피드백 표준. 외부 API(호출부 83개+)는 그대로 유지하고
 * 내부 구현만 SweetAlert2 toast mixin으로 교체(우상단 스택, 시맨틱 토큰만 참조하는 커스텀 CSS는 index.css).
 */
const toastMixin = Swal.mixin({
  toast: true,
  position: "top-end",
  showConfirmButton: false,
  timer: 4000,
  timerProgressBar: true,
  buttonsStyling: false,
  didOpen: (el) => {
    el.addEventListener("mouseenter", Swal.stopTimer);
    el.addEventListener("mouseleave", Swal.resumeTimer);
  },
  customClass: {
    title: "itsm-swal-toast-title",
    htmlContainer: "itsm-swal-toast-description",
  },
  showClass: { popup: "itsm-swal-toast-show" },
  hideClass: { popup: "itsm-swal-toast-hide" },
});

export const toast = {
  success(message: string, description?: string): void {
    void toastMixin.fire({
      title: message,
      text: description,
      customClass: { popup: "itsm-swal-toast itsm-swal-toast--success" },
    });
  },
  error(message: string, description?: string): void {
    void toastMixin.fire({
      title: message,
      text: description,
      customClass: { popup: "itsm-swal-toast itsm-swal-toast--danger" },
    });
  },
  info(message: string, description?: string): void {
    void toastMixin.fire({
      title: message,
      text: description,
      customClass: { popup: "itsm-swal-toast" },
    });
  },
};
