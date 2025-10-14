package deepseek.ws01.seq03;

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

@TestMethodOrder(OrderAnnotation.class)
public class SauceDemoTest {
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://www.saucedemo.com/v1/index.html";
    private static final String USERNAME = "standard_user";
    private static final String PASSWORD = "secret_sauce";

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
    public void testLogin() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("user-name"))).sendKeys(USERNAME);
        driver.findElement(By.id("password")).sendKeys(PASSWORD);
        driver.findElement(By.id("login-button")).click();
        wait.until(ExpectedConditions.urlContains("inventory.html"));
        Assertions.assertTrue(driver.findElement(By.className("inventory_list")).isDisplayed());
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        driver.findElement(By.id("user-name")).sendKeys("invalid_user");
        driver.findElement(By.id("password")).sendKeys("wrong_password");
        driver.findElement(By.id("login-button")).click();
        WebElement errorElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-test='error']")));
        Assertions.assertTrue(errorElement.getText().contains("Username and password do not match"));
    }

    @Test
    @Order(3)
    public void testSortingDropdown() {
        loginIfNeeded();
        Select sortDropdown = new Select(driver.findElement(By.className("product_sort_container")));
        
        // Test Name (A to Z)
        sortDropdown.selectByValue("az");
        List<WebElement> items = driver.findElements(By.className("inventory_item_name"));
        Assertions.assertTrue(items.get(0).getText().startsWith("Sauce Labs Backpack"));
        
        // Test Name (Z to A)
        sortDropdown.selectByValue("za");
        items = driver.findElements(By.className("inventory_item_name"));
        Assertions.assertTrue(items.get(0).getText().startsWith("Test.allTheThings() T-Shirt"));
        
        // Test Price (low to high)
        sortDropdown.selectByValue("lohi");
        items = driver.findElements(By.className("inventory_item_price"));
        Assertions.assertTrue(items.get(0).getText().startsWith("$7.99"));
        
        // Test Price (high to low)
        sortDropdown.selectByValue("hilo");
        items = driver.findElements(By.className("inventory_item_price"));
        Assertions.assertTrue(items.get(0).getText().startsWith("$49.99"));
    }

    @Test
    @Order(4)
    public void testMenuNavigation() {
        loginIfNeeded();
        
        // Open menu
        driver.findElement(By.id("react-burger-menu-btn")).click();
        WebElement menu = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("bm-menu-wrap")));
        
        // Test All Items
        driver.findElement(By.id("inventory_sidebar_link")).click();
        Assertions.assertTrue(driver.getCurrentUrl().contains("inventory.html"));
        
        // Test About (external)
        driver.findElement(By.id("react-burger-menu-btn")).click();
        menu = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("bm-menu-wrap")));
        driver.findElement(By.id("about_sidebar_link")).click();
        String originalWindow = driver.getWindowHandle();
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.equals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        Assertions.assertTrue(driver.getCurrentUrl().contains("saucelabs.com"));
        driver.close();
        driver.switchTo().window(originalWindow);
        
        // Test Reset App State
        driver.findElement(By.id("react-burger-menu-btn")).click();
        menu = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("bm-menu-wrap")));
        driver.findElement(By.id("reset_sidebar_link")).click();
        Assertions.assertEquals(0, driver.findElements(By.className("shopping_cart_badge")).size());
        
        // Test Logout
        driver.findElement(By.id("react-burger-menu-btn")).click();
        menu = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("bm-menu-wrap")));
        driver.findElement(By.id("logout_sidebar_link")).click();
        Assertions.assertTrue(driver.getCurrentUrl().contains("index.html"));
        loginIfNeeded();
    }

    @Test
    @Order(5)
    public void testSocialLinks() {
        loginIfNeeded();
        
        // Test Twitter
        driver.findElement(By.cssSelector(".social_twitter a")).click();
        handleExternalLink("twitter.com");
        
        // Test Facebook
        driver.findElement(By.cssSelector(".social_facebook a")).click();
        handleExternalLink("facebook.com");
        
        // Test LinkedIn
        driver.findElement(By.cssSelector(".social_linkedin a")).click();
        handleExternalLink("linkedin.com");
    }

    private void loginIfNeeded() {
        if (!driver.getCurrentUrl().contains("inventory.html")) {
            driver.get(BASE_URL);
            driver.findElement(By.id("user-name")).sendKeys(USERNAME);
            driver.findElement(By.id("password")).sendKeys(PASSWORD);
            driver.findElement(By.id("login-button")).click();
            wait.until(ExpectedConditions.urlContains("inventory.html"));
        }
    }

    private void handleExternalLink(String expectedDomain) {
        String originalWindow = driver.getWindowHandle();
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.equals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain));
        driver.close();
        driver.switchTo().window(originalWindow);
    }
}