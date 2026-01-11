package SunaGPT20b.ws07.seq09;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.*;
import org.openqa.selenium.support.ui.*;

import java.net.*;
import java.time.Duration;
import java.util.*;

@TestMethodOrder(OrderAnnotation.class)
public class JSFiddle {

    private static final String BASE_URL = "https://jsfiddle.net/";
    private static WebDriver driver;
    private static WebDriverWait wait;

    @BeforeAll
    public static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless"); // REQUIRED
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(30));
    }

    @AfterAll
    public static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @Order(1)
    public void testHomePageTitle() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
        String title = driver.getTitle();
        Assertions.assertTrue(title.toLowerCase().contains("jsfiddle"),
                "Home page title should contain 'jsfiddle'");
    }

    @Test
    @Order(2)
    public void testFirstLevelInternalPagesAndExternalLinks() throws MalformedURLException {
        driver.get(BASE_URL);
        // Ensure the page is loaded
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));

        String baseHost = new URL(BASE_URL).getHost();

        // Collect all unique hrefs
        Set<String> processed = new HashSet<>();
        List<String> hrefs = new ArrayList<>();

        List<WebElement> anchors = driver.findElements(By.tagName("a"));
        for (WebElement a : anchors) {
            String href = a.getAttribute("href");
            if (href == null || href.trim().isEmpty()) {
                continue;
            }
            // Normalize relative URLs
            URL url = new URL(new URL(BASE_URL), href);
            String urlString = url.toString();
            if (!processed.contains(urlString)) {
                processed.add(urlString);
                hrefs.add(urlString);
            }
        }

        // Iterate over collected URLs
        for (String link : hrefs) {
            URL url = new URL(link);
            String host = url.getHost();
            String path = url.getPath(); // includes leading '/'
            int depth = 0;
            if (!path.isEmpty()) {
                String[] parts = path.split("/");
                for (String part : parts) {
                    if (!part.isEmpty()) depth++;
                }
            }

            // INTERNAL LINK (same host) and ONE LEVEL DEEP (depth <= 1)
            if (host.equalsIgnoreCase(baseHost) && depth <= 1) {
                driver.navigate().to(url.toString());
                // Wait for body to be present
                wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
                Assertions.assertTrue(driver.getCurrentUrl().startsWith(BASE_URL),
                        "Internal page URL should start with base URL: " + url);
                // Return to base for next link
                driver.navigate().back();
                wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
            } else if (!host.equalsIgnoreCase(baseHost) && (url.getProtocol().startsWith("http"))) {
                // EXTERNAL LINK: open in new tab via JS
                ((JavascriptExecutor) driver).executeScript("window.open(arguments[0]);", url.toString());
                // Switch to new window
                Set<String> handles = driver.getWindowHandles();
                String newHandle = handles.stream()
                        .filter(h -> !h.equals(driver.getWindowHandle()))
                        .reduce((first, second) -> second) // get the last added
                        .orElseThrow(() -> new RuntimeException("Failed to open new tab"));
                driver.switchTo().window(newHandle);
                // Wait for navigation
                wait.until(webDriver -> ((JavascriptExecutor) webDriver)
                        .executeScript("return document.readyState").equals("complete"));
                String current = driver.getCurrentUrl();
                Assertions.assertTrue(current.contains(host),
                        "External page should contain its domain in URL. Expected domain: " + host + ", got: " + current);
                // Close external tab and switch back
                driver.close();
                driver.switchTo().window(driver.getWindowHandles().iterator().next());
            }
            // else: ignore (mailto, javascript, etc.)
        }
    }
}