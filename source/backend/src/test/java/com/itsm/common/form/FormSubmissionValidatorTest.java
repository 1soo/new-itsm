package com.itsm.common.form;

import com.itsm.common.exception.BusinessException;
import com.itsm.common.exception.ErrorCode;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FormSubmissionValidatorTest {

    private Map<String, Object> schemaWithPattern(Object pattern) {
        Map<String, Object> validate = pattern == null ? Map.of() : Map.of("pattern", pattern);
        return Map.of("display", "form", "components", List.of(
                Map.of("key", "reason", "label", "사유", "type", "textfield", "input", true, "validate", validate)));
    }

    @Test
    void blankPatternFromFormBuilderDoesNotRejectValidValue() {
        // TC-SRM-004/006 회귀: Form.io Builder가 pattern 미설정 필드도 "pattern":""로 직렬화한다.
        Map<String, Object> schema = schemaWithPattern("");

        assertThatCode(() -> FormSubmissionValidator.validate(schema, Map.of("reason", "고장")))
                .doesNotThrowAnyException();
    }

    @Test
    void actualPatternStillRejectsMismatchingValue() {
        Map<String, Object> schema = schemaWithPattern("^[0-9]+$");

        assertThatThrownBy(() -> FormSubmissionValidator.validate(schema, Map.of("reason", "고장")))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> org.assertj.core.api.Assertions.assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(ErrorCode.FORM_FIELD_INVALID));
    }

    @Test
    void actualPatternAllowsMatchingValue() {
        Map<String, Object> schema = schemaWithPattern("^[0-9]+$");

        assertThatCode(() -> FormSubmissionValidator.validate(schema, Map.of("reason", "123")))
                .doesNotThrowAnyException();
    }
}
