import { useState } from "react";
import { useTranslation } from "react-i18next";
import { Check, Globe } from "lucide-react";

import { Button } from "@/components/ui/button";
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover";
import { readStoredLanguage, storeLanguage, type Language } from "@/i18n/language";
import { cn } from "@/lib/utils";

/**
 * 언어 선택 — common.md SCR-COM-015.
 * 지구본 아이콘 클릭 시 팝업(한국어/English), 선택 시 즉시 전환 + 로컬 저장,
 * 최초 마운트 시 저장값 로드(theme-toggle.tsx와 동일한 자체 상태 관리 패턴).
 */
const LANGUAGE_OPTIONS: { value: Language; label: string }[] = [
  { value: "ko", label: "한국어" },
  { value: "en", label: "English" },
];

export function LanguageToggle() {
  const { t, i18n } = useTranslation("common");
  const [open, setOpen] = useState(false);
  const [language, setLanguage] = useState<Language>(() => readStoredLanguage());

  const select = (next: Language) => {
    setLanguage(next);
    storeLanguage(next);
    void i18n.changeLanguage(next);
    setOpen(false);
  };

  return (
    <Popover open={open} onOpenChange={setOpen}>
      <PopoverTrigger asChild>
        <Button variant="ghost" size="icon" aria-label={t("languageToggle.aria")}>
          <Globe />
        </Button>
      </PopoverTrigger>
      <PopoverContent className="w-40 p-1" align="end">
        <ul>
          {LANGUAGE_OPTIONS.map((option) => (
            <li key={option.value}>
              <button
                type="button"
                onClick={() => select(option.value)}
                className="flex w-full items-center justify-between rounded-sm px-2 py-1.5 text-left text-sm text-foreground outline-none hover:bg-accent focus-visible:bg-accent"
              >
                {option.label}
                {language === option.value ? (
                  <Check className={cn("size-4 text-primary")} />
                ) : null}
              </button>
            </li>
          ))}
        </ul>
      </PopoverContent>
    </Popover>
  );
}
