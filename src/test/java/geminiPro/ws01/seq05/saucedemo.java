package geminiPRO.ws01.seq05;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * JUnit 5 test suite for saucedemo.com using Selenium WebDriver with headless Firefox.
 * This suite covers login, product sorting, cart management, checkout, and external link validation.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SauceDemoE2ETest {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final String BASE_URL = "https://www.saucedemo.com/v1/index.html";
    private static final String INVENTORY_URL = "https://www.saucedemo.com/v1/inventory.html";
    private static final String USERNAME = "standard_user";
    private static final String PASSWORD = "secret_sauce";

    // --- Locators ---
    private final By usernameInput = By.id("user-name");
    private final By passwordInput = By.id("password");
    private final By loginButton = By.id("login-button");
    private final By errorHeader = By.cssSelector("h3[data-test='error']");
    private final By inventoryContainer = By.id("inventory_container");
    private final By productSortDropdown = By.className("product_sort_container");
    private final By inventoryItemName = By.className("inventory_item_name");
    private final By inventoryItemPrice = By.className("inventory_item_price");
    private final By addToCartButton = By.xpath("//button[starts-with(@class, 'btn_inventory')]");
    private final By shoppingCartBadge = By.className("shopping_cart_badge");
    private final By shoppingCartLink = By.className("shopping_cart_link");
    private final By burgerMenuButton = By.id("react-burger-menu-btn");
    private final By logoutLink = By.id("logout_sidebar_link");
    private final By aboutLink = By.id("about_sidebar_link");
    private final By allItemsLink = By.id("inventory_sidebar_link");
    private final By resetAppStateLink = By.id("reset_sidebar_link");
    private final By burgerMenuCloseButton = By.id("react-burger-cross-btn");

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
        driver.get(BASE_URL);
    }

    /**
     * Helper method to perform a standard login.
     */
    private void performLogin() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(usernameInput)).sendKeys(USERNAME);
        driver.findElement(passwordInput).sendKeys(PASSWORD);
        driver.findElement(loginButton).click();
        wait.until(ExpectedConditions.urlContains("inventory.html"));
    }

    /**
     * Helper method to open the burger menu and reset the application state.
     */
    private void resetAppState() {
        wait.until(ExpectedConditions.elementToBeClickable(burgerMenuButton)).click();
        wait.until(ExpectedConditions.elementToBeClickable(resetAppStateLink)).click();
        // Wait for the menu animation to finish before closing
        WebElement closeButton = wait.until(ExpectedConditions.elementToBeClickable(burgerMenuCloseButton));
        closeButton.click();
        // Assert menu is closed by checking for its absence or invisibility
        wait.until(ExpectedConditions.invisibilityOf(closeButton));
    }
    
    // -------------------- TEST CASES --------------------
    
    @Test
    @Order(1)
    @DisplayName("Test invalid login with wrong credentials")
    void testInvalidLogin() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(usernameInput)).sendKeys("wrong_user");
        driver.findElement(passwordInput).sendKeys("wrong_pass");
        driver.findElement(loginButton).click();

        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(errorHeader));
        Assertions.assertTrue(errorMessage.getText().contains("Username and password do not match"),
                "Error message for invalid login is not correct.");
    }

    @Test
    @Order(2)
    @DisplayName("Test successful login and subsequent logout")
    void testSuccessfulLoginAndLogout() {
        performLogin();
        Assertions.assertTrue(driver.getCurrentUrl().contains("inventory.html"), "URL should be the inventory page after login.");
        Assertions.assertTrue(driver.findElement(inventoryContainer).isDisplayed(), "Inventory container should be visible after login.");

        wait.until(ExpectedConditions.elementToBeClickable(burgerMenuButton)).click();
        wait.until(ExpectedConditions.elementToBeClickable(logoutLink)).click();

        wait.until(ExpectedConditions.urlToBe(BASE_URL));
        Assertions.assertTrue(driver.findElement(loginButton).isDisplayed(), "Login button should be visible after logout.");
    }
    
    @Test
    @Order(3)
    @DisplayName("Sort products by Name (Z to A) and verify order")
    void testProductSortByNameDescending() {
        performLogin();
        
        List<String> expectedNames = getDisplayedItemNames();
        expectedNames.sort(Collections.reverseOrder());
        
        new Select(driver.findElement(productSortDropdown)).selectByValue("za");
        
        List<String> actualNames = getDisplayedItemNames();
        Assertions.assertEquals(expectedNames, actualNames, "Products should be sorted by name Z to A.");
    }

    @Test
    @Order(4)
    @DisplayName("Sort products by Price (low to high) and verify order")
    void testProductSortByPriceAscending() {
        performLogin();
        
        List<Double> expectedPrices = getDisplayedItemPrices();
        Collections.sort(expectedPrices);

        new Select(driver.findElement(productSortDropdown)).selectByValue("lohi");

        List<Double> actualPrices = getDisplayedItemPrices();
        Assertions.assertEquals(expectedPrices, actualPrices, "Products should be sorted by price low to high.");
    }

    @Test
    @Order(5)
    @DisplayName("Sort products by Price (high to low) and verify order")
    void testProductSortByPriceDescending() {
        performLogin();
        
        List<Double> expectedPrices = getDisplayedItemPrices();
        expectedPrices.sort(Comparator.reverseOrder());

        new Select(driver.findElement(productSortDropdown)).selectByValue("hilo");

        List<Double> actualPrices = getDisplayedItemPrices();
        Assertions.assertEquals(expectedPrices, actualPrices, "Products should be sorted by price high to low.");
    }
    
    @Test
    @Order(6)
    @DisplayName("Add an item to cart and then remove it")
    void testAddToCartAndRemove() {
        performLogin();
        
        // Add item
        WebElement firstItemAddToCart = wait.until(ExpectedConditions.elementToBeClickable(addToCartButton));
        firstItemAddToCart.click();
        
        WebElement cartBadge = driver.findElement(shoppingCartBadge);
        Assertions.assertEquals("1", cartBadge.getText(), "Cart badge should show 1 after adding an item.");
        
        // Remove item
        WebElement removeButton = driver.findElement(By.xpath("//button[text()='REMOVE']"));
        removeButton.click();
        
        Assertions.assertFalse(isElementPresent(shoppingCartBadge), "Cart badge should disappear after removing the item.");
    }

    @Test
    @Order(7)
    @DisplayName("Execute a full checkout flow from start to finish")
    void testFullCheckoutFlow() {
        performLogin();
        
        // Add item to cart
        wait.until(ExpectedConditions.elementToBeClickable(addToCartButton)).click();
        
        // Go to cart
        driver.findElement(shoppingCartLink).click();
        wait.until(ExpectedConditions.urlContains("cart.html"));
        
        // Proceed to checkout
        wait.until(ExpectedConditions.elementToBeClickable(By.id("checkout"))).click();
        wait.until(ExpectedConditions.urlContains("checkout-step-one.html"));
        
        // Fill user information
        driver.findElement(By.id("first-name")).sendKeys("Gemini");
        driver.findElement(By.id("last-name")).sendKeys("Pro");
        driver.findElement(By.id("postal-code")).sendKeys("12345");
        driver.findElement(By.id("continue")).click();
        
        // Finalize purchase
        wait.until(ExpectedConditions.urlContains("checkout-step-two.html"));
        wait.until(ExpectedConditions.elementToBeClickable(By.id("finish"))).click();
        
        // Verify completion
        wait.until(ExpectedConditions.urlContains("checkout-complete.html"));
        WebElement thankYouHeader = driver.findElement(By.className("complete-header"));
        Assertions.assertEquals("THANK YOU FOR YOUR ORDER", thankYouHeader.getText(), "Checkout completion message is incorrect.");
    }
    
    @Test
    @Order(8)
    @DisplayName("Verify burger menu navigation and app state reset")
    void testBurgerMenuNavigationAndReset() {
        performLogin();
        
        // Add an item to test state reset
        wait.until(ExpectedConditions.elementToBeClickable(addToCartButton)).click();
        Assertions.assertTrue(isElementPresent(shoppingCartBadge), "Cart should have an item before reset.");
        
        // Reset state
        resetAppState();
        Assertions.assertFalse(isElementPresent(shoppingCartBadge), "Cart should be empty after app state reset.");
        
        // Navigate to 'All Items'
        wait.until(ExpectedConditions.elementToBeClickable(burgerMenuButton)).click();
        wait.until(ExpectedConditions.elementToBeClickable(allItemsLink)).click();
        Assertions.assertEquals(INVENTORY_URL, driver.getCurrentUrl(), "All Items link should lead to the inventory page.");
    }
    
    @Test
    @Order(9)
    @DisplayName("Verify the external 'About' link opens correctly")
    void testAboutLinkExternal() {
        performLogin();
        String originalWindow = driver.getWindowHandle();
        
        wait.until(ExpectedConditions.elementToBeClickable(burgerMenuButton)).click();
        wait.until(ExpectedConditions.elementToBeClickable(aboutLink)).click();
        
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        
        Set<String> allWindows = driver.getWindowHandles();
        allWindows.remove(originalWindow);
        String newWindow = new ArrayList<>(allWindows).get(0);
        
        driver.switchTo().window(newWindow);
        wait.until(ExpectedConditions.not(ExpectedConditions.urlContains("saucedemo.com")));
        
        Assertions.assertTrue(driver.getCurrentUrl().contains("saucelabs.com"), "New window URL should contain 'saucelabs.com'.");
        
        driver.close();
        driver.switchTo().window(originalWindow);
        
        Assertions.assertEquals(INVENTORY_URL, driver.getCurrentUrl(), "Driver should be back on the original inventory page.");
    }

    @ParameterizedTest(name = "Footer Link Test: {1}")
    @CsvSource({
        "a[href*='twitter.com'], twitter.com",
        "a[href*='facebook.com'], facebook.com",
        "a[href*='linkedin.com'], linkedin.com"
    })
    @Order(10)
    @DisplayName("Verify footer social media links")
    void testFooterSocialLinks(String selector, String expectedDomain) {
        performLogin();
        String originalWindow = driver.getWindowHandle();

        WebElement socialLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(selector)));
        socialLink.click();

        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        Set<String> allWindows = driver.getWindowHandles();
        allWindows.remove(originalWindow);
        driver.switchTo().window(new ArrayList<>(allWindows).get(0));

        wait.until(ExpectedConditions.not(ExpectedConditions.urlContains("saucedemo.com")));

        Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain),
                "The URL of the new tab should contain '" + expectedDomain + "'.");

        driver.close();
        driver.switchTo().window(originalWindow);
        Assertions.assertEquals(INVENTORY_URL, driver.getCurrentUrl(), "Should return to the inventory page.");
    }

    // -------------------- HELPER METHODS --------------------

    /**
     * Retrieves the text of all displayed inventory item names.
     * @return A list of product names as Strings.
     */
    private List<String> getDisplayedItemNames() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(inventoryItemName));
        List<WebElement> nameElements = driver.findElements(inventoryItemName);
        return nameElements.stream().map(WebElement::getText).collect(Collectors.toList());
    }

    /**
     * Retrieves the price of all displayed inventory items.
     * @return A list of product prices as Doubles.
     */
    private List<Double> getDisplayedItemPrices() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(inventoryItemPrice));
        List<WebElement> priceElements = driver.findElements(inventoryItemPrice);
        return priceElements.stream()
                .map(el -> Double.parseDouble(el.getText().replace("$", "")))
                .collect(Collectors.toList());
    }
    
    /**
     * Checks if an element is present on the page without throwing an exception.
     * @param locator The By locator of the element to check.
     * @return true if the element is found, false otherwise.
     */
    private boolean isElementPresent(By locator) {
        try {
            driver.findElement(locator);
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }
}