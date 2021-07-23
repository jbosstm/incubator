/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2017, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package io.narayana.lra.checker.failures;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Singleton storing list of String representing LRA definition failures.
 */
public enum FailureCatalog {
    INSTANCE;

    private final List<Failure> failureCatalog = Collections.synchronizedList(new ArrayList<>());

    /**
     * Adding a failure string under the list of failures represented by this catalog with a known error code.
     * The format of {@link String#format(String, Object...)} is expected.
     */
    public void add(ErrorCode errorCode, String format, Object... params) {
        failureCatalog.add(Failure.instance(errorCode, String.format(format, params)));
    }

    /**
     * Adding a failure string under the list of failures represented by this catalog with a known error code.
     */
    public void add(ErrorCode errorCode, String errorDetails) {
        failureCatalog.add(Failure.instance(errorCode, errorDetails));
    }

    /**
     * Informs if the catalog contains some failures.
     */
    public boolean isEmpty() {
        return failureCatalog.isEmpty();
    }

    public void clear() {
        failureCatalog.clear();
    }

    /**
     * Printing the failures from catalog as a formatted string.
     */
    public String formatCatalogContent() {
        return "[[" + failureCatalog.size() + "]]" +
            failureCatalog.stream()
                .map(Object::toString)
                .collect(Collectors.joining("; ", "->", System.lineSeparator()));
    }

}