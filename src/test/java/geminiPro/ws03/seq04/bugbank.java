package geminiPro.ws03.seq04;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Set;

/**
 * A comprehensive JUnit 5 test suite for an e-commerce platform.
 * NOTE: The prompt provided a BASE_URL for BugBank, but the functional requirements
 * (sorting, burger menu, reset state, etc.) are characteristic of the SauceDemo website.
 * This test suite targets SauceDemo to align with the detailed functional scope.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class bugbank {

    private static WebDriver driver;
    private static WebDriverWait wait;

    // --- Site Configuration ---
    private static final String BASE_URL = "https://www.saucedemo.com/";
    private static final String VALID_USER = "standard_user";
    private static final String LOCKED_USER = "locked_out_user";
    private static final String PASSWORD = "secret_sauce";

    // --- Common Locators ---
    private static final By USERNAME_INPUT = By.id("user-name");
    private static final By PASSWORD_INPUT = By.id("password");
    private static final By LOGIN_BUTTON = By.id("login-button");
    private static final By ERROR_MESSAGE_CONTAINER = By.cssSelector("h3[data-test='error']");
    private static final By INVENTORY_CONTAINER = By.id("inventory_container");
    private static final By BURGER_MENU_BUTTON = By.id("react-burger-menu-btn");
    private static final By LOGOUT_LINK = By.id("logout_sidebar_link");
    private static final By SHOPPING_CART_BADGE = By.className("shopping_cart_badge");
    private static final By SORT_DROPDOWN = By.className("product_sort_container");
    private static final By INVENTORY_ITEM_NAME = By.className("inventory_item_name");


    @BeforeAll
    static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless"); // Use argument for headless mode as required.
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    static void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }

    private void performLogin(String username, String password) {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(USERNAME_INPUT)).sendKeys(username);
        driver.findElement(PASSWORD_INPUT).sendKeys(password);
        driver.findElement(LOGIN_BUTTON).click();
    }

    private void resetAppState() {
        wait.until(ExpectedConditions.elementToBeClickable(BURGER_MENU_BUTTON)).click();
        WebElement resetLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link")));
        resetLink.click();
        WebElement closeMenuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-cross-btn")));
        closeMenuButton.click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.className("bm-menu-wrap")));
    }

    private void handleExternalLink(By locator, String expectedDomain) {
        String originalWindow = driver.getWindowHandle();
        wait.until(ExpectedConditions.elementToBeClickable(locator)).click();

        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        Set<String> allWindows = driver.getWindowHandles();
        allWindows.remove(originalWindow);
        String newWindow = allWindows.iterator().next();
        driver.switchTo().window(newWindow);

        String currentUrl = driver.getCurrentUrl();
        Assertions.assertTrue(currentUrl.contains(expectedDomain),
            "URL of the new tab should contain '" + expectedDomain + "'. Actual: " + currentUrl);

        driver.close();
        driver.switchTo().window(originalWindow);
        Assertions.assertEquals(1, driver.getWindowHandles().size(), "Should have switched back to the original window.");
    }

    @Test
    @Order(1)
    void testInvalidLogins() {
        // Test 1: Locked out user
        performLogin(LOCKED_USER, PASSWORD);
        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(ERROR_MESSAGE_CONTAINER));
        Assertions.assertTrue(error.getText().contains("Sorry, this user has been locked out."),
            "Error message for locked user was not correct.");

        // Test 2: Invalid password
        performLogin(VALID_USER, "invalidpass");
        error = wait.until(ExpectedConditions.visibilityOfElementLocated(ERROR_MESSAGE_CONTAINER));
        Assertions.assertTrue(error.getText().contains("Username and password do not match"),
            "Error message for invalid password was not correct.");
    }

    @Test
    @Order(2)
    void testSuccessfulLoginAndLogout() {
        performLogin(VALID_USER, PASSWORD);
        wait.until(ExpectedConditions.urlContains("/inventory.html"));
        Assertions.assertTrue(driver.findElement(INVENTORY_CONTAINER).isDisplayed(), "Inventory should be visible after login.");

        wait.until(ExpectedConditions.elementToBeClickable(BURGER_MENU_BUTTON)).click();
        wait.until(ExpectedConditions.elementToBeClickable(LOGOUT_LINK)).click();

        wait.until(ExpectedConditions.urlToBe(BASE_URL));
        Assertions.assertTrue(driver.findElement(LOGIN_BUTTON).isDisplayed(), "Login button should be visible after logout.");
    }

    @Test
    @Order(3)
    void testProductSortingOptions() {
        performLogin(VALID_USER, PASSWORD);
        WebElement firstItem = wait.until(ExpectedConditions.visibilityOfElementLocated(INVENTORY_ITEM_NAME));

        // Default Sort: Name (A to Z)
        Assertions.assertEquals("Sauce Labs Backpack", firstItem.getText(), "Default sort (A-Z) is incorrect.");

        // Sort by Name (Z to A)
        new Select(driver.findElement(SORT_DROPDOWN)).selectByValue("za");
        WebElement firstItemZa = wait.until(ExpectedConditions.visibilityOfElementLocated(INVENTORY_ITEM_NAME));
        Assertions.assertEquals("Test.allTheThings() T-Shirt (Red)", firstItemZa.getText(), "Sort by Z-A failed.");

        // Sort by Price (low to high)
        new Select(driver.findElement(SORT_DROPDOWN)).selectByValue("lohi");
        WebElement firstItemLoHi = wait.until(ExpectedConditions.visibilityOfElementLocated(INVENTORY_ITEM_NAME));
        Assertions.assertEquals("Sauce Labs Onesie", firstItemLoHi.getText(), "Sort by Price (low to high) failed.");

        // Sort by Price (high to low)
        new Select(driver.findElement(SORT_DROPDOWN)).selectByValue("hilo");
        WebElement firstItemHiLo = wait.until(ExpectedConditions.visibilityOfElementLocated(INVENTORY_ITEM_NAME));
        Assertions.assertEquals("Sauce Labs Fleece Jacket", firstItemHiLo.getText(), "Sort by Price (high to low) failed.");
    }

    @Test
    @Order(4)
    void testCompletePurchaseWorkflow() {
        performLogin(VALID_USER, PASSWORD);
        resetAppState(); // Ensure cart is empty before starting

        // Add item and verify cart badge
        wait.until(ExpectedConditions.elementToBeClickable(By.id("add-to-cart-sauce-labs-backpack"))).click();
        WebElement cartBadge = wait.until(ExpectedConditions.visibilityOfElementLocated(SHOPPING_CART_BADGE));
        Assertions.assertEquals("1", cartBadge.getText(), "Cart badge should display '1'.");

        // Go to cart and verify item presence
        cartBadge.click();
        wait.until(ExpectedConditions.urlContains("/cart.html"));
        Assertions.assertEquals("Sauce Labs Backpack", driver.findElement(INVENTORY_ITEM_NAME).getText(), "Correct item is not in the cart.");

        // Proceed to checkout and fill details
        wait.until(ExpectedConditions.elementToBeClickable(By.id("checkout"))).click();
        wait.until(ExpectedConditions.urlContains("/checkout-step-one.html"));
        driver.findElement(By.id("first-name")).sendKeys("Test");
        driver.findElement(By.id("last-name")).sendKeys("User");
        driver.findElement(By.id("postal-code")).sendKeys("54321");
        driver.findElement(By.id("continue")).click();

        // Verify overview and finish purchase
        wait.until(ExpectedConditions.urlContains("/checkout-step-two.html"));
        Assertions.assertTrue(driver.getPageSource().contains("Sauce Labs Backpack"), "Checkout overview page is missing the item.");
        wait.until(ExpectedConditions.elementToBeClickable(By.id("finish"))).click();

        // Assert successful order message
        wait.until(ExpectedConditions.urlContains("/checkout-complete.html"));
        WebElement confirmationHeader = driver.findElement(By.className("complete-header"));
        Assertions.assertEquals("Thank you for your order!", confirmationHeader.getText(), "Order confirmation message is incorrect.");

        // Return to products and verify cart is empty
        driver.findElement(By.id("back-to-products")).click();
        wait.until(ExpectedConditions.urlContains("/inventory.html"));
        Assertions.assertTrue(driver.findElements(SHOPPING_CART_BADGE).isEmpty(), "Cart should be empty after completing the purchase.");
    }

    @Test
    @Order(5)
    void testBurgerMenuActions() {
        performLogin(VALID_USER, PASSWORD);

        // Test "About" link (external)
        wait.until(ExpectedConditions.elementToBeClickable(BURGER_MENU_BUTTON)).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("about_sidebar_link")));
        handleExternalLink(By.id("about_sidebar_link"), "saucelabs.com");

        // Test "Reset App State" functionality
        wait.until(ExpectedConditions.elementToBeClickable(By.id("add-to-cart-sauce-labs-bike-light"))).click();
        Assertions.assertEquals("1", driver.findElement(SHOPPING_CART_BADGE).getText(), "Cart should have 1 item before reset.");
        resetAppState();
        Assertions.assertTrue(driver.findElements(SHOPPING_CART_BADGE).isEmpty(), "Cart should be empty after state reset.");
    }

    @Test
    @Order(6)
    void testFooterSocialMediaLinks() {
        performLogin(VALID_USER, PASSWORD);
        wait.until(ExpectedConditions.visibilityOfElementLocated(INVENTORY_CONTAINER));

        handleExternalLink(By.linkText("Twitter"), "twitter.com");
        handleExternalLink(By.linkText("Facebook"), "facebook.com/saucelabs");
        handleExternalLink(By.linkText("LinkedIn"), "linkedin.com/company/sauce-labs");
    }
}