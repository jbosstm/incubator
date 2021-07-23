/*
 * Copyright The Narayana Authors
 *
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package io.narayana.lra.checker.bean.multionmethod;

import org.eclipse.microprofile.lra.annotation.Compensate;
import org.eclipse.microprofile.lra.annotation.Complete;
import org.eclipse.microprofile.lra.annotation.Status;
import org.eclipse.microprofile.lra.annotation.ws.rs.LRA;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;

/**
 * LRA bean which contain {@link Complete} and {@link Status} at one method.
 */
public class CompleteAndStatusBean {
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

    @Complete
    @Status
    @Path("complete")
    @PUT
    public void complete() {
        // no implementation needed
    }
}
