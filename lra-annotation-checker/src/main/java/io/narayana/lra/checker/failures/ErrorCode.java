/*
 * Copyright The Narayana Authors
 *
 * SPDX-License-Identifier: LGPL-2.1-only
*/

package io.narayana.lra.checker.failures;

import org.eclipse.microprofile.lra.annotation.AfterLRA;
import org.eclipse.microprofile.lra.annotation.Compensate;
import org.eclipse.microprofile.lra.annotation.ws.rs.LRA;

public enum ErrorCode {
    UNKNOWN(0, ""),

    MISSING_ANNOTATIONS_COMPENSATE_AFTER_LRA(1, "The class annotated with " + LRA.class.getName() + " missing at least" +
            " one of the annotations " + Compensate.class.getSimpleName() + " or " + AfterLRA.class.getSimpleName()),
    MULTIPLE_ANNOTATIONS_OF_THE_SAME_TYPE(2, "Multiple annotations of the same type is used. Only one per the class is expected."),
    MULTIPLE_ANNOTATIONS_OF_VARIOUS_TYPES(3, "Makes no sense to declare multiple LRA annotations of different types at method."),
    WRONG_METHOD_SIGNATURE_NON_JAXRS_RESOURCE(4, "Wrong method signature for non JAX-RS resource method."),
    WRONG_JAXRS_COMPLEMENTARY_ANNOTATION(5, "Wrong complementary annotation of JAX-RS resource method."),
    MISSING_SUSPEND_ASYNC_CALLBACK(6, "Asynchronous @Suspend method parameter defined but " +
            "LRA class contain no necessary @Status and @Forget callbacks."),
    NO_LRA(7, "Type contains a LRA callback annotations but no @LRA. When deliberate then consider using abstract or interface instead of class.");

    private final int code;
    private final String description;

    ErrorCode(int code, String description) {
        this.code = code;
        this.description = description;
    }

    @Override
    public String toString() {
        if (code == 0) {
            return "<<no error code>>";
        } else {
            return code + ": " + description;
        }
    }
}
