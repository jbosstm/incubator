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

import javax.ws.rs.PUT;
import javax.ws.rs.Path;

/**
 * LRA bean which contain two methods annotated with {@link AfterLRA}.
 */
public class MultiAfterLRABean {
    @LRA
    @Path("lra")
    public void doWork() {
        // no implementation is needed
    }

    @AfterLRA
    @Path("afterLra")
    @PUT
    public void afterLra() {
        // no implementation needed
    }

    @AfterLRA
    @Path("afterLra")
    @PUT
    public void afterLra2() {
        // no implementation needed
    }
}
