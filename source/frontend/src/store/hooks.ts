import { useDispatch, useSelector } from "react-redux";

import type { AppDispatch, RootState } from "@/store";

/** 타입이 지정된 Redux 훅 — 앱 전역에서 이 훅만 사용한다. */
export const useAppDispatch = useDispatch.withTypes<AppDispatch>();
export const useAppSelector = useSelector.withTypes<RootState>();
