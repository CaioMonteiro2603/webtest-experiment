package geminiPRO.ws02.seq04;

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
import java.util.stream.Collectors;

/**
 * Comprehensive JUnit 5 test suite for the SauceDemo e-commerce website using Selenium WebDriver with Firefox in headless mode.
 * NOTE: The prompt provided a BASE_URL for ParaBank but described test cases (sorting, burger menu, etc.) specific to SauceDemo.
 * This suite tests SauceDemo to match the functional requirements.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SauceDemoHeadlessTest {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://www.saucedemo.com/";
    private static final String VALID_USER = "standard_user";
    private static final String LOCKED_USER = "locked_out_user";
    private static final String PASSWORD = "secret_sauce";

    // --- Locators ---
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
        options.addArguments("--headless"); // Per requirement, use arguments only for headless mode.
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

    private void handleExternalLink(By locator, String expectedDomain) {
        String originalWindowHandle = driver.getWindowHandle();
        wait.until(ExpectedConditions.elementToBeClickable(locator)).click();

        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        Set<String> allWindowHandles = driver.getWindowHandles();
        allWindowHandles.remove(originalWindowHandle);
        String newWindowHandle = allWindowHandles.iterator().next();

        driver.switchTo().window(newWindowHandle);
        String currentUrl = driver.getCurrentUrl();
        Assertions.assertTrue(currentUrl.contains(expectedDomain), "URL of new window should contain '" + expectedDomain + "'. Actual: " + currentUrl);

        driver.close();
        driver.switchTo().window(originalWindowHandle);
        Assertions.assertEquals(1, driver.getWindowHandles().size(), "Should have returned to the original window.");
    }

    private void resetAppState() {
        wait.until(ExpectedConditions.elementToBeClickable(BURGER_MENU_BUTTON)).click();
        WebElement resetLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link")));
        resetLink.click();
        // Close the menu by clicking the 'X' button
        WebElement closeMenuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-cross-btn")));
        closeMenuButton.click();
        // Wait for menu to be hidden
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.className("bm-menu-wrap")));
    }


    @Test
    @Order(1)
    void testInvalidLogins() {
        // Test with locked out user
        performLogin(LOCKED_USER, PASSWORD);
        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(ERROR_MESSAGE_CONTAINER));
        Assertions.assertTrue(error.getText().contains("Sorry, this user has been locked out."), "Error message for locked user is incorrect.");

        // Test with invalid password
        performLogin(VALID_USER, "wrong_password");
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
    void testProductSortOrder() {
        performLogin(VALID_USER, PASSWORD);

        // Default sort: Name (A to Z)
        List<WebElement> items = driver.findElements(INVENTORY_ITEM_NAME);
        Assertions.assertEquals("Sauce Labs Backpack", items.get(0).getText(), "Default sort order first item is incorrect.");

        // Sort by Name (Z to A)
        new Select(driver.findElement(SORT_DROPDOWN)).selectByValue("za");
        items = wait.until(ExpectedConditions.numberOfElementsToBe(INVENTORY_ITEM_NAME, 6));
        Assertions.assertEquals("Test.allTheThings() T-Shirt (Red)", items.get(0).getText(), "Sort by Z-A first item is incorrect.");

        // Sort by Price (low to high)
        new Select(driver.findElement(SORT_DROPDOWN)).selectByValue("lohi");
        items = wait.until(ExpectedConditions.numberOfElementsToBe(INVENTORY_ITEM_NAME, 6));
        Assertions.assertEquals("Sauce Labs Onesie", items.get(0).getText(), "Sort by Price Low-High first item is incorrect.");

        // Sort by Price (high to low)
        new Select(driver.findElement(SORT_DROPDOWN)).selectByValue("hilo");
        items = wait.until(ExpectedConditions.numberOfElementsToBe(INVENTORY_ITEM_NAME, 6));
        Assertions.assertEquals("Sauce Labs Fleece Jacket", items.get(0).getText(), "Sort by Price High-Low first item is incorrect.");
    }

    @Test
    @Order(4)
    void testFullCheckoutFlow() {
        performLogin(VALID_USER, PASSWORD);
        resetAppState(); // Ensure clean state before starting

        // Add item to cart and verify badge
        wait.until(ExpectedConditions.elementToBeClickable(By.id("add-to-cart-sauce-labs-backpack"))).click();
        WebElement cartBadge = wait.until(ExpectedConditions.visibilityOfElementLocated(SHOPPING_CART_BADGE));
        Assertions.assertEquals("1", cartBadge.getText(), "Cart badge should show 1 after adding an item.");

        // Go to cart and verify item
        cartBadge.click();
        wait.until(ExpectedConditions.urlContains("/cart.html"));
        List<WebElement> cartItems = driver.findElements(By.className("inventory_item_name"));
        Assertions.assertEquals(1, cartItems.size(), "There should be one item in the cart.");
        Assertions.assertEquals("Sauce Labs Backpack", cartItems.get(0).getText(), "The correct item should be in the cart.");

        // Proceed to checkout
        wait.until(ExpectedConditions.elementToBeClickable(By.id("checkout"))).click();
        wait.until(ExpectedConditions.urlContains("/checkout-step-one.html"));

        // Fill user information
        driver.findElement(By.id("first-name")).sendKeys("Caio");
        driver.findElement(By.id("last-name")).sendKeys("Gemini");
        driver.findElement(By.id("postal-code")).sendKeys("12345");
        driver.findElement(By.id("continue")).click();

        // Finalize purchase
        wait.until(ExpectedConditions.urlContains("/checkout-step-two.html"));
        wait.until(ExpectedConditions.elementToBeClickable(By.id("finish"))).click();

        // Verify completion
        wait.until(ExpectedConditions.urlContains("/checkout-complete.html"));
        WebElement completeHeader = driver.findElement(By.className("complete-header"));
        Assertions.assertEquals("Thank you for your order!", completeHeader.getText(), "Checkout completion message is incorrect.");

        // Go back and check if cart is empty
        driver.findElement(By.id("back-to-products")).click();
        wait.until(ExpectedConditions.urlContains("/inventory.html"));
        List<WebElement> badges = driver.findElements(SHOPPING_CART_BADGE);
        Assertions.assertTrue(badges.isEmpty(), "Cart badge should be gone after completing an order.");
    }

    @Test
    @Order(5)
    void testBurgerMenuLinks() {
        performLogin(VALID_USER, PASSWORD);
        
        // Test "About" link
        wait.until(ExpectedConditions.elementToBeClickable(BURGER_MENU_BUTTON)).click();
        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("about_sidebar_link")));
        
        String originalWindow = driver.getWindowHandle();
        aboutLink.click();
        
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        Set<String> allWindows = driver.getWindowHandles();
        allWindows.remove(originalWindow);
        String newWindow = allWindows.iterator().next();
        driver.switchTo().window(newWindow);
        
        wait.until(ExpectedConditions.urlContains("saucelabs.com"));
        Assertions.assertTrue(driver.getCurrentUrl().startsWith("https://saucelabs.com/"), "About link should navigate to saucelabs.com.");
        
        driver.close();
        driver.switchTo().window(originalWindow);
        
        // Test "Reset App State" by adding an item first
        wait.until(ExpectedConditions.elementToBeClickable(By.id("add-to-cart-sauce-labs-bike-light"))).click();
        Assertions.assertEquals("1", driver.findElement(SHOPPING_CART_BADGE).getText(), "Cart should have 1 item before reset.");
        
        resetAppState();
        
        Assertions.assertTrue(driver.findElements(SHOPPING_CART_BADGE).isEmpty(), "Cart should be empty after resetting app state.");
    }
    
    @Test
    @Order(6)
    void testFooterExternalLinks() {
        performLogin(VALID_USER, PASSWORD);
        wait.until(ExpectedConditions.visibilityOfElementLocated(INVENTORY_CONTAINER));

        // Test Twitter link
        handleExternalLink(By.linkText("Twitter"), "twitter.com");

        // Test Facebook link
        handleExternalLink(By.linkText("Facebook"), "facebook.com/saucelabs");

        // Test LinkedIn link
        handleExternalLink(By.linkText("LinkedIn"), "linkedin.com/company/sauce-labs");
    }
}