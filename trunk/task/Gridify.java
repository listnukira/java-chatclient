package task;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Gridify {
	String mapper() default "mapper";
	String reducer() default "reducer";
}
