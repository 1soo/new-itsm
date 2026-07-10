import { useEffect, useState } from "react";
import { Moon, Sun } from "lucide-react";

import { Button } from "@/components/ui/button";

/**
 * 테마 토글 — common.md SCR-COM-010.
 * 라이트=달 아이콘/다크=해 아이콘. 클릭 시 data-theme 즉시 전환 + 로컬 저장,
 * 최초 진입 시 저장값 로드(없거나 유효하지 않으면 라이트 기본).
 */
type Theme = "light" | "dark";

const STORAGE_KEY = "itsm-theme";

function readStoredTheme(): Theme {
  const stored = window.localStorage.getItem(STORAGE_KEY);
  return stored === "dark" ? "dark" : "light";
}

function applyTheme(theme: Theme) {
  document.documentElement.setAttribute("data-theme", theme);
}

export function ThemeToggle() {
  const [theme, setTheme] = useState<Theme>(() => {
    const initial = readStoredTheme();
    applyTheme(initial);
    return initial;
  });

  useEffect(() => {
    applyTheme(theme);
  }, [theme]);

  const toggle = () => {
    setTheme((current) => {
      const next: Theme = current === "dark" ? "light" : "dark";
      window.localStorage.setItem(STORAGE_KEY, next);
      return next;
    });
  };

  return (
    <Button variant="ghost" size="icon" onClick={toggle} aria-label="테마 전환">
      {theme === "dark" ? <Sun /> : <Moon />}
    </Button>
  );
}
