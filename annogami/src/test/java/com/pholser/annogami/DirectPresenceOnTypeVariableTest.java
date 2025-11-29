package com.pholser.annogami;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.TypeVariable;

import static com.pholser.annogami.Presences.DIRECT;
import static java.lang.annotation.ElementType.TYPE_PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.assertj.core.api.Assertions.assertThat;

class DirectPresenceOnTypeVariableTest {
  @Retention(RUNTIME) @Target(TYPE_PARAMETER) @interface TP {
    int value();
  }

  static class GenericClass<@TP(1) T, @TP(2) U> {
  }

  @Test void findsOnTypeVariableDeclaration() {
    @SuppressWarnings("rawtypes")
    TypeVariable<Class<GenericClass>>[] typeVars =
      GenericClass.class.getTypeParameters();

    TP tp = DIRECT.find(TP.class, typeVars[0]).orElseGet(Assertions::fail);

    assertThat(tp.value()).isEqualTo(1);
  }

  @Test void missesOnGenericDeclarationWhenAnnotationIsOnTypeParameter() {
    GenericDeclaration decl = GenericClass.class;

    DIRECT.find(TP.class, decl).ifPresent(AnnotationAssertions::falseFind);
  }
}
