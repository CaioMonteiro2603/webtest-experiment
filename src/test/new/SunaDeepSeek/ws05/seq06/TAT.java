package SunaDeepSeek.ws05.seq06;

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
    private static final String BASE_URL = "https://www.saucedemo.com/";
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
    public void testSuccessfulLogin() {
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
        
        WebElement errorElement = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("h3[data-test='error']")));
        Assertions.assertTrue(errorElement.getText().contains("Username and password do not match"));
    }

    @Test
    @Order(3)
    public void testSortingDropdown() {
        login();
        
        // Test sorting A to Z
        selectSortOption("az");
        List<WebElement> items = driver.findElements(By.className("inventory_item_name"));
        Assertions.assertTrue(items.get(0).getText().compareTo(items.get(1).getText()) < 0);
        
        // Test sorting Z to A
        selectSortOption("za");
        items = driver.findElements(By.className("inventory_item_name"));
        Assertions.assertTrue(items.get(0).getText().compareTo(items.get(1).getText()) > 0);
        
        // Test sorting low to high price
        selectSortOption("lohi");
        List<WebElement> prices = driver.findElements(By.className("inventory_item_price"));
        Assertions.assertTrue(extractPrice(prices.get(0)) <= extractPrice(prices.get(1)));
        
        // Test sorting high to low price
        selectSortOption("hilo");
        prices = driver.findElements(By.className("inventory_item_price"));
        Assertions.assertTrue(extractPrice(prices.get(0)) >= extractPrice(prices.get(1)));
    }

    @Test
    @Order(4)
    public void testMenuOptions() {
        login();
        
        // Open menu
        driver.findElement(By.id("react-burger-menu-btn")).click();
        WebElement menu = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.className("bm-menu")));
        
        // Test All Items
        driver.findElement(By.id("inventory_sidebar_link")).click();
        Assertions.assertTrue(driver.getCurrentUrl().contains("inventory.html"));
        
        // Test About (external link)
        driver.findElement(By.id("react-burger-menu-btn")).click();
        menu = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.className("bm-menu")));
        driver.findElement(By.id("about_sidebar_link")).click();
        
        String originalWindow = driver.getWindowHandle();
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
        
        // Test Logout
        driver.findElement(By.id("react-burger-menu-btn")).click();
        menu = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.className("bm-menu")));
        driver.findElement(By.id("logout_sidebar_link")).click();
        wait.until(ExpectedConditions.urlContains("index.html"));
    }

    @Test
    @Order(5)
    public void testFooterSocialLinks() {
        login();
        
        // Test Twitter link
        testExternalLink("social-twitter", "twitter.com");
        
        // Test Facebook link
        testExternalLink("social-facebook", "facebook.com");
        
        // Test LinkedIn link
        testExternalLink("social-linkedin", "linkedin.com");
    }

    @Test
    @Order(6)
    public void testResetAppState() {
        login();
        
        // Add item to cart
        driver.findElement(By.cssSelector(".btn_inventory")).click();
        Assertions.assertEquals("1", driver.findElement(By.className("shopping_cart_badge")).getText());
        
        // Reset app state
        driver.findElement(By.id("react-burger-menu-btn")).click();
        WebElement menu = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.className("bm-menu")));
        driver.findElement(By.id("reset_sidebar_link")).click();
        
        // Verify cart is empty
        Assertions.assertTrue(driver.findElements(By.className("shopping_cart_badge")).isEmpty());
    }

    private void login() {
        driver.get(BASE_URL);
        if (!driver.getCurrentUrl().contains("inventory.html")) {
            driver.findElement(By.id("user-name")).sendKeys(USERNAME);
            driver.findElement(By.id("password")).sendKeys(PASSWORD);
            driver.findElement(By.id("login-button")).click();
            wait.until(ExpectedConditions.urlContains("inventory.html"));
        }
    }

    private void selectSortOption(String value) {
        WebElement sortDropdown = driver.findElement(By.className("product_sort_container"));
        sortDropdown.click();
        sortDropdown.findElement(By.cssSelector("option[value='" + value + "']")).click();
    }

    private double extractPrice(WebElement priceElement) {
        return Double.parseDouble(priceElement.getText().replace("$", ""));
    }

    private void testExternalLink(String linkId, String expectedDomain) {
        String originalWindow = driver.getWindowHandle();
        driver.findElement(By.className(linkId)).click();
        
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
}