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
import org.eclipse.microprofile.lra.annotation.ws.rs.Leave;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

// no @LRA intentionally here
public abstract class BadParent {
    @Complete
    @Path("complete")
    @GET
    public void compl() {
        // no implementation needed
    }

    @Compensate
    @Path("compensate")
    @GET
    public void com() {
        // no implementation needed
    }

    @Status
    @Path("status")
    @DELETE
    public void st() {
        // no implementation needed
    }

    @AfterLRA
    @Path("after")
    @DELETE
    public void a() {
        // no implementation needed
    }

    @Forget
    @Path("forget")
    @GET
    public void f() {
        // no implementation needed
    }

    @Leave
    @Path("leave")
    @GET
    public void l() {
        // no implementation needed
    }
}
