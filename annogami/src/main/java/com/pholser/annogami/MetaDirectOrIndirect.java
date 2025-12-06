package com.pholser.annogami;

public final class MetaDirectOrIndirect extends MetaAllByType {
  MetaDirectOrIndirect() {
    super(
      new BreadthFirstMetaWalker(MetaWalkConfig.defaultsDeclared()),
      Sources.DECLARED);
  }
}
