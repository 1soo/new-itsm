import { type FormEvent, useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { Search } from "lucide-react";

import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Badge } from "@/components/ui/badge";
import { EmptyState, toast } from "@/components/common";
import { esmApi } from "@/features/esm/api";
import { DEPARTMENTS, departmentLabel } from "@/features/esm/status";
import type { CatalogItemSummary, Department } from "@/features/esm/types";
import { extractErrorMessage } from "@/lib/apiClient";
import { cn } from "@/lib/utils";

const ALL = "__ALL__";

/*
 * 부서 서비스 포털(SCR-ESM-001) — 부서 탭(전체+HR/법무/시설/재무)+검색바로 카탈로그 탐색.
 * 카드 클릭 시 요청 제출(SCR-ESM-002)로 이동. Tabs 공통 컴포넌트가 없어 버튼형 토글로 구현.
 */
export function DeptPortalPage() {
  const navigate = useNavigate();
  const [department, setDepartment] = useState<string>(ALL);
  const [keywordInput, setKeywordInput] = useState("");
  const [keyword, setKeyword] = useState("");
  const [items, setItems] = useState<CatalogItemSummary[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let active = true;
    setLoading(true);
    esmApi
      .listCatalog({
        department: department === ALL ? "" : (department as Department),
        keyword: keyword || undefined,
      })
      .then((data) => active && setItems(data))
      .catch((err) => active && toast.error(extractErrorMessage(err)))
      .finally(() => active && setLoading(false));
    return () => {
      active = false;
    };
  }, [department, keyword]);

  const handleSearch = (e: FormEvent) => {
    e.preventDefault();
    setKeyword(keywordInput.trim());
  };

  return (
    <div className="space-y-4">
      <div className="space-y-1">
        <h1 className="text-xl font-semibold text-foreground">부서 서비스 포털</h1>
        <p className="text-sm text-muted-foreground">부서별 요청 유형을 탐색해 요청을 제출하세요.</p>
      </div>

      <div className="flex flex-wrap items-center gap-1 rounded-lg border border-border bg-card p-1">
        <DeptTab label="전체" active={department === ALL} onClick={() => setDepartment(ALL)} />
        {DEPARTMENTS.map((d) => (
          <DeptTab key={d} label={departmentLabel(d)} active={department === d} onClick={() => setDepartment(d)} />
        ))}
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
              onClick={() => navigate(`/esm/portal/requests/new?item=${item.id}`)}
              onKeyDown={(e) => {
                if (e.key === "Enter") navigate(`/esm/portal/requests/new?item=${item.id}`);
              }}
              className="cursor-pointer transition-colors hover:border-primary"
            >
              <CardHeader>
                <CardTitle className="text-base">{item.name}</CardTitle>
              </CardHeader>
              <CardContent className="space-y-2 text-sm text-muted-foreground">
                <p className="line-clamp-2">{item.description}</p>
                <Badge variant="info">{departmentLabel(item.department)}</Badge>
              </CardContent>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
}

function DeptTab({ label, active, onClick }: { label: string; active: boolean; onClick: () => void }) {
  return (
    <button
      type="button"
      onClick={onClick}
      className={cn(
        "rounded-md px-3 py-1.5 text-sm transition-colors",
        active ? "bg-primary text-primary-foreground" : "text-muted-foreground hover:bg-accent",
      )}
    >
      {label}
    </button>
  );
}
