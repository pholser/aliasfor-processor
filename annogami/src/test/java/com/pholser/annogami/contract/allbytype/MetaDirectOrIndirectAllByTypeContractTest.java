package com.pholser.annogami.contract.allbytype;

import com.pholser.annogami.AllByType;

import static com.pholser.annogami.Presences.META_DIRECT_OR_INDIRECT;

final class MetaDirectOrIndirectAllByTypeContractTest extends AllByTypeContractTest {
  @Override protected AllByType subject() {
    return META_DIRECT_OR_INDIRECT;
  }

  @Override protected boolean supportsMeta() {
    return true;
  }

  @Override protected boolean honorsInherited() {
    return false;
  }
}
