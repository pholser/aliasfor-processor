package com.pholser.annogami;

public final class MetaAssociated extends MetaAllByType {
  MetaAssociated() {
    super(
      new BreadthFirstMetaWalker(MetaWalkConfig.defaultsPresentStart()),
      Sources.PRESENT);
  }
}
