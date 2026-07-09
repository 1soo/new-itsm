import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { useAppSelector } from "@/store/hooks";

/*
 * 대시보드 placeholder — auth 단계에는 대시보드 화면이 설계에 없으므로
 * 로그인 후 기본 홈으로서 최소 환영 화면만 제공한다. 도메인 대시보드는 이후 단계에서 확장.
 */
export function DashboardPage() {
  const user = useAppSelector((s) => s.auth.user);

  return (
    <div className="mx-auto max-w-3xl">
      <Card>
        <CardHeader>
          <CardTitle>환영합니다{user ? `, ${user.name}님` : ""}</CardTitle>
        </CardHeader>
        <CardContent className="text-sm text-muted-foreground">
          ITSM 플랫폼에 로그인했습니다. 좌측 메뉴에서 이용할 기능을 선택하세요.
        </CardContent>
      </Card>
    </div>
  );
}
