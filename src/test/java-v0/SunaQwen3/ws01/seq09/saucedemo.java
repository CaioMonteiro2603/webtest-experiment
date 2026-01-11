package SunaQwen3.ws01.seq09;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(OrderAnnotation.class)
public class saucedemo {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://www.saucedemo.com/v1/index.html";
    private static final String LOGIN = "standard_user";
    private static final String PASSWORD = "secret_sauce";

    @BeforeAll
    public static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @Order(1)
    public void testValidLoginSuccess() {
        driver.get(BASE_URL);

        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("user-name")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("login-button"));

        usernameField.sendKeys(LOGIN);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();

        // Assert successful login by checking URL and presence of inventory container
        wait.until(ExpectedConditions.urlContains("inventory.html"));
        assertTrue(driver.getCurrentUrl().contains("inventory.html"), "URL should contain inventory.html after login");

        WebElement inventoryContainer = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("inventory_container")));
        assertTrue(inventoryContainer.isDisplayed(), "Inventory container should be displayed after login");
    }

    @Test
    @Order(2)
    public void testInvalidLoginError() {
        driver.get(BASE_URL);

        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("user-name")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("login-button"));

        usernameField.sendKeys("invalid_user");
        passwordField.sendKeys("invalid_password");
        loginButton.click();

        // Assert error message is displayed
        WebElement errorButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error-button")));
        assertTrue(errorButton.isDisplayed(), "Error button should be visible on failed login");

        String errorMessage = driver.findElement(By.cssSelector(".error-message-container")).getText();
        assertTrue(errorMessage.contains("Epic sadface"), "Error message should indicate login failure");
    }

    @Test
    @Order(3)
    public void testSortingDropdownOptions() {
        // Ensure we're logged in
        loginIfNecessary();

        // Wait for sorting dropdown to be present
        WebElement sortDropdown = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".product_sort_container")));

        // Test Name (A to Z)
        sortDropdown.click();
        sortDropdown.sendKeys("az");
        sortDropdown.sendKeys(Keys.RETURN);

        List<WebElement> items = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(".inventory_item_name")));
        String firstItemText = items.get(0).getText();
        String lastItemText = items.get(items.size() - 1).getText();
        assertTrue(firstItemText.compareTo(lastItemText) <= 0, "Items should be sorted A to Z");

        // Test Name (Z to A)
        sortDropdown = driver.findElement(By.cssSelector(".product_sort_container"));
        sortDropdown.click();
        sortDropdown.sendKeys("za");
        sortDropdown.sendKeys(Keys.RETURN);

        items = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(".inventory_item_name")));
        firstItemText = items.get(0).getText();
        lastItemText = items.get(items.size() - 1).getText();
        assertTrue(firstItemText.compareTo(lastItemText) >= 0, "Items should be sorted Z to A");

        // Test Price (Low to High)
        sortDropdown = driver.findElement(By.cssSelector(".product_sort_container"));
        sortDropdown.click();
        sortDropdown.sendKeys("lohi");
        sortDropdown.sendKeys(Keys.RETURN);

        List<WebElement> prices = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(".inventory_item_price")));
        double firstPrice = parsePrice(prices.get(0).getText());
        double lastPrice = parsePrice(prices.get(prices.size() - 1).getText());
        assertTrue(firstPrice <= lastPrice, "Items should be sorted low to high price");

        // Test Price (High to Low)
        sortDropdown = driver.findElement(By.cssSelector(".product_sort_container"));
        sortDropdown.click();
        sortDropdown.sendKeys("hilo");
        sortDropdown.sendKeys(Keys.RETURN);

        prices = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(".inventory_item_price")));
        firstPrice = parsePrice(prices.get(0).getText());
        lastPrice = parsePrice(prices.get(prices.size() - 1).getText());
        assertTrue(firstPrice >= lastPrice, "Items should be sorted high to low price");
    }

    @Test
    @Order(4)
    public void testMenuAllItemsNavigation() {
        loginIfNecessary();

        // Open menu
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("menu_button")));
        menuButton.click();

        // Click All Items
        WebElement allItemsLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("inventory_sidebar_link")));
        allItemsLink.click();

        // Assert we are back on inventory page
        wait.until(ExpectedConditions.urlContains("inventory.html"));
        assertTrue(driver.getCurrentUrl().contains("inventory.html"), "Should return to inventory page after clicking All Items");
    }

    @Test
    @Order(5)
    public void testMenuAboutExternalLink() {
        loginIfNecessary();

        // Open menu
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("menu_button")));
        menuButton.click();

        // Click About (external link)
        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("about_sidebar_link")));
        aboutLink.click();

        // Switch to new tab
        String originalWindow = driver.getWindowHandle();
        String newWindow = wait.until(d -> {
            Set<String> handles = d.getWindowHandles();
            handles.remove(originalWindow);
            return handles.isEmpty() ? null : handles.iterator().next();
        });

        assertNotNull(newWindow, "New tab should open for About link");
        driver.switchTo().window(newWindow);

        // Assert URL contains expected domain
        wait.until(d -> d.getCurrentUrl().contains("saucelabs.com"));
        assertTrue(driver.getCurrentUrl().contains("saucelabs.com"), "About link should redirect to saucelabs.com");

        // Close new tab and switch back
        driver.close();
        driver.switchTo().window(originalWindow);

        // Assert back on inventory page
        assertTrue(driver.getCurrentUrl().contains("inventory.html"), "Should be back on inventory page after closing About tab");
    }

    @Test
    @Order(6)
    public void testMenuResetAppState() {
        loginIfNecessary();

        // Add an item to cart first
        WebElement addToCartButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#add-to-cart-sauce-labs-backpack")));
        addToCartButton.click();

        // Verify cart badge appears
        WebElement cartBadge = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".shopping_cart_badge")));
        assertEquals("1", cartBadge.getText(), "Cart badge should show 1 item");

        // Open menu
        WebElement menuButton = driver.findElement(By.id("menu_button"));
        menuButton.click();

        // Click Reset App State
        WebElement resetLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link")));
        resetLink.click();

        // Close menu
        menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("menu_button")));
        menuButton.click();

        // Verify cart is empty
        List<WebElement> cartBadges = driver.findElements(By.cssSelector(".shopping_cart_badge"));
        assertTrue(cartBadges.isEmpty() || !cartBadges.get(0).isDisplayed(), "Cart badge should be gone after reset");
    }

    @Test
    @Order(7)
    public void testMenuLogout() {
        loginIfNecessary();

        // Open menu
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("menu_button")));
        menuButton.click();

        // Click Logout
        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("logout_sidebar_link")));
        logoutLink.click();

        // Assert redirected to login page
        wait.until(ExpectedConditions.urlToBe(BASE_URL));
        assertEquals(BASE_URL, driver.getCurrentUrl(), "Should be redirected to login page after logout");

        // Try to access inventory directly
        driver.get(BASE_URL.replace("index.html", "inventory.html"));
        assertEquals(BASE_URL, driver.getCurrentUrl(), "Should not be able to access inventory without login");
    }

    @Test
    @Order(8)
    public void testFooterSocialLinks() {
        loginIfNecessary();

        // Verify Twitter link opens in new tab with correct domain
        testExternalLinkInNewTab(By.cssSelector(".social_twitter"), "twitter.com");

        // Verify Facebook link opens in new tab with correct domain
        testExternalLinkInNewTab(By.cssSelector(".social_facebook"), "facebook.com");

        // Verify LinkedIn link opens in new tab with correct domain
        testExternalLinkInNewTab(By.cssSelector(".social_linkedin"), "linkedin.com");
    }

    @Test
    @Order(9)
    public void testAddRemoveItemFromCart() {
        loginIfNecessary();

        // Reset app state via menu to ensure clean state
        resetAppState();

        // Add first item to cart
        WebElement addToCartButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#add-to-cart-sauce-labs-backpack")));
        addToCartButton.click();

        // Verify cart badge shows 1
        WebElement cartBadge = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".shopping_cart_badge")));
        assertEquals("1", cartBadge.getText(), "Cart badge should show 1 after adding item");

        // Navigate to cart
        WebElement cartLink = driver.findElement(By.cssSelector(".shopping_cart_link"));
        cartLink.click();

        // Wait for cart page
        wait.until(ExpectedConditions.urlContains("cart.html"));

        // Remove item from cart
        WebElement removeButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#remove-sauce-labs-backpack")));
        removeButton.click();

        // Verify cart is empty
        List<WebElement> cartItems = driver.findElements(By.cssSelector(".cart_item"));
        assertTrue(cartItems.isEmpty(), "Cart should be empty after removing item");

        // Verify badge is gone
        List<WebElement> badges = driver.findElements(By.cssSelector(".shopping_cart_badge"));
        assertTrue(badges.isEmpty(), "Cart badge should be gone after removing last item");
    }

    private void loginIfNecessary() {
        if (!driver.getCurrentUrl().contains("inventory.html")) {
            driver.get(BASE_URL);
            WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("user-name")));
            WebElement passwordField = driver.findElement(By.id("password"));
            WebElement loginButton = driver.findElement(By.id("login-button"));

            usernameField.clear();
            passwordField.clear();
            usernameField.sendKeys(LOGIN);
            passwordField.sendKeys(PASSWORD);
            loginButton.click();

            wait.until(ExpectedConditions.urlContains("inventory.html"));
        }
    }

    private void resetAppState() {
        // Open menu
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("menu_button")));
        menuButton.click();

        // Click Reset App State
        WebElement resetLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link")));
        resetLink.click();

        // Close menu
        menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("menu_button")));
        menuButton.click();

        // Wait for inventory to reload
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("inventory_container")));
    }

    private double parsePrice(String priceText) {
        return Double.parseDouble(priceText.replace("$", ""));
    }

    private void testExternalLinkInNewTab(By locator, String expectedDomain) {
        String originalWindow = driver.getWindowHandle();

        // Find and click the link
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(locator));
        link.click();

        // Switch to new window
        String newWindow = wait.until(d -> {
            Set<String> handles = d.getWindowHandles();
            handles.remove(originalWindow);
            return handles.isEmpty() ? null : handles.iterator().next();
        });

        assertNotNull(newWindow, "New tab should open for external link");
        driver.switchTo().window(newWindow);

        // Assert domain
        wait.until(d -> d.getCurrentUrl().contains(expectedDomain));
        assertTrue(driver.getCurrentUrl().contains(expectedDomain), "External link should redirect to expected domain: " + expectedDomain);

        // Close new tab and switch back
        driver.close();
        driver.switchTo().window(originalWindow);
    }
}