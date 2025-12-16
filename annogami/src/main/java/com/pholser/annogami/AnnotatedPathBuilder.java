package com.pholser.annogami;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Executable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.pholser.annogami.ClassHierarchies.breadthFirstHierarchyOf;
import static com.pholser.annogami.ClassHierarchies.breadthFirstOverrideHierarchyOf;
import static com.pholser.annogami.ClassHierarchies.depthFirstHierarchyOf;
import static com.pholser.annogami.ClassHierarchies.depthFirstOverrideHierarchyOf;

/**
 * Annotated path builder; builds and retains a sequence of annotated elements.
 */
public sealed class AnnotatedPathBuilder
  extends AnnotatedPath.SegmentBuilder
  permits AnnotatedPathBuilder.Parameter,
  AnnotatedPathBuilder.RecordComponent,
  AnnotatedPathBuilder.Field,
  AnnotatedPathBuilder.Constructor,
  AnnotatedPathBuilder.Method,
  AnnotatedPathBuilder.Methods,
  AnnotatedPathBuilder.Class,
  AnnotatedPathBuilder.Classes,
  AnnotatedPathBuilder.Package,
  AnnotatedPathBuilder.Module {

  /**
   * This segment builder's predecessor annotated elements.
   */
  protected final List<AnnotatedElement> predecessors = new ArrayList<>();

  /**
   * Protected for subclasses.
   */
  protected AnnotatedPathBuilder() {
  }

  @Override protected List<AnnotatedElement> predecessors() {
    return new ArrayList<>(predecessors);
  }

  /**
   * Initiates an annotated path at the given parameter.
   *
   * @param p a parameter
   * @return a builder object that can extend the path from the parameter
   */
  public static Parameter fromParameter(java.lang.reflect.Parameter p) {
    return new Parameter(p);
  }

  /**
   * Initiates an annotated path at the given method.
   *
   * @param m a method
   * @return a builder object that can extend the path from the method
   */
  public static Method fromMethod(java.lang.reflect.Method m) {
    return new Method(m);
  }

  /**
   * Initiates an annotated path at the given constructor.
   *
   * @param c a constructor
   * @return a builder object that can extend the path from the constructor
   */
  public static Constructor fromConstructor(
    java.lang.reflect.Constructor<?> c) {

    return new Constructor(c);
  }

  /**
   * Initiates an annotated path at the given field.
   *
   * @param f a field
   * @return a builder object that can extend the path from the field
   */
  public static Field fromField(java.lang.reflect.Field f) {
    return new Field(f);
  }

  /**
   * Initiates an annotated path at the given record component.
   *
   * @param r a record component
   * @return a builder object that can extend the path from the record
   * component
   */
  public static RecordComponent fromRecordComponent(
    java.lang.reflect.RecordComponent r) {

    return new RecordComponent(r);
  }

  /**
   * Initiates an annotated path at the given class.
   *
   * @param k a class
   * @return a builder object that can extend the path from the class
   */
  public static Class fromClass(java.lang.Class<?> k) {
    return new Class(k);
  }

  /**
   * Builder segment that can extend the in-progress path from a given
   * method or constructor parameter.
   */
  public static final class Parameter extends AnnotatedPathBuilder {
    private final java.lang.reflect.Parameter p;

    Parameter(java.lang.reflect.Parameter p) {
      this.p = p;
      predecessors.add(p);
    }

    /**
     * Extends the in-progress path from this segment's parameter to that
     * parameter's declaring constructor.
     *
     * @return builder segment focused at that constructor
     * @throws IllegalStateException if this segment's parameter is not a
     * constructor parameter
     */
    public Constructor toDeclaringConstructor() {
      Executable exec = p.getDeclaringExecutable();
      if (!(exec instanceof java.lang.reflect.Constructor<?> c)) {
        throw new IllegalStateException(
          "Parameter " + p + " not declared on a constructor");
      }

      return new Constructor(c, predecessors);
    }

    /**
     * Extends the in-progress path from this segment's parameter to that
     * parameter's declaring method.
     *
     * @return builder segment focused at that method
     * @throws IllegalStateException if this segment's parameter is not a
     * method parameter
     */
    public Method toDeclaringMethod() {
      Executable exec = p.getDeclaringExecutable();
      if (!(exec instanceof java.lang.reflect.Method m)) {
        throw new IllegalStateException(
          "Parameter " + p + " not declared on a method");
      }

      return new Method(m, predecessors);
    }
  }

  /**
   * Builder segment that can extend the in-progress path from a given
   * constructor.
   */
  public static final class Constructor extends AnnotatedPathBuilder {
    private final java.lang.reflect.Constructor<?> c;

    Constructor(java.lang.reflect.Constructor<?> c) {
      this(c, Collections.emptyList());
    }

    Constructor(
      java.lang.reflect.Constructor<?> c,
      List<AnnotatedElement> predecessors) {

      this.c = c;
      this.predecessors.addAll(predecessors);
      this.predecessors.add(c);
    }

    /**
     * Extends the in-progress path from this segment's constructor to that
     * constructor's declaring class.
     *
     * @return builder segment focused at that class
     */
    public Class toDeclaringClass() {
      return new Class(c.getDeclaringClass(), predecessors);
    }
  }

  /**
   * Builder segment that can extend the in-progress path from a given method.
   */
  public static final class Method extends AnnotatedPathBuilder {
    private final java.lang.reflect.Method m;

    Method(java.lang.reflect.Method m) {
      this(m, Collections.emptyList());
    }

    Method(
      java.lang.reflect.Method m,
      List<AnnotatedElement> predecessors) {

      this.m = m;
      this.predecessors.addAll(predecessors);
      this.predecessors.add(m);
    }

    /**
     * Extends the in-progress path from this segment's method to that
     * method's declaring class.
     *
     * @return builder segment focused at that class
     */
    public Class toDeclaringClass() {
      return new Class(m.getDeclaringClass(), predecessors);
    }

    /**
     * Extends the in-progress path from this segment's method through all
     * methods in the class hierarchy that this segment's method overrides
     * (depth-first; superclass, then directly implemented interfaces).
     * This is effectively a terminal operation, since the resulting
     * builder segment affords no operation other than {@link #build()}.
     *
     * @return builder segment focused after the method override chain
     */
    public Methods toDepthOverridden() {
      return new Methods(depthFirstOverrideHierarchyOf(m), predecessors);
    }

    /**
     * Extends the in-progress path from this segment's method through all
     * methods in the class hierarchy that this segment's method overrides
     * (breadth-first; superclass, then directly implemented interfaces).
     * This is effectively a terminal operation, since the resulting
     * builder segment affords no operation other than {@link #build()}.
     *
     * @return builder segment focused after the method override chain
     */
    public Methods toBreadthOverridden() {
      return new Methods(breadthFirstOverrideHierarchyOf(m), predecessors);
    }
  }

  /**
   * Builder segment that can extend the in-progress path from a given field.
   */
  public static final class Field extends AnnotatedPathBuilder {
    private final java.lang.reflect.Field f;

    Field(java.lang.reflect.Field f) {
      this.f = f;
      predecessors.add(f);
    }

    /**
     * Extends the in-progress path from this segment's field to that
     * field's declaring class.
     *
     * @return builder segment focused at that class
     */
    public Class toDeclaringClass() {
      return new Class(f.getDeclaringClass(), predecessors);
    }
  }

  /**
   * Builder segment that can extend the in-progress path from a given record
   * component.
   */
  public static final class RecordComponent
    extends AnnotatedPathBuilder {

    private final java.lang.reflect.RecordComponent r;

    RecordComponent(java.lang.reflect.RecordComponent r) {
      this.r = r;
      predecessors.add(r);
    }

    /**
     * Extends the in-progress path from this segment's record component to
     * that record component's declaring class.
     *
     * @return builder segment focused at that class
     */
    public Class toDeclaringRecord() {
      return new Class(r.getDeclaringRecord(), predecessors);
    }
  }

  /**
   * Builder segment that can extend the in-progress path from a given class.
   */
  public static final class Class extends AnnotatedPathBuilder {
    private final java.lang.Class<?> k;

    Class(java.lang.Class<?> k) {
      this(k, Collections.emptyList());
    }

    Class(
      java.lang.Class<?> k,
      List<AnnotatedElement> predecessors) {

      this.k = k;
      this.predecessors.addAll(predecessors);
      this.predecessors.add(k);
    }

    /**
     * Extends the in-progress path from this segment's class to that
     * class's declaring package.
     *
     * @return builder segment focused at that package
     */
    public Package toDeclaringPackage() {
      return new Package(k.getPackage(), predecessors);
    }

    /**
     * Extends the in-progress path from this segment's class to that
     * class's declaring module.
     *
     * @return builder segment focused at that module
     */
    public Module toDeclaringModule() {
      return new Module(k.getModule(), predecessors);
    }

    /**
     * Extends the in-progress path from this segment's class to that
     * class's enclosing method.
     *
     * @return builder segment focused at that method
     * @throws IllegalStateException if this segment's class is not a local
     * or anonymous class
     */
    public Method toEnclosingMethod() {
      java.lang.reflect.Method enclosing = k.getEnclosingMethod();
      if (enclosing == null) {
        throw new IllegalStateException(k + " has no enclosing method");
      }

      return new Method(enclosing, predecessors);
    }

    /**
     * Extends the in-progress path from this segment's class to that
     * class's enclosing constructor.
     *
     * @return builder segment focused at that constructor
     * @throws IllegalStateException if this segment's class is not a local
     * or anonymous class
     */
    public Constructor toEnclosingConstructor() {
      java.lang.reflect.Constructor<?> enclosing =
        k.getEnclosingConstructor();
      if (enclosing == null) {
        throw new IllegalStateException(k + " has no enclosing constructor");
      }

      return new Constructor(enclosing, predecessors);
    }

    /**
     * Extends the in-progress path from this segment's class through all
     * its nesting classes, from innermost to outermost. This is effectively
     * a terminal operation, since the resulting builder segment affords no
     * operation other than {@link #build()}.
     *
     * @return builder segment focused after the class enclosure chain
     */
    public Classes toClassEnclosure() {
      List<java.lang.Class<?>> enclosure = new ArrayList<>();
      for (java.lang.Class<?> c = k.getEnclosingClass();
           c != null;
           c = c.getEnclosingClass()) {

        enclosure.add(c);
      }

      return new Classes(enclosure, predecessors);
    }

    /**
     * Extends the in-progress path from this segment's class through all
     * classes in its hierarchy (depth-first; superclass, then directly
     * implemented interfaces). This is effectively a terminal operation,
     * since the resulting builder segment affords no operation other than
     * {@link #build()}.
     *
     * @return builder segment focused after the class hierarchy chain
     */
    public Classes toDepthHierarchy() {
      return new Classes(depthFirstHierarchyOf(k), predecessors);
    }

    /**
     * Extends the in-progress path from this segment's class through all
     * classes in its hierarchy (breadth-first; superclass, then directly
     * implemented interfaces). This is effectively a terminal operation,
     * since the resulting builder segment affords no operation other than
     * {@link #build()}.
     *
     * @return builder segment focused after the class hierarchy chain
     */
    public Classes toBreadthHierarchy() {
      return new Classes(breadthFirstHierarchyOf(k), predecessors);
    }
  }

  /**
   * Terminal builder segment representing a sequence of methods.
   */
  public static final class Methods extends AnnotatedPathBuilder {
    Methods(
      List<java.lang.reflect.Method> methods,
      List<AnnotatedElement> history) {

      predecessors.addAll(history);
      predecessors.addAll(methods);
    }
  }

  /**
   * Terminal builder segment representing a sequence of classes.
   */
  public static final class Classes extends AnnotatedPathBuilder {
    Classes(
      List<java.lang.Class<?>> classes,
      List<AnnotatedElement> history) {

      predecessors.addAll(history);
      predecessors.addAll(classes);
    }
  }

  /**
   * Terminal builder segment representing a package.
   */
  public static final class Package extends AnnotatedPathBuilder {
    Package(
      java.lang.Package p,
      List<AnnotatedElement> history) {

      predecessors.addAll(history);
      predecessors.add(p);
    }
  }

  /**
   * Terminal builder segment representing a module.
   */
  public static final class Module extends AnnotatedPathBuilder {
    Module(
      java.lang.Module m,
      List<AnnotatedElement> history) {

      predecessors.addAll(history);
      predecessors.add(m);
    }
  }
}
