import { type FormEvent, useCallback, useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { Plus, Trash2 } from "lucide-react";

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
import { toast } from "@/components/common";
import { FullscreenLoader } from "@/routes/FullscreenLoader";
import { assetApi } from "@/features/asset/api";
import { ASSET_TYPES, typeLabel } from "@/features/asset/status";
import type { AssetDetail, AssetType } from "@/features/asset/types";
import { extractErrorMessage } from "@/lib/apiClient";

interface AttributeRow {
  key: string;
  value: string;
}

/*
 * 자산 등록/수정(SCR-ITAM-002) — 이름/유형 필수, 유형 선택 시 유형별 속성(EAV 키-값) 동적 입력,
 * 만료일 3종(라이선스/보증/계약). id 없으면 신규 등록, 있으면 수정.
 */
export function AssetFormPage() {
  const navigate = useNavigate();
  const params = useParams();
  const id = params.id ? Number(params.id) : null;

  const [loading, setLoading] = useState(!!id);
  const [notFound, setNotFound] = useState(false);

  const [name, setName] = useState("");
  const [type, setType] = useState<AssetType | "">("");
  const [owner, setOwner] = useState("");
  const [location, setLocation] = useState("");
  const [purchaseDate, setPurchaseDate] = useState("");
  const [cost, setCost] = useState("");
  const [licenseExpiry, setLicenseExpiry] = useState("");
  const [warrantyExpiry, setWarrantyExpiry] = useState("");
  const [contractExpiry, setContractExpiry] = useState("");
  const [attributes, setAttributes] = useState<AttributeRow[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [saving, setSaving] = useState(false);

  const load = useCallback(() => {
    if (!id) return;
    setLoading(true);
    assetApi
      .get(id)
      .then((d: AssetDetail) => {
        setName(d.name);
        setType(d.type);
        setOwner(d.owner ?? "");
        setLocation(d.location ?? "");
        setLicenseExpiry(d.expiry.license.date ? d.expiry.license.date.slice(0, 10) : "");
        setWarrantyExpiry(d.expiry.warranty.date ? d.expiry.warranty.date.slice(0, 10) : "");
        setContractExpiry(d.expiry.contract.date ? d.expiry.contract.date.slice(0, 10) : "");
        setAttributes(Object.entries(d.attributes ?? {}).map(([key, value]) => ({ key, value })));
        setNotFound(false);
      })
      .catch((err) => {
        toast.error(extractErrorMessage(err));
        setNotFound(true);
      })
      .finally(() => setLoading(false));
  }, [id]);

  useEffect(load, [load]);

  const setAttr = (i: number, field: keyof AttributeRow, v: string) =>
    setAttributes((rows) => rows.map((r, idx) => (idx === i ? { ...r, [field]: v } : r)));
  const addAttr = () => setAttributes((rows) => [...rows, { key: "", value: "" }]);
  const removeAttr = (i: number) => setAttributes((rows) => rows.filter((_, idx) => idx !== i));

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError(null);
    if (!name.trim() || !type) {
      setError("이름과 유형은 필수입니다.");
      return;
    }
    const attributeMap = attributes.reduce<Record<string, string>>((acc, r) => {
      if (r.key.trim()) acc[r.key.trim()] = r.value.trim();
      return acc;
    }, {});
    const payload = {
      name: name.trim(),
      type,
      owner: owner.trim() || undefined,
      location: location.trim() || undefined,
      purchaseDate: purchaseDate ? new Date(purchaseDate).toISOString() : undefined,
      cost: cost ? Number(cost) : undefined,
      licenseExpiry: licenseExpiry ? new Date(licenseExpiry).toISOString() : undefined,
      warrantyExpiry: warrantyExpiry ? new Date(warrantyExpiry).toISOString() : undefined,
      contractExpiry: contractExpiry ? new Date(contractExpiry).toISOString() : undefined,
      attributes: Object.keys(attributeMap).length > 0 ? attributeMap : undefined,
    };
    setSaving(true);
    try {
      if (id) {
        await assetApi.update(id, payload);
        toast.success("자산이 저장되었습니다");
        navigate(`/assets/${id}`);
      } else {
        const created = await assetApi.create(payload);
        toast.success(`자산이 등록되었습니다 (${created.assetKey})`);
        navigate(`/assets/${created.id}`);
      }
    } catch (err) {
      setError(extractErrorMessage(err, "저장에 실패했습니다."));
    } finally {
      setSaving(false);
    }
  };

  if (loading) return <FullscreenLoader />;
  if (id && notFound) {
    return (
      <div className="mx-auto max-w-lg space-y-4 text-center">
        <p className="text-sm text-muted-foreground">자산을 찾을 수 없습니다.</p>
        <Button onClick={() => navigate("/assets")}>목록으로</Button>
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-2xl space-y-4">
      <h1 className="text-xl font-semibold text-foreground">{id ? "자산 수정" : "자산 등록"}</h1>
      <Card>
        <CardHeader>
          <CardTitle>{id ? "자산 정보 수정" : "새 자산"}</CardTitle>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit} className="space-y-4" noValidate>
            <div className="grid grid-cols-2 gap-3">
              <div className="space-y-1.5">
                <Label htmlFor="name">이름</Label>
                <Input id="name" value={name} onChange={(e) => setName(e.target.value)} aria-invalid={!!error && !name.trim()} required />
              </div>
              <div className="space-y-1.5">
                <Label>유형</Label>
                <Select value={type} onValueChange={(v) => setType(v as AssetType)}>
                  <SelectTrigger aria-invalid={!!error && !type}><SelectValue placeholder="유형 선택" /></SelectTrigger>
                  <SelectContent>
                    {ASSET_TYPES.map((t) => (
                      <SelectItem key={t} value={t}>{typeLabel(t)}</SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
            </div>
            <div className="grid grid-cols-2 gap-3">
              <div className="space-y-1.5">
                <Label htmlFor="owner">소유자</Label>
                <Input id="owner" value={owner} onChange={(e) => setOwner(e.target.value)} />
              </div>
              <div className="space-y-1.5">
                <Label htmlFor="location">위치</Label>
                <Input id="location" value={location} onChange={(e) => setLocation(e.target.value)} />
              </div>
            </div>
            <div className="grid grid-cols-2 gap-3">
              <div className="space-y-1.5">
                <Label htmlFor="purchase">구매일</Label>
                <Input id="purchase" type="date" value={purchaseDate} onChange={(e) => setPurchaseDate(e.target.value)} />
              </div>
              <div className="space-y-1.5">
                <Label htmlFor="cost">비용</Label>
                <Input id="cost" type="number" value={cost} onChange={(e) => setCost(e.target.value)} />
              </div>
            </div>

            <div className="grid grid-cols-3 gap-3">
              <div className="space-y-1.5">
                <Label htmlFor="license">라이선스 만료일</Label>
                <Input id="license" type="date" value={licenseExpiry} onChange={(e) => setLicenseExpiry(e.target.value)} />
              </div>
              <div className="space-y-1.5">
                <Label htmlFor="warranty">보증 만료일</Label>
                <Input id="warranty" type="date" value={warrantyExpiry} onChange={(e) => setWarrantyExpiry(e.target.value)} />
              </div>
              <div className="space-y-1.5">
                <Label htmlFor="contract">계약 만료일</Label>
                <Input id="contract" type="date" value={contractExpiry} onChange={(e) => setContractExpiry(e.target.value)} />
              </div>
            </div>

            {type ? (
              <div className="space-y-2">
                <Label>유형별 속성</Label>
                {attributes.map((row, i) => (
                  <div key={i} className="flex items-center gap-2">
                    <Input placeholder="속성명" value={row.key} onChange={(e) => setAttr(i, "key", e.target.value)} />
                    <Input placeholder="값" value={row.value} onChange={(e) => setAttr(i, "value", e.target.value)} />
                    <Button type="button" variant="ghost" size="icon" onClick={() => removeAttr(i)} aria-label="삭제">
                      <Trash2 />
                    </Button>
                  </div>
                ))}
                <Button type="button" variant="outline" size="sm" onClick={addAttr}>
                  <Plus />
                  속성 추가
                </Button>
              </div>
            ) : null}

            {error ? (
              <p role="alert" className="text-sm text-danger">{error}</p>
            ) : null}

            <div className="flex justify-end gap-2">
              <Button type="button" variant="outline" onClick={() => navigate("/assets")}>취소</Button>
              <Button type="submit" loading={saving}>저장</Button>
            </div>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}
