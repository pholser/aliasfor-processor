package com.pholser.annogami;

import java.util.Objects;

sealed abstract class AbstractMeta
  permits MetaSingleAll, MetaAllByType {

  protected final MetaWalker walker;
  protected final AnnotationSource source;

  protected AbstractMeta(MetaWalker walker, AnnotationSource source) {
    this.walker = Objects.requireNonNull(walker);
    this.source = Objects.requireNonNull(source);
  }
}
