package pageFactory.desktop;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
*
* Annotation used to specify multiple image locations for element identification in desktop page objects.
*/
@Retention(RUNTIME)
@Target(FIELD)
public @interface FindByImages {
	
	/**
	* Specifies the image locations for element identification.
	*
	* @return the image locations
	*/
	String[] value() default "";
	
	/**
	 * Specifies the similarity threshold for image matching.
	 *
	 * @return the similarity threshold
	 */
	float similarity() default 70;
	
	/**
	 * Specifies the x-coordinate offset for image matching.
	 *
	 * @return the x-coordinate offset
	 */
	int x() default 0;
	
	/**
	 * Specifies the y-coordinate offset for image matching.
	 *
	 * @return the y-coordinate offset
	 */
	int y() default 0;
}