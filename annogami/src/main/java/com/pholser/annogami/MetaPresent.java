package com.pholser.annogami;

public final class MetaPresent extends MetaSingleAll {
  MetaPresent() {
    super(
      new BreadthFirstMetaWalker(MetaWalkConfig.defaultsPresentStart()),
      Sources.PRESENT);
  }
}
