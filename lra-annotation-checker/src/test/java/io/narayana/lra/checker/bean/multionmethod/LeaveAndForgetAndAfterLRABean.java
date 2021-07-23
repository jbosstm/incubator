/*
 * Copyright The Narayana Authors
 *
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package io.narayana.lra.checker.bean.multionmethod;

import org.eclipse.microprofile.lra.annotation.AfterLRA;
import org.eclipse.microprofile.lra.annotation.Compensate;
import org.eclipse.microprofile.lra.annotation.Forget;
import org.eclipse.microprofile.lra.annotation.ws.rs.LRA;
import org.eclipse.microprofile.lra.annotation.ws.rs.Leave;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;

/**
 * LRA bean which contain {@link Leave} and {@link Forget} and {@link AfterLRA} at one method.
 */
public class LeaveAndForgetAndAfterLRABean {
    @LRA
    @Path("lra")
    public void doWork() {
        // no implementation is needed
    }

    @Compensate
    @AfterLRA
    @Path("compensate")
    @PUT
    public void compensate() {
        // no implementation needed
    }

    @Leave
    @Forget
    @Path("complete")
    public void complete() {
        // no implementation needed
    }
}
