package SunaDeepSeek.ws05.seq03;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TAT {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://cac-tat.s3.eu-central-1.amazonaws.com/index.html";

    @BeforeAll
    public static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @Order(1)
    public void testHomePageLoads() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.titleContains("Swag Labs"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("index.html"), "Should be on home page");
    }

    @Test
    @Order(2)
    public void testSuccessfulLogin() {
        driver.get(BASE_URL);
        WebElement username = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("user-name")));
        WebElement password = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("login-button"));

        username.sendKeys("standard_user");
        password.sendKeys("secret_sauce");
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("inventory.html"));
        Assertions.assertTrue(driver.findElements(By.className("inventory_item")).size() > 0, 
            "Inventory items should be visible after login");
    }

    @Test
    @Order(3)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        WebElement username = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("user-name")));
        WebElement password = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("login-button"));

        username.sendKeys("invalid_user");
        password.sendKeys("wrong_password");
        loginButton.click();

        WebElement error = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("[data-test='error']")));
        Assertions.assertTrue(error.getText().contains("Username and password do not match"), 
            "Error message should be displayed");
    }

    @Test
    @Order(4)
    public void testSortingDropdown() {
        testSuccessfulLogin(); // Ensure logged in
        
        WebElement sortDropdown = wait.until(ExpectedConditions.elementToBeClickable(
            By.className("product_sort_container")));
        
        // Test Name (A to Z)
        sortDropdown.sendKeys("az");
        List<WebElement> items = driver.findElements(By.className("inventory_item_name"));
        Assertions.assertTrue(items.get(0).getText().startsWith("Sauce Labs Backpack"), 
            "First item should be Backpack when sorted A-Z");

        // Test Name (Z to A)
        sortDropdown.sendKeys("za");
        items = driver.findElements(By.className("inventory_item_name"));
        Assertions.assertTrue(items.get(0).getText().startsWith("Test.allTheThings() T-Shirt"), 
            "First item should be T-Shirt when sorted Z-A");

        // Test Price (low to high)
        sortDropdown.sendKeys("lohi");
        items = driver.findElements(By.className("inventory_item_price"));
        Assertions.assertTrue(items.get(0).getText().equals("$7.99"), 
            "First item should be $7.99 when sorted low to high");

        // Test Price (high to low)
        sortDropdown.sendKeys("hilo");
        items = driver.findElements(By.className("inventory_item_price"));
        Assertions.assertTrue(items.get(0).getText().equals("$49.99"), 
            "First item should be $49.99 when sorted high to low");
    }

    @Test
    @Order(5)
    public void testMenuNavigation() {
        testSuccessfulLogin(); // Ensure logged in
        
        // Open menu
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuButton.click();
        
        // Test All Items
        WebElement allItems = wait.until(ExpectedConditions.elementToBeClickable(By.id("inventory_sidebar_link")));
        allItems.click();
        Assertions.assertTrue(driver.getCurrentUrl().contains("inventory.html"), 
            "Should be on inventory page after clicking All Items");

        // Open menu again
        menuButton.click();
        
        // Test About (external)
        WebElement about = wait.until(ExpectedConditions.elementToBeClickable(By.id("about_sidebar_link")));
        about.click();
        
        String originalWindow = driver.getWindowHandle();
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.contentEquals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        wait.until(ExpectedConditions.urlContains("saucelabs.com"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("saucelabs.com"), 
            "Should be on SauceLabs site after clicking About");
        driver.close();
        driver.switchTo().window(originalWindow);

        // Open menu again
        menuButton.click();
        
        // Test Logout
        WebElement logout = wait.until(ExpectedConditions.elementToBeClickable(By.id("logout_sidebar_link")));
        logout.click();
        wait.until(ExpectedConditions.urlContains("index.html"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("index.html"), 
            "Should be back on login page after logout");
    }

    @Test
    @Order(6)
    public void testFooterLinks() {
        testSuccessfulLogin(); // Ensure logged in
        String originalWindow = driver.getWindowHandle();
        
        // Test Twitter link
        WebElement twitter = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(".social_twitter a")));
        twitter.click();
        
        switchToNewWindow(originalWindow);
        wait.until(ExpectedConditions.urlContains("twitter.com"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("twitter.com"), 
            "Should be on Twitter after clicking Twitter link");
        driver.close();
        driver.switchTo().window(originalWindow);

        // Test Facebook link
        WebElement facebook = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(".social_facebook a")));
        facebook.click();
        
        switchToNewWindow(originalWindow);
        wait.until(ExpectedConditions.urlContains("facebook.com"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("facebook.com"), 
            "Should be on Facebook after clicking Facebook link");
        driver.close();
        driver.switchTo().window(originalWindow);

        // Test LinkedIn link
        WebElement linkedin = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(".social_linkedin a")));
        linkedin.click();
        
        switchToNewWindow(originalWindow);
        wait.until(ExpectedConditions.urlContains("linkedin.com"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("linkedin.com"), 
            "Should be on LinkedIn after clicking LinkedIn link");
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(7)
    public void testResetAppState() {
        testSuccessfulLogin(); // Ensure logged in
        
        // Add item to cart
        WebElement addToCart = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(".btn_inventory")));
        addToCart.click();
        
        // Verify cart has item
        WebElement cartBadge = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.className("shopping_cart_badge")));
        Assertions.assertEquals("1", cartBadge.getText(), "Cart should have 1 item");

        // Open menu and reset
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuButton.click();
        WebElement reset = wait.until(ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link")));
        reset.click();
        
        // Verify cart is empty
        Assertions.assertTrue(driver.findElements(By.className("shopping_cart_badge")).isEmpty(), 
            "Cart should be empty after reset");
    }

    private void switchToNewWindow(String originalWindow) {
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.contentEquals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
    }
}