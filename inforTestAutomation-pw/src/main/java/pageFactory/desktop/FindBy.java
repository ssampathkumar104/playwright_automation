package pageFactory.desktop;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
*
* Annotation used to locate elements in desktop page objects.
*/
@Retention(RUNTIME)
@Target(FIELD)
public @interface FindBy {
	String image() default "";
	
	/**
	 * Specifies multiple image file paths or URLs for element identification.
	 *
	 * @return an array of image file paths or URLs
	 */
	String[] images() default {""};
	
	/**
	 * Specifies the similarity threshold for image matching, ranging from 0 to 100.
	 * Default value is 70.
	 *
	 * @return the similarity threshold
	 */
	float similarity() default 70;
	
	/**
	 * Specifies the x-coordinate offset for element identification.
	 * Default value is 0.
	 *
	 * @return the x-coordinate offset
	 */
	int x() default 0;
	
	/**
	 * Specifies the y-coordinate offset for element identification.
	 * Default value is 0.
	 *
	 * @return the y-coordinate offset
	 */
	int y() default 0;
}