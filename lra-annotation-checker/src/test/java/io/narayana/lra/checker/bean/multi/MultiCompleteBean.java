/*
 *
 * Copyright The Narayana Authors
 *
 * SPDX-License-Identifier: LGPL-2.1-only
 *
 */

package io.narayana.lra.checker.bean.multi;

import org.eclipse.microprofile.lra.annotation.Compensate;
import org.eclipse.microprofile.lra.annotation.Complete;
import org.eclipse.microprofile.lra.annotation.ws.rs.LRA;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;

/**
 * LRA bean which contain two methods annotated with {@link Complete}.
 */
public class MultiCompleteBean {
    @LRA
    @Path("lra")
    public void doWork() {
        // no implementation is needed
    }

    @Complete
    @Path("complete")
    @PUT
    public void complete() {
        // no implementation needed
    }

    @Complete
    @Path("complete")
    @PUT
    public void complete2() {
        // no implementation needed
    }

    @Compensate
    @Path("compensate")
    @PUT
    public void compensateRequired() {
        // no implementation needed
    }
}
