package Qwen3.ws01.seq07;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class SauceDemoTest {

    private static WebDriver driver;
    private static WebDriverWait wait;

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
        driver.get("https://www.saucedemo.com/v1/index.html");
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.id("user-name")));
        usernameField.sendKeys("standard_user");
        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.sendKeys("secret_sauce");
        WebElement loginButton = driver.findElement(By.id("login-button"));
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("inventory"));
        assertEquals("https://www.saucedemo.com/v1/inventory.html", driver.getCurrentUrl());
        assertTrue(driver.findElement(By.className("inventory_list")).isDisplayed());
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get("https://www.saucedemo.com/v1/index.html");
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.id("user-name")));
        usernameField.sendKeys("invalid_user");
        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.sendKeys("invalid_password");
        WebElement loginButton = driver.findElement(By.id("login-button"));
        loginButton.click();

        WebElement errorElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".error-message-container")));
        assertTrue(errorElement.isDisplayed());
        assertTrue(errorElement.getText().contains("Epic sadface"));
    }

    @Test
    @Order(3)
    public void testSorting() {
        driver.get("https://www.saucedemo.com/v1/inventory.html");
        WebElement sortDropdown = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".product_sort_container")));
        sortDropdown.click();

        // Test sorting by price high to low
        WebElement priceHighLowOption = driver.findElement(By.xpath("//option[@value='hilo']"));
        priceHighLowOption.click();
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.className("product_sort_container"), "Price (high to low)"));

        // Test sorting by name A to Z
        sortDropdown.click();
        WebElement nameAToZOption = driver.findElement(By.xpath("//option[@value='az']"));
        nameAToZOption.click();
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.className("product_sort_container"), "Name (A to Z)"));

        // Test sorting by name Z to A
        sortDropdown.click();
        WebElement nameZToAOption = driver.findElement(By.xpath("//option[@value='za']"));
        nameZToAOption.click();
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.className("product_sort_container"), "Name (Z to A)"));

        // Test sorting by price low to high
        sortDropdown.click();
        WebElement priceLowToHighOption = driver.findElement(By.xpath("//option[@value='lohi']"));
        priceLowToHighOption.click();
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.className("product_sort_container"), "Price (low to high)"));
    }

    @Test
    @Order(4)
    public void testMenuFunctionality() {
        driver.get("https://www.saucedemo.com/v1/inventory.html");

        // Open menu
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuButton.click();

        // Click All Items
        WebElement allItemsLink = driver.findElement(By.id("inventory_sidebar_link"));
        allItemsLink.click();
        wait.until(ExpectedConditions.urlContains("inventory"));
        assertEquals("https://www.saucedemo.com/v1/inventory.html", driver.getCurrentUrl());

        // Open menu again
        menuButton.click();

        // Click About
        WebElement aboutLink = driver.findElement(By.id("about_sidebar_link"));
        aboutLink.click();
        Set<String> windowHandles = driver.getWindowHandles();
        assertEquals(2, windowHandles.size());
        String mainWindowHandle = driver.getWindowHandle();
        String newWindowHandle = null;
        for (String handle : windowHandles) {
            if (!handle.equals(mainWindowHandle)) {
                newWindowHandle = handle;
                break;
            }
        }
        driver.switchTo().window(newWindowHandle);
        assertTrue(driver.getCurrentUrl().contains("saucelabs.com"));
        driver.close();
        driver.switchTo().window(mainWindowHandle);

        // Open menu again
        menuButton.click();

        // Click Logout
        WebElement logoutLink = driver.findElement(By.id("logout_sidebar_link"));
        logoutLink.click();
        wait.until(ExpectedConditions.urlContains("index"));
        assertEquals("https://www.saucedemo.com/v1/index.html", driver.getCurrentUrl());

        // Login again for next tests
        testValidLogin();
    }

    @Test
    @Order(5)
    public void testFooterLinks() {
        driver.get("https://www.saucedemo.com/v1/inventory.html");

        // Test Twitter link
        WebElement twitterLink = driver.findElement(By.cssSelector("a[href*='twitter']"));
        twitterLink.click();
        Set<String> windowHandles = driver.getWindowHandles();
        assertEquals(2, windowHandles.size());
        String mainWindowHandle = driver.getWindowHandle();
        String newWindowHandle = null;
        for (String handle : windowHandles) {
            if (!handle.equals(mainWindowHandle)) {
                newWindowHandle = handle;
                break;
            }
        }
        driver.switchTo().window(newWindowHandle);
        assertTrue(driver.getCurrentUrl().contains("twitter.com"));
        driver.close();
        driver.switchTo().window(mainWindowHandle);

        // Test Facebook link
        WebElement facebookLink = driver.findElement(By.cssSelector("a[href*='facebook']"));
        facebookLink.click();
        windowHandles = driver.getWindowHandles();
        assertEquals(2, windowHandles.size());
        mainWindowHandle = driver.getWindowHandle();
        newWindowHandle = null;
        for (String handle : windowHandles) {
            if (!handle.equals(mainWindowHandle)) {
                newWindowHandle = handle;
                break;
            }
        }
        driver.switchTo().window(newWindowHandle);
        assertTrue(driver.getCurrentUrl().contains("facebook.com"));
        driver.close();
        driver.switchTo().window(mainWindowHandle);

        // Test LinkedIn link
        WebElement linkedInLink = driver.findElement(By.cssSelector("a[href*='linkedin']"));
        linkedInLink.click();
        windowHandles = driver.getWindowHandles();
        assertEquals(2, windowHandles.size());
        mainWindowHandle = driver.getWindowHandle();
        newWindowHandle = null;
        for (String handle : windowHandles) {
            if (!handle.equals(mainWindowHandle)) {
                newWindowHandle = handle;
                break;
            }
        }
        driver.switchTo().window(newWindowHandle);
        assertTrue(driver.getCurrentUrl().contains("linkedin.com"));
        driver.close();
        driver.switchTo().window(mainWindowHandle);
    }

    @Test
    @Order(6)
    public void testResetAppState() {
        driver.get("https://www.saucedemo.com/v1/inventory.html");

        // Add item to cart
        WebElement addToCartButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("add-to-cart-sauce-labs-backpack")));
        addToCartButton.click();

        // Open menu and reset state
        WebElement menuButton = driver.findElement(By.id("react-burger-menu-btn"));
        menuButton.click();
        WebElement resetAppStateLink = driver.findElement(By.id("reset_sidebar_link"));
        resetAppStateLink.click();

        // Verify reset
        WebElement cartBadge = driver.findElement(By.className("shopping_cart_badge"));
        assertEquals("0", cartBadge.getText());
    }
}