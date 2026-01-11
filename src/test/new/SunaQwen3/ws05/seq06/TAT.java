package SunaQwen3.ws05.seq06;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(OrderAnnotation.class)
public class TAT {
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://www.saucedemo.com/";
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
        driver.get(BASE_URL);
        assertEquals("Swag Labs", driver.getTitle(), "Page title should be 'Swag Labs'");

        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("user-name")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("login-button"));

        usernameField.sendKeys(USERNAME);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("inventory.html"));
        assertTrue(driver.getCurrentUrl().contains("inventory.html"), "URL should contain inventory.html after login");
        assertTrue(driver.findElement(By.cssSelector(".inventory_list")).isDisplayed(), "Inventory list should be displayed");
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

        WebElement errorElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-test='error']")));
        assertTrue(errorElement.isDisplayed(), "Error message container should be displayed");
        assertTrue(errorElement.getText().contains("Epic sadface"), "Error message should contain 'Epic sadface'");
    }

    @Test
    @Order(3)
    public void testSortingDropdown() {
        loginIfNecessary();

        WebElement sortDropdown = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".product_sort_container")));
        sortDropdown.click();

        // Test A to Z
        sortDropdown.sendKeys("Name (A to Z)");
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector(".product_sort_container"), "Name (A to Z)"));
        String firstItemAtoZ = driver.findElements(By.cssSelector(".inventory_item_name")).get(0).getText();
        assertNotEquals("", firstItemAtoZ, "First item name should not be empty in A to Z sort");

        // Test Z to A
        sortDropdown.sendKeys("Name (Z to A)");
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector(".product_sort_container"), "Name (Z to A)"));
        String firstItemZtoA = driver.findElements(By.cssSelector(".inventory_item_name")).get(0).getText();
        assertNotEquals("", firstItemZtoA, "First item name should not be empty in Z to A sort");
        assertNotEquals(firstItemAtoZ, firstItemZtoA, "First item should change when sorting from A to Z to Z to A");

        // Test Low to High
        sortDropdown.sendKeys("Price (low to high)");
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector(".product_sort_container"), "Price (low to high)"));
        String firstPriceLowHigh = driver.findElements(By.cssSelector(".inventory_item_price")).get(0).getText();
        assertNotEquals("", firstPriceLowHigh, "First item price should not be empty in low to high sort");

        // Test High to Low
        sortDropdown.sendKeys("Price (high to low)");
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector(".product_sort_container"), "Price (high to low)"));
        String firstPriceHighLow = driver.findElements(By.cssSelector(".inventory_item_price")).get(0).getText();
        assertNotEquals("", firstPriceHighLow, "First item price should not be empty in high to low sort");
        assertNotEquals(firstPriceLowHigh, firstPriceHighLow, "First item price should change when sorting from low to high to high to low");
    }

    @Test
    @Order(4)
    public void testMenuFunctionality() {
        loginIfNecessary();

        // Open menu
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuButton.click();

        // Wait for menu to open
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".bm-menu")));

        // Click All Items
        WebElement allItems = wait.until(ExpectedConditions.elementToBeClickable(By.id("inventory_sidebar_link")));
        allItems.click();
        wait.until(ExpectedConditions.urlContains("inventory.html"));
        assertTrue(driver.getCurrentUrl().contains("inventory.html"), "URL should contain inventory.html after clicking All Items");

        // Open menu again
        menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuButton.click();

        // Click About (external)
        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("about_sidebar_link")));
        aboutLink.click();

        // Switch to new tab
        String originalWindow = driver.getWindowHandle();
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        assertTrue(driver.getCurrentUrl().contains("saucelabs.com"), "About link should redirect to saucelabs.com domain");
        driver.close();
        driver.switchTo().window(originalWindow);

        // Open menu again
        menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuButton.click();

        // Click Reset App State
        WebElement resetButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link")));
        resetButton.click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.className("bm-menu")));

        // Verify inventory is still present
        assertTrue(driver.findElement(By.cssSelector(".inventory_list")).isDisplayed(), "Inventory list should still be displayed after reset");

        // Open menu again
        menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuButton.click();

        // Click Logout
        WebElement logoutButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("logout_sidebar_link")));
        logoutButton.click();
        wait.until(ExpectedConditions.urlContains("index.html"));
        assertTrue(driver.getCurrentUrl().contains("index.html"), "URL should contain index.html after logout");
        assertTrue(driver.findElement(By.id("login-button")).isDisplayed(), "Login button should be displayed after logout");
    }

    @Test
    @Order(5)
    public void testFooterSocialLinks() {
        loginIfNecessary();

        // Test Twitter link
        WebElement twitterLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".social_twitter a")));
        twitterLink.click();

        String originalWindow = driver.getWindowHandle();
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        assertTrue(driver.getCurrentUrl().contains("twitter.com"), "Twitter link should redirect to twitter.com domain");
        driver.close();
        driver.switchTo().window(originalWindow);

        // Test Facebook link
        WebElement facebookLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".social_facebook a")));
        facebookLink.click();

        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        assertTrue(driver.getCurrentUrl().contains("facebook.com"), "Facebook link should redirect to facebook.com domain");
        driver.close();
        driver.switchTo().window(originalWindow);

        // Test LinkedIn link
        WebElement linkedinLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".social_linkedin a")));
        linkedinLink.click();

        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        assertTrue(driver.getCurrentUrl().contains("linkedin.com"), "LinkedIn link should redirect to linkedin.com domain");
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(6)
    public void testAddRemoveItemsFromCart() {
        loginIfNecessary();

        // Add first item to cart
        WebElement addToCartButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".inventory_item:first-child .btn_inventory")));
        addToCartButton.click();

        // Verify cart badge appears with 1
        WebElement cartBadge = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".shopping_cart_badge")));
        assertEquals("1", cartBadge.getText(), "Cart badge should show 1 item");

        // Remove item from cart
        WebElement removeFromCartButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".inventory_item:first-child .btn_inventory")));
        removeFromCartButton.click();

        // Verify cart badge disappears
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".shopping_cart_badge")));
        assertEquals(0, driver.findElements(By.cssSelector(".shopping_cart_badge")).size(), "Cart badge should not be present after removing item");
    }

    @Test
    @Order(7)
    public void testCheckoutProcess() {
        loginIfNecessary();

        // Add an item to cart
        WebElement addToCartButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".inventory_item:first-child .btn_inventory")));
        addToCartButton.click();

        // Go to cart
        WebElement cartLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".shopping_cart_link")));
        cartLink.click();
        wait.until(ExpectedConditions.urlContains("cart.html"));

        // Click checkout
        WebElement checkoutButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("checkout")));
        checkoutButton.click();
        wait.until(ExpectedConditions.urlContains("checkout-step-one.html"));

        // Fill in checkout info
        WebElement firstNameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("first-name")));
        WebElement lastNameField = driver.findElement(By.id("last-name"));
        WebElement zipCodeField = driver.findElement(By.id("postal-code"));

        firstNameField.sendKeys("John");
        lastNameField.sendKeys("Doe");
        zipCodeField.sendKeys("12345");

        WebElement continueButton = driver.findElement(By.id("continue"));
        continueButton.click();
        wait.until(ExpectedConditions.urlContains("checkout-step-two.html"));

        // Click finish
        WebElement finishButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("finish")));
        finishButton.click();
        wait.until(ExpectedConditions.urlContains("checkout-complete.html"));

        // Verify success message
        WebElement completeHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".complete-header")));
        assertTrue(completeHeader.isDisplayed(), "Complete header should be displayed");
        assertEquals("Thank you for your order!", completeHeader.getText(), "Complete header should contain thank you message");
    }

    private void loginIfNecessary() {
        if (driver.getCurrentUrl().contains("index.html") || driver.getCurrentUrl().equals(BASE_URL)) {
            driver.get(BASE_URL);
            WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("user-name")));
            WebElement passwordField = driver.findElement(By.id("password"));
            WebElement loginButton = driver.findElement(By.id("login-button"));

            usernameField.sendKeys(USERNAME);
            passwordField.sendKeys(PASSWORD);
            loginButton.click();

            wait.until(ExpectedConditions.urlContains("inventory.html"));
        }
    }
}