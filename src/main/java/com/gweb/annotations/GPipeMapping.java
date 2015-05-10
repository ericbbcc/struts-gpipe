package com.gweb.annotations;

import java.lang.annotation.*;

/**
 * @author float.lu
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface GPipeMapping {
    String value() default "";
}
