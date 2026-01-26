package com.pholser.annogami;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public final class AnnotatedPath {
  private final List<AnnotatedElement> elements;

  public AnnotatedPath(List<AnnotatedElement> elements) {
    this.elements = List.copyOf(elements);
  }

  public <A extends Annotation> Optional<A> findFirst(
    Class<A> annoType,
    Single detector) {

    return Optional.empty();
  }

  public List<Annotation> all(All detector) {
    return Collections.emptyList();
  }

  public <A extends Annotation> List<A> find(
    Class<A> annoType,
    AllByType detector) {

    return Collections.emptyList();
  }

  /**
   * Abstract class for annotated path segment builders. Builders may structure
   * themselves however they wish; but ultimately must provide a sequence of
   * annotated elements from which another segment may continue the path.
   */
  public static abstract class SegmentBuilder {
    /**
     * Make a new path builder.
     */
    protected SegmentBuilder() {
    }

    /**
     * Complete an in-progress path.
     *
     * @return a complete path
     */
    public final AnnotatedPath build() {
      return new AnnotatedPath(predecessors());
    }

    /**
     * Gives a sequence of "predecessor" annotated elements for an annotated
     * path builder as it continues a path.
     *
     * @return predecessor elements in sequence
     */
    protected abstract List<AnnotatedElement> predecessors();
  }
}
