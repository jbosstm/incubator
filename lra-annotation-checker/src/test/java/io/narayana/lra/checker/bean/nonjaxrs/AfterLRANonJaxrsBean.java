/*
 *
 * Copyright The Narayana Authors
 *
 * SPDX-License-Identifier: LGPL-2.1-only
 *
 */

package io.narayana.lra.checker.bean.nonjaxrs;

import org.eclipse.microprofile.lra.annotation.AfterLRA;
import org.eclipse.microprofile.lra.annotation.LRAStatus;
import org.eclipse.microprofile.lra.annotation.ws.rs.LRA;

import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.net.URI;

/**
 * LRA bean which contain non JAX-RS {@link AfterLRA} method.
 */
public class AfterLRANonJaxrsBean {
    @LRA
    @Path("lra")
    public void doWork() {
        // no implementation is needed
    }

    @AfterLRA
    public Response afterLra(URI lraId, LRAStatus status) {
        return null;
    }
}
