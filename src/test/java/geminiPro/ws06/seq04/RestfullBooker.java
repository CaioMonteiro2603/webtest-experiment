package geminiPRO.ws06.seq04;

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
import java.util.List;
import java.util.Set;

/**
 * A comprehensive JUnit 5 test suite for an e-commerce platform.
 * NOTE: The prompt provided a BASE_URL for a hotel booking site, but the functional
 * requirements (login, sorting, burger menu, etc.) are characteristic of the SauceDemo website.
 * This test suite targets SauceDemo to align with the detailed functional scope.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ECommerceEndToEndTest {

    private static WebDriver driver;
    private static WebDriverWait wait;

    // --- Site Configuration (for SauceDemo) ---
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
    static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless"); // Use argument for headless mode as required.
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    static void tearDown() {
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
    
    private void testExternalLink(By locator, String expectedDomain) {
        String originalWindow = driver.getWindowHandle();
        wait.until(ExpectedConditions.visibilityOfElementLocated(INVENTORY_CONTAINER));
        wait.until(ExpectedConditions.elementToBeClickable(locator)).click();

        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        Set<String> allWindows = driver.getWindowHandles();
        allWindows.remove(originalWindow);
        String newWindow = allWindows.iterator().next();
        driver.switchTo().window(newWindow);

        String currentUrl = driver.getCurrentUrl();
        Assertions.assertTrue(currentUrl.contains(expectedDomain),
            "URL of the new window should contain '" + expectedDomain + "'. Actual: " + currentUrl);

        driver.close();
        driver.switchTo().window(originalWindow);
        Assertions.assertEquals(1, driver.getWindowHandles().size(), "Should have returned to the original window.");
    }


    @Test
    @Order(1)
    void testLoginFunctionality() {
        // Test 1: Locked out user
        performLogin(LOCKED_USER, PASSWORD);
        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(ERROR_MESSAGE_CONTAINER));
        Assertions.assertTrue(error.getText().contains("Sorry, this user has been locked out."), "Error message for locked user is incorrect.");

        // Test 2: Invalid password
        performLogin(VALID_USER, "incorrect_password");
        error = wait.until(ExpectedConditions.visibilityOfElementLocated(ERROR_MESSAGE_CONTAINER));
        Assertions.assertTrue(error.getText().contains("Username and password do not match"), "Error message for invalid password is not as expected.");
    }
    
    @Test
    @Order(2)
    void testSuccessfulLoginAndLogout() {
        performLogin(VALID_USER, PASSWORD);
        wait.until(ExpectedConditions.urlContains("/inventory.html"));
        Assertions.assertTrue(driver.findElement(INVENTORY_CONTAINER).isDisplayed(), "Inventory container should be visible after login.");

        wait.until(ExpectedConditions.elementToBeClickable(BURGER_MENU_BUTTON)).click();
        wait.until(ExpectedConditions.elementToBeClickable(LOGOUT_LINK)).click();

        wait.until(ExpectedConditions.urlToBe(BASE_URL));
        Assertions.assertTrue(driver.findElement(LOGIN_BUTTON).isDisplayed(), "Login button should be visible after logout.");
    }
    
    @Test
    @Order(3)
    void testProductSortingMechanisms() {
        performLogin(VALID_USER, PASSWORD);

        // Default: Name (A to Z)
        List<WebElement> items = driver.findElements(INVENTORY_ITEM_NAME);
        Assertions.assertEquals("Sauce Labs Backpack", items.get(0).getText(), "Default sort order (A-Z) is incorrect.");

        // Name (Z to A)
        new Select(driver.findElement(SORT_DROPDOWN)).selectByValue("za");
        items = wait.until(ExpectedConditions.numberOfElementsToBe(INVENTORY_ITEM_NAME, 6));
        Assertions.assertEquals("Test.allTheThings() T-Shirt (Red)", items.get(0).getText(), "Sort by Name (Z-A) is incorrect.");

        // Price (low to high)
        new Select(driver.findElement(SORT_DROPDOWN)).selectByValue("lohi");
        items = wait.until(ExpectedConditions.numberOfElementsToBe(INVENTORY_ITEM_NAME, 6));
        Assertions.assertEquals("Sauce Labs Onesie", items.get(0).getText(), "Sort by Price (low to high) is incorrect.");

        // Price (high to low)
        new Select(driver.findElement(SORT_DROPDOWN)).selectByValue("hilo");
        items = wait.until(ExpectedConditions.numberOfElementsToBe(INVENTORY_ITEM_NAME, 6));
        Assertions.assertEquals("Sauce Labs Fleece Jacket", items.get(0).getText(), "Sort by Price (high to low) is incorrect.");
    }
    
    @Test
    @Order(4)
    void testFullPurchaseFlow() {
        performLogin(VALID_USER, PASSWORD);
        resetAppState(); // Ensure clean state

        // Add item to cart
        wait.until(ExpectedConditions.elementToBeClickable(By.id("add-to-cart-sauce-labs-fleece-jacket"))).click();
        WebElement cartBadge = wait.until(ExpectedConditions.visibilityOfElementLocated(SHOPPING_CART_BADGE));
        Assertions.assertEquals("1", cartBadge.getText(), "Cart badge should display '1'.");

        // Navigate to cart and verify
        cartBadge.click();
        wait.until(ExpectedConditions.urlContains("/cart.html"));
        Assertions.assertEquals("Sauce Labs Fleece Jacket", driver.findElement(INVENTORY_ITEM_NAME).getText(), "Correct item is not in the cart.");

        // Checkout
        wait.until(ExpectedConditions.elementToBeClickable(By.id("checkout"))).click();
        wait.until(ExpectedConditions.urlContains("/checkout-step-one.html"));
        driver.findElement(By.id("first-name")).sendKeys("Gemini");
        driver.findElement(By.id("last-name")).sendKeys("Pro");
        driver.findElement(By.id("postal-code")).sendKeys("12345");
        driver.findElement(By.id("continue")).click();

        // Finish purchase
        wait.until(ExpectedConditions.urlContains("/checkout-step-two.html"));
        wait.until(ExpectedConditions.elementToBeClickable(By.id("finish"))).click();

        // Assert completion
        wait.until(ExpectedConditions.urlContains("/checkout-complete.html"));
        WebElement completeHeader = driver.findElement(By.className("complete-header"));
        Assertions.assertEquals("Thank you for your order!", completeHeader.getText(), "Order confirmation message is incorrect.");

        // Return to products and assert empty cart
        driver.findElement(By.id("back-to-products")).click();
        wait.until(ExpectedConditions.urlContains("/inventory.html"));
        Assertions.assertTrue(driver.findElements(SHOPPING_CART_BADGE).isEmpty(), "Cart should be empty after completing the purchase.");
    }
    
    @Test
    @Order(5)
    void testSideMenuNavigationAndStateReset() {
        performLogin(VALID_USER, PASSWORD);
        
        // Test "About" link
        wait.until(ExpectedConditions.elementToBeClickable(BURGER_MENU_BUTTON)).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("about_sidebar_link")));
        testExternalLink(By.id("about_sidebar_link"), "saucelabs.com");
        
        // Test "Reset App State"
        wait.until(ExpectedConditions.elementToBeClickable(By.id("add-to-cart-sauce-labs-onesie"))).click();
        Assertions.assertEquals("1", driver.findElement(SHOPPING_CART_BADGE).getText(), "Cart should contain 1 item before reset.");
        
        resetAppState();
        
        Assertions.assertTrue(driver.findElements(SHOPPING_CART_BADGE).isEmpty(), "Cart should be empty after resetting app state.");
    }
    
    @Test
    @Order(6)
    void testFooterSocialLinks() {
        performLogin(VALID_USER, PASSWORD);
        
        testExternalLink(By.linkText("Twitter"), "twitter.com");
        testExternalLink(By.linkText("Facebook"), "facebook.com/saucelabs");
        testExternalLink(By.linkText("LinkedIn"), "linkedin.com/company/sauce-labs");
    }
}