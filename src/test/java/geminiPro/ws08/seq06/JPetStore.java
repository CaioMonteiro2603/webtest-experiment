package geminiPRO.ws08.seq06;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * JUnit 5 test suite for the JPetStore demo application.
 * This suite covers searching for pets, adding items to the cart, cart management,
 * user login, the full checkout process, and external link validation.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class JPetStoreTest {

    private static final String BASE_URL = "https://jpetstore.aspectran.com/catalog/";
    private static final Duration WAIT_TIMEOUT = Duration.ofSeconds(10);
    private static final String VALID_USER = "j2ee";
    private static final String VALID_PASS = "j2ee";

    private static WebDriver driver;
    private static WebDriverWait wait;

    // Locators
    private static final By SIGN_IN_LINK = By.linkText("Sign In");
    private static final By SIGN_OUT_LINK = By.linkText("Sign Out");
    private static final By WELCOME_CONTENT = By.id("WelcomeContent");
    private static final By USERNAME_INPUT = By.name("username");
    private static final By PASSWORD_INPUT = By.name("password");
    private static final By LOGIN_BUTTON = By.name("signon");
    private static final By CART_LINK = By.xpath("//div[@id='MenuContent']/a[contains(@href, 'viewCart')]");

    @BeforeAll
    static void setupAll() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless"); // Use headless mode via arguments ONLY
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, WAIT_TIMEOUT);
    }

    @AfterAll
    static void tearDownAll() {
        if (driver != null) {
            driver.quit();
        }
    }

    @BeforeEach
    void setupEach() {
        driver.get(BASE_URL);
    }

    /**
     * Tests the pet search functionality from the home page.
     */
    @Test
    @Order(1)
    void productSearchTest() {
        WebElement searchInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("keyword")));
        searchInput.sendKeys("Dalmation");
        driver.findElement(By.name("searchProducts")).click();

        wait.until(ExpectedConditions.urlContains("searchProducts"));
        WebElement productLink = wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText("Spotted Adult Female Dalmation")));
        assertTrue(productLink.isDisplayed(), "The search result for 'Dalmation' was not found.");
    }

    /**
     * Tests navigating through categories to a product and adding it to the cart.
     */
    @Test
    @Order(2)
    void navigationAndAddToCartTest() {
        // Navigate through categories: Fish -> Angelfish -> Add Large Angelfish to cart
        driver.findElement(By.xpath("//div[@id='Content']//a[contains(@href, 'FISH')]")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//td[contains(., 'Angelfish')]")));
        driver.findElement(By.xpath("//a[contains(@href, 'FI-SW-01')]")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//td[contains(., 'Large Angelfish')]")));
        driver.findElement(By.xpath("//a[@href='/cart/addItemToCart?itemId=EST-1']")).click();

        // Verify the cart page and the item within it
        wait.until(ExpectedConditions.urlContains("viewCart"));
        WebElement cartItem = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//td[contains(., 'Large Angelfish')]")));
        assertTrue(cartItem.isDisplayed(), "The correct item was not found in the shopping cart.");
    }

    /**
     * Tests updating an item's quantity in the cart and then removing it.
     */
    @Test
    @Order(3)
    void cartManagementTest() {
        // First, add an item to the cart to manage
        driver.get(BASE_URL + "categories/CATS");
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("FL-DSH-01"))).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Add to Cart"))).click();
        wait.until(ExpectedConditions.urlContains("viewCart"));

        // Update quantity
        WebElement quantityInput = driver.findElement(By.name("EST-18"));
        quantityInput.clear();
        quantityInput.sendKeys("3");
        driver.findElement(By.name("updateCartQuantities")).click();

        // Verify updated price. Price is $58.50, so 3 * 58.50 = 175.50
        WebElement totalCost = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//td[contains(text(), '$175.50')]")));
        assertTrue(totalCost.isDisplayed(), "Total cost did not update correctly after changing quantity.");

        // Remove item
        driver.findElement(By.linkText("Remove")).click();
        WebElement emptyCartMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//td[contains(., 'Your cart is empty.')]")));
        assertTrue(emptyCartMessage.isDisplayed(), "Cart did not show 'empty' message after removing item.");
    }

    /**
     * Tests both failed and successful user login scenarios.
     */
    @Test
    @Order(4)
    void loginFunctionalityTest() {
        wait.until(ExpectedConditions.elementToBeClickable(SIGN_IN_LINK)).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(LOGIN_BUTTON));

        // Test Case 1: Failed Login
        performLogin(VALID_USER, "wrongpassword");
        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("messages")));
        assertTrue(errorMessage.getText().contains("Invalid username or password."), "Error message for invalid login was incorrect.");

        // Test Case 2: Successful Login
        performLogin(VALID_USER, VALID_PASS);
        WebElement welcomeMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(WELCOME_CONTENT));
        assertTrue(welcomeMessage.getText().contains("Welcome ABC!"), "Welcome message was not found after successful login.");
        assertTrue(driver.findElement(SIGN_OUT_LINK).isDisplayed(), "Sign Out link should be visible after login.");
    }

    /**
     * Tests the complete checkout flow from adding an item to final confirmation.
     */
    @Test
    @Order(5)
    void fullCheckoutFlowTest() {
        // Add an item to the cart
        driver.get(BASE_URL + "categories/REPTILES");
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("RP-SN-01"))).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Add to Cart"))).click();

        // Proceed to checkout and log in
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Proceed to Checkout"))).click();
        performLogin(VALID_USER, VALID_PASS);

        // Continue through shipping and confirmation
        wait.until(ExpectedConditions.urlContains("newOrderForm"));
        wait.until(ExpectedConditions.elementToBeClickable(By.name("newOrder"))).click();
        wait.until(ExpectedConditions.urlContains("confirmOrder"));
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Confirm"))).click();

        // Verify the final success message
        wait.until(ExpectedConditions.urlContains("viewOrder"));
        WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("messages")));
        assertTrue(successMessage.getText().contains("Thank you, your order has been submitted."), "Order confirmation message was not found.");
    }
    
    /**
     * Verifies that the external link in the footer opens correctly.
     */
    @Test
    @Order(6)
    void externalLinkTest() {
        String originalWindow = driver.getWindowHandle();
        WebElement mybatisLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("www.mybatis.org")));
        mybatisLink.click();
        
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.contentEquals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        wait.until(ExpectedConditions.urlContains("mybatis.org"));
        assertTrue(driver.getCurrentUrl().contains("mybatis.org"), "URL of the new tab should contain 'mybatis.org'.");

        driver.close();
        driver.switchTo().window(originalWindow);
        assertEquals(1, driver.getWindowHandles().size(), "Should have switched back to the original window.");
    }

    // --- Helper Methods ---

    /**
     * Fills the login form and clicks the login button.
     * Assumes driver is already on the login page.
     *
     * @param username The username to enter.
     * @param password The password to enter.
     */
    private void performLogin(String username, String password) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(USERNAME_INPUT)).clear();
        driver.findElement(USERNAME_INPUT).sendKeys(username);
        driver.findElement(PASSWORD_INPUT).clear();
        driver.findElement(PASSWORD_INPUT).sendKeys(password);
        driver.findElement(LOGIN_BUTTON).click();
    }
}