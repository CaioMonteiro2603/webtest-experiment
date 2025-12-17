package SunaQwen3.ws05.seq08;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(OrderAnnotation.class)
public class TAT {
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://cac-tat.s3.eu-central-1.amazonaws.com/index.html";
    private static final String LOGIN_URL = BASE_URL;
    private static final String USERNAME = "standard_user";
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
    public void testValidLogin() {
        driver.get(LOGIN_URL);
        assertEquals("Swag Labs", driver.getTitle(), "Page title should be 'Swag Labs'");

        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("user-name")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("login-button"));

        usernameField.sendKeys(USERNAME);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("inventory.html"));
        assertTrue(driver.getCurrentUrl().contains("inventory.html"), "URL should contain 'inventory.html' after login");
        assertTrue(driver.findElement(By.cssSelector(".inventory_list")).isDisplayed(), "Inventory list should be displayed");
        assertEquals("Products", driver.findElement(By.cssSelector(".title")).getText(), "Page should display 'Products' header");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(LOGIN_URL);

        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("user-name")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("login-button"));

        usernameField.sendKeys("invalid_user");
        passwordField.sendKeys("invalid_password");
        loginButton.click();

        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error-message-container")));
        assertTrue(errorMessage.isDisplayed(), "Error message container should be displayed");
        assertTrue(errorMessage.getText().contains("Epic sadface"), "Error message should contain 'Epic sadface'");
    }

    @Test
    @Order(3)
    public void testSortingDropdown() {
        performLogin();

        WebElement sortDropdown = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".product_sort_container")));
        sortDropdown.click();

        // Test Name (A to Z)
        sortDropdown.sendKeys("az");
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector(".active_option"), "Name (A to Z)"));
        List<WebElement> items = driver.findElements(By.cssSelector(".inventory_item_name"));
        assertTrue(items.size() > 0, "At least one item should be present");
        String firstItemText = items.get(0).getText();
        sortDropdown = driver.findElement(By.cssSelector(".product_sort_container"));
        assertEquals("Name (A to Z)", sortDropdown.getText(), "Sort option should be 'Name (A to Z)'");

        // Test Name (Z to A)
        sortDropdown.click();
        sortDropdown.sendKeys("za");
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector(".active_option"), "Name (Z to A)"));
        items = driver.findElements(By.cssSelector(".inventory_item_name"));
        String firstItemTextZA = items.get(0).getText();
        assertNotEquals(firstItemText, firstItemTextZA, "First item should change when sorting from Z to A");

        // Test Price (low to high)
        sortDropdown = driver.findElement(By.cssSelector(".product_sort_container"));
        sortDropdown.click();
        sortDropdown.sendKeys("lohi");
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector(".active_option"), "Price (low to high)"));
        List<WebElement> prices = driver.findElements(By.cssSelector(".inventory_item_price"));
        assertTrue(prices.size() > 0, "At least one price should be present");
        double firstPrice = Double.parseDouble(prices.get(0).getText().replace("$", ""));
        double secondPrice = Double.parseDouble(prices.get(1).getText().replace("$", ""));
        assertTrue(firstPrice <= secondPrice, "Prices should be sorted from low to high");

        // Test Price (high to low)
        sortDropdown = driver.findElement(By.cssSelector(".product_sort_container"));
        sortDropdown.click();
        sortDropdown.sendKeys("hilo");
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector(".active_option"), "Price (high to low)"));
        prices = driver.findElements(By.cssSelector(".inventory_item_price"));
        double firstPriceHiLo = Double.parseDouble(prices.get(0).getText().replace("$", ""));
        double secondPriceHiLo = Double.parseDouble(prices.get(1).getText().replace("$", ""));
        assertTrue(firstPriceHiLo >= secondPriceHiLo, "Prices should be sorted from high to low");
    }

    @Test
    @Order(4)
    public void testMenuNavigation() {
        performLogin();

        // Open menu
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuButton.click();

        // Click All Items
        WebElement allItemsLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("inventory_sidebar_link")));
        allItemsLink.click();
        wait.until(ExpectedConditions.urlContains("inventory.html"));
        assertTrue(driver.getCurrentUrl().contains("inventory.html"), "URL should contain 'inventory.html' after clicking All Items");
        assertTrue(driver.findElement(By.cssSelector(".inventory_list")).isDisplayed(), "Inventory list should be displayed");

        // Open menu again
        menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuButton.click();

        // Click About (external)
        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("about_sidebar_link")));
        aboutLink.click();

        // Switch to new tab
        String originalWindow = driver.getWindowHandle();
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        String newWindow = driver.getWindowHandles().stream()
                .filter(handle -> !handle.equals(originalWindow))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No new window appeared"));
        driver.switchTo().window(newWindow);

        // Assert URL contains expected domain
        assertTrue(driver.getCurrentUrl().contains("saucelabs.com"), "About link should open Saucelabs domain");
        driver.close();
        driver.switchTo().window(originalWindow);

        // Open menu again
        menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuButton.click();

        // Click Reset App State
        WebElement resetLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link")));
        resetLink.click();
        wait.until(ExpectedConditions.stalenessOf(resetLink));
        menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));

        // Open menu again to verify it's still functional
        menuButton.click();
        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("logout_sidebar_link")));
        assertTrue(logoutLink.isDisplayed(), "Logout link should be present after reset");

        // Click Logout
        logoutLink.click();
        wait.until(ExpectedConditions.urlToBe(LOGIN_URL));
        assertTrue(driver.getCurrentUrl().equals(LOGIN_URL), "Should return to login page after logout");
        assertTrue(driver.findElement(By.cssSelector("#login-button")).isDisplayed(), "Login button should be visible");
    }

    @Test
    @Order(5)
    public void testFooterExternalLinks() {
        performLogin();

        // Test Twitter link
        testExternalLink(By.cssSelector(".social_twitter"), "twitter.com");

        // Test Facebook link
        testExternalLink(By.cssSelector(".social_facebook"), "facebook.com");

        // Test LinkedIn link
        testExternalLink(By.cssSelector(".social_linkedin"), "linkedin.com");
    }

    @Test
    @Order(6)
    public void testAddRemoveCartItems() {
        performLogin();

        // Add first item to cart
        WebElement addToCartButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".inventory_item:first-child .btn_inventory")));
        addToCartButton.click();

        // Verify cart badge updates
        WebElement cartBadge = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".shopping_cart_badge")));
        assertEquals("1", cartBadge.getText(), "Cart badge should show 1 item");

        // Remove item from cart
        WebElement removeButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".inventory_item:first-child .btn_inventory")));
        removeButton.click();

        // Verify cart badge disappears
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".shopping_cart_badge")));
        List<WebElement> badges = driver.findElements(By.cssSelector(".shopping_cart_badge"));
        assertEquals(0, badges.size(), "Cart badge should not be present");
    }

    @Test
    @Order(7)
    public void testCheckoutProcess() {
        performLogin();

        // Add an item to cart
        WebElement addToCartButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".inventory_item:first-child .btn_inventory")));
        addToCartButton.click();

        // Go to cart
        WebElement cartLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".shopping_cart_link")));
        cartLink.click();
        wait.until(ExpectedConditions.urlContains("cart.html"));
        assertTrue(driver.getCurrentUrl().contains("cart.html"), "Should be on cart page");

        // Proceed to checkout
        WebElement checkoutButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#checkout")));
        checkoutButton.click();
        wait.until(ExpectedConditions.urlContains("checkout-step-one.html"));
        assertTrue(driver.getCurrentUrl().contains("checkout-step-one.html"), "Should be on checkout step one");

        // Fill in user information
        WebElement firstNameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("first-name")));
        WebElement lastNameField = driver.findElement(By.id("last-name"));
        WebElement zipCodeField = driver.findElement(By.id("postal-code"));

        firstNameField.sendKeys("John");
        lastNameField.sendKeys("Doe");
        zipCodeField.sendKeys("12345");

        WebElement continueButton = driver.findElement(By.cssSelector("#continue"));
        continueButton.click();

        // Wait for checkout step two
        wait.until(ExpectedConditions.urlContains("checkout-step-two.html"));
        assertTrue(driver.getCurrentUrl().contains("checkout-step-two.html"), "Should be on checkout step two");

        // Finish checkout
        WebElement finishButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#finish")));
        finishButton.click();

        // Verify completion
        wait.until(ExpectedConditions.urlContains("checkout-complete.html"));
        assertTrue(driver.getCurrentUrl().contains("checkout-complete.html"), "Should be on checkout complete page");
        WebElement completeHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".complete-header")));
        assertEquals("Thank you for your order!", completeHeader.getText(), "Completion message should be displayed");
    }

    private void performLogin() {
        driver.get(LOGIN_URL);
        if (driver.getCurrentUrl().equals(LOGIN_URL)) {
            WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("user-name")));
            WebElement passwordField = driver.findElement(By.id("password"));
            WebElement loginButton = driver.findElement(By.id("login-button"));

            usernameField.sendKeys(USERNAME);
            passwordField.sendKeys(PASSWORD);
            loginButton.click();

            wait.until(ExpectedConditions.urlContains("inventory.html"));
        }
    }

    private void testExternalLink(By locator, String expectedDomain) {
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(locator));
        link.click();

        // Switch to new tab
        String originalWindow = driver.getWindowHandle();
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        String newWindow = driver.getWindowHandles().stream()
                .filter(handle -> !handle.equals(originalWindow))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No new window appeared"));
        driver.switchTo().window(newWindow);

        // Assert URL contains expected domain
        assertTrue(driver.getCurrentUrl().contains(expectedDomain), "Link should open domain containing: " + expectedDomain);
        driver.close();
        driver.switchTo().window(originalWindow);
    }
}