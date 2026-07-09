/* INC 화면 공통 표시 포맷터. */

export function formatDate(iso: string): string {
  const d = new Date(iso);
  return Number.isNaN(d.getTime()) ? iso : d.toLocaleDateString("ko-KR");
}

export function formatDateTime(iso: string): string {
  const d = new Date(iso);
  return Number.isNaN(d.getTime()) ? iso : d.toLocaleString("ko-KR");
}

/** 분 단위 지표 표시(null이면 미산정). */
export function formatMinutes(m: number | null | undefined): string {
  if (m == null) return "미산정";
  return `${Math.round(m)}분`;
}
