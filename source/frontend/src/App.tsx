import { RouterProvider } from "react-router-dom";

import { router } from "@/routes";

/** 앱 루트 — 라우터 진입점. 전역 Provider/Toaster는 main.tsx에서 구성한다. */
export default function App() {
  return <RouterProvider router={router} />;
}
