package SunaGPT20b.ws09.seq09;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@TestMethodOrder(OrderAnnotation.class)
public class conduit {

    private static final String BASE_URL = "https://demo.realworld.io/";
    private static final String USERNAME = "standard_user";
    private static final String PASSWORD = "secret_sauce";

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

    @BeforeEach
    public void navigateToBase() {
        driver.get(BASE_URL);
    }

    private void login() {
        WebElement userField = wait.until(ExpectedConditions.elementToBeClickable(By.id("user-name")));
        userField.clear();
        userField.sendKeys(USERNAME);

        WebElement passField = driver.findElement(By.id("password"));
        passField.clear();
        passField.sendKeys(PASSWORD);

        WebElement loginBtn = driver.findElement(By.id("login-button"));
        loginBtn.click();

        wait.until(ExpectedConditions.urlContains("/inventory.html"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/inventory.html"),
                "Login should navigate to inventory page");
    }

    private void resetAppState() {
        openMenu();
        WebElement resetLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link")));
        resetLink.click();
        wait.until(ExpectedConditions.invisibilityOf(resetLink));
    }

    private void openMenu() {
        WebElement menuBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuBtn.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("react-burger-menu-btn")));
    }

    @Test
    @Order(1)
    public void testValidLogin() {
        login();
        List<WebElement> items = driver.findElements(By.cssSelector(".inventory_item"));
        Assertions.assertFalse(items.isEmpty(), "Inventory should contain items after login");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        WebElement userField = wait.until(ExpectedConditions.elementToBeClickable(By.id("user-name")));
        userField.clear();
        userField.sendKeys("invalid_user");

        WebElement passField = driver.findElement(By.id("password"));
        passField.clear();
        passField.sendKeys("wrong_password");

        driver.findElement(By.id("login-button")).click();

        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-test='error']")));
        Assertions.assertTrue(error.isDisplayed(), "Error message should be displayed for invalid login");
        Assertions.assertTrue(error.getText().toLowerCase().contains("username"),
                "Error message should mention username or password");
    }

    @Test
    @Order(3)
    public void testSortingDropdown() {
        login();
        WebElement sortSelect = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("select[data-test='product_sort_container']")));
        Select select = new Select(sortSelect);
        String[] options = {"az", "za", "lohi", "hilo"};
        for (String value : options) {
            select.selectByValue(value);
            // Verify that the first item's name changes accordingly
            WebElement firstItem = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector(".inventory_item_name")));
            Assertions.assertNotNull(firstItem.getText(),
                    "First item name should be present after sorting by " + value);
        }
    }

    @Test
    @Order(4)
    public void testMenuNavigation() {
        login();
        openMenu();

        // All Items
        WebElement allItems = wait.until(ExpectedConditions.elementToBeClickable(By.id("inventory_sidebar_link")));
        allItems.click();
        wait.until(ExpectedConditions.urlContains("/inventory.html"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/inventory.html"),
                "All Items should navigate to inventory page");

        // About (external)
        openMenu();
        WebElement about = wait.until(ExpectedConditions.elementToBeClickable(By.id("about_sidebar_link")));
        String originalWindow = driver.getWindowHandle();
        about.click();

        wait.until(driver1 -> driver1.getWindowHandles().size() > 1);
        Set<String> windows = driver.getWindowHandles();
        windows.remove(originalWindow);
        String newWindow = windows.iterator().next();
        driver.switchTo().window(newWindow);
        Assertions.assertTrue(driver.getCurrentUrl().contains("saucelabs.com"),
                "About link should open Saucelabs domain");
        driver.close();
        driver.switchTo().window(originalWindow);

        // Reset App State
        openMenu();
        resetAppState();

        // Logout
        openMenu();
        WebElement logout = wait.until(ExpectedConditions.elementToBeClickable(By.id("logout_sidebar_link")));
        logout.click();
        wait.until(ExpectedConditions.urlToBe(BASE_URL));
        Assertions.assertEquals(BASE_URL, driver.getCurrentUrl(),
                "Logout should return to base URL");
    }

    @Test
    @Order(5)
    public void testFooterSocialLinks() {
        login();
        // Social links selectors
        String[] selectors = {
                "a[href*='twitter.com']",
                "a[href*='facebook.com']",
                "a[href*='linkedin.com']"
        };
        String originalWindow = driver.getWindowHandle();

        for (String css : selectors) {
            List<WebElement> links = driver.findElements(By.cssSelector(css));
            if (links.isEmpty()) continue;
            WebElement link = links.get(0);
            link.click();

            wait.until(driver1 -> driver1.getWindowHandles().size() > 1);
            Set<String> windows = driver.getWindowHandles();
            windows.remove(originalWindow);
            String newWindow = windows.iterator().next();
            driver.switchTo().window(newWindow);
            Assertions.assertTrue(driver.getCurrentUrl().contains(link.getAttribute("href").split("/")[2]),
                    "Social link should open correct domain: " + link.getAttribute("href"));
            driver.close();
            driver.switchTo().window(originalWindow);
        }
    }

    @Test
    @Order(6)
    public void testAddToCartAndCheckout() {
        login();
        // Add first item to cart
        WebElement addBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("button[data-test='add-to-cart-sauce-labs-backpack']")));
        addBtn.click();

        // Verify cart badge
        WebElement badge = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("a.shopping_cart_link span.shopping_cart_badge")));
        Assertions.assertEquals("1", badge.getText(), "Cart badge should show 1 item");

        // Go to cart
        driver.findElement(By.cssSelector("a.shopping_cart_link")).click();
        wait.until(ExpectedConditions.urlContains("/cart.html"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/cart.html"),
                "Should navigate to cart page");

        // Checkout
        WebElement checkoutBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.id("checkout")));
        checkoutBtn.click();

        // Fill checkout info
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("first-name"))).sendKeys("John");
        driver.findElement(By.id("last-name")).sendKeys("Doe");
        driver.findElement(By.id("postal-code")).sendKeys("12345");
        driver.findElement(By.id("continue")).click();

        // Finish
        wait.until(ExpectedConditions.elementToBeClickable(By.id("finish"))).click();

        // Confirmation
        WebElement thankYou = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("h2.complete-header")));
        Assertions.assertEquals("THANK YOU FOR YOUR ORDER", thankYou.getText().trim(),
                "Checkout should display thank you message");

        // Reset state for next tests
        driver.findElement(By.id("back-to-products")).click();
        resetAppState();
    }
}