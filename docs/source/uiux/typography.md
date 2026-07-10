# 타이포그래피 (Typography)

> **제품 UI는 "Atlassian Sans"(본문/제목) + "Atlassian Mono"(코드) 두 서체로 통일된다.** 과거 macOS/Windows 시스템 폰트(SF Pro/Segoe UI)를 그대로 쓰면서 발생하던 플랫폼 간 렌더링 불일치(베이스라인 정렬 등)를 해결하기 위해 자체 서체로 전환했다.

본 문서는 [atlassian.design/foundations/typography](https://atlassian.design/foundations/typography)와 Atlassian 디자인 블로그를 분석하여 재구성했다.

---

## 🔤 서체 구성

| 서체 | 용도 | 기반 |
|---|---|---|
| **Atlassian Sans** | 제품 UI 전체 (제목, 본문) | Inter Variable 커스텀 튜닝 (가독성·글자 구분 개선) |
| **Atlassian Mono** | 코드 표현 전용 | JetBrains Mono 커스텀 튜닝 (소문자 높이 최대화) |
| **Charlie Sans** | 브랜드/마케팅 전용 (제품 UI에는 사용 안 함) | Atlassian 자체 브랜드 서체 |

### 역사

- **2017.09**: 브랜드 리브랜드와 함께 **Charlie Sans**를 브랜드/마케팅 서체로 도입 (제품 내부 폰트는 그대로 시스템 폰트 유지).
- **2024~2025** (Beta → GA): 제품 UI 폰트를 시스템 폰트 스택에서 **Atlassian Sans / Atlassian Mono**로 전면 교체. 도입 사유는 "SF Pro(Mac)와 Segoe UI(Windows)의 베이스라인 높이가 달라 아이콘-텍스트 정렬이 플랫폼마다 다르게 렌더링되는 문제"를 해결하기 위함.
- 본문 최소 폰트 크기를 접근성 목적으로 11px → **12px**로 상향.

### OpenType 세부 특징

- Atlassian Sans: 대문자 "i"에 세리프, 스퍼(spur) 있는 대문자 "G", 평평한 상단의 "3", 데이터 표기용 슬래시 0, 표 정렬용 tabular figures.
- Atlassian Mono: 슬래시 0 항상 사용, 리거처(ligature)는 사용하지 않음.

---

## 📏 타입 스케일

기준 단위: **16px = 1rem**. "minor third" 스케일(배율 1.2, 4의 배수로 반올림). 모든 크기는 **rem 단위**로 정의되어 브라우저 확대/축소에 대응한다.

### Heading (모두 Bold 웨이트)

| 레벨 | 토큰 | 폰트 크기 | 줄 높이 | 권장 용도 |
|---|---|---|---|---|
| XXL | `font.heading.xxlarge` | 32px | 36px | 브랜드/마케팅 콘텐츠 |
| XL | `font.heading.xlarge` | 28px | 32px | 브랜드/마케팅 콘텐츠 |
| L | `font.heading.large` | 24px | 28px | 앱 페이지 타이틀 |
| M | `font.heading.medium` | 20px | 24px | 모달 등 큰 컴포넌트의 헤더 |
| S | `font.heading.small` | 16px | 20px | 작은 컴포넌트, 공간 제한 헤더 |
| XS | `font.heading.xsmall` | 14px | 20px | 좁은 공간의 헤더 |
| XXS | `font.heading.xxsmall` | 12px | 16px | 좁은 공간의 헤더 |

### Body (기본 Regular 웨이트)

| 크기 | 토큰 | 폰트 크기 | 줄 높이 | 단락 간격 | 용도 |
|---|---|---|---|---|---|
| L | `font.body.large` | 16px | 24px | 16px | 블로그 등 긴 콘텐츠 |
| M (기본) | `font.body` | 14px | 20px | 12px | 짧은 텍스트, 버튼/컴포넌트 라벨 |
| S | `font.body.small` | 12px | 16px | 8px | 보조 콘텐츠, 잔글씨 |

### Metric (대시보드/차트 숫자 강조, 모두 Bold)

| 크기 | 토큰 | 폰트 크기 | 줄 높이 |
|---|---|---|---|
| Large | `font.metric.large` | 28px | 32px |
| Medium | `font.metric.medium` | 24px | 28px |
| Small | `font.metric.small` | 16px | 20px |

### Code

`font.code` — Regular 웨이트, 12px / 20px, 코드 블록 전용.

---

## ⚖️ 폰트 웨이트

| 이름 | 값 | 용도 |
|---|---|---|
| Regular | 400 | 일반 본문 기본값 |
| Medium | 500 | 컴포넌트/아이콘과 나란히 쓸 때 (베이스라인 정렬) |
| Semibold | 600 | 신중하게 사용 (Atlassian Sans가 아닌 폴백 폰트에서는 bold로 렌더링됨) |
| Bold | **653** | 모든 헤딩과 Metric에 사용. 표준 CSS 700이 아닌 **가변 폰트축 특이값**(Atlassian Sans 전용). 본문 강조는 절제해서 사용 |

---

## 📐 줄간격(line-height) 원칙

- 헤딩 ≈ 폰트 크기 × 1.2 (4의 배수로 반올림)
- 본문 ≈ 폰트 크기 × 1.5 (4의 배수로 반올림)
- rem 단위 사용 → 브라우저 폰트 크기 설정에 따라 함께 확대되어 접근성을 보장

---

## 🧱 타이포그래피 계층 원칙

핵심 원칙 3가지: **가독성 최적화(Optimize for readability)**, **시각적 조화(Create visual harmony)**, **사용자 맥락화(Contextualize for different users)**.

- 한 줄 길이는 **60~80자(약 10~12단어)**가 최적.
- 인접한 헤딩 레벨 간에는 **2~4단계 크기 차이**를 둬 뚜렷한 위계를 만든다.
- 본문의 편안한 최소 크기는 16px이며, 12px는 잔글씨/보조 콘텐츠 외에는 지양한다.
- 코드(Atlassian Mono)는 크기·굵기만 바꾸지 않고 **서체 자체를 분리**해 본문과 시각적으로 절대 혼동되지 않도록 한다.

---

## 📚 참고 자료

- [Foundations · Typography · Product typefaces and scale](https://atlassian.design/foundations/typography/product-typefaces-and-scale)
- [Foundations · Typography · Applying typography](https://atlassian.design/foundations/typography/applying-typography)
- [Implementing typography at scale — behind the screens](https://www.atlassian.com/blog/design/implementing-typography-at-scale-the-journey-behind-the-screens)
- [What's new — Typography and iconography updates](https://atlassian.design/whats-new/typography-and-iconography-updates)
- [Our bold new brand (Charlie Sans, 2017)](https://www.atlassian.com/blog/announcements/our-bold-new-brand)
