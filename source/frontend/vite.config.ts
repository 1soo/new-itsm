import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import tailwindcss from "@tailwindcss/vite";
import path from "node:path";

export default defineConfig({
  plugins: [react(), tailwindcss()],
  resolve: {
    alias: {
      "@": path.resolve(__dirname, "./src"),
    },
  },
  server: {
    port: 5173,
    // 쿠키 기반 refresh를 same-origin으로 처리하기 위한 dev proxy (cross-site Secure 이슈 회피)
    proxy: {
      "/api": { target: "http://localhost:8080", changeOrigin: true },
    },
  },
});
