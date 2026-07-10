import { useState } from "react";
import { Check, ChevronDown, X } from "lucide-react";

import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from "@/components/ui/popover";
import { cn } from "@/lib/utils";

/**
 * 멀티 셀렉트 — common.md SCR-COM-007 필터바(담당자 등 다중 선택).
 * shadcn Popover + 체크 리스트 조합.
 */
export interface MultiSelectOption {
  value: string;
  label: string;
}

export interface MultiSelectProps {
  options: MultiSelectOption[];
  value: string[];
  onChange: (value: string[]) => void;
  placeholder?: string;
  className?: string;
  disabled?: boolean;
}

export function MultiSelect({
  options,
  value,
  onChange,
  placeholder = "선택",
  className,
  disabled,
}: MultiSelectProps) {
  const [open, setOpen] = useState(false);

  const toggle = (v: string) => {
    onChange(value.includes(v) ? value.filter((x) => x !== v) : [...value, v]);
  };

  const selectedLabels = options.filter((o) => value.includes(o.value));

  return (
    <Popover open={open} onOpenChange={setOpen}>
      <PopoverTrigger asChild>
        <Button
          type="button"
          variant="outline"
          role="combobox"
          aria-expanded={open}
          disabled={disabled}
          className={cn(
            "h-9 w-full justify-between font-normal",
            value.length === 0 && "text-muted-foreground",
            className,
          )}
        >
          <span className="flex flex-1 flex-wrap items-center gap-1 overflow-hidden">
            {selectedLabels.length === 0 ? (
              placeholder
            ) : selectedLabels.length <= 2 ? (
              selectedLabels.map((o) => (
                <Badge key={o.value} variant="secondary" className="rounded-sm">
                  {o.label}
                </Badge>
              ))
            ) : (
              <Badge variant="secondary" className="rounded-sm">
                {selectedLabels.length}개 선택됨
              </Badge>
            )}
          </span>
          {value.length > 0 ? (
            <X
              className="size-4 shrink-0 opacity-60 hover:opacity-100"
              role="button"
              aria-label="선택 해제"
              onClick={(e) => {
                e.stopPropagation();
                onChange([]);
              }}
            />
          ) : (
            <ChevronDown className="size-4 shrink-0 opacity-50" />
          )}
        </Button>
      </PopoverTrigger>
      <PopoverContent className="w-[var(--radix-popover-trigger-width)] p-1" align="start">
        <ul role="listbox" aria-multiselectable="true" className="max-h-60 overflow-auto">
          {options.length === 0 ? (
            <li className="px-2 py-1.5 text-sm text-muted-foreground">항목 없음</li>
          ) : (
            options.map((o) => {
              const selected = value.includes(o.value);
              return (
                <li key={o.value} role="option" aria-selected={selected}>
                  <button
                    type="button"
                    onClick={() => toggle(o.value)}
                    className="flex w-full items-center gap-2 rounded-sm px-2 py-1.5 text-left text-sm outline-none hover:bg-accent focus-visible:bg-accent"
                  >
                    <span
                      className={cn(
                        "flex size-4 items-center justify-center rounded-xs border border-primary",
                        selected
                          ? "bg-primary text-primary-foreground"
                          : "opacity-70",
                      )}
                    >
                      {selected ? <Check className="size-3" /> : null}
                    </span>
                    {o.label}
                  </button>
                </li>
              );
            })
          )}
        </ul>
      </PopoverContent>
    </Popover>
  );
}
