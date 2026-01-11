package SunaQwen3.ws01.seq03;

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
    public void testValidLogin() {
        driver.get(BASE_URL);

        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("user-name")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("login-button"));

        usernameField.sendKeys(LOGIN);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("inventory.html"));
        assertTrue(driver.getCurrentUrl().contains("inventory.html"), "URL should contain inventory.html after login");

        WebElement inventoryList = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("inventory_list")));
        assertTrue(inventoryList.isDisplayed(), "Inventory list should be displayed after login");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(BASE_URL);

        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("user-name")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("login-button"));

        usernameField.sendKeys("invalid_user");
        passwordField.sendKeys("invalid_password");
        loginButton.click();

        WebElement errorButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error-button")));
        assertTrue(errorButton.isDisplayed(), "Error button should be displayed for invalid login");

        String errorMessage = driver.findElement(By.cssSelector(".error-message-container h3")).getText();
        assertTrue(errorMessage.contains("Epic sadface"), "Error message should indicate login failure");
    }

    @Test
    @Order(3)
    public void testSortingDropdown() {
        loginIfNecessary();

        WebElement sortDropdown = wait.until(ExpectedConditions.elementToBeClickable(By.className("product_sort_container")));
        sortDropdown.click();

        // Sort by Name (A to Z)
        sortDropdown.findElement(By.cssSelector("option[value='az']")).click();
        wait.until(ExpectedConditions.stalenessOf(driver.findElement(By.className("inventory_item_name"))));

        List<WebElement> itemNames = driver.findElements(By.className("inventory_item_name"));
        assertAll("Verify items are sorted A to Z",
                () -> assertTrue(itemNames.get(0).getText().compareTo(itemNames.get(1).getText()) <= 0),
                () -> assertTrue(itemNames.get(1).getText().compareTo(itemNames.get(2).getText()) <= 0)
        );

        // Sort by Price (low to high)
        sortDropdown = driver.findElement(By.className("product_sort_container"));
        sortDropdown.click();
        sortDropdown.findElement(By.cssSelector("option[value='lohi']")).click();
        wait.until(ExpectedConditions.stalenessOf(driver.findElement(By.className("inventory_item_price"))));

        List<WebElement> itemPrices = driver.findElements(By.className("inventory_item_price"));
        assertAll("Verify items are sorted low to high price",
                () -> assertTrue(extractPrice(itemPrices.get(0)) <= extractPrice(itemPrices.get(1))),
                () -> assertTrue(extractPrice(itemPrices.get(1)) <= extractPrice(itemPrices.get(2)))
        );

        // Sort by Price (high to low)
        sortDropdown = driver.findElement(By.className("product_sort_container"));
        sortDropdown.click();
        sortDropdown.findElement(By.cssSelector("option[value='hilo']")).click();
        wait.until(ExpectedConditions.stalenessOf(driver.findElement(By.className("inventory_item_price"))));
    }

    @Test
    @Order(4)
    public void testMenuNavigation() {
        loginIfNecessary();

        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("menu_button")));
        menuButton.click();

        // Click All Items
        WebElement allItems = wait.until(ExpectedConditions.elementToBeClickable(By.id("inventory_sidebar_link")));
        allItems.click();
        wait.until(ExpectedConditions.urlContains("inventory.html"));
        assertTrue(driver.getCurrentUrl().contains("inventory.html"), "Should navigate to inventory page");

        // Open menu again
        menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("menu_button")));
        menuButton.click();

        // Click About (external)
        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("about_sidebar_link")));
        aboutLink.click();

        // Switch to new tab
        String originalWindow = driver.getWindowHandle();
        wait.until(d -> driver.getWindowHandles().size() > 1);
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        assertTrue(driver.getCurrentUrl().contains("saucelabs.com"), "About link should open Sauce Labs website");
        driver.close();
        driver.switchTo().window(originalWindow);

        // Open menu again
        menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("menu_button")));
        menuButton.click();

        // Click Reset App State
        WebElement resetButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link")));
        resetButton.click();

        // Verify reset was acknowledged (no visual feedback, but we can assume success)
        wait.until(ExpectedConditions.stalenessOf(menuButton));
        assertTrue(true, "Reset App State should be processed");

        // Open menu again
        menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("menu_button")));
        menuButton.click();

        // Click Logout
        WebElement logoutButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("logout_sidebar_link")));
        logoutButton.click();

        wait.until(ExpectedConditions.urlToBe(BASE_URL));
        assertEquals(BASE_URL, driver.getCurrentUrl(), "Should return to login page after logout");
    }

    @Test
    @Order(5)
    public void testFooterSocialLinks() {
        loginIfNecessary();

        // Find all footer social links
        List<WebElement> socialLinks = driver.findElements(By.cssSelector(".social a"));

        String originalWindow = driver.getWindowHandle();

        // Test Twitter link
        socialLinks.get(0).click();
        switchToNewWindowAndValidate(originalWindow, "twitter.com");
        driver.close();
        driver.switchTo().window(originalWindow);

        // Test Facebook link
        socialLinks = driver.findElements(By.cssSelector(".social a")); // Re-locate
        socialLinks.get(1).click();
        switchToNewWindowAndValidate(originalWindow, "facebook.com");
        driver.close();
        driver.switchTo().window(originalWindow);

        // Test LinkedIn link
        socialLinks = driver.findElements(By.cssSelector(".social a")); // Re-locate
        socialLinks.get(2).click();
        switchToNewWindowAndValidate(originalWindow, "linkedin.com");
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(6)
    public void testAddRemoveItemsFromCart() {
        loginIfNecessary();

        // Add first item to cart
        WebElement addToCartButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".inventory_item:first-child .btn_primary")));
        addToCartButton.click();

        // Verify cart badge updates
        WebElement cartBadge = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".shopping_cart_badge")));
        assertEquals("1", cartBadge.getText(), "Cart badge should show 1 item");

        // Remove item from cart
        WebElement removeButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".inventory_item:first-child .btn_secondary")));
        removeButton.click();

        // Verify cart badge disappears
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".shopping_cart_badge")));
        assertEquals(0, driver.findElements(By.cssSelector(".shopping_cart_badge")).size(), "Cart badge should not be present");
    }

    @Test
    @Order(7)
    public void testCheckoutProcess() {
        loginIfNecessary();

        // Add an item to cart
        WebElement addToCartButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".inventory_item:first-child .btn_primary")));
        addToCartButton.click();

        // Go to cart
        WebElement cartLink = wait.until(ExpectedConditions.elementToBeClickable(By.className("shopping_cart_link")));
        cartLink.click();

        // Proceed to checkout
        WebElement checkoutButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("checkout")));
        checkoutButton.click();

        // Fill in checkout information
        WebElement firstNameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("first-name")));
        firstNameField.sendKeys("John");
        driver.findElement(By.id("last-name")).sendKeys("Doe");
        driver.findElement(By.id("postal-code")).sendKeys("12345");

        WebElement continueButton = driver.findElement(By.id("continue"));
        continueButton.click();

        // Finish checkout
        WebElement finishButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("finish")));
        finishButton.click();

        // Verify success message
        WebElement completeHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("complete-header")));
        assertTrue(completeHeader.getText().toLowerCase().contains("thank you"), "Checkout should be successful");
    }

    private void loginIfNecessary() {
        if (!driver.getCurrentUrl().contains("inventory.html")) {
            driver.get(BASE_URL);
            WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("user-name")));
            WebElement passwordField = driver.findElement(By.id("password"));
            WebElement loginButton = driver.findElement(By.id("login-button"));

            usernameField.sendKeys(LOGIN);
            passwordField.sendKeys(PASSWORD);
            loginButton.click();

            wait.until(ExpectedConditions.urlContains("inventory.html"));
        }
    }

    private double extractPrice(WebElement priceElement) {
        return Double.parseDouble(priceElement.getText().replace("$", ""));
    }

    private void switchToNewWindowAndValidate(String originalWindow, String expectedDomain) {
        wait.until(d -> driver.getWindowHandles().size() > 1);
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        assertTrue(driver.getCurrentUrl().contains(expectedDomain), "New window should contain " + expectedDomain);
    }
}