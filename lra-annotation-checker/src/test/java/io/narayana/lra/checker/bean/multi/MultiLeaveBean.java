/*
 *
 * Copyright The Narayana Authors
 *
 * SPDX-License-Identifier: LGPL-2.1-only
 *
 */

package io.narayana.lra.checker.bean.multi;

import org.eclipse.microprofile.lra.annotation.AfterLRA;
import org.eclipse.microprofile.lra.annotation.ws.rs.LRA;
import org.eclipse.microprofile.lra.annotation.ws.rs.Leave;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;

/**
 * LRA bean which contain two methods annotated with {@link Leave}.
 */
public class MultiLeaveBean {
    @LRA
    @Path("lra")
    public void doWork() {
        // no implementation is needed
    }

    @Leave
    @Path("leave")
    @PUT
    public void leave() {
        // no implementation needed
    }

    @Leave
    @Path("leave")
    @PUT
    public void leave2() {
        // no implementation needed
    }

    @AfterLRA
    @Path("afterlra")
    @PUT
    public void afterLraRequired() {
        // no implementation needed
    }
}
