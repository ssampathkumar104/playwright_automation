package testBase;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

/**
 * Wrapper class for Playwright browser components.
 */
public class PlaywrightDriver {
    private Playwright playwright;
    private Browser browser;
    private BrowserContext context;
    private Page page;

    public PlaywrightDriver(Playwright playwright, Browser browser, BrowserContext context, Page page) {
        this.playwright = playwright;
        this.browser = browser;
        this.context = context;
        this.page = page;
    }

    public Playwright getPlaywright() {
        return playwright;
    }

    public Browser getBrowser() {
        return browser;
    }

    public BrowserContext getContext() {
        return context;
    }

    public Page getPage() {
        return page;
    }
}