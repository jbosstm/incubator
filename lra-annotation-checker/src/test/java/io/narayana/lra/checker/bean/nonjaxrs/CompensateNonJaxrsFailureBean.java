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
import java.net.URL;

/**
 * LRA bean which contain non JAX-RS {@link Compensate} method with wrong signature.
 */
public class CompensateNonJaxrsFailureBean {
    @LRA
    @Path("lra")
    public void doWork() {
        // no implementation is needed
    }

    @Compensate // using URL instead of URI
    public void compensate(URI lraId, URL parentId) {
        // no implementation needed
    }
}
