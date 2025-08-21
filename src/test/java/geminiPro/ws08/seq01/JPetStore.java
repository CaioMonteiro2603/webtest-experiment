package geminiPro.ws08.seq01;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.UUID;

/**
 * A comprehensive JUnit 5 test suite for the JPetStore e-commerce application.
 * This suite uses Selenium WebDriver with Firefox in headless mode to test the full user journey,
 * including user registration, login, product search, cart management, and the complete checkout process.
 * A unique user is created for each test run to ensure idempotency.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class JPetStoreTest {

    private static WebDriver driver;
    private static WebDriverWait wait;

    // --- Site and User Credentials ---
    private static final String BASE_URL = "https://jpetstore.aspectran.com/catalog/";
    // A unique username is generated once for the entire test suite run
    private static final String USERNAME = "testuser-" + UUID.randomUUID().toString().substring(0, 8);
    private static final String PASSWORD = "password123";

    // --- Locators ---
    // Header & General
    private static final By SIGN_IN_LINK = By.linkText("Sign In");
    private static final By SIGN_OUT_LINK = By.linkText("Sign Out");
    private static final By MY_ACCOUNT_LINK = By.linkText("My Account");
    private static final By WELCOME_CONTENT = By.id("WelcomeContent");

    // Login/Registration
    private static final By LOGIN_USERNAME_INPUT = By.name("username");
    private static final By LOGIN_PASSWORD_INPUT = By.name("password");
    private static final By LOGIN_BUTTON = By.name("signon");
    private static final By REGISTER_NOW_LINK = By.linkText("Register Now!");
    private static final By NEW_USER_BUTTON = By.name("newAccount");
    private static final By ERROR_MESSAGE = By.cssSelector(".messages li");

    // Catalog & Cart
    private static final By SEARCH_INPUT = By.name("keyword");
    private static final By SEARCH_BUTTON = By.name("searchProducts");
    private static final By DOGS_CATEGORY_LINK = By.xpath("//div[@id='SidebarContent']/a[contains(@href, 'categoryId=DOGS')]");
    private static final By ADD_TO_CART_BUTTON = By.linkText("Add to Cart");
    private static final By PROCEED_TO_CHECKOUT_BUTTON = By.linkText("Proceed to Checkout");
    private static final By UPDATE_CART_BUTTON = By.name("updateCartQuantities");

    // Checkout
    private static final By CONTINUE_CHECKOUT_BUTTON = By.name("newOrder");
    private static final By CONFIRM_ORDER_BUTTON = By.linkText("Confirm");
    private static final By ORDER_SUCCESS_MESSAGE = By.xpath("//li[contains(text(),'Thank you, your order has been submitted.')]");


    @BeforeAll
    static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
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
    @DisplayName("Should register a new user successfully")
    void testUserRegistration() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.elementToBeClickable(SIGN_IN_LINK)).click();
        wait.until(ExpectedConditions.elementToBeClickable(REGISTER_NOW_LINK)).click();

        // User Information
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username"))).sendKeys(USERNAME);
        driver.findElement(By.name("password")).sendKeys(PASSWORD);
        driver.findElement(By.name("repeatedPassword")).sendKeys(PASSWORD);

        // Account Information
        driver.findElement(By.name("account.firstName")).sendKeys("Test");
        driver.findElement(By.name("account.lastName")).sendKeys("User");
        driver.findElement(By.name("account.email")).sendKeys("test.user@example.com");
        driver.findElement(By.name("account.phone")).sendKeys("1234567890");
        driver.findElement(By.name("account.address1")).sendKeys("123 Test St");
        driver.findElement(By.name("account.city")).sendKeys("Testville");
        driver.findElement(By.name("account.state")).sendKeys("TS");
        driver.findElement(By.name("account.zip")).sendKeys("12345");
        driver.findElement(By.name("account.country")).sendKeys("USA");
        new Select(driver.findElement(By.name("account.languagePreference"))).selectByVisibleText("english");
        new Select(driver.findElement(By.name("account.favouriteCategoryId"))).selectByVisibleText("DOGS");
        driver.findElement(By.name("account.listOption")).click();

        driver.findElement(NEW_USER_BUTTON).click();

        // Assert successful registration by checking for the Sign Out link
        wait.until(ExpectedConditions.visibilityOfElementLocated(SIGN_OUT_LINK));
        Assertions.assertTrue(driver.findElement(SIGN_OUT_LINK).isDisplayed(), "User registration failed or did not log in automatically.");
    }

    @Test
    @Order(2)
    @DisplayName("Should log out and then fail to log in with an invalid password")
    void testLogoutAndInvalidLogin() {
        // This test assumes user is logged in from the previous step
        wait.until(ExpectedConditions.elementToBeClickable(SIGN_OUT_LINK)).click();

        // Assert logout by checking for the Sign In link
        wait.until(ExpectedConditions.visibilityOfElementLocated(SIGN_IN_LINK));
        Assertions.assertTrue(driver.findElement(SIGN_IN_LINK).isDisplayed(), "User logout failed.");

        // Attempt invalid login
        driver.findElement(SIGN_IN_LINK).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(LOGIN_USERNAME_INPUT)).sendKeys(USERNAME);
        driver.findElement(LOGIN_PASSWORD_INPUT).sendKeys("wrongpassword");
        driver.findElement(LOGIN_BUTTON).click();

        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(ERROR_MESSAGE));
        Assertions.assertTrue(errorMessage.getText().contains("Invalid username or password."), "Error message for invalid login was incorrect or not found.");
    }

    @Test
    @Order(3)
    @DisplayName("Should log in successfully with valid credentials")
    void testSuccessfulLogin() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.elementToBeClickable(SIGN_IN_LINK)).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(LOGIN_USERNAME_INPUT)).sendKeys(USERNAME);
        driver.findElement(LOGIN_PASSWORD_INPUT).sendKeys(PASSWORD);
        driver.findElement(LOGIN_BUTTON).click();

        WebElement welcomeMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(WELCOME_CONTENT));
        Assertions.assertTrue(welcomeMessage.getText().contains("Welcome"), "Login was not successful or welcome message not found.");
        Assertions.assertTrue(driver.findElement(MY_ACCOUNT_LINK).isDisplayed(), "My Account link not visible after login.");
    }

    @Test
    @Order(4)
    @DisplayName("Should search for a product and navigate through categories")
    void testProductSearchAndNavigation() {
        // Search
        wait.until(ExpectedConditions.visibilityOfElementLocated(SEARCH_INPUT)).sendKeys("Goldfish");
        driver.findElement(SEARCH_BUTTON).click();
        WebElement searchResult = wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText("Goldfish")));
        Assertions.assertTrue(searchResult.isDisplayed(), "Search for 'Goldfish' failed.");

        // Category Navigation
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.elementToBeClickable(DOGS_CATEGORY_LINK)).click();
        wait.until(ExpectedConditions.urlContains("categoryId=DOGS"));
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("K9-BD-01"))).click(); // Bulldog
        wait.until(ExpectedConditions.urlContains("productId=K9-BD-01"));
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("EST-6"))).click(); // Male Adult Bulldog
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h2[contains(text(), 'Bulldog')]")));
        
        Assertions.assertTrue(driver.getCurrentUrl().contains("itemId=EST-6"), "Navigation to specific item page failed.");
    }

    @Test
    @Order(5)
    @DisplayName("Should add an item to the cart, update quantity, and proceed to checkout")
    void testAddToCartAndUpdate() {
        // Navigate to a specific item and add it to the cart
        driver.get(BASE_URL + "viewItem.do?itemId=EST-6"); // Male Adult Bulldog
        wait.until(ExpectedConditions.elementToBeClickable(ADD_TO_CART_BUTTON)).click();
        
        // Verify item is in cart
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//td[contains(text(), 'Bulldog')]")));
        WebElement quantityInput = driver.findElement(By.name("EST-6"));
        Assertions.assertEquals("1", quantityInput.getAttribute("value"), "Default quantity in cart should be 1.");
        
        // Update quantity and check total
        WebElement subTotalBefore = driver.findElement(By.xpath("//td[contains(text(), 'Sub Total: $')]"));
        double priceBefore = Double.parseDouble(subTotalBefore.getText().replace("Sub Total: $", "").trim());

        quantityInput.clear();
        quantityInput.sendKeys("3");
        driver.findElement(UPDATE_CART_BUTTON).click();
        
        WebElement subTotalAfter = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//td[contains(text(), 'Sub Total: $')]")));
        double priceAfter = Double.parseDouble(subTotalAfter.getText().replace("Sub Total: $", "").trim());
        Assertions.assertEquals(priceBefore * 3, priceAfter, 0.01, "Sub total did not update correctly after changing quantity.");

        // Proceed to checkout
        driver.findElement(PROCEED_TO_CHECKOUT_BUTTON).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h2[text()='Shipping Address']")));
        Assertions.assertTrue(driver.getCurrentUrl().contains("shippingAddressForm"), "Did not proceed to shipping address page.");
    }
    
    @Test
    @Order(6)
    @DisplayName("Should confirm and place the order")
    void testConfirmAndPlaceOrder() {
        // This test assumes the user is on the shipping address page from the previous test
        wait.until(ExpectedConditions.elementToBeClickable(CONTINUE_CHECKOUT_BUTTON)).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h2[text()='Confirm Order']")));
        
        driver.findElement(CONFIRM_ORDER_BUTTON).click();
        
        WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(ORDER_SUCCESS_MESSAGE));
        Assertions.assertTrue(successMessage.isDisplayed(), "Order confirmation message was not displayed.");
    }
}