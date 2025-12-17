package geminiPro.ws08.seq02;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * A complete JUnit 5 test suite for the JPetStore demo application.
 * This test uses Selenium WebDriver with Firefox running in headless mode
 * and covers the full end-to-end purchase flow.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class JPetStore{

    // Constants for configuration
    private static final String BASE_URL = "https://jpetstore.aspectran.com/";
    private static final Duration WAIT_TIMEOUT = Duration.ofSeconds(10);
    private static final String VALID_USERNAME = "j2ee";
    private static final String VALID_PASSWORD = "j2ee";

    // WebDriver and WebDriverWait instances shared across all tests
    private static WebDriver driver;
    private static WebDriverWait wait;

    // Locators
    private final By enterStoreLink = By.linkText("Enter the Store");
    private final By signInLink = By.linkText("Sign In");
    private final By signOutLink = By.linkText("Sign Out");
    private final By usernameInput = By.name("username");
    private final By passwordInput = By.name("password");
    private final By loginButton = By.name("signon");
    private final By welcomeContent = By.id("WelcomeContent");
    private final By errorMessage = By.cssSelector("#Content .messages li");
    private final By searchInput = By.name("keyword");
    private final By searchButton = By.name("searchProducts");

    // --- WebDriver Lifecycle ---

    @BeforeAll
    static void setup() {
        // As per requirements, initialize Firefox in headless mode via arguments
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        driver.manage().window().setSize(new Dimension(1920, 1080));
        wait = new WebDriverWait(driver, WAIT_TIMEOUT);
    }

    @AfterAll
    static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @BeforeEach
    void navigateToHome() {
        driver.get(BASE_URL);
    }

    // --- Test Cases ---

    @Test
    @Order(1)
    @DisplayName("Should enter the store and verify main categories are present")
    void testEnterStoreAndVerifyCategories() {
        enterTheStore();
        wait.until(ExpectedConditions.urlContains("catalog/main"));
        WebElement fishCategory = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@id='SidebarContent']/a[contains(@href, 'FISH')]")));
        Assertions.assertTrue(fishCategory.isDisplayed(), "The 'Fish' category link should be visible after entering the store.");
    }

    @Test
    @Order(2)
    @DisplayName("Should show an error message for invalid login credentials")
    void testInvalidLogin() {
        enterTheStore();
        wait.until(ExpectedConditions.elementToBeClickable(signInLink)).click();
        performLogin("invalid-user", "invalid-password");
        WebElement message = wait.until(ExpectedConditions.visibilityOfElementLocated(errorMessage));
        Assertions.assertTrue(message.getText().contains("Invalid username or password."), "Error message for invalid login is incorrect.");
    }

    @Test
    @Order(3)
    @DisplayName("Should log in and log out successfully")
    void testSuccessfulLoginAndLogout() {
        enterTheStore();
        wait.until(ExpectedConditions.elementToBeClickable(signInLink)).click();
        performLogin(VALID_USERNAME, VALID_PASSWORD);

        WebElement welcomeMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(welcomeContent));
        Assertions.assertTrue(welcomeMessage.getText().contains("Welcome ABC!"), "Welcome message is not displayed after login.");

        wait.until(ExpectedConditions.elementToBeClickable(signOutLink)).click();
        WebElement signInLinkAfterLogout = wait.until(ExpectedConditions.visibilityOfElementLocated(signInLink));
        Assertions.assertTrue(signInLinkAfterLogout.isDisplayed(), "Sign In link should be visible after logout.");
    }

    @Test
    @Order(4)
    @DisplayName("Should search for a product and find it in the results")
    void testProductSearch() {
        enterTheStore();
        wait.until(ExpectedConditions.visibilityOfElementLocated(searchInput)).sendKeys("Bulldog");
        driver.findElement(searchButton).click();

        wait.until(ExpectedConditions.urlContains("searchProducts"));
        WebElement resultsTable = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("Catalog")));
        Assertions.assertTrue(resultsTable.getText().contains("Bulldog"), "Search results table should contain 'Bulldog'.");
    }

    @Test
    @Order(5)
    @DisplayName("Should complete the full end-to-end purchase flow for an item")
    void testFullPurchaseFlow() {
        // --- 1. Enter Store and Login ---
        enterTheStore();
        wait.until(ExpectedConditions.elementToBeClickable(signInLink)).click();
        performLogin(VALID_USERNAME, VALID_PASSWORD);
        wait.until(ExpectedConditions.visibilityOfElementLocated(welcomeContent));

        // --- 2. Navigate to an Item and Add to Cart ---
        // Go to Fish category
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//div[@id='SidebarContent']/a[contains(@href, 'FISH')]"))).click();
        // Go to Angelfish product
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("FI-SW-01"))).click();
        // Add Large Angelfish to cart
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(@href, 'EST-1')]"))).click();

        // --- 3. Verify Cart and Proceed to Checkout ---
        wait.until(ExpectedConditions.urlContains("cart/viewCart"));
        WebElement cartTable = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("Cart")));
        Assertions.assertTrue(cartTable.getText().contains("Large Angelfish"), "Cart should contain 'Large Angelfish'.");
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Proceed to Checkout"))).click();

        // --- 4. Confirm Billing and Shipping ---
        wait.until(ExpectedConditions.urlContains("order/newOrderForm"));
        wait.until(ExpectedConditions.elementToBeClickable(By.name("newOrder"))).click();

        // --- 5. Confirm Final Order ---
        wait.until(ExpectedConditions.urlContains("order/confirmOrder"));
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Confirm"))).click();

        // --- 6. Assert Success ---
        wait.until(ExpectedConditions.urlContains("order/viewOrder"));
        WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//div[@id='Content']//li[contains(text(), 'Thank you, your order has been submitted.')]")
        ));
        Assertions.assertTrue(successMessage.isDisplayed(), "Order confirmation message was not found.");
    }


    // --- Helper Methods ---

    /**
     * Clicks the "Enter the Store" link on the landing page.
     */
    private void enterTheStore() {
        wait.until(ExpectedConditions.elementToBeClickable(enterStoreLink)).click();
    }

    /**
     * Fills the login form and submits. Assumes driver is already on the login page.
     * @param username The username to enter.
     * @param password The password to enter.
     */
    private void performLogin(String username, String password) {
        // Clear fields first to prevent issues on retries
        WebElement userField = wait.until(ExpectedConditions.visibilityOfElementLocated(usernameInput));
        userField.clear();
        userField.sendKeys(username);

        WebElement passField = driver.findElement(passwordInput);
        passField.clear();
        passField.sendKeys(password);

        driver.findElement(loginButton).click();
    }
}