-- =====================================================================
-- ITSM — common 증분: 알림 확인처리(유지보수 요청 2026-07-11)
-- 근거: docs/02_plan/database/common.md 4절 notification_dismissal,
--       docs/03_develop/plan/common.md "알림 확인처리" DB 절
-- append-only(수정 없음, audit_log/refresh_token과 동일 성격)라
--   updated_by/at, is_deleted 미사용.
-- =====================================================================

-- ── notification_dismissal : 사용자별 알림 확인처리 이력 ──────────
CREATE TABLE notification_dismissal (
    id                 BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id            BIGINT       NOT NULL REFERENCES app_user (id),
    notification_type  VARCHAR(30)  NOT NULL,   -- SERVICE_REQUEST_APPROVAL / CHANGE_APPROVAL / ASSET_EXPIRY
    source_id          BIGINT       NOT NULL,   -- 알림 원본 식별자(requestId/changeId/asset id)
    dismissed_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by         VARCHAR(100) NOT NULL,
    created_at         TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_notification_dismissal UNIQUE (user_id, notification_type, source_id)
);

-- ── 조회 인덱스 (API-COM-002 확인처리 이력 조회) ───────────────────
CREATE INDEX idx_notification_dismissal_lookup
    ON notification_dismissal (user_id, notification_type, source_id);
