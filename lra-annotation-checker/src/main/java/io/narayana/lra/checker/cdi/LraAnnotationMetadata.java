/*
 * Copyright The Narayana Authors
 *
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package io.narayana.lra.checker.cdi;

import jakarta.enterprise.inject.spi.Annotated;
import jakarta.enterprise.inject.spi.AnnotatedMethod;
import jakarta.enterprise.inject.spi.AnnotatedType;
import org.eclipse.microprofile.lra.annotation.AfterLRA;
import org.eclipse.microprofile.lra.annotation.Compensate;
import org.eclipse.microprofile.lra.annotation.Complete;
import org.eclipse.microprofile.lra.annotation.Forget;
import org.eclipse.microprofile.lra.annotation.Status;
import org.eclipse.microprofile.lra.annotation.ws.rs.LRA;
import org.eclipse.microprofile.lra.annotation.ws.rs.Leave;

import javax.ws.rs.Path;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class LraAnnotationMetadata<X> {
    static final List<Class<? extends Annotation>> LRA_METHOD_ANNOTATIONS = Arrays.asList(
            Compensate.class,
            Complete.class,
            AfterLRA.class,
            Forget.class,
            Status.class,
            Leave.class);

    // class with @LRA
    private final AnnotatedType<X> lraAnnotatedType;
    // class hierarchy of the class with @LRA
    private final List<Class<? super X>> classHierarchy;
    // LRA annotated methods
    private final List<Annotated> listOfLRAAnnotated = new ArrayList<>();
    private final Map<Class<? extends Annotation>, List<AnnotatedMethod<?>>> annotatedMethods;
    private final Map<Class<? extends Annotation>, List<AnnotatedMethod<?>>> mostConcreteAnnotatedMethods;

    static <X> LraAnnotationMetadata<X> loadMetadata(final AnnotatedType<X> lraAnnotatedClass) {
        return new LraAnnotationMetadata<>(lraAnnotatedClass);
    }

    private LraAnnotationMetadata(final AnnotatedType<X> lraAnnotatedType) {
        Objects.requireNonNull(lraAnnotatedType);
        this.lraAnnotatedType = lraAnnotatedType;
        this.classHierarchy = getClassHierarchy(lraAnnotatedType.getJavaClass());

        if (!lraAnnotatedType.getAnnotations(LRA.class).isEmpty()) {
            listOfLRAAnnotated.add(lraAnnotatedType);
        }
        listOfLRAAnnotated.addAll(this.getMethodsForAnnotation(LRA.class));

        annotatedMethods = LRA_METHOD_ANNOTATIONS.stream()
                .collect(Collectors.toMap(Function.identity(), this::getMethodsForAnnotation));
        mostConcreteAnnotatedMethods = LRA_METHOD_ANNOTATIONS.stream()
                .collect(Collectors.toMap(Function.identity(), this::getMostConcreteMethodForAnnotation));
    }

    /**
     * Get all annotated members that contains {@link LRA} annotation.
     */
    List<Annotated> getLRAAnnotated() {
        return listOfLRAAnnotated;
    }

    /**
     * Returns true if the type contains some LRA callback annotation.
     */
    boolean containsAnLRAMethodCallbackAnnotation(Class<? extends Annotation>... filterClasses) {
        return LRA_METHOD_ANNOTATIONS.stream()
                .filter(a -> filterClasses == null || !Arrays.asList(filterClasses).contains(a))
                .anyMatch(a -> !getAnnotatedMethods(a).isEmpty());
    }

    /**
     * Returns all methods in the type hierarchy that are annotated with the annotation.
     */
    List<AnnotatedMethod<?>> getAnnotatedMethods(final Class<? extends Annotation> annotationClass) {
        return annotatedMethods.get(annotationClass);
    }

    /**
     * Based on the class hierarchy it finds the most concrete LRA callbacks, i.e.,
     * class A is a parent of class B, both declares @AfterLRA annotation, only method from B will be returned here.
     * When the class hierarchy does *not* define a method for the annotation then empty {@link List} is returned.
     */
    List<AnnotatedMethod<?>> getAnnotatedMethodsFilteredToMostConcrete(final Class<? extends Annotation> annotationClass) {
        return mostConcreteAnnotatedMethods.get(annotationClass);
    }

    /**
     * Returns all methods in the type hierarchy that are annotated with the annotation and are considered as JAX-RS
     * (i.e., the method defines the {@link javax.ws.rs.Path} annotation as well).
     */
    List<AnnotatedMethod<?>> getAnnotatedMostConcreteJaxRsMethods(final Class<? extends Annotation> lraAnnotation) {
        return getAnnotatedMostConcreteFilteredMethods(lraAnnotation, true);
    }

    /**
     * Returns all methods in the type hierarchy that are annotated with the annotation and are considered as <b>non</b>-JAX-RS
     * (i.e., the method does not define the {@link javax.ws.rs.Path} as a compound annotation).
     */
    List<AnnotatedMethod<?>> getAnnotatedMostConcreteNonJaxRsMethods(final Class<? extends Annotation> lraAnnotation) {
        return getAnnotatedMostConcreteFilteredMethods(lraAnnotation, false);
    }

    private List<AnnotatedMethod<?>> getAnnotatedMostConcreteFilteredMethods(final Class<? extends Annotation> lraAnnotation, boolean isJaxRs) {
       return getAnnotatedMethodsFilteredToMostConcrete(lraAnnotation).stream()
                .filter(method -> method.isAnnotationPresent(Path.class) == isJaxRs)
                .collect(Collectors.toList());
    }

    Stream<AnnotatedMethod<?>> getAnnotatedMostConcreteJaxRsMethodsWithoutCompound(final Class<? extends Annotation> lraAnnotation, final Class<? extends Annotation> compoundAnnotation) {
        return this.getAnnotatedMostConcreteJaxRsMethods(lraAnnotation).stream()
                .filter(m -> m.getAnnotations().stream()
                        .noneMatch(a -> a.annotationType().equals(compoundAnnotation)));
    }

    /**
     * Class hierarchy is placed into list. The lowest index comes with the most childish class,
     * the biggest index contains the Object class as the parent of all classes in java.
     */
    private List<Class<? super X>> getClassHierarchy(final Class<X> childClass) {
        List<Class<? super X>> classHierarchy = new ArrayList<>();
        if (childClass == null) return classHierarchy;
        Class<? super X> classToAdd = childClass;
        while (classToAdd != null) {
            classHierarchy.add(classToAdd);
            classToAdd = classToAdd.getSuperclass();
        }
        return classHierarchy;
    }

    /**
     * Creates a method hierarchy annotated with particular annotation.
     * Based on the list created by {@link #getClassHierarchy(Class)} it returns a list of methods
     * which are annotated in this hierarchy by the specified annotation.
     *
     * E.g. let's have a class hierarchy A -> B -> C. The class C (the most concrete one) contains a method {@link Compensate},
     * class B contains no such method, class A contains again method annotated with @{@link Compensate}.
     * Then this method returns a list with method from C at index {@code 0} and method from A at index {@code 1}.
     */
    private Collection<Method> getMethodHierarchy(final List<Class<? super X>> classHierarchy, final Class<? extends Annotation> annotationClass) {
        Collection<Method> list = new ArrayList<>();
        for (Class<?> clazz : classHierarchy) {
            for (Method m : clazz.getMethods()) {
                if (Arrays.stream(m.getAnnotations()).anyMatch(a -> a.annotationType() == annotationClass)) {
                    list.add(m);
                }
            }
        }
        return list;
    }

    /**
     * Based on the class hierarchy it finds the most concrete LRA callbacks, i.e.,
     * class A is a parent of class B, both declares @AfterLRA annotation, only method from B will be returned here.
     * When the class hierarchy does *not* define a method for the annotation then empty {@link List} is returned.
     */
    private List<AnnotatedMethod<?>> getMostConcreteMethodForAnnotation(final Class<? extends Annotation> annotationClass) {
        Optional<Method> mostConcreteAnnotatedMethod = getMethodHierarchy(classHierarchy, annotationClass).stream().findFirst();
        return getMethodsForAnnotationStream(annotationClass)
                .filter(m -> mostConcreteAnnotatedMethod.isPresent() && m.getJavaMember().getDeclaringClass() == mostConcreteAnnotatedMethod.get().getDeclaringClass())
                .collect(Collectors.toList());
    }

    /**
     * Returns all active annotated methods for the annotated type from whole type hierarchy as a stream.
     */
    private Stream<AnnotatedMethod<? super X>> getMethodsForAnnotationStream(final Class<? extends Annotation> annotationClass) {
        return lraAnnotatedType.getMethods().stream()
                .filter(m -> m.isAnnotationPresent(annotationClass));
    }

    private List<AnnotatedMethod<?>> getMethodsForAnnotation(final Class<? extends Annotation> annotationClass) {
        return getMethodsForAnnotationStream(annotationClass)
                .collect(Collectors.toList());
    }
}
