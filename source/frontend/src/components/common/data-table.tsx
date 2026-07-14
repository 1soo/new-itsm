import type { ReactNode } from "react";

import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { Skeleton } from "@/components/ui/skeleton";
import { EmptyState } from "@/components/common/empty-state";
import { cn } from "@/lib/utils";

/**
 * 공통 데이터 표 — common.md SCR-COM-007 목록 표.
 * 로딩 시 스켈레톤, 0건 시 빈 상태. 행 클릭 시 상세 이동(onRowClick).
 * 컬럼 렌더링은 제네릭 accessor로 정의하여 도메인 무관하게 재사용한다.
 */
export interface Column<T> {
  /** 헤더 라벨 */
  header: string;
  /** 셀 렌더러 */
  cell: (row: T) => ReactNode;
  /** 헤더/셀 정렬 클래스 등 */
  className?: string;
  /** 컬럼 고정 폭(px). 미지정 시 잔여 폭을 흡수(auto). */
  width?: number;
}

export interface DataTableProps<T> {
  columns: Column<T>[];
  data: T[];
  rowKey: (row: T) => string | number;
  loading?: boolean;
  onRowClick?: (row: T) => void;
  emptyTitle?: string;
  emptyDescription?: string;
  /** 로딩 스켈레톤 행 수 */
  skeletonRows?: number;
  className?: string;
}

export function DataTable<T>({
  columns,
  data,
  rowKey,
  loading = false,
  onRowClick,
  emptyTitle,
  emptyDescription,
  skeletonRows = 8,
  className,
}: DataTableProps<T>) {
  return (
    <div className={cn("overflow-hidden rounded-lg border border-border", className)}>
      <Table className="table-fixed">
        <colgroup>
          {columns.map((col, i) =>
            col.width ? (
              <col key={i} style={{ width: col.width, minWidth: col.width, maxWidth: col.width }} />
            ) : (
              <col key={i} />
            ),
          )}
        </colgroup>
        <TableHeader>
          <TableRow className="hover:bg-transparent">
            {columns.map((col, i) => (
              <TableHead key={i} className={col.className}>
                {col.header}
              </TableHead>
            ))}
          </TableRow>
        </TableHeader>
        <TableBody>
          {loading ? (
            Array.from({ length: skeletonRows }).map((_, r) => (
              <TableRow key={`sk-${r}`} className="hover:bg-transparent">
                {columns.map((_, c) => (
                  <TableCell key={c}>
                    <Skeleton className="h-4 w-full max-w-[160px]" />
                  </TableCell>
                ))}
              </TableRow>
            ))
          ) : data.length === 0 ? (
            <TableRow className="hover:bg-transparent">
              <TableCell colSpan={columns.length} className="p-0">
                <EmptyState title={emptyTitle} description={emptyDescription} />
              </TableCell>
            </TableRow>
          ) : (
            data.map((row) => (
              <TableRow
                key={rowKey(row)}
                onClick={onRowClick ? () => onRowClick(row) : undefined}
                className={cn(onRowClick && "cursor-pointer")}
                tabIndex={onRowClick ? 0 : undefined}
                onKeyDown={
                  onRowClick
                    ? (e) => {
                        if (e.key === "Enter") onRowClick(row);
                      }
                    : undefined
                }
              >
                {columns.map((col, c) => (
                  <TableCell key={c} className={col.className}>
                    {col.cell(row)}
                  </TableCell>
                ))}
              </TableRow>
            ))
          )}
        </TableBody>
      </Table>
    </div>
  );
}
