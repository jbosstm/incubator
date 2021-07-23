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

package io.narayana.lra.checker.cdi;

import io.narayana.lra.checker.common.Tuple;
import io.narayana.lra.checker.failures.ErrorCode;
import io.narayana.lra.checker.failures.ErrorDetailsPrinter;
import io.narayana.lra.checker.failures.FailureCatalog;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.AnnotatedMethod;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessAnnotatedType;
import org.eclipse.microprofile.lra.annotation.AfterLRA;
import org.eclipse.microprofile.lra.annotation.Compensate;
import org.eclipse.microprofile.lra.annotation.Complete;
import org.eclipse.microprofile.lra.annotation.Forget;
import org.eclipse.microprofile.lra.annotation.LRAStatus;
import org.eclipse.microprofile.lra.annotation.ParticipantStatus;
import org.eclipse.microprofile.lra.annotation.Status;
import org.eclipse.microprofile.lra.annotation.ws.rs.Leave;
import org.jboss.logging.Logger;
import org.jboss.weld.util.collections.Sets;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Response;
import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.util.Set;
import java.util.concurrent.CompletionStage;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.narayana.lra.checker.cdi.LraAnnotationMetadata.LRA_METHOD_ANNOTATIONS;

/**
 * <p>
 * This is the annotation processing class in plugin.
 * It's written as a CDI extension that gets all LRA annotations within the project
 * and checks whether they break the rules of LRA specification.
 * </p>
 * <p>
 * All failures are stored under {@link FailureCatalog}.
 * </p>
 */
public class LraAnnotationProcessingExtension implements Extension {
    private static final Logger log = Logger.getLogger(LraAnnotationProcessingExtension.class);

    <X> void processLraAnnotatedType(@Observes ProcessAnnotatedType<X> cdiAnnotatedType) {
        log.debugf("Processing class:", cdiAnnotatedType);

        // LRA works only with instantiable classes - no abstract, no interface
        Class<X> classAnnotated = cdiAnnotatedType.getAnnotatedType().getJavaClass();
        if (classAnnotated.isAnnotation() || classAnnotated.isEnum() || classAnnotated.isInterface() || Modifier.isAbstract(classAnnotated.getModifiers())) {
            log.debugf("Skipping class: %s as it's not standard instantiable class", cdiAnnotatedType);
            return;
        }

        // Processing through the class and collecting data of LRA annotations
        LraAnnotationMetadata<X> metadata = LraAnnotationMetadata.loadMetadata(cdiAnnotatedType.getAnnotatedType());

        // Verify if the type is annotated with @LRA and contains some LRA callback annotations
        //   the only exception is @AfterLRA which is considered for being defined without @LRA in the class
        if (metadata.getLRAAnnotated().isEmpty() && metadata.containsAnLRAMethodCallbackAnnotation(AfterLRA.class)) {
            FailureCatalog.INSTANCE.add(ErrorCode.NO_LRA, "Type: " + classAnnotated);
        }
        if (metadata.getLRAAnnotated().isEmpty()) {
            log.debugf("Not an LRA type: " + classAnnotated);
            return;
        }

        // LRA type has to contain @Compensate or @AfterLRA
        if (metadata.getAnnotatedMethods(Compensate.class).isEmpty() && metadata.getAnnotatedMethods(AfterLRA.class).isEmpty()) {
            FailureCatalog.INSTANCE.add(ErrorCode.MISSING_ANNOTATIONS_COMPENSATE_AFTER_LRA,
                    "Class: " + classAnnotated.getName());
        }

        // Only one LRA annotation is permitted per class
        LRA_METHOD_ANNOTATIONS.stream()
                // @Status and @Leave could be (maybe) used multiple times within one class (spec says nothing in particular)
                .filter(clazz -> clazz == clazz )
                .map(lraAnnotation -> Tuple.of(lraAnnotation, metadata.getAnnotatedMethodsFilteredToMostConcrete(lraAnnotation)))
                .filter(t -> t.getValue().size() > 1) // multiple methods for the annotation was found
                .forEach(t -> FailureCatalog.INSTANCE.add(ErrorCode.MULTIPLE_ANNOTATIONS_OF_THE_SAME_TYPE,
                        ErrorDetailsPrinter.MULTIPLE_ANNOTATIONS.apply(classAnnotated).apply(t.getKey(), t.getValue())));

        // Multiple different LRA annotations does not make sense at the same method
        Set<Set<Class<? extends Annotation>>> lraAnnotationsCombination = LRA_METHOD_ANNOTATIONS.stream()
                .flatMap(oneLraAnnotation -> LRA_METHOD_ANNOTATIONS.stream() // generating any combination of two different LRA annotations
                        .flatMap(lraAnnotation2 -> Stream.of(Sets.newHashSet(oneLraAnnotation, lraAnnotation2))))
                .filter(s -> s.size() == 2) // filtering a set of one member, get rid of combinations of two same annotations
                .collect(Collectors.toSet());
        // processing all active methods and marking wrong any annotated with the combination of any two LRA annotations
        cdiAnnotatedType.getAnnotatedType().getMethods().stream().filter(method -> lraAnnotationsCombination.stream()
                .anyMatch(annotationSet -> annotationSet.stream().allMatch(method::isAnnotationPresent)))
                .forEach(method -> FailureCatalog.INSTANCE.add(ErrorCode.MULTIPLE_ANNOTATIONS_OF_VARIOUS_TYPES,
                        ErrorDetailsPrinter.METHOD_INFO.apply(method)));

        // -------------------------------------------------------------------------------------------
        // non-JAX-RS (CDI) style methods does not require @Path/@<method> but requires particular method signature
        metadata.getAnnotatedMostConcreteNonJaxRsMethods(Compensate.class).stream()
                // method signature for @Compensate: public void/CompleteStage compensate(URI lraId, URI parentId) { ...}
                .filter(LraAnnotationProcessingExtension.methodSignatureChecker(URI.class, URI.class))
                .forEach(method -> FailureCatalog.INSTANCE.add(ErrorCode.WRONG_METHOD_SIGNATURE_NON_JAXRS_RESOURCE,
                        ErrorDetailsPrinter.NON_JAXRS_SIGNATURE.apply(method, Compensate.class)));
        metadata.getAnnotatedMostConcreteNonJaxRsMethods(Complete.class).stream()
                .filter(LraAnnotationProcessingExtension.methodSignatureChecker(URI.class, URI.class))
                .forEach(method -> FailureCatalog.INSTANCE.add(ErrorCode.WRONG_METHOD_SIGNATURE_NON_JAXRS_RESOURCE,
                        ErrorDetailsPrinter.NON_JAXRS_SIGNATURE.apply(method, Complete.class)));
        metadata.getAnnotatedMostConcreteNonJaxRsMethods(Status.class).stream()
                .filter(LraAnnotationProcessingExtension.methodSignatureChecker(URI.class, URI.class))
                .forEach(method -> FailureCatalog.INSTANCE.add(ErrorCode.WRONG_METHOD_SIGNATURE_NON_JAXRS_RESOURCE,
                        ErrorDetailsPrinter.NON_JAXRS_SIGNATURE.apply(method, Status.class)));
        metadata.getAnnotatedMostConcreteNonJaxRsMethods(AfterLRA.class).stream()
                .filter(LraAnnotationProcessingExtension.methodSignatureChecker(URI.class, LRAStatus.class))
                .forEach(method -> FailureCatalog.INSTANCE.add(ErrorCode.WRONG_METHOD_SIGNATURE_NON_JAXRS_RESOURCE,
                        ErrorDetailsPrinter.NON_JAXRS_SIGNATURE.apply(method, AfterLRA.class)));
        metadata.getAnnotatedMostConcreteNonJaxRsMethods(Forget.class).stream()
                .filter(LraAnnotationProcessingExtension.methodSignatureChecker(URI.class, URI.class))
                .forEach(method -> FailureCatalog.INSTANCE.add(ErrorCode.WRONG_METHOD_SIGNATURE_NON_JAXRS_RESOURCE,
                        ErrorDetailsPrinter.NON_JAXRS_SIGNATURE.apply(method, Forget.class)));

        // --------------------------------------------------------------------------------------------
        // REST style methods requires all necessary REST annotations
        // @Compensate - requires @Path and @PUT
        metadata.getAnnotatedMostConcreteJaxRsMethodsWithoutCompound(Compensate.class, Path.class)
                .forEach(method -> FailureCatalog.INSTANCE.add(ErrorCode.WRONG_JAXRS_COMPLEMENTARY_ANNOTATION,
                    ErrorDetailsPrinter.MISSING_JAXRS.apply(method).apply(Compensate.class, Path.class)));
        metadata.getAnnotatedMostConcreteJaxRsMethodsWithoutCompound(Compensate.class, PUT.class)
                .forEach(method -> FailureCatalog.INSTANCE.add(ErrorCode.WRONG_JAXRS_COMPLEMENTARY_ANNOTATION,
                    ErrorDetailsPrinter.MISSING_JAXRS.apply(method).apply(Compensate.class, PUT.class)));
        // @Complete - requires @Path and @PUT
        metadata.getAnnotatedMostConcreteJaxRsMethodsWithoutCompound(Complete.class, Path.class)
                .forEach(method -> FailureCatalog.INSTANCE.add(ErrorCode.WRONG_JAXRS_COMPLEMENTARY_ANNOTATION,
                        ErrorDetailsPrinter.MISSING_JAXRS.apply(method).apply(Complete.class, Path.class)));
        metadata.getAnnotatedMostConcreteJaxRsMethodsWithoutCompound(Complete.class, PUT.class)
                .forEach(method -> FailureCatalog.INSTANCE.add(ErrorCode.WRONG_JAXRS_COMPLEMENTARY_ANNOTATION,
                        ErrorDetailsPrinter.MISSING_JAXRS.apply(method).apply(Complete.class, PUT.class)));
        // @Status or @Forget is not defined, let's verify if @Suspend is used for complete and compensate
        if (metadata.getAnnotatedMethods(Status.class).isEmpty() || metadata.getAnnotatedMethods(Forget.class).isEmpty()) {
            checkSuspendedAsync(metadata, Compensate.class);
            checkSuspendedAsync(metadata, Complete.class);
        }
        // @AfterLRA - requires @Path and @PUT
        metadata.getAnnotatedMostConcreteJaxRsMethodsWithoutCompound(AfterLRA.class, Path.class)
                .forEach(method -> FailureCatalog.INSTANCE.add(ErrorCode.WRONG_JAXRS_COMPLEMENTARY_ANNOTATION,
                        ErrorDetailsPrinter.MISSING_JAXRS.apply(method).apply(AfterLRA.class, Path.class)));
        metadata.getAnnotatedMostConcreteJaxRsMethodsWithoutCompound(AfterLRA.class, PUT.class)
                .forEach(method -> FailureCatalog.INSTANCE.add(ErrorCode.WRONG_JAXRS_COMPLEMENTARY_ANNOTATION,
                        ErrorDetailsPrinter.MISSING_JAXRS.apply(method).apply(AfterLRA.class, PUT.class)));
        // @Status - requires @Path and @GET
        metadata.getAnnotatedMostConcreteJaxRsMethodsWithoutCompound(Status.class, Path.class)
                .forEach(method -> FailureCatalog.INSTANCE.add(ErrorCode.WRONG_JAXRS_COMPLEMENTARY_ANNOTATION,
                        ErrorDetailsPrinter.MISSING_JAXRS.apply(method).apply(Status.class, Path.class)));
        metadata.getAnnotatedMostConcreteJaxRsMethodsWithoutCompound(Status.class, GET.class)
                .forEach(method -> FailureCatalog.INSTANCE.add(ErrorCode.WRONG_JAXRS_COMPLEMENTARY_ANNOTATION,
                        ErrorDetailsPrinter.MISSING_JAXRS.apply(method).apply(Status.class, GET.class)));
        // @Leave - requires @PUT
        metadata.getAnnotatedMostConcreteJaxRsMethodsWithoutCompound(Leave.class, PUT.class)
                .forEach(method -> FailureCatalog.INSTANCE.add(ErrorCode.WRONG_JAXRS_COMPLEMENTARY_ANNOTATION,
                        ErrorDetailsPrinter.MISSING_JAXRS.apply(method).apply(Leave.class, PUT.class)));
        // @Forget - requires @DELETE
        metadata.getAnnotatedMostConcreteJaxRsMethodsWithoutCompound(Forget.class, DELETE.class)
                .forEach(method -> FailureCatalog.INSTANCE.add(ErrorCode.WRONG_JAXRS_COMPLEMENTARY_ANNOTATION,
                        ErrorDetailsPrinter.MISSING_JAXRS.apply(method).apply(Forget.class, DELETE.class)));
    }

    private <X> void checkSuspendedAsync(LraAnnotationMetadata<X> metadata, Class<? extends Annotation> lraAnnotation) {
        metadata.getAnnotatedMostConcreteJaxRsMethods(lraAnnotation).forEach(method ->
                    method.getParameters().stream()
                            .flatMap(p -> p.getAnnotations().stream())
                            .filter(a -> a.annotationType().equals(Suspended.class)) // find if method parameter is annotated with @Suspended
                            .findAny().ifPresent(sa -> FailureCatalog.INSTANCE.add(ErrorCode.MISSING_SUSPEND_ASYNC_CALLBACK,
                                    ErrorDetailsPrinter.METHOD_WITH_LRA.apply(method, lraAnnotation)))
        );
    }

    private static Predicate<AnnotatedMethod<?>> methodSignatureChecker(Class<?>... expectedParameterTypes) {
        return method -> {
            // when the method is not public
            if (!Modifier.isPublic(method.getJavaMember().getModifiers())) {
                return true;
            }
            Class<?>[] parameterTypes = method.getJavaMember().getParameterTypes();
            // some number of parameters are considered but there is no parameter provided in method declaration then fail
            if (expectedParameterTypes.length > 0 && method.getJavaMember().getParameterCount() == 0) {
                return true;
            }
            // if number of declared parameters is bigger than the expected number of paramters
            if (method.getJavaMember().getParameterCount() > expectedParameterTypes.length) {
                return true;
            }
            // the number of declared method parameters do not need to match but those provided have to be of same type
            for (int i = 0; i < method.getJavaMember().getParameterCount(); i++) {
                if(!expectedParameterTypes[i].isAssignableFrom(parameterTypes[i])) return true; // one of the parameter types does not match
            }
            // return type is is one of Void, CompletionStage, ParticipantStatus or Response
            if (!method.getJavaMember().getReturnType().equals(Void.TYPE)
                    && !method.getJavaMember().getReturnType().equals(CompletionStage.class)
                    && !method.getJavaMember().getReturnType().equals(ParticipantStatus.class)
                    && !method.getJavaMember().getReturnType().equals(Response.class)) {
                return true;
            }
            // all checks passed: the method is public with matching parameter types
            return false;
        };
    }
}
