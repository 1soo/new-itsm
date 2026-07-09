# CLAUDE.md

Redux Toolkit 전역 상태. 앱 전역에서 공유하는 인증 상태만 보관한다(Access Token 자체는 apiClient의 Session Storage가 단일 원천).

## 파일
- `index.ts` — 스토어 구성(`configureStore`)과 `RootState`/`AppDispatch` 타입.
- `authSlice.ts` — 인증 슬라이스. 상태(status/user)와 thunk(`bootstrapAuth`/`login`/`logout`), `sessionExpired` 액션.
- `hooks.ts` — 타입 지정 Redux 훅(`useAppDispatch`/`useAppSelector`). 앱 전역에서 이 훅만 사용.
