# Infra Architecture 설계 컨벤션

인프라 아키텍처 구조를 **직관적이고 간결한** markdown으로 설계한다.

## 수행 조건

- **AWS / Azure 같은 CSP 환경일 때만** 아래 프로세스를 수행한다.
- **local 환경이면 진행하지 않는다.**

## 설계 규칙

1. **로드밸런서 사용 여부 확인**: AWS는 **Load Balancer**, Azure는 **Application Gateway** 사용 여부를 사용자에게 확인한다.
2. **최전방 리소스**: 워크로드 최전방 리소스는 public open, **TLS/SSL 인증**을 수행한다.
3. **Reverse Proxy (CSR 구조)**: Frontend는 **nginx**로 `/api` 요청을 Backend로 reverse proxy한다.
4. **네트워크 격리 (Security Group / Network Security Group)**:
   - Backend는 **Frontend의 요청만** 받는다.
   - Database는 **Backend의 요청만** 받는다.
5. **Database**: 종류에 맞는 관리형 DB를 사용한다 — AWS **RDS** / Azure **SQL Server** 등.

## 양식

표준 양식은 [template.md](template.md)를 사용한다.
