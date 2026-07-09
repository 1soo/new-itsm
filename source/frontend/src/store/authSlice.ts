import { createAsyncThunk, createSlice } from "@reduxjs/toolkit";

import { authApi } from "@/features/auth/api";
import type { AuthUser, LoginRequest } from "@/features/auth/types";
import {
  clearAccessToken,
  extractErrorMessage,
  getStatusCode,
  setAccessToken,
} from "@/lib/apiClient";

/** 로그인 실패 시 컴포넌트가 상태코드로 메시지를 결정할 수 있도록 전달. */
export interface LoginError {
  status?: number;
  message: string;
}

/*
 * 인증 전역 상태 — 앱 전역에서 공유하는 사용자·역할·세션 상태만 보관한다.
 * Access Token 자체는 Session Storage(apiClient)가 단일 원천이며, 여기서는
 * 인증 여부(status)와 사용자/역할(user)만 다룬다.
 */

export type AuthStatus = "idle" | "loading" | "authenticated" | "unauthenticated";

interface AuthState {
  status: AuthStatus;
  user: AuthUser | null;
}

const initialState: AuthState = {
  status: "idle",
  user: null,
};

/** 앱 진입 시 세션 복구: /auth/me 조회(만료 시 apiClient가 쿠키로 1회 재발급). */
export const bootstrapAuth = createAsyncThunk<AuthUser>(
  "auth/bootstrap",
  async (_, { rejectWithValue }) => {
    try {
      const me = await authApi.me();
      return me;
    } catch (error) {
      return rejectWithValue(extractErrorMessage(error));
    }
  },
);

export const login = createAsyncThunk<AuthUser, LoginRequest, { rejectValue: LoginError }>(
  "auth/login",
  async (body, { rejectWithValue }) => {
    try {
      const res = await authApi.login(body);
      setAccessToken(res.accessToken);
      return res.user;
    } catch (error) {
      return rejectWithValue({
        status: getStatusCode(error),
        message: extractErrorMessage(error),
      });
    }
  },
);

export const logout = createAsyncThunk<void>("auth/logout", async () => {
  try {
    await authApi.logout();
  } finally {
    clearAccessToken();
  }
});

const authSlice = createSlice({
  name: "auth",
  initialState,
  reducers: {
    /** apiClient 재발급 실패 등으로 세션이 만료됐을 때 호출. */
    sessionExpired(state) {
      clearAccessToken();
      state.status = "unauthenticated";
      state.user = null;
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(bootstrapAuth.pending, (state) => {
        state.status = "loading";
      })
      .addCase(bootstrapAuth.fulfilled, (state, action) => {
        state.status = "authenticated";
        state.user = action.payload;
      })
      .addCase(bootstrapAuth.rejected, (state) => {
        state.status = "unauthenticated";
        state.user = null;
      })
      .addCase(login.fulfilled, (state, action) => {
        state.status = "authenticated";
        state.user = action.payload;
      })
      .addCase(logout.fulfilled, (state) => {
        state.status = "unauthenticated";
        state.user = null;
      })
      .addCase(logout.rejected, (state) => {
        state.status = "unauthenticated";
        state.user = null;
      });
  },
});

export const { sessionExpired } = authSlice.actions;
export default authSlice.reducer;
