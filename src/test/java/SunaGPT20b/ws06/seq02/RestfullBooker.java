package SunaGPT20b.ws06.seq02;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.*;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@TestMethodOrder(OrderAnnotation.class)
public class AutomationTestingOnlineTest {

    private static final String BASE_URL = "https://automationintesting.online/";
    private static WebDriver driver;
    private static WebDriverWait wait;

    @BeforeAll
    public static void setUpAll() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void tearDownAll() {
        if (driver != null) {
            driver.quit();
        }
    }

    private void navigateToBase() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("login-button")));
    }

    private void login(String username, String password) {
        navigateToBase();
        WebElement userField = wait.until(ExpectedConditions.elementToBeClickable(By.id("user-name")));
        WebElement passField = driver.findElement(By.id("password"));
        WebElement loginBtn = driver.findElement(By.id("login-button"));
        userField.clear();
        userField.sendKeys(username);
        passField.clear();
        passField.sendKeys(password);
        loginBtn.click();
    }

    private void ensureLoggedIn() {
        wait.until(ExpectedConditions.urlContains("inventory"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("inventory_container")));
    }

    @Test
    @Order(1)
    public void testValidLogin() {
        login("standard_user", "secret_sauce");
        ensureLoggedIn();
        Assertions.assertTrue(driver.getCurrentUrl().contains("inventory"),
                "After valid login, URL should contain 'inventory'");
        Assertions.assertTrue(driver.findElements(By.className("inventory_item")).size() > 0,
                "Inventory items should be displayed after login");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        login("invalid_user", "wrong_password");
        WebElement errorContainer = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-test='error']")));
        Assertions.assertTrue(errorContainer.isDisplayed(),
                "Error message should be displayed for invalid credentials");
        Assertions.assertTrue(errorContainer.getText().toLowerCase().contains("username"),
                "Error message should mention username or password");
    }

    @Test
    @Order(3)
    public void testSortingDropdown() {
        login("standard_user", "secret_sauce");
        ensureLoggedIn();

        By sortLocator = By.cssSelector("select[data-test='product_sort_container']");
        WebElement sortDropdown = wait.until(ExpectedConditions.elementToBeClickable(sortLocator));
        Select select = new Select(sortDropdown);

        // Helper to get first item name
        java.util.function.Supplier<String> firstItemName = () -> {
            List<WebElement> names = driver.findElements(By.className("inventory_item_name"));
            return names.isEmpty() ? "" : names.get(0).getText();
        };

        // AZ
        select.selectByValue("az");
        wait.until(ExpectedConditions.textToBePresentInElementLocated(
                By.className("inventory_item_name"), firstItemName.get()));
        Assertions.assertEquals("Sauce Labs Backpack", firstItemName.get(),
                "First item should be alphabetical A‑Z after selecting AZ");

        // ZA
        select.selectByValue("za");
        wait.until(ExpectedConditions.textToBePresentInElementLocated(
                By.className("inventory_item_name"), firstItemName.get()));
        Assertions.assertEquals("Test.allTheThings() T-Shirt (Red)", firstItemName.get(),
                "First item should be alphabetical Z‑A after selecting ZA");

        // Price Low to High
        select.selectByValue("lohi");
        wait.until(ExpectedConditions.textToBePresentInElementLocated(
                By.className("inventory_item_name"), firstItemName.get()));
        Assertions.assertEquals("Sauce Labs Onesie", firstItemName.get(),
                "First item should be cheapest after selecting Lo‑Hi");

        // Price High to Low
        select.selectByValue("hilo");
        wait.until(ExpectedConditions.textToBePresentInElementLocated(
                By.className("inventory_item_name"), firstItemName.get()));
        Assertions.assertEquals("Sauce Labs Fleece Jacket", firstItemName.get(),
                "First item should be most expensive after selecting Hi‑Lo");
    }

    @Test
    @Order(4)
    public void testMenuActions() {
        login("standard_user", "secret_sauce");
        ensureLoggedIn();

        By menuBtn = By.id("react-burger-menu-btn");
        wait.until(ExpectedConditions.elementToBeClickable(menuBtn)).click();

        // All Items
        By allItems = By.id("inventory_sidebar_link");
        wait.until(ExpectedConditions.elementToBeClickable(allItems)).click();
        Assertions.assertTrue(driver.getCurrentUrl().contains("inventory"),
                "All Items should navigate to inventory page");

        // Open menu again for next actions
        wait.until(ExpectedConditions.elementToBeClickable(menuBtn)).click();

        // About (external)
        By aboutLink = By.id("about_sidebar_link");
        String originalWindow = driver.getWindowHandle();
        wait.until(ExpectedConditions.elementToBeClickable(aboutLink)).click();

        wait.until(driver -> driver.getWindowHandles().size() > 1);
        Set<String> windows = driver.getWindowHandles();
        windows.removeWindow);
        String newWindow = windows.iterator().next();
        driver.switchTo().window(newWindow);
        Assertions.assertTrue(driver.getCurrentUrl().contains("saucelabs.com"),
                "About link should open Saucelabs site");
        driver.close();
        driver.switchTo().window(originalWindow);

        // Open menu again
        wait.until(ExpectedConditions.elementToBeClickable(menuBtn)).click();

        // Logout
        By logoutLink = By.id("logout_sidebar_link");
        wait.until(ExpectedConditions.elementToBeClickable(logoutLink)).click();
        Assertions.assertTrue(driver.getCurrentUrl().contains("login"),
                "Logout should return to login page");

        // Login again for Reset App State test
        login("standard_user", "secret_sauce");
        ensureLoggedIn();

        // Add an item to cart
        By addToCart = By.id("add-to-cart-sauce-labs-backpack");
        wait.until(ExpectedConditions.elementToBeClickable(addToCart)).click();
        By cartBadge = By.className("shopping_cart_badge");
        wait.until(ExpectedConditions.visibilityOfElementLocated(cartBadge));
        Assertions.assertEquals("1", driver.findElement(cartBadge).getText(),
                "Cart badge should show 1 after adding an item");

        // Open menu and reset app state
        wait.until(ExpectedConditions.elementToBeClickable(menuBtn)).click();
        By resetLink = By.id("reset_sidebar_link");
        wait.until(ExpectedConditions.elementToBeClickable(resetLink)).click();

        // Verify cart badge cleared
        List<WebElement> badges = driver.findElements(cartBadge);
        Assertions.assertTrue(badges.isEmpty(),
                "Cart badge should be cleared after resetting app state");
    }

    @Test
    @Order(5)
    public void testFooterSocialLinks() {
        login("standard_user", "secret_sauce");
        ensureLoggedIn();

        // Social links selectors (adjust if IDs differ)
        By twitterLink = By.cssSelector("a[href*='twitter.com']");
        By facebookLink = By.cssSelector("a[href*='facebook.com']");
        By linkedInLink = By.cssSelector("a[href*='linkedin.com']");

        String originalWindow = driver.getWindowHandle();

        // Helper to test external link
        java.util.function.BiConsumer<By, String> testExternal = (locator, expectedDomain) -> {
            wait.until(ExpectedConditions.elementToBeClickable(locator)).click();
            wait.until(driver -> driver.getWindowHandles().size() > 1);
            Set<String> windows = driver.getWindowHandles();
            windows.remove(originalWindow);
            String newWindow = windows.iterator().next();
            driver.switchTo().window(newWindow);
            Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain),
                    "External link should navigate to domain containing '" + expectedDomain + "'");
            driver.close();
            driver.switchTo().window(originalWindow);
        };

        testExternal.accept(twitterLink, "twitter.com");
        testExternal.accept(facebookLink, "facebook.com");
        testExternal.accept(linkedInLink, "linkedin.com");
    }
}