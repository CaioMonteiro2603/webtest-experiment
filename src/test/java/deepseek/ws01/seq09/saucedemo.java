package deepseek.ws01.seq09;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(OrderAnnotation.class)
public class SauceDemoTest {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://www.saucedemo.com/v1/index.html";
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
    public void testSuccessfulLogin() {
        driver.get(BASE_URL);
        WebElement usernameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("user-name")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("login-button"));

        usernameField.sendKeys(USERNAME);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("inventory.html"));
        assertTrue(driver.getCurrentUrl().contains("inventory.html"));
        assertTrue(driver.findElements(By.className("inventory_list")).size() > 0);
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        WebElement usernameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("user-name")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("login-button"));

        usernameField.sendKeys("invalid_user");
        passwordField.sendKeys("wrong_password");
        loginButton.click();

        WebElement errorElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("[data-test='error']")));
        assertTrue(errorElement.getText().contains("Username and password do not match"));
    }

    @Test
    @Order(3)
    public void testSortingDropdown() {
        login();
        
        Select sortDropdown = new Select(wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".product_sort_container"))));
        
        // Test Name (A to Z)
        sortDropdown.selectByValue("az");
        List<WebElement> items = driver.findElements(By.className("inventory_item_name"));
        assertTrue(items.get(0).getText().startsWith("Sauce Labs Backpack"));
        
        // Test Name (Z to A)
        sortDropdown.selectByValue("za");
        items = driver.findElements(By.className("inventory_item_name"));
        assertTrue(items.get(0).getText().startsWith("Test.allTheThings() T-Shirt"));
        
        // Test Price (low to high)
        sortDropdown.selectByValue("lohi");
        items = driver.findElements(By.className("inventory_item_price"));
        assertEquals("$7.99", items.get(0).getText());
        
        // Test Price (high to low)
        sortDropdown.selectByValue("hilo");
        items = driver.findElements(By.className("inventory_item_price"));
        assertEquals("$49.99", items.get(0).getText());
    }

    @Test
    @Order(4)
    public void testMenuNavigation() {
        login();
        
        // Open menu
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuButton.click();
        
        // Test All Items
        WebElement allItems = wait.until(ExpectedConditions.elementToBeClickable(By.id("inventory_sidebar_link")));
        allItems.click();
        assertTrue(driver.getCurrentUrl().contains("inventory.html"));
        
        // Test About (external)
        menuButton.click();
        WebElement about = wait.until(ExpectedConditions.elementToBeClickable(By.id("about_sidebar_link")));
        about.click();
        
        String originalWindow = driver.getWindowHandle();
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        assertTrue(driver.getCurrentUrl().contains("saucelabs.com"));
        driver.close();
        driver.switchTo().window(originalWindow);
        
        // Test Logout
        menuButton.click();
        WebElement logout = wait.until(ExpectedConditions.elementToBeClickable(By.id("logout_sidebar_link")));
        logout.click();
        assertTrue(driver.getCurrentUrl().contains("index.html"));
        
        // Log back in for subsequent tests
        login();
    }

    @Test
    @Order(5)
    public void testSocialLinks() {
        login();
        
        // Twitter
        testExternalLink(".social_twitter a", "twitter.com");
        
        // Facebook
        testExternalLink(".social_facebook a", "facebook.com");
        
        // LinkedIn
        testExternalLink(".social_linkedin a", "linkedin.com");
    }

    @Test
    @Order(6)
    public void testResetAppState() {
        login();
        
        // Add item to cart
        WebElement addToCart = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".btn_inventory")));
        addToCart.click();
        assertTrue(driver.findElement(By.className("shopping_cart_badge")).getText().equals("1"));
        
        // Reset app state
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuButton.click();
        WebElement reset = wait.until(ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link")));
        reset.click();
        
        // Verify cart is empty
        assertEquals(0, driver.findElements(By.className("shopping_cart_badge")).size());
    }

    private void login() {
        driver.get(BASE_URL);
        if (!driver.getCurrentUrl().contains("inventory.html")) {
            WebElement usernameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("user-name")));
            WebElement passwordField = driver.findElement(By.id("password"));
            WebElement loginButton = driver.findElement(By.id("login-button"));

            usernameField.sendKeys(USERNAME);
            passwordField.sendKeys(PASSWORD);
            loginButton.click();

            wait.until(ExpectedConditions.urlContains("inventory.html"));
        }
    }

    private void testExternalLink(String selector, String expectedDomain) {
        String originalWindow = driver.getWindowHandle();
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(selector)));
        link.click();
        
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        assertTrue(driver.getCurrentUrl().contains(expectedDomain));
        driver.close();
        driver.switchTo().window(originalWindow);
    }
}