package com.itsm.change.domain;

/**
 * 변경 상태(6단계). REQUESTED→REVIEW→PLANNING→APPROVAL→IMPLEMENTATION→CLOSED.
 */
public enum ChangeStatus {
    REQUESTED,
    REVIEW,
    PLANNING,
    APPROVAL,
    IMPLEMENTATION,
    CLOSED
}
