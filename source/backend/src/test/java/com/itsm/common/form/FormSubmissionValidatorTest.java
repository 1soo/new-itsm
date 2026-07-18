package com.itsm.common.form;

import com.itsm.common.exception.BusinessException;
import com.itsm.common.exception.ErrorCode;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FormSubmissionValidatorTest {

    private Map<String, Object> schema(boolean required, String regex) {
        Map<String, Object> validation = new HashMap<>();
        validation.put("required", required);
        validation.put("regex", regex);
        return Map.of("components", List.of(
                Map.of("key", "reason", "label", "사유", "type", "text", "validation", validation)));
    }

    @Test
    void requiredFieldMissingRejected() {
        assertThatThrownBy(() -> FormSubmissionValidator.validate(schema(true, null), Map.of()))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(ErrorCode.REQUIRED_FIELD_MISSING));
    }

    @Test
    void blankRegexDoesNotRejectValidValue() {
        assertThatCode(() -> FormSubmissionValidator.validate(schema(false, ""), Map.of("reason", "고장")))
                .doesNotThrowAnyException();
    }

    @Test
    void actualRegexStillRejectsMismatchingValue() {
        assertThatThrownBy(() -> FormSubmissionValidator.validate(schema(false, "^[0-9]+$"), Map.of("reason", "고장")))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(ErrorCode.FORM_FIELD_INVALID));
    }

    @Test
    void actualRegexAllowsMatchingValue() {
        assertThatCode(() -> FormSubmissionValidator.validate(schema(false, "^[0-9]+$"), Map.of("reason", "123")))
                .doesNotThrowAnyException();
    }

    @Test
    void requiredAndRegexBothAppliedWhenValuePresent() {
        assertThatThrownBy(() -> FormSubmissionValidator.validate(schema(true, "^[0-9]+$"), Map.of("reason", "abc")))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(ErrorCode.FORM_FIELD_INVALID));
    }
}
