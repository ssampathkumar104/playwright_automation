package testBase;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Objects;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.BrowserType.ConnectOptions;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

/**
 * The BrowserFactory class is a utility class for initializing web browsers and
 * configuring browser options.
 * It provides methods to initialize the browser based on the specified browser
 * type and to configure browser options.
 * The class is designed as final and cannot be instantiated.
 */
public final class BrowserFactory extends BaseClass {

    /**
     * Private constructor to prevent instantiation of the BrowserFactory class.
     */
    private BrowserFactory() {
    }

    /**
     * Initializes the web browser based on the specified browser type.
     *
     * @param browserType
     *                        the type of the browser to initialize
     * @return the PlaywrightDriver instance
     * @throws MalformedURLException
     *                                   if the remote URL is malformed
     */
    public static PlaywrightDriver initBrowser(String browserType) throws MalformedURLException {
        Playwright playwright = Playwright.create();
        Browser browser;

        String viewportParam = getParameter("viewportSize");
        boolean recordVideo = "true".equals(getParameter("recordVideo", "false"));
        boolean isRemote = Objects.isNull(getDriver()) && "true".equals(getParameter("remote", "false"));

        if (isRemote) {
            log().info("Running on Hub");
            ConnectOptions options = new BrowserType.ConnectOptions().setSlowMo(50);
            browser = playwright.chromium().connect(getParameter("remoteURL", "ws://localhost:3000/"), options);
        } else {
            BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions()
                    .setHeadless(Objects.equals(getParameter("headless"), "true"))
                    .setSlowMo(Double.valueOf(getParameter("SlowMo", "50")));
            if (viewportParam == null)
                launchOptions.setArgs(Arrays.asList("--start-maximized", "--no-sandbox", "--disable-setuid-sandbox"));

            browser = switch (browserType.toLowerCase()) {
                case "firefox" -> playwright.firefox().launch(launchOptions);
                case "webkit", "safari" -> playwright.webkit().launch(launchOptions);
                default -> playwright.chromium().launch(launchOptions.setChannel("chrome"));
            };
        }

        // Set download directory

        Browser.NewContextOptions contextOptions = new Browser.NewContextOptions()
                .setUserAgent(
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .setLocale("en-US");

        if (recordVideo) {
            String videosDirPath = System.getProperty("user.dir") + File.separator + "videos";
            contextOptions.setRecordVideoDir(Paths.get(videosDirPath));
            System.err.println("Video recording is enabled. Videos will be saved in: " + videosDirPath);
        }

        if (viewportParam != null) {
            String[] size = viewportParam.split(",");
            contextOptions.setViewportSize(
                    size.length == 2 ? Integer.parseInt(size[0].trim()) : 1536,
                    size.length == 2 ? Integer.parseInt(size[1].trim()) : 864);
            contextOptions.setRecordVideoSize(
                    size.length == 2 ? Integer.parseInt(size[0].trim()) : 1536,
                    size.length == 2 ? Integer.parseInt(size[1].trim()) : 864);
        } else {
            contextOptions.setViewportSize(null);
            // Detect actual screen size via a temporary page so video matches the viewport
            if (recordVideo) {
                BrowserContext tempContext = browser.newContext(new Browser.NewContextOptions().setViewportSize(null));
                Page tempPage = tempContext.newPage();
                int screenWidth = (int) tempPage.evaluate("window.screen.width");
                int screenHeight = (int) tempPage.evaluate("window.screen.height");
                tempPage.close();
                tempContext.close();
                contextOptions.setRecordVideoSize(screenWidth, screenHeight);
            }
        }

        Path downloadDir = Paths.get(ThreadUtils.getDownloadDirectoryPath());
        downloadDir.toFile().mkdirs();

        BrowserContext context = browser.newContext(contextOptions);
        context.clearCookies();

        Page page = context.newPage();
        page.onDownload(download -> {
            Path savePath = downloadDir.resolve(download.suggestedFilename());
            try {
                Files.createDirectories(savePath.getParent());

                // Fail fast if the browser reported the download as failed.
                String failure = download.failure();
                if (failure != null) {
                    log().error("Download failed on browser side: " + failure);
                    return;
                }

                // The browser process runs in a separate filesystem namespace from the test
                // code (its artifacts live under /tmp/playwright-artifacts-* on the browser
                // side), so download.saveAs() fails with ENOENT because it tries to copyfile
                // into a directory that only exists on the code host. Instead, stream the bytes
                // over the Playwright connection and write them locally.
                //
                // download.createReadStream() returns the stream only once the download has
                // completed, so this does not produce the zero-byte file seen when reading too
                // early. Guard against a null stream just in case.
                try (InputStream in = download.createReadStream()) {
                    if (in == null) {
                        log().error("Download read stream was null for: " + savePath);
                        return;
                    }
                    long bytes = Files.copy(in, savePath, StandardCopyOption.REPLACE_EXISTING);
                    log().info("Downloaded to: " + savePath + " (" + bytes + " bytes)");
                }
            } catch (Exception e) {
                log().error("Failed to save download to: " + savePath + " - " + e.getMessage());
            }
        });

        return new PlaywrightDriver(playwright, browser, context, page);
    }

    /**
     * Closes the web browser if it is not null and removes the driver reference
     * from the thread.
     */
    public static void closeBrowser() {
        if (Objects.nonNull(ThreadUtils.getDriverRef())) {
            ThreadUtils.getDriverRef().getPlaywright().close();
        }
        ThreadUtils.removeDriverRef();
    }
}
