package SunaGPT20b.ws07.seq03;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.Assertions;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WindowType;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class JSFiddle{

    private static final String BASE_URL = "https://jsfiddle.net/";
    private static WebDriver driver;
    private static WebDriverWait wait;

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

    /** Helper to collect all distinct hrefs from the current page */
    private Set<String> collectLinks() {
        List<WebElement> anchorElements = driver.findElements(By.tagName("a"));
        Set<String> hrefs = new HashSet<>();
        for (WebElement el : anchorElements) {
            String href = el.getAttribute("href");
            if (href != null && !href.trim().isEmpty()
                    && !href.startsWith("javascript:")
                    && !href.startsWith("mailto:")) {
                hrefs.add(href.trim());
            }
        }
        return hrefs;
    }

    /** Helper to determine if a URL is internal (same domain) */
    private boolean isInternalLink(String url) {
        return url.startsWith(BASE_URL) || url.startsWith("/") || url.startsWith("./") || url.startsWith("../");
    }

    /** Helper to extract domain from a URL */
    private String extractDomain(String url) {
        try {
            return new java.net.URL(url).getHost();
        } catch (Exception e) {
            return "";
        }
    }

    @Test
    @Order(1)
    public void testBasePageLoads() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.titleIs(driver.getTitle()));
        String title = driver.getTitle();
        Assertions.assertTrue(title.toLowerCase().contains("jsfiddle"),
                "Base page title should contain 'jsfiddle', but was: " + title);
    }

    @Test
    @Order(2)
    public void testInternalLinksOneLevelDeep() {
        driver.get(BASE_URL);
        Set<String> allLinks = collectLinks();

        Set<String> internalLinks = allLinks.stream()
                .filter(this::isInternalLink)
                .map(link -> {
                    if (link.startsWith("/")) {
                        return BASE_URL.replaceAll("/+$", "") + link;
                    } else if (link.startsWith("./") || link.startsWith("../")) {
                        return BASE_URL + link;
                    } else {
                        return link;
                    }
                })
                .collect(Collectors.toSet());

        Assertions.assertFalse(internalLinks.isEmpty(),
                "No internal links found on the base page to test.");

        for (String link : internalLinks) {
            driver.navigate().to(link);
            // Wait for the document to be ready and title to be non‑empty
            wait.until(driver -> !driver.getTitle().isEmpty());
            String title = driver.getTitle();
            Assertions.assertFalse(title.isEmpty(),
                    "Page at " + link + " should have a non‑empty title.");
        }
    }

    @Test
    @Order(3)
    public void testExternalLinksOnBasePage() {
        driver.get(BASE_URL);
        Set<String> allLinks = collectLinks();

        Set<String> externalLinks = allLinks.stream()
                .filter(link -> !isInternalLink(link))
                .collect(Collectors.toSet());

        Assertions.assertFalse(externalLinks.isEmpty(),
                "No external links found on the base page to test.");

        String originalWindow = driver.getWindowHandle();

        for (String extLink : externalLinks) {
            // Open link in a new tab
            driver.switchTo().newWindow(WindowType.TAB);
            driver.get(extLink);

            // Wait for URL to be loaded
            wait.until(ExpectedConditions.urlContains(extLink));

            String currentUrl = driver.getCurrentUrl();
            String expectedDomain = extractDomain(extLink);
            String actualDomain = extractDomain(currentUrl);

            Assertions.assertTrue(actualDomain.contains(expectedDomain),
                    "External link should navigate to domain containing '" + expectedDomain +
                            "', but landed on '" + actualDomain + "' for URL: " + currentUrl);

            // Close the tab and switch back
            driver.close();
            driver.switchTo().window(originalWindow);
        }
    }
}