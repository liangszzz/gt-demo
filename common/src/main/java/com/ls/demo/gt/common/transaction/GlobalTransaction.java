package com.ls.demo.gt.common.transaction;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface GlobalTransaction {

  boolean start() default false;
}
