---
description: 이 프로젝트가 사용 중인 Agent Teams 스킬 패키지(에이전트·스킬·커스텀 커맨드·오케스트레이션 규약)를 원본 저장소의 최신 버전과 동기화한다.
disable-model-invocation: true
---

# /sync-skill — Agent Teams 스킬 패키지 동기화

이 프로젝트는 `https://github.com/1soo/claude-teams-based-skill` 의 Agent Teams 스킬 패키지를 가져와 사용 중이다. 원본 저장소가 업데이트되면 이 커맨드로 최신 내용을 현재 프로젝트에 반영한다. **이 커맨드는 당신(Main)이 직접 수행하며, teammate를 소집하지 않는다.**

## 동기화 범위

- **대상**(원본 저장소 기준 git 추적 파일만): `.claude/agents/`, `.claude/commands/`, `.claude/settings.json`, `.claude/skills/`, `.mcp.json`, 루트 `CLAUDE.md`.
- **비대상**: `docs/`, `source/`(프로젝트별 산출물), `.claude/settings.local.json`(로컬 설정), `README.md`/`LICENSE.md`/`.gitignore`(원본 저장소 자체 문서).

## 0. 원본 저장소 최신 버전 확보

임시 디렉토리에 원본을 얕은 클론한다(네트워크 접근 필요 — 샌드박스 등으로 차단되면 사용자에게 알리고 조치를 요청한다).

```bash
TMPDIR=$(mktemp -d)
git clone --depth 1 https://github.com/1soo/claude-teams-based-skill.git "$TMPDIR/source"
LATEST_COMMIT=$(git -C "$TMPDIR/source" rev-parse HEAD)
```

## 1. 로컬 반영 상태 확인

- `.claude/.skill-sync.json`이 있으면 그 안의 `commit`(마지막으로 반영한 원본 커밋 해시)을 읽는다. 없으면 최초 동기화로 간주한다.
- 마지막 반영 커밋이 `LATEST_COMMIT`과 같으면 "이미 최신 상태"라고 안내하고 `$TMPDIR`를 정리한 뒤 종료한다.
- 다르고 이전 기록이 있으면, 참고용으로 원본의 변경 이력을 보여준다:
  ```bash
  git -C "$TMPDIR/source" log --oneline <마지막 반영 커밋>..HEAD
  ```
  이전 기록이 없으면(최초 동기화) 생략한다.

## 2. 로컬 작업 상태 안전 확인

동기화 대상 경로(`.claude`, `.mcp.json`, `CLAUDE.md`)에 커밋되지 않은 변경사항이 있는지 `git status`로 확인한다. 있으면 사용자에게 알리고, 먼저 커밋하거나 stash할지 확인받은 뒤에만 다음 단계로 진행한다(임의로 덮어쓰지 않는다).

## 3. 변경 내역 비교

클론본과 현재 프로젝트를 비교한다.

```bash
diff -rq "$TMPDIR/source/.claude" .claude
diff -q "$TMPDIR/source/.mcp.json" .mcp.json
diff -q "$TMPDIR/source/CLAUDE.md" CLAUDE.md
```

(현재 프로젝트에 해당 파일/디렉토리가 아예 없으면 전부 "추가" 대상으로 취급한다)

결과를 아래 세 그룹으로 정리해 사용자에게 보여준다.

- **추가**: 원본에는 있지만 로컬에 없는 파일
- **수정**: 양쪽에 다 있지만 내용이 다른 파일
- **로컬에만 있음(원본에서 삭제됨)**: 로컬에는 있지만 원본에는 없는 파일 — 자동 삭제하지 않고 목록만 보여주며, 프로젝트에서 직접 추가한 파일일 수도 있으므로 사용자가 직접 검토 후 삭제 여부를 판단하도록 안내한다.

## 4. 사용자 확인 후 반영

위 요약을 보여주고 **반영해도 되는지 명시적으로 확인**받는다. 승인 시:

- "추가"·"수정" 대상 파일을 클론본에서 현재 프로젝트로 복사해 덮어쓴다.
- "로컬에만 있음" 파일은 사용자가 삭제를 명시적으로 요청한 경우에만 삭제한다(기본은 보존).

## 5. 반영 기록 갱신

반영이 끝나면 `.claude/.skill-sync.json`에 아래 내용을 기록한다.

```json
{
  "repo": "https://github.com/1soo/claude-teams-based-skill",
  "commit": "<LATEST_COMMIT>",
  "synced_at": "<Bash의 date 명령으로 확인한 현재 시각>"
}
```

## 6. 정리 및 보고

- 임시 디렉토리(`$TMPDIR`)를 삭제한다.
- 반영된 파일 목록과 원본 커밋 로그(있었다면)를 사용자에게 요약해 보고한다.
- **git add/commit/push는 이 커맨드의 범위가 아니다.** 사용자가 명시적으로 요청할 때만 별도로 수행한다.
