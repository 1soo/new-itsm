package com.itsm.asset.domain;

/**
 * 만료 상태(license/warranty/contract 중 가장 임박한 만료일 기준 계산).
 */
public enum ExpiryStatus {
    OK,
    EXPIRING,
    EXPIRED
}
