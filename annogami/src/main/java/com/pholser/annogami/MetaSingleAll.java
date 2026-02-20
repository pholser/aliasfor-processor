package com.pholser.annogami;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

abstract sealed class MetaSingleAll
  extends AbstractMeta
  implements Single, All
  permits MetaDirect, MetaPresent {

  private final SegmentResolver resolver;

  protected MetaSingleAll(MetaWalker walker, AnnotationSource source) {
    super(walker, source);
    this.resolver = SegmentResolver.withSeedSource(source);
  }

  @Override
  public final <A extends Annotation> Optional<A> find(
    Class<A> annoType,
    AnnotatedElement target) {

    Objects.requireNonNull(annoType, "type");
    Objects.requireNonNull(target, "target");

    return walker.walk(target)
      .map(v -> source.one(annoType, v.element()))
      .filter(Objects::nonNull)
      .findFirst();
  }

  @Override
  public final <A extends Annotation> Optional<A> find(
    Class<A> annoType,
    AnnotatedElement target,
    Aliasing aliasing) {

    Objects.requireNonNull(annoType, "type");
    Objects.requireNonNull(target, "target");
    Objects.requireNonNull(aliasing, "aliasing");

    return resolver.findFirst(annoType, target, this, aliasing);
  }

  @Override
  public final List<Annotation> all(AnnotatedElement target) {
    Objects.requireNonNull(target, "target");

    List<Annotation> results = new ArrayList<>();
    walker.walk(target).forEach(v ->
      Collections.addAll(results, source.all(v.element())));

    return List.copyOf(results);
  }

  @Override
  public final List<Annotation> all(
    AnnotatedElement target,
    Aliasing aliasing) {

    Objects.requireNonNull(target, "target");
    Objects.requireNonNull(aliasing, "aliasing");

    return resolver.all(target, all(target), aliasing);
  }
}
