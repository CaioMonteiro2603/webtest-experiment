package geminiPRO.ws01.seq10;

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
 * A comprehensive JUnit 5 test suite for the SauceDemo website using Selenium WebDriver with Firefox in headless mode.
 * This suite covers login, product sorting, cart management, the full checkout process, and external link validation.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SauceDemoComprehensiveTest {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://www.saucedemo.com/v1/index.html";
    private static final String INVENTORY_URL = "https://www.saucedemo.com/v1/inventory.html";
    private static final String USER_STANDARD = "standard_user";
    private static final String USER_LOCKED = "locked_out_user";
    private static final String PASSWORD_CORRECT = "secret_sauce";
    private static final String PASSWORD_INCORRECT = "wrong_password";

    // --- Locators ---
    private static final By USERNAME_INPUT = By.id("user-name");
    private static final By PASSWORD_INPUT = By.id("password");
    private static final By LOGIN_BUTTON = By.id("login-button");
    private static final By ERROR_MESSAGE_CONTAINER = By.cssSelector("h3[data-test='error']");
    private static final By INVENTORY_CONTAINER = By.id("inventory_container");
    private static final By BURGER_MENU_BUTTON = By.id("react-burger-menu-btn");
    private static final By LOGOUT_LINK = By.id("logout_sidebar_link");
    private static final By RESET_APP_STATE_LINK = By.id("reset_sidebar_link");
    private static final By MENU_CLOSE_BUTTON = By.id("react-burger-cross-btn");
    private static final By SORT_DROPDOWN = By.cssSelector("[data-test='product_sort_container']");
    private static final By INVENTORY_ITEM_NAME = By.className("inventory_item_name");
    private static final By INVENTORY_ITEM_PRICE = By.className("inventory_item_price");
    private static final By CART_BADGE = By.cssSelector(".shopping_cart_link .shopping_cart_badge");
    private static final By CART_ICON = By.id("shopping_cart_container");
    private static final By CHECKOUT_BUTTON = By.id("checkout");
    private static final By FIRST_NAME_INPUT = By.id("first-name");
    private static final By LAST_NAME_INPUT = By.id("last-name");
    private static final By POSTAL_CODE_INPUT = By.id("postal-code");
    private static final By CONTINUE_BUTTON = By.id("continue");
    private static final By FINISH_BUTTON = By.id("finish");
    private static final By CHECKOUT_COMPLETE_HEADER = By.className("complete-header");

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

    /**
     * Helper method for logging into the application.
     * @param username The username to use for login.
     * @param password The password to use for login.
     */
    private void login(String username, String password) {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(USERNAME_INPUT)).sendKeys(username);
        driver.findElement(PASSWORD_INPUT).sendKeys(password);
        driver.findElement(LOGIN_BUTTON).click();
    }

    /**
     * Helper method to reset the application state via the burger menu.
     */
    private void resetAppState() {
        wait.until(ExpectedConditions.elementToBeClickable(BURGER_MENU_BUTTON)).click();
        wait.until(ExpectedConditions.elementToBeClickable(RESET_APP_STATE_LINK)).click();
        wait.until(ExpectedConditions.elementToBeClickable(MENU_CLOSE_BUTTON)).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(INVENTORY_CONTAINER)); // Wait for menu to close
    }

    @Test
    @Order(1)
    @DisplayName("🧪 Test Login Page Functionality")
    void testLoginPage() {
        // Test case 1: Invalid credentials
        login(USER_STANDARD, PASSWORD_INCORRECT);
        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(ERROR_MESSAGE_CONTAINER));
        Assertions.assertTrue(error.getText().contains("Username and password do not match"), "Error message for wrong password was not correct.");

        // Test case 2: Locked out user
        login(USER_LOCKED, PASSWORD_CORRECT);
        error = wait.until(ExpectedConditions.visibilityOfElementLocated(ERROR_MESSAGE_CONTAINER));
        Assertions.assertTrue(error.getText().contains("Sorry, this user has been locked out"), "Error message for locked out user was not correct.");

        // Test case 3: Successful login
        login(USER_STANDARD, PASSWORD_CORRECT);
        wait.until(ExpectedConditions.urlContains("inventory.html"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("inventory.html"), "Successful login should navigate to the inventory page.");
        Assertions.assertTrue(driver.findElements(INVENTORY_CONTAINER).size() > 0, "Inventory container should be present after login.");
    }

    @Test
    @Order(2)
    @DisplayName("🧪 Test Product Sorting Options")
    void testProductSorting() {
        if (!driver.getCurrentUrl().equals(INVENTORY_URL)) {
             login(USER_STANDARD, PASSWORD_CORRECT);
        }
        wait.until(ExpectedConditions.visibilityOfElementLocated(INVENTORY_CONTAINER));

        Select sortSelect = new Select(driver.findElement(SORT_DROPDOWN));

        // Test Sort by Name (Z to A)
        sortSelect.selectByValue("za");
        List<String> namesZA = driver.findElements(INVENTORY_ITEM_NAME).stream().map(WebElement::getText).collect(Collectors.toList());
        List<String> sortedNamesZA = new ArrayList<>(namesZA);
        sortedNamesZA.sort(Collections.reverseOrder());
        Assertions.assertEquals(sortedNamesZA, namesZA, "Products should be sorted by name Z to A.");

        // Test Sort by Price (Low to High)
        sortSelect.selectByValue("lohi");
        List<Double> pricesLoHi = driver.findElements(INVENTORY_ITEM_PRICE).stream()
            .map(e -> Double.parseDouble(e.getText().replace("$", "")))
            .collect(Collectors.toList());
        List<Double> sortedPricesLoHi = new ArrayList<>(pricesLoHi);
        Collections.sort(sortedPricesLoHi);
        Assertions.assertEquals(sortedPricesLoHi, pricesLoHi, "Products should be sorted by price low to high.");

        // Test Sort by Price (High to Low)
        sortSelect.selectByValue("hilo");
        List<Double> pricesHiLo = driver.findElements(INVENTORY_ITEM_PRICE).stream()
            .map(e -> Double.parseDouble(e.getText().replace("$", "")))
            .collect(Collectors.toList());
        List<Double> sortedPricesHiLo = new ArrayList<>(pricesHiLo);
        sortedPricesHiLo.sort(Collections.reverseOrder());
        Assertions.assertEquals(sortedPricesHiLo, pricesHiLo, "Products should be sorted by price high to low.");

        // Return to default sort: Name (A to Z)
        sortSelect.selectByValue("az");
        List<String> namesAZ = driver.findElements(INVENTORY_ITEM_NAME).stream().map(WebElement::getText).collect(Collectors.toList());
        List<String> sortedNamesAZ = new ArrayList<>(namesAZ);
        Collections.sort(sortedNamesAZ);
        Assertions.assertEquals(sortedNamesAZ, namesAZ, "Products should be sorted by name A to Z.");
    }

    @Test
    @Order(3)
    @DisplayName("🧪 Test Full End-to-End Checkout Flow")
    void testFullCheckoutFlow() {
        if (!driver.getCurrentUrl().equals(INVENTORY_URL)) {
            login(USER_STANDARD, PASSWORD_CORRECT);
        }
        resetAppState(); // Start with a clean slate

        // Add two items to the cart
        wait.until(ExpectedConditions.elementToBeClickable(By.id("add-to-cart-sauce-labs-backpack"))).click();
        driver.findElement(By.id("add-to-cart-sauce-labs-bike-light")).click();

        // Verify cart badge shows "2"
        WebElement badge = wait.until(ExpectedConditions.visibilityOfElementLocated(CART_BADGE));
        Assertions.assertEquals("2", badge.getText(), "Cart badge should display 2 items.");

        // Proceed to cart and checkout
        driver.findElement(CART_ICON).click();
        wait.until(ExpectedConditions.urlContains("cart.html"));
        wait.until(ExpectedConditions.elementToBeClickable(CHECKOUT_BUTTON)).click();

        // Fill checkout information
        wait.until(ExpectedConditions.urlContains("checkout-step-one.html"));
        driver.findElement(FIRST_NAME_INPUT).sendKeys("Gemini");
        driver.findElement(LAST_NAME_INPUT).sendKeys("Tester");
        driver.findElement(POSTAL_CODE_INPUT).sendKeys("12345");
        driver.findElement(CONTINUE_BUTTON).click();

        // Finish checkout
        wait.until(ExpectedConditions.urlContains("checkout-step-two.html"));
        wait.until(ExpectedConditions.elementToBeClickable(FINISH_BUTTON)).click();

        // Assert successful order
        wait.until(ExpectedConditions.urlContains("checkout-complete.html"));
        WebElement completeHeader = driver.findElement(CHECKOUT_COMPLETE_HEADER);
        Assertions.assertEquals("THANK YOU FOR YOUR ORDER", completeHeader.getText(), "Checkout completion message is incorrect.");

        // Return to products page and verify cart is empty
        driver.findElement(By.id("back-to-products")).click();
        wait.until(ExpectedConditions.urlContains("inventory.html"));
        Assertions.assertEquals(0, driver.findElements(CART_BADGE).size(), "Cart should be empty after checkout.");
    }

    @Test
    @Order(4)
    @DisplayName("🧪 Test Reset App State Functionality")
    void testResetAppState() {
         if (!driver.getCurrentUrl().equals(INVENTORY_URL)) {
            login(USER_STANDARD, PASSWORD_CORRECT);
        }

        // Add an item and verify cart state
        String itemId = "add-to-cart-sauce-labs-fleece-jacket";
        wait.until(ExpectedConditions.elementToBeClickable(By.id(itemId))).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(CART_BADGE));
        Assertions.assertEquals("1", driver.findElement(CART_BADGE).getText(), "Cart should have 1 item before reset.");

        // Reset the app state
        resetAppState();

        // Verify cart is now empty
        Assertions.assertTrue(driver.findElements(CART_BADGE).isEmpty(), "Cart badge should not be present after reset.");

        // Verify the button text reverted from "Remove" to "Add to Cart"
        WebElement addButton = driver.findElement(By.id(itemId));
        Assertions.assertEquals("ADD TO CART", addButton.getText().toUpperCase(), "Button text should revert to 'ADD TO CART' after reset.");
    }

    @Test
    @Order(5)
    @DisplayName("🧪 Test External Links (About, Social Media)")
    void testExternalLinks() {
        if (!driver.getCurrentUrl().equals(INVENTORY_URL)) {
             login(USER_STANDARD, PASSWORD_CORRECT);
        }
        
        String originalWindow = driver.getWindowHandle();

        // --- Test "About" link from menu ---
        wait.until(ExpectedConditions.elementToBeClickable(BURGER_MENU_BUTTON)).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.id("about_sidebar_link"))).click();

        // Switch to the new window
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.contentEquals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        // Verify URL, close window, and switch back
        wait.until(ExpectedConditions.urlContains("saucelabs.com"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("saucelabs.com"), "About link should navigate to saucelabs.com.");
        driver.close();
        driver.switchTo().window(originalWindow);

        // --- Test Footer Social Links ---
        testSocialLink("Twitter", "twitter.com");
        testSocialLink("Facebook", "facebook.com/saucelabs");
        testSocialLink("LinkedIn", "linkedin.com");
    }

    /**
     * Helper method to test a social media link that opens a new tab.
     * @param linkText The visible text of the link to click.
     * @param expectedDomain The domain expected in the new tab's URL.
     */
    private void testSocialLink(String linkText, String expectedDomain) {
        String originalWindow = driver.getWindowHandle();
        
        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight)");
        
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText(linkText))).click();

        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.contentEquals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        wait.until(d -> d.getCurrentUrl().contains(expectedDomain));
        Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain), "Link should navigate to " + expectedDomain);
        
        driver.close();
        driver.switchTo().window(originalWindow);
        wait.until(ExpectedConditions.urlContains("inventory.html")); // Ensure focus is back
    }

    @Test
    @Order(6)
    @DisplayName("🧪 Test Logout Functionality")
    void testLogout() {
        if (!driver.getCurrentUrl().equals(INVENTORY_URL)) {
            login(USER_STANDARD, PASSWORD_CORRECT);
        }
        
        wait.until(ExpectedConditions.elementToBeClickable(BURGER_MENU_BUTTON)).click();
        wait.until(ExpectedConditions.elementToBeClickable(LOGOUT_LINK)).click();
        
        wait.until(ExpectedConditions.urlToBe(BASE_URL));
        Assertions.assertEquals(BASE_URL, driver.getCurrentUrl(), "Logout should return to the login page.");
        Assertions.assertTrue(driver.findElement(LOGIN_BUTTON).isDisplayed(), "Login button should be visible after logout.");
    }
}