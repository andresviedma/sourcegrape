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
   * Allows a more compact convenience form in one of two formats with optional appended attributes.
   * Must not be used if group(), module() or version() are used.
   * <p>
   * You can choose either format but not mix-n-match:<br>
   * {@code group:module:version:classifier@ext} (where only group and module are required)<br>
   * {@code group#module;version[confs]} (where only group and module are required and confs,
   * if used, is one or more comma separated configuration names)<br>
   * In addition, you can add any valid Ivy attributes at the end of your string value using
   * semi-colon separated name = value pairs, e.g.:<br>
   * {@code @Grab('junit:junit:*;transitive=false')}<br>
   * {@code @Grab('group=junit;module=junit;version=4.8.2;classifier=javadoc')}<br>
   */
  String value() default "";

  /**
   * By default, when a {@code @Grab} annotation is used, a {@code Grape.grab()} call is added
   * to the static initializers of the class the annotatable node appears in.
   * If you wish to disable this, add {@code initClass=false} to the annotation.
   */
  boolean initClass() default true;
  
  String uri() default "";
}
