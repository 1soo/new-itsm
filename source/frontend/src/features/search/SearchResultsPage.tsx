import { type FormEvent, useEffect, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { Search } from "lucide-react";

import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import {
  type Column,
  DataTable,
  Pagination,
  StatusBadge,
  TicketListLayout,
  toast,
} from "@/components/common";
import { searchApi } from "@/features/search/api";
import { formatDateTime } from "@/features/search/format";
import { domainLabel, resultStatusLabel, resultStatusTone } from "@/features/search/status";
import type { PageResponse, SearchResultItem } from "@/features/search/types";
import { extractErrorMessage } from "@/lib/apiClient";

/*
 * 통합 검색 결과(SCR-COM-011) — 헤더 검색(SCR-COM-002)에서 Enter/전체 결과 보기 시 진입.
 * 지식+티켓(SRM/INC/PRB/CHG) 교차 도메인 결과를 updatedAt 내림차순 페이지네이션(size=20)으로 표시.
 */
const PAGE_SIZE = 13;

export function SearchResultsPage() {
  const { t } = useTranslation("common");
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();
  const keyword = searchParams.get("keyword") ?? "";

  const [input, setInput] = useState(keyword);
  const [page, setPage] = useState(0);
  const [data, setData] = useState<PageResponse<SearchResultItem> | null>(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    setInput(keyword);
    setPage(0);
  }, [keyword]);

  useEffect(() => {
    if (!keyword.trim()) {
      setData(null);
      return;
    }
    let active = true;
    setLoading(true);
    searchApi
      .search({ keyword, page, size: PAGE_SIZE })
      .then((res) => active && setData(res))
      .catch((err) => active && toast.error(extractErrorMessage(err)))
      .finally(() => active && setLoading(false));
    return () => {
      active = false;
    };
  }, [keyword, page]);

  const handleSearch = (e: FormEvent) => {
    e.preventDefault();
    setSearchParams(input.trim() ? { keyword: input.trim() } : {});
  };

  const columns: Column<SearchResultItem>[] = [
    {
      header: t("search.columnDomain", { defaultValue: "도메인" }),
      width: 130,
      cell: (r) => <StatusBadge tone="info" label={domainLabel(t, r.domain)} />,
    },
    {
      header: t("search.columnTitle", { defaultValue: "제목" }),
      cell: (r) => (
        <div className="space-y-0.5">
          <p className="truncate text-sm font-medium text-foreground">{r.title}</p>
          <p className="truncate text-xs text-muted-foreground">{r.key}</p>
          {r.snippet ? <p className="line-clamp-1 text-xs text-muted-foreground">{r.snippet}</p> : null}
        </div>
      ),
    },
    {
      header: t("search.columnStatus", { defaultValue: "상태" }),
      width: 130,
      cell: (r) => (
        <StatusBadge tone={resultStatusTone(r.domain, r.status)} label={resultStatusLabel(t, r.domain, r.status)} />
      ),
    },
    { header: t("search.columnUpdatedAt", { defaultValue: "갱신일" }), width: 160, cell: (r) => formatDateTime(r.updatedAt) },
  ];

  const totalPages = data ? Math.ceil(data.totalElements / PAGE_SIZE) : 0;

  return (
    <TicketListLayout
      title={t("search.title", { defaultValue: "통합 검색 결과" })}
      description={
        keyword
          ? t("search.descriptionWithKeyword", { keyword, defaultValue: `"${keyword}" 검색 결과` })
          : t("search.descriptionEmpty", { defaultValue: "검색어를 입력하세요." })
      }
      filters={
        <form onSubmit={handleSearch} className="flex w-full max-w-md items-end gap-2">
          <div className="relative flex-1">
            <Search className="pointer-events-none absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
            <Input
              value={input}
              onChange={(e) => setInput(e.target.value)}
              placeholder={t("search.placeholder", { defaultValue: "지식·티켓 검색" })}
              className="pl-9"
              aria-label={t("search.inputAriaLabel", { defaultValue: "검색어" })}
            />
          </div>
          <Button type="submit">{t("search.searchButton", { defaultValue: "검색" })}</Button>
        </form>
      }
    >
      <DataTable
        columns={columns}
        data={data?.content ?? []}
        rowKey={(r) => `${r.domain}-${r.key}`}
        loading={loading}
        onRowClick={(r) => navigate(r.url)}
        emptyTitle={t("search.emptyTitle", { defaultValue: "검색 결과가 없습니다" })}
        emptyDescription={t("search.emptyDescription", { defaultValue: "다른 검색어로 다시 시도해보세요." })}
      />
      <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />
    </TicketListLayout>
  );
}
