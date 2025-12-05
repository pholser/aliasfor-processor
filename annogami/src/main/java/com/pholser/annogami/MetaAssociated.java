package com.pholser.annogami;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class MetaAssociated {
  private final MetaWalker walker =
    new BreadthFirstMetaWalker(MetaWalkConfig.defaultsPresentStart());
  private final AnnotationSource source = Sources.PRESENT;

  MetaAssociated() {
  }

  public <A extends Annotation> List<A> find(
    Class<A> annoType,
    AnnotatedElement target) {

    Objects.requireNonNull(annoType, "annoType");
    Objects.requireNonNull(target, "target");

    List<A> results = new ArrayList<>();
    walker.walk(target).forEach(v ->
      Collections.addAll(results, source.byType(annoType, v.element())));

    return List.copyOf(results);
  }
}
