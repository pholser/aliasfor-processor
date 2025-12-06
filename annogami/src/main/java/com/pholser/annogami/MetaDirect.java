package com.pholser.annogami;

public final class MetaDirect extends MetaSingleAll {
  MetaDirect() {
    super(
      new BreadthFirstMetaWalker(MetaWalkConfig.defaultsDeclared()),
      Sources.DECLARED);
  }
}
