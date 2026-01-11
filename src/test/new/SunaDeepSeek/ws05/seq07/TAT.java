package SunaDeepSeek.ws05.seq07;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;

@TestMethodOrder(OrderAnnotation.class)
public class TAT {
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://cac-tat.s3.eu-central-1.amazonaws.com/index.html";
    private static final String USERNAME = "standard_user";
    private static final String PASSWORD = "secret_sauce";

    @BeforeAll
    public static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    @AfterAll
    public static void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @Order(1)
    public void testLogin() {
        driver.get(BASE_URL);
        
        // Enter credentials and login
        WebElement usernameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("user-name")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("login-button"));
        
        usernameField.sendKeys(USERNAME);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();
        
        // Verify successful login
        wait.until(ExpectedConditions.urlContains("inventory.html"));
        WebElement inventoryContainer = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("inventory_container")));
        Assertions.assertTrue(inventoryContainer.isDisplayed(), "Inventory page should be displayed after login");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        
        // Enter invalid credentials
        WebElement usernameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("user-name")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("login-button"));
        
        usernameField.sendKeys("invalid_user");
        passwordField.sendKeys("wrong_password");
        loginButton.click();
        
        // Verify error message
        WebElement errorElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("[data-test='error']")));
        Assertions.assertTrue(errorElement.getText().contains("Username and password do not match"), 
            "Error message should be displayed for invalid credentials");
    }

    @Test
    @Order(3)
    public void testSortingDropdown() {
        // First login
        driver.get(BASE_URL);
        login(USERNAME, PASSWORD);
        
        // Test sorting options
        WebElement sortDropdown = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".product_sort_container")));
        sortDropdown.click();
        
        // Test Name (A to Z)
        driver.findElement(By.cssSelector("option[value='az']")).click();
        List<WebElement> items = driver.findElements(By.cssSelector(".inventory_item_name"));
        Assertions.assertTrue(items.get(0).getText().startsWith("Sauce Labs Backpack"), 
            "First item should be 'Sauce Labs Backpack' when sorted A-Z");
        
        // Test Name (Z to A)
        sortDropdown.click();
        driver.findElement(By.cssSelector("option[value='za']")).click();
        items = driver.findElements(By.cssSelector(".inventory_item_name"));
        Assertions.assertTrue(items.get(0).getText().startsWith("Test.allTheThings() T-Shirt (Red)"), 
            "First item should be 'Test.allTheThings() T-Shirt (Red)' when sorted Z-A");
        
        // Test Price (low to high)
        sortDropdown.click();
        driver.findElement(By.cssSelector("option[value='lohi']")).click();
        items = driver.findElements(By.cssSelector(".inventory_item_price"));
        Assertions.assertEquals("$7.99", items.get(0).getText(), 
            "First item price should be $7.99 when sorted low to high");
        
        // Test Price (high to low)
        sortDropdown.click();
        driver.findElement(By.cssSelector("option[value='hilo']")).click();
        items = driver.findElements(By.cssSelector(".inventory_item_price"));
        Assertions.assertEquals("$49.99", items.get(0).getText(), 
            "First item price should be $49.99 when sorted high to low");
    }

    @Test
    @Order(4)
    public void testMenuNavigation() {
        // First login
        driver.get(BASE_URL);
        login(USERNAME, PASSWORD);
        
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
        
        // Test About (external link)
        WebElement about = wait.until(ExpectedConditions.elementToBeClickable(By.id("about_sidebar_link")));
        about.click();
        
        // Switch to new tab and verify URL
        String originalWindow = driver.getWindowHandle();
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.contentEquals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        Assertions.assertTrue(driver.getCurrentUrl().contains("saucelabs.com"), 
            "About link should open saucelabs.com");
        driver.close();
        driver.switchTo().window(originalWindow);
        
        // Open menu again
        menuButton.click();
        
        // Test Logout
        WebElement logout = wait.until(ExpectedConditions.elementToBeClickable(By.id("logout_sidebar_link")));
        logout.click();
        Assertions.assertTrue(driver.getCurrentUrl().contains("index.html"), 
            "Should be back on login page after logout");
    }

    @Test
    @Order(5)
    public void testFooterLinks() {
        // First login
        driver.get(BASE_URL);
        login(USERNAME, PASSWORD);
        
        // Test Twitter link
        testExternalLink(By.cssSelector(".social_twitter a"), "twitter.com");
        
        // Test Facebook link
        testExternalLink(By.cssSelector(".social_facebook a"), "facebook.com");
        
        // Test LinkedIn link
        testExternalLink(By.cssSelector(".social_linkedin a"), "linkedin.com");
    }

    @Test
    @Order(6)
    public void testResetAppState() {
        // First login
        driver.get(BASE_URL);
        login(USERNAME, PASSWORD);
        
        // Add item to cart
        WebElement addToCart = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(".btn_inventory")));
        addToCart.click();
        
        // Verify cart has item
        WebElement cartBadge = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector(".shopping_cart_badge")));
        Assertions.assertEquals("1", cartBadge.getText(), "Cart should have 1 item");
        
        // Open menu and reset
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuButton.click();
        WebElement reset = wait.until(ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link")));
        reset.click();
        
        // Verify cart is empty
        List<WebElement> cartBadges = driver.findElements(By.cssSelector(".shopping_cart_badge"));
        Assertions.assertEquals(0, cartBadges.size(), "Cart should be empty after reset");
    }

    private void login(String username, String password) {
        WebElement usernameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("user-name")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("login-button"));
        
        usernameField.sendKeys(username);
        passwordField.sendKeys(password);
        loginButton.click();
        
        wait.until(ExpectedConditions.urlContains("inventory.html"));
    }

    private void testExternalLink(By locator, String expectedDomain) {
        String originalWindow = driver.getWindowHandle();
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(locator));
        link.click();
        
        // Switch to new tab
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.contentEquals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        // Verify domain and close tab
        Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain), 
            "Link should open " + expectedDomain);
        driver.close();
        driver.switchTo().window(originalWindow);
    }
}