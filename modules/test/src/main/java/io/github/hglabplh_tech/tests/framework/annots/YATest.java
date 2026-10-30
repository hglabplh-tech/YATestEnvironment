package io.github.hglabplh_tech.tests.framework.annots;


import org.junit.platform.commons.annotation.Testable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.sql.Date;
import java.time.Instant;

@Target({ ElementType.ANNOTATION_TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Testable
public @interface YATest  {
     TestCategory category() default TestCategory.UNIT_TEST;
     String userDef() default "";
     String testName() ;
     String testImplDate();
}
