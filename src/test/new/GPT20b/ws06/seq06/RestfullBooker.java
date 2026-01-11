package GPT20b.ws06.seq06;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.URI;
import java.time.Duration;
import java.util.*;

@TestMethodOrder(OrderAnnotation.class)
public class RestfullBooker {

    private static final String BASE_URL = "https://automationintesting.online/";
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
    /* Helper methods */
    /* --------------------------------------------------------------------- */
    private void navigateToHome() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.or(
                ExpectedConditions.titleIs("Automation in Testing"),
                ExpectedConditions.presenceOfElementLocated(By.tagName("body"))));
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
            // relative link
            return true;
        } catch (Exception e) {
            return false;
        }
    }


    /* --------------------------------------------------------------------- */
    /* Tests */
    /* --------------------------------------------------------------------- */
    @Test
    @Order(1)
    public void testHomePageLoads() {
        navigateToHome();
        String title = driver.getTitle();
        Assertions.assertTrue(title != null && !title.isEmpty(), "Home page title should not be empty");
    }

    @Test
    @Order(2)
    public void testNavigationLinksOneLevelBelow() {
        navigateToHome();
        List<WebElement> headerLinks = driver.findElements(By.cssSelector("header a, nav a"));
        Assumptions.assumeTrue(!headerLinks.isEmpty(), "No header links found; skipping navigation test");

        String originalUrl = driver.getCurrentUrl();
        for (int i = 0; i < headerLinks.size(); i++) {
            // Re-find elements to avoid stale element reference
            headerLinks = driver.findElements(By.cssSelector("header a, nav a"));
            WebElement link = headerLinks.get(i);
            
            String href = link.getAttribute("href");
            if (!isInternalLink(href)) continue;
            try {
                String linkText = link.getText();
                if (linkText == null || linkText.trim().isEmpty()) continue;
                // click and wait for navigation
                wait.until(ExpectedConditions.elementToBeClickable(link)).click();
                wait.until(ExpectedConditions.urlContains(href));
                Assertions.assertTrue(driver.getCurrentUrl().equals(href),
                        "Navigated URL should match the link href: " + href);
                // verify page has a title
                Assertions.assertTrue(driver.getTitle().trim().length() > 0,
                        "Page title after navigation should not be empty");
            } catch (Exception e) {
                Assertions.fail("Navigation through link failed: " + e.getMessage());
            } finally {
                driver.navigate().back();
                wait.until(ExpectedConditions.urlToBe(originalUrl));
            }
        }
    }

    @Test
    @Order(3)
    public void testSortingDropdown() {
        navigateToHome();
        // find a select element that looks like a sort dropdown
        List<WebElement> selects = driver.findElements(By.tagName("select"));
        WebElement sortSelect = null;
        for (WebElement sel : selects) {
            if (sel.findElements(By.tagName("option")).size() > 1) {
                sortSelect = sel;
                break;
            }
        }
        Assumptions.assumeTrue(sortSelect != null, "No suitable sorting dropdown found; skipping test");

        List<WebElement> options = sortSelect.findElements(By.tagName("option"));
        // capture first product name before sorting
        String firstItemBefore = getFirstProductName();
        Assertions.assertNotNull(firstItemBefore, "No product items found before sorting");
        for (WebElement opt : options) {
            String displayText = opt.getText();
            if (displayText == null || displayText.trim().isEmpty()) continue;
            wait.until(ExpectedConditions.elementToBeClickable(opt)).click();
            // wait a bit for sort to apply (could use a predicate, but simple wait)
            wait.until(d -> {
                String nameAfter = getFirstProductName();
                return nameAfter != null && !nameAfter.isEmpty();
            });
            String firstItemAfter = getFirstProductName();
            Assertions.assertNotEquals(firstItemBefore, firstItemAfter,
                    "Sorting option '" + displayText + "' should change order of items");
            // set for next iteration
            firstItemBefore = firstItemAfter;
        }
    }

    private String getFirstProductName() {
        List<WebElement> items = driver.findElements(By.cssSelector(
                ".product-card .product-title, .product-card .product-name, "
              + ".product-item .product-title, .product-item .product-name"));
        if (items.isEmpty()) {
            // try generic h2 inside product card
            items = driver.findElements(By.cssSelector(".product-card h2, .product-item h2"));
        }
        return items.isEmpty() ? null : items.get(0).getText();
    }

    @Test
    @Order(5)
    public void testResetAppStateLink() {
        navigateToHome();
        List<WebElement> resetLinks = driver.findElements(By.linkText("Reset App State"));
        Assumptions.assumeTrue(!resetLinks.isEmpty(), "\"Reset App State\" link not found; skipping test");

        WebElement resetLink = resetLinks.get(0);
        wait.until(ExpectedConditions.elementToBeClickable(resetLink)).click();
        // Wait for page to reset; keep it simple by expecting base URL
        wait.until(ExpectedConditions.urlToBe(BASE_URL));
        Assertions.assertEquals(BASE_URL, driver.getCurrentUrl(), "After reset, should return to base URL");
    }

    @Test
    @Order(6)
    public void testInvalidLogin() {
        navigateToHome();
        // Attempt to locate login form
        List<WebElement> usernameFields = driver.findElements(By.name("username"));
        List<WebElement> passwordFields = driver.findElements(By.name("password"));
        List<WebElement> loginButtons = driver.findElements(By.cssSelector("button[type='submit'], input[type='submit']"));
        Assumptions.assumeTrue(!usernameFields.isEmpty() && !passwordFields.isEmpty() && !loginButtons.isEmpty(),
                "Login form not present; skipping invalid login test");

        WebElement user = usernameFields.get(0);
        WebElement pass = passwordFields.get(0);
        WebElement loginBtn = loginButtons.get(0);

        user.clear(); user.sendKeys("incorrect");
        pass.clear(); pass.sendKeys("wrong");
        wait.until(ExpectedConditions.elementToBeClickable(loginBtn)).click();

        WebElement errorMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".error-message, .alert, .toast")));
        Assertions.assertTrue(errorMsg.getText().toLowerCase().contains("login"),
                "Error message should contain 'login' after invalid credentials");
    }
}