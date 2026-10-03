package pageFactory;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.stream.Stream;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.options.WaitForSelectorState;

import testBase.listners.EventHandler;

public class DynamicWebElementLocator {
	private final Field field;
	private final Page page;

	public DynamicWebElementLocator(Field field, Page page) {
		this.field = field;
		this.page = page;
	}

	private Locator getFrameLocator(String... values) {
		return FrameHelper.getFrameLocator(page, field, values);
	}

	public void click(String firstValue, String... otherValues) {
		String[] values = Stream.concat(Stream.of(firstValue), Arrays.stream(otherValues)).toArray(String[]::new);
		new EventHandler().beforeClick(getFrameLocator(values).first());
		getFrameLocator(values).first().click();
	}

	public void forceClick(String firstValue, String... otherValues) {
		String[] values = Stream.concat(Stream.of(firstValue), Arrays.stream(otherValues)).toArray(String[]::new);
		new EventHandler().beforeClick(getFrameLocator(values).first());
		getFrameLocator(values).first().click(new Locator.ClickOptions().setForce(true));
	}

	public void fill(String text, String firstValue, String... otherValues) {
		String[] values = Stream.concat(Stream.of(firstValue), Arrays.stream(otherValues)).toArray(String[]::new);
		getFrameLocator(values).first().fill(text);
		new EventHandler().afterFill(getFrameLocator(values).first(), text);
	}

	public String textContent(String firstValue, String... otherValues) {
		String[] values = Stream.concat(Stream.of(firstValue), Arrays.stream(otherValues)).toArray(String[]::new);
		return getFrameLocator(values).first().textContent();
	}

	public boolean isVisible(String firstValue, String... otherValues) {
		String[] values = Stream.concat(Stream.of(firstValue), Arrays.stream(otherValues)).toArray(String[]::new);
		return getFrameLocator(values).first().isVisible();
	}

	public void hover(String firstValue, String... otherValues) {
		String[] values = Stream.concat(Stream.of(firstValue), Arrays.stream(otherValues)).toArray(String[]::new);
		getFrameLocator(values).first().hover();
	}

	public void selectOption(String value, String firstValue, String... otherValues) {
		String[] values = Stream.concat(Stream.of(firstValue), Arrays.stream(otherValues)).toArray(String[]::new);
		getFrameLocator(values).first().selectOption(value);
	}

	public void check(String firstValue, String... otherValues) {
		String[] values = Stream.concat(Stream.of(firstValue), Arrays.stream(otherValues)).toArray(String[]::new);
		getFrameLocator(values).first().check();
	}

	public void uncheck(String firstValue, String... otherValues) {
		String[] values = Stream.concat(Stream.of(firstValue), Arrays.stream(otherValues)).toArray(String[]::new);
		getFrameLocator(values).first().uncheck();
	}

	public String getAttribute(String name, String firstValue, String... otherValues) {
		String[] values = Stream.concat(Stream.of(firstValue), Arrays.stream(otherValues)).toArray(String[]::new);
		return getFrameLocator(values).first().getAttribute(name);
	}
	
	public String getText(String firstValue, String... otherValues) {
		String[] values = Stream.concat(Stream.of(firstValue), Arrays.stream(otherValues)).toArray(String[]::new);
		return getFrameLocator(values).first().textContent();
	}

	public boolean isElementPresent(String firstValue, String... otherValues) {
		String[] values = Stream.concat(Stream.of(firstValue), Arrays.stream(otherValues)).toArray(String[]::new);
		try {
			getFrameLocator(values).first().waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(1000));
            // If no exception, element became visible
            return true;
        } catch (PlaywrightException e) {
            // Timeout or other exception means element not visible in given time
            return false;
        }
	}
	
	public boolean isElementPresent(int waitTime, String firstValue, String... otherValues) {
		String[] values = Stream.concat(Stream.of(firstValue), Arrays.stream(otherValues)).toArray(String[]::new);
		try {
			getFrameLocator(values).first().waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(waitTime * 1000));
            // If no exception, element became visible
            return true;
        } catch (PlaywrightException e) {
            // Timeout or other exception means element not visible in given time
            return false;
        }
	}

	public Locator getLocator(String firstValue, String... otherValues) {
		String[] values = Stream.concat(Stream.of(firstValue), Arrays.stream(otherValues)).toArray(String[]::new);
		return getFrameLocator(values);
	}
	
}