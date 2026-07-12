# 통합 테스트 결과 — service-request (SRM) (20260709-122918)

> 대상: SRM 도메인 · BE(:8080)/FE dev(:5173)/PostgreSQL(itsm-postgres) · React CSR + Spring Boot
> 역할 계정(SYSTEM_ADMIN이 생성): requester(END_USER)/agent(SERVICE_DESK_AGENT)/approver(APPROVER)/owner(PROCESS_OWNER), pw Init@1234
> baseline: 기본 큐(미분류/IT 서비스) + 데모 카탈로그 2건(노트북 신청=승인필요/approver_role APPROVER, 비밀번호 초기화=승인불필요)

## 요약

- 총 54건 · 성공 53 · 실패 1
- 실패: **TC-APR-008** — 이미 결정된 승인의 재처리가 409가 아니라 200이며 요청 상태를 오염시킴(워크플로우 무결성 결함).
- 그 외 카탈로그/요청/큐/상태전이/승인(정상·반려·권한·409대기)/코멘트/CSAT/지표/배정/RBAC/E2E 전부 통과.

## 상세

### A. 빌드
| TC | 결과 | 비고 |
|----|------|------|
| TC-BUILD-001 | PASS | BE `gradlew test` BUILD SUCCESSFUL |
| TC-BUILD-002 | PASS | FE dev 기동/빌드 정상(SPA 200) |

### B. 카탈로그 (API-SRM-001~004)
| TC | 결과 | 실제 |
|----|------|------|
| TC-CAT-001 목록 | PASS | 200 |
| TC-CAT-002 상세(formSchema)/404 | PASS | 200+formSchema / 없음 404 |
| TC-CAT-003 생성 PROCESS_OWNER | PASS | 201 |
| TC-CAT-004 이름·양식 누락 | PASS | 400 |
| TC-CAT-005 비-owner 생성 | PASS | 403 |
| TC-CAT-006 수정 owner/비-owner | PASS | 200 / 403 |

### C. 지식 추천 (API-SRM-005)
| TC-KN-001 | PASS | 200, 빈 배열 `[]`(KM 미구축, 정상) |

### D. 큐 (API-SRM-016)
| TC-QUEUE-001 Agent | PASS | 200 [{id,name,isDefault,openCount}] |
| TC-QUEUE-002 END_USER | PASS | 403 |

### E. 요청 제출/조회 (API-SRM-006~008)
| TC | 결과 | 실제 |
|----|------|------|
| TC-REQ-001 생성 | PASS | 201, ticketKey=SRM-2026-#### , SUBMITTED |
| TC-REQ-002 필수필드 누락 | PASS | 400 |
| TC-REQ-003 유효하지 않은 카탈로그 | PASS | 400 |
| TC-REQ-004 scope=mine | PASS | 본인 요청 포함 |
| TC-REQ-005 scope=all Agent/END_USER | PASS | 200 / 403 |
| TC-REQ-006 ?queue={id} 필터(Agent) | PASS | 200 |
| TC-REQ-007 상세(본인) | PASS | 200, linkedArticles=[]·sla·approval·allowedTransitions 포함 |
| TC-REQ-008 상세 무권한/없음 | PASS | 타 END_USER 403 / 없음 404 |

### F. 상태 전이 (API-SRM-010)
| TC | 결과 | 실제 |
|----|------|------|
| TC-ST-001 정상 전이 경로(승인불필요) | PASS | SUBMITTED→VALIDATED→ROUTED→IN_FULFILLMENT→FULFILLED→CLOSED 모두 200 |
| TC-ST-002 허용되지 않은 전이 | PASS | SUBMITTED→IN_FULFILLMENT 400 |
| TC-ST-003 권한 없는 전이 | PASS | END_USER VALIDATED 403 |
| TC-ST-004 종료 재전이 | PASS | 400 |
| TC-ST-005 요청자 종료(FULFILLED→CLOSED) | PASS | 200 |

### G. 승인 (API-SRM-011~012)
| TC | 결과 | 실제 |
|----|------|------|
| TC-APR-001 ROUTED→APPROVAL_PENDING 전환·Approval 생성 | PASS | status APPROVAL_PENDING, approval PENDING(approver_role APPROVER) |
| TC-APR-002 승인대기 중 이행 | PASS | 409 |
| TC-APR-003 approver_role 미보유 승인 | PASS | END_USER 403 / AGENT 403 |
| TC-APR-004 반려 사유 누락 | PASS | 400 |
| TC-APR-005 APPROVER 승인 | PASS | 200, status ROUTED, approval APPROVED |
| TC-APR-006 승인 후 이행→종료 | PASS | IN_FULFILLMENT→FULFILLED→CLOSED 200 |
| TC-APR-007 반려 | PASS | 200, status REJECTED, 이후 전이 400(terminal) |
| **TC-APR-008 이미 결정된 승인 재처리** | **FAIL** | **200(기대 409). 재-APPROVE 시 IN_FULFILLMENT→ROUTED로 되돌림, 재-REJECT 시 REJECTED(terminal)로 뒤집힘 — 상태 오염** |
| TC-APR-009 승인 대기 목록 | PASS | APPROVER 역할 공유함 200(PENDING 포함) / END_USER 403 |

### H. 코멘트 (API-SRM-013)
| TC-CM-001 요청자 등록 | PASS | 201, 상세 comments 반영 |
| TC-CM-002 무권한 등록 | PASS | 403 |

### I. CSAT (API-SRM-014)
| TC-CSAT-001 종료 요청 요청자 제출 | PASS | 200 |
| TC-CSAT-002 미종료 요청 | PASS | 400 |
| TC-CSAT-003 중복 | PASS | 409 |
| TC-CSAT-004 요청자 아님 | PASS | 403 |

### J. 지표 (API-SRM-015)
| TC-MET-001 PROCESS_OWNER | PASS | 200 {csatAvg,avgResponseMinutes,avgResolveMinutes,slaComplianceRate} |
| TC-MET-002 AGENT | PASS | 403 |

### K. 배정 (API-SRM-009)
| TC-ASG-001 Agent 본인 배정 | PASS | 200 |
| TC-ASG-002 END_USER 배정 | PASS | 403 |
| TC-ASG-003 없는 사용자/요청 | PASS | 404 / 404 |

### L. 인증
| TC-AUTH-001 미인증 SRM API | PASS | catalog 401 / requests 401 |

### M. FE E2E (playwright, 매 항목 새 context, Chrome) — 9/9 PASS
| TC | 결과 | 증적 |
|----|------|------|
| TC-E2E-001 포털→동적양식 제출→상세 | PASS | shots/srm-e2e-001-submit.png |
| TC-E2E-002 내 요청 목록 | PASS | shots/srm-e2e-002-mylist.png |
| TC-E2E-003 상담원 요청 큐(큐 목록) | PASS | shots/srm-e2e-003-queue.png |
| TC-E2E-004 요청 상세 + 코멘트 등록 | PASS | shots/srm-e2e-004-detail.png |
| TC-E2E-005 승인 대기함 | PASS | shots/srm-e2e-005-approvals.png |
| TC-E2E-006 상세 CSAT 섹션(종료 요청) | PASS | shots/srm-e2e-006-csat.png |
| TC-E2E-007 카탈로그 관리 | PASS | shots/srm-e2e-007-catalog.png |
| TC-E2E-008 지표 대시보드 | PASS | shots/srm-e2e-008-metrics.png |
| TC-E2E-009 RBAC(요청자→상담원 큐 차단) | PASS | /403 리다이렉트, shots/srm-e2e-009-rbac.png |

## 실패 항목 분석

### 결함 (HIGH) — TC-APR-008 : 이미 결정된 승인 재처리 미차단(409 누락) + 상태 오염
- **증상**: 이미 APPROVED/REJECTED된 승인 건에 대해 `POST /service-requests/{id}/approval`를 다시 호출하면 200이 반환되고 결정이 덮어써진다. 실측:
  - 승인 후 IN_FULFILLMENT까지 진행된 요청에 재-APPROVE → 200, 요청 상태가 **IN_FULFILLMENT → ROUTED로 되돌아감**.
  - 이어 재-REJECT → 200, 요청이 **REJECTED(종료)로 뒤집힘**.
- **근본원인**: `ServiceRequestService.decideApproval()`에 승인 상태가 이미 PENDING이 아닐 때의 가드가 없음(`approval.isPending()` 미확인). 관련 `ErrorCode`에 "이미 결정됨" 409 코드 자체가 없음. `ServiceRequest.changeStatus()`도 무조건 상태를 덮어써서, 진행 중 요청이 임의로 되돌려짐.
- **영향**: API-SRM-011 명세("409 이미 결정됨") 위반, REQ-SRM-005/FEAT-SRM-005. approver_role 보유자가 이미 처리된 승인을 재요청해 이행 중 요청을 강제로 되돌리거나 반려로 뒤집을 수 있어 **워크플로우 무결성 훼손**.
- **권장수정**: `decideApproval()` 진입 시 `if (!approval.isPending()) throw new BusinessException(APPROVAL_ALREADY_DECIDED)`(409) 추가. ErrorCode에 APPROVAL_ALREADY_DECIDED(409) 신설. 통합테스트(실 DB)로 재-결정 차단 검증 추가 권장.

## 비고
- baseline·잔여 데이터로 상대 검증(생성 후 포함) 수행, 절대 개수 assert 미사용.
- linkedArticles/지식추천은 빈 배열([])이 정상(KM 미구축) — 확인함.
- 동시 세션 제약(계정당 access_token_jti 1개)에 맞춰 각 역할 계정 1회 로그인 세션으로 수행.
</content>
