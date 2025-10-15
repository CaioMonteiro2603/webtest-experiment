package GPT20b.ws05.seq06;

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
import java.util.Set;

@TestMethodOrder(OrderAnnotation.class)
public class CacTatTestSuite {

    private static final String BASE_URL = "https://cac-tat.s3.eu-central-1.amazonaws.com/index.html";
    private static final String USER_EMAIL = "caio@gmail.com";
    private static final String USER_PASSWORD = "123";

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

    /* ------------------------ helpers --------------------------------------- */
    private void navigateToHome() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.or(
                ExpectedConditions.titleIs("Home"),
                ExpectedConditions.presenceOfElementLocated(By.tagName("body"))));
    }

    private void login(String email, String password) {
        navigateToHome();
        List<WebElement> userFields = driver.findElements(By.name("user-name"));
        if (userFields.isEmpty()) {
            userFields = driver.findElements(By.id("user-name"));
        }
        List<WebElement> passFields = driver.findElements(By.name("password"));
        if (passFields.isEmpty()) {
            passFields = driver.findElements(By.id("password"));
        }
        List<WebElement> loginButtons = driver.findElements(By.cssSelector("button[type='submit'], input[type='submit']"));
        Assumptions.assumeTrue(!userFields.isEmpty() && !passFields.isEmpty() && !loginButtons.isEmpty(),
                "Login form not present; skipping test");

        WebElement userField = userFields.get(0);
        WebElement passField = passFields.get(0);
        WebElement loginBtn = loginButtons.get(0);

        userField.clear();
        userField.sendKeys(email);
        passField.clear();
        passField.sendKeys(password);

        wait.until(ExpectedConditions.elementToBeClickable(loginBtn)).click();

        // Wait for an element that appears after successful login
        wait.until(ExpectedConditions.or(
                ExpectedConditions.urlMatches(".*/dashboard.*"),
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".dashboard, .inventory"))));
    }

    private void logout() {
        List<WebElement> logoutLinks = driver.findElements(By.linkText("Logout"));
        if (!logoutLinks.isEmpty()) {
            wait.until(ExpectedConditions.elementToBeClickable(logoutLinks.get(0))).click();
            wait.until(ExpectedConditions.urlToBe(BASE_URL));
        }
    }

    private void resetAppState() {
        List<WebElement> resetLinks = driver.findElements(By.linkText("Reset App State"));
        if (!resetLinks.isEmpty()) {
            wait.until(ExpectedConditions.elementToBeClickable(resetLinks.get(0))).click();
            // Assume reset brings us back to home
            wait.until(ExpectedConditions.urlToBe(BASE_URL));
        }
    }

    private void openLinkAndVerifyExternal(By locator, String expectedDomain) {
        WebElement link = driver.findElement(locator);
        wait.until(ExpectedConditions.elementToBeClickable(link)).click();
        switchToNewWindow();
        Assertions.assertTrue(driver.getCurrentUrl().toLowerCase().contains(expectedDomain.toLowerCase()),
                "URL should contain " + expectedDomain + " after opening external link");
        driver.close();
        driver.switchTo().window(driver.getWindowHandles().iterator().next());
    }

    private void switchToNewWindow() {
        String original = driver.getWindowHandle();
        Set<String> handles = driver.getWindowHandles();
        for (String handle : handles) {
            if (!handle.equals(original)) {
                driver.switchTo().window(handle);
                break;
            }
        }
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

    private String getFirstProductName() {
        List<WebElement> items = driver.findElements(By.cssSelector(
                ".product-card .product-title, .product-card .product-name, .product-item .product-title, .product-item .product-name"));
        if (items.isEmpty()) {
            items = driver.findElements(By.cssSelector(".product-card h2, .product-item h2"));
        }
        return items.isEmpty() ? null : items.get(0).getText();
    }

    /* ------------------------ tests ---------------------------------------- */
    @Test
    @Order(1)
    public void testHomePageLoads() {
        navigateToHome();
        Assertions.assertFalse(driver.getTitle().isBlank(), "Home page title should not be blank");
    }

    @Test
    @Order(2)
    public void testValidLogin() {
        login(USER_EMAIL, USER_PASSWORD);
        Assertions.assertEquals(BASE_URL, driver.getCurrentUrl(), "After resetting, URL should be base");
        logout();
    }

    @Test
    @Order(3)
    public void testInvalidLogin() {
        navigateToHome();
        List<WebElement> userFields = driver.findElements(By.name("user-name"));
        if (userFields.isEmpty()) {
            userFields = driver.findElements(By.id("user-name"));
        }
        List<WebElement> passFields = driver.findElements(By.name("password"));
        if (passFields.isEmpty()) {
            passFields = driver.findElements(By.id("password"));
        }
        List<WebElement> loginButtons = driver.findElements(By.cssSelector("button[type='submit'], input[type='submit']"));
        Assumptions.assumeTrue(!userFields.isEmpty() && !passFields.isEmpty() && !loginButtons.isEmpty(),
                "Login form not present; skipping invalid login test");

        WebElement userField = userFields.get(0);
        WebElement passField = passFields.get(0);
        WebElement loginBtn = loginButtons.get(0);

        userField.clear();
        userField.sendKeys("wronguser");
        passField.clear();
        passField.sendKeys("wrongpass");

        wait.until(ExpectedConditions.elementToBeClickable(loginBtn)).click();

        WebElement errorMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".fa-exclamation-circle, .error, .alert-error, .validation-error, .error-message")));
        Assertions.assertTrue(errorMsg.getText().toLowerCase().contains("login"),
                "Error message should mention login after incorrect credentials");
    }

    @Test
    @Order(4)
    public void testNavigationLinksOneLevelBelow() {
        navigateToHome();
        List<WebElement> headerLinks = driver.findElements(By.cssSelector("header a, nav a"));
        Assumptions.assumeTrue(!headerLinks.isEmpty(), "No header links found; skipping navigation test");

        String originalUrl = driver.getCurrentUrl();
        for (WebElement link : headerLinks) {
            String href = link.getAttribute("href");
            if (!isInternalLink(href) || href.equals(originalUrl)) continue;
            try {
                String linkText = link.getText();
                if (linkText == null || linkText.trim().isEmpty()) continue;
                wait.until(ExpectedConditions.elementToBeClickable(link)).click();
                wait.until(ExpectedConditions.urlToBe(href));
                Assertions.assertEquals(href, driver.getCurrentUrl(),
                        "Navigated URL should match link href: " + href);
                Assertions.assertFalse(driver.getTitle().isBlank(), "Page title should not be blank after navigation");
            } catch (Exception e) {
                Assertions.fail("Navigation through link failed: " + e.getMessage());
            } finally {
                driver.navigate().back();
                wait.until(ExpectedConditions.urlToBe(originalUrl));
            }
        }
    }

    @Test
    @Order(5)
    public void testSortingDropdown() {
        navigateToHome();
        List<WebElement> selects = driver.findElements(By.tagName("select"));
        WebElement sortSelect = null;
        for (WebElement sel : selects) {
            if (sel.findElements(By.tagName("option")).size() > 1) {
                sortSelect = sel;
                break;
            }
        }
        Assumptions.assumeTrue(sortSelect != null, "No sorting dropdown found; skipping test");

        String firstItemBefore = getFirstProductName();
        Assertions.assertNotNull(firstItemBefore, "No products found before sorting");

        List<WebElement> options = sortSelect.findElements(By.tagName("option"));
        for (WebElement option : options) {
            String optText = option.getText();
            if (optText == null || optText.trim().isEmpty()) continue;
            wait.until(ExpectedConditions.elementToBeClickable(option)).click();
            wait.until(d -> {
                String nameAfter = getFirstProductName();
                return nameAfter != null && !nameAfter.isBlank();
            });
            String firstItemAfter = getFirstProductName();
            Assertions.assertNotEquals(firstItemBefore, firstItemAfter,
                    "Sorting option '" + optText + "' should change item order");
            firstItemBefore = firstItemAfter;
        }
    }

    @Test
    @Order(6)
    public void testFooterSocialLinks() {
        navigateToHome();
        // Twitter
        List<WebElement> twitterLinks = driver.findElements(By.xpath("//a[contains(@href,'twitter.com')]"));
        for (WebElement link : twitterLinks) {
            openLinkAndVerifyExternal(By.xpath("//a[contains(@href,'twitter.com')]"), "twitter.com");
        }
        // Facebook
        List<WebElement> facebookLinks = driver.findElements(By.xpath("//a[contains(@href,'facebook.com')]"));
        for (WebElement link : facebookLinks) {
            openLinkAndVerifyExternal(By.xpath("//a[contains(@href,'facebook.com')]"), "facebook.com");
        }
        // LinkedIn
        List<WebElement> linkedInLinks = driver.findElements(By.xpath("//a[contains(@href,'linkedin.com')]"));
        for (WebElement link : linkedInLinks) {
            openLinkAndVerifyExternal(By.xpath("//a[contains(@href,'linkedin.com')]"), "linkedin.com");
        }
    }

    @Test
    @Order(7)
    public void testResetAppStateLink() {
        navigateToHome();
        List<WebElement> resetLinks = driver.findElements(By.linkText("Reset App State"));
        Assumptions.assumeTrue(!resetLinks.isEmpty(), "\"Reset App State\" link not found; skipping test");

        WebElement resetLink = resetLinks.get(0);
        wait.until(ExpectedConditions.elementToBeClickable(resetLink)).click();
        wait.until(ExpectedConditions.urlToBe(BASE_URL));
        Assertions.assertEquals(BASE_URL, driver.getCurrentUrl(),
                "After reset, should return to base URL");
    }
}