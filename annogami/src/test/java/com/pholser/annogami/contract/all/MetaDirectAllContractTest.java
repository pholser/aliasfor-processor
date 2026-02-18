package com.pholser.annogami.contract.all;

import com.pholser.annogami.All;

import static com.pholser.annogami.Presences.META_DIRECT;

final class MetaDirectAllContractTest extends AllContractTest {
  @Override protected All subject() {
    return META_DIRECT;
  }

  @Override protected boolean supportsMeta() {
    return true;
  }

  @Override protected boolean honorsInherited() {
    return false;
  }
}
