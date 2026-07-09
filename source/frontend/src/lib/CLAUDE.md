# CLAUDE.md

공통 유틸과 HTTP 클라이언트. 모든 API 요청은 여기의 apiClient를 경유한다(직접 fetch/axios 금지).

## 파일
- `apiClient.ts` — 공통 axios 인스턴스. Access Token(Session Storage) 자동 주입, 401 시 `/auth/refresh`로 1회 재발급(single-flight) 후 실패 시 세션 종료 핸들러 호출. Token 저장소 함수, 표준 오류 메시지/상태코드 추출 헬퍼 포함.
- `utils.ts` — shadcn 표준 className 병합 유틸(`cn`, clsx + tailwind-merge).
