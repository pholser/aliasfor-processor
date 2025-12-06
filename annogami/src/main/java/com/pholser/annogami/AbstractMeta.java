package com.pholser.annogami;

sealed abstract class AbstractMeta
  permits MetaSingleAll, MetaAllByType {

  protected final MetaWalker walker;
  protected final AnnotationSource source;

  protected AbstractMeta(MetaWalker walker, AnnotationSource source) {
    this.walker = walker;
    this.source = source;
  }
}
