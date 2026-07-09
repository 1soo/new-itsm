package com.itsm.auth.application;

import com.itsm.common.exception.BusinessException;
import com.itsm.common.exception.ErrorCode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PasswordPolicyTest {

    @Test
    void validPasswordPasses() {
        assertThatCode(() -> PasswordPolicy.validate("Welcome123!")).doesNotThrowAnyException();
    }

    @Test
    void tooShortThrows() {
        assertThatThrownBy(() -> PasswordPolicy.validate("Ab1"))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertErrorCode(e, ErrorCode.PASSWORD_POLICY_VIOLATION));
    }

    @Test
    void noDigitThrows() {
        assertThatThrownBy(() -> PasswordPolicy.validate("OnlyLetters"))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertErrorCode(e, ErrorCode.PASSWORD_POLICY_VIOLATION));
    }

    @Test
    void noLetterThrows() {
        assertThatThrownBy(() -> PasswordPolicy.validate("12345678"))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertErrorCode(e, ErrorCode.PASSWORD_POLICY_VIOLATION));
    }

    @Test
    void nullThrows() {
        assertThatThrownBy(() -> PasswordPolicy.validate(null))
                .isInstanceOf(BusinessException.class);
    }

    private void assertErrorCode(Throwable e, ErrorCode expected) {
        org.assertj.core.api.Assertions.assertThat(((BusinessException) e).getErrorCode()).isEqualTo(expected);
    }
}
