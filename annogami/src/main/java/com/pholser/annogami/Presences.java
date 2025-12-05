package com.pholser.annogami;

public final class Presences {
  public static final Direct DIRECT = new Direct();
  public static final DirectOrIndirect DIRECT_OR_INDIRECT =
    new DirectOrIndirect();
  public static final Present PRESENT = new Present();
  public static final Associated ASSOCIATED = new Associated();
  public static final MetaDirect META_DIRECT = new MetaDirect();
  public static final MetaDirectOrIndirect META_DIRECT_OR_INDIRECT =
    new MetaDirectOrIndirect();
  public static final MetaPresent META_PRESENT = new MetaPresent();
  public static final MetaAssociated META_ASSOCIATED = new MetaAssociated();

  private Presences() {
    throw new AssertionError();
  }
}
