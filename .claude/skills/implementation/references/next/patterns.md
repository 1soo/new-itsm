# Next 설계 패턴

Next.js 공식 문서 기반 설계 패턴. [conventions.md](conventions.md)의 컨벤션을 전제로, 서버/클라이언트 합성·데이터 패칭·서버 로직 분리·SOLID를 구체화한다. CSR에서는 Frontend, SSR에서는 서버 역할까지 이 패턴을 적용한다.

## 1. 서버/클라이언트 컴포넌트 합성 패턴

- **서버 컴포넌트를 기본값**으로 하고, 상호작용(이벤트 핸들러·hook·브라우저 API)이 필요한 부분만 `"use client"`로 분리한다.
- `"use client"` 경계는 **최소 leaf로 좁힌다.** 트리 상단에 `"use client"`를 붙이면 하위 전체가 클라이언트 번들에 포함되므로, 인터랙션 컴포넌트만 클라이언트로 만들고 나머지는 서버에 남긴다.
- **서버 컴포넌트를 클라이언트 컴포넌트의 `children`으로 전달(interleaving)**한다. 클라이언트 컴포넌트에 `children` slot을 두면, 그 자리에 넣은 서버 컴포넌트는 서버에서 먼저 렌더된 뒤 결과(RSC Payload)만 전달된다 — 서버 컴포넌트를 클라이언트 안으로 `import`하지 않고도 시각적으로 중첩할 수 있다.

```tsx
// page.tsx (서버 컴포넌트) — Modal은 클라이언트, Cart는 서버
import Modal from './ui/modal'
import Cart from './ui/cart'

export default function Page() {
  return (
    <Modal>
      <Cart />
    </Modal>
  )
}
```

- 서버→클라이언트 데이터 전달은 **props**로 하며, 값은 **직렬화 가능(serializable)**해야 한다. 스트리밍이 필요하면 promise를 넘겨 `use` API로 받는다.

참고: https://nextjs.org/docs/app/getting-started/server-and-client-components

## 2. 데이터 패칭 패턴

- **병렬 패칭**: 서로 의존하지 않는 요청은 먼저 시작(호출)해두고 `Promise.all`로 함께 await 한다. 순차 대기(waterfall)를 피해 지연을 줄인다.

```tsx
export default async function Page({ params }: { params: Promise<{ username: string }> }) {
  const { username } = await params
  const artistData = getArtist(username)   // await 하지 않고 시작
  const albumsData = getAlbums(username)
  const [artist, albums] = await Promise.all([artistData, albumsData])
  return <><h1>{artist.name}</h1><Albums list={albums} /></>
}
```

- **순차 패칭**: 뒤 요청이 앞 결과에 의존하면 순차로 진행하되, 의존 구간을 `<Suspense>`로 감싸 나머지 UI를 먼저 보여주고 스트리밍한다.
- **request memoization**: 같은 렌더 트리에서 동일 데이터를 여러 곳(예: metadata + 페이지)에서 필요로 하면 React `cache`로 감싸 중복 요청을 제거한다.

```ts
import { cache } from 'react'
export const getPost = cache(async (slug: string) => {
  return db.query.posts.findFirst({ where: eq(posts.slug, slug) })
})
```

- **Suspense 스트리밍**: 독립적인 비동기 영역을 각각의 `<Suspense>` 경계로 감싸 병렬 스트리밍한다. 느린 영역이 나머지 렌더를 막지 않는다.

```tsx
<Suspense fallback={<p>Loading revenue...</p>}><Revenue /></Suspense>
<Suspense fallback={<p>Loading orders...</p>}><RecentOrders /></Suspense>
```

참고: https://nextjs.org/docs/app/getting-started/fetching-data , https://nextjs.org/docs/app/guides/streaming

## 3. Route Handler / Server Action 설계 패턴

- **얇은 핸들러 + 서비스 계층 분리(관심사 분리)**: Route Handler(`app/api/**`)와 Server Action(`"use server"`)은 요청 파싱·응답 변환만 담당하고, 인증·인가·DB 접근 등 실제 로직은 `server-only` 서비스(DAL) 모듈에 위임한다.

```ts
// action: 얇게 유지, 서비스에 위임
'use server'
import { deletePost } from '@/data/posts'
import { revalidatePath } from 'next/cache'

export async function deletePostAction(postId: string) {
  await deletePost(postId)   // 인증·인가·DB는 서비스 내부에서
  revalidatePath('/posts')
}
```

```ts
// service(DAL): server-only, 도메인 로직 소유
import 'server-only'
import { auth } from '@/lib/auth'
import { db } from '@/lib/db'

export async function deletePost(postId: string) {
  const session = await auth()
  if (!session?.user) throw new Error('Unauthorized')
  const post = await db.post.findUnique({ where: { id: postId } })
  if (post.authorId !== session.user.id) throw new Error('Forbidden')
  await db.post.delete({ where: { id: postId } })
}
```

- Route Handler는 HTTP 메서드(GET/POST/PUT/DELETE/PATCH)별 함수로 정의하고, 각 메서드는 동일하게 서비스로 위임한다.
- 이 "얇은 핸들러 + 서비스 계층" 구조는 SSR에서 서버 역할을 겸할 때 Spring Boot의 DDD 계층 구조([references/spring-boot/conventions.md](../spring-boot/conventions.md): presentation→application→domain)와 같은 원리로 적용된다.

참고: https://nextjs.org/docs/app/guides/data-security , https://nextjs.org/docs/app/api-reference/file-conventions/route

## 4. SOLID 적용

- **단일 책임(SRP)**: 라우팅/렌더(page·Route Handler), 비즈니스 로직(서비스/DAL), 데이터 접근(db 모듈)을 각 계층으로 분리한다. 핸들러에 도메인 로직을 넣지 않는다.
- **의존 방향**: `server-only`로 표시한 서버 전용 코드는 클라이언트 컴포넌트에서 import 하지 않는다. 클라이언트는 props·Server Action 호출을 통해서만 서버에 의존하고, 서버 코드가 클라이언트 코드에 의존하지 않게 한다(의존성 역전).
- **개방-폐쇄/인터페이스 분리**: 서비스는 구체 구현이 아니라 데이터 접근 추상(db 인터페이스)에 의존하게 두어, 저장소 교체 시 핸들러·서비스 시그니처가 바뀌지 않게 한다.

참고: https://nextjs.org/docs/app/getting-started/server-and-client-components , https://nextjs.org/docs/app/guides/data-security
