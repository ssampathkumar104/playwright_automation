package annotations;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotation used to mark a field or type as an iframe.
 */
@Retention(RUNTIME)
@Target({ TYPE, FIELD })
public @interface IFrame {

	/**
	 * The name of the iframe.
	 */
	String name() default "";

	/**
	 * The type of the iframe.
	 */
	String frameType() default "";

	/**
	 * Additional attributes of the iframe.
	 */
	String[] attributes() default "";

	/**
	 * XPath expression to locate the iframe.
	 */
	String xpath() default "";

}
