import { type FormEvent, useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { Plus } from "lucide-react";

import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import {
  type Column,
  DataTable,
  Pagination,
  StatusBadge,
  TicketListLayout,
  toast,
} from "@/components/common";
import { hasAnyRole, ROLE_KNOWLEDGE_CONTRIBUTOR } from "@/features/auth/roles";
import { knowledgeApi } from "@/features/knowledge/api";
import { ARTICLE_STATUSES, statusLabel, statusTone } from "@/features/knowledge/status";
import type { ArticleStatus, ArticleSummary, Category, PageResponse } from "@/features/knowledge/types";
import { useAppSelector } from "@/store/hooks";
import { extractErrorMessage } from "@/lib/apiClient";

/*
 * 지식베이스 검색/목록(SCR-KM-001) — 키워드·카테고리·라벨·상태 필터.
 * 최종 사용자는 게시 기사만, 기여자/게이트키퍼는 초안·검토 포함 조회(BE가 역할별 필터링).
 */
const PAGE_SIZE = 14;
const ALL = "ALL";

interface Filters {
  keyword: string;
  category: string;
  label: string;
  status: string;
}

const EMPTY: Filters = { keyword: "", category: ALL, label: "", status: ALL };

export function KnowledgeListPage() {
  const { t } = useTranslation("knowledge");
  const navigate = useNavigate();
  const roles = useAppSelector((s) => s.auth.user?.roles);
  const canWrite = hasAnyRole(roles, [ROLE_KNOWLEDGE_CONTRIBUTOR]);

  const [categories, setCategories] = useState<Category[]>([]);
  const [inputs, setInputs] = useState<Filters>(EMPTY);
  const [applied, setApplied] = useState<Filters>(EMPTY);
  const [page, setPage] = useState(0);
  const [data, setData] = useState<PageResponse<ArticleSummary> | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    knowledgeApi.listCategories().then(setCategories).catch((err) => toast.error(extractErrorMessage(err)));
  }, []);

  useEffect(() => {
    let active = true;
    setLoading(true);
    knowledgeApi
      .list({
        keyword: applied.keyword || undefined,
        category: applied.category === ALL ? undefined : applied.category,
        label: applied.label || undefined,
        status: applied.status === ALL ? undefined : (applied.status as ArticleStatus),
        page,
        size: PAGE_SIZE,
      })
      .then((res) => active && setData(res))
      .catch((err) => active && toast.error(extractErrorMessage(err)))
      .finally(() => active && setLoading(false));
    return () => {
      active = false;
    };
  }, [applied, page]);

  const handleSearch = (e: FormEvent) => {
    e.preventDefault();
    setPage(0);
    setApplied(inputs);
  };

  const columns: Column<ArticleSummary>[] = [
    { header: t("knowledgeList.columnTitle", { defaultValue: "제목" }), cell: (a) => <span className="line-clamp-1">{a.title}</span> },
    {
      header: t("knowledgeList.columnSummary", { defaultValue: "요약" }),
      cell: (a) => <span className="line-clamp-1 text-muted-foreground">{a.summary}</span>,
    },
    {
      header: t("knowledgeList.columnCategory", { defaultValue: "카테고리" }),
      cell: (a) => a.category || "-",
      className: "whitespace-nowrap",
      width: 110,
    },
    {
      header: t("knowledgeList.columnStatus", { defaultValue: "상태" }),
      cell: (a) => <StatusBadge tone={statusTone(a.status)} label={statusLabel(t, a.status)} />,
      className: "whitespace-nowrap",
      width: 110,
    },
    {
      header: t("knowledgeList.columnHelpfulRate", { defaultValue: "유용성" }),
      cell: (a) => `${Math.round(a.helpfulRate)}%`,
      className: "whitespace-nowrap",
      width: 90,
    },
  ];

  const totalPages = data ? Math.ceil(data.totalElements / PAGE_SIZE) : 0;

  return (
    <TicketListLayout
      title={t("knowledgeList.title", { defaultValue: "지식베이스" })}
      actions={
        canWrite ? (
          <Button onClick={() => navigate("/knowledge/new")}>
            <Plus />
            {t("knowledgeList.createButton", { defaultValue: "기사 작성" })}
          </Button>
        ) : undefined
      }
      filters={
        <form onSubmit={handleSearch} className="flex flex-wrap items-end gap-2">
          <div className="flex-1 space-y-1 min-w-40">
            <Label htmlFor="keyword">{t("knowledgeList.filterKeyword", { defaultValue: "키워드" })}</Label>
            <Input id="keyword" value={inputs.keyword} onChange={(e) => setInputs((f) => ({ ...f, keyword: e.target.value }))} />
          </div>
          <div className="space-y-1">
            <Label>{t("knowledgeList.columnCategory", { defaultValue: "카테고리" })}</Label>
            <Select value={inputs.category} onValueChange={(v) => setInputs((f) => ({ ...f, category: v }))}>
              <SelectTrigger className="w-36"><SelectValue /></SelectTrigger>
              <SelectContent>
                <SelectItem value={ALL}>{t("knowledgeList.filterAll", { defaultValue: "전체" })}</SelectItem>
                {categories.map((c) => (
                  <SelectItem key={c.id} value={c.name}>{c.name}</SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
          <div className="space-y-1">
            <Label htmlFor="label">{t("knowledgeList.filterLabel", { defaultValue: "라벨" })}</Label>
            <Input id="label" className="w-32" value={inputs.label} onChange={(e) => setInputs((f) => ({ ...f, label: e.target.value }))} />
          </div>
          <div className="space-y-1">
            <Label>{t("knowledgeList.columnStatus", { defaultValue: "상태" })}</Label>
            <Select value={inputs.status} onValueChange={(v) => setInputs((f) => ({ ...f, status: v }))}>
              <SelectTrigger className="w-32"><SelectValue /></SelectTrigger>
              <SelectContent>
                <SelectItem value={ALL}>{t("knowledgeList.filterAll", { defaultValue: "전체" })}</SelectItem>
                {ARTICLE_STATUSES.map((s) => (
                  <SelectItem key={s} value={s}>{statusLabel(t, s)}</SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
          <Button type="submit">{t("knowledgeList.searchButton", { defaultValue: "검색" })}</Button>
        </form>
      }
    >
      <DataTable
        columns={columns}
        data={data?.content ?? []}
        rowKey={(a) => a.id}
        loading={loading}
        onRowClick={(a) => navigate(`/knowledge/${a.id}`)}
        emptyTitle={t("knowledgeList.emptyTitle", { defaultValue: "기사가 없습니다" })}
        emptyDescription={t("knowledgeList.emptyDescription", { defaultValue: "조건에 맞는 지식 기사가 없습니다." })}
      />
      <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />
    </TicketListLayout>
  );
}
