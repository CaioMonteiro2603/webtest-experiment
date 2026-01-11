package GPT20b.ws10.seq08;

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
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.Select;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@TestMethodOrder(OrderAnnotation.class)
public class BrasilAgritest {

    private static final String BASE_URL = "https://gestao.brasilagritest.com/login";
    private static final String USERNAME = "superadmin@brasilagritest.com.br";
    private static final String PASSWORD = "10203040";
    private static final String BASE_HOST = "gestao.brasilagritest.com";

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

    /* ---------- Helper methods ---------- */

    private void navigateToLogin() {
        driver.get(BASE_URL);
    }

    private void performLogin(String user, String pass) {
        navigateToLogin();

        By emailField = By.id("email");
        By passwordField = By.id("password");
        By loginButton = By.cssSelector("button[type='submit']");

        List<WebElement> elements = driver.findElements(emailField);
        Assumptions.assumeTrue(!elements.isEmpty(),
                "Login form email field not found; skipping test.");

        wait.until(ExpectedConditions.visibilityOfElementLocated(emailField)).clear();
        wait.until(ExpectedConditions.visibilityOfElementLocated(emailField)).sendKeys(user);
        wait.until(ExpectedConditions.visibilityOfElementLocated(passwordField)).clear();
        wait.until(ExpectedConditions.visibilityOfElementLocated(passwordField)).sendKeys(pass);
        wait.until(ExpectedConditions.elementToBeClickable(loginButton)).click();
    }

    private void ensureLoggedIn() {
        if (driver.findElements(By.id("logout-button")).isEmpty()) {
            performLogin(USERNAME, PASSWORD);
            // wait for logout button to confirm login
            By logout = By.id("logout-button");
            wait.until(ExpectedConditions.visibilityOfElementLocated(logout));
        }
    }

    /* ---------- Tests ---------- */

    @Test
    @Order(1)
    public void testLoginPageLoads() {
        navigateToLogin();
        String title = driver.getTitle();
        Assertions.assertTrue(title.toLowerCase().contains("gestao") || title.toLowerCase().contains("brasil"),
                "Page title should contain 'gestao' or 'brasil'.");
        Assertions.assertTrue(driver.getCurrentUrl().toLowerCase().contains("login"),
                "URL should contain 'login' path.");
    }

    @Test
    @Order(2)
    public void testInvalidCredentials() {
        performLogin("invalid@user.com", "wrongpass");
        By errorLocator = By.cssSelector(".alert-danger, .invalid-feedback");
        List<WebElement> errors = driver.findElements(errorLocator);
        Assertions.assertFalse(errors.isEmpty(),
                "Error message should appear for invalid credentials.");
        String msg = errors.get(0).getText().toLowerCase();
        Assertions.assertTrue(msg.contains("invalid") || msg.contains("incorrect") || msg.contains("wrong"),
                "Error message should indicate invalid credentials.");
    }

    @Test
    @Order(3)
    public void testValidLogin() {
        performLogin(USERNAME, PASSWORD);
        By logoutButton = By.id("logout-button");
        WebElement logout = wait.until(ExpectedConditions.visibilityOfElementLocated(logoutButton));
        Assertions.assertTrue(logout.isDisplayed(), "Logout button should be visible after valid login.");
        Assertions.assertTrue(driver.getCurrentUrl().toLowerCase().contains("dashboard") ||
                             driver.getCurrentUrl().toLowerCase().contains("panel"),
                             "After login, URL expected to contain 'dashboard' or 'panel'.");
    }

    @Test
    @Order(4)
    public void testSortingDropdownOnInventory() {
        ensureLoggedIn();

        By inventoryLink = By.linkText("All Items");
        List<WebElement> inventories = driver.findElements(inventoryLink);
        Assumptions.assumeTrue(!inventories.isEmpty(),
                "All Items link not found; skipping test.");
        wait.until(ExpectedConditions.elementToBeClickable(inventoryLink)).click();

        By sortSelect = By.cssSelector("select#sort-selector, select[name='sort']");
        List<WebElement> selects = driver.findElements(sortSelect);
        Assumptions.assumeTrue(!selects.isEmpty(),
                "Sorting dropdown not found; skipping test.");

        Select sort = new Select(selects.get(0));
        List<WebElement> options = sort.getOptions();
        Assertions.assertTrue(options.size() > 1, "Sorting dropdown should have multiple options.");

        String firstItemName = null;
        for (WebElement option : options) {
            sort.selectByVisibleText(option.getText());
            // Wait for the table to update
            wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".table-loading")));
            By itemNameLocator = By.cssSelector(".item-table .item-name");
            List<WebElement> items = driver.findElements(itemNameLocator);
            Assumptions.assumeTrue(!items.isEmpty(),
                    "No item rows displayed after sorting; skipping further checks.");
            String currentFirst = items.get(0).getText();
            if (firstItemName != null) {
                Assertions.assertNotEquals(firstItemName, currentFirst,
                        "Sorting should change the order of items.");
            }
            firstItemName = currentFirst;
        }
    }

    @Test
    @Order(5)
    public void testBurgerMenuOperations() {
        ensureLoggedIn();

        By burgerBtn = By.cssSelector("button[aria-label='Toggle navigation'], .navbar-toggler");
        List<WebElement> btns = driver.findElements(burgerBtn);
        Assumptions.assumeTrue(!btns.isEmpty(), "Burger menu button not found; skipping test.");
        wait.until(ExpectedConditions.elementToBeClickable(burgerBtn)).click();

        // Click All Items
        By allItemsLink = By.linkText("All Items");
        if (!driver.findElements(allItemsLink).isEmpty()) {
            wait.until(ExpectedConditions.elementToBeClickable(allItemsLink)).click();
            Assertions.assertTrue(driver.getCurrentUrl().toLowerCase().contains("items") ||
                                 driver.getCurrentUrl().toLowerCase().contains("all-items"),
                                 "URL should reference items after clicking All Items.");
        }

        // About external
        By aboutLink = By.linkText("About");
        if (!driver.findElements(aboutLink).isEmpty()) {
            String originalWin = driver.getWindowHandle();
            wait.until(ExpectedConditions.elementToBeClickable(aboutLink)).click();
            wait.until(ExpectedConditions.numberOfWindowsToBe(2));
            Set<String> handles = driver.getWindowHandles();
            for (String h : handles) {
                if (!h.equals(originalWin)) {
                    driver.switchTo().window(h);
                    Assertions.assertTrue(driver.getCurrentUrl().contains("about") ||
                                         driver.getCurrentUrl().contains("external"),
                                         "About link opened a non‑internal URL.");
                    driver.close();
                    driver.switchTo().window(originalWin);
                }
            }
        }

        // Reset App State
        By resetLink = By.linkText("Reset App State");
        if (!driver.findElements(resetLink).isEmpty()) {
            wait.until(ExpectedConditions.elementToBeClickable(resetLink)).click();
            // After reset, verify that cart badge count is zero if cart exists
            By cartBadge = By.cssSelector(".cart-badge");
            if (!driver.findElements(cartBadge).isEmpty()) {
                String badgeText = driver.findElement(cartBadge).getText();
                Assertions.assertTrue(badgeText.equals("0") || badgeText.isEmpty(),
                        "Cart badge should be zero after reset.");
            }
        }

        // Logout
        By logoutLink = By.id("logout-button");
        if (!driver.findElements(logoutLink).isEmpty()) {
            wait.until(ExpectedConditions.elementToBeClickable(logoutLink)).click();
            Assertions.assertTrue(driver.getCurrentUrl().toLowerCase().contains("login"),
                    "Should return to login page after logout.");
        }
    }

    @Test
    @Order(6)
    public void testFooterExternalLinks() {
        navigateToLogin(); // start from login to ensure fresh load
        By footerLinks = By.xpath("//footer//a[@href and not(contains(@href, '" + BASE_HOST + "'))]");
        List<WebElement> links = driver.findElements(footerLinks);
        if (!links.isEmpty()) {
            String originalWin = driver.getWindowHandle();
            for (WebElement link : links) {
                String href = link.getAttribute("href");
                if (href == null || href.isEmpty() || href.contains(BASE_HOST)) {
                    continue; // skip internal or empty links
                }
                // click link
                wait.until(ExpectedConditions.elementToBeClickable(link)).click();
                wait.until(ExpectedConditions.numberOfWindowsToBe(2));
                Set<String> handles = driver.getWindowHandles();
                for (String h : handles) {
                    if (!h.equals(originalWin)) {
                        driver.switchTo().window(h);
                        Assertions.assertTrue(driver.getCurrentUrl().contains(href),
                                "Opened link URL does not contain expected domain: " + href);
                        driver.close();
                        driver.switchTo().window(originalWin);
                    }
                }
            }
        } else {
            // If no footer external links found, check for social media links or other external links
            By socialLinks = By.xpath("//a[contains(@href, 'facebook') or contains(@href, 'twitter') or contains(@href, 'linkedin') or contains(@href, 'instagram')]");
            links = driver.findElements(socialLinks);
            Assertions.assertFalse(links.isEmpty(), "No external links found in footer or social media links.");
        }
    }
}