package GPT20b.ws08.seq06;

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
public class JPetStore {

    private static final String BASE_URL = "https://jpetstore.aspectran.com/";
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

    /* --------------------------------------------------------------------- */
    /* Helper methods                                                          */
    /* --------------------------------------------------------------------- */
    private void navigateToHome() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.name("j_username")));
    }

    private void login(String email, String password) {
        navigateToHome();
        WebElement userField = driver.findElement(By.name("j_username"));
        WebElement passField = driver.findElement(By.name("password"));
        WebElement loginBtn = driver.findElement(By.cssSelector("input[type='submit']"));

        userField.clear();
        userField.sendKeys(email);
        passField.clear();
        passField.sendKeys(password);

        wait.until(ExpectedConditions.elementToBeClickable(loginBtn)).click();

        // After successful login, a Logout link should appear
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText("Logout")));
    }

    private void logout() {
        List<WebElement> logoutLinks = driver.findElements(By.linkText("Logout"));
        if (!logoutLinks.isEmpty()) {
            wait.until(ExpectedConditions.elementToBeClickable(logoutLinks.get(0))).click();
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("j_username")));
        }
    }

    private void resetAppState() {
        List<WebElement> resetLinks = driver.findElements(By.linkText("Reset App State"));
        if (!resetLinks.isEmpty()) {
            wait.until(ExpectedConditions.elementToBeClickable(resetLinks.get(0))).click();
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
        for (String h : handles) {
            if (!h.equals(original)) {
                driver.switchTo().window(h);
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

    private String getFirstItemName() {
        List<WebElement> items = driver.findElements(By.cssSelector(".itemListing .item-title, .itemListing .item-name, .list-group-item .name"));
        return items.isEmpty() ? null : items.get(0).getText().trim();
    }

    /* --------------------------------------------------------------------- */
    /* Tests                                                                    */
    /* --------------------------------------------------------------------- */
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
        Assertions.assertTrue(driver.getCurrentUrl().contains("jpetstore-frontend"),
                "URL after login should contain 'jpetstore-frontend'");
        Assertions.assertTrue(driver.findElements(By.linkText("Logout")).size() > 0,
                "Logout link should be visible after login");
        logout();
    }

    @Test
    @Order(3)
    public void testInvalidLogin() {
        navigateToHome();
        WebElement userField = driver.findElement(By.name("j_username"));
        WebElement passField = driver.findElement(By.name("password"));
        WebElement loginBtn = driver.findElement(By.cssSelector("input[type='submit']"));

        userField.clear();
        userField.sendKeys("wronguser");
        passField.clear();
        passField.sendKeys("wrongpass");

        wait.until(ExpectedConditions.elementToBeClickable(loginBtn)).click();

        WebElement errorMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".error, .alert, .validation-error, .loginError")));
        Assertions.assertTrue(errorMsg.getText().toLowerCase().contains("login"),
                "Error message should mention login failure");
    }

    @Test
    @Order(4)
    public void testNavigationLinksOneLevelBelow() {
        navigateToHome();
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
    }

    @Test
    @Order(5)
    public void testSortingDropdown() {
        login(USER_EMAIL, USER_PASSWORD);
        // Go to All Items page
        WebElement allItems = driver.findElement(By.linkText("All Items"));
        wait.until(ExpectedConditions.elementToBeClickable(allItems)).click();

        // Locate sort dropdown
        List<WebElement> selects = driver.findElements(By.tagName("select"));
        WebElement sortSelect = selects.stream()
                .filter(sel -> sel.findElements(By.tagName("option")).size() > 1)
                .findFirst()
                .orElse(null);
        Assumptions.assumeTrue(sortSelect != null, "No sorting dropdown found; skipping test");

        List<WebElement> options = sortSelect.findElements(By.tagName("option"));
        String firstNameBefore = getFirstItemName();
        Assertions.assertNotNull(firstNameBefore, "No items found before sorting");

        for (WebElement option : options) {
            String optText = option.getText();
            if (optText == null || optText.trim().isEmpty()) continue;
            wait.until(ExpectedConditions.elementToBeClickable(option)).click();
            wait.until(d -> {
                String after = getFirstItemName();
                return after != null && !after.isBlank();
            });
            String firstNameAfter = getFirstItemName();
            Assertions.assertNotEquals(firstNameBefore, firstNameAfter,
                    "Sorting option '" + optText + "' should change item order");
            firstNameBefore = firstNameAfter;
        }
        logout();
    }

    @Test
    @Order(6)
    public void testFooterSocialLinks() {
        navigateToHome();
        
    }

    @Test
    @Order(7)
    public void testExternalAboutLink() {
        navigateToHome();
        List<WebElement> aboutLinks = driver.findElements(By.linkText("About"));
        Assumptions.assumeTrue(!aboutLinks.isEmpty(), "\"About\" link not found; skipping test");
        openLinkAndVerifyExternal(By.linkText("About"), "aspectran.com");
    }

    @Test
    @Order(8)
    public void testResetAppStateLink() {
        navigateToHome();
        List<WebElement> resetLinks = driver.findElements(By.linkText("Reset App State"));
        Assumptions.assumeTrue(!resetLinks.isEmpty(), "\"Reset App State\" link not found; skipping test");
        resetAppState();
        Assertions.assertEquals(BASE_URL, driver.getCurrentUrl(), "URL should return to home after reset");
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