package Qwen3.ws01.seq06;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(org.junit.jupiter.api.MethodOrderer.OrderAnnotation.class)
public class saucedemo {

    private static WebDriver driver;
    private static WebDriverWait wait;

    @BeforeAll
    public static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
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
        driver.get("https://www.saucedemo.com/v1/index.html");
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.id("user-name")));
        usernameField.sendKeys("standard_user");
        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.sendKeys("secret_sauce");
        WebElement loginButton = driver.findElement(By.id("login-button"));
        loginButton.click();

        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("inventory.html"), "Expected to be on inventory page after login");

        WebElement inventoryContainer = driver.findElement(By.id("inventory_container"));
        assertTrue(inventoryContainer.isDisplayed(), "Inventory container should be displayed");
    }

    @Test
    @Order(2)
    public void testInvalidLoginError() {
        driver.get("https://www.saucedemo.com/v1/index.html");
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.id("user-name")));
        usernameField.sendKeys("invalid_user");
        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.sendKeys("invalid_password");
        WebElement loginButton = driver.findElement(By.id("login-button"));
        loginButton.click();

        WebElement errorContainer = driver.findElement(By.cssSelector("[data-test=\"error\"]"));
        assertTrue(errorContainer.isDisplayed(), "Error message should be displayed for invalid login");

        String expectedErrorMessage = "Epic sadface: Username and password do not match any user in this service";
        assertEquals(expectedErrorMessage, errorContainer.getText(), "Error message should match expected text");
    }

    @Test
    @Order(3)
    public void testSortingDropdown() {
        driver.get("https://www.saucedemo.com/v1/inventory.html");
        Select sortDropdown = new Select(driver.findElement(By.className("product_sort_container")));
        
        // Test sorting by name (A-Z)
        sortDropdown.selectByVisibleText("Name (A to Z)");
        List<WebElement> items = driver.findElements(By.className("inventory_item_name"));
        assertEquals(" Sauce Labs Backpack", items.get(0).getText(), "First item should be 'Sauce Labs Backpack'");

        // Test sorting by price (low to high)
        sortDropdown.selectByVisibleText("Price (low to high)");
        assertEquals("$7.99", driver.findElements(By.className("inventory_item_price")).get(0).getText(), "First item should be '$7.99'");

        // Test sorting by price (high to low)
        sortDropdown.selectByVisibleText("Price (high to low)");
        assertEquals("$99.99", driver.findElements(By.className("inventory_item_price")).get(0).getText(), "First item should be '$99.99'");
    }

    @Test
    @Order(4)
    public void testMenuActions() {
        driver.get("https://www.saucedemo.com/v1/inventory.html");

        // Open menu
        WebElement menuButton = driver.findElement(By.id("menu_button_container"));
        menuButton.click();

        // Click All Items
        WebElement allItemsLink = driver.findElement(By.id("inventory_sidebar_link"));
        allItemsLink.click();
        assertEquals("https://www.saucedemo.com/v1/inventory.html", driver.getCurrentUrl());

        // Reset app state
        driver.get("https://www.saucedemo.com/v1/inventory.html");
        menuButton = driver.findElement(By.id("menu_button_container"));
        menuButton.click();
        WebElement resetStateLink = driver.findElement(By.id("reset_sidebar_link"));
        resetStateLink.click();
        assertEquals("https://www.saucedemo.com/v1/inventory.html", driver.getCurrentUrl());

        // Logout
        menuButton = driver.findElement(By.id("menu_button_container"));
        menuButton.click();
        WebElement logoutLink = driver.findElement(By.id("logout_sidebar_link"));
        logoutLink.click();
        assertEquals("https://www.saucedemo.com/v1/index.html", driver.getCurrentUrl());
    }

    @Test
    @Order(5)
    public void testExternalLinksInFooter() {
        driver.get("https://www.saucedemo.com/v1/index.html");

        // Twitter link
        WebElement twitterLink = driver.findElement(By.cssSelector("a[href*='twitter']"));
        String oldTab = driver.getWindowHandle();
        twitterLink.click();
        String winHandle = driver.getWindowHandle();
        driver.switchTo().window(winHandle);
        assertTrue(driver.getCurrentUrl().contains("twitter.com"), "Twitter link should navigate to Twitter website");
        driver.close();
        driver.switchTo().window(oldTab);

        // Facebook link
        WebElement facebookLink = driver.findElement(By.cssSelector("a[href*='facebook']"));
        oldTab = driver.getWindowHandle();
        facebookLink.click();
        winHandle = driver.getWindowHandle();
        driver.switchTo().window(winHandle);
        assertTrue(driver.getCurrentUrl().contains("facebook.com"), "Facebook link should navigate to Facebook website");
        driver.close();
        driver.switchTo().window(oldTab);

        // LinkedIn link
        WebElement linkedinLink = driver.findElement(By.cssSelector("a[href*='linkedin']"));
        oldTab = driver.getWindowHandle();
        linkedinLink.click();
        winHandle = driver.getWindowHandle();
        driver.switchTo().window(winHandle);
        assertTrue(driver.getCurrentUrl().contains("linkedin.com"), "LinkedIn link should navigate to LinkedIn website");
        driver.close();
        driver.switchTo().window(oldTab);
    }

    @Test
    @Order(6)
    public void testAddRemoveItemFromCart() {
        driver.get("https://www.saucedemo.com/v1/inventory.html");

        // Add item to cart
        WebElement addToCartButton = driver.findElement(By.id("add-to-cart-sauce-labs-backpack"));
        addToCartButton.click();

        // Verify cart count increased
        WebElement cartBadge = driver.findElement(By.className("shopping_cart_badge"));
        assertEquals("1", cartBadge.getText(), "Cart badge should show 1 item");

        // Remove item from cart
        WebElement removeFromCartButton = driver.findElement(By.id("remove-sauce-labs-backpack"));
        removeFromCartButton.click();

        // Verify cart is empty
        cartBadge = driver.findElement(By.className("shopping_cart_badge"));
        assertFalse(cartBadge.isDisplayed(), "Cart badge should not be displayed when cart is empty");
    }

    @Test
    @Order(7)
    public void testCheckoutProcess() {
        driver.get("https://www.saucedemo.com/v1/inventory.html");
        WebElement addToCartButton = driver.findElement(By.id("add-to-cart-sauce-labs-backpack"));
        addToCartButton.click();

       	WebElement cartLink = driver.findElement(By.className("shopping_cart_link"));
        cartLink.click();

       	WebElement checkoutButton = driver.findElement(By.id("checkout"));
        checkoutButton.click();

       	WebElement firstNameField = driver.findElement(By.id("first-name"));
        firstNameField.sendKeys("John");
        WebElement lastNameField = driver.findElement(By.id("last-name"));
        lastNameField.sendKeys("Doe");
        WebElement postalCodeField = driver.findElement(By.id("postal-code"));
        postalCodeField.sendKeys("12345");

       	WebElement continueButton = driver.findElement(By.id("continue"));
       	continueButton.click();

       	WebElement finishButton = driver.findElement(By.id("finish"));
       	finishButton.click();

       	WebElement completeMessage = driver.findElement(By.className("complete-header"));
       	assertEquals("THANK YOU FOR YOUR ORDER", completeMessage.getText(), "Should see 'THANK YOU FOR YOUR ORDER'");
    }
}