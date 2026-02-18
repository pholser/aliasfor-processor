package com.pholser.annogami.contract.allbytype;

import com.pholser.annogami.AllByType;

import static com.pholser.annogami.Presences.META_ASSOCIATED;

final class MetaAssociatedAllByTypeContractTest extends AllByTypeContractTest {
  @Override protected AllByType subject() {
    return META_ASSOCIATED;
  }

  @Override protected boolean supportsMeta() {
    return true;
  }

  @Override protected boolean honorsInherited() {
    return true;
  }
}
