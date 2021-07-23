/*
 * Copyright The Narayana Authors
 *
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package io.narayana.lra.checker;

import io.narayana.lra.checker.failures.FailureCatalog;
import org.apache.maven.plugin.MojoFailureException;
import org.eclipse.microprofile.lra.tck.participant.invalid.LRAResourceWithoutCompensateOrAfteRLRA;
import org.eclipse.microprofile.lra.tck.participant.nonjaxrs.InvalidAfterLRASignatureListener;
import org.eclipse.microprofile.lra.tck.participant.nonjaxrs.InvalidArgumentTypesParticipant;
import org.eclipse.microprofile.lra.tck.participant.nonjaxrs.InvalidReturnTypeParticipant;
import org.eclipse.microprofile.lra.tck.participant.nonjaxrs.TooManyArgsParticipant;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static io.narayana.lra.checker.common.TestProcessor.initWeld;

@RunWith(Parameterized.class)
public class LraTCKCheckTest {
    private static final String LRA_TCK_MAVEN_COORDINATES = "org.eclipse.microprofile.lra:microprofile-lra-tck:1.0";

    // classes which are written a way that should fail (org.eclipse.microprofile.lra.tck.TckInvalidSignaturesTests)
    private static final List<Class<?>> failingClasses = Arrays.asList(
            LRAResourceWithoutCompensateOrAfteRLRA.class,
            InvalidReturnTypeParticipant.class,
            InvalidArgumentTypesParticipant.class,
            TooManyArgsParticipant.class,
            InvalidAfterLRASignatureListener.class
    );

    private Class<?> tckClass;

    @ClassRule
    public static final TemporaryFolder folder = new TemporaryFolder();

    @Parameterized.Parameters
    public static Collection<Object[]> generateData() throws IOException, MojoFailureException {
        File file = Maven.resolver().resolve(LRA_TCK_MAVEN_COORDINATES).withoutTransitivity().asSingleFile();
        ClassAndFilesProcessing checkerUtil = new ClassAndFilesProcessing(null);
        List<Class<?>> loadedClasses = checkerUtil.loadFromJar(file, Maven.resolver().getClass().getClassLoader());

        return loadedClasses.stream()
                // Filter is useful to run the test against a particular bean
                // .filter(clazz -> clazz == AfterLRAListener.class || clazz == LRATypeTckInterface.class)
                .map(clazz -> new Object[]{clazz})
                .collect(Collectors.toList());
    }

    public LraTCKCheckTest(Class<?> clazz) {
        this.tckClass = clazz;
    }

    @Before
    public void setUp() {
        FailureCatalog.INSTANCE.clear();
    }

    @Test
    public void testTckClassIsCorrect() {
        initWeld(tckClass);

        if (failingClasses.contains(tckClass)) {
            // expected failure
            Assert.assertFalse("A expected a failure at TCK class " + tckClass.getName() + ", "
                    + FailureCatalog.INSTANCE.formatCatalogContent(), FailureCatalog.INSTANCE.isEmpty());
        } else {
            Assert.assertTrue("No failure expected at in TCK class " + tckClass.getName() + " but there is some:"
                    + FailureCatalog.INSTANCE.formatCatalogContent(), FailureCatalog.INSTANCE.isEmpty());
        }
    }
}
