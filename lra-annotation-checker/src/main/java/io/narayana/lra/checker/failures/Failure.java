/*
 * Copyright The Narayana Authors
 *
 * SPDX-License-Identifier: LGPL-2.1-only
 */


package io.narayana.lra.checker.failures;

/**
 * Simple DTO which gathers the error code of the failure and
 * details about the error as {@link String}.
 */
public class Failure {
    private final ErrorCode errorCode;
    private final String details;

    public static Failure instance(ErrorCode errorCode, String details) {
        return new Failure(errorCode, details);
    }

    public static Failure instance(String details) {
        return new Failure(ErrorCode.UNKNOWN, details);
    }

    private Failure(ErrorCode errorCode, String details) {
        this.errorCode = errorCode;
        this.details = details;
    }

    @Override
    public String toString() {
        return errorCode.toString() + " " + details;
    }
}
