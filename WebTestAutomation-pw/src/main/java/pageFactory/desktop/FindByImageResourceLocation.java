package pageFactory.desktop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
*
* Annotation used to specify the image resource location for element identification in desktop page objects.
*/
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE})
public @interface FindByImageResourceLocation {
	
	/**
	* Specifies the image resource location for element identification.
	*
	* @return the image resource location
	*/
	String value();
}