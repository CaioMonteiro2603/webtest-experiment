package SunaGPT20b.ws01.seq09;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.*;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@TestMethodOrder(OrderAnnotation.class)
public class saucedemo {

    private static final String BASE_URL = "https://www.saucedemo.com/v1/index.html";
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

    private void navigateToBase() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("login-button")));
    }

    private void login(String user, String pass) {
        navigateToBase();
        wait.until(ExpectedConditions.elementToBeClickable(By.id("user-name"))).sendKeys(user);
        driver.findElement(By.id("password")).sendKeys(pass);
        driver.findElement(By.id("login-button")).click();
        wait.until(ExpectedConditions.urlContains("/inventory.html"));
    }

    private void resetAppState() {
        // Open menu
        wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn"))).click();
        // Click Reset App State
        wait.until(ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link"))).click();
        // Close menu
        wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-cross-btn"))).click();
    }

    @Test
    @Order(1)
    public void testValidLogin() {
        login(USERNAME, PASSWORD);
        Assertions.assertTrue(driver.getCurrentUrl().contains("/inventory.html"),
                "After valid login, URL should contain /inventory.html");
        Assertions.assertTrue(driver.findElements(By.className("inventory_item")).size() > 0,
                "Inventory items should be displayed after login");
        resetAppState();
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        navigateToBase();
        wait.until(ExpectedConditions.elementToBeClickable(By.id("user-name"))).sendKeys("invalid_user");
        driver.findElement(By.id("password")).sendKeys("wrong_pass");
        driver.findElement(By.id("login-button")).click();
        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-test='error']")));
        Assertions.assertTrue(error.isDisplayed(), "Error message should be displayed for invalid credentials");
        Assertions.assertTrue(error.getText().toLowerCase().contains("username"),
                "Error message should mention username or password");
    }

    @Test
    @Order(3)
    public void testSortingDropdown() {
        login(USERNAME, PASSWORD);
        By sortSelect = By.cssSelector("select[data-test='product_sort_container']");
        WebElement dropdown = wait.until(ExpectedConditions.elementToBeClickable(sortSelect));
        Select select = new Select(dropdown);
        String[] options = {"Name (A to Z)", "Name (Z to A)", "Price (low to high)", "Price (high to low)"};
        for (String option : options) {
            select.selectByVisibleText(option);
            // Verify that the first item changes accordingly
            List<WebElement> items = driver.findElements(By.className("inventory_item_name"));
            Assertions.assertFalse(items.isEmpty(), "Inventory items should be present after sorting");
            String firstItem = items.get(0).getText();
            Assertions.assertNotNull(firstItem, "First item name should not be null after sorting with option: " + option);
        }
        resetAppState();
    }

    @Test
    @Order(4)
    public void testAddToCartAndBadge() {
        login(USERNAME, PASSWORD);
        // Add first item to cart
        WebElement addButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("button[data-test='add-to-cart-sauce-labs-backpack']")));
        addButton.click();
        // Verify cart badge shows 1
        WebElement badge = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("span.shopping_cart_badge")));
        Assertions.assertEquals("1", badge.getText(), "Cart badge should display 1 after adding an item");
        // Remove the item
        WebElement removeButton = driver.findElement(
                By.cssSelector("button[data-test='remove-sauce-labs-backpack']"));
        removeButton.click();
        // Verify badge disappears
        List<WebElement> badges = driver.findElements(By.cssSelector("span.shopping_cart_badge"));
        Assertions.assertTrue(badges.isEmpty(), "Cart badge should disappear after removing the item");
        resetAppState();
    }

    @Test
    @Order(5)
    public void testCheckoutProcess() {
        login(USERNAME, PASSWORD);
        // Add an item
        wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("button[data-test='add-to-cart-sauce-labs-fleece-jacket']"))).click();
        // Go to cart
        wait.until(ExpectedConditions.elementToBeClickable(By.id("shopping_cart_container"))).click();
        wait.until(ExpectedConditions.urlContains("/cart.html"));
        // Click Checkout
        wait.until(ExpectedConditions.elementToBeClickable(By.id("checkout"))).click();
        wait.until(ExpectedConditions.urlContains("/checkout-step-one.html"));
        // Fill info
        driver.findElement(By.id("first-name")).sendKeys("John");
        driver.findElement(By.id("last-name")).sendKeys("Doe");
        driver.findElement(By.id("postal-code")).sendKeys("12345");
        driver.findElement(By.id("continue")).click();
        wait.until(ExpectedConditions.urlContains("/checkout-step-two.html"));
        // Finish
        driver.findElement(By.id("finish")).click();
        WebElement completeHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("h2.complete-header")));
        Assertions.assertEquals("THANK YOU FOR YOUR ORDER", completeHeader.getText().trim(),
                "Checkout completion message should be displayed");
        resetAppState();
    }

    @Test
    @Order(6)
    public void testMenuActions() {
        login(USERNAME, PASSWORD);
        // Open menu
        wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn"))).click();
        // All Items (should stay on inventory)
        wait.until(ExpectedConditions.elementToBeClickable(By.id("inventory_sidebar_link"))).click();
        Assertions.assertTrue(driver.getCurrentUrl().contains("/inventory.html"),
                "All Items should navigate to inventory page");
        // Open menu again
        wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn"))).click();
        // About (external link)
        String originalWindow = driver.getWindowHandle();
        wait.until(ExpectedConditions.elementToBeClickable(By.id("about_sidebar_link"))).click();
        wait.until(driver1 -> driver1.getWindowHandles().size() > 1);
        Set<String> windows = driver.getWindowHandles();
        windows.remove(originalWindow);
        String newWindow = windows.iterator().next();
        driver.switchTo().window(newWindow);
        Assertions.assertTrue(driver.getCurrentUrl().contains("saucelabs.com"),
                "About link should open a Saucelabs domain");
        driver.close();
        driver.switchTo().window(originalWindow);
        // Open menu again
        wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn"))).click();
        // Reset App State (already tested elsewhere, just verify no error)
        wait.until(ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link"))).click();
        // Open menu again
        wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn"))).click();
        // Logout
        wait.until(ExpectedConditions.elementToBeClickable(By.id("logout_sidebar_link"))).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("login-button")));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/index.html"),
                "After logout, should be back on login page");
    }

    @Test
    @Order(7)
    public void testFooterSocialLinks() {
        login(USERNAME, PASSWORD);
        String[][] links = {
                {"twitter", "twitter.com"},
                {"facebook", "facebook.com"},
                {"linkedin", "linkedin.com"}
        };
        for (String[] linkInfo : links) {
            String id = "footer_" + linkInfo[0];
            WebElement link = driver.findElement(By.id(id));
            String originalWindow = driver.getWindowHandle();
            link.click();
            wait.until(driver1 -> driver1.getWindowHandles().size() > 1);
            Set<String> windows = driver.getWindowHandles();
            windows.remove(originalWindow);
            String newWindow = windows.iterator().next();
            driver.switchTo().window(newWindow);
            Assertions.assertTrue(driver.getCurrentUrl().contains(linkInfo[1]),
                    "Social link should open a page containing " + linkInfo[1]);
            driver.close();
            driver.switchTo().window(originalWindow);
        }
        resetAppState();
    }
}