---
date: 20260716-125723
domain: incident
change_type: [new, modified]
keywords: [상태전이버튼라벨, 타임라인actor표시, 상태라벨유틸]
---

# 유지보수 이력 — incident

> 유지보수 일시: 20260716-125723 · 도메인: incident

## 1. 요구사항

상태 변경 버튼의 문구가 도착 상태명으로만 표시되어 있어, 이를 실제 수행하는 행위에 맞는 동사형 문구로 변경해야 한다.
타임라인에 나타나는 상태 변경 표시가 코드가 아닌 사람이 읽을 수 있는 이름으로 나타나야 한다.
타임라인의 각 항목에 그 행위를 수행한 주체자가 표시되어야 한다.

## 2. 해결 방법

상태 변경 버튼 문구는 `features/incident/status.ts`에 `transitionLabel()`을 신규 추가해 동작 동사형으로 전환했다.
`IncidentDetailPage.tsx`의 전이 버튼 라벨을 `transitionLabel()`로 교체했다.
상태 변경 완료 토스트 문구는 기존 `statusLabel()`을 그대로 유지했다.
타임라인 코드→이름 전환을 위해 백엔드 `IncidentStatus`에 한글 상태 라벨을 반환하는 `label()`을 신규 추가했다.
상태 라벨을 홑따옴표+받침 유무에 맞는 조사(로/으로)로 조립하는 공용 유틸 `common.ticket.TimelineMessages.quotedWithParticle()`(SRM/ESM/INCIDENT 공용, srm 배치에서 신설)을 `IncidentService`의 상태 전이 타임라인 저장 로직에서 사용하도록 했다.
타임라인 행위 주체자 표시를 위해 `IncidentDetailResponse.TimelineEntry`에 `actor` 필드를 추가했다.
`TimelineEvent`의 `createdBy`(인증 사용자 email)를 표시 이름으로 변환하는 공용 default 메서드 `AppUserRepository.resolveDisplayName(email)`(srm 배치에서 신설)을 재사용해 `IncidentService`의 타임라인 저장 시 actor를 채웠다.

## 3. 변경 파일

- `source/frontend/src/features/incident/status.ts`
- `source/frontend/src/features/incident/IncidentDetailPage.tsx`
- `source/backend/src/main/java/com/itsm/incident/domain/IncidentStatus.java`
- `source/backend/src/main/java/com/itsm/incident/application/IncidentService.java`
- `source/backend/src/main/java/com/itsm/incident/application/dto/IncidentDetailResponse.java`

## 4. 테스트 결과

통합 테스트 결과는 `docs/04_test/20260716-080731`에 기록되어 있으며 PASS했다.
커밋 `1759a25`로 반영했다.
