import * as LucideIcons from "lucide-react";
import { HelpCircle, type LucideIcon } from "lucide-react";

/** iconName(문자열, lucide-react 컴포넌트명) → 실제 컴포넌트. 존재하지 않으면 기본 아이콘으로 폴백. */
export function resolveIcon(iconName: string | null | undefined): LucideIcon {
  if (!iconName) return HelpCircle;
  const icon = (LucideIcons as unknown as Record<string, LucideIcon>)[iconName];
  return icon ?? HelpCircle;
}
