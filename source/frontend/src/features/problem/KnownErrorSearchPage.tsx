import { type FormEvent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { Search } from "lucide-react";

import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Badge } from "@/components/ui/badge";
import { EmptyState, Pagination, toast } from "@/components/common";
import { problemApi } from "@/features/problem/api";
import type { KnownError, PageResponse } from "@/features/problem/types";
import { extractErrorMessage } from "@/lib/apiClient";

/*
 * KEDB 검색(SCR-PRB-004) — 알려진 오류(근본원인+워크어라운드)를 키워드로 조회.
 * 결과 없으면 빈 상태. 결과 목록(제목·근본원인·워크어라운드·연결 문제).
 */
const PAGE_SIZE = 10;

export function KnownErrorSearchPage() {
  const { t } = useTranslation("problem");
  const [keywordInput, setKeywordInput] = useState("");
  const [keyword, setKeyword] = useState("");
  const [page, setPage] = useState(0);
  const [data, setData] = useState<PageResponse<KnownError> | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let active = true;
    setLoading(true);
    problemApi
      .searchKnownErrors({ keyword: keyword || undefined, page, size: PAGE_SIZE })
      .then((res) => active && setData(res))
      .catch((err) => active && toast.error(extractErrorMessage(err)))
      .finally(() => active && setLoading(false));
    return () => {
      active = false;
    };
  }, [keyword, page]);

  const handleSearch = (e: FormEvent) => {
    e.preventDefault();
    setPage(0);
    setKeyword(keywordInput.trim());
  };

  const results = data?.content ?? [];
  const totalPages = data ? Math.ceil(data.totalElements / PAGE_SIZE) : 0;

  return (
    <div className="mx-auto max-w-3xl space-y-4">
      <h1 className="text-xl font-semibold text-foreground">
        {t("knownErrorSearch.title", { defaultValue: "알려진 오류(KEDB) 검색" })}
      </h1>

      <form onSubmit={handleSearch} className="flex items-end gap-2">
        <div className="flex-1 space-y-1">
          <Input
            value={keywordInput}
            onChange={(e) => setKeywordInput(e.target.value)}
            placeholder={t("knownErrorSearch.searchPlaceholder", { defaultValue: "제목·증상 키워드로 검색" })}
          />
        </div>
        <Button type="submit">
          <Search />
          {t("problemList.searchButton", { defaultValue: "검색" })}
        </Button>
      </form>

      {loading ? (
        <p className="text-sm text-muted-foreground">
          {t("knownErrorSearch.loading", { defaultValue: "불러오는 중..." })}
        </p>
      ) : results.length === 0 ? (
        <EmptyState
          title={t("knownErrorSearch.emptyTitle", { defaultValue: "알려진 오류가 없습니다" })}
          description={t("knownErrorSearch.emptyDescription", { defaultValue: "조건에 맞는 알려진 오류가 없습니다." })}
        />
      ) : (
        <div className="space-y-3">
          {results.map((ke) => (
            <Card key={ke.id}>
              <CardHeader className="flex-row items-center justify-between gap-2">
                <CardTitle className="text-base">{ke.title}</CardTitle>
                {ke.problemKey ? <Badge variant="info">{ke.problemKey}</Badge> : null}
              </CardHeader>
              <CardContent className="space-y-2 text-sm">
                <div>
                  <p className="font-medium text-foreground">
                    {t("problemDetail.rootCause", { defaultValue: "근본 원인" })}
                  </p>
                  <p className="whitespace-pre-wrap text-muted-foreground">{ke.rootCause || "-"}</p>
                </div>
                <div>
                  <p className="font-medium text-foreground">
                    {t("problemDetail.workaroundTitle", { defaultValue: "워크어라운드" })}
                  </p>
                  <p className="whitespace-pre-wrap text-muted-foreground">{ke.workaround || "-"}</p>
                </div>
              </CardContent>
            </Card>
          ))}
          <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />
        </div>
      )}
    </div>
  );
}
