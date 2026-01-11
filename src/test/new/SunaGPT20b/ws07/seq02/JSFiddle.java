package SunaGPT20b.ws07.seq02;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WindowType;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class JSFiddle{

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://jsfiddle.net/";

    @BeforeAll
    public static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @Order(1)
    public void testHomePageLoads() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.titleContains("JSFiddle"));
        Assertions.assertTrue(driver.getCurrentUrl().startsWith(BASE_URL),
                "Home page URL does not start with expected base URL");
    }

    @Test
    @Order(2)
    public void testInternalLinks() {
        driver.get(BASE_URL);
        List<WebElement> linkElements = driver.findElements(By.cssSelector("a[href]"));
        Set<String> internalUrls = new LinkedHashSet<>();

        for (WebElement el : linkElements) {
            String href = el.getAttribute("href");
            if (href != null && href.matches("^https://jsfiddle\\.net/[^/]+/?$")) {
                internalUrls.add(href);
            }
        }

        Assertions.assertFalse(internalUrls.isEmpty(), "No internal one‑level links found on the home page");

        int visited = 0;
        for (String url : internalUrls) {
            driver.get(url);
            wait.until(ExpectedConditions.titleContains("JSFiddle"));
            Assertions.assertTrue(driver.getCurrentUrl().startsWith(url),
                    "Failed to navigate to internal URL: " + url);
            visited++;
            if (visited >= 3) { // limit to a few links for speed and stability
                break;
            }
        }
    }

    @Test
    @Order(3)
    public void testExternalLinks() throws MalformedURLException {
        driver.get(BASE_URL);
        List<WebElement> linkElements = driver.findElements(By.cssSelector("a[href]"));
        Set<String> externalUrls = new LinkedHashSet<>();

        for (WebElement el : linkElements) {
            String href = el.getAttribute("href");
            if (href != null && href.startsWith("http") && !href.contains("jsfiddle.net")) {
                externalUrls.add(href);
            }
        }

        Assertions.assertFalse(externalUrls.isEmpty(), "No external links found on the home page");

        String originalWindow = driver.getWindowHandle();
        int checked = 0;
        for (String url : externalUrls) {
            driver.switchTo().newWindow(WindowType.TAB);
            driver.get(url);
            URL parsed = new URL(url);
            String expectedHost = parsed.getHost();

            try {
                wait.until(drv -> drv.getCurrentUrl().contains(expectedHost));
                Assertions.assertTrue(driver.getCurrentUrl().contains(expectedHost),
                        "External URL did not load expected domain: " + url);
            } catch (Exception e) {
                // Some external links may redirect or block automated access
                // Log the URL and continue
                System.out.println("Could not verify external link: " + url);
            }

            driver.close();
            driver.switchTo().window(originalWindow);
            checked++;
            if (checked >= 3) { // limit to a few external links
                break;
            }
        }
    }
}