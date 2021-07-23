/*
 * Copyright The Narayana Authors
 *
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package io.narayana.lra.checker.bean.hierarchy.wrong;

import org.eclipse.microprofile.lra.annotation.AfterLRA;
import org.eclipse.microprofile.lra.annotation.Compensate;
import org.eclipse.microprofile.lra.annotation.Complete;
import org.eclipse.microprofile.lra.annotation.Forget;
import org.eclipse.microprofile.lra.annotation.Status;
import org.eclipse.microprofile.lra.annotation.ws.rs.LRA;
import org.eclipse.microprofile.lra.annotation.ws.rs.Leave;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;

@LRA
public class GoodLRAChild extends BadParent {
    @Complete
    @Path("complete")
    @PUT
    public void complete() {
        // no implementation needed
    }

    @Compensate
    @Path("compensate")
    @PUT
    public void compensate() {
        // no implementation needed
    }

    @Status
    @Path("status")
    @GET
    public void status() {
        // no implementation needed
    }

    @AfterLRA
    @Path("after")
    @PUT
    public void afterLra() {
        // no implementation needed
    }

    @Forget
    @Path("forget")
    @DELETE
    public void forget() {
        // no implementation needed
    }

    @Leave
    @Path("leave")
    @PUT
    public void leave() {
        // no implementation needed
    }
}
