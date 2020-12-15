package com.ls.libnavannotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
public @interface FragmentDestination {

    String pagerUrl();

    boolean needLogin() default false;

    boolean asStarter() default false;
}
