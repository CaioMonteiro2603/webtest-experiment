package geminiPRO.ws01.seq02;

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

/**
 * A complete JUnit 5 test suite for the Sauce Demo website using Selenium WebDriver
 * with Firefox running in headless mode.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SauceDemoCompleteTest {

    // Constants for test data and configuration
    private static final String BASE_URL = "https://www.saucedemo.com/v1/index.html";
    private static final String VALID_USER = "standard_user";
    private static final String LOCKED_USER = "locked_out_user";
    private static final String PASSWORD = "secret_sauce";
    private static final Duration WAIT_TIMEOUT = Duration.ofSeconds(10);

    // WebDriver and WebDriverWait instances are static to be shared across all tests
    private static WebDriver driver;
    private static WebDriverWait wait;

    // Locators for commonly used elements
    private final By usernameInput = By.id("user-name");
    private final By passwordInput = By.id("password");
    private final By loginButton = By.id("login-button");
    private final By inventoryContainer = By.id("inventory_container");
    private final By burgerMenuButton = By.id("react-burger-menu-btn");
    private final By logoutLink = By.id("logout_sidebar_link");
    private final By resetStateLink = By.id("reset_sidebar_link");
    private final By aboutLink = By.id("about_sidebar_link");
    private final By errorContainer = By.cssSelector("h3[data-test='error']");
    private final By sortDropdown = By.className("product_sort_container");
    private final By inventoryItemName = By.className("inventory_item_name");
    private final By inventoryItemPrice = By.className("inventory_item_price");
    private final By cartBadge = By.className("shopping_cart_badge");
    private final By cartLink = By.id("shopping_cart_container");
    private final By checkoutButton = By.id("checkout");
    private final By firstNameInput = By.id("first-name");
    private final By lastNameInput = By.id("last-name");
    private final By postalCodeInput = By.id("postal-code");
    private final By continueButton = By.id("continue");
    private final By finishButton = By.id("finish");
    private final By completeHeader = By.className("complete-header");
    private final By burgerMenuCloseButton = By.className("bm-cross-button");

    @BeforeAll
    static void setup() {
        // As per requirements, initialize Firefox in headless mode via arguments
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, WAIT_TIMEOUT);
    }

    @AfterAll
    static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @BeforeEach
    void navigateToLogin() {
        // Start each test from a clean slate at the login page
        driver.get(BASE_URL);
        Assertions.assertTrue(driver.getCurrentUrl().endsWith("index.html"), "Should be on the login page.");
    }

    // --- LOGIN AND LOGOUT TESTS ---

    @Test
    @Order(1)
    @DisplayName("Test login with invalid credentials should show an error")
    void testInvalidLogin() {
        performLogin(VALID_USER, "wrong_password");
        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(errorContainer));
        Assertions.assertTrue(error.getText().contains("Username and password do not match"),
                "Error message for invalid credentials was not correct.");
    }

    @Test
    @Order(2)
    @DisplayName("Test login with a locked-out user should show an error")
    void testLockedOutUserLogin() {
        performLogin(LOCKED_USER, PASSWORD);
        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(errorContainer));
        Assertions.assertTrue(error.getText().contains("Sorry, this user has been locked out."),
                "Error message for locked out user was not correct.");
    }

    @Test
    @Order(3)
    @DisplayName("Test successful login and subsequent logout")
    void testSuccessfulLoginAndLogout() {
        performLogin(VALID_USER, PASSWORD);
        wait.until(ExpectedConditions.urlContains("/inventory.html"));
        Assertions.assertTrue(driver.findElement(inventoryContainer).isDisplayed(), "Inventory should be visible after login.");

        wait.until(ExpectedConditions.elementToBeClickable(burgerMenuButton)).click();
        wait.until(ExpectedConditions.elementToBeClickable(logoutLink)).click();

        wait.until(ExpectedConditions.urlToBe(BASE_URL));
        Assertions.assertTrue(driver.findElement(loginButton).isDisplayed(), "Login button should be visible after logout.");
    }

    // --- INVENTORY AND SORTING TESTS ---

    @Test
    @Order(4)
    @DisplayName("Test product sorting by Name (A to Z), which is the default")
    void testProductSortByNameAscending() {
        performLogin(VALID_USER, PASSWORD);
        wait.until(ExpectedConditions.visibilityOfElementLocated(inventoryContainer));

        List<String> originalNames = getDisplayedItemNames();
        List<String> sortedNames = new ArrayList<>(originalNames);
        Collections.sort(sortedNames);

        Assertions.assertEquals(sortedNames, originalNames, "Items should be sorted by name A-Z by default.");
    }

    @Test
    @Order(5)
    @DisplayName("Test product sorting by Name (Z to A)")
    void testProductSortByNameDescending() {
        performLogin(VALID_USER, PASSWORD);
        wait.until(ExpectedConditions.visibilityOfElementLocated(inventoryContainer));

        new Select(driver.findElement(sortDropdown)).selectByValue("za");

        List<String> actualNames = getDisplayedItemNames();
        List<String> expectedNames = new ArrayList<>(actualNames);
        expectedNames.sort(Collections.reverseOrder());

        Assertions.assertEquals(expectedNames, actualNames, "Items should be sorted by name Z-A.");
    }

    @Test
    @Order(6)
    @DisplayName("Test product sorting by Price (low to high)")
    void testProductSortByPriceAscending() {
        performLogin(VALID_USER, PASSWORD);
        wait.until(ExpectedConditions.visibilityOfElementLocated(inventoryContainer));

        new Select(driver.findElement(sortDropdown)).selectByValue("lohi");

        List<Double> actualPrices = getDisplayedItemPrices();
        List<Double> expectedPrices = new ArrayList<>(actualPrices);
        Collections.sort(expectedPrices);

        Assertions.assertEquals(expectedPrices, actualPrices, "Items should be sorted by price low to high.");
    }

    @Test
    @Order(7)
    @DisplayName("Test product sorting by Price (high to low)")
    void testProductSortByPriceDescending() {
        performLogin(VALID_USER, PASSWORD);
        wait.until(ExpectedConditions.visibilityOfElementLocated(inventoryContainer));

        new Select(driver.findElement(sortDropdown)).selectByValue("hilo");

        List<Double> actualPrices = getDisplayedItemPrices();
        List<Double> expectedPrices = new ArrayList<>(actualPrices);
        expectedPrices.sort(Collections.reverseOrder());

        Assertions.assertEquals(expectedPrices, actualPrices, "Items should be sorted by price high to low.");
    }

    // --- CART FUNCTIONALITY TESTS ---

    @Test
    @Order(8)
    @DisplayName("Add items to cart and verify cart badge increments")
    void testAddItemToCart() {
        performLogin(VALID_USER, PASSWORD);
        wait.until(ExpectedConditions.visibilityOfElementLocated(inventoryContainer));

        // Add first item
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("(//button[text()='Add to cart'])[1]"))).click();
        Assertions.assertEquals("1", driver.findElement(cartBadge).getText(), "Cart badge should show 1.");

        // Add second item
        driver.findElement(By.xpath("(//button[text()='Add to cart'])[1]")).click();
        Assertions.assertEquals("2", driver.findElement(cartBadge).getText(), "Cart badge should show 2.");

        // Cleanup for next tests
        resetAppState();
    }

    @Test
    @Order(9)
    @DisplayName("Remove item from cart and verify badge update")
    void testRemoveItemFromCart() {
        performLogin(VALID_USER, PASSWORD);
        wait.until(ExpectedConditions.visibilityOfElementLocated(inventoryContainer));

        // Add an item to create a state
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("(//button[text()='Add to cart'])[1]"))).click();
        Assertions.assertTrue(driver.findElements(cartBadge).size() > 0, "Cart badge should be visible.");

        // Remove the item from the inventory page
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("(//button[text()='Remove'])[1]"))).click();
        Assertions.assertTrue(driver.findElements(cartBadge).isEmpty(), "Cart badge should disappear after removing item.");
    }

    // --- MENU AND EXTERNAL LINK TESTS ---

    @Test
    @Order(10)
    @DisplayName("Verify the 'About' external link opens a new tab to saucelabs.com")
    void testMenuAboutLink() {
        performLogin(VALID_USER, PASSWORD);
        testExternalLinkInteraction(burgerMenuButton, aboutLink, "saucelabs.com");
    }

    @Test
    @Order(11)
    @DisplayName("Verify 'Reset App State' functionality clears the cart")
    void testResetAppState() {
        performLogin(VALID_USER, PASSWORD);
        wait.until(ExpectedConditions.visibilityOfElementLocated(inventoryContainer));

        // Add item to cart to create a state to reset
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("(//button[text()='Add to cart'])[1]"))).click();
        Assertions.assertEquals("1", driver.findElement(cartBadge).getText(), "Cart should have 1 item before reset.");

        // Perform reset and verify outcome
        resetAppState();
        Assertions.assertTrue(driver.findElements(cartBadge).isEmpty(), "Cart badge should be gone after resetting app state.");
    }

    // --- FULL END-TO-END CHECKOUT FLOW ---

    @Test
    @Order(12)
    @DisplayName("Test the complete checkout process for a single item")
    void testFullCheckoutFlow() {
        performLogin(VALID_USER, PASSWORD);
        wait.until(ExpectedConditions.visibilityOfElementLocated(inventoryContainer));

        // 1. Add item to cart
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//div[text()='Sauce Labs Backpack']/ancestor::div[@class='inventory_item']//button"))).click();

        // 2. Go to cart and proceed to checkout
        driver.findElement(cartLink).click();
        wait.until(ExpectedConditions.urlContains("/cart.html"));
        wait.until(ExpectedConditions.elementToBeClickable(checkoutButton)).click();

        // 3. Fill user information
        wait.until(ExpectedConditions.urlContains("/checkout-step-one.html"));
        driver.findElement(firstNameInput).sendKeys("Gemini");
        driver.findElement(lastNameInput).sendKeys("Pro");
        driver.findElement(postalCodeInput).sendKeys("12345");
        driver.findElement(continueButton).click();

        // 4. Verify overview and finish transaction
        wait.until(ExpectedConditions.urlContains("/checkout-step-two.html"));
        Assertions.assertTrue(driver.getPageSource().contains("Sauce Labs Backpack"), "Backpack should be in the overview.");
        wait.until(ExpectedConditions.elementToBeClickable(finishButton)).click();

        // 5. Assert completion
        wait.until(ExpectedConditions.urlContains("/checkout-complete.html"));
        WebElement header = driver.findElement(completeHeader);
        Assertions.assertEquals("THANK YOU FOR YOUR ORDER", header.getText(), "Completion message should be displayed.");
    }

    // --- FOOTER EXTERNAL LINK TESTS ---

    @Test
    @Order(13)
    @DisplayName("Verify footer Twitter link")
    void testFooterTwitterLink() {
        performLogin(VALID_USER, PASSWORD);
        testExternalLinkInteraction(null, By.linkText("Twitter"), "twitter.com");
    }

    @Test
    @Order(14)
    @DisplayName("Verify footer Facebook link")
    void testFooterFacebookLink() {
        performLogin(VALID_USER, PASSWORD);
        testExternalLinkInteraction(null, By.linkText("Facebook"), "facebook.com");
    }

    @Test
    @Order(15)
    @DisplayName("Verify footer LinkedIn link")
    void testFooterLinkedInLink() {
        performLogin(VALID_USER, PASSWORD);
        testExternalLinkInteraction(null, By.linkText("LinkedIn"), "linkedin.com");
    }

    // --- HELPER METHODS ---

    /** A reusable helper to perform login. */
    private void performLogin(String username, String password) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(usernameInput)).sendKeys(username);
        driver.findElement(passwordInput).sendKeys(password);
        driver.findElement(loginButton).click();
    }

    /** Resets the application state via the burger menu. */
    private void resetAppState() {
        wait.until(ExpectedConditions.elementToBeClickable(burgerMenuButton)).click();
        wait.until(ExpectedConditions.elementToBeClickable(resetStateLink)).click();
        wait.until(ExpectedConditions.elementToBeClickable(burgerMenuCloseButton)).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.className("bm-menu-wrap"))); // Wait for menu to close
    }

    /** Generic helper to test an interaction that opens and switches to a new tab. */
    private void testExternalLinkInteraction(By openerLocator, By linkLocator, String expectedDomain) {
        String originalWindow = driver.getWindowHandle();

        if (openerLocator != null) {
            wait.until(ExpectedConditions.elementToBeClickable(openerLocator)).click();
        }
        
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(linkLocator));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", link);
        link.click();
        
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.contentEquals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        wait.until(ExpectedConditions.not(ExpectedConditions.urlToBe("about:blank")));
        Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain), "URL should contain " + expectedDomain);

        driver.close();
        driver.switchTo().window(originalWindow);
        Assertions.assertTrue(driver.getCurrentUrl().contains("/inventory.html"), "Should return to the inventory page.");
    }
    
    /** Retrieves the text of all displayed inventory item names. */
    private List<String> getDisplayedItemNames() {
        return driver.findElements(inventoryItemName)
                     .stream()
                     .map(WebElement::getText)
                     .collect(Collectors.toList());
    }

    /** Retrieves and parses the prices of all displayed inventory items. */
    private List<Double> getDisplayedItemPrices() {
        return driver.findElements(inventoryItemPrice)
                     .stream()
                     .map(e -> Double.parseDouble(e.getText().replace("$", "")))
                     .collect(Collectors.toList());
    }
}package geminiPro.ws01.seq01;

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

/**
 * A comprehensive JUnit 5 test suite for the Sauce Demo website using Selenium WebDriver with Firefox in headless mode.
 * This suite covers login, product sorting, cart management, menu actions, external links, and the full checkout process.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SauceDemoComprehensiveTest {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final String BASE_URL = "https://www.saucedemo.com/v1/index.html";
    private static final String USERNAME = "standard_user";
    private static final String PASSWORD = "secret_sauce";
    private static final String LOCKED_OUT_USERNAME = "locked_out_user";

    // --- Locators ---
    private static final By USERNAME_INPUT = By.id("user-name");
    private static final By PASSWORD_INPUT = By.id("password");
    private static final By LOGIN_BUTTON = By.id("login-button");
    private static final By ERROR_MESSAGE_CONTAINER = By.cssSelector("h3[data-test='error']");
    private static final By INVENTORY_CONTAINER = By.id("inventory_container");
    private static final By MENU_BUTTON = By.id("react-burger-menu-btn");
    private static final By LOGOUT_LINK = By.id("logout_sidebar_link");
    private static final By RESET_APP_STATE_LINK = By.id("reset_sidebar_link");
    private static final By ABOUT_LINK = By.id("about_sidebar_link");
    private static final By SHOPPING_CART_BADGE = By.className("shopping_cart_badge");
    private static final By SHOPPING_CART_LINK = By.id("shopping_cart_container");
    private static final By SORT_DROPDOWN = By.className("product_sort_container");
    private static final By INVENTORY_ITEM_NAME = By.className("inventory_item_name");
    private static final By INVENTORY_ITEM_PRICE = By.className("inventory_item_price");

    @BeforeAll
    static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless"); // Use arguments for headless mode as required
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    // --- Helper Methods ---

    /**
     * Logs into the application. Navigates to the base URL before performing login.
     * @param username The username to use for login.
     * @param password The password to use for login.
     */
    private void login(String username, String password) {
        // Navigate to a clean base state to avoid issues with previous test states
        driver.get(BASE_URL.replace("index.html", ""));
        wait.until(ExpectedConditions.visibilityOfElementLocated(USERNAME_INPUT)).sendKeys(username);
        driver.findElement(PASSWORD_INPUT).sendKeys(password);
        driver.findElement(LOGIN_BUTTON).click();
    }

    /**
     * Resets the application state using the burger menu option.
     * This ensures tests that modify data (like the cart) can start from a known clean state.
     */
    private void resetAppState() {
        wait.until(ExpectedConditions.elementToBeClickable(MENU_BUTTON)).click();
        wait.until(ExpectedConditions.elementToBeClickable(RESET_APP_STATE_LINK)).click();
        // Close the menu by clicking the 'X' button to return to a usable state
        driver.findElement(By.className("bm-cross-button")).click();
        // Wait for the menu to be fully hidden to prevent it from obscuring other elements
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.className("bm-menu-wrap")));
    }

    /**
     * Validates that a link opens a new tab/window to an external domain.
     * It switches to the new window, asserts the URL, closes it, and switches back.
     * @param locator The locator for the link element.
     * @param expectedDomain The domain expected in the new window's URL.
     */
    private void validateExternalLink(By locator, String expectedDomain) {
        String originalWindow = driver.getWindowHandle();
        wait.until(ExpectedConditions.elementToBeClickable(locator)).click();

        // Wait for the new window or tab to open
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));

        // Switch to the new window
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.contentEquals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        // Assert the URL of the new window contains the expected domain
        wait.until(ExpectedConditions.urlContains(expectedDomain));
        Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain),
            "URL should contain '" + expectedDomain + "'. Actual: " + driver.getCurrentUrl());

        // Close the new window and switch back to the original
        driver.close();
        driver.switchTo().window(originalWindow);
        Assertions.assertEquals(1, driver.getWindowHandles().size(), "Should have returned to the original window.");
    }


    // --- Test Cases ---

    @Test
    @Order(1)
    @DisplayName("Login should fail for a locked out user")
    void testLockedOutUserLogin() {
        login(LOCKED_OUT_USERNAME, PASSWORD);
        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(ERROR_MESSAGE_CONTAINER));
        Assertions.assertTrue(errorMessage.getText().contains("Sorry, this user has been locked out."),
            "Error message for locked out user was not displayed or incorrect.");
    }

    @Test
    @Order(2)
    @DisplayName("Login should fail for invalid credentials")
    void testInvalidCredentialsLogin() {
        login(USERNAME, "wrong_password");
        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(ERROR_MESSAGE_CONTAINER));
        Assertions.assertTrue(errorMessage.getText().contains("Username and password do not match"),
            "Error message for invalid credentials was not displayed or incorrect.");
    }

    @Test
    @Order(3)
    @DisplayName("Login should succeed and then logout should return to login page")
    void testSuccessfulLoginAndLogout() {
        login(USERNAME, PASSWORD);
        wait.until(ExpectedConditions.urlContains("/inventory.html"));
        Assertions.assertTrue(driver.getCurrentUrl().endsWith("/inventory.html"), "Login failed or redirected incorrectly.");
        Assertions.assertTrue(driver.findElement(INVENTORY_CONTAINER).isDisplayed(), "Inventory container not found after login.");

        // Test logout functionality
        wait.until(ExpectedConditions.elementToBeClickable(MENU_BUTTON)).click();
        wait.until(ExpectedConditions.elementToBeClickable(LOGOUT_LINK)).click();
        wait.until(ExpectedConditions.urlToBe(BASE_URL));
        Assertions.assertTrue(driver.getCurrentUrl().endsWith("/index.html"), "Logout failed to return to the login page.");
        Assertions.assertTrue(driver.findElement(LOGIN_BUTTON).isDisplayed(), "Login button not found after logout.");
    }

    @Test
    @Order(4)
    @DisplayName("User should be able to add and remove items from the cart")
    void testAddAndRemoveItems() {
        login(USERNAME, PASSWORD);
        wait.until(ExpectedConditions.visibilityOfElementLocated(INVENTORY_CONTAINER));

        resetAppState();
        Assertions.assertEquals(0, driver.findElements(SHOPPING_CART_BADGE).size(), "Cart should be empty after reset.");

        // Add Sauce Labs Backpack
        driver.findElement(By.xpath("//div[text()='Sauce Labs Backpack']/ancestor::div[@class='inventory_item']//button")).click();
        Assertions.assertEquals("1", driver.findElement(SHOPPING_CART_BADGE).getText(), "Cart badge should display 1.");

        // Add Sauce Labs Bike Light
        driver.findElement(By.xpath("//div[text()='Sauce Labs Bike Light']/ancestor::div[@class='inventory_item']//button")).click();
        Assertions.assertEquals("2", driver.findElement(SHOPPING_CART_BADGE).getText(), "Cart badge should display 2.");

        // Remove Sauce Labs Backpack
        driver.findElement(By.xpath("//div[text()='Sauce Labs Backpack']/ancestor::div[@class='inventory_item']//button[text()='REMOVE']")).click();
        Assertions.assertEquals("1", driver.findElement(SHOPPING_CART_BADGE).getText(), "Cart badge should display 1 after removal.");

        resetAppState(); // Cleanup for next test
        Assertions.assertEquals(0, driver.findElements(SHOPPING_CART_BADGE).size(), "Cart should be empty after final reset.");
    }

    @Test
    @Order(5)
    @DisplayName("Product sort functionality should work correctly")
    void testProductSorting() {
        login(USERNAME, PASSWORD);
        wait.until(ExpectedConditions.visibilityOfElementLocated(INVENTORY_CONTAINER));
        Select sortDropdown = new Select(driver.findElement(SORT_DROPDOWN));

        // Test Name (Z to A)
        sortDropdown.selectByValue("za");
        List<String> namesZA = driver.findElements(INVENTORY_ITEM_NAME).stream().map(WebElement::getText).collect(Collectors.toList());
        List<String> sortedNamesZA = new ArrayList<>(namesZA);
        sortedNamesZA.sort(Collections.reverseOrder());
        Assertions.assertEquals(sortedNamesZA, namesZA, "Products are not sorted by Name (Z to A).");

        // Test Price (low to high)
        sortDropdown.selectByValue("lohi");
        List<Double> pricesLoHi = driver.findElements(INVENTORY_ITEM_PRICE).stream()
            .map(e -> Double.parseDouble(e.getText().replace("$", "")))
            .collect(Collectors.toList());
        List<Double> sortedPricesLoHi = new ArrayList<>(pricesLoHi);
        Collections.sort(sortedPricesLoHi);
        Assertions.assertEquals(sortedPricesLoHi, pricesLoHi, "Products are not sorted by Price (low to high).");

        // Test Price (high to low)
        sortDropdown.selectByValue("hilo");
        List<Double> pricesHiLo = driver.findElements(INVENTORY_ITEM_PRICE).stream()
            .map(e -> Double.parseDouble(e.getText().replace("$", "")))
            .collect(Collectors.toList());
        List<Double> sortedPricesHiLo = new ArrayList<>(pricesHiLo);
        sortedPricesHiLo.sort(Collections.reverseOrder());
        Assertions.assertEquals(sortedPricesHiLo, pricesHiLo, "Products are not sorted by Price (high to low).");
    }

    @Test
    @Order(6)
    @DisplayName("Menu 'About' link should navigate to saucelabs.com")
    void testMenuAboutLink() {
        login(USERNAME, PASSWORD);
        wait.until(ExpectedConditions.visibilityOfElementLocated(INVENTORY_CONTAINER));

        wait.until(ExpectedConditions.elementToBeClickable(MENU_BUTTON)).click();
        validateExternalLink(ABOUT_LINK, "saucelabs.com");

        Assertions.assertTrue(driver.getCurrentUrl().contains("/inventory.html"), "Did not return to the inventory page.");
    }

    @Test
    @Order(7)
    @DisplayName("Footer social media links should navigate to correct domains")
    void testFooterSocialLinks() {
        login(USERNAME, PASSWORD);
        wait.until(ExpectedConditions.visibilityOfElementLocated(INVENTORY_CONTAINER));

        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight)");

        validateExternalLink(By.linkText("Twitter"), "twitter.com");
        validateExternalLink(By.linkText("Facebook"), "facebook.com");
        validateExternalLink(By.linkText("LinkedIn"), "linkedin.com");

        Assertions.assertTrue(driver.getCurrentUrl().contains("/inventory.html"), "Did not remain on inventory page after checking social links.");
    }

    @Test
    @Order(8)
    @DisplayName("User should be able to complete the full end-to-end checkout process")
    void testFullCheckoutProcess() {
        login(USERNAME, PASSWORD);
        wait.until(ExpectedConditions.visibilityOfElementLocated(INVENTORY_CONTAINER));

        resetAppState(); // 1. Start with a clean slate

        // 2. Add an item to the cart
        driver.findElement(By.xpath("//div[text()='Sauce Labs Fleece Jacket']/ancestor::div[@class='inventory_item']//button")).click();
        Assertions.assertEquals("1", driver.findElement(SHOPPING_CART_BADGE).getText(), "Cart badge should be 1.");

        // 3. Go to the cart and verify item
        driver.findElement(SHOPPING_CART_LINK).click();
        wait.until(ExpectedConditions.urlContains("/cart.html"));
        Assertions.assertTrue(driver.findElement(By.xpath("//div[@class='inventory_item_name' and text()='Sauce Labs Fleece Jacket']")).isDisplayed(),
            "Fleece Jacket not found in the cart.");

        // 4. Proceed to checkout and fill information
        driver.findElement(By.id("checkout")).click();
        wait.until(ExpectedConditions.urlContains("/checkout-step-one.html"));
        driver.findElement(By.id("first-name")).sendKeys("Gemini");
        driver.findElement(By.id("last-name")).sendKeys("Test");
        driver.findElement(By.id("postal-code")).sendKeys("12345");
        driver.findElement(By.id("continue")).click();

        // 5. Verify overview and finish checkout
        wait.until(ExpectedConditions.urlContains("/checkout-step-two.html"));
        Assertions.assertTrue(driver.findElement(By.xpath("//div[text()='Sauce Labs Fleece Jacket']")).isDisplayed(),
            "Item not found on checkout overview page.");
        Assertions.assertTrue(driver.findElement(By.className("summary_total_label")).getText().contains("53.99"),
            "Total price is incorrect on overview page.");
        driver.findElement(By.id("finish")).click();

        // 6. Assert completion message
        wait.until(ExpectedConditions.urlContains("/checkout-complete.html"));
        WebElement completeHeader = driver.findElement(By.className("complete-header"));
        Assertions.assertEquals("THANK YOU FOR YOUR ORDER", completeHeader.getText(), "Checkout completion message is incorrect.");

        // 7. Return to products page and verify cart is empty
        driver.findElement(By.id("back-to-products")).click();
        wait.until(ExpectedConditions.urlContains("/inventory.html"));
        Assertions.assertEquals(0, driver.findElements(SHOPPING_CART_BADGE).size(), "Cart should be empty after completing an order.");
    }
}