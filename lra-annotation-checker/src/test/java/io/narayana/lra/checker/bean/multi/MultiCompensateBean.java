/*
 *
 * Copyright The Narayana Authors
 *
 * SPDX-License-Identifier: LGPL-2.1-only
 *
 */

package io.narayana.lra.checker.bean.multi;

import org.eclipse.microprofile.lra.annotation.Compensate;
import org.eclipse.microprofile.lra.annotation.ws.rs.LRA;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;

/**
 * LRA bean which contain two methods annotated with {@link Compensate}.
 */
public class MultiCompensateBean {
    @LRA
    @Path("lra")
    public void doWork() {
        // no implementation is needed
    }

    @Compensate
    @Path("compensate")
    @PUT
    public void compensate() {
        // no implementation needed
    }

    @Compensate
    @Path("compensate")
    @PUT
    public void compensate2() {
        // no implementation needed
    }
}
