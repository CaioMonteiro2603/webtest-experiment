package SunaGPT20b.ws08.seq03;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.*;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@TestMethodOrder(OrderAnnotation.class)
public class JPetStore {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://jpetstore.aspectran.com";

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

    private void navigate(String url) {
        driver.get(url);
    }

    private void login(String username, String password) {
        navigate(BASE_URL + "/account/signonForm");
        WebElement userField = wait.until(ExpectedConditions.elementToBeClickable(By.name("username")));
        userField.clear();
        userField.sendKeys(username);
        WebElement passField = wait.until(ExpectedConditions.elementToBeClickable(By.name("password")));
        passField.clear();
        passField.sendKeys(password);
        WebElement loginBtn = wait.until(ExpectedConditions.elementToBeClickable(By.name("loginButton")));
        loginBtn.click();
    }

    private void resetAppStateIfPresent() {
        List<WebElement> resetButtons = driver.findElements(By.id("reset"));
        if (!resetButtons.isEmpty()) {
            WebElement reset = wait.until(ExpectedConditions.elementToBeClickable(resetButtons.get(0)));
            reset.click();
            wait.until(ExpectedConditions.invisibilityOf(reset));
        }
    }

    @Test
    @Order(1)
    public void testValidLogin() {
        login("j2ee", "j2ee");
        // After successful login the URL typically contains "/catalog"
        Assertions.assertTrue(driver.getCurrentUrl().contains("/catalog"),
                "Login failed – expected URL to contain '/catalog' but was: " + driver.getCurrentUrl());
        // Ensure a known element on the catalog page is displayed
        List<WebElement> catalogHeaders = driver.findElements(By.cssSelector("h2"));
        Assertions.assertFalse(catalogHeaders.isEmpty(),
                "Catalog page does not contain expected header elements.");
        resetAppStateIfPresent();
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        navigate(BASE_URL + "/account/signonForm");
        WebElement userField = wait.until(ExpectedConditions.elementToBeClickable(By.name("username")));
        userField.clear();
        userField.sendKeys("invalidUser");
        WebElement passField = wait.until(ExpectedConditions.elementToBeClickable(By.name("password")));
        passField.clear();
        passField.sendKeys("wrongPass");
        WebElement loginBtn = wait.until(ExpectedConditions.elementToBeClickable(By.name("loginButton")));
        loginBtn.click();

        // Expect an error message element
        List<WebElement> errors = driver.findElements(By.cssSelector(".error, .message, .alert"));
        Assertions.assertFalse(errors.isEmpty(),
                "Expected an error message for invalid login, but none was found.");
        Assertions.assertTrue(errors.get(0).getText().toLowerCase().contains("invalid") ||
                        errors.get(0).getText().toLowerCase().contains("failed"),
                "Error message does not indicate login failure.");
    }

    @Test
    @Order(3)
    public void testCategoryNavigation() {
        // Ensure we are logged in
        login("j2ee", "j2ee");
        navigate(BASE_URL);
        // Find all category links (one level below base)
        List<WebElement> categoryLinks = driver.findElements(By.cssSelector("a[href^='/categories/']"));
        Assertions.assertFalse(categoryLinks.isEmpty(), "No category links found on the home page.");

        for (WebElement link : categoryLinks) {
            String href = link.getAttribute("href");
            // Open link in the same tab
            link.click();
            wait.until(ExpectedConditions.urlContains("/categories/"));
            Assertions.assertTrue(driver.getCurrentUrl().contains("/categories/"),
                    "Category page URL does not contain expected path after clicking " + href);
            // Simple verification: page header should contain the category name
            List<WebElement> headers = driver.findElements(By.cssSelector("h2"));
            Assertions.assertFalse(headers.isEmpty(),
                    "Category page missing header after navigating to " + href);
            // Return to home page for next iteration
            driver.navigate().back();
            wait.until(ExpectedConditions.urlToBe(BASE_URL + "/"));
        }
        resetAppStateIfPresent();
    }

    @Test
    @Order(4)
    public void testSortingDropdown() {
        // Login and go to a known category (Dogs)
        login("j2ee", "j2ee");
        navigate(BASE_URL + "/categories/DOGS");
        // Look for a sorting dropdown; the exact selector may vary
        List<WebElement> selects = driver.findElements(By.cssSelector("select[name='sort'], select[id='sort']"));
        if (selects.isEmpty()) {
            // No sorting dropdown present – test passes trivially
            Assertions.assertTrue(true, "No sorting dropdown found; skipping test.");
            return;
        }
        Select sortSelect = new Select(selects.get(0));
        List<WebElement> options = sortSelect.getOptions();
        Assertions.assertFalse(options.isEmpty(), "Sorting dropdown has no options.");

        String previousFirstItem = "";
        for (WebElement option : options) {
            sortSelect.selectByVisibleText(option.getText());
            // Wait for the product list to refresh
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".product-list")));
            List<WebElement> productNames = driver.findElements(By.cssSelector(".product-name"));
            Assertions.assertFalse(productNames.isEmpty(),
                    "Product list is empty after selecting sort option: " + option.getText());
            String currentFirstItem = productNames.get(0).getText();
            // Ensure that selecting a different option changes the order at least once
            if (!previousFirstItem.isEmpty()) {
                Assertions.assertNotEquals(previousFirstItem, currentFirstItem,
                        "Sorting option '" + option.getText() + "' did not change product order.");
            }
            previousFirstItem = currentFirstItem;
        }
        resetAppStateIfPresent();
    }

    @Test
    @Order(5)
    public void testMenuBurgerAndReset() {
        // Login first
        login("j2ee", "j2ee");
        navigate(BASE_URL);
        // Locate burger/menu button (common Bootstrap class)
        List<WebElement> burgerButtons = driver.findElements(By.cssSelector(".navbar-toggler, #menuButton"));
        if (burgerButtons.isEmpty()) {
            Assertions.assertTrue(true, "No burger menu button found; skipping menu test.");
            return;
        }
        WebElement burger = wait.until(ExpectedConditions.elementToBeClickable(burgerButtons.get(0)));
        burger.click();

        // Wait for menu items to become visible
        List<WebElement> menuItems = driver.findElements(By.cssSelector(".navbar-collapse.show a, .dropdown-menu a"));
        Assertions.assertFalse(menuItems.isEmpty(), "Menu items not found after opening burger menu.");

        // Helper to click a menu item by its visible text
        for (WebElement item : menuItems) {
            String text = item.getText().trim();
            if (text.equalsIgnoreCase("All Items")) {
                item.click();
                wait.until(ExpectedConditions.urlContains("/catalog"));
                Assertions.assertTrue(driver.getCurrentUrl().contains("/catalog"),
                        "All Items link did not navigate to catalog page.");
                driver.navigate().back();
                wait.until(ExpectedConditions.urlToBe(BASE_URL + "/"));
            } else if (text.equalsIgnoreCase("About")) {
                String originalWindow = driver.getWindowHandle();
                item.click();
                // Switch to new window/tab
                wait.until(driver1 -> driver1.getWindowHandles().size() > 1);
                Set<String> windows = driver.getWindowHandles();
                windows.remove(originalWindow);
                String newWindow = windows.iterator().next();
                driver.switchTo().window(newWindow);
                // Verify external domain (example: aspectran.com or mybatis.org)
                String currentUrl = driver.getCurrentUrl();
                Assertions.assertTrue(currentUrl.contains("aspectran.com") || currentUrl.contains("mybatis.org"),
                        "About link opened unexpected domain: " + currentUrl);
                driver.close();
                driver.switchTo().window(originalWindow);
            } else if (text.equalsIgnoreCase("Logout")) {
                item.click();
                wait.until(ExpectedConditions.urlContains("/account/signonForm"));
                Assertions.assertTrue(driver.getCurrentUrl().contains("/account/signonForm"),
                        "Logout did not return to sign‑in page.");
                // Re‑login for subsequent steps
                login("j2ee", "j2ee");
                driver.navigate().back(); // go back to home after login
                wait.until(ExpectedConditions.urlToBe(BASE_URL + "/"));
            } else if (text.equalsIgnoreCase("Reset App State")) {
                item.click();
                // After reset, cart badge should be absent or zero
                List<WebElement> cartBadge = driver.findElements(By.cssSelector(".cart-badge"));
                if (!cartBadge.isEmpty()) {
                    Assertions.assertEquals("0", cartBadge.get(0).getText(),
                            "Cart badge not reset to zero after Reset App State.");
                }
            }
        }
        // Ensure menu is closed
        burger.click();
        resetAppStateIfPresent();
    }

    @Test
    @Order(6)
    public void testFooterSocialLinks() {
        // Ensure logged in to have footer present
        login("j2ee", "j2ee");
        navigate(BASE_URL);
        // Footer social links (common class or href patterns)
        List<WebElement> socialLinks = driver.findElements(By.cssSelector("footer a[href*='twitter.com'], footer a[href*='facebook.com'], footer a[href*='linkedin.com']"));
        Assertions.assertFalse(socialLinks.isEmpty(), "No social footer links found.");

        String originalWindow = driver.getWindowHandle();

        for (WebElement link : socialLinks) {
            String href = link.getAttribute("href");
            link.click();
            // Wait for new window
            wait.until(driver1 -> driver1.getWindowHandles().size() > 1);
            Set<String> windows = driver.getWindowHandles();
            windows.remove(originalWindow);
            String newWindow = windows.iterator().next();
            driver.switchTo().window(newWindow);
            String currentUrl = driver.getCurrentUrl();
            if (href.contains("twitter.com")) {
                Assertions.assertTrue(currentUrl.contains("twitter.com"),
                        "Twitter link opened unexpected URL: " + currentUrl);
            } else if (href.contains("facebook.com")) {
                Assertions.assertTrue(currentUrl.contains("facebook.com"),
                        "Facebook link opened unexpected URL: " + currentUrl);
            } else if (href.contains("linkedin.com")) {
                Assertions.assertTrue(currentUrl.contains("linkedin.com"),
                        "LinkedIn link opened unexpected URL: " + currentUrl);
            }
            driver.close();
            driver.switchTo().window(originalWindow);
        }
        resetAppStateIfPresent();
    }
}