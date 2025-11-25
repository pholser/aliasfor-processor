package org.springframework.core.annotation;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Minimal test stub for Spring's {@code @AliasFor}.
 * Matches the elements that the processor uses.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface AliasFor {
  /**
   * Alias target annotation type; default is {@link
   * java.lang.annotation.Annotation}, which processor treats as "no explicit
   * annotation".
   */
  Class<? extends Annotation> annotation() default Annotation.class;

  /**
   * Alias target attribute name.
   */
  String attribute() default "";

  /**
   * Convenience alias for 'attribute'.
   */
  String value() default "";
}
