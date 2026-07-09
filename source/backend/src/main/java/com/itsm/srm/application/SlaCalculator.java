package com.itsm.srm.application;

import com.itsm.srm.domain.SlaStatus;

import java.time.Duration;
import java.time.OffsetDateTime;

/**
 * SLA 상태 산정. 기한 대비 잔여 시간으로 OK/WARNING/BREACHED를 판정한다.
 */
final class SlaCalculator {

    private static final double WARNING_THRESHOLD = 0.25; // 잔여 25% 이하 → WARNING

    private SlaCalculator() {
    }

    static SlaStatus status(OffsetDateTime createdAt, OffsetDateTime due, boolean done) {
        if (due == null) {
            return SlaStatus.OK;
        }
        if (done) {
            return SlaStatus.OK; // 완료 처리된 단계는 준수로 간주
        }
        OffsetDateTime now = OffsetDateTime.now();
        if (!now.isBefore(due)) {
            return SlaStatus.BREACHED;
        }
        long total = Duration.between(createdAt, due).toSeconds();
        long remaining = Duration.between(now, due).toSeconds();
        if (total > 0 && (double) remaining / total <= WARNING_THRESHOLD) {
            return SlaStatus.WARNING;
        }
        return SlaStatus.OK;
    }
}
