package testBase;

import java.io.File;
import java.net.MalformedURLException;
import java.nio.file.Paths;
import java.util.Objects;

import com.microsoft.playwright.Video;

/**
 * The Driver class is a utility class for initializing and quitting the
 * Playwright browser instance for web automation.
 * It provides methods to initialize the browser based on the specified browser
 * type and to quit the browser.
 * The class is designed as final and cannot be instantiated.
 */
public final class Driver {

    /**
     * Private constructor to prevent instantiation of the Driver class.
     */
    private Driver() {
    }

    /**
     * Initializes the Playwright browser instance for web automation based on the
     * specified browser type.
     *
     * @param browserType the type of the browser to initialize
     * @throws MalformedURLException if the browser URL is malformed
     */
    public static void initDriverForWeb(String browserType) throws MalformedURLException {
        if (Objects.isNull(ThreadUtils.getDriverRef())) {
            PlaywrightDriver driver = BrowserFactory.initBrowser(browserType);
            ThreadUtils.setDriverRef(driver);
        }
    }

    /**
     * Quits the Playwright browser instance if it is not null and removes the
     * driver reference from the thread.
     */
    public static void quitDriver(String testClassName) {
        if (Objects.nonNull(ThreadUtils.getDriverRef())) {
            PlaywrightDriver driver = ThreadUtils.getDriverRef();

            driver.getContext().close();

            Video recording = driver.getPage().video();
            if (recording != null) {
                recording.saveAs(Paths.get(System.getProperty("user.dir") + File.separator + "videos" + File.separator
                        + testClassName + ".webm"));
            }
            driver.getPage().close();

            driver.getBrowser().close();
            driver.getPlaywright().close();
            ThreadUtils.removeDriverRef();
        }
    }
}
