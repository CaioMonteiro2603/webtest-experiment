package GPT20b.ws06.seq08;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.Select;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@TestMethodOrder(OrderAnnotation.class)
public class AutomationIntTestingTest {

    private static final String BASE_URL = "https://automationintesting.online/";
    private static final String BASE_HOST = "automationintesting.online";
    private static WebDriver driver;
    private static WebDriverWait wait;

    @BeforeAll
    public static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless"); // REQUIRED
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    /* ---------- Common helper methods ---------- */

    private void navigateToBase() {
        driver.get(BASE_URL);
    }

    private void clickLogin() {
        List<WebElement> loginLinks = driver.findElements(By.linkText("Login"));
        if (!loginLinks.isEmpty()) {
            wait.until(ExpectedConditions.elementToBeClickable(loginLinks.get(0))).click();
        } else {
            Assumptions.assumeTrue(false, "Login link not present; skipping specific test.");
        }
    }

    private void performLogin(String username, String password) {
        clickLogin();
        By emailField = By.id("email");
        By passwordField = By.id("password");
        By submitBtn = By.cssSelector("button[type='submit']");

        if (driver.findElements(emailField).isEmpty() ||
            driver.findElements(passwordField).isEmpty() ||
            driver.findElements(submitBtn).isEmpty()) {
            Assumptions.assumeTrue(false, "Login form elements not found; skipping test.");
        }

        wait.until(ExpectedConditions.visibilityOfElementLocated(emailField)).clear();
        wait.until(ExpectedConditions.visibilityOfElementLocated(emailField)).sendKeys(username);
        wait.until(ExpectedConditions.visibilityOfElementLocated(passwordField)).clear();
        wait.until(ExpectedConditions.visibilityOfElementLocated(passwordField)).sendKeys(password);
        wait.until(ExpectedConditions.elementToBeClickable(submitBtn)).click();
    }

    private void ensureLoggedIn() {
        if (driver.findElements(By.id("account-menu")).isEmpty()) {
            performLogin("caio@gmail.com", "123");
        }
    }

    /* ---------- Test 1: Home page load ---------- */

    @Test
    @Order(1)
    public void testHomePageLoads() {
        navigateToBase();
        String title = driver.getTitle();
        Assertions.assertFalse(title.isBlank(), "Page title must not be empty.");
        Assertions.assertTrue(title.length() > 0, "Page title must have content.");
    }

    /* ---------- Test 2: Valid login ---------- */

    @Test
    @Order(2)
    public void testValidLogin() {
        navigateToBase();
        performLogin("caio@gmail.com", "123");
        // After login, expect an account menu or dashboard
        By accountMenu = By.id("account-menu");
        WebElement menu = wait.until(ExpectedConditions.visibilityOfElementLocated(accountMenu));
        Assertions.assertTrue(menu.isDisplayed(), "Account menu should appear after login.");
        Assertions.assertTrue(driver.getCurrentUrl().toLowerCase().contains("dashboard") ||
                             driver.getCurrentUrl().toLowerCase().contains("account"),
                             "URL should indicate user dashboard or account after login.");
    }

    /* ---------- Test 3: Invalid login ---------- */

    @Test
    @Order(3)
    public void testInvalidLogin() {
        navigateToBase();
        performLogin("wronguser", "wrongpass");
        By errorLocator = By.cssSelector(".error, .alert, .api-error");
        List<WebElement> errors = driver.findElements(errorLocator);
        Assertions.assertFalse(errors.isEmpty(), "Error message should be displayed for invalid login.");
        String msg = errors.get(0).getText().toLowerCase();
        Assertions.assertTrue(msg.contains("invalid") || msg.contains("incorrect") || msg.contains("wrong"),
                "Error message text should indicate invalid credentials.");
    }

    /* ---------- Test 4: Sorting dropdown ---------- */

    @Test
    @Order(4)
    public void testSortingDropdown() {
        ensureLoggedIn();
        // Navigate to inventory or products page if needed
        List<WebElement> inventoryLinks = driver.findElements(By.linkText("All Items"));
        if (inventoryLinks.isEmpty()) {
            // Assuming inventory is default after login
            inventoryLinks = driver.findElements(By.linkText("Products"));
        }
        if (!inventoryLinks.isEmpty()) {
            wait.until(ExpectedConditions.elementToBeClickable(inventoryLinks.get(0))).click();
        } else {
            Assumptions.assumeTrue(false, "Inventory page link not found; skipping test.");
        }

        By sortDropdownLocator = By.cssSelector("select[id*='sort'], select[id*='order']");
        if (driver.findElements(sortDropdownLocator).isEmpty()) {
            Assumptions.assumeTrue(false, "Sorting dropdown not found; skipping test.");
        }
        WebElement sortElement = wait.until(ExpectedConditions.elementToBeClickable(sortDropdownLocator));
        Select sortSelect = new Select(sortElement);
        List<WebElement> options = sortSelect.getOptions();
        Assumptions.assumeTrue(options.size() > 1, "At least two sorting options needed.");

        String firstItemBefore = "";
        if (!driver.findElements(By.cssSelector(".product-title")).isEmpty()) {
            firstItemBefore = driver.findElements(By.cssSelector(".product-title")).get(0).getText();
        }

        for (WebElement opt : options) {
            sortSelect.selectByVisibleText(opt.getText());
            wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".loading-indicator")));
            List<WebElement> titles = driver.findElements(By.cssSelector(".product-title"));
            Assertions.assertFalse(titles.isEmpty(), "Products list should be visible after sorting.");
            String firstItemAfter = titles.get(0).getText();
            Assertions.assertNotEquals(firstItemBefore, firstItemAfter,
                    "Sorting should change the order of items.");
            firstItemBefore = firstItemAfter;
        }
    }

    /* ---------- Test 5: External links policy ---------- */

    @Test
    @Order(5)
    public void testExternalLinksPolicy() {
        navigateToBase();
        List<WebElement> links = driver.findElements(By.xpath("//a[starts-with(@href,'http')]"));
        Assertions.assertFalse(links.isEmpty(), "No external links found on the home page.");

        String originalWindow = driver.getWindowHandle();
        for (WebElement link : links) {
            String href = link.getAttribute("href");
            if (href == null || href.isEmpty() || href.contains(BASE_HOST)) {
                continue; // skip internal or empty links
            }
            // Click and switch
            link.click();
            wait.until(ExpectedConditions.numberOfWindowsToBe(2));
            Set<String> handles = driver.getWindowHandles();
            for (String handle : handles) {
                if (!handle.equals(originalWindow)) {
                    driver.switchTo().window(handle);
                    Assertions.assertTrue(driver.getCurrentUrl().contains(href),
                            "External link URL should contain expected domain: " + href);
                    driver.close();
                    driver.switchTo().window(originalWindow);
                }
            }
        }
    }

    /* ---------- Test 6: Burger menu operations ---------- */

    @Test
    @Order(6)
    public void testBurgerMenuOperations() {
        ensureLoggedIn();
        // Locate burger menu button (common class for many frameworks)
        By burgerBtn = By.cssSelector("button.navbar-toggler, .burger, .hamburger");
        if (!driver.findElements(burgerBtn).isEmpty()) {
            WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(burgerBtn));
            btn.click();
        } else {
            Assumptions.assumeTrue(false, "Burger menu button not found; skipping test.");
        }

        // All Items
        By allItemsLink = By.linkText("All Items");
        if (!driver.findElements(allItemsLink).isEmpty()) {
            WebElement link = wait.until(ExpectedConditions.elementToBeClickable(allItemsLink));
            link.click();
            Assertions.assertTrue(driver.getCurrentUrl().toLowerCase().contains("items") ||
                                 driver.getCurrentUrl().toLowerCase().contains("products"),
                                 "Inventory page should be displayed after clicking All Items.");
        } else {
            Assumptions.assumeTrue(false, "All Items link not found in burger menu.");
        }

        // About (external)
        By aboutLink = By.linkText("About");
        if (!driver.findElements(aboutLink).isEmpty()) {
            WebElement link = wait.until(ExpectedConditions.elementToBeClickable(aboutLink));
            String original = driver.getWindowHandle();
            link.click();
            wait.until(ExpectedConditions.numberOfWindowsToBe(2));
            for (String handle : driver.getWindowHandles()) {
                if (!handle.equals(original)) {
                    driver.switchTo().window(handle);
                    Assertions.assertTrue(driver.getCurrentUrl().contains("about") ||
                                         driver.getCurrentUrl().contains("external"),
                            "About link opened external domain.");
                    driver.close();
                    driver.switchTo().window(original);
                }
            }
        } else {
            Assumptions.assumeTrue(false, "About link not found in burger menu.");
        }

        // Reset App State
        By resetLink = By.linkText("Reset App State");
        if (!driver.findElements(resetLink).isEmpty()) {
            WebElement link = wait.until(ExpectedConditions.elementToBeClickable(resetLink));
            link.click();
            // Expect some indicator changes, e.g., cart count resets
            By cartBadge = By.cssSelector(".cart-badge");
            if (!driver.findElements(cartBadge).isEmpty()) {
                try {
                    wait.until(ExpectedConditions.attributeToBe(cartBadge, "data-value", "0"));
                } catch (TimeoutException e) {
                    Assertions.fail("Cart badge did not reset after Reset App State.");
                }
            }
        } else {
            Assumptions.assumeTrue(false, "Reset App State link not found in burger menu.");
        }

        // Logout
        By logoutLink = By.linkText("Logout");
        if (!driver.findElements(logoutLink).isEmpty()) {
            WebElement link = wait.until(ExpectedConditions.elementToBeClickable(logoutLink));
            link.click();
            Assertions.assertTrue(driver.getCurrentUrl().toLowerCase().contains("login") ||
                                 driver.getCurrentUrl().toLowerCase().contains("logout"),
                                 "User should be redirected to login after logout.");
        } else {
            Assumptions.assumeTrue(false, "Logout link not found in burger menu.");
        }
    }

    /* ---------- Test 7: Footer social links ---------- */

    @Test
    @Order(7)
    public void testFooterSocialLinks() {
        navigateToBase();
        List<WebElement> socialLinks = driver.findElements(By.xpath("//a[contains(@href,'twitter.com') or contains(@href,'facebook.com') or contains(@href,'linkedin.com')]"));
        Assertions.assertFalse(socialLinks.isEmpty(), "No social links found in footer.");

        String originalWindow = driver.getWindowHandle();
        for (WebElement link : socialLinks) {
            String href = link.getAttribute("href");
            if (href == null || href.isEmpty()) {
                continue;
            }
            link.click();
            wait.until(ExpectedConditions.numberOfWindowsToBe(2));
            for (String handle : driver.getWindowHandles()) {
                if (!handle.equals(originalWindow)) {
                    driver.switchTo().window(handle);
                    Assertions.assertTrue(driver.getCurrentUrl().contains(href),
                            "Social link URL should contain expected domain: " + href);
                    driver.close();
                    driver.switchTo().window(originalWindow);
                }
            }
        }
    }
}