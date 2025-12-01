package com.pholser.annogami;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.TypeVariable;
import java.util.List;

import static com.pholser.annogami.Presences.DIRECT_OR_INDIRECT;
import static java.lang.annotation.ElementType.TYPE_PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.assertj.core.api.Assertions.assertThat;

class DirectOrIndirectPresenceOnTypeVariableTest {
  @Retention(RUNTIME) @Target(TYPE_PARAMETER) @interface TP {
    int value();
  }

  static class GenericClass<@TP(1) T, @TP(2) U> {
  }

  @Test void findsOnTypeVariableDeclaration() {
    @SuppressWarnings("rawtypes")
    TypeVariable<Class<GenericClass>>[] typeVars =
      GenericClass.class.getTypeParameters();

    List<TP> tps = DIRECT_OR_INDIRECT.find(TP.class, typeVars[0]);
    assertThat(tps).hasSize(1);

    TP tp = tps.stream().findFirst().orElseGet(Assertions::fail);
    assertThat(tp.value()).isEqualTo(1);
  }

  @Test void missesOnGenericDeclarationItself() {
    GenericDeclaration decl = GenericClass.class;

    List<TP> tps = DIRECT_OR_INDIRECT.find(TP.class, decl);

    assertThat(tps).isEmpty();
  }
}
