import axios, {
  AxiosError,
  AxiosHeaders,
  type AxiosInstance,
  type InternalAxiosRequestConfig,
} from "axios";

/*
 * 공통 apiClient — react 컨벤션/인증 설계(authentication.md) 준수.
 * - Base: VITE_API_BASE_URL(기본 "/api/v1", Vite dev proxy로 same-origin 처리).
 * - Access Token은 Session Storage에 저장, 요청 시 Authorization: Bearer 자동 주입.
 * - 401 응답 시 POST /auth/refresh(httpOnly Cookie)로 1회 재발급 재시도 → 실패 시
 *   세션 종료 핸들러 호출(로그인 이동 + 토스트). (SCR-COM-005)
 * - Refresh Token은 httpOnly Cookie로만 전송하므로 withCredentials로 요청한다.
 */

const baseURL = import.meta.env.VITE_API_BASE_URL || "/api/v1";

const ACCESS_TOKEN_KEY = "itsm.accessToken";

/** 재발급을 시도하지 않는 인증 엔드포인트(무한 루프 방지). */
const AUTH_SKIP_REFRESH = ["/auth/login", "/auth/refresh", "/auth/logout"];

// --- Access Token 저장소 (Session Storage) ---
export function getAccessToken(): string | null {
  return sessionStorage.getItem(ACCESS_TOKEN_KEY);
}

export function setAccessToken(token: string): void {
  sessionStorage.setItem(ACCESS_TOKEN_KEY, token);
}

export function clearAccessToken(): void {
  sessionStorage.removeItem(ACCESS_TOKEN_KEY);
}

// --- 세션 만료 핸들러 (앱이 등록: 로그인 이동 + 토스트) ---
type SessionExpiredHandler = () => void;
let sessionExpiredHandler: SessionExpiredHandler | null = null;

export function setSessionExpiredHandler(handler: SessionExpiredHandler | null): void {
  sessionExpiredHandler = handler;
}

// --- 표준 오류 응답({code,message,timestamp}) → 사용자 메시지 정규화 ---
interface StandardError {
  code?: string;
  message?: string;
  timestamp?: string;
}

export function extractErrorMessage(error: unknown, fallback = "요청 처리 중 오류가 발생했습니다"): string {
  if (error instanceof AxiosError) {
    const data = error.response?.data as StandardError | undefined;
    if (data?.message) return data.message;
    if (error.message) return error.message;
  }
  if (error instanceof Error && error.message) return error.message;
  return fallback;
}

export function getStatusCode(error: unknown): number | undefined {
  return error instanceof AxiosError ? error.response?.status : undefined;
}

export const apiClient: AxiosInstance = axios.create({
  baseURL,
  withCredentials: true,
  headers: { "Content-Type": "application/json" },
});

// 요청 인터셉터: Access Token을 Authorization 헤더에 주입.
apiClient.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const token = getAccessToken();
  if (token) {
    const headers = AxiosHeaders.from(config.headers);
    headers.set("Authorization", `Bearer ${token}`);
    config.headers = headers;
  }
  return config;
});

// --- 재발급 단일화(single-flight): 동시 401 다발 시 refresh 1회만 수행 ---
let refreshPromise: Promise<string> | null = null;

async function doRefresh(): Promise<string> {
  // 인터셉터 재귀를 피하기 위해 apiClient가 아닌 bare axios로 호출.
  const res = await axios.post<{ accessToken: string }>(
    `${baseURL}/auth/refresh`,
    null,
    { withCredentials: true },
  );
  const token = res.data.accessToken;
  setAccessToken(token);
  return token;
}

function refreshOnce(): Promise<string> {
  if (!refreshPromise) {
    refreshPromise = doRefresh().finally(() => {
      refreshPromise = null;
    });
  }
  return refreshPromise;
}

// 응답 인터셉터: 401 → refresh 1회 재시도 → 실패 시 세션 종료.
apiClient.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const original = error.config as
      | (InternalAxiosRequestConfig & { _retry?: boolean })
      | undefined;

    const status = error.response?.status;
    const url = original?.url ?? "";
    const isAuthEndpoint = AUTH_SKIP_REFRESH.some((p) => url.includes(p));

    if (status === 401 && original && !original._retry && !isAuthEndpoint) {
      original._retry = true;
      try {
        await refreshOnce();
        return apiClient(original);
      } catch {
        clearAccessToken();
        sessionExpiredHandler?.();
        return Promise.reject(error);
      }
    }

    return Promise.reject(error);
  },
);
