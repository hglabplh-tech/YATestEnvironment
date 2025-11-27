package io.github.hglabplh_tech.tests.framework.annots;


import org.junit.platform.commons.annotation.Testable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.ANNOTATION_TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Testable
public @interface YATest  {
     TestCategory category() default TestCategory.UNIT_TEST;
     String testName() ;
     String testImplDate() default "01/01/2025";
}
