/*
 * Copyright The Narayana Authors
 *
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package io.narayana.lra.checker.failures;

import jakarta.enterprise.inject.spi.AnnotatedMethod;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Locale;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class ErrorDetailsPrinter {
    private ErrorDetailsPrinter() {
        // utility
    }

    public static final Function<Class<?>, BiFunction<Class<? extends Annotation>, List<AnnotatedMethod<?>>, String>> MULTIPLE_ANNOTATIONS =
            annotatedLraClass -> (clazz, methods) ->
                    String.format(
                        "Multiple annotations '%s' in the class '%s' on methods %s.",
                        clazz.getName(), annotatedLraClass, toMethodNames(methods));

    public static final Function<AnnotatedMethod<?>, String> METHOD_INFO =
            method -> String.format("Method '%s', class '%s', annotations '%s'.",
                    method.getJavaMember().getName(),
                    method.getJavaMember().getDeclaringClass(), method.getAnnotations());

    public static final BiFunction<AnnotatedMethod<?>, Class<? extends Annotation>, String> METHOD_WITH_LRA =
            (method, lraAnnotation) -> String.format("Class '%s', method '%s' annotated with LRA '%s'.",
                    method.getJavaMember().getDeclaringClass(), method.getJavaMember().getName(), lraAnnotation);

    private static final String signatureFormat = "public void/CompletionStage/ParticipantStatus %s(java.net.URI lraId, java.net.URI parentId)";
    public static final BiFunction<AnnotatedMethod<?>, Class<? extends Annotation>, String> NON_JAXRS_SIGNATURE =
            (method, lraAnnotation) ->
                String.format("Signature for annotation '%s' in the class '%s' on method '%s'. It should be '%s'",
                        lraAnnotation.getName(), method.getJavaMember().getDeclaringClass().getName(), method.getJavaMember().getName(),
                        String.format(signatureFormat, lraAnnotation.getSimpleName().toLowerCase(Locale.ROOT)));

    public static final Function<AnnotatedMethod<?>, BiFunction<Class<? extends Annotation>, Class<? extends Annotation>, String>> MISSING_JAXRS =
            method -> (lraAnnotation, missingAnnotation) ->
                String.format("Method '%s' of class '%s' annotated with '%s' misses complementary annotation %s.",
                    method.getJavaMember().getName(), method.getJavaMember().getDeclaringClass(),
                        lraAnnotation.getName(), missingAnnotation.getName());

    private static List<String> toMethodNames(List<AnnotatedMethod<?>> annotatedMethods) {
        return annotatedMethods.stream().map(a -> a.getJavaMember().getName()).collect(Collectors.toList());
    }
}
