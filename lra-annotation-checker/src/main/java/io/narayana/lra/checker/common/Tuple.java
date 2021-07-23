/*
 * Copyright The Narayana Authors
 *
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package io.narayana.lra.checker.common;

public final class Tuple<X,Y> {
    final X key;
    final Y value;

    public static <X,Y> Tuple<X,Y> of(X key, Y value) {
        return new Tuple<>(key, value);
    }

    public Tuple(X key, Y value) {
        this.key = key;
        this.value = value;
    }

    public X getKey() {
        return key;
    }

    public Y getValue() {
        return value;
    }
}
