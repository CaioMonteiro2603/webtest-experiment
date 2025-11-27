package SunaQwen3.ws01.seq06;

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

@TestMethodOrder(OrderAnnotation.class)
public class SwagLabsTestSuite {
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://www.saucedemo.com/v1/index.html";
    private static final String LOGIN = "standard_user";
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
    void testValidLoginSuccess() {
        driver.get(BASE_URL);
        login(LOGIN, PASSWORD);

        // Assert successful login by checking URL and presence of inventory container
        wait.until(ExpectedConditions.urlContains("inventory.html"));
        assertTrue(driver.getCurrentUrl().contains("inventory.html"), "URL should contain inventory.html after login");

        WebElement inventoryContainer = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("inventory_container")));
        assertNotNull(inventoryContainer, "Inventory container should be visible after login");
    }

    @Test
    @Order(2)
    void testInvalidLoginError() {
        driver.get(BASE_URL);
        login("invalid_user", "invalid_password");

        // Assert error message is displayed
        WebElement errorButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error-button")));
        WebElement errorMessage = driver.findElement(By.cssSelector(".error-message-container h3"));
        assertEquals("Epic sadface: Username and password do not match any user in this service",
                errorMessage.getText(), "Error message should indicate invalid credentials");
    }

    @Test
    @Order(3)
    void testSortingOptions() {
        // Ensure we're logged in
        if (!driver.getCurrentUrl().contains("inventory.html")) {
            driver.get(BASE_URL);
            login(LOGIN, PASSWORD);
            wait.until(ExpectedConditions.urlContains("inventory.html"));
        }

        WebElement sortDropdown = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".product_sort_container")));

        // Test Name (A to Z)
        sortDropdown.click();
        driver.findElement(By.cssSelector("option[value='az']")).click();
        wait.until(ExpectedConditions.textToBePresentInElement(sortDropdown, "Name (A to Z)"));
        List<WebElement> items = driver.findElements(By.cssSelector(".inventory_item_name"));
        assertOrderedAscending(items, "Item names should be sorted A to Z");

        // Test Name (Z to A)
        sortDropdown.click();
        driver.findElement(By.cssSelector("option[value='za']")).click();
        wait.until(ExpectedConditions.textToBePresentInElement(sortDropdown, "Name (Z to A)"));
        items = driver.findElements(By.cssSelector(".inventory_item_name"));
        assertOrderedDescending(items, "Item names should be sorted Z to A");

        // Test Price (low to high)
        sortDropdown.click();
        driver.findElement(By.cssSelector("option[value='lohi']")).click();
        wait.until(ExpectedConditions.textToBePresentInElement(sortDropdown, "Price (low to high)"));
        List<WebElement> prices = driver.findElements(By.cssSelector(".inventory_item_price"));
        assertPricesOrderedLowToHigh(prices, "Prices should be sorted low to high");

        // Test Price (high to low)
        sortDropdown.click();
        driver.findElement(By.cssSelector("option[value='hilo']")).click();
        wait.until(ExpectedConditions.textToBePresentInElement(sortDropdown, "Price (high to low)"));
        prices = driver.findElements(By.cssSelector(".inventory_item_price"));
        assertPricesOrderedHighToLow(prices, "Prices should be sorted high to low");
    }

    @Test
    @Order(4)
    void testMenuNavigationAndActions() {
        // Ensure we're on inventory page
        if (!driver.getCurrentUrl().contains("inventory.html")) {
            driver.get(BASE_URL);
            login(LOGIN, PASSWORD);
            wait.until(ExpectedConditions.urlContains("inventory.html"));
        }

        // Open menu
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("menu_button")));
        menuButton.click();

        // Click All Items
        WebElement allItems = wait.until(ExpectedConditions.elementToBeClickable(By.id("inventory_sidebar_link")));
        allItems.click();
        wait.until(ExpectedConditions.urlContains("inventory.html"));
        assertTrue(driver.getCurrentUrl().contains("inventory.html"), "Should remain on inventory page after clicking All Items");

        // Open menu again
        menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("menu_button")));
        menuButton.click();

        // Click About (external)
        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("about_sidebar_link")));
        aboutLink.click();

        // Switch to new tab
        String originalHandle = driver.getWindowHandle();
        String newHandle = wait.until(d -> {
            Set<String> handles = d.getWindowHandles();
            handles.remove(originalHandle);
            return handles.isEmpty() ? null : handles.iterator().next();
        });
        assertNotNull(newHandle, "New tab should open for About link");
        driver.switchTo().window(newHandle);

        // Assert URL contains expected domain
        assertTrue(driver.getCurrentUrl().contains("saucelabs.com"), "About link should redirect to saucelabs.com domain");

        // Close new tab and switch back
        driver.close();
        driver.switchTo().window(originalHandle);

        // Verify back on inventory page
        assertTrue(driver.getCurrentUrl().contains("inventory.html"), "Should return to inventory page after closing About tab");

        // Open menu again
        menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("menu_button")));
        menuButton.click();

        // Click Reset App State
        WebElement resetButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link")));
        resetButton.click();

        // Wait for menu to close (indirect confirmation)
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("reset_sidebar_link")));

        // Add to cart some items
        List<WebElement> addToCartButtons = driver.findElements(By.cssSelector(".btn_primary.btn_inventory"));
        if (addToCartButtons.size() > 0) {
            addToCartButtons.get(0).click();
        }

        // Open menu again
        menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("menu_button")));
        menuButton.click();

        // Click Reset App State again
        resetButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link")));
        resetButton.click();

        // Wait for menu to close
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("reset_sidebar_link")));

        // Verify cart is empty
        List<WebElement> cartBadge = driver.findElements(By.cssSelector(".shopping_cart_badge"));
        assertEquals(0, cartBadge.size(), "Cart badge should not be present after reset");

        // Open menu again
        menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("menu_button")));
        menuButton.click();

        // Click Logout
        WebElement logoutButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("logout_sidebar_link")));
        logoutButton.click();

        // Assert back to login page
        wait.until(ExpectedConditions.urlToBe(BASE_URL));
        assertEquals(BASE_URL, driver.getCurrentUrl(), "Should be redirected to login page after logout");
    }

    @Test
    @Order(5)
    void testFooterExternalLinks() {
        // Re-login if needed
        if (!driver.getCurrentUrl().equals(BASE_URL)) {
            driver.get(BASE_URL);
        }

        login(LOGIN, PASSWORD);
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
        // Ensure logged in
        if (!driver.getCurrentUrl().contains("inventory.html")) {
            driver.get(BASE_URL);
            login(LOGIN, PASSWORD);
            wait.until(ExpectedConditions.urlContains("inventory.html"));
        }

        // Reset app state via menu
        resetAppState();

        // Add first item to cart
        List<WebElement> addToCartButtons = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(".btn_primary.btn_inventory")));
        assertTrue(addToCartButtons.size() > 0, "At least one item should be available to add to cart");
        String firstItemName = driver.findElements(By.cssSelector(".inventory_item_name")).get(0).getText();
        addToCartButtons.get(0).click();

        // Verify cart badge shows 1
        WebElement cartBadge = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".shopping_cart_badge")));
        assertEquals("1", cartBadge.getText(), "Cart badge should show 1 after adding first item");

        // Add second item to cart
        List<WebElement> remainingAddButtons = driver.findElements(By.cssSelector(".btn_primary.btn_inventory"));
        if (remainingAddButtons.size() > 0) {
            remainingAddButtons.get(0).click();
        }

        // Verify cart badge shows 2
        cartBadge = wait.until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector(".shopping_cart_badge"), "2"));
        assertEquals("2", cartBadge.getText(), "Cart badge should show 2 after adding second item");

        // Go to cart
        WebElement cartLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".shopping_cart_link")));
        cartLink.click();

        // Verify items in cart
        List<WebElement> cartItems = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(".cart_item")));
        assertEquals(2, cartItems.size(), "Cart should contain 2 items");

        // Remove one item
        List<WebElement> removeButtons = driver.findElements(By.cssSelector(".btn_secondary.cart_button"));
        assertTrue(removeButtons.size() > 0, "At least one remove button should be present");
        removeButtons.get(0).click();

        // Wait for removal
        wait.until(ExpectedConditions.numberOfElementsToBe(By.cssSelector(".cart_item"), 1));

        // Verify cart badge shows 1
        cartBadge = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".shopping_cart_badge")));
        assertEquals("1", cartBadge.getText(), "Cart badge should show 1 after removing one item");
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

    private void resetAppState() {
        // Open menu
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("menu_button")));
        menuButton.click();

        // Click Reset App State
        WebElement resetButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link")));
        resetButton.click();

        // Wait for menu to close
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("reset_sidebar_link")));
    }

    private void testExternalLink(By locator, String expectedDomain) {
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(locator));
        String originalHandle = driver.getWindowHandle();

        // Click link that opens new tab
        link.click();

        // Switch to new tab
        String newHandle = wait.until(d -> {
            Set<String> handles = d.getWindowHandles();
            handles.remove(originalHandle);
            return handles.isEmpty() ? null : handles.iterator().next();
        });
        assertNotNull(newHandle, "New tab should open for external link");
        driver.switchTo().window(newHandle);

        // Assert URL contains expected domain
        assertTrue(driver.getCurrentUrl().contains(expectedDomain), "External link should redirect to " + expectedDomain);

        // Close new tab and switch back
        driver.close();
        driver.switchTo().window(originalHandle);
    }

    private void assertOrderedAscending(List<WebElement> elements, String message) {
        String previousText = "";
        for (WebElement element : elements) {
            String currentText = element.getText();
            assertTrue(previousText.compareTo(currentText) <= 0, message + " - '" + previousText + "' should come before '" + currentText + "'");
            previousText = currentText;
        }
    }

    private void assertOrderedDescending(List<WebElement> elements, String message) {
        String previousText = "";
        for (WebElement element : elements) {
            String currentText = element.getText();
            if (!previousText.isEmpty()) {
                assertTrue(previousText.compareTo(currentText) >= 0, message + " - '" + previousText + "' should come after '" + currentText + "'");
            }
            previousText = currentText;
        }
    }

    private void assertPricesOrderedLowToHigh(List<WebElement> priceElements, String message) {
        double previousPrice = 0;
        for (WebElement element : priceElements) {
            double currentPrice = parsePrice(element.getText());
            assertTrue(previousPrice <= currentPrice, message + " - " + previousPrice + " should be <= " + currentPrice);
            previousPrice = currentPrice;
        }
    }

    private void assertPricesOrderedHighToLow(List<WebElement> priceElements, String message) {
        double previousPrice = Double.MAX_VALUE;
        for (WebElement element : priceElements) {
            double currentPrice = parsePrice(element.getText());
            assertTrue(previousPrice >= currentPrice, message + " - " + previousPrice + " should be >= " + currentPrice);
            previousPrice = currentPrice;
        }
    }

    private double parsePrice(String priceText) {
        return Double.parseDouble(priceText.replaceAll("[^\\d.]", ""));
    }
}