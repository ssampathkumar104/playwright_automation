package pageFactory;

import java.lang.reflect.Field;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.options.WaitForSelectorState;

import testBase.listners.EventHandler;

public class WebElementLocator {
    private final Field field;
    private final Page page;

    public WebElementLocator(Field field, Page page) {
        this.field = field;
        this.page = page;
    }

    private Locator getFrameLocator() {
        return FrameHelper.getFrameLocator(page, field);
    }

    public void click() {
    	new EventHandler().beforeClick(getFrameLocator().first());
        getFrameLocator().first().click();
    }

    public void fill(String text) {
        getFrameLocator().first().fill(text);
        new EventHandler().afterFill(getFrameLocator(), text);
    }
    
    public void fillAndTab(String text) {
        getFrameLocator().first().fill(text);
        new EventHandler().afterFill(getFrameLocator().first(), text);
        getFrameLocator().first().press("Tab");
    }
    
    public String textContent() {
        return getFrameLocator().first().textContent();
    }

    public boolean isVisible() {
        return getFrameLocator().first().isVisible();
    }

    public void hover() {
        getFrameLocator().first().hover();
    }

    public void selectOption(String value) {
        getFrameLocator().first().selectOption(value);
    }

    public void check() {
        getFrameLocator().first().check();
    }

    public void uncheck() {
        getFrameLocator().first().uncheck();
    }

    public String getAttribute(String name) {
        return getFrameLocator().first().getAttribute(name);
    }
    
    public String getText() {
        return getFrameLocator().first().textContent();
    }
    
    public String getValue() {
    	getFrameLocator().first().waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.ATTACHED));
        return getFrameLocator().first().inputValue();
    }
	
    public boolean isElementPresent() {
    	try {
			getFrameLocator().waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(1000));
            // If no exception, element became visible
            return true;
        } catch (PlaywrightException e) {
            // Timeout or other exception means element not visible in given time
            return false;
        }
    }
	
	public boolean isElementPresent(int waitTime) {
		try {
			getFrameLocator().waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(waitTime * 1000));
            // If no exception, element became visible
            return true;
        } catch (PlaywrightException e) {
            // Timeout or other exception means element not visible in given time
            return false;
        }
	}

    public Locator getLocator() {
        return getFrameLocator();
    }
    
}