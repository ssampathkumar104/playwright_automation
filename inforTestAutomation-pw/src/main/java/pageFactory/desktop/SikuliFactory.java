package pageFactory.desktop;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import org.sikuli.script.Screen;

import testBase.SikuliElement;

/**
*
* Utility class for initializing Sikuli elements on a page.
*/
public class SikuliFactory { 

	/**
	 * Initializes Sikuli elements in the specified page object using the provided Sikuli Screen.
	 *
	 * @param sikuli the Sikuli Screen object
	 * @param page the page object to initialize Sikuli elements in
	 * @param <C> the type of the page object
	 */
	public static <C> void initElements(Screen sikuli, Object page) {
		Class<?> proxyIn = page.getClass();
		while (proxyIn != Object.class) {
			proxyFields(sikuli, page, proxyIn);
			proxyIn = proxyIn.getSuperclass();
		}
	}

	/**
	*
	* Proxies the fields in a page object with Sikuli elements.
	*
	* @param sikuli the Sikuli Screen object
	*
	* @param page the page object
	*
	* @param proxyIn the class to proxy fields in
	*/
	private static void proxyFields(Screen sikuli, Object page, Class<?> proxyIn) {
		
		String imgPath = page.getClass().getDeclaredAnnotation(FindByImageResourceLocation.class).value();
		String imgPath2 = imgPath.replace("\\", File.separator).replace(".", File.separator);

		if (!imgPath2.endsWith(File.separator)) {
			imgPath2 = imgPath2.concat(File.separator);
		}
		
		Field[] fields = proxyIn.getDeclaredFields();
		for (Field field : fields) {
			field.setAccessible(true);
			if (field.getType().getTypeName().contains("SikuliElement")) {
				SikuliElement sikuliElement = createSikuliElement(sikuli, field, imgPath2);
				try {
					field.set(page, sikuliElement);
				} catch (IllegalArgumentException e) {
					throw new RuntimeException(e);
				} catch (IllegalAccessException e) {
					throw new RuntimeException(e);
				}
			}

		}
	}
	
	/**
	*
	* Creates a SikuliElement based on the field annotations.
	* @param sikuli the Sikuli Screen object
	* @param field the field to create SikuliElement for
	* @param imgPath the image path for SikuliElement
	*/
	private static SikuliElement createSikuliElement(Screen sikuli, Field field, String imgPath) {
		Annotation[] annotations = field.getDeclaredAnnotations();
		String image = null;
		String[] images = { "" };
		float similarity = 70;
		int x = 0;
		int y = 0;
		for (Annotation annotation : annotations) {
			if (annotation instanceof FindBy) {
				FindBy myAnnotation = (FindBy) annotation;
				image = myAnnotation.image();
				images = myAnnotation.images();
				similarity = myAnnotation.similarity();
				x = myAnnotation.x();
				y = myAnnotation.y();
			} else if (annotation instanceof FindByImage) {
				FindByImage myAnnotation = (FindByImage) annotation;
				image = myAnnotation.value();
				similarity = myAnnotation.similarity();
				x = myAnnotation.x();
				y = myAnnotation.y();
			} else if (annotation instanceof FindByImages) {
				FindByImages myAnnotation = (FindByImages) annotation;
				images = myAnnotation.value();
				similarity = myAnnotation.similarity();
				x = myAnnotation.x();
				y = myAnnotation.y();
			}
		}
		return new SikuliElement(sikuli, imgPath, image, images, similarity, x, y);
	}

}
