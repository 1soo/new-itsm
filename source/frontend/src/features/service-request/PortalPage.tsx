import { type FormEvent, useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { Search } from "lucide-react";

import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Badge } from "@/components/ui/badge";
import { EmptyState, toast } from "@/components/common";
import { srmApi } from "@/features/service-request/api";
import type {
  CatalogItemSummary,
  KnowledgeSuggestion,
} from "@/features/service-request/types";
import { extractErrorMessage } from "@/lib/apiClient";

/*
 * 서비스 포털(SCR-SRM-001) — 카탈로그 유형 탐색·선택.
 * 카드 클릭 시 요청 제출(SCR-SRM-002)로 이동. 검색 시 관련 지식 기사 추천 배너(있을 때만) 노출.
 */
export function PortalPage() {
  const navigate = useNavigate();
  const [keywordInput, setKeywordInput] = useState("");
  const [keyword, setKeyword] = useState("");
  const [items, setItems] = useState<CatalogItemSummary[]>([]);
  const [loading, setLoading] = useState(true);
  const [suggestions, setSuggestions] = useState<KnowledgeSuggestion[]>([]);

  useEffect(() => {
    let active = true;
    setLoading(true);
    srmApi
      .listCatalog({ keyword: keyword || undefined })
      .then((data) => active && setItems(data))
      .catch((err) => active && toast.error(extractErrorMessage(err)))
      .finally(() => active && setLoading(false));
    return () => {
      active = false;
    };
  }, [keyword]);

  useEffect(() => {
    if (!keyword) {
      setSuggestions([]);
      return;
    }
    let active = true;
    srmApi
      .suggestions({ keyword })
      .then((data) => active && setSuggestions(data))
      .catch(() => active && setSuggestions([]));
    return () => {
      active = false;
    };
  }, [keyword]);

  const handleSearch = (e: FormEvent) => {
    e.preventDefault();
    setKeyword(keywordInput.trim());
  };

  return (
    <div className="space-y-4">
      <div className="space-y-1">
        <h1 className="text-xl font-semibold text-foreground">서비스 포털</h1>
        <p className="text-sm text-muted-foreground">필요한 서비스 유형을 선택해 요청을 제출하세요.</p>
      </div>

      <form onSubmit={handleSearch} className="relative max-w-md">
        <Search className="pointer-events-none absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
        <Input
          value={keywordInput}
          onChange={(e) => setKeywordInput(e.target.value)}
          placeholder="요청 유형·키워드 검색"
          className="pl-9"
          aria-label="카탈로그 검색"
        />
      </form>

      {suggestions.length > 0 ? (
        <Card className="border-l-4 border-l-[color:var(--info)]">
          <CardHeader>
            <CardTitle className="text-base">관련 지식 기사</CardTitle>
          </CardHeader>
          <CardContent className="space-y-1">
            {suggestions.map((s) => (
              <button
                key={s.articleId}
                type="button"
                onClick={() => navigate(`/knowledge/${s.articleId}`)}
                className="block text-left text-sm text-primary hover:underline"
              >
                {s.title}
              </button>
            ))}
          </CardContent>
        </Card>
      ) : null}

      {loading ? (
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {Array.from({ length: 6 }).map((_, i) => (
            <Card key={i} className="h-32 animate-pulse bg-muted/40" />
          ))}
        </div>
      ) : items.length === 0 ? (
        <EmptyState title="카탈로그 항목이 없습니다" description="등록된 요청 유형이 없습니다." />
      ) : (
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {items.map((item) => (
            <Card
              key={item.id}
              role="button"
              tabIndex={0}
              onClick={() => navigate(`/portal/requests/new?item=${item.id}`)}
              onKeyDown={(e) => {
                if (e.key === "Enter") navigate(`/portal/requests/new?item=${item.id}`);
              }}
              className="cursor-pointer transition-colors hover:border-primary"
            >
              <CardHeader>
                <CardTitle className="text-base">{item.name}</CardTitle>
              </CardHeader>
              <CardContent className="space-y-2 text-sm text-muted-foreground">
                <p className="line-clamp-2">{item.description}</p>
                {item.category ? <Badge variant="outline">{item.category}</Badge> : null}
              </CardContent>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
}
