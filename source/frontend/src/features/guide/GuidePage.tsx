import { useEffect, useRef, useState } from "react";

import {
  UserGuideDomainSection,
  UserGuideOverview,
  UserGuideRoleSection,
} from "@/components/common/user-guide-content";
import { cn } from "@/lib/utils";
import { useAppSelector } from "@/store/hooks";

/** 좌측 TOC 3개 링크(common.md v0.8) — 도메인/역할 개별 하위 링크 없음. */
const SECTIONS = [
  { id: "overview", label: "개요" },
  { id: "domains", label: "도메인 및 원칙" },
  { id: "roles", label: "역할별 수행 내용과 방법" },
] as const;

/**
 * 사용자 가이드 전용 화면(SCR-COM-012 v0.8) — Confluence 문서 스타일.
 * 좌측 sticky TOC + 우측 본문(개요/도메인/역할 순). 신규 API 없음, 전부 정적 콘텐츠.
 */
export function GuidePage() {
  const user = useAppSelector((s) => s.auth.user);
  const [activeId, setActiveId] = useState<string>(SECTIONS[0].id);
  const sectionRefs = useRef<Record<string, HTMLElement | null>>({});

  useEffect(() => {
    const observer = new IntersectionObserver(
      (entries) => {
        const visible = entries
          .filter((entry) => entry.isIntersecting)
          .sort((a, b) => a.boundingClientRect.top - b.boundingClientRect.top);
        if (visible[0]) setActiveId(visible[0].target.id);
      },
      { rootMargin: "-96px 0px -70% 0px", threshold: 0 },
    );

    for (const section of SECTIONS) {
      const el = sectionRefs.current[section.id];
      if (el) observer.observe(el);
    }

    return () => observer.disconnect();
  }, []);

  const scrollToSection = (id: string) => {
    sectionRefs.current[id]?.scrollIntoView({ behavior: "smooth", block: "start" });
  };

  return (
    <div className="mx-auto flex max-w-5xl gap-8">
      <nav
        className="sticky top-6 hidden h-fit w-48 shrink-0 md:block"
        aria-label="사용자 가이드 목차"
      >
        <ul className="space-y-1 text-sm">
          {SECTIONS.map((section) => (
            <li key={section.id}>
              <button
                type="button"
                onClick={() => scrollToSection(section.id)}
                className={cn(
                  "block w-full rounded-md px-3 py-1.5 text-left text-muted-foreground hover:text-foreground",
                  activeId === section.id && "bg-accent font-medium text-foreground",
                )}
              >
                {section.label}
              </button>
            </li>
          ))}
        </ul>
      </nav>

      <div className="min-w-0 flex-1 space-y-10">
        <h1 className="text-heading-large font-bold text-foreground">사용자 가이드</h1>

        <section
          id="overview"
          ref={(el) => {
            sectionRefs.current.overview = el;
          }}
        >
          <h2 className="mb-3 text-heading-medium font-bold text-foreground">개요</h2>
          <UserGuideOverview />
        </section>

        <section
          id="domains"
          ref={(el) => {
            sectionRefs.current.domains = el;
          }}
        >
          <h2 className="mb-3 text-heading-medium font-bold text-foreground">도메인 및 원칙</h2>
          <UserGuideDomainSection />
        </section>

        <section
          id="roles"
          ref={(el) => {
            sectionRefs.current.roles = el;
          }}
        >
          <h2 className="mb-3 text-heading-medium font-bold text-foreground">
            역할별 수행 내용과 방법
          </h2>
          <UserGuideRoleSection myRoles={user?.roles} />
        </section>
      </div>
    </div>
  );
}
