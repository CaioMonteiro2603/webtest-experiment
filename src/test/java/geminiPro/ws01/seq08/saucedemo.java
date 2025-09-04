package geminiPRO.ws01.seq08;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SauceDemoComprehensiveTest {

    private static final String BASE_URL = "https://www.saucedemo.com/v1/index.html";
    private static final String VALID_USER = "standard_user";
    private static final String LOCKED_OUT_USER = "locked_out_user";
    private static final String PASSWORD = "secret_sauce";
    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    private static WebDriver driver;
    private static WebDriverWait wait;

    // Locators
    private final By usernameInput = By.id("user-name");
    private final By passwordInput = By.id("password");
    private final By loginButton = By.id("login-button");
    private final By inventoryContainer = By.id("inventory_container");
    private final By burgerMenuButton = By.id("react-burger-menu-btn");
    private final By logoutLink = By.id("logout_sidebar_link");
    private final By shoppingCartBadge = By.cssSelector(".shopping_cart_badge");
    private final By sortDropdown = By.cssSelector(".product_sort_container");
    private final By inventoryItemName = By.cssSelector(".inventory_item_name");
    private final By inventoryItemPrice = By.cssSelector(".inventory_item_price");

    @BeforeAll
    static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, TIMEOUT);
    }

    @AfterAll
    static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @BeforeEach
    void navigateToLogin() {
        driver.get(BASE_URL);
    }

    private void login(String username, String password) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(usernameInput)).sendKeys(username);
        driver.findElement(passwordInput).sendKeys(password);
        driver.findElement(loginButton).click();
    }
    
    private void resetAppState() {
        wait.until(ExpectedConditions.elementToBeClickable(burgerMenuButton)).click();
        WebElement resetLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link")));
        resetLink.click();
        WebElement closeMenuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-cross-btn")));
        closeMenuButton.click();
    }

    private void assertExternalLink(By locator, String expectedDomain) {
        String originalWindow = driver.getWindowHandle();
        wait.until(ExpectedConditions.elementToBeClickable(locator)).click();

        wait.until(ExpectedConditions.numberOfWindowsToBe(2));

        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.contentEquals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        Assertions.assertTrue(wait.until(ExpectedConditions.urlContains(expectedDomain)), "URL should contain " + expectedDomain);
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(1)
    @DisplayName("Test Login Page Title and Elements")
    void testLoginPageTitleAndElements() {
        Assertions.assertEquals("Swag Labs", driver.getTitle(), "Page title should be 'Swag Labs'");
        Assertions.assertTrue(driver.findElement(usernameInput).isDisplayed(), "Username input should be visible");
        Assertions.assertTrue(driver.findElement(passwordInput).isDisplayed(), "Password input should be visible");
    }

    @Test
    @Order(2)
    @DisplayName("Test Login with Invalid Credentials")
    void testLoginWithInvalidCredentials() {
        login(VALID_USER, "wrong_password");
        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h3[data-test='error']")));
        Assertions.assertTrue(errorMessage.getText().contains("Username and password do not match"), "Error message should be displayed for invalid credentials");
    }

    @Test
    @Order(3)
    @DisplayName("Test Login with Locked Out User")
    void testLoginWithLockedOutUser() {
        login(LOCKED_OUT_USER, PASSWORD);
        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h3[data-test='error']")));
        Assertions.assertTrue(errorMessage.getText().contains("Sorry, this user has been locked out"), "Error message should be displayed for locked out user");
    }

    @Test
    @Order(4)
    @DisplayName("Test Successful Login and Logout")
    void testSuccessfulLoginAndLogout() {
        // Login
        login(VALID_USER, PASSWORD);
        wait.until(ExpectedConditions.urlContains("/inventory.html"));
        Assertions.assertTrue(driver.findElement(inventoryContainer).isDisplayed(), "Inventory container should be visible after login");

        // Logout
        driver.findElement(burgerMenuButton).click();
        wait.until(ExpectedConditions.elementToBeClickable(logoutLink)).click();
        wait.until(ExpectedConditions.urlContains("index.html"));
        Assertions.assertTrue(driver.findElement(loginButton).isDisplayed(), "Login button should be visible after logout");
    }
    
    @Test
    @Order(5)
    @DisplayName("Test Product Sorting: Name (A to Z)")
    void testProductSortByNameAscending() {
        login(VALID_USER, PASSWORD);
        wait.until(ExpectedConditions.visibilityOfElementLocated(inventoryContainer));

        List<String> originalNames = driver.findElements(inventoryItemName)
                                           .stream()
                                           .map(WebElement::getText)
                                           .collect(Collectors.toList());
        
        List<String> sortedNames = new ArrayList<>(originalNames);
        Collections.sort(sortedNames);

        Assertions.assertEquals(sortedNames, originalNames, "Products should be sorted by name A-Z by default");
    }
    
    @Test
    @Order(6)
    @DisplayName("Test Product Sorting: Name (Z to A)")
    void testProductSortByNameDescending() {
        login(VALID_USER, PASSWORD);
        wait.until(ExpectedConditions.visibilityOfElementLocated(inventoryContainer));
        
        new Select(driver.findElement(sortDropdown)).selectByValue("za");

        List<String> productNames = wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(inventoryItemName, 0))
                                       .stream()
                                       .map(WebElement::getText)
                                       .collect(Collectors.toList());
        
        List<String> expectedOrder = new ArrayList<>(productNames);
        expectedOrder.sort(Collections.reverseOrder());

        Assertions.assertEquals(expectedOrder, productNames, "Products should be sorted by name Z-A");
    }

    @Test
    @Order(7)
    @DisplayName("Test Product Sorting: Price (low to high)")
    void testProductSortByPriceAscending() {
        login(VALID_USER, PASSWORD);
        wait.until(ExpectedConditions.visibilityOfElementLocated(inventoryContainer));

        new Select(driver.findElement(sortDropdown)).selectByValue("lohi");

        List<Double> productPrices = wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(inventoryItemPrice, 0))
                                          .stream()
                                          .map(p -> Double.parseDouble(p.getText().replace("$", "")))
                                          .collect(Collectors.toList());
        
        List<Double> sortedPrices = new ArrayList<>(productPrices);
        Collections.sort(sortedPrices);

        Assertions.assertEquals(sortedPrices, productPrices, "Products should be sorted by price low to high");
    }

    @Test
    @Order(8)
    @DisplayName("Test Product Sorting: Price (high to low)")
    void testProductSortByPriceDescending() {
        login(VALID_USER, PASSWORD);
        wait.until(ExpectedConditions.visibilityOfElementLocated(inventoryContainer));

        new Select(driver.findElement(sortDropdown)).selectByValue("hilo");

        List<Double> productPrices = wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(inventoryItemPrice, 0))
                                          .stream()
                                          .map(p -> Double.parseDouble(p.getText().replace("$", "")))
                                          .collect(Collectors.toList());
        
        List<Double> sortedPrices = new ArrayList<>(productPrices);
        sortedPrices.sort(Collections.reverseOrder());
        
        Assertions.assertEquals(sortedPrices, productPrices, "Products should be sorted by price high to low");
    }

    @Test
    @Order(9)
    @DisplayName("Test Adding and Removing Item from Cart")
    void testAddAndRemoveItemFromCart() {
        login(VALID_USER, PASSWORD);
        wait.until(ExpectedConditions.visibilityOfElementLocated(inventoryContainer));
        resetAppState(); // Ensure clean state

        // Add item
        WebElement addToCartButton = driver.findElement(By.cssSelector(".btn_inventory"));
        addToCartButton.click();
        
        WebElement badge = wait.until(ExpectedConditions.visibilityOfElementLocated(shoppingCartBadge));
        Assertions.assertEquals("1", badge.getText(), "Cart badge should display '1' after adding one item");

        // Remove item
        WebElement removeButton = driver.findElement(By.cssSelector(".btn_secondary")); // The button text changes
        removeButton.click();
        
        Assertions.assertTrue(wait.until(ExpectedConditions.invisibilityOfElementLocated(shoppingCartBadge)), "Cart badge should disappear after removing the item");
    }
    
    @Test
    @Order(10)
    @DisplayName("Test App State Reset Functionality")
    void testResetAppState() {
        login(VALID_USER, PASSWORD);
        wait.until(ExpectedConditions.visibilityOfElementLocated(inventoryContainer));

        // Add an item to create state
        driver.findElement(By.cssSelector(".btn_inventory")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(shoppingCartBadge));
        Assertions.assertEquals("1", driver.findElement(shoppingCartBadge).getText(), "Cart should have 1 item before reset");
        
        // Reset state
        resetAppState();

        // Verify state is reset
        Assertions.assertTrue(driver.findElements(shoppingCartBadge).isEmpty(), "Cart badge should be gone after resetting app state");
    }

    @Test
    @Order(11)
    @DisplayName("Test Full Checkout Flow")
    void testFullCheckoutFlow() {
        login(VALID_USER, PASSWORD);
        wait.until(ExpectedConditions.visibilityOfElementLocated(inventoryContainer));
        resetAppState();
        
        // 1. Add item and go to cart
        driver.findElement(By.id("add-to-cart-sauce-labs-backpack")).click();
        driver.findElement(By.className("shopping_cart_link")).click();
        wait.until(ExpectedConditions.urlContains("/cart.html"));

        // 2. Start checkout
        driver.findElement(By.id("checkout")).click();
        wait.until(ExpectedConditions.urlContains("/checkout-step-one.html"));

        // 3. Fill user info
        driver.findElement(By.id("first-name")).sendKeys("Gemini");
        driver.findElement(By.id("last-name")).sendKeys("Pro");
        driver.findElement(By.id("postal-code")).sendKeys("12345");
        driver.findElement(By.id("continue")).click();
        wait.until(ExpectedConditions.urlContains("/checkout-step-two.html"));

        // 4. Finish checkout
        driver.findElement(By.id("finish")).click();
        wait.until(ExpectedConditions.urlContains("/checkout-complete.html"));

        // 5. Assert completion
        WebElement confirmationHeader = driver.findElement(By.className("complete-header"));
        Assertions.assertEquals("THANK YOU FOR YOUR ORDER", confirmationHeader.getText(), "Confirmation message should be displayed");

        // 6. Return home and verify cart is empty
        driver.findElement(By.id("back-to-products")).click();
        wait.until(ExpectedConditions.urlContains("/inventory.html"));
        Assertions.assertTrue(driver.findElements(shoppingCartBadge).isEmpty(), "Cart should be empty after checkout completion");
    }

    @Test
    @Order(12)
    @DisplayName("Test Menu 'About' External Link")
    void testBurgerMenuAboutLink() {
        login(VALID_USER, PASSWORD);
        wait.until(ExpectedConditions.visibilityOfElementLocated(inventoryContainer));
        
        driver.findElement(burgerMenuButton).click();
        assertExternalLink(By.id("about_sidebar_link"), "saucelabs.com");
    }

    @Test
    @Order(13)
    @DisplayName("Test Footer Twitter External Link")
    void testFooterTwitterLink() {
        login(VALID_USER, PASSWORD);
        wait.until(ExpectedConditions.visibilityOfElementLocated(inventoryContainer));
        assertExternalLink(By.linkText("Twitter"), "twitter.com");
    }

    @Test
    @Order(14)
    @DisplayName("Test Footer Facebook External Link")
    void testFooterFacebookLink() {
        login(VALID_USER, PASSWORD);
        wait.until(ExpectedConditions.visibilityOfElementLocated(inventoryContainer));
        assertExternalLink(By.linkText("Facebook"), "facebook.com");
    }

    @Test
    @Order(15)
    @DisplayName("Test Footer LinkedIn External Link")
    void testFooterLinkedInLink() {
        login(VALID_USER, PASSWORD);
        wait.until(ExpectedConditions.visibilityOfElementLocated(inventoryContainer));
        assertExternalLink(By.linkText("LinkedIn"), "linkedin.com");
    }
}