package SunaQwen3.ws05.seq05;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import static org.junit.jupiter.api.Assertions.*;
import java.time.Duration;
import java.util.List;
import java.util.Set;

@TestMethodOrder(OrderAnnotation.class)
public class TAT {
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://cac-tat.s3.eu-central-1.amazonaws.com/index.html";
    private static final String USERNAME = "standard_user";
    private static final String PASSWORD = "secret_sauce";

    @BeforeAll
    static void setUp() {
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
    void testValidLogin() {
        driver.get(BASE_URL);
        login(USERNAME, PASSWORD);

        wait.until(ExpectedConditions.urlContains("inventory.html"));
        assertTrue(driver.getCurrentUrl().contains("inventory.html"), "Should be redirected to inventory page after login");

        WebElement inventoryContainer = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("inventory_container")));
        assertTrue(inventoryContainer.isDisplayed(), "Inventory container should be displayed");
    }

    @Test
    @Order(2)
    void testInvalidLogin() {
        driver.get(BASE_URL);
        login("invalid_user", PASSWORD);

        WebElement errorElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error-message-container")));
        assertTrue(errorElement.isDisplayed(), "Error message container should be displayed");
        assertTrue(errorElement.getText().contains("Username and password do not match"), "Error message should indicate invalid credentials");
    }

    @Test
    @Order(3)
    void testSortingDropdownOptions() {
        driver.get(BASE_URL);
        login(USERNAME, PASSWORD);
        wait.until(ExpectedConditions.urlContains("inventory.html"));

        By sortSelector = By.cssSelector(".product_sort_container");
        WebElement sortDropdown = wait.until(ExpectedConditions.visibilityOfElementLocated(sortSelector));

        // Test Name (A to Z)
        sortDropdown.click();
        driver.findElement(By.cssSelector("option[value='az']")).click();
        wait.until(ExpectedConditions.textToBePresentInElementLocated(sortSelector, "Name (A to Z)"));

        List<WebElement> items = driver.findElements(By.cssSelector(".inventory_item_name"));
        assertAll("Items should be sorted A to Z",
                () -> assertTrue(items.size() > 0, "Should have at least one item"),
                () -> assertTrue(items.get(0).getText().compareTo(items.get(items.size() - 1).getText()) <= 0,
                        "First item should come before last item alphabetically")
        );

        // Test Name (Z to A)
        sortDropdown = driver.findElement(sortSelector);
        sortDropdown.click();
        driver.findElement(By.cssSelector("option[value='za']")).click();
        wait.until(ExpectedConditions.textToBePresentInElementLocated(sortSelector, "Name (Z to A)"));

        // Test Price (low to high)
        sortDropdown = driver.findElement(sortSelector);
        sortDropdown.click();
        driver.findElement(By.cssSelector("option[value='lohi']")).click();
        wait.until(ExpectedConditions.textToBePresentInElementLocated(sortSelector, "Price (low to high)"));

        List<WebElement> prices = driver.findElements(By.cssSelector(".inventory_item_price"));
        assertAll("Prices should be sorted low to high",
                () -> assertTrue(prices.size() > 0, "Should have at least one price"),
                () -> {
                    double firstPrice = parsePrice(prices.get(0).getText());
                    double lastPrice = parsePrice(prices.get(prices.size() - 1).getText());
                    assertTrue(firstPrice <= lastPrice, "First price should be less than or equal to last price");
                }
        );

        // Test Price (high to low)
        sortDropdown = driver.findElement(sortSelector);
        sortDropdown.click();
        driver.findElement(By.cssSelector("option[value='hilo']")).click();
        wait.until(ExpectedConditions.textToBePresentInElementLocated(sortSelector, "Price (high to low)"));
    }

    @Test
    @Order(4)
    void testMenuNavigation() {
        driver.get(BASE_URL);
        login(USERNAME, PASSWORD);
        wait.until(ExpectedConditions.urlContains("inventory.html"));

        // Open menu
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuButton.click();

        // Click All Items
        WebElement allItemsLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("inventory_sidebar_link")));
        allItemsLink.click();
        wait.until(ExpectedConditions.urlContains("inventory.html"));
        assertTrue(driver.getCurrentUrl().contains("inventory.html"), "Should remain on inventory page after clicking All Items");

        // Open menu again
        menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuButton.click();

        // Click About (external)
        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("about_sidebar_link")));
        aboutLink.click();

        // Switch to new tab
        String originalWindow = driver.getWindowHandle();
        String newWindow = wait.until(d -> {
            Set<String> handles = d.getWindowHandles();
            handles.remove(originalWindow);
            return handles.size() > 0 ? handles.iterator().next() : null;
        });
        driver.switchTo().window(newWindow);

        assertTrue(driver.getCurrentUrl().contains("saucelabs.com"), "About link should open Sauce Labs website");

        // Close new tab and switch back
        driver.close();
        driver.switchTo().window(originalWindow);
        assertTrue(driver.getCurrentUrl().contains("inventory.html"), "Should return to inventory page after closing About tab");

        // Open menu again
        menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuButton.click();

        // Click Reset App State
        WebElement resetLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link")));
        resetLink.click();

        // Wait for menu to close
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("react-burger-cross-btn")));

        // Verify inventory is still present
        WebElement inventoryContainer = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("inventory_container")));
        assertTrue(inventoryContainer.isDisplayed(), "Inventory container should still be displayed after reset");

        // Open menu again
        menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuButton.click();

        // Click Logout
        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("logout_sidebar_link")));
        logoutLink.click();

        wait.until(ExpectedConditions.urlContains("index.html"));
        assertTrue(driver.getCurrentUrl().contains("index.html"), "Should be redirected to login page after logout");
    }

    @Test
    @Order(5)
    void testFooterSocialLinks() {
        driver.get(BASE_URL);
        login(USERNAME, PASSWORD);
        wait.until(ExpectedConditions.urlContains("inventory.html"));

        // Test Twitter link
        testExternalLink(By.cssSelector("a[href*='twitter.com']"), "twitter.com");

        // Test Facebook link
        testExternalLink(By.cssSelector("a[href*='facebook.com']"), "facebook.com");

        // Test LinkedIn link
        testExternalLink(By.cssSelector("a[href*='linkedin.com']"), "linkedin.com");
    }

    @Test
    @Order(6)
    void testAddRemoveItemsFromCart() {
        driver.get(BASE_URL);
        login(USERNAME, PASSWORD);
        wait.until(ExpectedConditions.urlContains("inventory.html"));

        // Add first item to cart
        List<WebElement> addToCartButtons = driver.findElements(By.cssSelector(".btn_inventory"));
        assertTrue(addToCartButtons.size() > 0, "Should have at least one item to add to cart");
        WebElement firstAddButton = addToCartButtons.get(0);
        firstAddButton.click();

        // Wait for button text to change to "Remove"
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector(".btn_inventory"), "Remove"));

        // Check cart badge
        WebElement cartBadge = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".shopping_cart_badge")));
        assertEquals("1", cartBadge.getText(), "Cart badge should show 1 item");

        // Remove item from cart
        WebElement removeButton = driver.findElement(By.cssSelector(".btn_inventory"));
        removeButton.click();

        // Wait for button text to change back to "Add to cart"
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector(".btn_inventory"), "Add to cart"));

        // Cart badge should disappear
        List<WebElement> badgeElements = driver.findElements(By.cssSelector(".shopping_cart_badge"));
        assertEquals(0, badgeElements.size(), "Cart badge should disappear when cart is empty");
    }

    private void login(String username, String password) {
        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("user-name")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("login-button"));

        usernameField.clear();
        usernameField.sendKeys(username);
        passwordField.clear();
        passwordField.sendKeys(password);
        loginButton.click();
    }

    private void testExternalLink(By locator, String expectedDomain) {
        String originalWindow = driver.getWindowHandle();

        // Find and click the link
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(locator));
        link.click();

        // Switch to new tab
        String newWindow = wait.until(d -> {
            Set<String> handles = d.getWindowHandles();
            handles.remove(originalWindow);
            return handles.size() > 0 ? handles.iterator().next() : null;
        });
        driver.switchTo().window(newWindow);

        // Assert URL contains expected domain
        assertTrue(driver.getCurrentUrl().contains(expectedDomain), 
                   "External link should navigate to expected domain: " + expectedDomain);

        // Close new tab and switch back
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    private double parsePrice(String priceText) {
        return Double.parseDouble(priceText.replace("$", ""));
    }
}