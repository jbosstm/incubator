/*
 *
 * Copyright The Narayana Authors
 *
 * SPDX-License-Identifier: LGPL-2.1-only
 *
 */

package io.narayana.lra.checker.bean.lra;

import org.eclipse.microprofile.lra.annotation.Complete;

public class NoLRAClass {
    @Complete
    void complete() {
        // nothing
    }
}
