package geminiPRO.ws01.seq06;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * JUnit 5 test suite for saucedemo.com using Selenium WebDriver with headless Firefox.
 * This suite covers login, inventory management, sorting, cart functionality,
 * the full checkout process, menu navigation, and external link validation.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SauceDemoComprehensiveTest {

    private static final String BASE_URL = "https://www.saucedemo.com/v1/index.html";
    private static final String VALID_USER = "standard_user";
    private static final String LOCKED_USER = "locked_out_user";
    private static final String PASSWORD = "secret_sauce";
    private static final Duration WAIT_TIMEOUT = Duration.ofSeconds(10);

    private static WebDriver driver;
    private static WebDriverWait wait;

    // Locators
    private static final By USERNAME_INPUT = By.id("user-name");
    private static final By PASSWORD_INPUT = By.id("password");
    private static final By LOGIN_BUTTON = By.id("login-button");
    private static final By INVENTORY_LIST = By.className("inventory_list");
    private static final By BURGER_MENU_BUTTON = By.id("react-burger-menu-btn");
    private static final By LOGOUT_LINK = By.id("logout_sidebar_link");
    private static final By RESET_APP_STATE_LINK = By.id("reset_sidebar_link");
    private static final By CLOSE_MENU_BUTTON = By.id("react-burger-cross-btn");
    private static final By SHOPPING_CART_BADGE = By.className("shopping_cart_badge");
    private static final By SORT_DROPDOWN = By.cssSelector("[data-test='product_sort_container']");
    private static final By INVENTORY_ITEM_NAME = By.className("inventory_item_name");
    private static final By INVENTORY_ITEM_PRICE = By.className("inventory_item_price");

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
        // Ensure each test starts from a clean slate by navigating to the base URL
        driver.get(BASE_URL);
    }

    /**
     * Test various login scenarios: invalid password, locked out user, and successful login.
     */
    @Test
    @Order(1)
    void loginFunctionalityTest() {
        // Test Case 1: Invalid Password
        performLogin(VALID_USER, "wrong_password");
        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-test='error']")));
        assertTrue(error.getText().contains("Username and password do not match"), "Error message for invalid password was not correct.");

        // Test Case 2: Locked Out User
        driver.navigate().refresh(); // Refresh to clear form
        performLogin(LOCKED_USER, PASSWORD);
        error = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-test='error']")));
        assertTrue(error.getText().contains("Sorry, this user has been locked out."), "Error message for locked out user was not correct.");

        // Test Case 3: Successful Login
        driver.navigate().refresh(); // Refresh to clear form
        performLogin(VALID_USER, PASSWORD);
        assertTrue(driver.getCurrentUrl().contains("inventory.html"), "Successful login should redirect to the inventory page.");
        assertTrue(wait.until(ExpectedConditions.visibilityOfElementLocated(INVENTORY_LIST)).isDisplayed(), "Inventory list should be visible after login.");
    }

    /**
     * Verifies the sorting functionality on the inventory page.
     */
    @Test
    @Order(2)
    void productSortTest() {
        loginAsStandardUser();
        Select sortSelect = new Select(driver.findElement(SORT_DROPDOWN));

        // Test Name (Z to A)
        List<String> originalNames = getInventoryItemNames();
        List<String> sortedNames = new ArrayList<>(originalNames);
        Collections.sort(sortedNames, Collections.reverseOrder());
        sortSelect.selectByValue("za");
        List<String> namesAfterZaSort = getInventoryItemNames();
        assertEquals(sortedNames, namesAfterZaSort, "Items should be sorted by name Z to A.");

        // Test Price (low to high)
        sortSelect.selectByValue("lohi");
        List<Double> pricesAfterLoHiSort = getInventoryItemPrices();
        List<Double> sortedPrices = new ArrayList<>(pricesAfterLoHiSort);
        Collections.sort(sortedPrices);
        assertEquals(sortedPrices, pricesAfterLoHiSort, "Items should be sorted by price low to high.");

        // Test Price (high to low)
        sortSelect.selectByValue("hilo");
        List<Double> pricesAfterHiLoSort = getInventoryItemPrices();
        List<Double> reverseSortedPrices = new ArrayList<>(pricesAfterHiLoSort);
        reverseSortedPrices.sort(Collections.reverseOrder());
        assertEquals(reverseSortedPrices, pricesAfterHiLoSort, "Items should be sorted by price high to low.");
    }

    /**
     * Tests adding and removing items from the cart and verifies the cart badge count.
     */
    @Test
    @Order(3)
    void cartFunctionalityTest() {
        loginAsStandardUser();
        resetAppState(); // Ensure clean state

        // Add first item
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("[data-test='add-to-cart-sauce-labs-backpack']"))).click();
        assertEquals("1", driver.findElement(SHOPPING_CART_BADGE).getText(), "Cart badge should show 1 after adding one item.");

        // Add second item
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("[data-test='add-to-cart-sauce-labs-bike-light']"))).click();
        assertEquals("2", driver.findElement(SHOPPING_CART_BADGE).getText(), "Cart badge should show 2 after adding a second item.");

        // Remove first item
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("[data-test='remove-sauce-labs-backpack']"))).click();
        assertEquals("1", driver.findElement(SHOPPING_CART_BADGE).getText(), "Cart badge should show 1 after removing an item.");

        // Remove second item
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("[data-test='remove-sauce-labs-bike-light']"))).click();
        assertFalse(isElementPresent(SHOPPING_CART_BADGE), "Cart badge should disappear when cart is empty.");
    }

    /**
     * Executes the entire checkout flow, from adding an item to successful purchase.
     */
    @Test
    @Order(4)
    void fullCheckoutFlowTest() {
        loginAsStandardUser();
        resetAppState();

        // Add item and go to cart
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("[data-test='add-to-cart-sauce-labs-onesie']"))).click();
        driver.findElement(By.className("shopping_cart_link")).click();
        wait.until(ExpectedConditions.urlContains("cart.html"));
        assertTrue(driver.findElement(By.linkText("Sauce Labs Onesie")).isDisplayed(), "Item should be visible in the cart.");

        // Proceed to checkout step one
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("[data-test='checkout']"))).click();
        wait.until(ExpectedConditions.urlContains("checkout-step-one.html"));

        // Fill user information
        driver.findElement(By.cssSelector("[data-test='firstName']")).sendKeys("Gemini");
        driver.findElement(By.cssSelector("[data-test='lastName']")).sendKeys("Pro");
        driver.findElement(By.cssSelector("[data-test='postalCode']")).sendKeys("12345");
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("[data-test='continue']"))).click();

        // Proceed to checkout step two
        wait.until(ExpectedConditions.urlContains("checkout-step-two.html"));
        assertTrue(driver.findElement(By.className("summary_info")).isDisplayed(), "Order summary should be visible.");
        String itemTotalText = driver.findElement(By.className("summary_subtotal_label")).getText();
        assertNotEquals("", itemTotalText, "Item total should not be empty.");

        // Finish checkout
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("[data-test='finish']"))).click();
        wait.until(ExpectedConditions.urlContains("checkout-complete.html"));
        WebElement confirmationHeader = driver.findElement(By.className("complete-header"));
        assertEquals("THANK YOU FOR YOUR ORDER", confirmationHeader.getText(), "Confirmation message should be displayed.");

        // Return to inventory
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("[data-test='back-to-products']"))).click();
        assertTrue(driver.getCurrentUrl().contains("inventory.html"), "Should be redirected back to the inventory page.");
    }

    /**
     * Tests menu navigation, app state reset, and logout functionality.
     */
    @Test
    @Order(5)
    void menuNavigationAndStateTest() {
        loginAsStandardUser();

        // Add an item to test reset
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("[data-test='add-to-cart-sauce-labs-fleece-jacket']"))).click();
        assertTrue(isElementPresent(SHOPPING_CART_BADGE), "Cart should have an item before reset.");

        // Reset App State
        resetAppState();
        assertFalse(isElementPresent(SHOPPING_CART_BADGE), "Cart should be empty after resetting app state.");

        // Test Logout
        wait.until(ExpectedConditions.elementToBeClickable(BURGER_MENU_BUTTON)).click();
        wait.until(ExpectedConditions.elementToBeClickable(LOGOUT_LINK)).click();
        assertTrue(driver.getCurrentUrl().equals(BASE_URL), "Should be redirected to the login page after logout.");
        assertTrue(wait.until(ExpectedConditions.visibilityOfElementLocated(LOGIN_BUTTON)).isDisplayed(), "Login button should be visible after logout.");
    }

    /**
     * Verifies that external links (social media, about page) open correctly in new tabs.
     */
    @Test
    @Order(6)
    void externalLinksTest() {
        loginAsStandardUser();

        // Test "About" link from menu
        wait.until(ExpectedConditions.elementToBeClickable(BURGER_MENU_BUTTON)).click();
        verifyExternalLink(wait.until(ExpectedConditions.elementToBeClickable(By.id("about_sidebar_link"))), "saucelabs.com");

        // Test footer links
        verifyExternalLink(driver.findElement(By.linkText("Twitter")), "twitter.com");
        verifyExternalLink(driver.findElement(By.linkText("Facebook")), "facebook.com");
        verifyExternalLink(driver.findElement(By.linkText("LinkedIn")), "linkedin.com");
    }

    // --- Helper Methods ---

    /**
     * Performs a login action.
     *
     * @param username The username to use.
     * @param password The password to use.
     */
    private void performLogin(String username, String password) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(USERNAME_INPUT)).sendKeys(username);
        driver.findElement(PASSWORD_INPUT).sendKeys(password);
        driver.findElement(LOGIN_BUTTON).click();
    }

    /**
     * A convenience method to log in as the standard user.
     */
    private void loginAsStandardUser() {
        if (!driver.getCurrentUrl().contains("inventory.html")) {
            driver.get(BASE_URL);
            performLogin(VALID_USER, PASSWORD);
            wait.until(ExpectedConditions.urlContains("inventory.html"));
        }
    }

    /**
     * Resets the application state via the burger menu.
     */
    private void resetAppState() {
        wait.until(ExpectedConditions.elementToBeClickable(BURGER_MENU_BUTTON)).click();
        wait.until(ExpectedConditions.elementToBeClickable(RESET_APP_STATE_LINK)).click();
        // The menu animation can be slow, wait for it to be clickable before closing
        wait.until(ExpectedConditions.elementToBeClickable(CLOSE_MENU_BUTTON)).click();
    }

    /**
     * Checks if an element is present on the page.
     *
     * @param by The locator for the element.
     * @return True if the element exists, false otherwise.
     */
    private boolean isElementPresent(By by) {
        return !driver.findElements(by).isEmpty();
    }

    /**
     * Retrieves the text of all inventory item names.
     *
     * @return A list of product names as strings.
     */
    private List<String> getInventoryItemNames() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(INVENTORY_ITEM_NAME));
        List<WebElement> nameElements = driver.findElements(INVENTORY_ITEM_NAME);
        return nameElements.stream().map(WebElement::getText).collect(Collectors.toList());
    }

    /**
     * Retrieves the prices of all inventory items.
     *
     * @return A list of product prices as doubles.
     */
    private List<Double> getInventoryItemPrices() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(INVENTORY_ITEM_PRICE));
        List<WebElement> priceElements = driver.findElements(INVENTORY_ITEM_PRICE);
        return priceElements.stream()
                .map(el -> Double.parseDouble(el.getText().replace("$", "")))
                .collect(Collectors.toList());
    }

    /**
     * Clicks a link, switches to the new tab, verifies the URL, closes the tab, and switches back.
     *
     * @param linkElement     The WebElement of the link to click.
     * @param expectedDomain  The domain expected in the new tab's URL.
     */
    private void verifyExternalLink(WebElement linkElement, String expectedDomain) {
        String originalWindow = driver.getWindowHandle();
        wait.until(ExpectedConditions.elementToBeClickable(linkElement)).click();

        // Wait for the new window or tab
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));

        // Switch to the new window
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.contentEquals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        // Verify the URL of the new tab
        wait.until(ExpectedConditions.urlContains(expectedDomain));
        assertTrue(driver.getCurrentUrl().contains(expectedDomain), "URL of the new tab should contain " + expectedDomain);

        // Close the new tab and switch back
        driver.close();
        driver.switchTo().window(originalWindow);
        assertTrue(driver.getCurrentUrl().contains("saucedemo.com"), "Should have switched back to the original window.");
    }
}