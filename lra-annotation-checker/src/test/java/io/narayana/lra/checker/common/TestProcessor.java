/**
 * Copyright The Narayana Authors
 *
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package io.narayana.lra.checker.common;

import io.narayana.lra.checker.cdi.LraAnnotationProcessingExtension;
import io.narayana.lra.checker.failures.ErrorCode;
import io.narayana.lra.checker.failures.FailureCatalog;
import org.hamcrest.MatcherAssert;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.junit.Assert;

public final class TestProcessor {
    private TestProcessor() {
        // util class
    }

    public static void initWeld(Class<?> beanClassToCheck) {
        @SuppressWarnings("unchecked")
        Weld weld = new Weld().disableDiscovery().addExtensions(LraAnnotationProcessingExtension.class)
                .addBeanClasses(beanClassToCheck);

        try (WeldContainer container = weld.initialize()) {
            // weld intializes and works with extensions here
        }
    }

    public static void assertFailureCatalogContains(Class<?> beanClass, String failureStringToCheck) {
        Assert.assertFalse("Failure on checking bean " + beanClass.getName() + " should happen",
                FailureCatalog.INSTANCE.isEmpty());
        MatcherAssert.assertThat(FailureCatalog.INSTANCE.formatCatalogContent().split(System.lineSeparator()),
                HamcrestRegexpMatcher.matches(failureStringToCheck));
    }

    public static void assertFailureCatalogContains(Class<?> beanClass, ErrorCode errorCode, Class<?> failureDetailsClass) {
        Assert.assertFalse("Failure on checking bean " + beanClass.getName() + " should happen",
                FailureCatalog.INSTANCE.isEmpty());
        MatcherAssert.assertThat(FailureCatalog.INSTANCE.formatCatalogContent().split(System.lineSeparator()),
                HamcrestRegexpMatcher.matches(errorCode.toString() + ".*" + failureDetailsClass.getName()));
    }
}
