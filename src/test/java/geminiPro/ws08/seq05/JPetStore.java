package geminiPRO.ws08.seq05;

import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

/**
 * JUnit 5 test suite for the JPetStore demo application.
 * This suite uses Selenium WebDriver with headless Firefox to test the core e-commerce
 * user journeys, including login, product search, category navigation, and a full
 * end-to-end checkout process.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class JPetStoreE2ETest {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final String BASE_URL = "https://jpetstore.aspectran.com/";
    private static final String CATALOG_URL = BASE_URL + "actions/Catalog.action";
    private static final String USERNAME = "j2ee";
    private static final String PASSWORD = "j2ee";

    // --- Locators ---
    private final By signInLink = By.linkText("Sign In");
    private final By signOutLink = By.linkText("Sign Out");
    private final By myAccountLink = By.linkText("My Account");
    private final By usernameInput = By.name("username");
    private final By passwordInput = By.name("password");
    private final By loginButton = By.name("signon");
    private final By welcomeContent = By.id("WelcomeContent");
    private final By searchInput = By.name("keyword");
    private final By searchButton = By.name("searchProducts");
    private final By mainContent = By.id("Content");

    @BeforeAll
    static void setupAll() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    static void tearDownAll() {
        if (driver != null) {
            driver.quit();
        }
    }

    @BeforeEach
    void setup() {
        driver.get(CATALOG_URL);
    }

    /**
     * Helper method to perform a login with the default credentials.
     */
    private void performLogin() {
        wait.until(ExpectedConditions.elementToBeClickable(signInLink)).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(usernameInput)).sendKeys(USERNAME);
        driver.findElement(passwordInput).sendKeys(PASSWORD);
        driver.findElement(loginButton).click();
        wait.until(ExpectedConditions.elementToBeClickable(signOutLink));
    }

    @Test
    @Order(1)
    @DisplayName("Should show an error message for invalid login credentials")
    void testInvalidLogin() {
        wait.until(ExpectedConditions.elementToBeClickable(signInLink)).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(usernameInput)).sendKeys(USERNAME);
        driver.findElement(passwordInput).sendKeys("wrongpassword");
        driver.findElement(loginButton).click();

        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".messages li")
        ));
        Assertions.assertTrue(
            errorMessage.getText().contains("Invalid username or password"),
            "Error message for invalid login is not correct."
        );
    }

    @Test
    @Order(2)
    @DisplayName("Should successfully log in and then log out")
    void testSuccessfulLoginAndLogout() {
        performLogin();
        Assertions.assertTrue(
            driver.findElement(welcomeContent).getText().contains("Welcome"),
            "Welcome message should be visible after login."
        );

        wait.until(ExpectedConditions.elementToBeClickable(signOutLink)).click();
        WebElement signIn = wait.until(ExpectedConditions.elementToBeClickable(signInLink));
        Assertions.assertTrue(signIn.isDisplayed(), "Sign In link should be visible after logout.");
    }

    @Test
    @Order(3)
    @DisplayName("Should search for a product, add it to the cart, and then remove it")
    void testProductSearchAndAddToCart() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(searchInput)).sendKeys("Goldfish");
        driver.findElement(searchButton).click();

        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Goldfish"))).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("EST-20"))).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Add to Cart"))).click();
        
        // Assert item is in cart
        WebElement cartTable = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("Cart")));
        Assertions.assertTrue(
            cartTable.getText().contains("Adult Male Goldfish"),
            "Cart should contain the added item."
        );

        // Cleanup: remove item from cart to ensure test independence
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Remove"))).click();
        wait.until(ExpectedConditions.textToBePresentInElementLocated(mainContent, "Your cart is empty."));
        Assertions.assertTrue(
             driver.findElement(mainContent).getText().contains("Your cart is empty."),
             "Cart should be empty after removing the item."
        );
    }
    
    @Test
    @Order(4)
    @DisplayName("Should navigate through product categories")
    void testCategoryNavigation() {
        // Use the main image map to navigate
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//div[@id='MainImageContent']//area[@alt='Dogs']"))).click();
        
        WebElement title = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h2[normalize-space()='Dogs']")));
        Assertions.assertEquals("Dogs", title.getText(), "Should be on the Dogs category page.");
        
        // Select a breed
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("K9-DL-01"))).click();
        WebElement breedTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h2[normalize-space()='Dalmation']")));
        Assertions.assertEquals("Dalmation", breedTitle.getText(), "Should be on the Dalmation product page.");
        
        // Assert a specific item is present
        Assertions.assertTrue(
            driver.findElement(mainContent).getText().contains("Spotless Male Puppy"),
            "Dalmation item list should contain 'Spotless Male Puppy'."
        );
    }

    @Test
    @Order(5)
    @DisplayName("Should complete a full checkout flow for an item")
    void testFullCheckoutFlow() {
        // --- Login ---
        performLogin();
        
        // --- Navigate and Add Item to Cart ---
        driver.findElement(By.xpath("//div[@id='SidebarContent']/a[contains(@href, 'CATS')]")).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("FL-DSH-01"))).click(); // Manx
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Add to Cart"))).click();
        
        // --- Proceed to Checkout ---
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Proceed to Checkout"))).click();
        
        // --- Confirm Shipping Details ---
        wait.until(ExpectedConditions.elementToBeClickable(By.name("newOrder"))).click();
        
        // --- Confirm Final Order ---
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Confirm"))).click();
        
        // --- Verify Success ---
        WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".messages li")
        ));
        Assertions.assertEquals(
            "Thank you, your order has been submitted.",
            successMessage.getText(),
            "Order confirmation message is not correct."
        );
        
        // --- Logout to clean up session state ---
        wait.until(ExpectedConditions.elementToBeClickable(signOutLink)).click();
    }
}