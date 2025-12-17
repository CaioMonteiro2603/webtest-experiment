package geminiPro.ws01.seq04;

import org.junit.jupiter.api.*;
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
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * A complete JUnit 5 test suite for the SauceDemo website using Selenium WebDriver with Firefox in headless mode.
 * This suite covers login, inventory management, sorting, cart functionality, a full checkout flow,
 * menu actions, and external social media links.
 * Tests are ordered to follow a logical user flow.
 */
@DisplayName("SauceDemo Full Test Suite (Firefox Headless)")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class saucedemo {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://www.saucedemo.com/v1/index.html";
    private static final String USERNAME = "standard_user";
    private static final String PASSWORD = "secret_sauce";

    /**
     * Initializes the Firefox WebDriver in headless mode once before all tests run.
     */
    @BeforeAll
    static void setupAll() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    /**
     * Quits the WebDriver instance after all tests have completed.
     */
    @AfterAll
    static void tearDownAll() {
        if (driver != null) {
            driver.quit();
        }
    }

    /**
     * Navigates to the base URL before each test to ensure a clean starting point.
     */
    @BeforeEach
    void setupEach() {
        // Navigate to the login page before each test, as logout will redirect here.
        // This ensures tests can run independently if the session is somehow maintained.
        driver.get(BASE_URL);
    }

    // --- Test Cases ---

    @Test
    @Order(1)
    @DisplayName("Should display login page elements correctly")
    void testLoginPageElements() {
        assertEquals("Swag Labs", driver.getTitle(), "Page title should be 'Swag Labs'");
        assertTrue(driver.findElement(By.id("user-name")).isDisplayed(), "Username input should be visible");
        assertTrue(driver.findElement(By.id("password")).isDisplayed(), "Password input should be visible");
        assertTrue(driver.findElement(By.id("login-button")).isDisplayed(), "Login button should be visible");
    }

    @Test
    @Order(2)
    @DisplayName("Should show error for invalid credentials")
    void testLoginWithInvalidCredentials() {
        driver.findElement(By.id("user-name")).sendKeys("invalid_user");
        driver.findElement(By.id("password")).sendKeys("wrong_password");
        driver.findElement(By.id("login-button")).click();

        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h3[data-test='error']")));
        assertTrue(errorMessage.getText().contains("Username and password do not match"), "Error message for invalid login is incorrect.");
    }

    @Test
    @Order(3)
    @DisplayName("Should log in successfully and land on inventory page")
    void testSuccessfulLoginAndInventoryPageLoad() {
        driver.findElement(By.id("user-name")).sendKeys(USERNAME);
        driver.findElement(By.id("password")).sendKeys(PASSWORD);
        driver.findElement(By.id("login-button")).click();

        wait.until(ExpectedConditions.urlContains("inventory.html"));
        assertEquals("https://www.saucedemo.com/v1/inventory.html", driver.getCurrentUrl(), "URL should be the inventory page after login.");
        assertTrue(driver.findElement(By.className("product_label")).isDisplayed(), "Products label should be visible, confirming login.");
    }

    @Test
    @Order(4)
    @DisplayName("Should sort products correctly by all options")
    void testProductSorting() {
        // Ensure we are on the inventory page
        if (!driver.getCurrentUrl().contains("inventory.html")) {
            testSuccessfulLoginAndInventoryPageLoad();
        }

        Select sortDropdown = new Select(driver.findElement(By.className("product_sort_container")));

        // Test Name (Z to A)
        sortDropdown.selectByValue("za");
        List<String> namesZA = getInventoryItemNames();
        List<String> sortedNamesZA = new ArrayList<>(namesZA);
        sortedNamesZA.sort(Collections.reverseOrder());
        assertEquals(sortedNamesZA, namesZA, "Products should be sorted by name Z to A.");

        // Test Price (low to high)
        sortDropdown.selectByValue("lohi");
        List<Double> pricesLoHi = getInventoryItemPrices();
        List<Double> sortedPricesLoHi = new ArrayList<>(pricesLoHi);
        Collections.sort(sortedPricesLoHi);
        assertEquals(sortedPricesLoHi, pricesLoHi, "Products should be sorted by price low to high.");

        // Test Price (high to low)
        sortDropdown.selectByValue("hilo");
        List<Double> pricesHiLo = getInventoryItemPrices();
        List<Double> sortedPricesHiLo = new ArrayList<>(pricesHiLo);
        sortedPricesHiLo.sort(Collections.reverseOrder());
        assertEquals(sortedPricesHiLo, pricesHiLo, "Products should be sorted by price high to low.");
        
        // Reset to default Name (A to Z)
        sortDropdown.selectByValue("az");
        List<String> namesAZ = getInventoryItemNames();
        List<String> sortedNamesAZ = new ArrayList<>(namesAZ);
        Collections.sort(sortedNamesAZ);
        assertEquals(sortedNamesAZ, namesAZ, "Products should be sorted by name A to Z.");
    }

    @Test
    @Order(5)
    @DisplayName("Should add and remove an item from the cart on the inventory page")
    void testAddAndRemoveItemFromInventory() {
        // Add item
        WebElement addToCartButton = driver.findElement(By.xpath("//div[text()='Sauce Labs Fleece Jacket']/ancestor::div[@class='inventory_item']//button"));
        addToCartButton.click();
        
        WebElement cartBadge = driver.findElement(By.className("shopping_cart_badge"));
        assertEquals("1", cartBadge.getText(), "Cart badge should show 1 after adding an item.");
        assertEquals("REMOVE", addToCartButton.getText(), "Button text should change to REMOVE.");

        // Remove item
        addToCartButton.click();
        assertTrue(wait.until(ExpectedConditions.invisibilityOfElementLocated(By.className("shopping_cart_badge"))), "Cart badge should disappear after removing the item.");
        assertEquals("ADD TO CART", addToCartButton.getText(), "Button text should revert to ADD TO CART.");
    }

    @Test
    @Order(6)
    @DisplayName("Should complete the full checkout flow successfully")
    void testCompleteCheckoutFlow() {
        // 1. Reset state and add item to cart
        resetAppState();
        driver.findElement(By.xpath("//div[text()='Sauce Labs Backpack']/ancestor::div[@class='inventory_item']//button")).click();
        
        // 2. Go to cart
        driver.findElement(By.className("shopping_cart_link")).click();
        wait.until(ExpectedConditions.urlContains("cart.html"));
        assertEquals(1, driver.findElements(By.className("cart_item")).size(), "There should be one item in the cart.");

        // 3. Proceed to checkout
        driver.findElement(By.cssSelector("a.btn_action.checkout_button")).click();
        wait.until(ExpectedConditions.urlContains("checkout-step-one.html"));
        
        // 4. Fill checkout information
        driver.findElement(By.id("first-name")).sendKeys("Test");
        driver.findElement(By.id("last-name")).sendKeys("User");
        driver.findElement(By.id("postal-code")).sendKeys("12345");
        driver.findElement(By.cssSelector("input.btn_primary.cart_button")).click();
        
        // 5. Verify overview and finish
        wait.until(ExpectedConditions.urlContains("checkout-step-two.html"));
        assertTrue(driver.findElement(By.className("summary_info")).isDisplayed(), "Checkout summary should be visible.");
        driver.findElement(By.cssSelector("a.btn_action.cart_button")).click();

        // 6. Assert completion
        wait.until(ExpectedConditions.urlContains("checkout-complete.html"));
        WebElement completeHeader = driver.findElement(By.className("complete-header"));
        assertEquals("THANK YOU FOR YOUR ORDER", completeHeader.getText(), "Checkout completion message is incorrect.");
        
        // Return to inventory page for next test
        driver.findElement(By.className("bm-burger-button")).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.id("inventory_sidebar_link"))).click();
    }

    @Test
    @Order(7)
    @DisplayName("Should open 'About' page and return")
    void testMenuAboutLink() {
        handleExternalLink(By.id("about_sidebar_link"), "saucelabs.com");
    }

    @Test
    @Order(8)
    @DisplayName("Should open Twitter footer link and return")
    void testFooterTwitterLink() {
        handleExternalLink(By.linkText("Twitter"), "twitter.com");
    }

    @Test
    @Order(9)
    @DisplayName("Should open Facebook footer link and return")
    void testFooterFacebookLink() {
        handleExternalLink(By.linkText("Facebook"), "facebook.com/saucelabs");
    }

    @Test
    @Order(10)
    @DisplayName("Should open LinkedIn footer link and return")
    void testFooterLinkedInLink() {
        handleExternalLink(By.linkText("LinkedIn"), "linkedin.com/company/sauce-labs");
    }

    @Test
    @Order(11)
    @DisplayName("Should log out successfully")
    void testMenuLogout() {
        driver.findElement(By.className("bm-burger-button")).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.id("logout_sidebar_link"))).click();
        
        wait.until(ExpectedConditions.urlToBe(BASE_URL));
        assertTrue(driver.findElement(By.id("login-button")).isDisplayed(), "Should be on the login page after logout.");
    }

    // --- Helper Methods ---

    /**
     * Resets the application state using the sidebar menu option.
     * Assumes the user is logged in.
     */
    private void resetAppState() {
        driver.findElement(By.className("bm-burger-button")).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link"))).click();
        driver.findElement(By.className("bm-cross-button")).click();
        // Wait for menu to close
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.className("bm-menu-wrap")));
    }

    /**
     * Handles clicking a link that opens a new tab, verifying the new tab's URL,
     * closing it, and switching back to the original tab.
     *
     * @param locator The locator for the link to click.
     * @param expectedUrlPart The expected partial URL of the new tab.
     */
    private void handleExternalLink(By locator, String expectedUrlPart) {
        String originalWindow = driver.getWindowHandle();
        
        // For menu items, we need to open the menu first
        if (locator.toString().contains("sidebar_link")) {
            driver.findElement(By.className("bm-burger-button")).click();
            wait.until(ExpectedConditions.elementToBeClickable(locator)).click();
        } else {
             wait.until(ExpectedConditions.elementToBeClickable(locator)).click();
        }
        
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        Set<String> allWindows = driver.getWindowHandles();
        allWindows.remove(originalWindow);
        String newWindow = allWindows.iterator().next();

        driver.switchTo().window(newWindow);
        wait.until(ExpectedConditions.urlContains(expectedUrlPart));
        assertTrue(driver.getCurrentUrl().contains(expectedUrlPart), "URL of the new tab should contain " + expectedUrlPart);
        
        driver.close();
        driver.switchTo().window(originalWindow);
        assertEquals("https://www.saucedemo.com/v1/inventory.html", driver.getCurrentUrl(), "Should have returned to the inventory page.");
    }

    /**
     * Retrieves the names of all inventory items currently displayed on the page.
     * @return A list of product names as strings.
     */
    private List<String> getInventoryItemNames() {
        List<WebElement> itemElements = driver.findElements(By.className("inventory_item_name"));
        return itemElements.stream().map(WebElement::getText).collect(Collectors.toList());
    }

    /**
     * Retrieves the prices of all inventory items currently displayed on the page.
     * @return A list of product prices as doubles.
     */
    private List<Double> getInventoryItemPrices() {
        List<WebElement> priceElements = driver.findElements(By.className("inventory_item_price"));
        return priceElements.stream()
                            .map(el -> Double.parseDouble(el.getText().replace("$", "")))
                            .collect(Collectors.toList());
    }
}