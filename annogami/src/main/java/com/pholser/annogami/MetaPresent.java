package com.pholser.annogami;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class MetaPresent {
  private final MetaWalker walker =
    new BreadthFirstMetaWalker(MetaWalkConfig.defaultsPresentStart());
  private final AnnotationSource source = Sources.PRESENT;

  public <A extends Annotation> Optional<A> findFirst(
    Class<A> annoType,
    AnnotatedElement target) {

    Objects.requireNonNull(annoType, "annoType");
    Objects.requireNonNull(target, "target");

    return walker.walk(target)
      .map(v -> source.one(annoType, v.element()))
      .filter(Objects::nonNull)
      .findFirst();
  }

  public List<Annotation> all(AnnotatedElement target) {
    Objects.requireNonNull(target, "target");

    List<Annotation> results = new ArrayList<>();
    walker.walk(target).forEach(v ->
      Collections.addAll(results, source.all(v.element())));

    return List.copyOf(results);
  }
}
