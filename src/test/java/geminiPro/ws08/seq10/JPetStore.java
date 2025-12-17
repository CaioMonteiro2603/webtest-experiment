package geminiPro.ws08.seq10;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.UUID;
import java.util.List;

/**
 * A comprehensive JUnit 5 test suite for the JPetStore demo application.
 * This suite covers the full e-commerce lifecycle including user registration, login,
 * product search, adding items to a cart, and completing a purchase.
 * It uses Selenium WebDriver with Firefox in headless mode.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class JPetStore {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://jpetstore.aspectran.com/actions/Catalog.action";

    // --- Test User Details ---
    // Using a unique username to ensure the registration test can be re-run.
    private static final String USERNAME = "gemini-" + UUID.randomUUID().toString().substring(0, 8);
    private static final String PASSWORD = "password123";

    // --- Locators ---
    private static final By SIGN_IN_LINK = By.linkText("Sign In");
    private static final By SIGN_OUT_LINK = By.linkText("Sign Out");
    private static final By REGISTER_NOW_LINK = By.linkText("Register Now!");
    private static final By WELCOME_CONTENT = By.id("WelcomeContent");

    // Registration & Account
    private static final By USERNAME_INPUT = By.name("username");
    private static final By PASSWORD_INPUT = By.name("password");
    private static final By REPEATED_PASSWORD_INPUT = By.name("repeatedPassword");
    private static final By FIRST_NAME_INPUT = By.name("account.firstName");
    private static final By LAST_NAME_INPUT = By.name("account.lastName");
    private static final By EMAIL_INPUT = By.name("account.email");
    private static final By PHONE_INPUT = By.name("account.phone");
    private static final By ADDRESS_1_INPUT = By.name("account.address1");
    private static final By CITY_INPUT = By.name("account.city");
    private static final By STATE_INPUT = By.name("account.state");
    private static final By ZIP_INPUT = By.name("account.zip");
    private static final By COUNTRY_INPUT = By.name("account.country");
    private static final By SAVE_ACCOUNT_BUTTON = By.name("newAccount");

    // Login
    private static final By LOGIN_BUTTON = By.name("signon");
    private static final By LOGIN_ERROR_MESSAGE = By.cssSelector(".messages li");

    // Search
    private static final By SEARCH_INPUT = By.name("keyword");
    private static final By SEARCH_BUTTON = By.name("searchProducts");
    
    // Catalog & Cart
    private static final By DOGS_CATEGORY_LINK = By.xpath("//div[@id='SidebarContent']/a[contains(@href, 'DOGS')]");
    private static final By ADD_TO_CART_BUTTON = By.linkText("Add to Cart");
    private static final By PROCEED_TO_CHECKOUT_BUTTON = By.linkText("Proceed to Checkout");
    private static final By NEW_ORDER_BUTTON = By.name("newOrder");
    private static final By CONFIRM_ORDER_BUTTON = By.linkText("Confirm");
    private static final By THANK_YOU_MESSAGE = By.xpath("//li[contains(text(),'Thank you')]");


    @BeforeAll
    static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless"); // Use arguments as required
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @Order(1)
    @DisplayName("🧪 Test New User Registration")
    void testUserRegistration() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.elementToBeClickable(SIGN_IN_LINK)).click();
        wait.until(ExpectedConditions.elementToBeClickable(REGISTER_NOW_LINK)).click();

        // Fill out registration form
        wait.until(ExpectedConditions.visibilityOfElementLocated(USERNAME_INPUT)).sendKeys(USERNAME);
        driver.findElement(PASSWORD_INPUT).sendKeys(PASSWORD);
        driver.findElement(REPEATED_PASSWORD_INPUT).sendKeys(PASSWORD);
        driver.findElement(FIRST_NAME_INPUT).sendKeys("Gemini");
        driver.findElement(LAST_NAME_INPUT).sendKeys("Tester");
        driver.findElement(EMAIL_INPUT).sendKeys("gemini.tester@example.com");
        driver.findElement(PHONE_INPUT).sendKeys("5551234567");
        driver.findElement(ADDRESS_1_INPUT).sendKeys("123 Test St");
        driver.findElement(CITY_INPUT).sendKeys("Testville");
        driver.findElement(STATE_INPUT).sendKeys("TS");
        driver.findElement(ZIP_INPUT).sendKeys("12345");
        driver.findElement(COUNTRY_INPUT).sendKeys("USA");
        
        driver.findElement(SAVE_ACCOUNT_BUTTON).click();

        // After registration, the user is automatically logged in.
        WebElement welcomeMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(WELCOME_CONTENT));
        Assertions.assertTrue(welcomeMessage.getText().contains("Welcome Gemini!"), "Welcome message should be displayed after registration.");
    }

    @Test
    @Order(2)
    @DisplayName("🧪 Test Logout and Login Functionality")
    void testLoginAndLogout() {
        // --- Logout first ---
        driver.get(BASE_URL);
        // This handles both being logged in from the previous test or starting fresh
        List<WebElement> logoutLinks = driver.findElements(SIGN_OUT_LINK);
        if (!logoutLinks.isEmpty()) {
            logoutLinks.get(0).click();
        }

        // --- Invalid Login ---
        wait.until(ExpectedConditions.elementToBeClickable(SIGN_IN_LINK)).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(USERNAME_INPUT)).sendKeys(USERNAME);
        driver.findElement(PASSWORD_INPUT).sendKeys("wrongpassword");
        driver.findElement(LOGIN_BUTTON).click();
        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(LOGIN_ERROR_MESSAGE));
        Assertions.assertTrue(errorMessage.getText().contains("Invalid username or password"), "Error message for invalid login should be displayed.");
        
        // --- Valid Login ---
        driver.findElement(USERNAME_INPUT).clear();
        driver.findElement(USERNAME_INPUT).sendKeys(USERNAME);
        driver.findElement(PASSWORD_INPUT).clear();
        driver.findElement(PASSWORD_INPUT).sendKeys(PASSWORD);
        driver.findElement(LOGIN_BUTTON).click();
        WebElement welcomeMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(WELCOME_CONTENT));
        Assertions.assertTrue(welcomeMessage.getText().contains("Welcome Gemini!"), "Welcome message should be displayed after valid login.");
        
        // --- Logout ---
        driver.findElement(SIGN_OUT_LINK).click();
        wait.until(ExpectedConditions.elementToBeClickable(SIGN_IN_LINK));
        Assertions.assertTrue(driver.findElement(SIGN_IN_LINK).isDisplayed(), "Sign In link should be visible after logout.");
    }
    
    @Test
    @Order(3)
    @DisplayName("🧪 Test Product Search Functionality")
    void testProductSearch() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(SEARCH_INPUT)).sendKeys("Bulldog");
        driver.findElement(SEARCH_BUTTON).click();
        
        By bulldogLink = By.linkText("K9-BD-01");
        WebElement searchResult = wait.until(ExpectedConditions.visibilityOfElementLocated(bulldogLink));
        Assertions.assertTrue(searchResult.isDisplayed(), "Search results should contain a link for Bulldog (K9-BD-01).");
    }

    @Test
    @Order(4)
    @DisplayName("🧪 Test Full End-to-End Purchase Flow")
    void testFullPurchaseE2EFlow() {
        // Log in to start the purchase
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.elementToBeClickable(SIGN_IN_LINK)).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(USERNAME_INPUT)).sendKeys(USERNAME);
        driver.findElement(PASSWORD_INPUT).sendKeys(PASSWORD);
        driver.findElement(LOGIN_BUTTON).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(WELCOME_CONTENT));

        // Navigate and add an item to cart
        driver.findElement(DOGS_CATEGORY_LINK).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("K9-BD-01"))).click(); // Bulldog
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("EST-6"))).click(); // Male Adult Bulldog
        wait.until(ExpectedConditions.elementToBeClickable(ADD_TO_CART_BUTTON)).click();

        // Proceed to checkout
        wait.until(ExpectedConditions.elementToBeClickable(PROCEED_TO_CHECKOUT_BUTTON)).click();
        wait.until(ExpectedConditions.elementToBeClickable(NEW_ORDER_BUTTON)).click(); // Continue on shipping page
        wait.until(ExpectedConditions.elementToBeClickable(CONFIRM_ORDER_BUTTON)).click();
        
        // Assert successful order
        WebElement thankYou = wait.until(ExpectedConditions.visibilityOfElementLocated(THANK_YOU_MESSAGE));
        Assertions.assertTrue(thankYou.isDisplayed(), "Thank you message should be displayed after confirming order.");
    }
}