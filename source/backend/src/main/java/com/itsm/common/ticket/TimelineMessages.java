package com.itsm.common.ticket;

/**
 * STATUS_* 타임라인 기본 메시지 문구 조립 유틸(SRM/ESM/INCIDENT 공용).
 */
public final class TimelineMessages {

    private TimelineMessages() {
    }

    /**
     * 상태 라벨을 홑따옴표로 감싸고, 마지막 글자의 받침 유무(+ㄹ받침 예외)에 맞는 조사(로/으로)를 붙인다.
     * 예: "이행 중" → "'이행 중'으로", "종료" → "'종료'로".
     */
    public static String quotedWithParticle(String label) {
        char last = label.charAt(label.length() - 1);
        int idx = last - 0xAC00;
        boolean hasBatchim = idx >= 0 && idx <= 11171 && (idx % 28) != 0;
        boolean rieulBatchim = idx >= 0 && idx <= 11171 && (idx % 28) == 8;
        String particle = (!hasBatchim || rieulBatchim) ? "로" : "으로";
        return "'" + label + "'" + particle;
    }
}
