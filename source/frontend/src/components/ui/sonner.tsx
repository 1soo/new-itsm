import { Toaster as Sonner, type ToasterProps } from "sonner";

/**
 * 토스트 컨테이너 — common.md SCR-COM-009.
 * 우상단 스택. 성공(Success)·오류(Danger) 시맨틱 색상은 toast helper에서 지정한다.
 */
function Toaster(props: ToasterProps) {
  return (
    <Sonner
      position="top-right"
      toastOptions={{
        classNames: {
          toast:
            "group border border-border bg-popover text-popover-foreground shadow-overlay rounded-lg",
          description: "text-muted-foreground",
          actionButton: "bg-primary text-primary-foreground",
          cancelButton: "bg-muted text-muted-foreground",
        },
      }}
      {...props}
    />
  );
}

export { Toaster };
