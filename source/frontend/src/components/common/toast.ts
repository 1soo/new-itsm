import { toast as sonnerToast } from "sonner";

/**
 * 토스트 helper — common.md SCR-COM-009.
 * 성공(Success)·오류(Danger) 피드백 표준. 시맨틱 색상은 토큰으로 지정한다.
 */
export const toast = {
  success(message: string, description?: string) {
    return sonnerToast.success(message, {
      description,
      style: {
        borderColor: "var(--success)",
        color: "var(--success)",
      },
    });
  },
  error(message: string, description?: string) {
    return sonnerToast.error(message, {
      description,
      style: {
        borderColor: "var(--danger)",
        color: "var(--danger)",
      },
    });
  },
  info(message: string, description?: string) {
    return sonnerToast(message, { description });
  },
};
