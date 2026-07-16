# Infra Architecture — {AWS / Azure}

> CSP: {aws / azure} · 버전: 0.1 · 작성일: {YYYY-MM-DD}
> 로드밸런서 사용: {LB(AWS) / App Gateway(Azure) 사용 여부}

## 변경 이력

| 날짜 | 요약 |
|------|------|
| {YYYY-MM-DD} | 최초 작성 |

## 1. 개요

{배포 환경과 아키텍처 목적을 1~2문장으로 기술}

## 2. 구성 요소

| 계층 | 리소스 | 설명 |
|------|--------|------|
| 최전방 (public) | {LB / App Gateway} | public open, TLS/SSL 인증 |
| Frontend | nginx | `/api` → Backend reverse proxy (CSR) |
| Backend | {App 서버} | Frontend 요청만 수신 |
| Database | {RDS / SQL Server} | Backend 요청만 수신 |

## 3. 트래픽 흐름

```
Client (HTTPS)
  → [LB / App Gateway]  (public, TLS/SSL 종료)
  → Frontend (nginx)    (/api → reverse proxy)
  → Backend             (SG/NSG: Frontend 만 허용)
  → Database            (SG/NSG: Backend 만 허용)
```

## 4. 네트워크 보안 (Security Group / NSG)

| 리소스 | 인바운드 허용 | 비고 |
|--------|---------------|------|
| 최전방 | 0.0.0.0/0 (443) | public, TLS/SSL |
| Backend | Frontend 만 | |
| Database | Backend 만 | |

## 5. 리소스 목록

- {리소스명} — {용도}
