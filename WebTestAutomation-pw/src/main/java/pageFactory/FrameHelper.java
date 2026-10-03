package pageFactory;

import java.lang.reflect.Field;

import org.apache.commons.lang3.StringUtils;

import com.microsoft.playwright.FrameLocator;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import annotations.IFrame;
import annotations.IFrames;
import testBase.BaseClass;

public class FrameHelper {
	
	public static void switchToFrame(final Field field) {
		if (field != null) {
			Page page = BaseClass.getDriver();
			if (page != null) {
				// Switch to main frame first
				page.mainFrame();
				if (field.getAnnotation(IFrames.class) != null) {
					final IFrames iframes = field.getAnnotation(IFrames.class);
					final IFrame[] frames = iframes.value();
					
					for (final IFrame eachFrame : frames) {
						if (!ignoreIframe(eachFrame)) {
							if (StringUtils.isNotBlank(eachFrame.xpath())) {
								page.frameLocator(eachFrame.xpath());
							} else if (StringUtils.isNotBlank(eachFrame.name())) {
								page.frameLocator("iframe[name='" + eachFrame.name() + "']");
							}
						}
					}
				}
			}
		}
	}

	private static boolean ignoreIframe(final IFrame frame) {
		final String frameToIgnore = BaseClass.getParameter("frame_to_ignore", "");
		if (StringUtils.isEmpty(frameToIgnore)) return false;
		
		final String[] attrToIgnore = frameToIgnore.split(":");
		if (attrToIgnore.length != 2) return false;
		
		if ("name".equals(attrToIgnore[0]) && frame.name() != null && frame.name().contains(attrToIgnore[1])) {
			return true;
		}
		if ("xpath".equals(attrToIgnore[0]) && frame.xpath() != null && frame.xpath().contains(attrToIgnore[1])) {
			return true;
		}
		
		for (final String attribute : frame.attributes()) {
			try {
				final String[] attPair = attribute.split("=");
				if (attrToIgnore[0].equals(attPair[0].trim()) && attPair[1].trim().contains(attrToIgnore[1])) {
					return true;
				}
			} catch (Exception e) {
				// Ignore parsing errors
			}
		}
		return false;
	}
	
	public static Locator getFrameLocator(Page page, Field field) {
		if (field != null && field.getAnnotation(IFrames.class) != null) {
			final IFrames iframes = field.getAnnotation(IFrames.class);
			final IFrame[] frames = iframes.value();
			FrameLocator frameLocator = null;
			for (final IFrame eachFrame : frames) {
				if (!ignoreIframe(eachFrame)) {
					if (StringUtils.isNotBlank(eachFrame.xpath())) {
						frameLocator = frameLocator == null ? page.frameLocator(eachFrame.xpath()) : frameLocator.frameLocator(eachFrame.xpath());
//						return page.frameLocator(eachFrame.xpath()).locator(getSelector(field));
					} else if (StringUtils.isNotBlank(eachFrame.name())) {
						frameLocator = frameLocator == null ? page.frameLocator("iframe[name='" + eachFrame.name() + "']") : frameLocator.frameLocator("iframe[name='" + eachFrame.name() + "']");
					}
				}
			}
			return frameLocator.locator(getSelector(field));
		}
		return page.locator(getSelector(field));
	}
	
	public static Locator getFrameLocator(Page page, Field field, String... values) {
		if (field != null && field.getAnnotation(IFrames.class) != null) {
			final IFrames iframes = field.getAnnotation(IFrames.class);
			final IFrame[] frames = iframes.value();
			FrameLocator frameLocator = null;
			for (final IFrame eachFrame : frames) {
				if (!ignoreIframe(eachFrame)) {
					if (StringUtils.isNotBlank(eachFrame.xpath())) {
						frameLocator = frameLocator == null ? page.frameLocator(eachFrame.xpath()) : frameLocator.frameLocator(eachFrame.xpath());
//						return page.frameLocator(eachFrame.xpath()).locator(getSelector(field));
					} else if (StringUtils.isNotBlank(eachFrame.name())) {
						frameLocator = frameLocator == null ? page.frameLocator("iframe[name='" + eachFrame.name() + "']") : frameLocator.frameLocator("iframe[name='" + eachFrame.name() + "']");
//						return page.frameLocator("iframe[name='" + eachFrame.name() + "']").locator(getSelector(field));
					}
				}
			}
			return frameLocator.locator(getSelector(field, values));
		}
		return page.locator(getSelector(field, values));
	}
	
	private static String getSelector(Field field) {
		final FindBy findBy = field.getAnnotation(FindBy.class);
		if (findBy != null && !findBy.using().isEmpty()) {
			String locator = findBy.using();
			return locator.contains("=") || locator.startsWith("//") || locator.startsWith("(//") || locator.startsWith("#") || locator.startsWith(".")  ? locator : "#" + locator;
		}
		return "#" + field.getName();
	}
	
	private static String getSelector(Field field, String... values) {
		final FindBy findBy = field.getAnnotation(FindBy.class);
		if (findBy != null && !findBy.using().isEmpty()) {
			String locator = String.format(findBy.using(), values);
			return locator.contains("=") || locator.startsWith("//") || locator.startsWith("(//") || locator.startsWith("#") || locator.startsWith(".")  ? locator : "#" + locator;
		}
		return "#" + field.getName();
	}
}