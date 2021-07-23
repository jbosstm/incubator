/*
 *
 * Copyright The Narayana Authors
 *
 * SPDX-License-Identifier: LGPL-2.1-only
 *
 */

package io.narayana.lra.checker.bean.multi;

import org.eclipse.microprofile.lra.annotation.AfterLRA;
import org.eclipse.microprofile.lra.annotation.Status;
import org.eclipse.microprofile.lra.annotation.ws.rs.LRA;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;

/**
 * LRA bean which contain two methods annotated with {@link Status}.
 */
public class MultiStatusBean {
    @LRA
    @Path("lra")
    public void doWork() {
        // no implementation is needed
    }

    @Status
    @Path("status")
    @GET
    public void status() {
        // no implementation needed
    }

    @Status
    @Path("status")
    @GET
    public void status2() {
        // no implementation needed
    }

    @AfterLRA
    @Path("afterlra")
    @PUT
    public void afterLraRequired() {
        // no implementation needed
    }
}
