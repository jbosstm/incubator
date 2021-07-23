/*
 *
 * Copyright The Narayana Authors
 *
 * SPDX-License-Identifier: LGPL-2.1-only
 *
 */

package io.narayana.lra.checker.bean.nonjaxrs;

import org.eclipse.microprofile.lra.annotation.Compensate;
import org.eclipse.microprofile.lra.annotation.ws.rs.LRA;

import javax.ws.rs.Path;
import java.net.URI;

/**
 * LRA bean which contain non JAX-RS {@link Compensate} method.
 */
public class CompensateNonJaxrsBean {
    @LRA
    @Path("lra")
    public void doWork() {
        // no implementation is needed
    }

    @Compensate
    public void compensate(URI lraId, URI parentId) {
        // no implementation needed
    }
}
