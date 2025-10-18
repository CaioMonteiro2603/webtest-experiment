package GPT20b.ws01.seq08;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.Select;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@TestMethodOrder(OrderAnnotation.class)
public class SaucedemoTest {

    private static final String BASE_URL = "https://www.saucedemo.com/v1/index.html";
    private static final String USERNAME = "standard_user";
    private static final String PASSWORD = "secret_sauce";

    private static WebDriver driver;
    private static WebDriverWait wait;

    @BeforeAll
    public static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless"); // REQUIRED
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    // Helper: navigate to base page and perform login with valid credentials
    private void loginValid() {
        driver.get(BASE_URL);
        By loginButton = By.cssSelector("[data-test='login-button']");
        wait.until(ExpectedConditions.elementToBeClickable(loginButton)).click();

        // Wait for inventory container to be visible
        By inventoryContainer = By.id("inventory_container");
        Assertions.assertTrue(
                wait.until(ExpectedConditions.visibilityOfElementLocated(inventoryContainer)).isDisplayed(),
                "Inventory container not displayed after login.");
    }

    // Helper: perform a login with provided credentials
    private void login(String username, String password) {
        driver.get(BASE_URL);
        By usernameField = By.id("user-name");
        By passwordField = By.id("password");
        By loginBtn = By.cssSelector("[data-test='login-button']");

        wait.until(ExpectedConditions.visibilityOfElementLocated(usernameField)).sendKeys(username);
        wait.until(ExpectedConditions.visibilityOfElementLocated(passwordField)).sendKeys(password);
        wait.until(ExpectedConditions.elementToBeClickable(loginBtn)).click();
    }

    // Helper: reset app state via burger menu
    private void resetAppState() {
        By menuBtn = By.id("react-burger-menu-btn");
        wait.until(ExpectedConditions.elementToBeClickable(menuBtn)).click();

        By resetBtn = By.id("reset_sidebar_link");
        wait.until(ExpectedConditions.elementToBeClickable(resetBtn)).click();

        // Verify inventory page reloaded
        By inventoryContainer = By.id("inventory_container");
        wait.until(ExpectedConditions.visibilityOfElementLocated(inventoryContainer));
    }

    // Helper: open burger menu and perform click on specified id
    private void clickMenuItem(String itemId) {
        By menuBtn = By.id("react-burger-menu-btn");
        wait.until(ExpectedConditions.elementToBeClickable(menuBtn)).click();

        By item = By.id(itemId);
        wait.until(ExpectedConditions.elementToBeClickable(item)).click();
    }

    // Helper: get cart badge count
    private int getCartBadgeCount() {
        List<WebElement> badges = driver.findElements(By.cssSelector(".shopping_cart_badge"));
        if (badges.isEmpty()) {
            return 0;
        }
        return Integer.parseInt(badges.get(0).getText());
    }

    // Helper: add all items to cart
    private void addAllItemsToCart() {
        List<WebElement> addButtons = wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(
                By.cssSelector("button[data-test^='add-to-cart']"), 0));
        for (WebElement btn : addButtons) {
            if (btn.isDisplayed()) {
                wait.until(ExpectedConditions.elementToBeClickable(btn)).click();
            }
        }
    }

    // TEST 1: Login with valid credentials
    @Test
    @Order(1)
    public void testValidLogin() {
        loginValid();
        // Further assertions already done in helper
    }

    // TEST 2: Login with invalid credentials
    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        login("invalid_user", "wrong_pass");
        By errorMsg = By.cssSelector("[data-test='error']");
        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(errorMsg));
        Assertions.assertTrue(error.getText().contains("Username and password do not match"),
                "Expected error message for invalid credentials.");
    }

    // TEST 3: Sorting options functionality
    @Test
    @Order(3)
    public void testSortingOptions() {
        loginValid();
        By sortSelect = By.cssSelector("select[data-test='product_sort_container']");
        WebElement sortElement = wait.until(ExpectedConditions.elementToBeClickable(sortSelect));
        Select sort = new Select(sortElement);

        // Mapping of option value to expected first item name
        var expectations = new java.util.LinkedHashMap<String, String>();
        expectations.put("az", "Sauce Labs Backpack");
        expectations.put("za", "Test.allTheThings() T-Shirt (Red)");
        expectations.put("lohi", "Sauce Labs Onesie");
        expectations.put("hilo", "Sauce Labs Fleece Jacket");

        for (var entry : expectations.entrySet()) {
            sort.selectByValue(entry.getKey());
            // Wait for inventory items to be reloaded
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".inventory_item_name")));

            List<WebElement> items = driver.findElements(By.cssSelector(".inventory_item_name"));
            Assertions.assertFalse(items.isEmpty(), "No inventory items found after sorting.");
            String firstItemName = items.get(0).getText();
            Assertions.assertEquals(
                    entry.getValue(),
                    firstItemName,
                    String.format("After sorting '%s', first item should be '%s' but found '%s'",
                            entry.getKey(), entry.getValue(), firstItemName));
        }
    }

    // TEST 4: Burger menu interactions and external links
    @Test
    @Order(4)
    public void testBurgerMenuItems() {
        loginValid();

        // All Items
        clickMenuItem("inventory_sidebar_link");
        By inventoryContainer = By.id("inventory_container");
        Assertions.assertTrue(
                wait.until(ExpectedConditions.visibilityOfElementLocated(inventoryContainer)).isDisplayed(),
                "All Items page not displayed.");

        // About (external)
        clickMenuItem("about_sidebar_link");
        String aboutWindow = driver.getWindowHandle();
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String handle : driver.getWindowHandles()) {
            if (!handle.equals(aboutWindow)) {
                driver.switchTo().window(handle);
                Assertions.assertTrue(driver.getCurrentUrl().contains("saucelabs.com"),
                        "About link should open Saucelabs domain.");
                driver.close();
                driver.switchTo().window(aboutWindow);
            }
        }

        // Reset App State
        clickMenuItem("reset_sidebar_link");
        wait.until(ExpectedConditions.visibilityOfElementLocated(inventoryContainer));
        Assertions.assertEquals(0, getCartBadgeCount(), "Cart badge should be 0 after reset.");

        // Logout
        clickMenuItem("logout_sidebar_link");
        Assertions.assertTrue(driver.getCurrentUrl().contains("index.html"),
                "Should be redirected to login page after logout.");
        By loginButton = By.cssSelector("[data-test='login-button']");
        Assertions.assertTrue(wait.until(ExpectedConditions.visibilityOfElementLocated(loginButton)).isDisplayed(),
                "Login button not displayed after logout.");
    }

    // TEST 5: Complete checkout flow
    @Test
    @Order(5)
    public void testCheckoutFlow() {
        loginValid();

        // Add first two items to cart
        List<WebElement> addButtons = wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(
                By.cssSelector("button[data-test^='add-to-cart']"), 0));
        for (int i = 0; i < 2; i++) {
            wait.until(ExpectedConditions.elementToBeClickable(addButtons.get(i))).click();
        }
        Assertions.assertEquals(2, getCartBadgeCount(), "Cart badge should be 2 after adding items.");

        // Go to cart
        By cartIcon = By.id("shopping_cart_container");
        wait.until(ExpectedConditions.elementToBeClickable(cartIcon)).click();

        // Checkout
        By checkoutBtn = By.id("checkout");
        wait.until(ExpectedConditions.elementToBeClickable(checkoutBtn)).click();
        Assertions.assertTrue(driver.getCurrentUrl().contains("checkout-step-one.html"),
                "Did not navigate to checkout step one.");

        // Fill form
        By firstNameField = By.id("first-name");
        By lastNameField = By.id("last-name");
        By postalField = By.id("postal-code");
        By continueBtn = By.id("continue");

        wait.until(ExpectedConditions.visibilityOfElementLocated(firstNameField)).sendKeys("John");
        wait.until(ExpectedConditions.visibilityOfElementLocated(lastNameField)).sendKeys("Doe");
        wait.until(ExpectedConditions.visibilityOfElementLocated(postalField)).sendKeys("12345");
        wait.until(ExpectedConditions.elementToBeClickable(continueBtn)).click();

        // Review order
        Assertions.assertTrue(driver.getCurrentUrl().contains("checkout-step-two.html"),
                "Did not navigate to checkout step two.");
        By finishBtn = By.id("finish");
        wait.until(ExpectedConditions.elementToBeClickable(finishBtn)).click();

        // Confirmation
        Assertions.assertTrue(driver.getCurrentUrl().contains("checkout-complete.html"),
                "Did not navigate to checkout complete page.");
        By thankYouMsg = By.cssSelector(".complete-header");
        Assertions.assertTrue(wait.until(ExpectedConditions.visibilityOfElementLocated(thankYouMsg)).isDisplayed(),
                "Thank you message not displayed.");
        Assertions.assertTrue(driver.findElement(thankYouMsg).getText().contains("THANK YOU FOR YOUR ORDER"),
                "Thank you message does not contain expected text.");

        // Verify cart badge reset
        Assertions.assertEquals(0, getCartBadgeCount(), "Cart badge should be reset to 0 after checkout.");
    }

    // TEST 6: Add and remove items from cart
    @Test
    @Order(6)
    public void testCartAddRemove() {
        loginValid();

        // Ensure cart is empty
        Assertions.assertEquals(0, getCartBadgeCount(), "Cart should be initially empty.");

        // Add one item
        WebElement firstAddBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("button[data-test='add-to-cart-sauce-labs-backpack']")));
        firstAddBtn.click();
        Assertions.assertEquals(1, getCartBadgeCount(), "Cart badge should be 1 after adding one item.");

        // Remove item
        WebElement removeBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("button[data-test='remove-sauce-labs-backpack']")));
        removeBtn.click();
        Assertions.assertEquals(0, getCartBadgeCount(), "Cart badge should be 0 after removing item.");
    }

    // TEST 7: External About link from burger menu
    @Test
    @Order(7)
    public void testExternalAboutLink() {
        loginValid();
        clickMenuItem("about_sidebar_link");
        String originalWindow = driver.getWindowHandle();
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String handle : driver.getWindowHandles()) {
            if (!handle.equals(originalWindow)) {
                driver.switchTo().window(handle);
                Assertions.assertTrue(driver.getCurrentUrl().contains("saucelabs.com"),
                        "About link should contain saucelabs.com URL.");
                driver.close();
                driver.switchTo().window(originalWindow);
            }
        }
    }

    // TEST 8: Footer social links (external)
    @Test
    @Order(8)
    public void testFooterSocialLinks() {
        loginValid();
        // Footer social links may be the same on login page; select by link text
        List<WebElement> socialLinks = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
                By.cssSelector(".social_link")));
        Assertions.assertFalse(socialLinks.isEmpty(), "No social links found in footer.");

        String originalWindow = driver.getWindowHandle();
        for (WebElement link : socialLinks) {
            String href = link.getAttribute("href");
            link.click();
            wait.until(ExpectedConditions.numberOfWindowsToBe(2));
            for (String handle : driver.getWindowHandles()) {
                if (!handle.equals(originalWindow)) {
                    driver.switchTo().window(handle);
                    Assertions.assertTrue(driver.getCurrentUrl().contains(href),
                            String.format("Opened link %s does not match expected href %s", driver.getCurrentUrl(), href));
                    driver.close();
                    driver.switchTo().window(originalWindow);
                }
            }
        }
    }
}