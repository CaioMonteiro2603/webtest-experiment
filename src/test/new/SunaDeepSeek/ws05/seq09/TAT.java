package SunaDeepSeek.ws05.seq09;

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
    private static final String BASE_URL = "https://cac-tat.s3.eu-central-1.amazonaws.com/index.html";
    private static WebDriver driver;
    private static WebDriverWait wait;

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
    public void testLoginPage() {
        driver.get(BASE_URL);
        Assertions.assertEquals("Central de Atendimento ao Cliente TAT", driver.getTitle());
        
        WebElement username = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[type='text']")));
        WebElement password = driver.findElement(By.cssSelector("input[type='password']"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        
        username.sendKeys("standard_user");
        password.sendKeys("secret_sauce");
        loginButton.click();
        
        wait.until(ExpectedConditions.urlContains("inventory.html"));
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        WebElement username = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[type='text']")));
        WebElement password = driver.findElement(By.cssSelector("input[type='password']"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        
        username.sendKeys("invalid_user");
        password.sendKeys("wrong_password");
        loginButton.click();
        
        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".error")));
        Assertions.assertTrue(error.getText().contains("Username and password do not match") || 
                             error.getText().contains("invalid") || 
                             error.getText().contains("error"));
    }

    @Test
    @Order(3)
    public void testSortingOptions() {
        driver.get(BASE_URL);
        WebElement username = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[type='text']")));
        WebElement password = driver.findElement(By.cssSelector("input[type='password']"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        
        username.sendKeys("standard_user");
        password.sendKeys("secret_sauce");
        loginButton.click();
        
        WebElement sortDropdown = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(".product_sort_container")));
        sortDropdown.click();
        
        List<WebElement> options = driver.findElements(By.cssSelector(".product_sort_container option"));
        Assertions.assertEquals(4, options.size());
        
        // Test Name (A to Z)
        sortDropdown.sendKeys("az");
        wait.until(ExpectedConditions.textToBePresentInElementLocated(
            By.cssSelector(".inventory_item_name"), "Sauce Labs Backpack"));
        
        // Test Name (Z to A)
        sortDropdown.sendKeys("za");
        wait.until(ExpectedConditions.textToBePresentInElementLocated(
            By.cssSelector(".inventory_item_name"), "Test.allTheThings() T-Shirt (Red)"));
        
        // Test Price (low to high)
        sortDropdown.sendKeys("lohi");
        wait.until(ExpectedConditions.textToBePresentInElementLocated(
            By.cssSelector(".inventory_item_price"), "$7.99"));
        
        // Test Price (high to low)
        sortDropdown.sendKeys("hilo");
        wait.until(ExpectedConditions.textToBePresentInElementLocated(
            By.cssSelector(".inventory_item_price"), "$49.99"));
    }

    @Test
    @Order(4)
    public void testMenuNavigation() {
        driver.get(BASE_URL);
        WebElement username = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[type='text']")));
        WebElement password = driver.findElement(By.cssSelector("input[type='password']"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        
        username.sendKeys("standard_user");
        password.sendKeys("secret_sauce");
        loginButton.click();
        
        // Open menu
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.id("react-burger-menu-btn")));
        menuButton.click();
        
        // Test All Items
        WebElement allItems = wait.until(ExpectedConditions.elementToBeClickable(
            By.id("inventory_sidebar_link")));
        allItems.click();
        Assertions.assertTrue(driver.getCurrentUrl().contains("inventory.html"));
        
        // Open menu again
        menuButton.click();
        
        // Test About (external)
        WebElement about = wait.until(ExpectedConditions.elementToBeClickable(
            By.id("about_sidebar_link")));
        about.click();
        
        String originalWindow = driver.getWindowHandle();
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.contentEquals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        Assertions.assertTrue(driver.getCurrentUrl().contains("saucelabs.com"));
        driver.close();
        driver.switchTo().window(originalWindow);
        
        // Open menu again
        menuButton.click();
        
        // Test Logout
        WebElement logout = wait.until(ExpectedConditions.elementToBeClickable(
            By.id("logout_sidebar_link")));
        logout.click();
        wait.until(ExpectedConditions.urlContains("index.html"));
    }

    @Test
    @Order(5)
    public void testSocialLinks() {
        driver.get(BASE_URL);
        WebElement username = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[type='text']")));
        WebElement password = driver.findElement(By.cssSelector("input[type='password']"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        
        username.sendKeys("standard_user");
        password.sendKeys("secret_sauce");
        loginButton.click();
        
        String originalWindow = driver.getWindowHandle();
        
        // Test Twitter
        WebElement twitter = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(".social_twitter a")));
        twitter.click();
        
        switchToNewWindow(originalWindow);
        Assertions.assertTrue(driver.getCurrentUrl().contains("twitter.com") || 
                             driver.getCurrentUrl().contains("x.com"));
        driver.close();
        driver.switchTo().window(originalWindow);
        
        // Test Facebook
        WebElement facebook = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(".social_facebook a")));
        facebook.click();
        
        switchToNewWindow(originalWindow);
        Assertions.assertTrue(driver.getCurrentUrl().contains("facebook.com"));
        driver.close();
        driver.switchTo().window(originalWindow);
        
        // Test LinkedIn
        WebElement linkedin = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(".social_linkedin a")));
        linkedin.click();
        
        switchToNewWindow(originalWindow);
        Assertions.assertTrue(driver.getCurrentUrl().contains("linkedin.com"));
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(6)
    public void testResetAppState() {
        driver.get(BASE_URL);
        WebElement username = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[type='text']")));
        WebElement password = driver.findElement(By.cssSelector("input[type='password']"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        
        username.sendKeys("standard_user");
        password.sendKeys("secret_sauce");
        loginButton.click();
        
        // Add item to cart
        WebElement addToCart = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(".btn_inventory")));
        addToCart.click();
        
        // Verify cart has item
        WebElement cartBadge = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".shopping_cart_badge")));
        Assertions.assertEquals("1", cartBadge.getText());
        
        // Open menu and reset
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.id("react-burger-menu-btn")));
        menuButton.click();
        
        WebElement reset = wait.until(ExpectedConditions.elementToBeClickable(
            By.id("reset_sidebar_link")));
        reset.click();
        
        // Verify cart is empty
        List<WebElement> cartBadges = driver.findElements(By.cssSelector(".shopping_cart_badge"));
        Assertions.assertEquals(0, cartBadges.size());
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