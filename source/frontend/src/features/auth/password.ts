/*
 * 비밀번호 정책 사전검증 — BE 정책과 정합(최소 8자 + 영문·숫자 포함).
 * 클라이언트 UX용 사전검증이며, 최종 검증은 BE(400)이 수행하고 그 메시지는 인라인 노출한다.
 * 계정 생성(initialPassword)·비밀번호 변경(newPassword) 공통 사용.
 */
export const MIN_PASSWORD_LENGTH = 8;

export function validatePasswordPolicy(password: string): string | null {
  if (password.length < MIN_PASSWORD_LENGTH) {
    return `비밀번호는 최소 ${MIN_PASSWORD_LENGTH}자 이상이어야 합니다.`;
  }
  if (!/[A-Za-z]/.test(password) || !/[0-9]/.test(password)) {
    return "비밀번호는 영문과 숫자를 모두 포함해야 합니다.";
  }
  return null;
}
