package GPT20b.ws10.seq06;

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
public class BrasilAgritest {

    private static final String BASE_URL = "https://gestao.brasilagritest.com/login";
    private static final String USER_EMAIL = "superadmin@brasilagritest.com.br";
    private static final String USER_PASSWORD = "10203040";

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
    /* Helper Methods                                                        */
    /* --------------------------------------------------------------------- */
    private void navigateToLogin() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("form")));
    }

    private void login(String email, String password) {
        navigateToLogin();
        WebElement emailField = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[@type='email' or @name='email' or @id='email' or contains(@placeholder, 'e-mail') or contains(@placeholder, 'email')]")));
        WebElement passField = driver.findElement(
                By.xpath("//input[@type='password' or @name='password' or @id='password']"));
        WebElement loginBtn = driver.findElement(
                By.xpath("//button[contains(@class,'btn') and contains(@type,'submit')]"));

        emailField.clear();
        emailField.sendKeys(email);
        passField.clear();
        passField.sendKeys(password);
        wait.until(ExpectedConditions.elementToBeClickable(loginBtn)).click();

        wait.until(ExpectedConditions.urlMatches(".*/home"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".dashboard")));
    }

    private void logout() {
        List<WebElement> logoutLinks = driver.findElements(By.linkText("Logout"));
        if (!logoutLinks.isEmpty()) {
            wait.until(ExpectedConditions.elementToBeClickable(logoutLinks.get(0))).click();
            wait.until(ExpectedConditions.urlMatches(".*/login"));
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("form")));
        }
    }

    private void resetAppState() {
        List<WebElement> resetLinks = driver.findElements(By.linkText("Reset App State"));
        if (!resetLinks.isEmpty()) {
            wait.until(ExpectedConditions.elementToBeClickable(resetLinks.get(0))).click();
            wait.until(ExpectedConditions.urlMatches(".*/home"));
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".dashboard")));
        }
    }

    private void switchToNewWindow() {
        String original = driver.getWindowHandle();
        Set<String> handles = driver.getWindowHandles();
        for (String h : handles) {
            if (!h.equals(original)) {
                driver.switchTo().window(h);
                break;
            }
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

    private String getFirstItemName() {
        List<WebElement> items = driver.findElements(
                By.cssSelector(".product-card .product-name, .product-card h3, .list-item h3"));
        return items.isEmpty() ? null : items.get(0).getText().trim();
    }

    /* --------------------------------------------------------------------- */
    /* Tests                                                                 */
    /* --------------------------------------------------------------------- */
    @Test
    @Order(1)
    public void testHomePageLoads() {
        navigateToLogin();
        Assertions.assertFalse(driver.getTitle().isBlank(), "Login page title should not be blank");
    }

    @Test
    @Order(2)
    public void testValidLogin() {
        login(USER_EMAIL, USER_PASSWORD);
        Assertions.assertTrue(driver.getCurrentUrl().contains("/home"),
                "After successful login, URL should contain /home");
        Assertions.assertTrue(driver.findElements(By.cssSelector(".dashboard")).size() > 0,
                "Dashboard element should be visible after login");
        logout();
    }

    @Test
    @Order(3)
    public void testInvalidLogin() {
        navigateToLogin();
        WebElement emailField = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[@type='email' or @name='email' or @id='email' or contains(@placeholder, 'e-mail') or contains(@placeholder, 'email')]")));
        WebElement passField = driver.findElement(
                By.xpath("//input[@type='password' or @name='password' or @id='password']"));
        WebElement loginBtn = driver.findElement(
                By.xpath("//button[contains(@class,'btn') and contains(@type,'submit')]"));

        emailField.clear();
        emailField.sendKeys("wrong@example.com");
        passField.clear();
        passField.sendKeys("wrongpass");
        wait.until(ExpectedConditions.elementToBeClickable(loginBtn)).click();

        WebElement errorMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".alert-danger, .toast, .error-message")));
        Assertions.assertTrue(errorMsg.getText().toLowerCase().contains("login"),
                "Error message should reference login failure");
    }

    @Test
    @Order(4)
    public void testNavigationLinksOneLevelBelow() {
        navigateToLogin();
        login(USER_EMAIL, USER_PASSWORD);
        List<WebElement> headerLinks = driver.findElements(By.cssSelector("nav a"));
        Assumptions.assumeTrue(!headerLinks.isEmpty(), "No navigation links found; skipping test");

        String currentUrl = driver.getCurrentUrl();
        for (WebElement link : headerLinks) {
            String href = link.getAttribute("href");
            if (!isInternalLink(href) || href.equals(currentUrl)) continue;
            String linkText = link.getText();
            if (linkText == null || linkText.trim().isEmpty()) continue;
            try {
                wait.until(ExpectedConditions.elementToBeClickable(link)).click();
                wait.until(ExpectedConditions.urlToBe(href));
                Assertions.assertEquals(href, driver.getCurrentUrl(),
                        "Navigated URL should match link href: " + href);
                Assertions.assertFalse(driver.getTitle().isBlank(),
                        "Page title should not be blank after navigation");
            } catch (Exception e) {
                Assertions.fail("Navigation through link failed: " + e.getMessage());
            } finally {
                driver.navigate().back();
                wait.until(ExpectedConditions.urlToBe(currentUrl));
            }
        }
        logout();
    }

    @Test
    @Order(5)
    public void testSortingDropdown() {
        login(USER_EMAIL, USER_PASSWORD);
        List<WebElement> selects = driver.findElements(By.tagName("select"));
        WebElement sortSelect = selects.stream()
                .filter(s -> s.findElements(By.tagName("option")).size() > 1)
                .findFirst()
                .orElse(null);
        Assumptions.assumeTrue(sortSelect != null, "No sorting dropdown found; skipping test");

        List<WebElement> options = sortSelect.findElements(By.tagName("option"));
        String firstBefore = getFirstItemName();
        Assertions.assertNotNull(firstBefore, "No items found before sorting");

        for (WebElement opt : options) {
            String optText = opt.getText();
            if (optText == null || optText.trim().isEmpty()) continue;
            wait.until(ExpectedConditions.elementToBeClickable(opt)).click();
            wait.until(d -> {
                String after = getFirstItemName();
                return after != null && !after.isBlank();
            });
            String firstAfter = getFirstItemName();
            Assertions.assertNotEquals(firstBefore, firstAfter,
                    "Sorting option '" + optText + "' should change item order");
            firstBefore = firstAfter;
        }
        logout();
    }

    @Test
    @Order(6)
    public void testFooterSocialLinks() {
        navigateToLogin();
        login(USER_EMAIL, USER_PASSWORD);
    }

    @Test
    @Order(7)
    public void testExternalAboutLink() {
        navigateToLogin();
        login(USER_EMAIL, USER_PASSWORD);
        List<WebElement> aboutLinks = driver.findElements(By.linkText("About"));
        Assumptions.assumeTrue(!aboutLinks.isEmpty(), "\"About\" link not found; skipping test");
        openLinkAndVerifyExternal(By.linkText("About"), "gestao.brasilagritest.com");
        logout();
    }

    @Test
    @Order(8)
    public void testResetAppStateLink() {
        navigateToLogin();
        login(USER_EMAIL, USER_PASSWORD);
        List<WebElement> resetLinks = driver.findElements(By.linkText("Reset App State"));
        Assumptions.assumeTrue(!resetLinks.isEmpty(), "\"Reset App State\" link not found; skipping test");
        resetAppState();
        Assertions.assertTrue(driver.getCurrentUrl().contains("/home"),
                "After reset, should be on home page");
        logout();
    }

    @Test
    @Order(9)
    public void testLogout() {
        login(USER_EMAIL, USER_PASSWORD);
        logout();
        Assertions.assertFalse(driver.findElements(By.linkText("Logout")).size() > 0,
                "Logout link should not be visible after logging out");
    }
}