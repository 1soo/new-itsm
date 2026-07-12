# 유지보수 이력 — common

> 유지보수 일시: 20260712-180448 · 도메인: common

## 1. 요구사항

다국어 지원 기능이 추가되어야 한다.
i18n 라이브러리를 사용하여 한국어와 영어 버전 전환이 가능해야 한다.
언어 선택은 우측 상단 "?"(가이드) 아이콘과 알림 아이콘 사이의 지구본 아이콘을 통해 가능해야 하고, 아이콘 클릭 시 바로 아래에 언어 선택 미니 팝업이 렌더링되어야 한다.
모든 화면에 대해 다국어 전환이 적용되어야 하고, 알림 메시지도 언어 전환이 되어야 한다.
Frontend에 SweetAlert2를 도입하고, 적용 가능한 부분에는 모두 적용해야 한다.
(확정) SweetAlert2 적용 범위는 확인/취소류(ConfirmDialog)와 성공/오류/정보 알림(toast)까지로 한정하고, 폼/상세 등 복잡한 콘텐츠를 담는 Modal은 대상에서 제외한다.
(확정) 날짜/숫자 포맷은 이번 범위에서 현지화하지 않고 텍스트(라벨·메시지)만 번역한다.
(확정) 언어 선택은 localStorage에 저장하고 기본값은 한국어로 한다.
(확정) 사용자 가이드(SCR-COM-012) 화면은 UI chrome뿐 아니라 본문 콘텐츠(11개 도메인 설명 + 16개 역할 페르소나)까지 번역 대상에 포함한다.

## 2. 해결 방법

i18n 코어 인프라를 신규 구축했다(`source/frontend/src/i18n/index.ts`, `language.ts`, `locales/ko`·`locales/en` 12개 네임스페이스).
헤더의 "?" 아이콘과 알림 벨 아이콘 사이에 지구본 아이콘과 언어 선택 미니 팝업(`language-toggle.tsx`, SCR-COM-015)을 신규 구현해, 클릭 시 한국어/English 즉시 전환과 `localStorage`(`itsm-language`) 저장을 지원하도록 했다.
`toast.ts`와 `confirm-dialog.tsx`의 내부 구현을 SweetAlert2로 교체했다(외부 API는 그대로 유지해 83개 이상 기존 호출부는 무변경).
`modal.tsx`는 결정에 따라 SweetAlert2 적용 대상에서 제외하고 기존 Radix Dialog 구현을 유지했다.
sonner 패키지와 관련 컴포넌트(`components/ui/sonner.tsx`)를 제거해 SweetAlert2로 완전히 대체했다.
사용자 가이드(SCR-COM-012) 영어 본문을 신규 작성해(`user-guide-content.en.md`) 언어별로 렌더링되도록 구현했다.
그 외 `header.tsx`, `theme-toggle.tsx`, `forbidden-view.tsx`, `not-found-view.tsx`, `AppLayout.tsx`, `approval-step-progress.tsx`, `approval-panel.tsx`, `dialog.tsx`(sr-only 텍스트), `ApprovalInboxPage.tsx`, `features/search` 관련 화면, `DashboardPage.tsx`, `GuidePage.tsx`(chrome), `SessionBridge.tsx`, `pagination.tsx`, `multi-select.tsx`, `field-builder.tsx`, `rating.tsx`, `form-schema.ts`의 하드코딩 텍스트를 번역 키로 전환했다.
알림 메시지(`AppLayout.tsx`에서 조립되는 `domainLabel`/`text`)는 FE에서만 조립되는 구조임을 확인하고 BE/DB 변경 없이 FE 라벨 매핑만 번역 키로 전환했다.
날짜/숫자 포맷은 확정 결정에 따라 언어 전환과 무관하게 `ko-KR` 고정을 유지했다.
개발 중 결함 2건(테마 토글 aria-label 미전환, 승인 단계 진행 현황 미전환)을 발견해 수정 후 재검증했다.

## 3. 변경 파일

- `source/frontend/src/i18n/index.ts`(신규)
- `source/frontend/src/i18n/language.ts`(신규)
- `source/frontend/src/i18n/locales/ko/*`(신규, 12개 네임스페이스)
- `source/frontend/src/i18n/locales/en/*`(신규, 12개 네임스페이스)
- `source/frontend/src/components/layout/language-toggle.tsx`(신규)
- `source/frontend/src/components/layout/header.tsx`
- `source/frontend/src/components/layout/theme-toggle.tsx`
- `source/frontend/src/components/common/toast.ts`
- `source/frontend/src/components/common/confirm-dialog.tsx`
- `source/frontend/src/components/common/forbidden-view.tsx`
- `source/frontend/src/components/common/not-found-view.tsx`
- `source/frontend/src/components/common/approval-step-progress.tsx`
- `source/frontend/src/components/common/approval-panel.tsx`
- `source/frontend/src/components/common/pagination.tsx`
- `source/frontend/src/components/common/multi-select.tsx`
- `source/frontend/src/components/common/field-builder.tsx`
- `source/frontend/src/components/common/rating.tsx`
- `source/frontend/src/components/common/form-schema.ts`
- `source/frontend/src/components/common/user-guide-content.tsx`
- `source/frontend/src/components/common/user-guide-content.en.md`(신규)
- `source/frontend/src/components/ui/dialog.tsx`
- `source/frontend/src/components/ui/sonner.tsx`(제거)
- `source/frontend/src/routes/AppLayout.tsx`
- `source/frontend/src/routes/SessionBridge.tsx`
- `source/frontend/src/routes/DashboardPage.tsx`
- `source/frontend/src/features/common/ApprovalInboxPage.tsx`
- `source/frontend/src/features/search/*`
- `source/frontend/src/features/guide/GuidePage.tsx`(chrome)
- `source/frontend/package.json`(sonner 제거, sweetalert2 추가)

## 4. 테스트 결과

통합 테스트 22건 전부 PASS했다(재테스트 포함).
1차 테스트에서 발견된 결함 2건(테마 토글 aria-label 미전환, 승인 단계 진행 현황 미전환)을 수정 후 재검증해 PASS 처리했다.
커밋 `bc66f6c`, 재테스트 반영 커밋 `78eeead` 이전 시점(v3절)으로 반영했다.
