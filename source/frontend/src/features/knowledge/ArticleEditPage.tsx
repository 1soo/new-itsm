import { type FormEvent, useCallback, useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";

import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { ConfirmDialog, StatusBadge, toast } from "@/components/common";
import { FullscreenLoader } from "@/routes/FullscreenLoader";
import { knowledgeApi } from "@/features/knowledge/api";
import { statusLabel, statusTone } from "@/features/knowledge/status";
import type { ArticleDetail, Category } from "@/features/knowledge/types";
import { extractErrorMessage } from "@/lib/apiClient";

/*
 * 기사 작성·편집(SCR-KM-003) — 제목/본문 필수, 카테고리/라벨 지정, 검토 요청(DRAFT→IN_REVIEW), 삭제.
 * id 없으면 신규 작성 모드, 저장 성공 시 동일 화면(편집 모드)으로 이동.
 */
export function ArticleEditPage() {
  const navigate = useNavigate();
  const params = useParams();
  const id = params.id ? Number(params.id) : null;

  const [detail, setDetail] = useState<ArticleDetail | null>(null);
  const [categories, setCategories] = useState<Category[]>([]);
  const [loading, setLoading] = useState(!!id);
  const [notFound, setNotFound] = useState(false);

  const [title, setTitle] = useState("");
  const [body, setBody] = useState("");
  const [categoryId, setCategoryId] = useState("");
  const [labels, setLabels] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [saving, setSaving] = useState(false);
  const [busy, setBusy] = useState<string | null>(null);
  const [confirmDelete, setConfirmDelete] = useState(false);

  useEffect(() => {
    knowledgeApi.listCategories().then(setCategories).catch((err) => toast.error(extractErrorMessage(err)));
  }, []);

  const load = useCallback(() => {
    if (!id) return;
    setLoading(true);
    knowledgeApi
      .get(id)
      .then((d) => {
        setDetail(d);
        setTitle(d.title);
        setBody(d.body);
        setLabels(d.labels.join(", "));
        setNotFound(false);
      })
      .catch((err) => {
        toast.error(extractErrorMessage(err));
        setNotFound(true);
      })
      .finally(() => setLoading(false));
  }, [id]);

  useEffect(load, [load]);

  useEffect(() => {
    if (detail && categories.length > 0) {
      const match = categories.find((c) => c.name === detail.category);
      if (match) setCategoryId(String(match.id));
    }
  }, [detail, categories]);

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError(null);
    if (!title.trim() || !body.trim()) {
      setError("제목과 본문은 필수입니다.");
      return;
    }
    const payload = {
      title: title.trim(),
      body: body.trim(),
      categoryId: categoryId ? Number(categoryId) : undefined,
      labels: labels.trim() ? labels.split(",").map((l) => l.trim()).filter(Boolean) : undefined,
    };
    setSaving(true);
    try {
      if (id) {
        await knowledgeApi.update(id, payload);
        toast.success("기사가 저장되었습니다");
        load();
      } else {
        const created = await knowledgeApi.create(payload);
        toast.success("기사가 작성되었습니다");
        navigate(`/knowledge/${created.id}/edit`);
      }
    } catch (err) {
      setError(extractErrorMessage(err, "저장에 실패했습니다."));
    } finally {
      setSaving(false);
    }
  };

  const handleRequestReview = async () => {
    if (!id) return;
    setBusy("review");
    try {
      await knowledgeApi.transition(id, "IN_REVIEW");
      toast.success("검토가 요청되었습니다");
      load();
    } catch (err) {
      toast.error(extractErrorMessage(err));
    } finally {
      setBusy(null);
    }
  };

  const handleDelete = async () => {
    if (!id) return;
    setBusy("delete");
    try {
      await knowledgeApi.remove(id);
      toast.success("기사가 삭제되었습니다");
      navigate("/knowledge");
    } catch (err) {
      toast.error(extractErrorMessage(err));
    } finally {
      setBusy(null);
      setConfirmDelete(false);
    }
  };

  if (loading) return <FullscreenLoader />;
  if (id && (notFound || !detail)) {
    return (
      <div className="mx-auto max-w-lg space-y-4 text-center">
        <p className="text-sm text-muted-foreground">기사를 찾을 수 없습니다.</p>
        <Button onClick={() => navigate("/knowledge")}>목록으로</Button>
      </div>
    );
  }

  return (
    <>
      <div className="space-y-4">
        <div className="flex items-center justify-between">
          <h1 className="text-xl font-semibold text-foreground">{id ? "기사 편집" : "기사 작성"}</h1>
          {detail ? <StatusBadge tone={statusTone(detail.status)} label={statusLabel(detail.status)} /> : null}
        </div>

        <div className="grid gap-4 lg:grid-cols-[minmax(0,1fr)_18rem]">
          <Card>
            <CardHeader><CardTitle className="text-base">내용</CardTitle></CardHeader>
            <CardContent>
              <form onSubmit={handleSubmit} className="space-y-4" noValidate>
                <div className="space-y-1.5">
                  <Label htmlFor="title">제목</Label>
                  <Input id="title" value={title} onChange={(e) => setTitle(e.target.value)} aria-invalid={!!error && !title.trim()} required />
                </div>
                <div className="space-y-1.5">
                  <Label htmlFor="body">본문</Label>
                  <Textarea id="body" value={body} onChange={(e) => setBody(e.target.value)} rows={12} aria-invalid={!!error && !body.trim()} required />
                </div>
                {error ? (
                  <p role="alert" className="text-sm text-danger">{error}</p>
                ) : null}
                <div className="flex justify-end">
                  <Button type="submit" loading={saving}>저장</Button>
                </div>
              </form>
            </CardContent>
          </Card>

          <div className="space-y-4">
            <Card>
              <CardHeader><CardTitle className="text-base">분류</CardTitle></CardHeader>
              <CardContent className="space-y-3">
                <div className="space-y-1.5">
                  <Label>카테고리</Label>
                  <Select value={categoryId} onValueChange={setCategoryId}>
                    <SelectTrigger><SelectValue placeholder="카테고리 선택" /></SelectTrigger>
                    <SelectContent>
                      {categories.map((c) => (
                        <SelectItem key={c.id} value={String(c.id)}>{c.name}</SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>
                <div className="space-y-1.5">
                  <Label htmlFor="labels">라벨 (쉼표로 구분)</Label>
                  <Input id="labels" value={labels} onChange={(e) => setLabels(e.target.value)} placeholder="예: 네트워크, VPN" />
                </div>
              </CardContent>
            </Card>

            {id ? (
              <Card>
                <CardHeader><CardTitle className="text-base">작업</CardTitle></CardHeader>
                <CardContent className="space-y-2">
                  {detail?.status === "DRAFT" ? (
                    <Button
                      className="w-full bg-warning text-warning-foreground hover:bg-warning/90"
                      loading={busy === "review"}
                      onClick={handleRequestReview}
                    >
                      검토 요청
                    </Button>
                  ) : null}
                  <Button
                    variant="destructive"
                    className="w-full"
                    onClick={() => setConfirmDelete(true)}
                  >
                    삭제
                  </Button>
                </CardContent>
              </Card>
            ) : null}
          </div>
        </div>
      </div>

      <ConfirmDialog
        open={confirmDelete}
        onOpenChange={setConfirmDelete}
        title="기사를 삭제하시겠습니까?"
        description="삭제된 기사는 복구할 수 없습니다."
        confirmLabel="삭제"
        loading={busy === "delete"}
        onConfirm={handleDelete}
      />
    </>
  );
}
