/*
 *
 * Copyright The Narayana Authors
 *
 * SPDX-License-Identifier: LGPL-2.1-only
 *
 */

package io.narayana.lra.checker.bean.nonjaxrs;

import org.eclipse.microprofile.lra.annotation.AfterLRA;
import org.eclipse.microprofile.lra.annotation.ws.rs.LRA;

import javax.ws.rs.Path;
import java.util.concurrent.CompletionStage;

/**
 * LRA bean which contain non JAX-RS {@link AfterLRA} method with wrong signature.
 */
public class AfterLRANonJaxrsFailureBean {
    @LRA
    @Path("lra")
    public void doWork() {
        // no implementation is needed
    }

    @AfterLRA
    public CompletionStage<String> afterLra(String wrongParameter) {
        return null;
    }
}
