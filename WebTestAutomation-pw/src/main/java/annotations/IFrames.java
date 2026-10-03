package annotations;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotation used to represent a collection of IFrames.
 */
@Retention(RUNTIME)
@Target({ TYPE, FIELD })
public @interface IFrames {
	
	/**
	 * The array of IFrame annotations.
	 */
	IFrame[] value();
}
