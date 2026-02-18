package com.pholser.annogami.contract.allbytype;

import com.pholser.annogami.AllByType;

import static com.pholser.annogami.Presences.ASSOCIATED;

final class AssociatedAllByTypeContractTest extends AllByTypeContractTest {
  @Override protected AllByType subject() {
    return ASSOCIATED;
  }

  @Override protected boolean supportsMeta() {
    return false;
  }

  @Override protected boolean honorsInherited() {
    return true;
  }
}
