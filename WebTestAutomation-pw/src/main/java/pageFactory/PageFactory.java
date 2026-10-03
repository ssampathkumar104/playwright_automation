package pageFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public class PageFactory {

	public static <T> T initElements(Page page, Class<T> pageClassToProxy) {
		T pageObject = instantiatePage(page, pageClassToProxy);
		initElements(page, pageObject);
		return pageObject;
	}

	public static void initElements(Page page, Object pageObject) {
		Class<?> proxyIn = pageObject.getClass();
		while (proxyIn != Object.class) {
			proxyFields(page, pageObject, proxyIn);
			proxyIn = proxyIn.getSuperclass();
		}
	}

	private static void proxyFields(Page page, Object pageObject, Class<?> proxyIn) {
		for (Field field : proxyIn.getDeclaredFields()) {
			Class<?> fieldType = field.getType();
			Object instance = null;

			if (fieldType == WebElementLocator.class) {
				instance = new WebElementLocator(field, page);
			} else if (fieldType == DynamicWebElementLocator.class) {
				instance = new DynamicWebElementLocator(field, page);
			} else if (fieldType == Locator.class) {
				FindBy findBy = field.getAnnotation(FindBy.class);
				if (findBy != null && !findBy.using().isEmpty()) {
					instance = page.locator(findBy.using());
				}
			} else if (fieldType == List.class) {
				Type genericType = field.getGenericType();
				if (genericType instanceof ParameterizedType) {
					Type[] typeArgs = ((ParameterizedType) genericType).getActualTypeArguments();
					if (typeArgs.length > 0) {
						FindBy findBy = field.getAnnotation(FindBy.class);
						if (findBy != null && !findBy.using().isEmpty()) {
							if (typeArgs[0] == Locator.class) {
								instance = page.locator(findBy.using()).all();
							} else if (typeArgs[0] == WebElementLocator.class) {
								List<WebElementLocator> list = new ArrayList<>();
								list.add(new WebElementLocator(field, page));
								instance = list;
							} else if (typeArgs[0] == DynamicWebElementLocator.class) {
								List<DynamicWebElementLocator> list = new ArrayList<>();
								list.add(new DynamicWebElementLocator(field, page));
								instance = list;
							}
						}
					}
				}
			}

			if (instance != null) {
				try {
					field.setAccessible(true);
					field.set(pageObject, instance);
				} catch (IllegalAccessException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

	private static <T> T instantiatePage(Page page, Class<T> pageClassToProxy) {
		try {
			try {
				Constructor<T> constructor = pageClassToProxy.getConstructor(Page.class);
				return constructor.newInstance(page);
			} catch (NoSuchMethodException e) {
				return pageClassToProxy.getDeclaredConstructor().newInstance();
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}