import { type FormEvent, useEffect, useState } from "react";

import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { type Column, DataTable, StatusBadge, toast } from "@/components/common";
import { infraApi } from "@/features/infra-monitoring/api";
import { utilizationTone } from "@/features/infra-monitoring/status";
import type { CapacityPlan } from "@/features/infra-monitoring/types";
import { extractErrorMessage } from "@/lib/apiClient";

/*
 * 용량 계획 관리(SCR-IOM-004) — 팀/서비스 단위 역량·예상 수요 등록 및 목록 조회.
 * 활용률(demand/capacity)은 조회 시점 계산값을 BE가 내려준다.
 */
export function InfraCapacityPlanPage() {
  const [teamOrService, setTeamOrService] = useState("");
  const [capacity, setCapacity] = useState("");
  const [demand, setDemand] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  const [plans, setPlans] = useState<CapacityPlan[]>([]);
  const [loading, setLoading] = useState(true);

  const load = () => {
    setLoading(true);
    infraApi
      .listCapacityPlans()
      .then(setPlans)
      .catch((err) => toast.error(extractErrorMessage(err)))
      .finally(() => setLoading(false));
  };

  useEffect(load, []);

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError(null);
    if (!teamOrService.trim() || !capacity || !demand) {
      setError("팀/서비스명·역량·예상 수요는 필수입니다.");
      return;
    }
    setSubmitting(true);
    try {
      await infraApi.createCapacityPlan({
        teamOrService: teamOrService.trim(),
        capacity: Number(capacity),
        demand: Number(demand),
      });
      toast.success("용량 계획이 등록되었습니다");
      setTeamOrService("");
      setCapacity("");
      setDemand("");
      load();
    } catch (err) {
      setError(extractErrorMessage(err, "등록에 실패했습니다."));
    } finally {
      setSubmitting(false);
    }
  };

  const columns: Column<CapacityPlan>[] = [
    { header: "팀/서비스", cell: (p) => p.teamOrService },
    { header: "역량", cell: (p) => p.capacity },
    { header: "예상 수요", cell: (p) => p.demand },
    {
      header: "활용률",
      cell: (p) => {
        const percent = p.utilizationRate * 100;
        return <StatusBadge tone={utilizationTone(percent)} label={`${Math.round(percent)}%`} />;
      },
    },
  ];

  return (
    <div className="space-y-4">
      <h1 className="text-xl font-semibold text-foreground">용량 계획 관리</h1>

      <Card>
        <CardHeader>
          <CardTitle className="text-base">용량 계획 등록</CardTitle>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit} className="flex flex-wrap items-end gap-3">
            <div className="flex-1 min-w-[160px] space-y-1.5">
              <Label htmlFor="teamOrService">팀/서비스명</Label>
              <Input id="teamOrService" value={teamOrService} onChange={(e) => setTeamOrService(e.target.value)} />
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="capacity">역량</Label>
              <Input id="capacity" type="number" value={capacity} onChange={(e) => setCapacity(e.target.value)} className="w-32" />
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="demand">예상 수요</Label>
              <Input id="demand" type="number" value={demand} onChange={(e) => setDemand(e.target.value)} className="w-32" />
            </div>
            <Button type="submit" loading={submitting}>등록</Button>
          </form>
          {error ? <p role="alert" className="mt-2 text-sm text-danger">{error}</p> : null}
        </CardContent>
      </Card>

      <DataTable
        columns={columns}
        data={plans}
        rowKey={(p) => p.id}
        loading={loading}
        emptyTitle="용량 계획이 없습니다"
        emptyDescription="등록된 팀/서비스 용량 계획이 없습니다."
      />
    </div>
  );
}
