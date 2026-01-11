package GPT20b.ws07.seq06;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.URI;
import java.time.Duration;
import java.util.List;

@TestMethodOrder(OrderAnnotation.class)
public class JSFiddle {

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

    /* --------------------------------------------------------------------- */
    /* Helper methods                                                           */
    /* --------------------------------------------------------------------- */
    private void navigateToHome() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.or(
                ExpectedConditions.titleContains("JSFiddle"),
                ExpectedConditions.presenceOfElementLocated(By.cssSelector("ul.fiddle-list"))));
    }



    private boolean isInternalLink(String href) {
        if (href == null) return false;
        href = href.trim();
        if (href.isEmpty() || href.startsWith("javascript:") || href.startsWith("mailto:")) return false;
        try {
            URI uri = new URI(href);
            if (uri.isAbsolute()) {
                URI baseUri = new URI(driver.getCurrentUrl());
                return baseUri.getHost().equalsIgnoreCase(uri.getHost());
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /* --------------------------------------------------------------------- */
    /* Tests                                                                     */
    /* --------------------------------------------------------------------- */
    @Test
    @Order(1)
    public void testHomePageLoads() {
        navigateToHome();
        Assertions.assertFalse(driver.getTitle().isBlank(), "Home page title should not be blank");
    }

    @Test
    @Order(2)
    public void testFooterSocialLinks() {
        navigateToHome();
        // Twitter
        WebElement twitterLink = driver.findElement(By.cssSelector("a[href*='twitter.com']"));
        Assertions.assertNotNull(twitterLink, "Twitter link should be present");
        Assertions.assertTrue(twitterLink.isDisplayed(), "Twitter link should be visible");
    }

    @Test
    @Order(3)
    public void testExternalLinksOneLevelBelow() {
        navigateToHome();
        List<WebElement> internalLinks = driver.findElements(By.cssSelector("a[href]"));
        Assumptions.assumeTrue(!internalLinks.isEmpty(), "No internal links found; skipping test");

        String originalUrl = driver.getCurrentUrl();
        for (WebElement link : internalLinks) {
            String href = link.getAttribute("href");
            if (!isInternalLink(href) || href.equals(originalUrl)) continue;
            String linkText = link.getText();
            if (linkText == null || linkText.trim().isEmpty()) continue;
            try {
                wait.until(ExpectedConditions.elementToBeClickable(link)).click();
                wait.until(ExpectedConditions.urlToBe(href));
                Assertions.assertEquals(href, driver.getCurrentUrl(),
                        "Navigated URL does not match link href: " + href);
                Assertions.assertFalse(driver.getTitle().isBlank(),
                        "Page title should not be blank after navigation");
            } catch (Exception e) {
                Assertions.fail("Navigation through link failed: " + e.getMessage());
            } finally {
                driver.navigate().back();
                wait.until(ExpectedConditions.urlToBe(originalUrl));
            }
        }
    }

    @Test
    @Order(4)
    public void testSortingDropdown() {
        navigateToHome();
        // Find a sorting dropdown
        List<WebElement> selects = driver.findElements(By.tagName("select"));
        WebElement sortSelect = selects.stream()
                .filter(sel -> sel.findElements(By.tagName("option")).size() > 1)
                .findFirst()
                .orElse(null);
        Assumptions.assumeTrue(sortSelect != null, "No sorting dropdown found; skipping test");

        List<WebElement> options = sortSelect.findElements(By.tagName("option"));
        if (options.isEmpty()) return;

        // Capture first item text before sorting
        String firstItemBefore = getFirstFiddleTitle();
        Assertions.assertNotNull(firstItemBefore, "No fiddles found before sorting");
        for (WebElement option : options) {
            String optText = option.getText();
            if (optText == null || optText.trim().isEmpty()) continue;
            wait.until(ExpectedConditions.elementToBeClickable(option)).click();
            wait.until(d -> {
                String after = getFirstFiddleTitle();
                return after != null && !after.isBlank();
            });
            String firstItemAfter = getFirstFiddleTitle();
            Assertions.assertNotEquals(firstItemBefore, firstItemAfter,
                    "Sorting option '" + optText + "' should change order of riffs");
            firstItemBefore = firstItemAfter;
        }
    }

    private String getFirstFiddleTitle() {
        List<WebElement> items = driver.findElements(By.cssSelector("ul.fiddle-list li.fiddle-item"));
        if (items.isEmpty()) {
            return null;
        }
        WebElement first = items.get(0);
        String title = first.findElement(By.cssSelector("h3.fiddle-title")).getText();
        return title != null ? title.trim() : null;
    }
}