package SunaDeepSeek.ws01.seq04;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
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
    public void testLoginWithValidCredentials() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("user-name"))).sendKeys(USERNAME);
        driver.findElement(By.id("password")).sendKeys(PASSWORD);
        driver.findElement(By.id("login-button")).click();
        Assertions.assertTrue(driver.getCurrentUrl().contains("inventory.html"), "Login failed");
    }

    @Test
    @Order(2)
    public void testLoginWithInvalidCredentials() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("user-name"))).sendKeys("invalid_user");
        driver.findElement(By.id("password")).sendKeys("wrong_password");
        driver.findElement(By.id("login-button")).click();
        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-test='error']")));
        Assertions.assertTrue(error.getText().contains("Username and password do not match"), "Error message not displayed");
    }

    @Test
    @Order(3)
    public void testProductSorting() {
        login();
        WebElement sortDropdown = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".product_sort_container")));
        sortDropdown.click();
        
        // Test Name (A to Z)
        driver.findElement(By.cssSelector("option[value='az']")).click();
        List<WebElement> items = driver.findElements(By.cssSelector(".inventory_item_name"));
        Assertions.assertTrue(items.get(0).getText().compareTo(items.get(1).getText()) < 0, "Items not sorted A-Z");
        
        // Test Name (Z to A)
        sortDropdown.click();
        driver.findElement(By.cssSelector("option[value='za']")).click();
        items = driver.findElements(By.cssSelector(".inventory_item_name"));
        Assertions.assertTrue(items.get(0).getText().compareTo(items.get(1).getText()) > 0, "Items not sorted Z-A");
        
        // Test Price (low to high)
        sortDropdown.click();
        driver.findElement(By.cssSelector("option[value='lohi']")).click();
        List<WebElement> prices = driver.findElements(By.cssSelector(".inventory_item_price"));
        Assertions.assertTrue(Double.parseDouble(prices.get(0).getText().substring(1)) <= 
                            Double.parseDouble(prices.get(1).getText().substring(1)), "Prices not sorted low-high");
        
        // Test Price (high to low)
        sortDropdown.click();
        driver.findElement(By.cssSelector("option[value='hilo']")).click();
        prices = driver.findElements(By.cssSelector(".inventory_item_price"));
        Assertions.assertTrue(Double.parseDouble(prices.get(0).getText().substring(1)) >= 
                            Double.parseDouble(prices.get(1).getText().substring(1)), "Prices not sorted high-low");
    }

    @Test
    @Order(4)
    public void testMenuNavigation() {
        login();
        
        // Open menu
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".bm-burger-button")));
        menuButton.click();
        
        // Test All Items
        WebElement allItems = wait.until(ExpectedConditions.elementToBeClickable(By.id("inventory_sidebar_link")));
        allItems.click();
        Assertions.assertTrue(driver.getCurrentUrl().contains("inventory.html"), "All Items navigation failed");
        
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
        Assertions.assertTrue(driver.getCurrentUrl().contains("saucelabs.com"), "About page not opened");
        driver.close();
        driver.switchTo().window(originalWindow);
        
        // Open menu again
        menuButton.click();
        
        // Test Logout
        WebElement logout = wait.until(ExpectedConditions.elementToBeClickable(By.id("logout_sidebar_link")));
        logout.click();
        Assertions.assertTrue(driver.getCurrentUrl().contains("index.html"), "Logout failed");
        
        // Login again for remaining tests
        login();
    }

    @Test
    @Order(5)
    public void testResetAppState() {
        login();
        
        // Add item to cart
        WebElement addToCart = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".btn_primary")));
        addToCart.click();
        
        // Open menu and reset
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".bm-burger-button")));
        menuButton.click();
        WebElement reset = wait.until(ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link")));
        reset.click();
        
        // Verify cart is empty
        List<WebElement> cartBadge = driver.findElements(By.cssSelector(".shopping_cart_badge"));
        Assertions.assertEquals(0, cartBadge.size(), "Cart not reset");
    }

    @Test
    @Order(6)
    public void testSocialLinks() {
        login();
        String originalWindow = driver.getWindowHandle();
        
        // Test Twitter
        WebElement twitter = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".social_twitter a")));
        twitter.click();
        switchToNewWindow(originalWindow);
        Assertions.assertTrue(driver.getCurrentUrl().contains("twitter.com"), "Twitter link not working");
        driver.close();
        driver.switchTo().window(originalWindow);
        
        // Test Facebook
        WebElement facebook = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".social_facebook a")));
        facebook.click();
        switchToNewWindow(originalWindow);
        Assertions.assertTrue(driver.getCurrentUrl().contains("facebook.com"), "Facebook link not working");
        driver.close();
        driver.switchTo().window(originalWindow);
        
        // Test LinkedIn
        WebElement linkedin = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".social_linkedin a")));
        linkedin.click();
        switchToNewWindow(originalWindow);
        Assertions.assertTrue(driver.getCurrentUrl().contains("linkedin.com"), "LinkedIn link not working");
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

    private void switchToNewWindow(String originalWindow) {
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.contentEquals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
    }
}