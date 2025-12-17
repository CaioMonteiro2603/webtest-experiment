package SunaDeepSeek.ws01.seq10;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;

@TestMethodOrder(OrderAnnotation.class)
public class saucedemo {

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
    public void testLoginWithValidCredentials() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("user-name"))).sendKeys(USERNAME);
        driver.findElement(By.id("password")).sendKeys(PASSWORD);
        driver.findElement(By.id("login-button")).click();
        
        wait.until(ExpectedConditions.urlContains("inventory.html"));
        Assertions.assertTrue(driver.findElement(By.className("inventory_list")).isDisplayed());
    }

    @Test
    @Order(2)
    public void testLoginWithInvalidCredentials() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("user-name"))).sendKeys("invalid_user");
        driver.findElement(By.id("password")).sendKeys("wrong_password");
        driver.findElement(By.id("login-button")).click();
        
        WebElement errorElement = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("h3[data-test='error']")));
        Assertions.assertTrue(errorElement.getText().contains("Username and password do not match"));
    }

    @Test
    @Order(3)
    public void testSortingDropdown() {
        login();
        
        // Test Name (A to Z)
        driver.findElement(By.className("product_sort_container")).click();
        driver.findElement(By.cssSelector("option[value='az']")).click();
        List<WebElement> items = driver.findElements(By.className("inventory_item_name"));
        Assertions.assertTrue(items.get(0).getText().startsWith("Sauce Labs Backpack"));

        // Test Name (Z to A)
        driver.findElement(By.className("product_sort_container")).click();
        driver.findElement(By.cssSelector("option[value='za']")).click();
        items = driver.findElements(By.className("inventory_item_name"));
        Assertions.assertTrue(items.get(0).getText().startsWith("Test.allTheThings()"));

        // Test Price (low to high)
        driver.findElement(By.className("product_sort_container")).click();
        driver.findElement(By.cssSelector("option[value='lohi']")).click();
        List<WebElement> prices = driver.findElements(By.className("inventory_item_price"));
        Assertions.assertTrue(prices.get(0).getText().startsWith("$7.99"));

        // Test Price (high to low)
        driver.findElement(By.className("product_sort_container")).click();
        driver.findElement(By.cssSelector("option[value='hilo']")).click();
        prices = driver.findElements(By.className("inventory_item_price"));
        Assertions.assertTrue(prices.get(0).getText().startsWith("$49.99"));
    }

    @Test
    @Order(4)
    public void testMenuOptions() {
        login();
        
        // Open menu
        driver.findElement(By.className("bm-burger-button")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("inventory_sidebar_link")));

        // Test All Items
        driver.findElement(By.id("inventory_sidebar_link")).click();
        Assertions.assertTrue(driver.getCurrentUrl().contains("inventory.html"));
        Assertions.assertTrue(driver.findElement(By.className("inventory_list")).isDisplayed());

        // Open menu again
        driver.findElement(By.className("bm-burger-button")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("about_sidebar_link")));

        // Test About (external)
        String originalWindow = driver.getWindowHandle();
        driver.findElement(By.id("about_sidebar_link")).click();
        
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
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
        driver.findElement(By.className("bm-burger-button")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("reset_sidebar_link")));

        // Test Reset App State
        driver.findElement(By.id("reset_sidebar_link")).click();
        Assertions.assertEquals(0, driver.findElements(By.className("shopping_cart_badge")).size());

        // Open menu again
        driver.findElement(By.className("bm-burger-button")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("logout_sidebar_link")));

        // Test Logout
        driver.findElement(By.id("logout_sidebar_link")).click();
        Assertions.assertTrue(driver.getCurrentUrl().contains("index.html"));
    }

    @Test
    @Order(5)
    public void testFooterSocialLinks() {
        login();
        
        String originalWindow = driver.getWindowHandle();
        
        // Test Twitter link
        driver.findElement(By.cssSelector(".social_twitter a")).click();
        switchToNewWindowAndVerify(originalWindow, "twitter.com");
        
        // Test Facebook link
        driver.findElement(By.cssSelector(".social_facebook a")).click();
        switchToNewWindowAndVerify(originalWindow, "facebook.com");
        
        // Test LinkedIn link
        driver.findElement(By.cssSelector(".social_linkedin a")).click();
        switchToNewWindowAndVerify(originalWindow, "linkedin.com");
    }

    private void switchToNewWindowAndVerify(String originalWindow, String expectedDomain) {
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.contentEquals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain));
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    private void login() {
        driver.get(BASE_URL);
        if (!driver.getCurrentUrl().contains("inventory.html")) {
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("user-name"))).sendKeys(USERNAME);
            driver.findElement(By.id("password")).sendKeys(PASSWORD);
            driver.findElement(By.id("login-button")).click();
            wait.until(ExpectedConditions.urlContains("inventory.html"));
        }
    }
}