package com.sourcegrape;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target({
        ElementType.CONSTRUCTOR,
        ElementType.FIELD,
        ElementType.LOCAL_VARIABLE,
        ElementType.METHOD,
        ElementType.PARAMETER,
        ElementType.TYPE})
public @interface SourceGrab {
  /**
   * Allows a more compact convenience form when only the source repo uri is going to be specified.<br>
   * {@code @SourceGrab('<uri>')}<br>
   * {@code @Grab('group=junit;module=junit;version=4.8.2;classifier=javadoc')}<br>
   */
  String value() default "";

  /**
   * By default, when a {@code @SourceGrab} annotation is used, a {@code SourceGrape.grab()} call is added
   * to the static initializers of the class the annotatable node appears in.
   * If you wish to disable this, add {@code initClass=false} to the annotation.
   */
  boolean initClass() default true;

  /**
   * Git source repo URI.<br>
   */
  String uri() default "";
}
