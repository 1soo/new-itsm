/**
 * 언어 선호도 로컬 저장 — common.md SCR-COM-015.
 * theme-toggle.tsx와 동일한 패턴(itsm-language 키, 저장값 없거나 유효하지 않으면 한국어 기본).
 */
export type Language = "ko" | "en";

const STORAGE_KEY = "itsm-language";

export function readStoredLanguage(): Language {
  const stored = window.localStorage.getItem(STORAGE_KEY);
  return stored === "en" ? "en" : "ko";
}

export function storeLanguage(language: Language) {
  window.localStorage.setItem(STORAGE_KEY, language);
}
