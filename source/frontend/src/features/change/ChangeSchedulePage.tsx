import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { ChevronLeft, ChevronRight } from "lucide-react";

import { Button } from "@/components/ui/button";
import { Label } from "@/components/ui/label";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { StatusBadge, toast } from "@/components/common";
import { cn } from "@/lib/utils";
import { changeApi } from "@/features/change/api";
import { CHANGE_TYPES, typeLabel, typeTone } from "@/features/change/status";
import type { ScheduleItem } from "@/features/change/types";
import { extractErrorMessage } from "@/lib/apiClient";

const ALL = "ALL";

function toDateKey(iso: string): string {
  const d = new Date(iso);
  return `${d.getFullYear()}-${d.getMonth()}-${d.getDate()}`;
}

/*
 * 변경 일정(SCR-CHG-005) — 월 캘린더 그리드. 유형 필터, 날짜별 변경 항목(유형 색상 칩), 클릭 시 상세.
 * 기능 전용 최소 구현(공용 캘린더 컴포넌트 미도입 — 재사용 필요 시 추후 공용화).
 */
export function ChangeSchedulePage() {
  const { t } = useTranslation("change");
  const navigate = useNavigate();
  const [month, setMonth] = useState(() => {
    const now = new Date();
    return new Date(now.getFullYear(), now.getMonth(), 1);
  });
  const [type, setType] = useState(ALL);
  const [items, setItems] = useState<ScheduleItem[]>([]);
  const [loading, setLoading] = useState(true);

  const monthStart = useMemo(() => new Date(month.getFullYear(), month.getMonth(), 1), [month]);
  const monthEnd = useMemo(() => new Date(month.getFullYear(), month.getMonth() + 1, 0), [month]);

  useEffect(() => {
    let active = true;
    setLoading(true);
    changeApi
      .schedule({
        from: monthStart.toISOString(),
        to: monthEnd.toISOString(),
        type: type === ALL ? undefined : type,
      })
      .then((res) => active && setItems(res))
      .catch((err) => active && toast.error(extractErrorMessage(err)))
      .finally(() => active && setLoading(false));
    return () => {
      active = false;
    };
  }, [monthStart, monthEnd, type]);

  const itemsByDay = useMemo(() => {
    const map = new Map<string, ScheduleItem[]>();
    for (const item of items) {
      const key = toDateKey(item.scheduledAt);
      const list = map.get(key) ?? [];
      list.push(item);
      map.set(key, list);
    }
    return map;
  }, [items]);

  const cells = useMemo(() => {
    const firstWeekday = monthStart.getDay();
    const daysInMonth = monthEnd.getDate();
    const result: (Date | null)[] = [];
    for (let i = 0; i < firstWeekday; i++) result.push(null);
    for (let d = 1; d <= daysInMonth; d++) result.push(new Date(month.getFullYear(), month.getMonth(), d));
    return result;
  }, [month, monthStart, monthEnd]);

  const weekdays = [
    t("changeSchedule.weekdaySun", { defaultValue: "일" }),
    t("changeSchedule.weekdayMon", { defaultValue: "월" }),
    t("changeSchedule.weekdayTue", { defaultValue: "화" }),
    t("changeSchedule.weekdayWed", { defaultValue: "수" }),
    t("changeSchedule.weekdayThu", { defaultValue: "목" }),
    t("changeSchedule.weekdayFri", { defaultValue: "금" }),
    t("changeSchedule.weekdaySat", { defaultValue: "토" }),
  ];

  return (
    <div className="space-y-4">
      <div className="flex flex-wrap items-center justify-between gap-2">
        <h1 className="text-xl font-semibold text-foreground">
          {t("changeSchedule.title", { defaultValue: "변경 일정" })}
        </h1>
        <div className="flex items-center gap-2">
          <div className="space-y-1">
            <Label>{t("changeList.columnType", { defaultValue: "유형" })}</Label>
            <Select value={type} onValueChange={setType}>
              <SelectTrigger className="w-28"><SelectValue /></SelectTrigger>
              <SelectContent>
                <SelectItem value={ALL}>{t("changeList.filterAll", { defaultValue: "전체" })}</SelectItem>
                {CHANGE_TYPES.map((ty) => (
                  <SelectItem key={ty} value={ty}>{typeLabel(t, ty)}</SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
        </div>
      </div>

      <div className="flex items-center justify-center gap-3 rounded-lg border border-border bg-card p-3">
        <Button
          variant="outline"
          size="icon"
          onClick={() => setMonth((m) => new Date(m.getFullYear(), m.getMonth() - 1, 1))}
          aria-label={t("changeSchedule.prevMonth", { defaultValue: "이전 달" })}
        >
          <ChevronLeft />
        </Button>
        <span className="min-w-32 text-center font-medium text-foreground">
          {t("changeSchedule.monthLabel", {
            year: month.getFullYear(),
            month: month.getMonth() + 1,
            defaultValue: `${month.getFullYear()}년 ${month.getMonth() + 1}월`,
          })}
        </span>
        <Button
          variant="outline"
          size="icon"
          onClick={() => setMonth((m) => new Date(m.getFullYear(), m.getMonth() + 1, 1))}
          aria-label={t("changeSchedule.nextMonth", { defaultValue: "다음 달" })}
        >
          <ChevronRight />
        </Button>
      </div>

      <div className="grid grid-cols-7 gap-px overflow-hidden rounded-lg border border-border bg-border">
        {weekdays.map((w) => (
          <div key={w} className="bg-muted p-2 text-center text-xs font-medium text-muted-foreground">
            {w}
          </div>
        ))}
        {cells.map((date, i) => {
          const key = date ? `${date.getFullYear()}-${date.getMonth()}-${date.getDate()}` : `empty-${i}`;
          const dayItems = date ? (itemsByDay.get(key) ?? []) : [];
          return (
            <div
              key={key}
              className={cn("min-h-24 space-y-1 bg-card p-1.5", !date && "bg-muted/30")}
            >
              {date ? <span className="text-xs text-muted-foreground">{date.getDate()}</span> : null}
              {dayItems.map((it) => (
                <button
                  key={it.id}
                  className="block w-full truncate rounded px-1.5 py-0.5 text-left text-xs"
                  onClick={() => navigate(`/changes/${it.id}`)}
                  title={it.summary}
                >
                  <StatusBadge tone={typeTone(it.type)} label={it.summary} />
                </button>
              ))}
            </div>
          );
        })}
      </div>

      {!loading && items.length === 0 ? (
        <p className="text-center text-sm text-muted-foreground">
          {t("changeSchedule.empty", { defaultValue: "예정된 변경이 없습니다." })}
        </p>
      ) : null}
    </div>
  );
}
