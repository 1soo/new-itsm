package com.itsm.auth.application;

import com.itsm.common.exception.BusinessException;
import com.itsm.common.exception.ErrorCode;

/**
 * 비밀번호 정책: 최소 8자, 영문·숫자를 각각 1자 이상 포함.
 */
public final class PasswordPolicy {

    private PasswordPolicy() {
    }

    public static void validate(String password) {
        if (password == null
                || password.length() < 8
                || !password.matches(".*[A-Za-z].*")
                || !password.matches(".*\\d.*")) {
            throw new BusinessException(ErrorCode.PASSWORD_POLICY_VIOLATION,
                    "비밀번호는 8자 이상이며 영문과 숫자를 포함해야 합니다.");
        }
    }
}
