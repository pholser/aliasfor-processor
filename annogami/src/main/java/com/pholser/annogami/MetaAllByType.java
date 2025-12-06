package com.pholser.annogami;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

abstract sealed class MetaAllByType
  extends AbstractMeta
  implements AllByType
  permits MetaDirectOrIndirect, MetaAssociated {

  protected MetaAllByType(MetaWalker walker, AnnotationSource source) {
    super(walker, source);
  }

  @Override public final <A extends Annotation> List<A> find(
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
