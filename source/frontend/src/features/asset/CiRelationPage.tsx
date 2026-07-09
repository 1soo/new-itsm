import { type FormEvent, useEffect, useState } from "react";
import { Plus } from "lucide-react";

import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { StatusBadge, toast } from "@/components/common";
import { assetApi } from "@/features/asset/api";
import type { Ci, CiRelationType, ImpactItem } from "@/features/asset/types";
import { extractErrorMessage } from "@/lib/apiClient";
import { cn } from "@/lib/utils";

/*
 * CI·CMDB 관계 뷰(SCR-ITAM-004) — CI 목록/등록, 관계 추가 폼, 영향 범위 패널.
 * 관계 시각화는 신규 그래프 라이브러리 없이 단순 리스트(깊이 표시)로 최소 구현.
 */
export function CiRelationPage() {
  const [keyword, setKeyword] = useState("");
  const [cis, setCis] = useState<Ci[]>([]);
  const [loadingCis, setLoadingCis] = useState(true);
  const [selectedId, setSelectedId] = useState<number | null>(null);

  const [impact, setImpact] = useState<ImpactItem[]>([]);
  const [loadingImpact, setLoadingImpact] = useState(false);

  const [newCiName, setNewCiName] = useState("");
  const [newCiType, setNewCiType] = useState("");
  const [creatingCi, setCreatingCi] = useState(false);

  const [targetCiId, setTargetCiId] = useState("");
  const [relationType, setRelationType] = useState<CiRelationType>("DEPENDS_ON");
  const [savingRelation, setSavingRelation] = useState(false);

  const loadCis = () => {
    setLoadingCis(true);
    assetApi
      .listCis({ keyword: keyword || undefined })
      .then((res) => setCis(res.content))
      .catch((err) => toast.error(extractErrorMessage(err)))
      .finally(() => setLoadingCis(false));
  };

  useEffect(loadCis, []);

  const loadImpact = (ciId: number) => {
    setLoadingImpact(true);
    assetApi
      .impact(ciId)
      .then(setImpact)
      .catch((err) => toast.error(extractErrorMessage(err)))
      .finally(() => setLoadingImpact(false));
  };

  useEffect(() => {
    if (selectedId != null) loadImpact(selectedId);
  }, [selectedId]);

  const handleSearch = (e: FormEvent) => {
    e.preventDefault();
    loadCis();
  };

  const handleCreateCi = async (e: FormEvent) => {
    e.preventDefault();
    if (!newCiName.trim()) return;
    setCreatingCi(true);
    try {
      const created = await assetApi.createCi({ name: newCiName.trim(), type: newCiType.trim() || undefined });
      toast.success("CI가 등록되었습니다");
      setNewCiName("");
      setNewCiType("");
      loadCis();
      setSelectedId(created.id);
    } catch (err) {
      toast.error(extractErrorMessage(err));
    } finally {
      setCreatingCi(false);
    }
  };

  const handleAddRelation = async (e: FormEvent) => {
    e.preventDefault();
    if (selectedId == null || !targetCiId) return;
    setSavingRelation(true);
    try {
      await assetApi.createRelation(selectedId, { targetCiId: Number(targetCiId), relationType });
      toast.success("관계가 등록되었습니다");
      setTargetCiId("");
      loadImpact(selectedId);
    } catch (err) {
      toast.error(extractErrorMessage(err));
    } finally {
      setSavingRelation(false);
    }
  };

  const selectedCi = cis.find((c) => c.id === selectedId);

  return (
    <div className="space-y-4">
      <h1 className="text-xl font-semibold text-foreground">CI·CMDB 관계</h1>

      <div className="grid gap-4 lg:grid-cols-[18rem_minmax(0,1fr)]">
        <Card>
          <CardHeader><CardTitle className="text-base">CI 목록</CardTitle></CardHeader>
          <CardContent className="space-y-3">
            <form onSubmit={handleSearch} className="flex items-end gap-2">
              <div className="flex-1 space-y-1">
                <Label htmlFor="kw">검색</Label>
                <Input id="kw" value={keyword} onChange={(e) => setKeyword(e.target.value)} />
              </div>
              <Button type="submit" size="sm">검색</Button>
            </form>

            <div className="space-y-1">
              {loadingCis ? (
                <p className="text-sm text-muted-foreground">불러오는 중...</p>
              ) : cis.length === 0 ? (
                <p className="text-sm text-muted-foreground">등록된 CI가 없습니다.</p>
              ) : (
                cis.map((ci) => (
                  <button
                    key={ci.id}
                    type="button"
                    onClick={() => setSelectedId(ci.id)}
                    className={cn(
                      "block w-full rounded-md px-3 py-2 text-left text-sm transition-colors hover:bg-accent",
                      selectedId === ci.id ? "bg-accent font-medium text-foreground" : "text-muted-foreground",
                    )}
                  >
                    {ci.name}
                    {ci.type ? <span className="ml-1 text-xs">({ci.type})</span> : null}
                  </button>
                ))
              )}
            </div>

            <form onSubmit={handleCreateCi} className="space-y-2 border-t border-border pt-3">
              <Label>새 CI 등록</Label>
              <Input placeholder="이름" value={newCiName} onChange={(e) => setNewCiName(e.target.value)} />
              <Input placeholder="유형(선택)" value={newCiType} onChange={(e) => setNewCiType(e.target.value)} />
              <Button type="submit" size="sm" className="w-full" loading={creatingCi} disabled={!newCiName.trim()}>
                <Plus />
                등록
              </Button>
            </form>
          </CardContent>
        </Card>

        <div className="space-y-4">
          <Card>
            <CardHeader><CardTitle className="text-base">관계 추가</CardTitle></CardHeader>
            <CardContent>
              {selectedCi ? (
                <form onSubmit={handleAddRelation} className="flex flex-wrap items-end gap-2">
                  <p className="w-full text-sm text-muted-foreground">기준 CI: {selectedCi.name}</p>
                  <div className="space-y-1.5">
                    <Label htmlFor="targetCi">대상 CI ID</Label>
                    <Input id="targetCi" type="number" className="w-32" value={targetCiId} onChange={(e) => setTargetCiId(e.target.value)} />
                  </div>
                  <div className="space-y-1.5">
                    <Label>관계 유형</Label>
                    <Select value={relationType} onValueChange={(v) => setRelationType(v as CiRelationType)}>
                      <SelectTrigger className="w-40"><SelectValue /></SelectTrigger>
                      <SelectContent>
                        <SelectItem value="DEPENDS_ON">의존(DEPENDS_ON)</SelectItem>
                        <SelectItem value="RUNS_ON">실행(RUNS_ON)</SelectItem>
                        <SelectItem value="CONNECTS_TO">연결(CONNECTS_TO)</SelectItem>
                      </SelectContent>
                    </Select>
                  </div>
                  <Button type="submit" loading={savingRelation} disabled={!targetCiId}>추가</Button>
                </form>
              ) : (
                <p className="text-sm text-muted-foreground">좌측에서 CI를 선택하세요.</p>
              )}
            </CardContent>
          </Card>

          <Card>
            <CardHeader><CardTitle className="text-base">영향 범위</CardTitle></CardHeader>
            <CardContent>
              {!selectedCi ? (
                <p className="text-sm text-muted-foreground">좌측에서 CI를 선택하세요.</p>
              ) : loadingImpact ? (
                <p className="text-sm text-muted-foreground">불러오는 중...</p>
              ) : impact.length === 0 ? (
                <p className="text-sm text-muted-foreground">연결된 CI가 없습니다.</p>
              ) : (
                <ul className="space-y-2">
                  {impact.map((it) => (
                    <li key={`${it.ciId}-${it.relationType}`} className="flex items-center justify-between gap-2 rounded-md border border-border p-3 text-sm">
                      <span className="flex items-center gap-2">
                        <span style={{ marginLeft: `${(it.depth - 1) * 16}px` }} />
                        {it.name}
                      </span>
                      <span className="flex items-center gap-2">
                        <StatusBadge tone="info" label={it.relationType} />
                        <span className="text-xs text-muted-foreground">depth {it.depth}</span>
                      </span>
                    </li>
                  ))}
                </ul>
              )}
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
}
