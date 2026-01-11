package geminiPro.ws01.seq07;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
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
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A comprehensive JUnit 5 test suite for the Sauce Demo website.
 * This suite covers login, product sorting, cart functionality, the full checkout process,
 * menu navigation, and validation of external social media links.
 * It uses Selenium WebDriver with Firefox running in headless mode.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class saucedemo {

    private static final String BASE_URL = "https://www.saucedemo.com/v1/index.html";
    private static final String USERNAME = "standard_user";
    private static final String PASSWORD = "secret_sauce";
    private static final Duration WAIT_TIMEOUT = Duration.ofSeconds(10);

    private static WebDriver driver;
    private static WebDriverWait wait;

    // Locators
    private final By usernameField = By.id("user-name");
    private final By passwordField = By.id("password");
    private final By loginButton = By.id("login-button");
    private final By inventoryContainer = By.id("inventory_container");
    private final By burgerMenuButton = By.id("react-burger-menu-btn");
    private final By logoutLink = By.id("logout_sidebar_link");
    private final By resetAppStateLink = By.id("reset_sidebar_link");
    private final By shoppingCartLink = By.id("shopping_cart_container");
    private final By shoppingCartBadge = By.cssSelector(".shopping_cart_badge");
    private final By sortDropdown = By.className("product_sort_container");

    @BeforeAll
    static void setupAll() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless"); // Use arguments for headless mode as required
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
        // Navigate to the login page before each test, unless already there
        if (!driver.getCurrentUrl().equals(BASE_URL) && !isLoggedIn()) {
            driver.get(BASE_URL);
        }
    }

    @Test
    @Order(1)
    void testInvalidLogin() {
        driver.get(BASE_URL);
        performLogin("wrong_user", "wrong_password");
        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h3[data-test='error']")));
        assertTrue(errorMessage.getText().contains("Username and password do not match"), "Error message for invalid login should be displayed.");
    }

    @Test
    @Order(2)
    void testSuccessfulLoginAndLogout() {
        driver.get(BASE_URL);
        performLogin(USERNAME, PASSWORD);
        assertTrue(driver.getCurrentUrl().contains("inventory.html"), "Login should redirect to the inventory page.");
        wait.until(ExpectedConditions.visibilityOfElementLocated(inventoryContainer));

        performLogout();
        wait.until(ExpectedConditions.visibilityOfElementLocated(loginButton));
        assertEquals(BASE_URL, driver.getCurrentUrl(), "Logout should return to the login page.");
    }

    @Test
    @Order(3)
    void testProductSortByNameAscending() {
        ensureLoggedIn();
        sortProductsBy("az"); // Name (A to Z)
        List<String> productNames = getProductNames();
        List<String> sortedNames = new ArrayList<>(productNames);
        Collections.sort(sortedNames);
        assertEquals(sortedNames, productNames, "Products should be sorted by name in ascending order.");
    }

    @Test
    @Order(4)
    void testProductSortByNameDescending() {
        ensureLoggedIn();
        sortProductsBy("za"); // Name (Z to A)
        List<String> productNames = getProductNames();
        List<String> sortedNames = new ArrayList<>(productNames);
        sortedNames.sort(Collections.reverseOrder());
        assertEquals(sortedNames, productNames, "Products should be sorted by name in descending order.");
    }

    @Test
    @Order(5)
    void testProductSortByPriceAscending() {
        ensureLoggedIn();
        sortProductsBy("lohi"); // Price (low to high)
        List<Double> productPrices = getProductPrices();
        List<Double> sortedPrices = new ArrayList<>(productPrices);
        Collections.sort(sortedPrices);
        assertEquals(sortedPrices, productPrices, "Products should be sorted by price in ascending order.");
    }

    @Test
    @Order(6)
    void testProductSortByPriceDescending() {
        ensureLoggedIn();
        sortProductsBy("hilo"); // Price (high to low)
        List<Double> productPrices = getProductPrices();
        List<Double> sortedPrices = new ArrayList<>(productPrices);
        sortedPrices.sort(Collections.reverseOrder());
        assertEquals(sortedPrices, productPrices, "Products should be sorted by price in descending order.");
    }

    @Test
    @Order(7)
    void testAddToCartAndRemoveFromCartPage() {
        ensureLoggedIn();
        resetAppState(); // Ensure clean state

        // Add item to cart from inventory page
        WebElement addToCartButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("add-to-cart-sauce-labs-backpack")));
        addToCartButton.click();

        // Assert cart badge shows "1"
        WebElement cartBadge = wait.until(ExpectedConditions.visibilityOfElementLocated(shoppingCartBadge));
        assertEquals("1", cartBadge.getText(), "Cart badge should display '1' after adding an item.");

        // Go to cart and verify item presence
        driver.findElement(shoppingCartLink).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("cart_item")));
        assertEquals(1, driver.findElements(By.className("cart_item")).size(), "There should be one item in the cart.");

        // Remove item from cart page
        WebElement removeButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("remove-sauce-labs-backpack")));
        removeButton.click();

        // Assert cart is now empty
        assertEquals(0, driver.findElements(By.className("cart_item")).size(), "Cart should be empty after removing the item.");
        assertFalse(isElementPresent(shoppingCartBadge), "Cart badge should disappear when cart is empty.");
        
        // Return to a known state
        driver.get(BASE_URL.replace("index.html", "inventory.html"));
    }

    @Test
    @Order(8)
    void testFullCheckoutFlow() {
        ensureLoggedIn();
        resetAppState();

        // Add item and go to cart
        driver.findElement(By.id("add-to-cart-sauce-labs-onesie")).click();
        driver.findElement(shoppingCartLink).click();
        wait.until(ExpectedConditions.urlContains("cart.html"));

        // Proceed to checkout
        driver.findElement(By.id("checkout")).click();
        wait.until(ExpectedConditions.urlContains("checkout-step-one.html"));

        // Fill checkout information
        driver.findElement(By.id("first-name")).sendKeys("Gemini");
        driver.findElement(By.id("last-name")).sendKeys("Pro");
        driver.findElement(By.id("postal-code")).sendKeys("12345");
        driver.findElement(By.id("continue")).click();
        wait.until(ExpectedConditions.urlContains("checkout-step-two.html"));

        // Finish checkout
        driver.findElement(By.id("finish")).click();
        wait.until(ExpectedConditions.urlContains("checkout-complete.html"));

        // Assert success message
        WebElement successHeader = driver.findElement(By.className("complete-header"));
        assertEquals("Thank you for your order!", successHeader.getText(), "Checkout completion message should be visible.");

        // Reset state for next tests
        resetAppState();
        driver.get(BASE_URL.replace("index.html", "inventory.html"));
    }
    
    @Test
    @Order(9)
    void testBurgerMenuAboutLink() {
        ensureLoggedIn();
        String originalWindow = driver.getWindowHandle();
        
        openBurgerMenu();
        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("about_sidebar_link")));
        aboutLink.click();
        
        handleExternalLink("saucelabs.com", originalWindow);
    }

    @Test
    @Order(10)
    void testFooterSocialLinks() {
        ensureLoggedIn();
        String originalWindow = driver.getWindowHandle();
        
        // Test Twitter
        driver.findElement(By.linkText("Twitter")).click();
        handleExternalLink("x.com", originalWindow);
        
        // Test Facebook
        driver.findElement(By.linkText("Facebook")).click();
        handleExternalLink("facebook.com/saucelabs", originalWindow);
        
        // Test LinkedIn
        driver.findElement(By.linkText("LinkedIn")).click();
        handleExternalLink("linkedin.com/company/sauce-labs", originalWindow);
    }
    
    // --- Helper Methods ---

    private void performLogin(String user, String pass) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(usernameField)).sendKeys(user);
        driver.findElement(passwordField).sendKeys(pass);
        driver.findElement(loginButton).click();
    }

    private void ensureLoggedIn() {
        if (!isLoggedIn()) {
            driver.get(BASE_URL);
            performLogin(USERNAME, PASSWORD);
            wait.until(ExpectedConditions.visibilityOfElementLocated(inventoryContainer));
        }
    }
    
    private boolean isLoggedIn() {
        return driver.getCurrentUrl().contains("inventory.html");
    }
    
    private void openBurgerMenu() {
        wait.until(ExpectedConditions.elementToBeClickable(burgerMenuButton)).click();
        // Wait for a menu item to be visible to ensure menu is open
        wait.until(ExpectedConditions.visibilityOfElementLocated(logoutLink));
    }

    private void performLogout() {
        openBurgerMenu();
        wait.until(ExpectedConditions.elementToBeClickable(logoutLink)).click();
    }
    
    private void resetAppState() {
        // App must be in a state where the menu is accessible
        if (!isLoggedIn()) return;
        openBurgerMenu();
        wait.until(ExpectedConditions.elementToBeClickable(resetAppStateLink)).click();
        // Close the menu by clicking the 'X' button
        wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-cross-btn"))).click();
        // Wait for menu to be fully closed, e.g., by waiting for inventory to be visible again
        wait.until(ExpectedConditions.visibilityOfElementLocated(inventoryContainer));
    }

    private void sortProductsBy(String value) {
        WebElement dropdown = wait.until(ExpectedConditions.visibilityOfElementLocated(sortDropdown));
        Select select = new Select(dropdown);
        select.selectByValue(value);
    }

    private List<String> getProductNames() {
        List<WebElement> items = driver.findElements(By.className("inventory_item_name"));
        return items.stream().map(WebElement::getText).collect(Collectors.toList());
    }

    private List<Double> getProductPrices() {
        List<WebElement> items = driver.findElements(By.className("inventory_item_price"));
        return items.stream()
            .map(el -> Double.parseDouble(el.getText().replace("$", "")))
            .collect(Collectors.toList());
    }
    
    private boolean isElementPresent(By locator) {
        try {
            driver.findElement(locator);
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }
    
    private void handleExternalLink(String expectedDomain, String originalWindowHandle) {
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindowHandle.contentEquals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        wait.until(ExpectedConditions.urlContains(expectedDomain));
        assertTrue(driver.getCurrentUrl().contains(expectedDomain), "URL should contain " + expectedDomain);
        
        driver.close();
        driver.switchTo().window(originalWindowHandle);
        assertTrue(driver.getCurrentUrl().contains("inventory.html"), "Should have returned to the inventory page.");
    }
}