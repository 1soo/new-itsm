import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import { Provider } from "react-redux";

import { Toaster } from "@/components/ui/sonner";
import { store } from "@/store";
import App from "@/App";
import "./index.css";

/*
 * 진입점 — Redux Provider로 전역 상태를 주입하고 라우터 App을 렌더한다.
 * Toaster(공통 토스트 컨테이너, dev-ui 소유)는 공통 인프라이므로 여기서 마운트한다.
 */
createRoot(document.getElementById("root")!).render(
  <StrictMode>
    <Provider store={store}>
      <App />
      <Toaster />
    </Provider>
  </StrictMode>,
);
