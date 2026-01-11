package SunaDeepSeek.ws06.seq09;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RestfullBooker {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://automationintesting.online/";

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
    public void testHomePage() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.titleContains("Restful-booker-platform demo"));
        
        // Verify main elements
        Assertions.assertTrue(driver.findElement(By.cssSelector(".navbar-brand")).isDisplayed());
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("roomName")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("roomType")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("roomPrice")));
    }

    @Test
    @Order(2)
    public void testRoomBookingPage() {
        driver.get(BASE_URL + "#/");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("firstname")));
        WebElement bookButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(".btn.btn-outline-primary.book-room")));
        bookButton.click();
        
        wait.until(ExpectedConditions.urlContains("booking"));
        Assertions.assertTrue(driver.findElement(By.id("firstname")).isDisplayed());
        Assertions.assertTrue(driver.findElement(By.id("lastname")).isDisplayed());
        Assertions.assertTrue(driver.findElement(By.id("totalprice")).isDisplayed());
    }

    @Test
    @Order(3)
    public void testAdminLoginPage() {
        driver.get(BASE_URL + "#/admin");
        wait.until(ExpectedConditions.urlContains("admin"));
        
        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("doLogin"));
        
        // Test invalid login
        usernameField.sendKeys("invalid");
        passwordField.sendKeys("invalid");
        loginButton.click();
        
        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".alert.alert-danger")));
        Assertions.assertTrue(errorMessage.getText().contains("Bad credentials"));
    }

    @Test
    @Order(4)
    public void testValidAdminLogin() {
        driver.get(BASE_URL + "#/admin");
        
        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.id("username")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("doLogin"));
        
        // Valid login
        usernameField.sendKeys("admin");
        passwordField.sendKeys("password");
        loginButton.click();
        
        wait.until(ExpectedConditions.urlContains("admin"));
        Assertions.assertTrue(driver.findElement(By.id("roomName")).isDisplayed());
    }

    @Test
    @Order(5)
    public void testFooterLinks() {
        driver.get(BASE_URL);
        
        // Test Twitter link
        testExternalLink(By.cssSelector("a[href*='twitter.com']"), "twitter.com");
        
        // Test Facebook link
        testExternalLink(By.cssSelector("a[href*='facebook.com']"), "facebook.com");
        
        // Test LinkedIn link
        testExternalLink(By.cssSelector("a[href*='linkedin.com']"), "linkedin.com");
    }

    private void testExternalLink(By locator, String expectedDomain) {
        String originalWindow = driver.getWindowHandle();
        
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(locator));
        link.click();
        
        // Switch to new window
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.equals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        // Verify domain and close
        Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain));
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(6)
    public void testNavigationMenu() {
        driver.get(BASE_URL);
        
        // Open menu
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(".navbar-toggler-icon")));
        menuButton.click();
        
        // Verify menu items
        List<WebElement> menuItems = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(
            By.cssSelector(".navbar-nav .nav-item")));
        Assertions.assertTrue(menuItems.size() > 0);
        
        // Close menu
        menuButton.click();
        wait.until(ExpectedConditions.invisibilityOf(menuItems.get(0)));
    }

    @Test
    @Order(7)
    public void testRoomCreation() {
        driver.get(BASE_URL + "#/admin");
        wait.until(ExpectedConditions.urlContains("admin"));
        
        // Login if not already logged in
        if (driver.findElements(By.id("username")).size() > 0) {
            driver.findElement(By.id("username")).sendKeys("admin");
            driver.findElement(By.id("password")).sendKeys("password");
            driver.findElement(By.id("doLogin")).click();
            wait.until(ExpectedConditions.urlContains("admin"));
        }
        
        // Create a new room
        WebElement createButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(".btn.btn-primary")));
        createButton.click();
        
        WebElement roomName = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.id("roomName")));
        roomName.sendKeys("Test Room");
        
        driver.findElement(By.id("type")).sendKeys("Single");
        driver.findElement(By.id("accessible")).sendKeys("true");
        driver.findElement(By.id("roomPrice")).sendKeys("100");
        driver.findElement(By.cssSelector("[type='submit']")).click();
        
        // Verify room was created
        WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".alert.alert-success")));
        Assertions.assertTrue(successMessage.getText().contains("created"));
    }
}