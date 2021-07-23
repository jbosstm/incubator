/*
 * Copyright The Narayana Authors
 *
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package io.narayana.lra.checker.bean.hierarchy.base;

import org.eclipse.microprofile.lra.annotation.AfterLRA;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;

public abstract class BaseWithAfterLraBean {
    @AfterLRA
    @Path("afterLra")
    @PUT
    public void afterLra() {
        // no implementation needed
    }

}
