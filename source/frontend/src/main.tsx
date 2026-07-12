import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import { Provider } from "react-redux";

import { store } from "@/store";
import App from "@/App";
import "@/i18n";
import "./index.css";

/*
 * 진입점 — Redux Provider로 전역 상태를 주입하고 라우터 App을 렌더한다.
 * 토스트 컨테이너는 SweetAlert2가 자체 DOM으로 렌더링하므로 별도 마운트가 필요 없다
 * (common.md SCR-COM-009, 2026-07-12 SweetAlert2 도입).
 */
createRoot(document.getElementById("root")!).render(
  <StrictMode>
    <Provider store={store}>
      <App />
    </Provider>
  </StrictMode>,
);
