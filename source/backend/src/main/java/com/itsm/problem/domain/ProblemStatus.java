package com.itsm.problem.domain;

/**
 * 문제 상태(6단계). DETECTION→CLASSIFICATION→INVESTIGATION→KNOWN_ERROR→WORKAROUND→RESOLVED_CLOSED.
 */
public enum ProblemStatus {
    DETECTION,
    CLASSIFICATION,
    INVESTIGATION,
    KNOWN_ERROR,
    WORKAROUND,
    RESOLVED_CLOSED
}
