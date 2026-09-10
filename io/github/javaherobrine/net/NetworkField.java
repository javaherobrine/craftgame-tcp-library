package io.github.javaherobrine.net;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.SOURCE;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
@Documented
@Retention(SOURCE)
@Target(FIELD)
public @interface NetworkField {
	String name();
}
