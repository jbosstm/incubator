/*
 * Copyright The Narayana Authors
 *
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package io.narayana.lra.checker.bean.hierarchy.base;

import org.eclipse.microprofile.lra.annotation.ws.rs.LRA;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/")
public class ChildLRABean extends BaseWithAfterLraBean {
    @LRA
    @GET
    public void doWork() {
        // some work here
    }
}
