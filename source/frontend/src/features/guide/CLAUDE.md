# CLAUDE.md

사용자 가이드 전용 화면(SCR-COM-012 v0.8) 기능. 헤더 "?" 버튼 클릭 시 이동하는 `/guide` 라우트로, 신규 API 없이 정적 콘텐츠(`components/common/user-guide-content.tsx`)를 Confluence 문서 스타일로 렌더링한다.

## 파일
- `GuidePage.tsx` — 사용자 가이드 화면(SCR-COM-012). 좌측 sticky TOC(개요/도메인 및 원칙/역할별 수행 내용과 방법 3개 링크, `IntersectionObserver`로 스크롤 위치에 따라 활성 링크 강조) + 우측 본문(`UserGuideOverview`→`UserGuideDomainSection`→`UserGuideRoleSection` 순, 로그인 사용자 역할은 `useAppSelector`로 조회해 `UserGuideRoleSection`에 전달).
