package SunaDeepSeek.ws01.seq02;

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
        driver.findElement(By.id("user-name")).sendKeys("invalid_user");
        driver.findElement(By.id("password")).sendKeys("wrong_password");
        driver.findElement(By.id("login-button")).click();
        
        WebElement errorElement = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("h3[data-test='error']")));
        Assertions.assertTrue(errorElement.getText().contains("Username and password do not match"));
    }

    @Test
    @Order(3)
    public void testProductSorting() {
        login();
        
        // Test A-Z sorting
        selectSortOption("az");
        List<WebElement> items = driver.findElements(By.className("inventory_item_name"));
        Assertions.assertTrue(items.get(0).getText().startsWith("Sauce Labs Backpack"));
        
        // Test Z-A sorting
        selectSortOption("za");
        items = driver.findElements(By.className("inventory_item_name"));
        Assertions.assertTrue(items.get(0).getText().startsWith("Test.allTheThings() T-Shirt (Red)"));
        
        // Test low to high price
        selectSortOption("lohi");
        items = driver.findElements(By.className("inventory_item_price"));
        Assertions.assertTrue(items.get(0).getText().startsWith("$7.99"));
        
        // Test high to low price
        selectSortOption("hilo");
        items = driver.findElements(By.className("inventory_item_price"));
        Assertions.assertTrue(items.get(0).getText().startsWith("$49.99"));
    }

    @Test
    @Order(4)
    public void testMenuNavigation() {
        login();
        
        // Close menu if open
        List<WebElement> closeButtons = driver.findElements(By.className("bm-cross-button"));
        if (!closeButtons.isEmpty() && closeButtons.get(0).isDisplayed()) {
            closeButtons.get(0).click();
        }
        
        // Open menu
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.id("react-burger-menu-btn")));
        try {
            menuButton.click();
        } catch (ElementClickInterceptedException e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", menuButton);
        }
        
        // Test All Items
        WebElement allItems = wait.until(ExpectedConditions.elementToBeClickable(
            By.id("inventory_sidebar_link")));
        allItems.click();
        Assertions.assertTrue(driver.getCurrentUrl().contains("inventory.html"));
        
        // Test About (external)
        menuButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.id("react-burger-menu-btn")));
        menuButton.click();
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
        
        // Test Logout
        menuButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.id("react-burger-menu-btn")));
        menuButton.click();
        WebElement logout = wait.until(ExpectedConditions.elementToBeClickable(
            By.id("logout_sidebar_link")));
        logout.click();
        Assertions.assertTrue(driver.getCurrentUrl().contains("index.html"));
        
        // Login again for remaining tests
        login();
    }

    @Test
    @Order(5)
    public void testSocialLinks() {
        login();
        
        // Test Twitter link
        testExternalLink("social_twitter", "twitter.com");
        
        // Test Facebook link
        testExternalLink("social_facebook", "facebook.com");
        
        // Test LinkedIn link
        testExternalLink("social_linkedin", "linkedin.com");
    }

    @Test
    @Order(6)
    public void testResetAppState() {
        login();
        
        // Add item to cart
        WebElement addToCart = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(".btn_primary.btn_inventory")));
        addToCart.click();
        
        // Verify cart has item
        WebElement cartBadge = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.className("shopping_cart_badge")));
        Assertions.assertEquals("1", cartBadge.getText());
        
        // Reset app state
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.id("react-burger-menu-btn")));
        menuButton.click();
        
        WebElement reset = wait.until(ExpectedConditions.elementToBeClickable(
            By.id("reset_sidebar_link")));
        reset.click();
        
        // Verify cart is empty
        List<WebElement> badges = driver.findElements(By.className("shopping_cart_badge"));
        Assertions.assertEquals(0, badges.size());
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
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        WebElement sortDropdown = wait.until(ExpectedConditions.elementToBeClickable(
            By.className("product_sort_container")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].value = arguments[1]; arguments[0].dispatchEvent(new Event('change'));", sortDropdown, value);
        
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void testExternalLink(String linkClass, String expectedDomain) {
        String originalWindow = driver.getWindowHandle();
        
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(
            By.className(linkClass)));
        link.click();
        
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