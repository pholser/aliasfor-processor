package com.pholser.annogami;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class Present implements Single, All {
  Present() {
  }

  @Override
  public <A extends Annotation> Optional<A> find(
    Class<A> annoType,
    AnnotatedElement target) {

    return Optional.ofNullable(Sources.PRESENT.one(annoType, target));
  }

  @Override
  public <A extends Annotation> Optional<A> find(
    Class<A> annoType,
    AnnotatedElement target,
    Aliasing aliasing) {

    Objects.requireNonNull(annoType, "type");
    Objects.requireNonNull(target, "target");
    Objects.requireNonNull(aliasing, "aliasing");
    return SegmentResolver.withSeedSource(Sources.PRESENT)
      .findFirst(annoType, target, this, aliasing);
  }

  @Override
  public List<Annotation> all(AnnotatedElement target) {
    return List.of(Sources.PRESENT.all(target));
  }
}
