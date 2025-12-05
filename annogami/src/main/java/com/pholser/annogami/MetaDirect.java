package com.pholser.annogami;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class MetaDirect implements SingleByType, All {
  private final MetaWalker walker =
    new BreadthFirstMetaWalker(MetaWalkConfig.defaultsDeclared());
  private final AnnotationSource source = Sources.DECLARED;

  @Override public <A extends Annotation> Optional<A> find(
    Class<A> annoType,
    AnnotatedElement target) {

    Objects.requireNonNull(annoType, "annoType");
    Objects.requireNonNull(target, "target");

    return walker.walk(target)
      .map(v -> source.one(annoType, v.element()))
      .filter(Objects::nonNull)
      .findFirst();
  }

  @Override public List<Annotation> all(AnnotatedElement target) {
    Objects.requireNonNull(target, "target");

    List<Annotation> results = new ArrayList<>();
    walker.walk(target).forEach(v ->
      results.addAll(
        List.of(source.all(v.element()))));

    return List.copyOf(results);
  }
}
